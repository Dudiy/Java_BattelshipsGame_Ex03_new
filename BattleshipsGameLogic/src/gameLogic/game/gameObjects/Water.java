package gameLogic.game.gameObjects;

import gameLogic.game.board.BoardCoordinates;
import gameLogic.game.eAttackResult;

public class Water extends GameObject {

    public Water(BoardCoordinates position) {
        super(position, VISIBLE);
    }

    // ======================================= getters =======================================
    @Override
    public eAttackResult getAttackResult() {
        return eAttackResult.HIT_WATER;
    }
}
