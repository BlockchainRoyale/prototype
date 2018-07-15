# non-blockchain prototype

To build, you need [maven](https://maven.apache.org/):

```sh
mvn package appassembler:assemble
```

To start the server navigate to prototype/target/appassembler/bin and run server.bat
Should see "Started: 8822"

Then interact with the API (see servlet.scala for all the paths available) to create a game:

```sh
curl -Xpost http://localhost:8822/api/new_game
```

will return id, for example "1"

```sh
curl -Xpost http://localhost:8822/api/add_player/1/named/pablo
```

will return the id and true or false depending whether the game has started

```sh
curl -Xpost http://localhost:8822/api/add_player/1/named/player2
curl -Xpost http://localhost:8822/api/add_player/1/named/player3
curl -Xpost http://localhost:8822/api/add_player/1/named/player4
```

more players till it starts

```sh
curl -Xget http://localhost:8822/api/state/1
```

game state looks like this:

```json
{
   "players" : [
      {
         "row" : 2,
         "holds" : [
            "Gun"
         ],
         "col" : 4,
         "name" : "pablo",
         "kills" : [],
         "id" : 1,
         "alive" : true
      },
      {
         "holds" : [
            "Grenade"
         ],
         "row" : 2,
         "id" : 2,
         "alive" : true,
         "kills" : [],
         "name" : "player2",
         "col" : 2
      },
      {
         "name" : "player3",
         "col" : 3,
         "id" : 3,
         "kills" : [],
         "alive" : true,
         "row" : 3,
         "holds" : [
            "Grenade"
         ]
      },
      {
         "row" : 1,
         "holds" : [
            "Gun"
         ],
         "name" : "player4",
         "col" : 2,
         "id" : 4,
         "kills" : [],
         "alive" : true
      }
   ],
   "actions" : [
      "NewPlayerAct(4,player4)",
      "NewPlayerAct(3,player3)",
      "NewPlayerAct(2,player2)",
      "NewPlayerAct(1,pablo)",
      "GenesisAct()"
   ],
   "rooms" : [
      [
         {
            "players" : [],
            "row" : 0,
            "objects" : [
               {
                  "type" : "Shield",
                  "id" : 8
               }
            ],
            "active" : true,
            "col" : 0
         },
         {
            "active" : true,
            "col" : 1,
            "objects" : [],
            "row" : 0,
            "players" : []
         },
         {
            "active" : true,
            "col" : 2,
            "players" : [],
            "objects" : [],
            "row" : 0
         },
         {
            "active" : true,
            "col" : 3,
            "row" : 0,
            "objects" : [],
            "players" : []
         },
         {
            "row" : 0,
            "objects" : [],
            "players" : [],
            "col" : 4,
            "active" : true
         }
      ],
      [
         {
            "players" : [],
            "objects" : [
               {
                  "type" : "Shield",
                  "id" : 5
               }
            ],
            "row" : 1,
            "col" : 0,
            "active" : true
         },
         {
            "objects" : [],
            "row" : 1,
            "players" : [],
            "active" : true,
            "col" : 1
         },
         {
            "players" : [
               4
            ],
            "row" : 1,
            "objects" : [],
            "col" : 2,
            "active" : true
         },
         {
            "col" : 3,
            "active" : true,
            "objects" : [],
            "row" : 1,
            "players" : []
         },
         {
            "row" : 1,
            "objects" : [],
            "players" : [],
            "col" : 4,
            "active" : true
         }
      ],
      [
         {
            "objects" : [],
            "row" : 2,
            "players" : [],
            "active" : true,
            "col" : 0
         },
         {
            "objects" : [],
            "row" : 2,
            "players" : [],
            "active" : true,
            "col" : 1
         },
         {
            "players" : [
               2
            ],
            "row" : 2,
            "objects" : [],
            "active" : true,
            "col" : 2
         },
         {
            "active" : true,
            "col" : 3,
            "players" : [],
            "row" : 2,
            "objects" : [
               {
                  "type" : "Shield",
                  "id" : 4
               },
               {
                  "id" : 7,
                  "type" : "Shield"
               }
            ]
         },
         {
            "col" : 4,
            "active" : true,
            "row" : 2,
            "objects" : [],
            "players" : [
               1
            ]
         }
      ],
      [
         {
            "col" : 0,
            "active" : true,
            "row" : 3,
            "objects" : [],
            "players" : []
         },
         {
            "col" : 1,
            "active" : true,
            "objects" : [],
            "row" : 3,
            "players" : []
         },
         {
            "active" : true,
            "col" : 2,
            "objects" : [],
            "row" : 3,
            "players" : []
         },
         {
            "active" : true,
            "col" : 3,
            "players" : [
               3
            ],
            "row" : 3,
            "objects" : []
         },
         {
            "col" : 4,
            "active" : true,
            "players" : [],
            "objects" : [],
            "row" : 3
         }
      ],
      [
         {
            "players" : [],
            "row" : 4,
            "objects" : [],
            "col" : 0,
            "active" : true
         },
         {
            "col" : 1,
            "active" : true,
            "players" : [],
            "row" : 4,
            "objects" : [
               {
                  "id" : 6,
                  "type" : "Shield"
               }
            ]
         },
         {
            "row" : 4,
            "objects" : [],
            "players" : [],
            "col" : 2,
            "active" : true
         },
         {
            "row" : 4,
            "objects" : [],
            "players" : [],
            "col" : 3,
            "active" : true
         },
         {
            "row" : 4,
            "objects" : [],
            "players" : [],
            "active" : true,
            "col" : 4
         }
      ]
   ]
}
```

