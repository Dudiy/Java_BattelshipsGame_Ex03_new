package consoleUI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import gameLogic.IGamesLogic;
import javafx.fxml.LoadException;
import gameLogic.exceptions.*;
import gameLogic.game.*;
import gameLogic.users.*;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.GamesManager;

public class ConsoleUIManager {
    private IGamesLogic gamesManager = new GamesManager();
    // console application may have only 1 game
    private Game activeGame;
    private Menu menu = new Menu();
    private BoardPrinter boardPrinter = new BoardPrinter();
    private Scanner scanner = new Scanner(System.in);
    private boolean exitGameSelected = false;
    private int computerPlayerIndex = 0;

    public void run() {
        printWelcomeScreen();

        do {
            try {
                if (activeGame != null &&
                        activeGame.getGameState() == eGameState.STARTED &&
                        activeGame.getActivePlayer() instanceof ComputerPlayer) {
                    makeMove();
                } else {
                    eMenuOption menuItemSelected = menu.display(activeGame);
                    invokeMenuItem(menuItemSelected);
                }
            } catch (Exception e) {
                System.out.println("Error: while invoking menu item. game will restart");
                activeGame = null;
            } finally {
                pressAnyKeyToContinue();
            }
        } while (!exitGameSelected);
    }

    private void invokeMenuItem(eMenuOption menuItemSelected) {
        switch (menuItemSelected) {
            case LOAD_GAME:
                loadGame();
                break;
            case START_GAME:
                startGame();
                break;
            case SHOW_GAME_STATE:
                showGameState();
                break;
            case MAKE_MOVE:
                makeMove();
                break;
            case SHOW_STATISTICS:
                showStatistics();
                break;
            case END_GAME:
                endGame();
                break;
            case PLANT_MINE:
                plantMine();
                break;
            case SAVE_GAME:
                saveGame();
                break;
            case LOAD_SAVED_GAME:
                loadSavedGame();
                break;
            case PLAY_AGAINST_COMPUTER:
                playAgainstComputer();
                break;
            case EXIT:
                exit();
                break;
        }
    }

    private void playAgainstComputer() {
        setComputerPlayer();

    }

    // ======================================= Load game =======================================
    private void loadGame() {
        try {
            String path = getFilePathFromUser();
            if (path != null) {
                activeGame = gamesManager.loadGameFile(path);
                System.out.println("game loaded");
            }
        } catch (LoadException e) {
            System.out.println("Error while loading game: " + e.getMessage() + ". Please try again.");
        }
        catch (UserSelectedCancelException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getFilePathFromUser() throws UserSelectedCancelException {
        String path;
        File file;
        boolean endOfInput = false;

        do {
            path = getInputFromUser("Please enter an XML path file");

            if (path != null) {
                file = openFileFromPath(path);
                if (file != null) {
                    if (checkFileType(file, "text/xml")) {
                        endOfInput = true;
                    } else {
                        System.out.println("Error: file type mismatch");
                    }

                } else {
                    System.out.println("Error: file doesn't exist");
                }
            } else {
                endOfInput = true;
            }
        } while (!endOfInput);

        return path;
    }

    private File openFileFromPath(String filePath) {
        File file = new File(filePath);

        if (!file.exists() || file.isDirectory()) {
            file = null;
        }

        return file;
    }

    private boolean checkFileType(final File file, final String fileTypeToCompare) {
        String fileType;
        boolean sameType = false;

        try {
            fileType = Files.probeContentType(file.toPath());
            sameType = fileType.equals(fileTypeToCompare);
        } catch (IOException ioException) {
            System.out.println("Error: Unable to determine file type for " + file.getName());
        }

        return sameType;
    }

    // ======================================= Start game =======================================
    private void startGame() {
        try {
            ComputerPlayer computerPlayer = computerPlayerIndex == 0 ? null :
                    new ComputerPlayer("ComputerPlayer2", "Computer Player", activeGame.getSizeOfMinimalShipOnBoard());
            Player player1 = computerPlayerIndex == 1 ?
                    computerPlayer :
                    new Player("P1", "Player 1");
            Player player2 = computerPlayerIndex == 2 ?
                    computerPlayer :
                    new Player("P2", "Player 2");

            gamesManager.startGame(activeGame, player1, player2);
            if (computerPlayerIndex == 1) {
                activeGame.swapPlayers();
            }
            setInitialValuesForPlayer();
            // give each player 2 mines
            player1.getMyBoard().setMinesAvailable(2);
            player2.getMyBoard().setMinesAvailable(2);
            showGameState();
            System.out.println("game started");
        } catch (InvalidGameObjectPlacementException e) {
            String message = "\nError while initializing board.\n" +
                    "Cannot place a given " + e.getGameObjectType() + " at position " + e.GetCoordinates() + ".\n" +
                    "reason: " + e.getReason() + "\n";
            System.out.println(message);
            errorWhileStartingGame();
        } catch (Exception e) {
            System.out.println("Error while starting game. " + e.getMessage());
            errorWhileStartingGame();
        }
    }

    private void setInitialValuesForPlayer() {
        Player[] players = activeGame.getPlayers();

        for (Player player : players){
            player.getMyBoard().setMinesAvailable(2);
            player.setActiveShipsOnBoard(activeGame.getGameSettings().getShipAmountsOnBoard());
        }
    }

    private void errorWhileStartingGame() {
        activeGame = null;
        System.out.println("game file given was invalid therefor it was not loaded. \nPlease check the file and try again.");
    }

    // ======================================= Show game State =======================================
    private void showGameState() {
        System.out.println("game state:");
        printPlayerBoard(activeGame.getActivePlayer(), !BoardPrinter.PRINT_SINGLE_BOARD);
    }

    private void printPlayerBoard(Player player, boolean printSingleBoard) {
        System.out.println("Player: " + player.getName());
        System.out.println("Score: " + player.getScore());
        boardPrinter.printBoards(player, printSingleBoard);
    }

    // ======================================= Make Move =======================================
    private void makeMove() {
        Instant startTime = Instant.now();
        BoardCoordinates positionToAttack;
        eAttackResult attackResult = null;
        boolean moveEnded = false;
        boolean printGameState = true;
        boolean currentPlayerIsComputer;

        // get active player before players are swapped
        Player activePlayer = activeGame.getActivePlayer();
        do {
            try {
                currentPlayerIsComputer = activePlayer instanceof ComputerPlayer;
                if (printGameState &&
                        attackResult != eAttackResult.CELL_ALREADY_ATTACKED
                        && !currentPlayerIsComputer) {
                    showGameState();
                }

                positionToAttack = currentPlayerIsComputer ?
                        ((ComputerPlayer) activePlayer).getNextPositionToAttack() :
                        getPositionFromUser();
                attackResult = gamesManager.makeMove(activeGame, positionToAttack);
                moveEnded = attackResult.moveEnded() || activeGame.getGameState() == eGameState.PLAYER_WON;

                if (!currentPlayerIsComputer) {
                    System.out.println("Attack result: " + attackResult);
                    if (!moveEnded) {
                        pressAnyKeyToContinue();
                    }
                } else {
                    if (moveEnded) {
                        printComputerAttackLog((ComputerPlayer) activePlayer);
                    }
                }
            } catch (CellNotOnBoardException e) {
                System.out.println("The cell selected is not on the board, try again");
                printGameState = false;
            } catch (ComputerPlayerException e) {
                System.out.println(e.getMessage());
                break;
            }
        } while (!moveEnded);

        Duration turnTime = Duration.between(startTime, Instant.now());
        activePlayer.addTurnDurationToTotal(turnTime);
        System.out.println(String.format("Total duration for this turn was: %d:%02d", turnTime.toMinutes(), turnTime.getSeconds() % 60));

        if (activeGame.getGameState() == eGameState.PLAYER_WON) {
            onGameEnded(eGameState.STARTED);
        }
    }

    private void printComputerAttackLog(ComputerPlayer computerPlayer) {
        for (String loggedAttackResult : computerPlayer.getMovesLog()) {
            System.out.println(loggedAttackResult);
        }
        computerPlayer.clearMovesLog();
    }

    private BoardCoordinates getPositionFromUser() {
        BoardCoordinates userSelection = null;
        boolean isValidSelection = false;

        while (!isValidSelection) {
            try {
                System.out.print("Please select cell coordinates (format = \"A1\"): ");
                userSelection = BoardCoordinates.Parse(scanner.nextLine());
                isValidSelection = true;
            } catch (Exception e) {
                System.out.println("Invalid input please try again. error: " + e.getMessage());
            }
        }

        return userSelection;
    }

    // ======================================= Show Statistics =======================================
    private void showStatistics() {
        System.out.println("***** Showing game statistics: *****");
        // total turns played
        System.out.println("\tTotal turns played: " + activeGame.getMovesCounter());
        // total game duration
        Duration gameDuration = gamesManager.getGameDuration(activeGame);
        String durationStr = String.format("%d:%02d", gameDuration.toMinutes(), gameDuration.getSeconds() % 60);
        System.out.println("\tTotal game time: " + durationStr);
        System.out.println("\t* If a saved game was loaded, then time since the game was loaded *");
        // player info
        System.out.println("\n\t***** Player statistics *****");
        showPlayerStatistics(activeGame.getActivePlayer());
        System.out.println();
        showPlayerStatistics(activeGame.getOtherPlayer());
        System.out.println();
    }

    private void showPlayerStatistics(Player player) {
        System.out.println("\t\tShowing player statistics for " + player.getName());
        System.out.println("\t\tCurrent score: " + player.getScore());
        System.out.println("\t\tTimes missed: " + player.getTimesMissed());
        // avg turn duration
        Duration avgDuration = player.getAvgTurnDuration();
        System.out.println(String.format("\t\tAverage turn duration: %d:%02d", avgDuration.toMinutes(), avgDuration.getSeconds() % 60));
    }

    // ======================================= End game =======================================
    private void endGame() {
        eGameState gameStateBeforeEndGame = activeGame.getGameState();
        gamesManager.endGame(activeGame);
        onGameEnded(gameStateBeforeEndGame);
    }

    private void onGameEnded(eGameState stateBeforeEndingGame) {
        if (stateBeforeEndingGame.gameHasStarted()) {
            printPlayerWonScreen(activeGame.getWinnerPlayer().getName());
            pressAnyKeyToContinue();
            showStatistics();
            System.out.println("Player boards:");
            printPlayerBoard(activeGame.getActivePlayer(), BoardPrinter.PRINT_SINGLE_BOARD);
            printPlayerBoard(activeGame.getOtherPlayer(), BoardPrinter.PRINT_SINGLE_BOARD);
            System.out.println("The current game has ended...resetting the game");
        }

        // set the game to be as if it was just started
        resetGame(activeGame);
    }

    // ======================================= Plant mine =======================================
    private void plantMine() {
        boolean minePlantedOrNotAvailable = false;
        BoardCoordinates position;
        printPlayerBoard(activeGame.getActivePlayer(), !BoardPrinter.PRINT_SINGLE_BOARD);

        while (!minePlantedOrNotAvailable) {
            try {
                position = getPositionFromUser();
                gamesManager.plantMine(activeGame, position);
                minePlantedOrNotAvailable = true;
                System.out.println("You have successfully planted a mine at position " + position.toString() + " ;)");
            } catch (CellNotOnBoardException | InvalidGameObjectPlacementException e) {
                System.out.println(e.getMessage());
            } catch (NoMinesAvailableException e) {
                minePlantedOrNotAvailable = true;
                System.out.println(e.getMessage());
            }
        }
    }

    // ======================================= Save game =======================================
    private void saveGame() {
        String fileName;
        try {
            fileName = getInputFromUser("Please enter a file name for saving:");

            if (!fileName.isEmpty()) {
                fileName = GameSettings.SAVED_GAME_DIR + fileName + GameSettings.SAVED_GAME_EXTENSION;
                try {
                    gamesManager.saveGameToFile(activeGame, fileName);
                    System.out.println("game saved successfully!");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } else {
                System.out.println("Filename can not be empty, the game was not saved");
            }
        } catch (UserSelectedCancelException e) {
            System.out.println(e.getMessage());
        }
    }

    // ======================================= Load saved game=======================================
    private void loadSavedGame() {
//        String fileName = getInputFromUser("Please enter a file name for the loading the saving file:");
        String fileName;

        try {
            HashMap<Integer, String> savedGamesList = getSavedGamesList();
            fileName = getGameToLoadFromUser(savedGamesList);
            activeGame = gamesManager.loadSavedGameFromFile(fileName);
            activeGame.setGameStartTime(Instant.now());
            System.out.println("game loaded from file!");
        } catch (UserSelectedCancelException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error while loading saved games: " + e.getMessage());
        }
    }

    private HashMap<Integer, String> getSavedGamesList() throws LoadException {
        HashMap<Integer, String> savedGamesList = new HashMap<>();
        File savedGamesDir = new File(GameSettings.SAVED_GAME_DIR);
        int fileCounter = 0;

        if (savedGamesDir.exists()) {
            for (File file : savedGamesDir.listFiles(File::isFile)) {
                fileCounter++;
                savedGamesList.put(fileCounter, file.getName());
            }
        } else {
            throw new LoadException("No \"Saved Games\" directory found");
        }

        if (fileCounter == 0) {
            throw new LoadException("No saved game file found in \"Saved Games\" directory");
        }

        return savedGamesList;
    }

    private String getGameToLoadFromUser(HashMap<Integer, String> savedGamesList) throws UserSelectedCancelException {
        boolean isValidSelection = false;
        String selectedFileName = null;
        System.out.println("Saved games available :");

        for (Map.Entry<Integer, String> savedGame : savedGamesList.entrySet()) {
            System.out.println(savedGame.getKey() + ") " + savedGame.getValue());
        }

        while (!isValidSelection) {
            try {
                String inputStr = getInputFromUser("Please select an index of a game to load: ");
                int input = Integer.parseInt(inputStr);
                selectedFileName = savedGamesList.get(input);
                if (selectedFileName == null) {
                    System.out.println("Invalid selection, please select the index of one of the files above");
                } else {
                    isValidSelection = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input format please input a number");
            } catch (UserSelectedCancelException e) {
                throw new UserSelectedCancelException();
            } catch (Exception e) {
                System.out.println("Invalid selection, please select the index of one of the files above");
            }
        }

        return GameSettings.SAVED_GAME_DIR + selectedFileName;
    }

    // ======================================= Exit =======================================
    private void exit() {
        if (activeGame != null && activeGame.getGameState() != eGameState.INVALID) {
            endGame();
        }
        exitGameSelected = true;
        printGoodbyeScreen();
    }

    // ======================================= Other methods =======================================
    private void pressAnyKeyToContinue() {
        System.out.println("\n--- Press enter to continue ---");
        scanner.reset();
        scanner.nextLine();
    }

    private String getInputFromUser(String title) throws UserSelectedCancelException {
        System.out.print(title + "(Or 0 to return to main menu): ");
        String inputFromUser = scanner.nextLine();
        if (inputFromUser.equals("0")) {
            throw new UserSelectedCancelException();
        }

        return inputFromUser;
    }

    private void printWelcomeScreen() {
        System.out.println(" ~~~~~ WELCOME TO THE MOST AMAZING BATTLESHIP GAME OF ALL! ~~~~~\n\n");
        System.out.println("                      ,:',:`,:'");
        System.out.println("                   __||_||_||_||__");
        System.out.println("              ____[\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"]____");
        System.out.println("              \\ \" '''''''''''''''''''' |");
        System.out.println("       ~~~~~~^~^~^^~^~^~^~^~^~^~^~~^~^~^^~~^~^");
/*
                   ,:',:`,:'
                __||_||_||_||__
           ____["""""""""""""""]____
           \ " '''''''''''''''''''' |
    ~~~~~~^~^~^^~^~^~^~^~^~^~^~~^~^~^^~~^~^
*/
    }

    private void printGoodbyeScreen() {
        System.out.println("Thank you for playing, goodbye!\n");
        System.out.println("                  /\\/\\,\\,\\ ,");
        System.out.println("                 /        ` \\'\\,");
        System.out.println("                /               '/|_");
        System.out.println("               /                   /");
        System.out.println("              /                   /");
        System.out.println("             /                   ;");
        System.out.println("             ;-\"\"-.  ____       ,");
        System.out.println("            /      )'    `.     '");
        System.out.println("           (    o |        )   ;");
        System.out.println("            ),'\"\"\"\\    o   ;  :");
        System.out.println("            ;\\___  `._____/ ,-:");
        System.out.println("           ;                 @ )");
        System.out.println("          /                `;-'");
        System.out.println("       ,. `-.______________,|");
        System.out.println("  ,(`._||         \\__\\__\\__)|");
        System.out.println(" ,`.`-   \\        '.        |");
        System.out.println("  `._  ) :          )______,;\\_");
        System.out.println("     \\    \\_   _,--/       ,   `.");
        System.out.println("      \\     `--\\   :      /      `.");
        System.out.println("       \\        \\  ;     |         \\");
        System.out.println("        `-._____ ;|      |       _,'");
        System.out.println("                \\/'      `-.----' \\");
        System.out.println("                 /          \\      \\");
        System.out.println();
    }

    private void printPlayerWonScreen(String winner) {
        System.out.println("\n~~~~~ Good job " + winner + " you have won the battle! ~~~~~\n");
        System.out.println("                  _,----.");
        System.out.println("               ,-'     __`.");
        System.out.println("              /    .  /--\\`)");
        System.out.println("             /  .  )\\/_,--\\");
        System.out.println("            /  ,'\\/,-'    _\\_");
        System.out.println("           |  /  ,' ,---'  __\\");
        System.out.println("          ,' / ,:     _,-\\'_,(");
        System.out.println("           (/ /  \\ \\,'   |'  _)         ,. ,.,.");
        System.out.println("            \\/   |          '  \\        \\ ,. \\ )");
        System.out.println("             \\, ,-              \\       /,' )//");
        System.out.println("              ; \\'`      _____,-'      _|`  ,'");
        System.out.println("               \\ `\"\\    (_,'_)     _,-'    ,'");
        System.out.println("                \\   \\       \\  _,-'       ,'");
        System.out.println("                |, , )       `'       _,-'");
        System.out.println("                /`/ Y    ,    \\   _,-'");
        System.out.println("                   :    /      \\-'");
        System.out.println("                   |     `--.__\\___");
        System.out.println("                   |._           __)");
        System.out.println("                   |  `--.___    _)");
        System.out.println("                   |         `----'");
        System.out.println("                  /                \\");
        System.out.println("                 '                . )");
        System.out.println();
    }

    private void setComputerPlayer() {
        boolean isValidSelection = false;

        do {
            try {
                System.out.println("Which player would you like the computer to be (1 or 2)?");
                System.out.println("* enter 3 if you would like to with another human player *");
                int selectedPlayerIndex = scanner.nextInt();
                if (selectedPlayerIndex == 1 || selectedPlayerIndex == 2) {
                    computerPlayerIndex = selectedPlayerIndex;
                    isValidSelection = true;
                } else if (selectedPlayerIndex == 3) {
                    computerPlayerIndex = 0;
                    isValidSelection = true;
                } else {
                    System.out.println("Invalid selection, Please enter \"1\" or \"2\"");
                    scanner.nextLine();
                }
            } catch (Exception e) {
                System.out.println("Error while selecting player: " + e.getMessage() + ". Please enter \"1\" or \"2\"");
                scanner.nextLine();
            }
        } while (!isValidSelection);
    }

    private void resetGame(Game activeGame) {
        try {
            activeGame.resetGame();
        } catch (Exception e) {
            System.out.println(e.getMessage() + ".\nPlease load the file again or choose another file");
            activeGame.setGameState(eGameState.INVALID);
        }
    }
}