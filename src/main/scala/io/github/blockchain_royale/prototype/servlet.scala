package io.github.blockchain_royale.prototype

import org.scalatra.ScalatraServlet

class GameServlet extends ScalatraServlet {

  private var games: Map[Int, Game] = Map[Int, Game]()

  get("/") {

  }

  get("/api/new_game") {
    games.synchronized {
      val newId = games.size + 1
      games = games + (newId -> Game(newId, Map(), GameMap(Array())))
      newId.toString
    }
  }
  get("/api/new_game") {
    games.synchronized {
      val newId = games.size + 1
      games = games + (newId -> Game(newId, Map(), GameMap(Array())))
      newId.toString
    }
  }

  get("/api/add_player/:game/named/:name") {
    games.synchronized {
      val game = games(params("game").toInt)
      val newPlayer = Player(game.players.size + 1, params("name"))
      val newGame = game.copy(players = game.players + (newPlayer.id -> newPlayer))

      val potentiallyStartedGame =
        if (newGame.players.size == 6)
          newGame.start()
        else
          newGame

      games = games + (game.id -> potentiallyStartedGame)
      newPlayer.id.toString
    }

  }

}