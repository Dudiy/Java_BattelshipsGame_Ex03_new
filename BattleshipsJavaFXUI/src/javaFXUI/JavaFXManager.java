package javaFXUI;

import gameLogic.*;
import gameLogic.exceptions.*;
import gameLogic.game.*;
import gameLogic.game.board.BoardCell;
import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.gameObjects.Mine;
import gameLogic.game.gameObjects.ship.AbstractShip;
import gameLogic.users.*;
import javaFXUI.model.AlertHandlingUtils;
import javaFXUI.model.ImageViewProxy;
import javaFXUI.model.PlayerAdapter;
import javaFXUI.model.ReplayGame;
import javaFXUI.view.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JavaFXManager extends Application {
    // ============== stages and layouts ==============
    private Stage primaryStage;
    private final Stage secondaryStage = new Stage();
    private final Stage playerInitializerStage = new Stage();
    private final Stage gameEndedStage = new Stage();
    private AnchorPane welcomeScreen;
    private BorderPane mainWindowLayout;
    private Scene mainWindowScene;
    private Scene pauseWindowScene;
    private Scene playerInitializerScene;
    private Scene gameEndedScene;
    private Scene welcomeScreenScene;
    // ============== controllers ==============
    private MainWindowController mainWindowController;
    private LayoutCurrentTurnInfoController currentTurnInfoController;
    private PlayerInitializerController playerInitializerController;
    private GameEndedLayoutController gameEndedLayoutController;
    private WelcomeSceneController welcomeSceneController;
    // ============== properties ==============
    private final Property<eGameState> gameState = new SimpleObjectProperty<>();
    private final Property<Game> activeGame = new SimpleObjectProperty<>();
    private final Property<Player> activePlayer = new SimpleObjectProperty<>();
    private final Property<eAttackResult> attackResult = new SimpleObjectProperty<>();
    private final IntegerProperty totalMovesCounter = new SimpleIntegerProperty();
    // ============== other members ==============
    private final IGamesLogic gamesManager = new GamesManager();
    private FXMLLoader fxmlLoader;
    private final List<Runnable> resetGameEvent = new ArrayList<>();
    private Instant turnPlayerTimer;
    private LinkedList<ReplayGame> previousMoves;
    private LinkedList<ReplayGame> nextMoves;
    private int currReplayIndex;
    private ReplayGame.eReplayStatus lastReplayCommand;
    private boolean animationsDisabled;
    private String styleSheetURL;
    private Thread javaFxManagerThread = Thread.currentThread();

    // ===================================== Init =====================================
    static void Run(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Battleships by Or and Dudi");
        this.primaryStage.getIcons().add(new Image(Constants.GAME_ICON_URL));
        initMainWindow();
        welcomeSceneController.gameTaskFinishedProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            if (newValue) {
                onLoadGameComplete();
            }
        }));
        initPlayerInitializerWindow();
        initGameEndedWindow();
        initPauseWindow();
        previousMoves = new LinkedList<>();
        nextMoves = new LinkedList<>();
    }

    private void initGameEndedWindow() {
        try {
            fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(JavaFXManager.class.getResource("/javaFXUI/view/GameEndedLayout.fxml"));
            AnchorPane gameEndedLayout = fxmlLoader.load();
            gameEndedLayoutController = fxmlLoader.getController();

            gameEndedScene = new Scene(gameEndedLayout);
            gameEndedStage.setScene(gameEndedScene);
            gameEndedStage.setResizable(false);
            gameEndedStage.initOwner(primaryStage);
            gameEndedStage.initStyle(StageStyle.UTILITY);
        } catch (IOException e) {
            AlertHandlingUtils.showErrorMessage(e, "Error while initializing game ended window");
        }
    }

    private void initMainWindow() {
        try {
            loadMainWindow();
            loadRightPane();
            mainWindowScene = new Scene(mainWindowLayout);

            loadWelcomeScreen();
            welcomeScreenScene = new Scene(welcomeScreen);
            primaryStage.setScene(welcomeScreenScene);
            primaryStage.getIcons().add(new Image(JavaFXManager.class.getResourceAsStream(Constants.GAME_ICON_IMAGE_URL)));
            primaryStage.show();
        } catch (IOException e) {
            AlertHandlingUtils.showErrorMessage(e, "Error while loading main window");
        }
    }

    private void loadMainWindow() throws IOException {
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(JavaFXManager.class.getResource("/javaFXUI/view/MainWindow.fxml"));
        mainWindowLayout = fxmlLoader.load();
        mainWindowController = fxmlLoader.getController();
        mainWindowController.setJavaFXManager(this);
        resetGameEvent.add(mainWindowController::resetGame);
        resetGameEvent.add(() -> {
            previousMoves.clear();
            nextMoves.clear();
            mainWindowController.setReplayMode(false);
            currentTurnInfoController.setReplayMode(false);
        });
    }

    private void loadRightPane() throws IOException {
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(JavaFXManager.class.getResource("/javaFXUI/view/LayoutCurrentTurnInfo.fxml"));
        ScrollPane rightPane = fxmlLoader.load();
        currentTurnInfoController = fxmlLoader.getController();
        currentTurnInfoController.setJavaFXManager(this);
        mainWindowLayout.setRight(rightPane);
    }

    private void loadWelcomeScreen() throws IOException {
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(JavaFXManager.class.getResource("/javaFXUI/view/WelcomeScene.fxml"));
        welcomeScreen = fxmlLoader.load();
        welcomeSceneController = fxmlLoader.getController();
        welcomeSceneController.setJavaFXManager(this);
    }

    private void initPlayerInitializerWindow() throws IOException {
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(JavaFXManager.class.getResource("/javaFXUI/view/PlayerInitializer.fxml"));
        AnchorPane playerInitializerLayout = fxmlLoader.load();
        playerInitializerController = fxmlLoader.getController();
        playerInitializerController.setOwnerWindow(primaryStage, this);

        playerInitializerScene = new Scene(playerInitializerLayout);
        playerInitializerStage.setScene(playerInitializerScene);
        playerInitializerStage.setOnCloseRequest(event -> playerInitializerController.updatePlayerInfo());
        playerInitializerStage.initOwner(primaryStage);
        playerInitializerStage.initStyle(StageStyle.UTILITY);
        playerInitializerStage.setResizable(false);
        playerInitializerStage.initModality(Modality.WINDOW_MODAL);
    }

    private void initPauseWindow() {
        try {
            fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(JavaFXManager.class.getResource("/javaFXUI/view/PauseWindow.fxml"));
            StackPane pauseWindowLayout = fxmlLoader.load();
            PauseWindowController pauseWindowController = fxmlLoader.getController();
            pauseWindowController.setJavaFXManager(this);

            pauseWindowScene = new Scene(pauseWindowLayout);
            secondaryStage.setScene(pauseWindowScene);
            secondaryStage.initOwner(primaryStage);
            secondaryStage.initModality(Modality.WINDOW_MODAL);
            secondaryStage.setResizable(false);
            secondaryStage.initStyle(StageStyle.UNDECORATED);
            secondaryStage.setTitle("Game Paused");
            secondaryStage.getIcons().add(new Image(JavaFXManager.class.getResourceAsStream("/resources/images/gameIcon.png")));
            showPauseMenu();
        } catch (IOException e) {
            AlertHandlingUtils.showErrorMessage(e, "Error while loading pause window");
        }
    }

    // ===================================== Getters =====================================
    public Property<Game> getActiveGame() {
        return activeGame;
    }

    public Property<eGameState> gameStateProperty() {
        return gameState;
    }

    public Property<Player> activePlayerProperty() {
        return activePlayer;
    }

    public IntegerProperty totalMovesCounterProperty() {
        return totalMovesCounter;
    }

    public Stage getSecondaryStage() {
        return secondaryStage;
    }

    public Property<eAttackResult> attackResultProperty() {
        return attackResult;
    }

    public LinkedList<ReplayGame> getPreviousMoves() {
        return previousMoves;
    }

    public LinkedList<ReplayGame> getNextMoves() {
        return nextMoves;
    }

    public boolean getAnimationsDisabled() {
        return animationsDisabled;
    }

    public Thread getJavaFxManagerThread() {
        return javaFxManagerThread;
    }

    // ===================================== Setters =====================================

    public void setAnimationsDisabled(boolean animationsDisabled) {
        this.animationsDisabled = animationsDisabled;
    }

    // ===================================== Load Game =====================================
    public void loadGame(String xmlFilePath) throws LoadException {
        Game loadedGame = gamesManager.loadGameFile(xmlFilePath);
        activeGame.setValue(loadedGame);
        gameState.setValue(loadedGame.getGameState());
        if (gameState.getValue() == eGameState.LOADED) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Game file successfully loaded from location: \n" + xmlFilePath);
            alert.showAndWait();
        }
    }

    // ===================================== Start Game =====================================
    public void startGame() {
        if (!welcomeSceneController.gameTaskFinishedProperty().getValue()) {
            hidePauseMenu();
            welcomeSceneController.onStartGameSelected();
        } else {
            onLoadGameComplete();
        }
    }

    public void onLoadGameComplete() {
        try {
            updateGameStateProperty();
            if (gameState.getValue() != eGameState.LOADED) {
                // set the game to be as if it was just started
                resetGame(activeGame.getValue());
            }
            primaryStage.setScene(mainWindowScene);
            currentTurnInfoController.setReplayMode(false);
            Player player1 = initializePlayer(1);
            Player player2 = initializePlayer(2);
            gamesManager.startGame(activeGame.getValue(), player1, player2);
            setInitialValuesForPlayers();
            updateGameStateProperty();
            updateActivePlayer();
            hidePauseMenu();
        } catch (InvalidGameObjectPlacementException e) {
            String message = "Cannot place a given " + e.getGameObjectType() + " at position " + e.GetCoordinates() + ".\n" +
                    "reason: " + e.getReason() + "\n";
            AlertHandlingUtils.showErrorMessage(e, "Error while initializing board.", message);
            errorWhileStartingGame();
        } catch (Exception e) {
            AlertHandlingUtils.showErrorMessage(e, "Error while starting game. ");
            errorWhileStartingGame();
        }
    }

    private void updateGameStateProperty() {
        gameState.setValue(activeGame.getValue().getGameState());
    }

    // display the initialize player window and return the player according to the data
    private Player initializePlayer(int playerNumber) {
        PlayerAdapter newPlayer = new PlayerAdapter("Player" + playerNumber, "Player " + playerNumber);
        playerInitializerController.setPlayer(newPlayer, playerNumber);
        playerInitializerStage.showAndWait();
        return newPlayer;
    }

    public void playerWindowOkClicked() {
        playerInitializerStage.close();
    }

    private void updateActivePlayer() {
        Player activePlayerInActiveGame = activeGame.getValue().getActivePlayer();
        if (activePlayer.getValue() != activePlayerInActiveGame) {
            activePlayer.setValue(activePlayerInActiveGame);
            turnPlayerTimer = Instant.now();
        }
    }

    private void setInitialValuesForPlayers() {
        Game activeGame = this.activeGame.getValue();
        Player[] players = activeGame.getPlayers();

        for (Player player : players) {
            player.getMyBoard().setMinesAvailable(activeGame.getGameSettings().getMinesPerPlayer());
            player.setActiveShipsOnBoard(activeGame.getGameSettings().getShipAmountsOnBoard());
        }
    }

    // ===================================== Make Move =====================================
    public eAttackResult makeMove(ImageViewProxy cellAsImageView) {
        eAttackResult attackResult = null;
        try {
            BoardCoordinates coordinatesOfTheCell = BoardCoordinates.Parse(cellAsImageView.getId());
            updateActivePlayer();
            Player activePlayerWhoMakeMove = activePlayer.getValue();
            ReplayGame replayMove = new ReplayGame(activePlayerWhoMakeMove, coordinatesOfTheCell);
            attackResult = gamesManager.makeMove(activeGame.getValue(), coordinatesOfTheCell);
            // BEFORE move save to previous replay
            if (attackResult != eAttackResult.CELL_ALREADY_ATTACKED) {
                previousMoves.addLast(replayMove);
            }
            updateActivePlayerAttackResult(cellAsImageView, attackResult);
            updateActivePlayer();
            // AFTER move save to next replay
            gameState.setValue(activeGame.getValue().getGameState());
            if (gameState.getValue() != eGameState.PLAYER_QUIT) {
                if (attackResult != eAttackResult.CELL_ALREADY_ATTACKED) {
                    replayMove = new ReplayGame(activePlayerWhoMakeMove, coordinatesOfTheCell);
                    replayMove.setAttackResult(attackResult);
                    nextMoves.addLast(replayMove);
                }
            }
            if (gameState.getValue() == eGameState.PLAYER_WON) {
                onGameEnded();
            }
        } catch (CellNotOnBoardException e) {
            AlertHandlingUtils.showErrorMessage(e, "Error while making move");
        }

        return attackResult;
    }

    private void updateActivePlayerAttackResult(ImageViewProxy cellAsImageView, eAttackResult attackResult) {
        Game activeGame = this.activeGame.getValue();
        // cell update
        if (animationsDisabled) {
            cellAsImageView.updateImage();
        } else {
            cellAsImageView.updateImageWithTransition(attackResult);
        }
        // statistic update
        currentTurnInfoController.attackResultUpdated(attackResult);
        if (attackResult.moveEnded() || activeGame.getGameState() == eGameState.PLAYER_WON) {
            Duration turnTime = Duration.between(turnPlayerTimer, Instant.now());
            activePlayer.getValue().addTurnDurationToTotal(turnTime);
        }
        totalMovesCounter.setValue(activeGame.getMovesCounter());

        Alert moveResult = new Alert(Alert.AlertType.INFORMATION, attackResult.toString()
                + "\nPress enter OR space to continue");
        moveResult.showAndWait();
    }

    public void plantMine(ImageViewProxy boardCellAsImage) throws InvalidGameObjectPlacementException, NoMinesAvailableException, CellNotOnBoardException {
        BoardCoordinates minePosition = boardCellAsImage.getBoardCell().getPosition();
        activeGame.getValue().plantMineOnActivePlayersBoard(minePosition);
        mainWindowController.plantMine(boardCellAsImage);
        updateActivePlayer();
    }

    // ===================================== Replay Mode =====================================
    public void startReplay() {
        // it is more than the max index
        currReplayIndex = previousMoves.size() - 1;
        mainWindowController.setReplayMode(true);
        currentTurnInfoController.setReplayMode(true);
        hidePauseMenu();
        // because move stop within move attack
        if (activeGame.getValue().getGameState() == eGameState.PLAYER_QUIT) {
            swapPlayer();
        }
        if (previousMoves.isEmpty()) {
            currentTurnInfoController.setEnablePreviousReplay(false);
        }
        currentTurnInfoController.setEnableNextReplay(false);
        if (currReplayIndex >= 0) {
            currentTurnInfoController.updateReplayMove(nextMoves.peekLast(), lastReplayCommand);
        }
        lastReplayCommand = ReplayGame.eReplayStatus.END_LIST;
    }

    public void replayLastMove() {
        ReplayGame currentMoveBeforeAttack = previousMoves.get(currReplayIndex);
        ReplayGame currentMoveAfterAttack = nextMoves.get(currReplayIndex);
        ReplayGame previousMoveAfterAttack;

        setReplayActivePlayer(currentMoveBeforeAttack);
        if (currentMoveAfterAttack.getAttackResult() != eAttackResult.PLANT_MINE) {
            updateLastMoveChanges(currentMoveBeforeAttack);
        } else {
            removeReplayMine(currentMoveBeforeAttack);
        }
        if (currReplayIndex > 0) {
            currReplayIndex--;
            previousMoveAfterAttack = nextMoves.get(currReplayIndex);
            setReplayActivePlayer(previousMoveAfterAttack);
            updateLastMoveChanges(previousMoveAfterAttack);
            lastReplayCommand = ReplayGame.eReplayStatus.PREV;
        } else {
            currentTurnInfoController.setEnablePreviousReplay(false);
            lastReplayCommand = ReplayGame.eReplayStatus.START_LIST;
        }
        currentTurnInfoController.setEnableNextReplay(true);
        mainWindowController.redrawBoards(activePlayer.getValue());
    }

    private void setReplayActivePlayer(ReplayGame replayMove) {
        if (activePlayer.getValue() != replayMove.getActivePlayer()) {
            swapPlayer();
        }
    }

    private void swapPlayer() {
        activeGame.getValue().swapPlayers();
        updateActivePlayer();
    }

    private void updateLastMoveChanges(ReplayGame replayMoveBack) {
        boardsReplayChanges(replayMoveBack, ReplayGame.eReplayStatus.PREV);
        statisticReplayChanges(replayMoveBack);
    }

    private void boardsReplayChanges(ReplayGame replayMove, ReplayGame.eReplayStatus replayStatus) {
        try {
            BoardCoordinates positionAttacked = replayMove.getPositionAttacked();
            BoardCell myBoardCell = activePlayer.getValue().getMyBoard().getBoardCellAtCoordinates(positionAttacked);
            myBoardCell.setWasAttacked(replayMove.wasMyBoardCellAttacked());
            BoardCell opponentBoardCell = activePlayer.getValue().getOpponentBoard().getBoardCellAtCoordinates(positionAttacked);
            opponentBoardCell.setWasAttacked(replayMove.wasOpponentsBoardCellAttacked());
            if (opponentBoardCell.getCellValue() instanceof AbstractShip) {
                replayShipCell(replayMove, replayStatus, opponentBoardCell);
            } else if (opponentBoardCell.getCellValue() instanceof Mine && myBoardCell.getCellValue() instanceof AbstractShip) {
                if (replayStatus == ReplayGame.eReplayStatus.PREV) {
                    ((AbstractShip) myBoardCell.getCellValue()).increaseHitsRemainingUntilSunk();
                } else if (replayStatus == ReplayGame.eReplayStatus.NEXT) {
                    ((AbstractShip) myBoardCell.getCellValue()).decreaseHitsRemainingUntilSunk();
                }
            }
        } catch (CellNotOnBoardException e) {
            AlertHandlingUtils.showErrorMessage(e, "Error while trying to get previous move");
        }
    }

    private void replayShipCell(ReplayGame replayMove, ReplayGame.eReplayStatus replayStatus, BoardCell opponentBoardCell) {
        AbstractShip ship = (AbstractShip) opponentBoardCell.getCellValue();
        Player opponentPlayer = activeGame.getValue().getOtherPlayer();
        boolean prevSelected = replayStatus == ReplayGame.eReplayStatus.PREV || replayStatus == ReplayGame.eReplayStatus.START_LIST;
        boolean nextSelected = replayStatus == ReplayGame.eReplayStatus.NEXT || replayStatus == ReplayGame.eReplayStatus.END_LIST;
        // when update BEFORE state
        if (replayMove.getAttackResult() == null) {
            if (prevSelected) {
                if (ship.getHitsRemainingUntilSunk() == 0) {
                    opponentPlayer.OnShipComeBackToLife(ship);
                }

                ship.increaseHitsRemainingUntilSunk();
            }
        } else {
            if (nextSelected) {
                ship.decreaseHitsRemainingUntilSunk();
                if (ship.getHitsRemainingUntilSunk() == 0) {
                    opponentPlayer.OnShipSunk(ship);
                }
            }
        }

        currentTurnInfoController.updateShipsRemainingTable();
    }

    private void statisticReplayChanges(ReplayGame replayMove) {
        activePlayer.getValue().getMyBoard().setMinesAvailable(replayMove.getMinesAmount());
        activePlayer.getValue().setScore(replayMove.getCurrentScore());
        activePlayer.getValue().setTotalTurnsDuration(replayMove.getTotalPlayerTurnsDuration());
        activePlayer.getValue().setNumTurnsPlayed(replayMove.getNumPlayerTurnsPlayed());
        activePlayer.getValue().setTimesHit(replayMove.getHitNum());
        activePlayer.getValue().setTimesMissed(replayMove.getMissNum());
        currentTurnInfoController.updateReplayMove(replayMove, lastReplayCommand);
    }

    private void removeReplayMine(ReplayGame replayMoveBack) {
        try {
            BoardCoordinates positionOfMine = replayMoveBack.getPositionAttacked();
            BoardCell mineCell = replayMoveBack.getActivePlayer().getMyBoard().getBoardCellAtCoordinates(positionOfMine);
            mineCell.removeGameObjectFromCell();
        } catch (CellNotOnBoardException e) {
            AlertHandlingUtils.showErrorMessage(e, "Error while remove replay mine");
        }
        statisticReplayChanges(replayMoveBack);
    }

    public void replayNextMove() {
        ReplayGame nextMoveAfterAttack;
        if (lastReplayCommand != ReplayGame.eReplayStatus.START_LIST) {
            currReplayIndex++;
            nextMoveAfterAttack = nextMoves.get(currReplayIndex);
        } else {
            nextMoveAfterAttack = nextMoves.get(currReplayIndex);
        }

        if (currReplayIndex < nextMoves.size() - 1) {
            lastReplayCommand = ReplayGame.eReplayStatus.NEXT;
        } else {
            currentTurnInfoController.setEnableNextReplay(false);
            lastReplayCommand = ReplayGame.eReplayStatus.END_LIST;
        }

        setReplayActivePlayer(nextMoveAfterAttack);
        if (nextMoveAfterAttack.getAttackResult() != eAttackResult.PLANT_MINE) {
            updateNextMoveChanges(nextMoveAfterAttack);
        } else {
            addReplayMine(nextMoveAfterAttack);
        }
        currentTurnInfoController.setEnablePreviousReplay(true);
        mainWindowController.redrawBoards(activePlayer.getValue());
    }

    private void updateNextMoveChanges(ReplayGame replayMoveForward) {
        boardsReplayChanges(replayMoveForward, ReplayGame.eReplayStatus.NEXT);
        statisticReplayChanges(replayMoveForward);
    }

    private void addReplayMine(ReplayGame replayMoveForward) {
        try {
            BoardCoordinates positionOfPlantMine = replayMoveForward.getPositionAttacked();
            BoardCell cellToPlantMine = replayMoveForward.getActivePlayer().getMyBoard().getBoardCellAtCoordinates(positionOfPlantMine);
            cellToPlantMine.setCellValue(new Mine(positionOfPlantMine));
        } catch (Exception e) {
            AlertHandlingUtils.showErrorMessage(e, "Error while remove replay mine");
        }
        statisticReplayChanges(replayMoveForward);
    }

    // ===================================== End Game =====================================
    public void endGame() {
        gamesManager.endGame(activeGame.getValue());
        onGameEnded();
    }

    private void onGameEnded() {
        gameState.setValue(this.activeGame.getValue().getGameState());
        showGameEndedWindow();
        showPauseMenu();
    }

    private void showGameEndedWindow() {
        Game activeGame = this.activeGame.getValue();
        gameEndedLayoutController.setWinnerName(activeGame.getWinnerPlayer().getName());
        gameEndedLayoutController.setPlayers(activeGame.getPlayers());
        gameEndedStage.showAndWait();
    }

    // ===================================== Exit Game =====================================
    public void exitGame() {
        primaryStage.close();
    }

    // ===================================== Other Methods =====================================
    private void errorWhileStartingGame() {
        activeGame.setValue(null);
        primaryStage.setScene(welcomeScreenScene);
        welcomeSceneController.gameTaskFinishedProperty().setValue(false);
        String message = "game file given was invalid therefor it was not loaded. \nPlease check the file and try again.";
        AlertHandlingUtils.showErrorMessage(new Exception("Invalid game file"), "Game file validation error", message);
        showPauseMenu();
    }

    public void showPauseMenu() {
        if (!secondaryStage.isShowing()) {
            secondaryStage.showAndWait();
        }
    }

    public void hidePauseMenu() {
        secondaryStage.hide();
    }

    private void resetGame(Game activeGame) {
        try {
            activeGame.resetGame();
            gameState.setValue(activeGame.getGameState());
            resetGameEvent.forEach(Runnable::run);
        } catch (Exception e) {
            AlertHandlingUtils.showErrorMessage(e, "Error while reloading the game, Please load the file again or choose another file", e.getMessage());
            activeGame.setGameState(eGameState.INVALID);
            gameState.setValue(activeGame.getGameState());
        }
    }

    public void setStyleSheets() {
        try {
            mainWindowScene.getStylesheets().clear();
            mainWindowScene.getStylesheets().addAll(JavaFXManager.class.getResource(styleSheetURL).toExternalForm());

            pauseWindowScene.getStylesheets().clear();
            pauseWindowScene.getStylesheets().setAll(JavaFXManager.class.getResource(styleSheetURL).toExternalForm());

            playerInitializerScene.getStylesheets().clear();
            playerInitializerScene.getStylesheets().setAll(JavaFXManager.class.getResource(styleSheetURL).toExternalForm());

            gameEndedScene.getStylesheets().clear();
            gameEndedScene.getStylesheets().setAll(JavaFXManager.class.getResource(styleSheetURL).toExternalForm());
        } catch (Exception e) {
            AlertHandlingUtils.showErrorMessage(e, "Error while setting style sheets");
        }
    }

    public void setStyleSheetURL(String styleSheetURL) {
        this.styleSheetURL = styleSheetURL;
    }

    public void clearStyleSheets() {
        mainWindowScene.getStylesheets().clear();
        pauseWindowScene.getStylesheets().clear();
        playerInitializerScene.getStylesheets().clear();
        gameEndedScene.getStylesheets().clear();
    }


}
