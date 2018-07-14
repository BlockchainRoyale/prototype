package io.github.blockchain_royale.prototype

import scala.util.Random

abstract class ObjectType {
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

import Types._

case class Player(id: PlayerId, name: String)

case class Object(id: ObjectId, _type: ObjectType)

case class Room(location: Coord, objects: List[ObjectId], players: List[PlayerId], open: Boolean)

case class Stats(alive: Boolean, kills: List[PlayerId])

case class GameMap(rooms: Array[Room])

case class Game(id: GameId,
                players: Map[PlayerId, Player] = Map(),
                objects: Map[ObjectId, Object] = Map(),
                holding: Map[PlayerId, List[ObjectId]] = Map(),
                stats: Map[PlayerId, Stats] = Map(),
                map: GameMap) {
  def start: Game = {

    var objectCount = 0
    var givenWeapons: Map[PlayerId, ObjectId] = Map()
    var newObjects: Map[ObjectId, Object] = Map()
    var newHolding: Map[PlayerId, List[ObjectId]] = Map()

    // given guns
    val newObject = (objectId: ObjectId) => {
      Random.nextInt(3) match {
        case 0 => Object(objectId, Gun)
        case 1 => Object(objectId, Grenade)
        case 2 => Object(objectId, Shield)
      }
    }
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
    for (_ <- 1.to(10)) {
      val coord = Tuple2(Random.nextInt(5), Random.nextInt(5))
      val objectId = objectCount
      objectCount += 1
      objectsAtRoom += (coord -> (objectsAtRoom.getOrElse(coord, List()) ++ List(objectId)))
      newObjects += objectId -> newObject(objectId)
    }

    // pick initial position for players
    var initialPos: Map[Coord, List[PlayerId]] = Map()
    for (_id <- players.keys) {
      val coord = Tuple2(Random.nextInt(5), Random.nextInt(5))
      initialPos += (coord -> (initialPos.getOrElse(coord, List()) ++ List(_id)))
    }

    // build map
    var rooms: Map[Coord, Room] = Map()
    for (x <- 0.to(4)) {
      for (y <- 0.to(4)) {
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


