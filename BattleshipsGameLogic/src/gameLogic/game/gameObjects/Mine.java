package gameLogic.game.gameObjects;

import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.eAttackResult;

public class Mine extends GameObject {
    public Mine(BoardCoordinates position) {
        super(position, !VISIBLE);
    }

    @Override
    public eAttackResult getAttackResult() {
        return eAttackResult.HIT_MINE;
    }
}
