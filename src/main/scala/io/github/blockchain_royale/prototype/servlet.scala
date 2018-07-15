package io.github.blockchain_royale.prototype

import org.scalatra.ScalatraServlet
import org.json.JSONObject
import org.json.JSONArray

class GameServlet extends ScalatraServlet {

  private var games: Map[Int, Game] = Map[Int, Game]()

  import Types._
  import Consts._

  get("/") {

  }

  post("/api/new_game") {
    games.synchronized {
      val newId = games.size + 1
      games = games + (newId -> GameLogic.newGame(newId))
      newId.toString
    }
  }

  post("/api/add_player/:game/named/:name") {
    games.synchronized {
      val game = games(params("game").toInt)
      val newPlayer = Player(game.players.size + 1, params("name"))
      val newGame = game.copy(players = game.players + (newPlayer.id -> newPlayer))
      //TODO read public key from stdin

      val (potentiallyStartedGame, started) =
        if (newGame.players.size == NUM_PLAYERS)
          (GameLogic.startGame(newGame), true)
        else
          (newGame, false)

      games = games + (game.id -> potentiallyStartedGame.copy(chain = NewPlayerGA(newPlayer.id, newPlayer.name) :: potentiallyStartedGame.chain))
      s"${newPlayer.id.toString} $started"
    }
  }

  post("/api/move/:game/:id/:direction") {
    games.synchronized {
      val gameId = params("game").toInt
      val game = games(gameId)
      val playerId = params("id").toInt
      val stats = game.stats(playerId)
      if (stats.alive) {
        val (room, roomIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(playerId)).get
        val newCoord = params("direction") match {
          case "N" => room.location.copy(_1 = (room.location._1 - 1 + 5) % 5)
          case "S" => room.location.copy(_1 = (room.location._1 + 1) % 5)
          case "W" => room.location.copy(_2 = (room.location._2 - 1 + 5) % 5)
          case "E" => room.location.copy(_2 = (room.location._2 + 1) % 5)
        }
        val (destRoom, destRoomIdx) = game.map.rooms.zipWithIndex.find(_._1.location == newCoord).get
        game.map.rooms(roomIdx) = room.copy(players = room.players.filterNot(_ == playerId))
        game.map.rooms(destRoomIdx) = destRoom.copy(players = destRoom.players ++ List(playerId))

        val potentiallyKilledPlayer = if (!destRoom.open) game.copy(stats = game.stats + (playerId -> (game.stats(playerId).copy(alive = false)))) else game

        games += gameId -> potentiallyKilledPlayer.copy(chain = PlayerMoveGA(playerId, params("direction")) :: game.chain)
        newCoord
      } else {
        "400 Dead players tell no lies"
      }
    }
  }

  post("/api/pick/:game/:player/:id") {
    games.synchronized {
      val gameId = params("game").toInt
      val game = games(gameId)
      val playerId = params("player").toInt
      val objectId = params("id").toInt
      val stats = game.stats(playerId)
      if (stats.alive) {
        val (room, roomIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(playerId)).get
        if (room.objects.contains(objectId)) {
          game.map.rooms(roomIdx) = room.copy(objects = room.objects.filter(_ != objectId))
          games += (gameId -> game.copy(
            holding = game.holding + (playerId -> (game.holding(playerId) ++ List(objectId))),
            chain = PlayerPickGA(playerId, objectId) :: game.chain))
          s"$objectId"
        } else {
          "400 object is not there"
        }
      } else {
        "400 Dead players tell no lies"
      }
    }
  }

  post("/api/attack/:game/:player/start/:other") {
    games.synchronized {
      val gameId = params("game").toInt
      val game = games(gameId)
      val playerId = params("player").toInt
      val otherId = params("other").toInt
      val actionsHash = request.body
      val statsOwn = game.stats(playerId)
      val statsOther = game.stats(playerId)
      if (statsOwn.alive) {
        if (statsOther.alive) {
          val (roomOwn, roomOwnIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(playerId)).get
          val (roomOther, roomOtherIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(otherId)).get
          if (roomOwnIdx != roomOtherIdx) {
            games += gameId -> game.copy(chain = PlayerStartAttackGA(playerId, otherId, actionsHash) :: game.chain)
            actionsHash
          } else {
            "400 player is not there"
          }
        } else {
          "400 It's dead Jim, it's dead"
        }
      } else {
        "400 Dead players tell no lies"
      }
    }
  }

  post("/api/attack/:game/:player/execute/:other") {
    games.synchronized {
      val gameId = params("game").toInt
      val game = games(gameId)
      val playerId = params("player").toInt
      val otherId = params("other").toInt
      val actionsJson = request.body
      val statsOwn = game.stats(playerId)
      val statsOther = game.stats(playerId)
      if (actionsJson.length() > 512) {
        "400 action string too long (don't cheat!)"
      } else if (!statsOwn.alive) {
        "400 Dead players tell no lies"
      } else if (!statsOther.alive) {
        "400 It's dead Jim, it's dead"
      } else {
        val (roomOwn, roomOwnIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(playerId)).get
        val (roomOther, roomOtherIdx) = game.map.rooms.zipWithIndex.find(_._1.players.contains(otherId)).get
        if (roomOwnIdx != roomOtherIdx) {
          "400 player is not there"
        } else {
          val start = game.chain.find(act => act match {
            case PlayerStartAttackGA(p, o, _) => p == playerId && o == otherId
            case _                             => false
          }).map(_.asInstanceOf[PlayerStartAttackGA])
          if (!start.isDefined) {
            "400 you never started the attack"
          } else if (!(start.get.actionsHash.equals(GameUtils.hash(actionsJson)))) {
            "400 hashes do not match! cheater!"
          } else {
            val act = PlayerFinishAttackGA(playerId, otherId, actionsJson)
            val gameChained = game.copy(chain = act :: game.chain)
            val otherStart = game.chain.find(act => act match {
              case PlayerStartAttackGA(p, o, _) => p == otherId && o == playerId
              case _                             => false
            }).map(_.asInstanceOf[PlayerStartAttackGA])
            val otherAttack = game.chain.find(act => act match {
              case PlayerFinishAttackGA(p, o, _) => p == otherId && o == playerId
              case _                              => false
            }).map(_.asInstanceOf[PlayerFinishAttackGA])
            val (possiblyCombatChained, msg) = if (otherStart.isDefined && otherAttack.isDefined && otherAttack.get.timestamp > otherStart.get.timestamp) {
              // execute combat
              val (startA, startB, outcome) = GameLogic.combat(act, otherAttack.get, game.objects)
              val combatChained = gameChained.copy(chain =
                CombatGA(playerId, otherId, startA, startB, actionsJson, otherAttack.get.actionsString, outcome) :: gameChained.chain)

              if (outcome.dead.size > 0) {
                // process deaths
                if (outcome.dead.size == 1) {
                  val deadId = outcome.dead(0)
                  val aliveId = outcome.alive(0)
                  val oneDead = combatChained.copy(
                    stats = combatChained.stats +
                      (deadId -> combatChained.stats(playerId).copy(alive = false)) +
                      (aliveId -> (combatChained.stats(otherId).copy(
                        kills = deadId :: combatChained.stats(otherId).kills))),
                    holding = combatChained.holding + (aliveId -> (combatChained.holding(aliveId) ++ combatChained.holding(deadId))) + (deadId -> List()))

                  (oneDead, "200  " + game.players(deadId).name + "died")
                } else { // both died
                  val bothDied = combatChained.copy(
                    stats = combatChained.stats +
                      (playerId -> (combatChained.stats(playerId).copy(alive = false,
                        kills = otherId :: combatChained.stats(playerId).kills))) +
                        (otherId -> (combatChained.stats(otherId).copy(alive = false,
                          kills = playerId :: combatChained.stats(otherId).kills))),
                    holding = combatChained.holding + (playerId -> List()) + (otherId -> List()))
                  val room = bothDied.map.rooms(roomOwnIdx)
                  bothDied.map.rooms(roomOwnIdx) = room.copy(objects = room.objects ++ combatChained.holding(playerId) ++ combatChained.holding(otherId))

                  (bothDied, "200 both died")
                }
              } else (combatChained, "200 nobody died")
            } else (gameChained, "200 awaiting other player")

            games += gameId -> possiblyCombatChained
            if (possiblyCombatChained.stats.values.filter(_.alive).size <= 1)
              "200 finished"
            else
              msg
          }
        }
      }
    }
  }

  get("/api/state/:game") {
    games.synchronized {
      val game = games(params("game").toInt)
      GameUtils.toJson(game)
    }
  }
}