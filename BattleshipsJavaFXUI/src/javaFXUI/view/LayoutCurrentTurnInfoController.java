package javaFXUI.view;

import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.eAttackResult;
import gameLogic.game.eGameState;
import gameLogic.users.Player;
import javaFXUI.Constants;
import javaFXUI.JavaFXManager;
import javaFXUI.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LayoutCurrentTurnInfoController {
    @FXML
    private ImageView imageViewPlayerProfile;
    @FXML
    private Label labelCurrentPlayer;
    @FXML
    public Pane paneReplay;
    @FXML
    public Button buttonPrevious;
    @FXML
    public Button buttonNext;
    @FXML
    public Label labelHitOrMiss;
    @FXML
    public Label labelAttackResult;
    @FXML
    private ImageView imageViewMinesAvailable;
    @FXML
    private Label labelMinesAvailable;
    @FXML
    private TableView<ShipsStateDataModel> TableShipsState;
    @FXML
    private TableColumn<ShipsStateDataModel, String> columnShipType;
    @FXML
    private TableColumn<ShipsStateDataModel, Integer> columnInitialShips;
    @FXML
    private TableColumn<ShipsStateDataModel, Integer> columnRemainingShipsMe;
    @FXML
    private TableColumn<ShipsStateDataModel, Integer> columnRemainingShipsEnemy;
    @FXML
    private Label labelCurrentScoreMe;
    @FXML
    private Label labelTotalMovesCounter;
    @FXML
    private Label labelCurrentScoreOpponent;
    @FXML
    private Label labelAvgTurnDuration;
    @FXML
    private Label labelHitsCounter;
    @FXML
    private Label labelMissCounter;
    private JavaFXManager javaFXManager;
    private final Map<String, ShipsStateDataModel> shipsState = new HashMap<>();
    private final Image noMinesAvailableImage = new Image(Constants.NO_MINES_AVAILABLE_IMAGE_URL);
    private final Image oneMineAvailableImage = new Image(Constants.MINE_IMAGE_URL);
    private final Image multipleMinesAvailable = new Image(Constants.MULTIPLE_MINES_IMAGE_URL);

    // ===================================== Init =====================================
    @FXML
    private void initialize() {
        columnShipType.setCellValueFactory(new PropertyValueFactory<>("shipType"));
        columnInitialShips.setCellValueFactory(new PropertyValueFactory<>("initialAmount"));
        columnRemainingShipsMe.setCellValueFactory(new PropertyValueFactory<>("shipsRemainingActivePlayer"));
        columnRemainingShipsEnemy.setCellValueFactory(new PropertyValueFactory<>("shipsRemainingOtherPlayer"));

        setDragAndDropMine();
    }

    private void setDragAndDropMine() {
        imageViewMinesAvailable.setOnDragDetected((event) -> {
            int boardSize = javaFXManager.getActiveGame().getValue().getActivePlayer().getMyBoard().getBoardSize();
            int cellSize = BoardAdapter.getCellSize(boardSize);
            ImageView draggedImage = new ImageView(new Image(Constants.MINE_IMAGE_URL, cellSize, cellSize, true, true));
            WritableImage snapshot = draggedImage.snapshot(new SnapshotParameters(), null);
            Dragboard db = imageViewMinesAvailable.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();
            content.putImage(draggedImage.getImage());
            db.setContent(content);
            db.setDragView(snapshot, 25, 25);

            event.consume();
        });

        imageViewMinesAvailable.setOnDragDone((event) -> {
            if (event.getTransferMode() == TransferMode.MOVE) {
                try {
                    Dragboard dragboard = event.getDragboard();
                    ImageViewProxy selectedBoardCellAsImage = getSelectedBoardCell(dragboard);

                    // BEFORE move save to previous replay
                    Player activePlayerWhoMakeMove = javaFXManager.getActiveGame().getValue().getActivePlayer();
                    BoardCoordinates coordinatesOfTheCell = selectedBoardCellAsImage.getBoardCell().getPosition();
                    ReplayGame replayMove = new ReplayGame(activePlayerWhoMakeMove, coordinatesOfTheCell);
                    javaFXManager.plantMine(selectedBoardCellAsImage);
                    // if we got here is mean the mine plant safe
                    javaFXManager.getPreviousMoves().addLast(replayMove);
                    // AFTER move save to next replay
                    replayMove = new ReplayGame(activePlayerWhoMakeMove, coordinatesOfTheCell);
                    replayMove.setAttackResult(eAttackResult.PLANT_MINE);
                    javaFXManager.getNextMoves().addLast(replayMove);

                    updateMinesAvailableImageView();
                } catch (Exception e){
                    AlertHandlingUtils.showErrorMessage(e, "Error while plant mine");
                }
            }
            event.consume();
        });
    }

    private ImageViewProxy getSelectedBoardCell(Dragboard dragboard) {
        ImageViewProxy boardCellAsImage = null;
        for (DataFormat dataFormat : dragboard.getContentTypes()) {
            for (String className : dataFormat.getIdentifiers()) {
                if (className.equals("ImageViewProxy")) {
                    boardCellAsImage = (ImageViewProxy) dragboard.getContent(dataFormat);
                    break;
                }
            }
        }
        return boardCellAsImage;
    }

    private void updateMinesAvailableImageView() {
        Integer minesAvailable = javaFXManager.getActiveGame().getValue().getActivePlayer().getMyBoard().getMinesAvailable();
        if (minesAvailable <= 0) {
            imageViewMinesAvailable.setImage(noMinesAvailableImage);
            imageViewMinesAvailable.setDisable(true);
        } else if (minesAvailable == 1) {
            imageViewMinesAvailable.setImage(oneMineAvailableImage);
        } else {
            imageViewMinesAvailable.setImage(multipleMinesAvailable);
        }
        labelMinesAvailable.setText(minesAvailable.toString());
    }

    public void setJavaFXManager(JavaFXManager javaFXManager) {
        this.javaFXManager = javaFXManager;
        addListeners();
    }

    private void addListeners() {
        javaFXManager.gameStateProperty().addListener((observable, oldValue, newValue) -> gameStateChanged(newValue));
        javaFXManager.activePlayerProperty().addListener((observable, oldValue, newValue) -> playerChanged(newValue));
        javaFXManager.totalMovesCounterProperty().addListener((observable, oldValue, newValue) -> updateStatistics());
        javaFXManager.attackResultProperty().addListener((observable, oldValue, newValue) -> attackResultUpdated(newValue));
    }

    public void setReplayMode(boolean enable) {
        paneReplay.setVisible(enable);
        buttonPrevious.setDisable(false);
        buttonNext.setDisable(false);
        labelHitOrMiss.setText("");
        labelAttackResult.setText("");
    }

    // ===================================== Game State Property =====================================
    private void gameStateChanged(eGameState newValue) {
        switch (newValue) {
            case INVALID:
                break;
            case LOADED:
                break;
            case INITIALIZED:
                break;
            case STARTED:
                gameStarted();
                break;
            case PLAYER_WON:
                break;
            case PLAYER_QUIT:
                break;
        }
    }

    private void gameStarted() {
        setShipTypeValues();
    }

    private void setShipTypeValues() {
        ObservableList<ShipsStateDataModel> shipsRemaining = FXCollections.observableArrayList();
        Map<String, Integer> shipTypes = javaFXManager.getActiveGame().getValue().getActivePlayer().getActiveShipsOnBoard();

        for (Map.Entry<String, Integer> entry : shipTypes.entrySet()) {
            shipsState.put(entry.getKey(), new ShipsStateDataModel(entry.getKey(), entry.getValue()));
            shipsRemaining.add(new ShipsStateDataModel(entry.getKey(), entry.getValue()));
        }

        TableShipsState.setItems(shipsRemaining);
        TableShipsState.refresh();
    }

    // ===================================== Active Player Property =====================================
    private void playerChanged(Player currentPlayer) {
        labelCurrentPlayer.setText(currentPlayer.getName());
        imageViewPlayerProfile.setImage(((PlayerAdapter) currentPlayer).getPlayerImage());
        updateStatistics();
        updateShipsRemainingTable();
    }

    public void updateShipsRemainingTable() {
        ObservableList<ShipsStateDataModel> shipsRemaining = FXCollections.observableArrayList();
        int i = 0;
        Set<Map.Entry<String, Integer>> entries = javaFXManager.getActiveGame().getValue().getOtherPlayer().getActiveShipsOnBoard().entrySet();

        for (Map.Entry<String, Integer> entry : entries) {
            TableShipsState.getItems().get(i).setShipsRemainingOtherPlayer(entry.getValue());
            i++;
        }

        i = 0;
        entries = javaFXManager.getActiveGame().getValue().getActivePlayer().getActiveShipsOnBoard().entrySet();
        for (Map.Entry<String, Integer> entry : entries) {
            TableShipsState.getItems().get(i).setShipsRemainingActivePlayer(entry.getValue());
            shipsRemaining.add(TableShipsState.getItems().get(i).clone());
            i++;
        }

        TableShipsState.setItems(shipsRemaining);
    }

    public void updateStatistics() {
        Player activePlayer = javaFXManager.activePlayerProperty().getValue();
        labelTotalMovesCounter.setText(String.valueOf(javaFXManager.getActiveGame().getValue().getMovesCounter()));
        labelCurrentScoreMe.setText(((Integer) activePlayer.getScore()).toString());
        labelCurrentScoreOpponent.setText(String.valueOf(javaFXManager.getActiveGame().getValue().getOtherPlayer().getScore()));
        Duration avgDuration = activePlayer.getAvgTurnDuration();
        labelAvgTurnDuration.setText(String.format("%d:%02d", avgDuration.toMinutes(), avgDuration.getSeconds() % 60));
        labelHitsCounter.setText(((Integer) activePlayer.getTimesHit()).toString());
        labelMissCounter.setText(((Integer) activePlayer.getTimesMissed()).toString());
        updateMinesAvailableImageView();
    }

    // ===================================== Attack Result Property =====================================
    public void attackResultUpdated(eAttackResult newValue) {
        switch (newValue) {
            case HIT_SHIP:
                break;
            case HIT_AND_SUNK_SHIP:
                onShipSunk();
                break;
            case CELL_ALREADY_ATTACKED:
                break;
            case HIT_MINE:
                break;
            case HIT_WATER:
                break;
        }
    }

    private void onShipSunk() {
        updateShipsRemainingTable();
        TableShipsState.refresh();
    }

    // ===================================== Replay Methods =====================================
    @FXML
    public void buttonPreviousOnMouseClicked(MouseEvent mouseEvent) {
        javaFXManager.replayLastMove();
    }

    @FXML
    public void buttonNextOnMouseClicked(MouseEvent mouseEvent) {
        javaFXManager.replayNextMove();
    }

    public void setEnablePreviousReplay(boolean enable) {
        buttonPrevious.setDisable(!enable);
    }

    public void setEnableNextReplay(boolean enable) {
        buttonNext.setDisable(!enable);
    }

    public void updateReplayMove(ReplayGame replayMove, ReplayGame.eReplayStatus replayStatus) {
        updateStatistics();
        setReplayStatus(replayMove);
    }

    public void setReplayStatus(ReplayGame replayGame) {
        String attackResult = replayGame.getAttackResult() != null ?
                "Attacked " + replayGame.getPositionAttacked().toString() : "";
        String hitOrMiss = replayGame.getAttackResult() != null ? replayGame.getAttackResult().toString() : "";
        labelAttackResult.setText(attackResult);
        labelHitOrMiss.setText(hitOrMiss);
    }
}
