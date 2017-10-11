function submitFunc(nameInput) {
    if (nameInput === "") {
        $(".form-control-feedback").text("Name is required, please input and try again");
        $("#playerName").addClass("form-control-danger");
        $("#playerInput").addClass("has-danger");
    }
    else {
        $.ajax({
            data: {"playername": nameInput, "action": "login"},
            url: "login",
            async: false,

            error: function (err) {
                $(".form-control-feedback").html("Error while trying to login: <hr/>" + err.responseText + "<hr/> Please try again");
                $("#playerName").addClass("form-control-danger");
                $("#playerInput").addClass("has-danger");
            },
            success: function (response) {
                debugger;
                if (response === "no user name entered") {

                }
                else if (response.indexOf("already exists") !== -1) {
                    $(".form-control-feedback").text("The name " + nameInput + " is already taken, please try again");
                    $("#playerName").addClass("form-control-danger");
                    $("#playerInput").addClass("has-danger");
                }
                else if (response.indexOf("already logged in") !== -1) {
                    // var continueAsLoggedInUser = confirm(response);
                    loggedInAsAnotherUser(nameInput, response);
                }
                else {
                    window.location.href = "../gamesRoom/gamesRoom.html";
                }
            }
        });
    }
}

function loggedInAsAnotherUser(nameInput, alreadyLoggedInMessage) {
    swal({
        title: "Already logged in",
        text: alreadyLoggedInMessage,
        icon: "warning",
        buttons: {
            continueASLoggedIn: "Continue as logged in user",
            switchUser: "Switch logged in user"
        },
        closeOnClickOutside: false,
        dangerMode: true
    }).then(function (res) {
        switch (res) {
            case "continueASLoggedIn":
                window.location.href = "../gamesRoom/gamesRoom.html";
                break;
            case "switchUser":
                $.ajax({
                    data: {"playername": nameInput, "action": "logout"},
                    url: "login",
                    async: false,
                    success: function () {
                        swal({
                            title: "Logged Out",
                            text: "Log out successful, click ok to continue as " + nameInput,
                            icon: "success"
                        })
                            .then(function () {
                                submitFunc(nameInput);
                            });
                    }
                });
                break;
        }
    });
}