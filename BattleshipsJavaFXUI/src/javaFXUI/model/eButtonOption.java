package javaFXUI.model;

import gameLogic.game.eGameState;
import java.util.EnumSet;

public enum eButtonOption {
    LOAD_GAME(EnumSet.of(eGameState.INVALID, eGameState.INITIALIZED, eGameState.LOADED, eGameState.PLAYER_WON, eGameState.PLAYER_QUIT)),
    START_GAME(EnumSet.of(eGameState.INITIALIZED, eGameState.LOADED, eGameState.PLAYER_WON, eGameState.PLAYER_QUIT)),
    END_GAME(EnumSet.of(eGameState.STARTED)),
    CONTINUE_GAME(EnumSet.of(eGameState.STARTED)),
    SHOW_REPLAY_AND_STATISTIC(EnumSet.of(eGameState.PLAYER_WON, eGameState.PLAYER_QUIT)),
    EXIT(EnumSet.allOf(eGameState.class));

    private EnumSet<eGameState> displayedConditions;

    eButtonOption(EnumSet<eGameState> displayConditions) {
        this.displayedConditions = displayConditions;
    }

    public Boolean isVisibleAtGameState(eGameState gameState) {
        return displayedConditions.contains(gameState);
    }
}
