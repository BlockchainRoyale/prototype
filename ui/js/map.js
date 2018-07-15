//import BlocchainRoyale from '../contracts/BlockchainRoyale.sol';
/*
const fs = require('fs');
const keccak256 = require('js-sha3').keccak256;*/

//hash();
//Player address, x, y, name, weapons...

var dummyData = [
    ["faddress1", 0, 4, "Bob", "Shotgun", "Pistol", "Shovel"],
    ["faddress2", 0, 4, "Freddie Fish", "Rifle", "Gun", "Shovel", "Branch"],
    ["faddress3", 0, 4, "Job", "Sniper", "Minigun", "Deagle"]
];

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


function setup(){
    fillInformation();
    menuSelect();
}

var yourLocation = [5,3];
var yourAddress = "asdf";

var angryPeople = [];
angryPeople.push([]);
angryPeople.push([]);
angryPeople.push([]);

angryPeople[0] = ["asdf", 1, 2, "fakeaddress1"];
angryPeople[1] = ["asdd", 1, 2, "fakeaddress2"];
angryPeople[2] = ["asdf", 1, 3, "fakeaddress3"];

const colors = ['red', 'purple', 'indigo', 'blue', 'green', 'yellow', 'orange', 'brown', 'light blue'];

var gridsize = document.getElementById("map").width / 7;
var MAXSIZE = document.getElementById("map").width;
const ICONSIZE = gridsize / 6;

const ASIZE = document.getElementById("arena").width / 5;
const MAXASIZE = document.getElementById("arena").width;
const AICONSIZE = ASIZE / 3;

initialize();

function mapRedraw() {
    gridsize = map.width / 7;
    drawMap();
    drawPlayerLocations();
}
function drawMap() {
    var map = document.getElementById("map");
    var ctx = map.getContext("2d");
    ctx.strokeStyle = "white";
    ctx.clearRect(0, 0, map.width, map.height);
    ctx.beginPath();
    for (let x = gridsize; x < MAXSIZE; x = x + gridsize) {
        ctx.moveTo(x, 0);
        ctx.lineTo(x, MAXSIZE);
    }
    for (let y = gridsize; y < MAXSIZE; y = y + gridsize) {
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
                curX = (gridsize * playerLocations[i][0]) + (((gridsize / 4.5) - (ICONSIZE / 2)) * xCo);
                curY = (gridsize * playerLocations[i][1]) + (((gridsize / 4.5) - (ICONSIZE / 2)) * yCo);
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
            curX = (gridsize * playerLocations[i][0] + (gridsize * 7/25));
            curY = (gridsize * playerLocations[i][1]) + (gridsize * (3 / 5));
            if(playerLocations[i][0] == yourLocation[0] && playerLocations[i][1] == yourLocation[1]){
                ctx.drawImage(tooManyG, 5 + (gridsize * playerLocations[i][0]), 5 + (gridsize * playerLocations[i][1]), gridsize - 10, gridsize - 10);
            }else {
                ctx.drawImage(tooManyR, 5 + (gridsize * playerLocations[i][0]), 5 + (gridsize * playerLocations[i][1]), gridsize - 10, gridsize - 10);
            }

            ctx.font = "200% Courier New";
            ctx.fillStyle = "white";
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
    for(let i=0; i<dummyData.length; i++){
        playerInfo.push([]);
        for(let n=0; n<dummyData[i].length;n++){
            playerInfo[i][n] = dummyData[i][n];
        }
    }
}

//Arena Functions
function swapCanvas() {
    var arena = document.getElementById("arenaContainer");
    console.log(arena.style.border);
    if(arena.style.visibility == "visible"){
        arena.style.visibility = "hidden";
    }else{
        arena.style.visibility = "visible";
        drawArena();
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

var battleInput1 = ["up", "down", "shoot", "left", "shoot", 3];
var battleInput2 = ["left", "left", "shoot", "right", "shoot", 1];
var battleloc1 = [0,0];
var battleloc2 = [3, 5];

function playArena() {
    for(let i=0; i<battleInput1.length; i++){
        switch(battleInput1[i]){
            case "up":
                battleloc1[1]++;
                battleloc1[1] = over(battleloc1[1]);
                break;
            case "right":
                battleloc1[0]++;
                battleloc1[0] = over(battleloc1[0]);
            case "down":
                battleloc1[0]--;
                battleloc1[0] = over(battleloc1[0]);
            case "left":
                battleloc1[0]++;
                battleloc1[0] = over(battleloc1[0]);
            case "shoot":
        }
    }
}

function over(inNum) {
    if(inNum>4 ){
        return 0;
    }else if(inNum<0){
        return 4;
    }
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
    switch(selection){
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