package javaFXUI.model;

import gameLogic.game.board.BoardCell;
import gameLogic.game.eAttackResult;
import gameLogic.game.gameObjects.GameObject;
import gameLogic.game.gameObjects.Mine;
import gameLogic.game.gameObjects.Water;
import gameLogic.game.gameObjects.ship.AbstractShip;
import javaFXUI.Constants;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.util.Duration;
import javafx.scene.input.TransferMode;

public class ImageViewProxy extends ImageView {
    private BoardCell boardCell;
    private boolean isVisible;
    private static final Image WATER_IMAGE = new Image(Constants.WATER_IMAGE_URL);
    private static final Image SHIP_IMAGE = new Image(Constants.SHIP_IMAGE_URL);
    private static final Image SINKING_SHIP_IMAGE = new Image(Constants.SINKING_SHIP_IMAGE_URL);
    private static final Image MINE_IMAGE = new Image(Constants.MINE_ON_WATER_IMAGE_URL);
    private static final Image HIT_IMAGE = new Image(Constants.HIT_IMAGE_URL);
    private static final Image MISS_IMAGE = new Image(Constants.MISSING_IMAGE_URL);
    private static final Image PROBLEM_IMAGE = new Image(Constants.PROBLEM_IMAGE_URL);
    private static DataFormat boardCellAsImageDataFormat = new DataFormat("ImageViewProxy");

    public ImageViewProxy(BoardCell boardCell, int cellSize, boolean isVisible) {
        this.boardCell = boardCell;
        this.isVisible = isVisible;
        setFitHeight(cellSize);
        setFitWidth(cellSize);
        setImage(getImageForCell());
        setId(boardCell.getPosition().toString());
        if (!isVisible) {
            setOnMouseEntered(event -> mouseEnteredCell());
            setOnMouseExited(event -> setEffect(null));
        } else {
            setOnDragOver(event -> {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            });
            setOnDragEntered(event -> {
                mouseEnteredCell();
                event.consume();
            });
            setOnDragExited(event -> {
                setEffect(null);
                event.consume();
            });
            setOnDragDropped(event -> {
                ClipboardContent content = new ClipboardContent();
                content.put(boardCellAsImageDataFormat, this);
                event.getDragboard().setContent(content);
                event.setDropCompleted(true);
                event.consume();
            });
        }
    }

    public BoardCell getBoardCell() {
        return boardCell;
    }

    private Image getImageForCell() {
        Image imageToReturn;
        GameObject cellValue = boardCell.getCellValue();

        if (boardCell.wasAttacked()) {
            if (cellValue instanceof Water) {
                imageToReturn = MISS_IMAGE;
            } else if (cellValue instanceof Mine || cellValue instanceof AbstractShip) {
                if (cellValue instanceof AbstractShip && ((AbstractShip) cellValue).isSunk()) {
                    imageToReturn = SINKING_SHIP_IMAGE;
                } else {
                    imageToReturn = HIT_IMAGE;
                }
            } else {
                imageToReturn = PROBLEM_IMAGE;
            }
        } else {
            if (cellValue instanceof AbstractShip) {
                imageToReturn = isVisible ? SHIP_IMAGE : WATER_IMAGE;
            } else if (cellValue instanceof Water) {
                imageToReturn = WATER_IMAGE;
            } else if (cellValue instanceof Mine) {
                imageToReturn = isVisible ? MINE_IMAGE : WATER_IMAGE;
            } else {
                imageToReturn = PROBLEM_IMAGE;
            }
        }

        return imageToReturn;
    }

    private void mouseEnteredCell() {
        ColorAdjust highlight = new ColorAdjust();
        highlight.setBrightness(0.5);
        setEffect(highlight);
    }

    public void updateImage() {
        setImage(getImageForCell());
    }

    public void updateImageWithTransition(eAttackResult attackResult) {
        Transition transition;

        if (attackResult == eAttackResult.HIT_MINE) {
            transition = new FadeTransition(Duration.millis(1000),this );
            ((FadeTransition)transition).setFromValue(0);
            ((FadeTransition)transition).setToValue(1);
        } else {
            transition = new RotateTransition(Duration.millis(1000), this);
            ((RotateTransition) transition).setAxis(new Point3D(0, 1, 0));
            ((RotateTransition) transition).setByAngle(360);
        }

        Thread transitionThread = new Thread(transition::play);
        transition.setOnFinished(event -> {
            updateImage();
            setMouseTransparent(false);
        });
        setMouseTransparent(true);
        transitionThread.start();
    }
}
