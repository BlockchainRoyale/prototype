//import BlocchainRoyale from '../contracts/BlockchainRoyale.sol';
/*
const fs = require('fs');
const keccak256 = require('js-sha3').keccak256;*/

//hash();
//Player address, x, y, name, weapons...
var dummyData = [
    ["faddress1", 0, 4, "Bob", "Shotgun", "Pistol", "Shovel"],
    ["faddress2", 0, 4, "Freddie Fish", "Rifle", "Gun", "Shovel", "Branch"],
    ["faddress3", 0, 4, "Bob", "Sniper", "Minigun", "Deagle"]
]

//Player Locations are stored here
var playerLocations = [
    [0, 0, 2],
    [2, 4, 4],
    [5, 3, 5],
    [1, 2, 11],
    [4, 6, 7]
];

//Player Information is stored here
//Player ID, cards
var playerInfo = [];
fillInformation();

/*
playerInfo.push([]);
playerInfo.push([]);
playerInfo.push([]);

playerInfo[0][0] = "fakeaddress1";
playerInfo[0][1] = 1;
playerInfo[0][2] = 2;
playerInfo[0][3] = "Shotgun";
playerInfo[0][4] = "Pistol";
playerInfo[0][5] = "Shovel";

playerInfo[1][0] = "fakeaddress2";
playerInfo[1][1] = 2;
playerInfo[1][2] = 4;
playerInfo[1][3] = "Sniper";
playerInfo[1][4] = "Grenade";
playerInfo[1][5] = "Shotgun";
playerInfo[1][6] = "RPG";

playerInfo[2][0] = "fakeaddress3";
playerInfo[2][1] = 5;
playerInfo[2][2] = 3;
playerInfo[2][3] = "Sniper";
playerInfo[2][4] = "Grenade";
playerInfo[2][5] = "Pistol";
playerInfo[2][6] = "RPG";
playerInfo[2][7] = "Minigun";
*/

var yourLocation = [1,2];
var yourAddress = "asdf";

var angryPeople = [];
angryPeople.push([]);
angryPeople.push([]);
angryPeople.push([]);

angryPeople[0] = ["asdf", 1, 2, "fakeaddress1"];
angryPeople[1] = ["asdd", 1, 2, "fakeaddress2"];
angryPeople[2] = ["asdf", 1, 3, "fakeaddress3"];

const colors = ['red', 'purple', 'indigo', 'blue', 'green', 'yellow', 'orange', 'brown', 'light blue'];

const GRIDSIZE = document.getElementById("map").width / 7;
const MAXSIZE = document.getElementById("map").width;
const ICONSIZE = GRIDSIZE / 6;

function drawMap() {
    var map = document.getElementById("map");
    var ctx = map.getContext("2d");
    ctx.clearRect(0, 0, map.width, map.height);
    ctx.beginPath();
    for (let x = GRIDSIZE; x < MAXSIZE; x = x + GRIDSIZE) {
        ctx.moveTo(x, 0);
        ctx.lineTo(x, MAXSIZE);
    }
    for (let y = GRIDSIZE; y < MAXSIZE; y = y + GRIDSIZE) {
        ctx.moveTo(0, y);
        ctx.lineTo(MAXSIZE, y);
    }
    ctx.stroke();
    drawPlayerLocations();
}

function drawPlayerLocations() {
    var curX;
    var curY;
    var map = document.getElementById("map");
    var ctx = map.getContext("2d");
    var tooManyR = document.getElementById("TooManyR");
    var tooManyG = document.getElementById("TooManyG");
    var pOffset, numPlayers;
    var yCo, xCo, alreadyHere;
    for (let i = 0; i < playerLocations.length; i++) {
        if (!(playerLocations[i][2] > 9)) {
            numPlayers = 0;
            pOffset = playerLocations[i][2];
            yCo = 1;
            xCo = 1;
            alreadyHere = false;
            for (let x = 0; numPlayers < playerLocations[i][2]; x++) {
                curX = (GRIDSIZE * playerLocations[i][0]) + (((GRIDSIZE / 4.5) - (ICONSIZE / 2)) * xCo);
                curY = (GRIDSIZE * playerLocations[i][1]) + (((GRIDSIZE / 4.5) - (ICONSIZE / 2)) * yCo);
                if(playerLocations[i][0] == yourLocation[0] && playerLocations[i][1] == yourLocation[1] && !alreadyHere){
                    ctx.fillStyle = colors[4];
                    alreadyHere = true;
                }else {
                    ctx.fillStyle = colors[0];
                }

                ctx.fillRect(curX, curY, ICONSIZE, ICONSIZE);
                numPlayers++;
                xCo += 2;
                if (xCo > 5) {
                    yCo += 2;
                    xCo = 1;
                }
            }
        } else {
            curX = (GRIDSIZE * playerLocations[i][0] + (GRIDSIZE * 7/25));
            curY = (GRIDSIZE * playerLocations[i][1]) + (GRIDSIZE * (3 / 5));
            if(playerLocations[i][0] == yourLocation[0] && playerLocations[i][1] == yourLocation[1]){
                ctx.drawImage(tooManyG, 5 + (GRIDSIZE * playerLocations[i][0]), 5 + (GRIDSIZE * playerLocations[i][1]), GRIDSIZE - 10, GRIDSIZE - 10);
            }else {
                ctx.drawImage(tooManyR, 5 + (GRIDSIZE * playerLocations[i][0]), 5 + (GRIDSIZE * playerLocations[i][1]), GRIDSIZE - 10, GRIDSIZE - 10);
            }

            ctx.font = "200% Courier New";
            ctx.fillStyle = "black";
            ctx.fillText(playerLocations[i][2], curX, curY);
        }

    }
}
function playerInfoSetup() {
    console.log(playerInfo.length);
    var option;
    var select = document.getElementById("playerInfoInput");
    for(let i = 0; i < playerInfo.length;i++){
        option = document.createElement("option");
        option.innerHTML = i;
        option.value = i;
        select.add(option);
    }
    playerInformation();
}

function playerInformation() {
    var nameText = document.getElementById("details");
    var cardText = document.getElementById("playerCards");
    var i = document.getElementById("playerInfoInput").value;
    document.getElementById("playerInfoInput").max = playerInfo[i].length;
    nameText.innerHTML = playerInfo[i][0] + "<br>" + "Location: (" + playerInfo[i][1] + ", " +playerInfo[i][2] + ")";
    cardText.innerHTML = '';
    for (let x = 4; x < playerInfo[i].length; x++) {
        cardText.innerHTML += playerInfo[i][x] + '<br>';
    }
}

function battleRequests() {
    document.getElementById("requests").innerHTML = '';
    for(let i = 0; i < angryPeople.length; i++){
        if(yourLocation[0] == angryPeople[i][1] && yourLocation[1] == angryPeople[i][2] && yourAddress == angryPeople[i][0]){
            document.getElementById("requests").innerHTML += angryPeople[i][3] + '<br>';
        }
    }
}

function fillInformation() {
    for(let i=0; i< dummyData.length; i++){
        playerInfo.push([]);
        for(let n=0; n<dummyData[i].length;n++){
            playerInfo[i][n] = dummyData[i][n];
        }
    }
}

function hash() {
    var arrayStuff = [1, 2, 3, 4, 5];
    console.log(keccak256(arrayStuff));
}

/*
function fillInformation() {
    for(let i=0; i< dummyData.length; i++){
        playerInfo.push([]);
        for(let n=0; n<dummyData[i].length;n++){
            playerInfo[i][n] = dummyData[i][n];
        }
    }
}
 */