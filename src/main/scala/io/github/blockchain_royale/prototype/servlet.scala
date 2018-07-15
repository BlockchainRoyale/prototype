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
      val result = new JSONObject()
      result.put("status", 200)
      result.put("game_id", newId)
      result.put("msg", "new game successfully created")
      result.toString
    }
  }

  def withGame(func: Game => Any): Any = {
    games.synchronized {
      val result = new JSONObject()
      result.put("status", 400)
      if (!params.contains("game")) {
        result.put("msg", "must specify a game")
        result.toString
      } else {
        val gameIdStr = params("game")
        if (gameIdStr.isEmpty()) {
          result.put("status", 400)
          result.put("msg", "must specify a game")
          result.toString
        } else if (gameIdStr.matches("[^0-9]")) {
          result.put("msg", "game must be a number")
          result.toString
        } else {
          val gameId = gameIdStr.toInt
          if (!games.contains(gameId)) {
            result.put("msg", "unknown game")
            result.toString
          } else {
            func(games(gameId))
          }
        }
      }
    }
  }

  def withPlayer(game: Game)(func: (Game, Player, String) => Any): Any = {
    val requestStr = request.body
    val result = new JSONObject()
    result.put("status", 400)
    if (!params.contains("player")) {
      result.put("msg", "must specify a player")
      result.toString
    } else {
      val playerIdStr = params("player")
      if (playerIdStr.isEmpty()) {
        result.put("status", 400)
        result.put("msg", "must specify a player")
        result.toString
      } else if (playerIdStr.matches("[^0-9]")) {
        result.put("msg", "player must be a number")
        result.toString
      } else if (requestStr.isEmpty()) {
        result.put("msg", "must provide a password in the post body")
        result.toString
      } else {
        val playerId = playerIdStr.toInt
        val stats = game.stats(playerId)
        if (!game.players.contains(playerId)) {
          result.put("msg", "unknown player")
          result.toString
        } else {
          val player = game.players(playerId)
          val firstLineAndRest = requestStr.split("\n", 2)
          val providedkey = firstLineAndRest(0)
          if (!providedkey.equals(player.pass)) {
            result.put("msg", "wrong password")
            result.toString
          } else {
            val stats = game.stats(playerId)
            if (!stats.alive) {
              result.put("msg", "dead players tell no lies")
              result.toString
            } else {
              func(game, player, if (firstLineAndRest.length == 1) "" else firstLineAndRest(1))
            }
          }
        }
      }
    }
  }

  post("/api/add_player/:game/named/:name")(withGame { game =>
    val pass = request.body
    val result = new JSONObject()
    result.put("status", 400)

    if (!params.contains("name")) {
      result.put("msg", "must specify a name")
    } else {
      val name = params("name")
      if (name.isEmpty()) {
        result.put("msg", "must specify a name")
      } else if (pass.isEmpty()) {
        result.put("msg", "must specify a password")
      } else if (!game.holding.isEmpty) {
        result.put("msg", "game already started")
      } else {
        val (newGame, id, started) = GameLogic.newPlayer(game, name, pass)
        games += (game.id -> newGame)
        result.put("status", 200)
        result.put("id", id)
        result.put("started", started)
        result.put("msg", if (started) "game started" else "player added")
      }
    }
    result.toString
  })

  post("/api/move/:game/:player/:direction")(withGame { game =>
    withPlayer(game) { (game, player, body) =>
      val result = new JSONObject()
      result.put("status", 400)

      if (!params.contains("direction")) {
        result.put("msg", "must specify a direction (N,S,E,W)")
        result.toString
      } else {
        val dir = params("direction")
        if (dir.isEmpty()) {
          result.put("msg", "must specify a direction (N,S,E,W)")
          result.toString
        } else if (!Set("N", "S", "E", "W").contains(dir)) {
          result.put("msg", "direction must be one of {N,S,E,W}")
          result.toString
        } else {
          val (newGame, newCoord, killed) = GameLogic.move(game, player, dir)
          games += (game.id -> newGame)
          result.put("status", 200)
          if (killed) {
            result.put("msg", "killed")
          } else {
            result.put("msg", s"new location: $newCoord")
          }
        }
      }
    }
  })

  post("/api/pick/:game/:player/:id")(withGame { game =>
    withPlayer(game) { (game, player, body) =>
      val result = new JSONObject()
      result.put("status", 400)

      if (!params.contains("id")) {
        result.put("msg", "must specify an object")
        result.toString
      } else {
        val objectIdStr = params("id")
        if (objectIdStr.isEmpty()) {
          result.put("msg", "must specify an object")
          result.toString
        } else if (objectIdStr.matches("[^0-9]")) {
          result.put("msg", "object must be a number")
          result.toString
        } else {
          val objectId = objectIdStr.toInt
          if (!game.objects.contains(objectId)) {
            result.put("msg", "unknown object")
            result.toString
          } else {
            val (newGame, found) = GameLogic.picked(game, player, objectId)
            if (!found) {
              result.put("msg", "object is not there")
              result.toString
            } else {
              games += (game.id -> newGame)
              result.put("status", 200)
              result.put("msg", "picked")
              result.toString
            }
          }
        }
      }
    }
  })

  def withOtherPlayer(game: Game, player: Player, body: String)(func: (Game, Player, Player, String) => Any): Any = {
    val result = new JSONObject()
    result.put("status", 400)

    if (!params.contains("other")) {
      result.put("msg", "must specify another player")
      result.toString
    } else {
      val otherIdStr = params("other")
      if (otherIdStr.isEmpty()) {
        result.put("status", 400)
        result.put("msg", "must specify another player")
        result.toString
      } else if (otherIdStr.matches("[^0-9]")) {
        result.put("msg", "the other player must be a number")
        result.toString
      } else {
        val otherId = otherIdStr.toInt
        val stats = game.stats(otherId)
        if (!game.players.contains(otherId)) {
          result.put("msg", "unknown other player")
          result.toString
        } else {
          val other = game.players(otherId)
          val stats = game.stats(otherId)
          if (!stats.alive) {
            result.put("msg", "it's dead Jim, it's dead")
            result.toString
          } else {
            func(game, player, other, body)
          }
        }
      }
    }
  }

  post("/api/attack/:game/:player/start/:other")(withGame { game =>
    withPlayer(game) { (game, player, body) =>
      withOtherPlayer(game, player, body) { (game, player, other, body) =>
        val result = new JSONObject()

        val actionsHash = body
        val (newGame, notThere) = GameLogic.startAttack(game, player, other, actionsHash)
        if (notThere) {
          result.put("status", 400)
          result.put("msg", "the other player is not there")
        } else {
          games += (game.id -> newGame)
          result.put("status", 200)
          result.put("msg", "attack request noted")
        }
        result.toString
      }
    }
  })

  post("/api/attack/:game/:player/execute/:other")(withGame { game =>
    withPlayer(game) { (game, player, body) =>
      withOtherPlayer(game, player, body) { (game, player, other, body) =>
        val result = new JSONObject()
        val actionsJson = body
        val (newGame, inError, outcome, msg) = GameLogic.attack(game, player, other, actionsJson)
        games += (game.id -> newGame)
        result.put("status", if (inError) 400 else 200)
        result.put("msg", msg)
        if (outcome.isDefined)
          result.put("outcome", outcome.get.toJsonObject())
        result.toString
      }
    }
  })

  get("/api/state/:game")(withGame { game =>
    //System.out.println(game); game.map.rooms.foreach(r => System.out.println(r))
    GameUtils.toJson(game)
  })
}