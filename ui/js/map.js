//import BlocchainRoyale from '../contracts/BlockchainRoyale.sol';
/*
const fs = require('fs');
const keccak256 = require('js-sha3').keccak256;*/

//hash();
//Player address, x, y, name, weapons...

var dummyData = [
    ["faddress1", 0, 4, "Quinn QuickHands", "Shotgun", "Pistol", "Shovel"],
    ["faddress2", 0, 4, "Sammy Shooter", "Rifle", "Gun", "Shovel", "Branch"],
    ["faddress3", 0, 4, "Will the winner", "Sniper", "Minigun", "Deagle"]
];

//Player Locations are stored here
var playerLocations = [
    [0, 0, 2],
    [0, 1, 0],
    [0, 2, 0],
    [0, 3, 0],
    [0, 4, 0],
    [0, 5, 0],
    [0, 6, 0],
    [1, 0, 0],
    [1, 1, 0],
    [1, 2, 3],
    [1, 3, 0],
    [1, 4, 0],
    [1, 5, 4],
    [1, 6, 0],
    [2, 0, 3],
    [2, 1, 0],
    [2, 2, 0],
    [2, 3, 0],
    [2, 4, 4],
    [2, 5, 0],
    [2, 6, 0],
    [3, 0, 2],
    [3, 1, 0],
    [3, 2, 3],
    [3, 3, 0],
    [3, 4, 0],
    [3, 5, 0],
    [3, 6, 0],
    [4, 0, 4],
    [4, 1, 0],
    [4, 2, 0],
    [4, 3, 0],
    [4, 4, 0],
    [4, 5, 0],
    [4, 6, 0],
    [5, 0, 0],
    [5, 1, 0],
    [5, 2, 3],
    [5, 3, 2],
    [5, 4, 0],
    [5, 5, 0],
    [5, 6, 0],
    [6, 0, 0],
    [6, 1, 0],
    [6, 2, 1],
    [6, 3, 0],
    [6, 4, 0],
    [6, 5, 5],
    [6, 6, 0],

];

//Player Information is stored here
//Player ID, cards
var playerInfo = [];


function setup() {
    fillInformation();
    menuSelect();
}

var yourLocation = [5, 3];
var yourAddress = "asdf";

var angryPeople = [];
angryPeople.push([]);
angryPeople.push([]);
angryPeople.push([]);

angryPeople[0] = ["asdf", 1, 2, "fakeaddress1"];
angryPeople[1] = ["asdd", 1, 2, "fakeaddress2"];
angryPeople[2] = ["asdf", 3, 2, "fakeaddress3"];

const colors = ['red', 'purple', 'indigo', 'blue', 'green', 'yellow', 'orange', 'brown', 'light blue'];

var GRIDSIZE = document.getElementById("map").width / 7;
var MAXSIZE = document.getElementById("map").width;
const ICONSIZE = GRIDSIZE / 6;

const ASIZE = document.getElementById("arena").width / 5;
const MAXASIZE = document.getElementById("arena").width;
const AICONSIZE = ASIZE / 3;

initialize();

function findLocation() {
    var out = yourLocation[0] * 7 + yourLocation[1];
}

function moveUp(){
    playerLocations[yourLocation[0] * 7 + yourLocation[1]][2]--;
    yourLocation[1]--;
    playerLocations[yourLocation[0] * 7 + yourLocation[1]][2]++;
    battleRequests();
    mapRedraw();
}

function moveRight(){
    playerLocations[yourLocation[0] * 7 + yourLocation[1]][2]--;
    yourLocation[0]++
    playerLocations[yourLocation[0] * 7 + yourLocation[1]][2]++;
    battleRequests();
    mapRedraw();
}

function moveDown(){
    playerLocations[yourLocation[0] * 7 + yourLocation[1]][2]--;
    yourLocation[1]++;
    playerLocations[yourLocation[0] * 7 + yourLocation[1]][2]++;
    battleRequests();
    mapRedraw();
}

function moveLeft(){
    playerLocations[yourLocation[0] * 7 + yourLocation[1]][2]--;
    yourLocation[0]--
    playerLocations[yourLocation[0] * 7 + yourLocation[1]][2]++;
    battleRequests();
    mapRedraw();
}

function mapRedraw() {
    drawtiles();
    playerList();
    drawMap();
    drawPlayerLocations();
}

function drawMap() {
    var map = document.getElementById("map");
    var ctx = map.getContext("2d");
    ctx.strokeStyle = "white";
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
                if (playerLocations[i][0] == yourLocation[0] && playerLocations[i][1] == yourLocation[1] && !alreadyHere) {
                    ctx.fillStyle = colors[5];
                    alreadyHere = true;
                } else {
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
            curX = (GRIDSIZE * playerLocations[i][0] + (GRIDSIZE * 7 / 25));
            curY = (GRIDSIZE * playerLocations[i][1]) + (GRIDSIZE * (3 / 5));
            if (playerLocations[i][0] == yourLocation[0] && playerLocations[i][1] == yourLocation[1]) {
                ctx.drawImage(tooManyG, 5 + (GRIDSIZE * playerLocations[i][0]), 5 + (GRIDSIZE * playerLocations[i][1]), GRIDSIZE - 10, GRIDSIZE - 10);
            } else {
                ctx.drawImage(tooManyR, 5 + (GRIDSIZE * playerLocations[i][0]), 5 + (GRIDSIZE * playerLocations[i][1]), GRIDSIZE - 10, GRIDSIZE - 10);
            }

            ctx.font = "200% Courier New";
            ctx.fillStyle = "white";
            ctx.fillText(playerLocations[i][2], curX, curY);
        }

    }
}

function submitInputs(){
    var textArea = document.getElementById("inputTextArea");
    var stuff = textArea.value;
    textArea.value = '';

}

function playerInfoSetup() {
    console.log(playerInfo.length);
    var option;
    var select = document.getElementById("playerInfoInput");
    for (let i = 0; i < playerInfo.length; i++) {
        option = document.createElement("option");
        option.innerHTML = playerInfo[i][3];
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
    nameText.innerHTML = "<p>" + playerInfo[i][0] + "<br>" + "Location: <br>(" + playerInfo[i][1] + ", " + playerInfo[i][2] + ")</p>";
    cardText.innerHTML = '';
    for (let x = 4; x < playerInfo[i].length; x++) {
        cardText.innerHTML += playerInfo[i][x] + '<br>';
    }
}
function playerList() {
    var playerList = document.getElementById("listOfPlayers");
    playerList.innerHTML = '';
    for (let x = 0; x < dummyData.length; x++) {
        playerList.innerHTML += '<li><button>' + dummyData[x][3] + '</button></li>';
    }
}

function battleRequests() {
    document.getElementById("requests").innerHTML = '';
    for (let i = 0; i < angryPeople.length; i++) {
        if (yourLocation[0] == angryPeople[i][1] && yourLocation[1] == angryPeople[i][2] && yourAddress == angryPeople[i][0]) {
            document.getElementById("requests").innerHTML += angryPeople[i][3] + '<br>';
        }
    }
}

function fillInformation() {
    for (let i = 0; i < dummyData.length; i++) {
        playerInfo.push([]);
        for (let n = 0; n < dummyData[i].length; n++) {
            playerInfo[i][n] = dummyData[i][n];
        }
    }
}

//Arena Functions
function swapCanvas() {
    var arena = document.getElementById("arenaContainer");
    console.log(arena.style.border);
    if (arena.style.visibility == "visible") {
        arena.style.visibility = "hidden";
    } else {
        arena.style.visibility = "visible";
        drawArena();
    }
}

var turncount = 0;
var playtimer;
var drawingArena = false;

function redrawArena() {
    if (!drawingArena) {
        drawingArena = true;
        battleAnimate();
        playTimer = setInterval(playArena, 1000);
    }
}

function drawArena() {
    var arena = document.getElementById("arena");
    var ctx = arena.getContext("2d");
    ctx.strokeStyle = "black";
    ctx.clearRect(0, 0, arena.width, arena.height);
    ctx.beginPath();

    for (let x = ASIZE; x < MAXASIZE; x = x + ASIZE) {
        ctx.moveTo(x, 0);
        ctx.lineTo(x, MAXASIZE);
    }
    for (let y = ASIZE; y < MAXASIZE; y = y + ASIZE) {
        ctx.moveTo(0, y);
        ctx.lineTo(MAXASIZE, y);
    }
    ctx.stroke();
}


var battleInput1 = ["up", "down", "shoot", "left", "right"];
var battleInput2 = ["left", "left", "left", "right", "shoot"];
var battleloc1 = [1, 1];
var battleloc2 = [3, 4];
var p1shoot = false;
var p2shoot = false;

function playArena() {
    drawArena();
    p1Moves(turncount);
    p2Moves(turncount);
    battleAnimate();
    if (battleloc1[0] == battleloc2[0] || battleloc1[1] == battleloc2[1]) {
        if (battleInput1[turncount] == "shoot" && battleInput2[turncount] == "shoot") {
            turncount = 0;
            clearInterval(playTimer);
            drawingArena = false;
            document.getElementById("outText").innerHTML = "Both Died";
        } else if (battleInput1[turncount] == "shoot") {
            turncount = 0;
            clearInterval(playTimer);
            drawingArena = false;
            document.getElementById("outText").innerHTML = "Player 1 Won!";
        } else if (battleInput2[turncount] == "shoot") {
            turncount = 0;
            clearInterval(playTimer);
            drawingArena = false;
            document.getElementById("outText").innerHTML = "Player 2 Won!";
        }
    }
    turncount++;
    if (turncount > 4) {
        turncount = 0;
        clearInterval(playTimer);
        drawingArena = false;
    }
}

function battleAnimate() {
    var arena = document.getElementById("arena");
    var ctx = arena.getContext("2d");
    ctx.clearRect(0, 0, arena.width, arena.height);
    drawArena();

    if (p1shoot) {
        ctx.fillStyle = "red";
    } else {
        ctx.fillStyle = "green";
    }

    ctx.beginPath();
    console.log(battleloc1[0] + " , " + battleloc1[1]);
    ctx.arc((battleloc1[0] * ASIZE) + (ASIZE) / 2, (battleloc1[1] * ASIZE) + (ASIZE) / 2, AICONSIZE, 0, Math.PI * 2);
    ctx.fill();

    if (p2shoot) {
        ctx.fillStyle = "red";
    } else {
        ctx.fillStyle = "green";
    }
    ctx.beginPath();
    console.log(battleloc2[0] + " , " + battleloc2[1]);
    ctx.arc((battleloc2[0] * ASIZE) + (ASIZE) / 2, (battleloc2[1] * ASIZE) + (ASIZE) / 2, AICONSIZE, 0, Math.PI * 2);
    ctx.fill();

    p1shoot = false;
    p2shoot = false;
}

function p1Moves(i) {
    console.log(battleInput1[i]);
    switch (battleInput1[i]) {
        case "up":
            battleloc1[1]--;
            battleloc1[1] = over(battleloc1[1]);
            break;
        case "right":
            battleloc1[0]++;
            battleloc1[0] = over(battleloc1[0]);
            break;
        case "down":
            battleloc1[1]++;
            battleloc1[1] = over(battleloc1[1]);
            break;
        case "left":
            battleloc1[0]--;
            battleloc1[0] = over(battleloc1[0]);
            break;
        case "shoot":
            p1shoot = true;
            break;
    }
    document.getElementById("battleInfo").innerHTML += battleInput1[i] + "<br>";
}

function p2Moves(i) {
    console.log(battleInput2[i]);
    switch (battleInput2[i]) {
        case "up":
            battleloc2[1]--;
            battleloc2[1] = over(battleloc2[1]);
            break;
        case "right":
            battleloc2[0]++;
            battleloc2[0] = over(battleloc2[0]);
            break;
        case "down":
            battleloc2[1]++;
            battleloc2[1] = over(battleloc2[1]);
            break;
        case "left":
            battleloc2[0]--;
            battleloc2[0] = over(battleloc2[0]);

            break;
        case "shoot":
            p2shoot = true;
            break;
    }
    document.getElementById("battleInfo").innerHTML += battleInput2[i] + "<br>";
}

function over(inNum) {
    if (inNum > 4) {
        return 0;
    } else if (inNum < 0) {
        return 4;
    }
    return inNum;
}

/*
function hash() {
    var arrayStuff = [1, 2, 3, 4, 5];
    console.log(keccak256(arrayStuff));
}
*/

function menuSelect() {
    var playerInventories = document.getElementById("infoContainer");
    var battleRequest = document.getElementById("battleRequests");
    var selection = document.getElementById("menuSelection").value;
    switch (selection) {
        case "1":
            console.log("InfoContainer");
            playerInventories.style.display = 'block';
            battleRequest.style.display = 'none';
            break;
        case "2":
            console.log("InfoContainer");
            playerInventories.style.display = 'none';
            battleRequest.style.display = 'block';
            break;
        default:
            console.log("None");
            playerInventories.style.display = 'block';
            battleRequest.style.display = 'none';
            break;
    }
}

function displayInputs() {
    var log = document.getElementById("battleInfo");
    var input = document.getElementById("battleInput");
    log.style.display = 'none';
    input.style.display = 'block';
}

function displayLog() {
    var log = document.getElementById("battleInfo");
    var input = document.getElementById("battleInput");

    log.style.display = 'block';
    input.style.display = 'none';
}

function initialize() {
    window.addEventListener('resize', resizeCanvas, false);
    resizeCanvas();
}

function resizeCanvas() {
    if (window.innerHeight < 700) {
        map.width = window.innerHeight * 0.9;
        map.height = window.innerHeight * 0.9;
        mapRedraw();
    }
}

function drawtiles() {
    var c = document.getElementById("map");
    var ctx = c.getContext("2d");
    ctx.clearRect(0, 0, map.width, map.height);
    var grid1 = document.getElementById("grid1");
    var grid2 = document.getElementById("grid2");
    var grid3 = document.getElementById("grid3");
    var grid4 = document.getElementById("grid4");
    var grid5 = document.getElementById("grid5");
    var grid6 = document.getElementById("grid6");
    var grid7 = document.getElementById("grid7");
    var grid8 = document.getElementById("grid8");
    var extra1 = document.getElementById("extra1");
    var extra2 = document.getElementById("extra2");
    var fg1 = document.getElementById("fg1");
    var fg2 = document.getElementById("fg2");
    var fg3 = document.getElementById("fg3");
    var fg4 = document.getElementById("fg4");
    var gr1 = document.getElementById("gr1");
    var gr2 = document.getElementById("gr2");
    var gr3 = document.getElementById("gr3");
    var gr4 = document.getElementById("gr4");

    ctx.drawImage(grid3, 0, 0, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid4, 0, GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid5, 0, 2 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid2, 0, 3 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(extra2, 0, 4 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid2, 0, 5 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid5, 0, 6 * GRIDSIZE, GRIDSIZE, GRIDSIZE);

    ctx.drawImage(grid1, GRIDSIZE, 0, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid6, GRIDSIZE, GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid8, GRIDSIZE, 2 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid7, GRIDSIZE, 3 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid5, GRIDSIZE, 4 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid6, GRIDSIZE, 5 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid8, GRIDSIZE, 6 * GRIDSIZE, GRIDSIZE, GRIDSIZE);

    ctx.drawImage(grid1, 2 * GRIDSIZE, 0, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg3, 2 * GRIDSIZE, GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr2, 2 * GRIDSIZE, 2 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg2, 2 * GRIDSIZE, 3 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg3, 2 * GRIDSIZE, 4 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg4, 2 * GRIDSIZE, 5 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg4, 2 * GRIDSIZE, 6 * GRIDSIZE, GRIDSIZE, GRIDSIZE);

    ctx.drawImage(grid7, 3 * GRIDSIZE, 0, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(extra2, 3 * GRIDSIZE, GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid5, 3 * GRIDSIZE, 2 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr1, 3 * GRIDSIZE, 3 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr1, 3 * GRIDSIZE, 4 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr3, 3 * GRIDSIZE, 5 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr3, 3 * GRIDSIZE, 6 * GRIDSIZE, GRIDSIZE, GRIDSIZE);

    ctx.drawImage(fg1, 4 * GRIDSIZE, 0, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg2, 4 * GRIDSIZE, GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg3, 4 * GRIDSIZE, 2 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg4, 4 * GRIDSIZE, 3 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(extra1, 4 * GRIDSIZE, 4 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(extra2, 4 * GRIDSIZE, 5 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr1, 4 * GRIDSIZE, 6 * GRIDSIZE, GRIDSIZE, GRIDSIZE);

    ctx.drawImage(fg2, 5 * GRIDSIZE, 0, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr1, 5 * GRIDSIZE, GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg4, 5 * GRIDSIZE, 2 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg3, 5 * GRIDSIZE, 3 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg1, 5 * GRIDSIZE, 4 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(extra2, 5 * GRIDSIZE, 5 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr2, 5 * GRIDSIZE, 6 * GRIDSIZE, GRIDSIZE, GRIDSIZE);

    ctx.drawImage(extra1, 6 * GRIDSIZE, 0, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(extra2, 6 * GRIDSIZE, GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(grid5, 6 * GRIDSIZE, 2 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr1, 6 * GRIDSIZE, 3 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(fg1, 6 * GRIDSIZE, 4 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr3, 6 * GRIDSIZE, 5 * GRIDSIZE, GRIDSIZE, GRIDSIZE);
    ctx.drawImage(gr4, 6 * GRIDSIZE, 6 * GRIDSIZE, GRIDSIZE, GRIDSIZE);


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