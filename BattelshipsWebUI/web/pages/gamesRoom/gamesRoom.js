var clientPlayersVersion = 0;
var clientGamesVersion = 0;
var REFRESH_RATE = 2000;

// on page load
$(function () {
    // setTitle();
    refreshAll();
    //prevent IE from caching ajax calls
    $.ajaxSetup({cache: false});
    // will execute the method every REFRESH_RATE milliseconds
    setInterval(refreshAll, REFRESH_RATE);
});

function uploadFile() {
    var file_data = $("#gamefile").prop("files")[0]; // Getting the properties of file from file field
    var form_data = new FormData(); // Creating object of FormData class
    var gameTitle = "";
    swal({
        title: "Please input game title:",
        content: "input",
        closeOnClickOutside: false
    }).then(function (value) {
        gameTitle += value;
        form_data.append("file", file_data); // Appending parameter named file with properties of file_field to form_data
        form_data.append("gameTitle", gameTitle);
        $.ajax({
            url: "addGameFromFile", // Upload Script
            cache: false,
            contentType: false,
            processData: false,

            data: form_data, // Setting the data attribute of ajax with file_data
            type: 'post',
            error: function () {
                console.error("Failed to get ajax response from startGame while try to add new game");
            },
            success: function (response) {
                if (response.validFile) {
                    swal("Game loaded successfully", {icon: "success"})
                }
                else {
                    swal(response.loadResult, {icon: "error"});
                }

                swal(response.loadResult, {icon: response.validFile ? "success" : "error"})
                // alert(response.loadResult)
            }
        });
    });
}

function refreshAll() {
    refreshPlayersList();
    refreshGamesList();
}

function refreshPlayersList() {
    $.ajax({
        data: "typeInfo=allPlayersName",
        url: "onlinePlayers",
        error: function () {
            console.error("Failed to get ajax response from startGame while try to get all players name");
        },
        success: function (response) {
            if (clientPlayersVersion !== response.playersVersion) {
                $("#active-players-table").empty();
                $.each(response.playersName || [], addPlayer);
            }
            console.log("my version " + clientPlayersVersion + ", server version " + response.playersVersion);
            clientPlayersVersion = response.playersVersion;
        }
    });
}

function addPlayer(index, playerName) {
    $('<tr><td style="padding: 15px">-  ' + playerName + '</td></tr>').appendTo($("#active-players-table"));
}

function refreshGamesList() {
    debugger;
    $.ajax({
        url: "ServletAllGames",
        error: function () {
            console.error("Failed to get ajax response from startGame while try to get all games name");
        },
        success: function (response) {
            if (clientGamesVersion !== response.gamesVersion) {
                $("#gamesList").empty();
                $.each(response.gamesDetails || [], addGame);
            }
            console.log("my version " + clientGamesVersion + ", server version " + response.gamesVersion);
            clientGamesVersion = response.gamesVersion;
        }
    });
}

function addGame(index, game) {
    debugger;
    var joinGameDisabledValue = (game.gameState === "INITIALIZED" || game.gameState === "LOADED") ? "" : "disabled";
    var deleteGameDisabledValue = (game.creatorName === game.activePlayerFromSession && game.activePlayers[0] === null && game.activePlayers[1] === null) ? "" : "disabled";
    $('<tr id=' + game.gameID + ' class="activeGame">' +
        '<td>' + game.gameID + '</td>' +
        '<td>' + game.gameTitle + '</td>' +
        '<td>' + game.creatorName + '</td>' +
        '<td>' + game.boardSize + '</td>' +
        '<td>' + game.gameType + '</td>' +
        '<td class="activePlayersCell">' + addActivePlayerInGame(game.activePlayers, game.gameID) + '</td>' +
        '<td align="center">' +
        '   <button style="margin: 5px" class="btn-sm btn-success" ' + joinGameDisabledValue + ' onclick="joinGame(' + game.gameID + ')"> Join Game </button>' +
        '   <button style="margin: 5px" class="btn-sm btn-danger" ' + deleteGameDisabledValue + ' onclick="swalDeleteAlert(' + game.gameID + ')"> Delete Game </button>' +
        '   <button style="margin: 5px" class="btn-sm btn-warning" title="Bonus option - not implemented" disabled> View Game </button>' +
        '</td>' +
        '</tr>').appendTo($("#gamesList"));
    colorActiveGames();
}

function addActivePlayerInGame(activePlayers, gameID) {
    var activePlayerString;

    activePlayerString = "";
    if (activePlayers[0] !== null) {
        activePlayerString += activePlayers[0];
    }

    if (activePlayers[1] !== null) {
        activePlayerString += ", " + activePlayers[1];
    }

    if (activePlayers[0] !== null && activePlayers[1] !== null) {
        activePlayerString += " (active game)";
    }

    return activePlayerString;
}

function colorActiveGames() {
    var games = $(".activeGame");
    $.each(games || [], function (index, game) {
        debugger;
        var activePlayers = game.getElementsByClassName("activePlayersCell")[0].innerHTML;
        if (activePlayers.indexOf("active game") !== -1) {
            $("#" + game.id).addClass("table-success");
        }
        else {
            if ($("#" + game.id).hasClass("table-success")) {
                $("#" + game.id).removeClass("table-success");
            }
        }
        // if (activePlayers[0] !== null && activePlayers[1] !== null) {
        //     activePlayerString += " (active game)";
        //     $("#" + gameID).addClass("table-success");
        // }
        // else {
        //     $("#" + gameID).removeClass("table-success");
        // }
    });
}

function logout() {
    $.ajax({
        data: {"action": "logout"},
        url: "../login/login",
        async: false,
        success: function (response) {
            swal("Logged out", {icon: "success"});
            // alert("logged out");
            window.location.href = "../login/login.html";
        }
    });
}

function joinGame(gameId) {
    debugger;
    $.ajax({
        data: {"gameID": gameId.toString()},
        url: "joinGame",
        async: false,
        success: function (response) {
            refreshGamesList();
            // alert("joined game....going to game screen");
            location.href = "../activeGame/activeGame.html";
        }
    });
}

function deleteGame(gameId) {
    debugger;
    swalDeleteAlert();
    $.ajax({
        data: {"gameID": gameId.toString()},
        url: "deleteGame",
        async: false,
        success: function (response) {
            // alert("game deleted");
            swal("File deleted", {icon: "success"});
            refreshGamesList();
        }
    });
}

function swalDeleteAlert(gameId) {
    swal({
        title: "Delete Game",
        text: "are you sure you would like to delete this game?",
        icon: "warning",
        buttons: true,
        closeOnClickOutside: false,
        dangerMode: true
    }).then(function (selection) {
        if (selection) {
            deleteGame(gameId)
        }
        else {
            swal("The game was not deleted")
        }
    });
}