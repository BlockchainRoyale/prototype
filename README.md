# non-blockchain prototype

mvn package appassembler:assemble

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

game state
