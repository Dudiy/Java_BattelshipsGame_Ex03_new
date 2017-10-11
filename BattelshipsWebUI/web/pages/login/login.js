
$(function () {
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

//                        timeout: 2000,
//                        error: function (err) {
//                            // TODO check error message/type
//                            $(".form-control-feedback").text("The name " + nameInput + " is already taken, please try again");
//                            $("#playerName").addClass("form-control-danger");
//                            $("#playerInput").addClass("has-danger");
//                        },
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
                        var continueAsLoggedInUser = confirm(response);
                        if (continueAsLoggedInUser){
                            window.location.href = "../gamesRoom/gamesRoom.html";
                        }
                        else{
                            $.ajax({
                                data:{"playername": nameInput, "action": "logout"},
                                url: "login",
                                async: false,
                                success: function (response) {
                                    // alert("logged out");
                                    swal("Logged out", {icon: "success"});
                                }
                            });
                            submitFunc(nameInput)
                        }
                    }
                    else {
                        window.location.href = "../gamesRoom/gamesRoom.html";
                    }
                }
            });
        }
    }

    $("#submit").click(function () {
        submitFunc($("#playerName").val());
    })
});
