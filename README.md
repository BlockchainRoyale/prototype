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
curl -Xpost -H "Content-type: text/plain" --data pass http://localhost:8822/api/add_player/1/named/pablo
```

will return the id and true or false depending whether the game has started

```sh
curl -Xpost -H "Content-type: text/plain" --data pass2 http://localhost:8822/api/add_player/1/named/player2
curl -Xpost -H "Content-type: text/plain" --data pass3 http://localhost:8822/api/add_player/1/named/player3
curl -Xpost -H "Content-type: text/plain" --data pass4 http://localhost:8822/api/add_player/1/named/player4
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

## Sample actions

```sh
curl -Xpost http://localhost:8822/api/new_game
curl -Xpost  -H "Content-type: text/plain" --data pass1 http://localhost:8822/api/add_player/1/named/player1
curl -Xpost  -H "Content-type: text/plain" --data pass2 http://localhost:8822/api/add_player/1/named/player2
curl -Xpost  -H "Content-type: text/plain" --data pass3 http://localhost:8822/api/add_player/1/named/player3
curl -Xpost  -H "Content-type: text/plain" --data pass4 http://localhost:8822/api/add_player/1/named/player4
curl -Xpost  -H "Content-type: text/plain" --data pass2 http://localhost:8822/api/move/1/2/W
curl -Xpost  -H "Content-type: text/plain" --data pass2 http://localhost:8822/api/move/1/2/S
curl -Xpost  -H "Content-type: text/plain" --data-binary @start_attack.txt http://localhost:8822/api/attack/1/2/start/3
curl -Xpost  -H "Content-type: text/plain" --data-binary @respond_attack.txt http://localhost:8822/api/attack/1/3/execute/2
curl -Xpost  -H "Content-type: text/plain" --data-binary @execute_attack.txt http://localhost:8822/api/attack/1/2/execute/3
```

with start_attack.txt as

```txt
pass2
a8e36d3705a1ad54e7720760fa94bbad01781913
```

respond_attack.txt

```txt
pass3
[{"type":"Use","object":2},
{"type":"MoveNorth"},
{"type":"Use","object":2},
{"type":"MoveNorth"},
{"type":"Use","object":2},
{"type":"MoveNorth"},
{"type":"Use","object":2},
{"type":"MoveNorth"}
]
```

```txt
pass2
[{"type":"Use","object":1},
{"type":"MoveNorth"},
{"type":"Use","object":1},
{"type":"MoveNorth"},
{"type":"Use","object":1},
{"type":"MoveNorth"},
{"type":"Use","object":1},
{"type":"MoveNorth"}
]
```


