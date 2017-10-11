var currentPlayer;
var activePlayer;
var numPlayersInGame;
// TODO
var clientGameVersion = 0;
var clientChatVersion = 0;
var tableSize = 5;
var REFRESH_RATE_QUICK = 500;
var REFRESH_RATE = 2000;
var TABLE_DRAW_SIZE = 600;
var quickRefreshInterval;
var refreshInterval;

// on page load
$(function () {
    refreshAll();
    setGameType();
    //prevent IE from caching ajax calls
    $.ajaxSetup({cache: false});
    // will execute the method every REFRESH_RATE milliseconds
    quickRefreshInterval = setInterval(refreshGameState, REFRESH_RATE_QUICK);
    refreshInterval = setInterval(checkForUpdate, REFRESH_RATE);
    // TODO do we need to check if the user exits a game withou the end game button?
    // $(window).beforeunload  = function () {
    //     swal("window closing");
    //     endGame();
    // }
});

function refreshAll() {
    refreshGameState();
    refreshPlayersTitle();
    refreshBoardAndStatistic();
}

function checkForUpdate() {
    $.ajax({
        data: {"action": "gameVersion"},
        url: "activeGame",
        error: function () {
            console.error("Failed to get ajax response get active game version");
        },
        success: function (serverVersion) {
            if (clientGameVersion < serverVersion) {
                console.log("active game versions: client- " + clientGameVersion + "server- " + serverVersion);
                clientGameVersion = serverVersion;
                refreshAll();
            }
        }
    });

    $.ajax({
        data: {"action": "chatVersion"},
        url: "activeGame",
        error: function () {
            console.error("Failed to get ajax response get active chat version");
        },
        success: function (chatVersion) {
            if (clientChatVersion < chatVersion) {
                console.log("active chat versions: client- " + clientChatVersion + "chat- " + chatVersion);
                updateNewChatMessages(chatVersion);
            }
        }
    });
}

function refreshGameState() {
    $.ajax({
        data: {"action": "gameState"},
        url: "activeGame",
        error: function () {
            console.error("Failed to get ajax response from startGame while try to get game state");
        },
        success: function (gameState) {
            $("#buttonSwitchPlayer").attr("disabled", true);
            if (gameState === "LOADED" || gameState === "INITIALIZED") {
                $("#requestOverlay").removeAttr("hidden");
                $("#waiting-for-opponent-msg").html("Waiting for another player to join the game");
            }
            else if (gameState === "STARTED" && currentPlayer !== activePlayer) {
                $("#requestOverlay").removeAttr("hidden");
                $("#waiting-for-opponent-msg").html("Waiting for <b>" + activePlayer + "</b> to make a move");
            }
            else if (gameState === "PLAYER_WON" || gameState === "PLAYER_QUIT") {
                clearInterval(quickRefreshInterval);
                clearInterval(refreshInterval);
                endGame(false);
            }
            else {
                $("#requestOverlay").attr("hidden", "");
            }
        }
    });
}

function endGame() {
    var message = "";
    clearInterval(quickRefreshInterval);
    clearInterval(refreshInterval);

    $.ajax({
        data: {"action": "endGame"},
        url: "activeGame",
        async: false,
        error: function () {
            console.error("Failed to get ajax response from startGame while getting the winner's name");
        },
        success: function (endGameResult) {
            if (endGameResult !== null) {

                if (endGameResult.winner === currentPlayer) {
                    message = "You win !! :)\n";
                } else {
                    message = "You lost :(\n";
                }

                message += endGameResult.scores[0].playerName + "'s score: " + endGameResult.scores[0].playerScore + "\n";
                message += endGameResult.scores[1].playerName + "'s score: " + endGameResult.scores[1].playerScore + "\n";
                message += "press OK to go back to games room";

                newAlert("Game Ended", message, "success", "OK", function () {
                    exitActiveGameScreen("../gamesRoom/gamesRoom.html");
                })
                // afterOK(logout, urlAfter);
                // alert(message);
            }
            else {
                exitActiveGameScreen("../gamesRoom/gamesRoom.html");
            }


        }
    });
}

function refreshPlayersTitle() {
    $.ajax({
        data: {"action": "activePlayer"},
        url: "activeGame",
        error: function () {
            console.error("Failed to get ajax response from startGame while trying refresh player names");
        },
        success: function (gamePlayers) {
            // debugger;
            currentPlayer = gamePlayers.currentPlayer;
            activePlayer = gamePlayers.activePlayer;
            numPlayersInGame = gamePlayers.numPlayersInGame;
            document.getElementById("thisPlayerName").innerHTML = currentPlayer;
            document.getElementById("activePlayerName").innerHTML = activePlayer;
        }
    });
}

function refreshBoardAndStatistic() {
    $.ajax({
        data: {"action": "gameDetails"},
        url: "activeGame",
        error: function () {
            console.error("Failed to get ajax response from startGame while try to get board and statistic");
        },
        success: function (gameDetails) {
            if (gameDetails !== null) {
                var imgSrc;
                tableSize = gameDetails.myBoard.boardSize;
                drawTables(myBoard.id, gameDetails.myBoard.boardView);
                drawTables(opponentBoard.id, gameDetails.opponentBoard.boardView);
                var minesAvailable = gameDetails.gameStatistics.minesRemaining;
                $("#numMinesAvailableText").text(minesAvailable);
                if (minesAvailable > 1) {
                    imgSrc = "../../images/Multiple mines.png";
                } else if (minesAvailable == 1) {
                    imgSrc = "../../images/Mine.png";
                } else {
                    imgSrc = "../../images/No mines available.png";
                    $("#minesAvailableImg").attr("draggable", "false");
                    $("#minesAvailableImg").css("cursor", "not-allowed")

                }

                $("#minesAvailableImg").attr("src", imgSrc);

                // draw sips state table
                var shipState
                $("#shipsStateTableBody").text("");
                $.each(gameDetails.gameStatistics.allShipsState, function (i, val) {
                    addRowToShipsStateTable(val);
                });
                // add statistics
                $("#totalMovesVal").text(gameDetails.gameStatistics.totalMoveCounter);
                $("#myScoreVal").text(gameDetails.gameStatistics.myScore);
                $("#opponentsScoreVal").text(gameDetails.gameStatistics.opponentScore);
                $("#avgTurnDurationVal").text(gameDetails.gameStatistics.averageTurnDuration);
                $("#hitsVal").text(gameDetails.gameStatistics.hitCounter);
                $("#missesVal").text(gameDetails.gameStatistics.missCounter);
            }
        }
    });
}

function addRowToShipsStateTable(shipState) {
    var tempRow = "<tr class='table-info'>\n";
    tempRow +=
        '<td style="padding: 10px">' + shipState.shipType + '</td>\n' +
        '<td style="padding: 10px" class="text-center">' + shipState.initAmount + '</td>\n' +
        '<td style="padding: 10px" class="text-center">' + shipState.myAmount + '</td>\n' +
        '<td style="padding: 10px" class="text-center">' + shipState.opponentAmount + '</td>\n';
    tempRow += "</tr>";
    $("#shipsStateTableBody").append(tempRow);
}

function drawTables(boardID, boardData) {
    var i, j;
    var tempRow;
    var dragAttributes = boardID === "myBoard" ? "ondrop='drop(event)' ondragover='allowDrop(event)'" : "";
    $("#" + boardID).html("");
    for (i = 0; i < tableSize; i++) {
        tempRow = "<tr row='" + (i + 1) + "'>\n";
        for (j = 0; j < tableSize; j++) {
            tempRow += "<td " +
                "col='" + j + "' " +
                "id='" + String.fromCharCode(97 + j) + (i + 1) + "' " +
                "style='" +
                "width: " + getCellDrawSize() + "px; " +
                "height: " + getCellDrawSize() + "px; " +
                "background-size: " + getCellDrawSize() + "px " + getCellDrawSize() + "px '" +
                "class='" + boardData[i][j] + " boardCell'" +
                "onclick='makeMove(this)'" +
                dragAttributes +
                "></td>\n";
        }
        tempRow += "</tr>";
        $("#" + boardID).append(tempRow);
    }
}

function getCellDrawSize() {
    return TABLE_DRAW_SIZE / tableSize;
}

function makeMove(caller) {
    //cellcoordinate
    if (caller.parentElement.parentElement.id === "opponentBoard") {
        $.ajax({
            data: {
                "action": "makeMove",
                "cellcoordinate": caller.id
            },
            url: "activeGame",
            error: function () {
                console.error("Failed to get ajax response from startGame while try to make a move");
            },
            success: function (attackResult) {
                alert("attacked cell (" + caller.id + ")" + "\n Attack result: " + attackResult);
                refreshAll();
            }
        });
    }
}

function setGameType() {
    $.ajax({
        data: {"action": "gameType"},
        url: "activeGame",
        error: function () {
            console.error("Failed to get ajax response get active game type");
        },
        success: function (gameType) {
            $("#gameType").html(gameType);
        }
    });
}

function exitActiveGameScreen(urlAfter) {
    debugger;
    $.ajax({
        data: {
            "action": "resetGame"
        },
        url: "activeGame",
        error: function () {
            console.error("Failed to get ajax response from startGame while try to return to main screen");
        },
        success: function () {
            window.location.href = urlAfter;
        }
    });
}

function newAlert(titleInput, textInput, typeInput, buttonTextInput, callbackFunc) {
    swal({
        closeOnClickOutside: false,
        html: true,
        title: titleInput,
        text: textInput,
        type: typeInput,
        button: buttonTextInput
    }).then(callbackFunc);
}

function drag(event) {
    event.dataTransfer.setData("text", "mine");
}

function drop(event) {
    debugger;
    event.preventDefault();
    var data = event.dataTransfer.getData("text");
    plantMine(event.target.id);
    // event.target.appendChild(document.getElementById(data));
}

function allowDrop(event) {
    event.preventDefault();
}

function plantMine(targetCell) {
    $.ajax({
        data: {
            "action": "plantMine",
            "targetCell": targetCell
        },
        url: "activeGame",
        error: function () {
            console.error("Failed to get ajax response from startGame while try to return to main screen");
        },
        success: function (result) {
            debugger;
            var alertMessage = "";

            if (result.minePlanted === "true") {
                alertMessage += "mine successfully placed on cell " + targetCell;
            }
            else {
                alertMessage += result.errorMessage;
            }

            alert(alertMessage);
            refreshAll();
        }
    });
}

function sendChatMessage(targetCell) {
    var message = document.getElementById('chatMessage').value;
    $.ajax({
        data: {
            "message": message
        },
        url: "sendChatMessage",
        error: function () {
            console.error("Failed to get ajax response from startGame while try to send chat message");
        },
        success: function (result) {
            $("#chatMessage").html="";
        }
    });
}

function updateNewChatMessages(chatVersion) {
    debugger;
    $.ajax({
        data: {
            "version": clientChatVersion
        },
        url: "getNewChatMessages",
        async: false,
        error: function () {
            console.error("Failed to get ajax response from startGame while try update chat message");
        },
        success: function (newChatMessages) {
            debugger;
            $.each(newChatMessages || [], appendChatMessage);
            debugger;
            clientChatVersion = chatVersion;

            debugger;

        }
    });
}

function appendChatMessage(index, chatMessage) {
    debugger;
    var message = chatMessage.playerName +"(" + chatMessage.messageTime+"): "+chatMessage.messageString;
    $("<p>" + message.toString() + "</p>").appendTo("#allChatMessages");
    $("<br>").appendTo("#allChatMessages");
    debugger;
}
