package io.github.blockchain_royale.prototype

import org.scalatra.ScalatraServlet
import org.json.JSONObject
import org.json.JSONArray

class GameServlet extends ScalatraServlet {

  private var games: Map[Int, Game] = Map[Int, Game]()

  import Types._

  get("/") {

  }

  post("/api/new_game") {
    games.synchronized {
      val newId = games.size + 1
      games = games + (newId -> Game(newId, map = GameMap(Array())))
      newId.toString
    }
  }

  post("/api/add_player/:game/named/:name") {
    games.synchronized {
      val game = games(params("game").toInt)
      val newPlayer = Player(game.players.size + 1, params("name"))
      val newGame = game.copy(players = game.players + (newPlayer.id -> newPlayer))

      val potentiallyStartedGame =
        if (newGame.players.size == 6)
          newGame.start
        else
          newGame

      games = games + (game.id -> potentiallyStartedGame)
      newPlayer.id.toString
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

        if (!destRoom.open) {
          games += gameId -> game.copy(stats = game.stats + (playerId -> (game.stats(playerId).copy(alive = false))))
        }
        newCoord
      } else {
        "Dead players tell no lies"
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
          game.map.rooms(roomIdx) = room.copy(objects = room.objects.filterNot(_ == objectId))
          games += gameId -> game.copy(holding = game.holding + (playerId -> (game.holding(playerId) ++ List (objectId))))
          objectId
        } else {
          "object is not there"
        }
      } else {
        "Dead players tell no lies"
      }
    }
  }

  get("/api/state/:game") {
    games.synchronized {
      val game = games(params("game").toInt)

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
      obj.put("players", playerArray)
      obj.put("rooms", rows)

      obj.toString
    }
  }
}