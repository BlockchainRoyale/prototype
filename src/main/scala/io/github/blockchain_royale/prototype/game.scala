package io.github.blockchain_royale.prototype

import scala.util.Random
import org.json.JSONArray
import org.json.JSONML
import org.json.JSONTokener
import org.json.JSONObject
import org.apache.commons.codec.digest.DigestUtils

sealed abstract class ObjectType {
  val name: String
}

object Gun extends ObjectType {
  val name = "Gun"
}

object Grenade extends ObjectType {
  val name = "Grenade"
}

object Shield extends ObjectType {
  val name = "Shield"
}

object Types {
  type PlayerId = Int
  type ObjectId = Int
  type GameId = Int
  type Coord = Tuple2[Int, Int]
}

object Consts {
  val ALL_OBJECTS = List(Gun, Grenade, Shield).toArray
  val NUM_PLAYERS = 4
  val MAP_SIZE = 7
  val TOTAL_OBJECTS = 9
  val ARENA_SIZE = 5
}

import Types._
import Consts._

case class Player(id: PlayerId, name: String, pass: String)
case class Object(id: ObjectId, _type: ObjectType)
case class Room(location: Coord, objects: List[ObjectId], players: List[PlayerId], open: Boolean)
case class Stats(alive: Boolean, kills: List[PlayerId])
case class GameMap(rooms: Array[Room])

// player actions, for combat

sealed abstract class PlayerAction
case class MovePA(dir: Tuple2[Int, Int]) extends PlayerAction
case class ShootPA(dir: Tuple2[Int, Int], obj: ObjectId) extends PlayerAction
case class TurnTowardsPlayerPA() extends PlayerAction
case class MoveForwardPA() extends PlayerAction
case class MoveBackwardPA() extends PlayerAction
case class TurnLeftPA() extends PlayerAction
case class TurnRightPA() extends PlayerAction
case class UsePA(obj: ObjectId) extends PlayerAction

case class Outcome(alive: List[PlayerId], dead: List[PlayerId])

// game actions, for the chain
sealed abstract class GameAction {
  val timestamp = System.currentTimeMillis()
  val player: PlayerId
  def toJsonObject(): JSONObject = {
    val obj = new JSONObject()
    obj.put("type", this.getClass.getSimpleName)
    obj.put("timestamp", timestamp)
    extra(obj)
    obj
  }
  def extra(obj: JSONObject): Unit
  def toJson(): String = toJsonObject().toString
}
case class GenesisGA() extends GameAction {
  val player = -1
  def extra(obj: JSONObject) = {}
}
case class NewPlayerGA(player: PlayerId, name: String) extends GameAction {
  def extra(obj: JSONObject) = { obj.put("player", player); obj.put("name", name) }
}
case class PlayerMoveGA(player: PlayerId, dir: String) extends GameAction {
  def extra(obj: JSONObject) = { obj.put("player", player); obj.put("dir", dir) }
}
case class PlayerPickGA(player: PlayerId, obj: ObjectId) extends GameAction {
  def extra(obj: JSONObject) = { obj.put("player", player); obj.put("obj", obj) }
}
case class PlayerStartAttackGA(player: PlayerId, other: PlayerId, actionsHash: String) extends GameAction {
  def extra(obj: JSONObject) = { obj.put("player", player); obj.put("other", other); obj.put("hash", actionsHash); }
}
case class PlayerFinishAttackGA(player: PlayerId, other: PlayerId, actionsString: String) extends GameAction {
  def extra(obj: JSONObject) = { obj.put("player", player); obj.put("other", other); obj.put("actions", actionsString); }

  private var cache: List[PlayerAction] = null
  def actions: List[PlayerAction] = {
    if (cache == null) {
      val parser = new JSONTokener(actionsString)
      val top = new JSONArray(parser)
      cache = (for (idx <- 0.to(top.length() - 1)) yield {
        val obj = top.getJSONObject(idx)
        obj.getString("type") match {
          case "MoveNorth"         => MovePA((-1, 0))
          case "MoveSouth"         => MovePA((1, 0))
          case "MoveWest"          => MovePA((0, -1))
          case "MoveEast"          => MovePA((0, 1))
          case "ShootNorth"        => ShootPA((-1, 0), obj.getInt("object"))
          case "ShootSouth"        => ShootPA((1, 0), obj.getInt("object"))
          case "ShootWest"         => ShootPA((0, -1), obj.getInt("object"))
          case "ShootEast"         => ShootPA((0, 1), obj.getInt("object"))
          case "TurnTowardsPlayer" => TurnTowardsPlayerPA()
          case "MoveForward"       => MoveForwardPA()
          case "MoveBackward"      => MoveBackwardPA()
          case "TurnLeft"          => TurnLeftPA()
          case "TurnRight"         => TurnRightPA()
          case "UsePA"             => UsePA(obj.getInt("object"))
        }
      }).toList
    }
    cache
  }
}
case class CombatGA(a: PlayerId, b: PlayerId, startA: Coord, startB: Coord, actionsA: String, actionsB: String, outcome: Outcome) extends GameAction {
  val player = -1
  def extra(obj: JSONObject) = {
    obj.put("playerA", a)
    obj.put("playeB", b)
    obj.put("startA_row", startA._1)
    obj.put("startA_col", startA._2)
    obj.put("startB_row", startB._1)
    obj.put("startB_col", startB._2)
    obj.put("outcome", outcome)
  }
}

case class Game(id: GameId,
                players: Map[PlayerId, Player] = Map(),
                objects: Map[ObjectId, Object] = Map(),
                holding: Map[PlayerId, List[ObjectId]] = Map(),
                stats: Map[PlayerId, Stats] = Map(),
                map: GameMap,
                chain: List[GameAction])

object GameLogic {

  def newGame(id: Int): Game = Game(id, map = GameMap(Array()), chain = List(GenesisGA()))

  def startGame(game: Game): Game = {
    var objectCount = 0
    var newObjects: Map[ObjectId, Object] = Map()
    var newHolding: Map[PlayerId, List[ObjectId]] = Map()

    // given guns
    def newObject(objectId: ObjectId) = Object(objectId, ALL_OBJECTS(Random.nextInt(ALL_OBJECTS.size)))
    for (_id <- game.players.keys) {
      val objectId = objectCount
      objectCount += 1
      newObjects += (objectId -> newObject(objectId))
      newHolding += (_id -> List(objectId))
    }

    // drop objects
    var objectsAtRoom: Map[Coord, List[ObjectId]] = Map()
    for (_ <- 1.to(TOTAL_OBJECTS - NUM_PLAYERS)) {
      val coord = Tuple2(Random.nextInt(MAP_SIZE), Random.nextInt(MAP_SIZE))
      val objectId = objectCount
      objectCount += 1
      objectsAtRoom += (coord -> (objectsAtRoom.getOrElse(coord, List()) ++ List(objectId)))
      newObjects += (objectId -> newObject(objectId))
    }

    // pick initial position for players
    var initialPos: Map[Coord, List[PlayerId]] = Map()
    for (_id <- game.players.keys) {
      val coord = Tuple2(Random.nextInt(MAP_SIZE), Random.nextInt(5))
      initialPos += (coord -> (initialPos.getOrElse(coord, List()) ++ List(_id)))
    }

    // build map
    var rooms: Map[Coord, Room] = Map()
    for (x <- 0.to(MAP_SIZE - 1)) {
      for (y <- 0.to(MAP_SIZE - 1)) {
        val coord = Tuple2(x, y)
        val room = Room(coord, objectsAtRoom.getOrElse(coord, List()), initialPos.getOrElse(coord, List()), true)
        rooms += coord -> room
      }
    }

    game.copy(
      objects = newObjects,
      map = GameMap(rooms.values.toArray),
      holding = newHolding,
      stats = game.players.mapValues(v => Stats(true, List())))
  }

  def newPlayer(game: Game, name: String, pass: String): (Game, PlayerId, Boolean) = {
    val newPlayer = Player(game.players.size + 1, name, pass)
    val newGame = game.copy(players = game.players + (newPlayer.id -> newPlayer))

    val (potentiallyStartedGame, started) =
      if (newGame.players.size == NUM_PLAYERS)
        (GameLogic.startGame(newGame), true)
      else
        (newGame, false)
    (potentiallyStartedGame.copy(chain = NewPlayerGA(newPlayer.id, newPlayer.name) :: potentiallyStartedGame.chain), newPlayer.id, started)
  }

  def move(game: Game, player: Player, dir: String): (Game, Coord, Boolean) = {
    val (room, roomIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(player.id)).get
    val newCoord = dir match {
      case "N" => room.location.copy(_1 = (room.location._1 - 1 + 5) % 5)
      case "S" => room.location.copy(_1 = (room.location._1 + 1) % 5)
      case "W" => room.location.copy(_2 = (room.location._2 - 1 + 5) % 5)
      case "E" => room.location.copy(_2 = (room.location._2 + 1) % 5)
    }
    val (destRoom, destRoomIdx) = game.map.rooms.zipWithIndex.find(_._1.location == newCoord).get
    game.map.rooms(roomIdx) = room.copy(players = room.players.filterNot(_ == player.id))
    game.map.rooms(destRoomIdx) = destRoom.copy(players = destRoom.players ++ List(player.id))

    val (potentiallyKilledPlayer, killed) = if (!destRoom.open)
      (game.copy(stats = game.stats + (player.id -> (game.stats(player.id).copy(alive = false)))), true)
    else (game, false)

    (potentiallyKilledPlayer.copy(chain = PlayerMoveGA(player.id, dir) :: game.chain), newCoord, killed)
  }

  def picked(game: Game, player: Player, objectId: ObjectId): (Game, Boolean) = {
    val (room, roomIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(player.id)).get
    if (room.objects.contains(objectId)) {
      game.map.rooms(roomIdx) = room.copy(objects = room.objects.filter(_ != objectId))
      (game.copy(
        holding = game.holding + (player.id -> (game.holding(player.id) ++ List(objectId))),
        chain = PlayerPickGA(player.id, objectId) :: game.chain), true)
    } else (game, false)
  }

  def startAttack(game: Game, player: Player, other: Player, actionsHash: String): (Game, Boolean) = {
    val (roomOwn, roomOwnIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(player.id)).get
    val (roomOther, roomOtherIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(other.id)).get
    if (roomOwnIdx != roomOtherIdx) {
      (game, true)
    } else
      (game.copy(chain = PlayerStartAttackGA(player.id, other.id, actionsHash) :: game.chain), false)
  }
  def attack(game: Game, player: Player, other: Player, actionsStr: String): (Game, Boolean, Option[Outcome], String) = {
    if (actionsStr.length() > 512)
      return (game, true, None, "action string too long (don't cheat!)")

    val (roomOwn, roomOwnIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(player.id)).get
    val (roomOther, roomOtherIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(other.id)).get
    if (roomOwnIdx != roomOtherIdx)
      return (game, true, None, "player is not there")

    // player started?
    val startOwn = game.chain.find(act => act match {
      case PlayerStartAttackGA(p, o, _) => p == player.id && o == other.id
      case _                            => false
    }).map(_.asInstanceOf[PlayerStartAttackGA])

    // other started?
    val startOther = game.chain.find(act => act match {
      case PlayerStartAttackGA(p, o, _) => o == player.id && p == other.id
      case _                            => false
    }).map(_.asInstanceOf[PlayerStartAttackGA])

    // other attacked?
    val otherAttack = game.chain.find(act => act match {
      case PlayerFinishAttackGA(p, o, _) => o == player.id && p == other.id
      case _                             => false
    }).map(_.asInstanceOf[PlayerFinishAttackGA])

    if (!startOwn.isDefined && !startOther.isDefined)
      return (game, true, None, "an attack was never started")

    val ownAttack = PlayerFinishAttackGA(player.id, other.id, actionsStr)
    val gameChained = game.copy(chain = ownAttack :: game.chain)
    if (startOwn.isDefined && startOther.isDefined) {
      if (startOwn.get.timestamp < startOther.get.timestamp) {
        // other remains
        return (gameChained, false, None, "waiting for actions from initiating player")
      } else {
        // mine remains
        if (otherAttack.isDefined && otherAttack.get.timestamp > startOwn.get.timestamp) {
          // it's on! check hashes
          if (!(startOwn.get.actionsHash.equals(GameUtils.hash(actionsStr))))
            return (game, true, None, "hashes do not match! cheater!")

        } else return (game, true, None, "don't disclose your moves yet!")
      }
    } else if (startOwn.isDefined) {
      if (otherAttack.isDefined && otherAttack.get.timestamp > startOwn.get.timestamp) {
        // it's on! check hashes
        if (!(startOwn.get.actionsHash.equals(GameUtils.hash(actionsStr))))
          return (game, true, None, "hashes do not match! cheater!")
      } else return (game, true, None, "don't disclose your moves yet!")
    } else // other is defined
      return (gameChained, false, None, "waiting for actions from initiating player")

    // now fight
    val (inError, msg, startA, startB, outcome) = GameLogic.combat(game, ownAttack, otherAttack.get, game.objects)
    if (inError)
      return (game, true, None, msg)

    val combatChained = gameChained.copy(
      chain = CombatGA(player.id, other.id, startA, startB, actionsStr, otherAttack.get.actionsString, outcome) :: gameChained.chain)

    if (outcome.dead.size > 0) {
      // process deaths
      var result: (Game, Boolean, Option[Outcome], String) = null
      if (outcome.dead.size == 1) {
        val deadId = outcome.dead(0)
        val aliveId = outcome.alive(0)
        val oneDead = combatChained.copy(
          stats = combatChained.stats +
            (deadId -> combatChained.stats(player.id).copy(alive = false)) +
            (aliveId -> (combatChained.stats(other.id).copy(
              kills = deadId :: combatChained.stats(other.id).kills))),
          holding = combatChained.holding + (aliveId -> (combatChained.holding(aliveId) ++ combatChained.holding(deadId))) + (deadId -> List()))

        result = (oneDead, false, Some(outcome), game.players(deadId).name + "died")
      } else { // both died
        val bothDied = combatChained.copy(
          stats = combatChained.stats +
            (player.id -> (combatChained.stats(player.id).copy(alive = false,
              kills = other.id :: combatChained.stats(player.id).kills))) +
              (other.id -> (combatChained.stats(other.id).copy(alive = false,
                kills = player.id :: combatChained.stats(other.id).kills))),
          holding = combatChained.holding + (player.id -> List()) + (other.id -> List()))
        val room = bothDied.map.rooms(roomOwnIdx)
        bothDied.map.rooms(roomOwnIdx) = room.copy(objects = room.objects ++ combatChained.holding(player.id) ++ combatChained.holding(other.id))
        result = (bothDied, false, Some(outcome), "200 both died")
      }

      //TODO shrink map
      return if (result._1.stats.values.filter(_.alive).size <= 1) result.copy(_4 = "game finished") else result
    } else return (combatChained, false, Some(outcome), "nobody died")
  }

  def combat(game: Game, a: PlayerFinishAttackGA, b: PlayerFinishAttackGA, objects: Map[ObjectId, Object]): (Boolean, String, Coord, Coord, Outcome) = {
    //TODO validate the weapons used belong to the players

    val hashed = GameUtils.hash(a.actionsString + b.actionsString)
    val startAopt = (hashed(0).toInt % MAP_SIZE, hashed(1).toInt % MAP_SIZE)
    val startB = (hashed(2).toInt % MAP_SIZE, hashed(3).toInt % MAP_SIZE)
    // slight advantage to player 1 to entice submitting actions fast
    val startA = if (startAopt == startB) (startAopt._1, (startAopt._2 + 1) % MAP_SIZE) else startAopt
    var currentA = startA
    var currentB = startB
    var dirA: Tuple2[Int, Int] = null
    var dirB: Tuple2[Int, Int] = null
    // start facing each other
    def faceDir(person: Coord, other: Coord): Tuple2[Int, Int] = {
      val diff1 = person._1 - other._1
      val diff2 = person._2 - other._2
      if (Math.abs(diff1) > Math.abs(diff2)) {
        (Math.signum(diff1).toInt, 0)
      } else {
        (0, Math.signum(diff2).toInt)
      }
    }
    def turnFacing(dir: Tuple2[Int, Int], target: Tuple2[Int, Int]): Tuple2[Int, Int] = (dir, target) match {
      case ((-1, 0), (1, 0))  => (0, 1)
      case ((-1, 0), (0, 1))  => (-1, 0)
      case ((-1, 0), (0, -1)) => (-1, 0)
      case ((1, 0), (-1, 0))  => (0, 1)
      case ((1, 0), (0, 1))   => (1, 0)
      case ((1, 0), (0, -1))  => (1, 0)

      case ((0, -1), (1, 0))  => (0, -1)
      case ((0, -1), (0, 1))  => (1, 0)
      case ((0, -1), (-1, 0)) => (-1, 0)
      case ((0, 1), (-1, 0))  => (-1, 0)
      case ((0, 1), (1, 0))   => (1, 0)
      case ((0, 1), (0, -1))  => (1, 0)
    }

    dirA = faceDir(currentA, currentB)
    dirB = (dirA._1 * -1, dirA._2 * -1)

    val actionsA = a.actions
    val actionsB = b.actions
    for ((actA, actB) <- actionsA.zip(actionsB)) {
      var aDead = false
      var bDead = false
      var newPosA = currentA
      var newPosB = currentB

      def hasShield(act: PlayerAction) = act match {
        case UsePA(objId2) => objects(objId2)._type == Shield
        case _             => false
      }

      (if (actA.isInstanceOf[UsePA] && !hasShield(actA)) ShootPA(dirA, actA.asInstanceOf[UsePA].obj) else actA) match {
        case MovePA(dir) => newPosA = (currentA._1 + dir._1, currentA._2 + dir._2)
        case ShootPA(dir, objId) =>
          val objType = objects(objId)._type
          objType match {
            case Gun | Grenade => if (dir._1 == 0) {
              if (currentA._1 == currentB._1) {
                if (Math.signum(currentA._2 - currentB._2).toInt == dir._2) {
                  if (hasShield(actB) && objType != Grenade) { /* blocked! */ } else bDead = true
                }
              } else { /* noop*/ }
            } else { // dir._2 == 0
              if (currentA._2 == currentB._2) {
                if (Math.signum(currentA._1 - currentB._1).toInt == dir._1) {
                  if (hasShield(actB) && objType != Grenade) { /* blocked! */ } else bDead = true
                }
              } else { /* noop*/ }
            }
            // TODO: grenade, with radious
            case _ => // noop
          }
        case TurnTowardsPlayerPA() =>
          val targetDir = faceDir(currentA, currentB)
          if (targetDir == dirA) {
            // noop
          } else {
            dirA = turnFacing(dirA, targetDir)
          }
        case MoveForwardPA()  => newPosA = ((currentA._1 + dirA._1 + MAP_SIZE) % MAP_SIZE, (currentA._2 + dirA._2 + MAP_SIZE) % MAP_SIZE)
        case MoveBackwardPA() => newPosA = ((currentA._1 - dirA._1 + MAP_SIZE) % MAP_SIZE, (currentA._2 - dirA._2 + MAP_SIZE) % MAP_SIZE)
        case TurnLeftPA() => dirA = dirA match {
          case (1, 0)  => (0, -1)
          case (0, -1) => (-1, 0)
          case (-1, 0) => (0, 1)
          case (0, 1)  => (1, 0)
        }
        case TurnRightPA() => dirA = dirA match {
          case (1, 0)  => (0, 1)
          case (0, 1)  => (-1, 0)
          case (-1, 0) => (0, -1)
          case (0, -1) => (1, 0)
        }
        case UsePA(obj) => // noop
      }

      (if (actB.isInstanceOf[UsePA] && !hasShield(actB)) ShootPA(dirB, actB.asInstanceOf[UsePA].obj) else actB) match {
        case MovePA(dir) => newPosB = (currentB._1 + dir._1, currentB._2 + dir._2)
        case ShootPA(dir, objId) =>
          val objType = objects(objId)._type
          objType match {
            case Gun | Grenade => if (dir._1 == 0) {
              if (currentB._1 == currentA._1) {
                if (Math.signum(currentB._2 - currentA._2).toInt == dir._2) {
                  if (hasShield(actA) && objType != Grenade) { /* blocked! */ } else aDead = true
                }
              } else { /* noop*/ }
            } else { // dir._2 == 0
              if (currentB._2 == currentA._2) {
                if (Math.signum(currentB._1 - currentA._1).toInt == dir._1) {
                  if (hasShield(actA) && objType != Grenade) { /* blocked! */ } else aDead = true
                }
              } else { /* noop*/ }
            }
            // TODO: grenade, with radious
            case _ => // noop
          }
        case TurnTowardsPlayerPA() =>
          val targetDir = faceDir(currentB, currentA)
          if (targetDir == dirB) {
            // noop
          } else {
            dirB = turnFacing(dirB, targetDir)
          }
        case MoveForwardPA()  => newPosB = ((currentB._1 + dirB._1 + MAP_SIZE) % MAP_SIZE, (currentB._2 + dirB._2 + MAP_SIZE) % MAP_SIZE)
        case MoveBackwardPA() => newPosB = ((currentB._1 - dirB._1 + MAP_SIZE) % MAP_SIZE, (currentB._2 - dirB._2 + MAP_SIZE) % MAP_SIZE)
        case TurnLeftPA() => dirB = dirB match {
          case (1, 0)  => (0, -1)
          case (0, -1) => (-1, 0)
          case (-1, 0) => (0, 1)
          case (0, 1)  => (1, 0)
        }
        case TurnRightPA() => dirB = dirB match {
          case (1, 0)  => (0, 1)
          case (0, 1)  => (-1, 0)
          case (-1, 0) => (0, -1)
          case (0, -1) => (1, 0)
        }
        case UsePA(obj) => // noop
      }
      if (aDead || bDead)
        return (false, "", startA, startB,
          if (aDead && bDead)
            Outcome(List(), List(a.player, b.player))
          else if (aDead)
            Outcome(List(b.player), List(a.player))
          else
            Outcome(List(a.player), List(b.player)))
      currentA = newPosA
      currentB = newPosB
    }

    (false, "", startA, startB, Outcome(List(a.player, b.player), List()))
  }
}

object GameUtils {

  def toJson(game: Game) = {
    val obj = new JSONObject()
    var players: Map[PlayerId, JSONObject] = Map()
    for (player <- game.players.values) {
      val playerObj = new JSONObject()
      val stats = game.stats(player.id)
      playerObj.put("id", player.id)
      playerObj.put("name", player.name)
      playerObj.put("alive", stats.alive)
      val killsArray = new JSONArray()
      for (killed <- stats.kills) {
        killsArray.put(killed)
      }
      playerObj.put("kills", killsArray)
      val holdArray = new JSONArray()
      for (holdId <- game.holding(player.id)) {
        val objObj = new JSONObject()
        objObj.put("id", holdId)
        objObj.put("type", game.objects(holdId)._type.name)
        holdArray.put(objObj)
      }
      playerObj.put("holds", holdArray)

      players += player.id -> playerObj
    }

    val rows = new JSONArray()
    for (r <- 0.to(4)) {
      val row = new JSONArray()
      for (c <- 0.to(4)) {
        val roomObj = new JSONObject()
        roomObj.put("row", r)
        roomObj.put("col", c)
        val playersInRoom = new JSONArray()
        val room = game.map.rooms.find(_.location == Tuple2(r, c)).get
        for (player <- room.players) {
          players(player).put("row", r)
          players(player).put("col", c)
          playersInRoom.put(player)
        }
        val objectsInRoom = new JSONArray()
        for (obj <- room.objects) {
          val objObj = new JSONObject()
          objObj.put("id", obj)
          objObj.put("type", game.objects(obj)._type.name)
          objectsInRoom.put(objObj)
        }
        roomObj.put("players", playersInRoom)
        roomObj.put("objects", objectsInRoom)
        roomObj.put("active", room.open)
        row.put(roomObj)
      }
      rows.put(row)
    }

    val playerArray = new JSONArray()
    for (playerObj <- players.values) {
      playerArray.put(playerObj)
    }

    val actionArray = new JSONArray()
    for (act <- game.chain) {
      actionArray.put(act.toJsonObject())
    }
    obj.put("players", playerArray)
    obj.put("rooms", rows)
    obj.put("actions", actionArray)

    obj.toString
  }

  def hash(str: String): String = DigestUtils.sha1Hex(str)
}
