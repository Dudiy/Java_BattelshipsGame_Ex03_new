package javaFXUI.model;

import gameLogic.exceptions.CellNotOnBoardException;
import gameLogic.game.board.Board;
import gameLogic.game.board.BoardCell;
import gameLogic.game.board.BoardCoordinates;
import javafx.scene.Node;
import javafx.scene.layout.TilePane;

public class BoardAdapter {
    private Board board;
    private TilePane boardAsTilePane;
    private boolean isVisible;

    private static final int BOARD_WIDTH = 400;

    // ===================================== Init =====================================
    public BoardAdapter(Board board, boolean isVisible) {
        this.board = board;
        this.isVisible = isVisible;
        generateTilePane();
    }

    private void generateTilePane() {
        boardAsTilePane = new TilePane();
        int boardSize = board.getBoardSize();
        int cellSize = getCellSize(boardSize);
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                BoardCoordinates coordinates = BoardCoordinates.Parse(i, j);
                try {
                    BoardCell cell = board.getBoardCellAtCoordinates(coordinates);
                    addCellToTilePane(cell, cellSize);
                } catch (Exception e) {
                    AlertHandlingUtils.showErrorMessage(e,"Error while generating tile pane");
                }
            }
        }
    }

    public static int getCellSize(int boardSize) {
        return BOARD_WIDTH / boardSize;
    }

    private void addCellToTilePane(BoardCell boardCell, int cellSize) throws Exception {
        if (boardCell != null) {
            ImageViewProxy boardCellAsImageView = new ImageViewProxy(boardCell, cellSize, isVisible);
            boardAsTilePane.getChildren().add(boardCellAsImageView);
        } else {
            throw new Exception("input boardCell was not initialized");
        }
    }

    // ===================================== Other Methods =====================================
    public TilePane getBoardAsTilePane() {
        return boardAsTilePane;
    }

    public static void updateImages(TilePane tilePane) {
        for (Node image : tilePane.getChildren()) {
            if (image instanceof ImageViewProxy) {
                ((ImageViewProxy) image).updateImage();
            }
        }
    }
}
