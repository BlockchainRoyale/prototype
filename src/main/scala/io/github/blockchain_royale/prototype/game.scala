package io.github.blockchain_royale.prototype

import scala.util.Random

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
  val MAP_SIZE = 5
  val TOTAL_OBJECTS = 9
  val ARENA_SIZE = 5
}

import Types._
import Consts._
import org.json.JSONArray
import org.json.JSONML
import org.json.JSONTokener
import org.json.JSONObject
import org.apache.commons.codec.digest.DigestUtils

case class Player(id: PlayerId, name: String)
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
}
case class GenesisAct() extends GameAction { val player = -1 }
case class NewPlayerAct(player: PlayerId, name: String) extends GameAction
case class PlayerMoveAct(player: PlayerId, dir: String) extends GameAction
case class PlayerPickAct(player: PlayerId, obj: ObjectId) extends GameAction
case class PlayerStartAttackAct(player: PlayerId, other: PlayerId, actionsHash: String) extends GameAction
case class PlayerFinishAttackAct(player: PlayerId, other: PlayerId, actionsString: String) extends GameAction {
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
case class CombatAct(a: PlayerId, b: PlayerId, startA: Coord, startB: Coord, actionsA: String, actionsB: String, outcome: Outcome) extends GameAction {
  val player = -1
}

case class Game(id: GameId,
                players: Map[PlayerId, Player] = Map(),
                objects: Map[ObjectId, Object] = Map(),
                holding: Map[PlayerId, List[ObjectId]] = Map(),
                stats: Map[PlayerId, Stats] = Map(),
                map: GameMap,
                chain: List[GameAction]) {
  def start: Game = {

    var objectCount = 0
    var givenWeapons: Map[PlayerId, ObjectId] = Map()
    var newObjects: Map[ObjectId, Object] = Map()
    var newHolding: Map[PlayerId, List[ObjectId]] = Map()

    // given guns
    def newObject(objectId: ObjectId) = Object(objectId, ALL_OBJECTS(Random.nextInt(ALL_OBJECTS.size)))
    for (_id <- players.keys) {
      val objectId = objectCount
      objectCount += 1
      val weapon = newObject(objectId)
      givenWeapons += (_id -> objectId)
      newObjects += (_id -> weapon)
      newHolding += (_id -> List(objectId))
    }

    // drop objects
    var objectsAtRoom: Map[Coord, List[ObjectId]] = Map()
    for (_ <- 1.to(TOTAL_OBJECTS - NUM_PLAYERS)) {
      val coord = Tuple2(Random.nextInt(MAP_SIZE), Random.nextInt(MAP_SIZE))
      val objectId = objectCount
      objectCount += 1
      objectsAtRoom += (coord -> (objectsAtRoom.getOrElse(coord, List()) ++ List(objectId)))
      newObjects += objectId -> newObject(objectId)
    }

    // pick initial position for players
    var initialPos: Map[Coord, List[PlayerId]] = Map()
    for (_id <- players.keys) {
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

    this.copy(objects = newObjects,
      map = GameMap(rooms.values.toArray),
      holding = newHolding,
      stats = players.mapValues(v => Stats(true, List())))
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
        holdArray.put(game.objects(holdId)._type.name)
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
      actionArray.put(act.toString)
    }
    obj.put("players", playerArray)
    obj.put("rooms", rows)
    obj.put("actions", actionArray)

    obj.toString
  }

  def hash(str: String): String = DigestUtils.sha1Hex(str)

  def combat(a: PlayerFinishAttackAct, b: PlayerFinishAttackAct): Tuple3[Coord, Coord, Outcome] = {
    ((0, 0), (1, 1), Outcome(List(a.player, b.player), List()))
  }
}
