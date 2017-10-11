package javaFXUI.view;

import gameLogic.game.Game;
import gameLogic.game.eGameState;
import gameLogic.users.Player;
import javaFXUI.Constants;
import javaFXUI.JavaFXManager;
import javaFXUI.model.AlertHandlingUtils;
import javaFXUI.model.BoardAdapter;
import javaFXUI.model.ImageViewProxy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class MainWindowController {
    private Map<Player, TilePane> myBoardAsTilePane = new HashMap<>();
    private Map<Player, TilePane> opponentsBoardAsTilePane = new HashMap<>();
    private JavaFXManager javaFXManager;
    private boolean boardsInitialized;

    @FXML
    private VBox vBoxMyBoard;
    @FXML
    private VBox vBoxOpponentsBoard;
    @FXML
    private TilePane tilePaneMyBoard;
    @FXML
    private TilePane tilePaneOpponentsBoard;
    @FXML
    private MenuItem buttonPauseGame;
    @FXML
    private MenuItem buttonEndCurrentGame;
    @FXML
    public TilePane tilePaneMines;
    @FXML
    private MenuItem buttonExit;
    @FXML
    private CheckMenuItem menuItemTransitionsEnabled;
    @FXML
    private RadioMenuItem menuItemStyleA;
    @FXML
    private RadioMenuItem menuItemStyleB;
    @FXML
    private RadioMenuItem menuItemStyleC;
    @FXML
    private RadioMenuItem menuItemNoStyle;

    // ===================================== Init =====================================
    public void setJavaFXManager(JavaFXManager javaFXManager) {
        this.javaFXManager = javaFXManager;
        addListeners();
    }

    private void addListeners() {
        javaFXManager.gameStateProperty().addListener((observable, oldValue, newValue) -> gameStateChanged(newValue));
        javaFXManager.activePlayerProperty().addListener((observable, oldValue, newValue) -> activePlayerChanged(newValue));
        javaFXManager.totalMovesCounterProperty().addListener((observable, oldValue, newValue) -> movePlayed());
    }

    private void initBoards() {
        Game activeGame = javaFXManager.getActiveGame().getValue();
        for (Player currentPlayer : activeGame.getPlayers()) {
            try {
                if (!myBoardAsTilePane.containsKey(currentPlayer)) {
                    BoardAdapter boardAdapter = new BoardAdapter(currentPlayer.getMyBoard(), true);
                    myBoardAsTilePane.put(currentPlayer, boardAdapter.getBoardAsTilePane());
                }
                if (!opponentsBoardAsTilePane.containsKey(currentPlayer)) {
                    BoardAdapter boardAdapter = new BoardAdapter(currentPlayer.getOpponentBoard(), false);
                    // set onClick event
                    for (Node imageView : boardAdapter.getBoardAsTilePane().getChildren()) {
                        if (imageView instanceof ImageViewProxy) {
                            imageView.setOnMouseClicked(event -> makeMove((ImageViewProxy) imageView));
                        }
                    }
                    opponentsBoardAsTilePane.put(currentPlayer, boardAdapter.getBoardAsTilePane());
                }
            } catch (Exception e) {
                AlertHandlingUtils.showErrorMessage(e, "Error while drawing boards");
            }
        }

        boardsInitialized = true;
        redrawBoards(activeGame.getActivePlayer());
    }

    // ===================================== Setter =====================================
    public void setReplayMode(boolean enable) {
        vBoxMyBoard.setDisable(enable);
        vBoxOpponentsBoard.setDisable(enable);
        buttonEndCurrentGame.setDisable(enable);
    }

    // ===================================== Getter =====================================
    public TilePane getTilePaneMyBoard() {
        return tilePaneMyBoard;
    }

    public TilePane getTilePaneOpponentsBoard() {
        return tilePaneOpponentsBoard;
    }

    // ===================================== Other Methods =====================================
    private void gameStateChanged(eGameState newValue) {
        switch (newValue) {
            case INVALID:
                break;
            case LOADED:
                break;
            case INITIALIZED:
                break;
            case STARTED:
                initBoards();
                break;
            case PLAYER_WON:
                break;
            case PLAYER_QUIT:
                break;
        }
    }

    private void activePlayerChanged(Player activePlayer) {
        try {
            if (boardsInitialized) {
                redrawBoards(activePlayer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void plantMine(ImageViewProxy boardCellAsImage) {
        boardCellAsImage.updateImage();
    }

    public void redrawBoards(Player activePlayer) {
        BoardAdapter.updateImages(myBoardAsTilePane.get(activePlayer));
        BoardAdapter.updateImages(opponentsBoardAsTilePane.get(activePlayer));
        vBoxMyBoard.getChildren().set(2, myBoardAsTilePane.get(activePlayer));
        vBoxOpponentsBoard.getChildren().set(2, opponentsBoardAsTilePane.get(activePlayer));
    }

    private void movePlayed() {
        redrawBoards(javaFXManager.activePlayerProperty().getValue());
    }

    private void makeMove(ImageViewProxy cellAsImageView) {
        javaFXManager.makeMove(cellAsImageView);
    }

    public void resetGame() {
        myBoardAsTilePane.clear();
        opponentsBoardAsTilePane.clear();
    }

    // ===================================== ToolBar Methods =====================================
    @FXML
    public void OnClickPauseMenu(ActionEvent actionEvent) {
        javaFXManager.showPauseMenu();
    }

    @FXML
    public void OnClickEndCurrentGame(ActionEvent actionEvent) {
        javaFXManager.endGame();
    }

    @FXML
    public void OnClickExit(ActionEvent actionEvent) {
        javaFXManager.exitGame();
    }


    // ===================================== Mines Methods =====================================
    @FXML
    public void OnMouseDragEntered(MouseDragEvent mouseDragEvent) {
        /* drag was detected, start drag-and-drop gesture*/
        System.out.println("onDragDetected");
        /* allow any transfer mode */
        Dragboard db = tilePaneMines.startDragAndDrop(TransferMode.ANY);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("!!");
        mouseDragEvent.consume();
    }

    public void OnAnimationsDisabled_Click(ActionEvent actionEvent) {
        javaFXManager.setAnimationsDisabled(!javaFXManager.getAnimationsDisabled());
    }

    public void menuItemStyleA_Clicked(ActionEvent actionEvent) {
        if (menuItemStyleA.isSelected()) {
            menuItemNoStyle.setSelected(false);
            menuItemStyleB.setSelected(false);
            menuItemStyleC.setSelected(false);
            javaFXManager.setStyleSheetURL(Constants.STYLESHEET_A_URL);
            javaFXManager.setStyleSheets();
        } else {
            menuItemStyleA.setSelected(true);
        }
    }

    public void menuItemStyleB_Clicked(ActionEvent actionEvent) {
        if (menuItemStyleB.isSelected()) {
            menuItemNoStyle.setSelected(false);
            menuItemStyleA.setSelected(false);
            menuItemStyleC.setSelected(false);
            javaFXManager.setStyleSheetURL(Constants.STYLESHEET_B_URL);
            javaFXManager.setStyleSheets();
        } else {
            menuItemStyleB.setSelected(true);
        }
    }

    public void menuItemStyleC_clicked(ActionEvent actionEvent) {
        if (menuItemStyleC.isSelected()) {
            menuItemNoStyle.setSelected(false);
            menuItemStyleA.setSelected(false);
            menuItemStyleB.setSelected(false);
            javaFXManager.setStyleSheetURL(Constants.STYLESHEET_C_URL);
            javaFXManager.setStyleSheets();
        } else {
            menuItemStyleC.setSelected(true);
        }
    }

    public void menuItemNoStyle_Clicked(ActionEvent actionEvent) {
        if (menuItemNoStyle.isSelected()) {
            menuItemStyleA.setSelected(false);
            menuItemStyleB.setSelected(false);
            menuItemStyleC.setSelected(false);
            javaFXManager.clearStyleSheets();
        } else {
            menuItemNoStyle.setSelected(true);
        }
    }
}
