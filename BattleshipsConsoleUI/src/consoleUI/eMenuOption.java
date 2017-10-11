package consoleUI;

import gameLogic.game.eGameState;
import java.util.EnumSet;

public enum eMenuOption {
    LOAD_GAME(1, "Load game", EnumSet.of(eGameState.INVALID, eGameState.INITIALIZED, eGameState.LOADED)),
    START_GAME(2, "Start game", EnumSet.of(eGameState.INITIALIZED, eGameState.LOADED)),
    SHOW_GAME_STATE(3, "Show game state", EnumSet.of(eGameState.STARTED)),
    MAKE_MOVE(4, "Make a move", EnumSet.of(eGameState.STARTED)),
    SHOW_STATISTICS(5, "Show statistics", EnumSet.of(eGameState.STARTED)),
    END_GAME(6, "End game", EnumSet.of(eGameState.STARTED)),
    PLANT_MINE(7,"Plant mine",EnumSet.of(eGameState.STARTED)),
    SAVE_GAME(8, "Save game", EnumSet.of(eGameState.STARTED)),
    LOAD_SAVED_GAME(9, "Load saved game", EnumSet.of(eGameState.INVALID, eGameState.INITIALIZED, eGameState.LOADED)),
    PLAY_AGAINST_COMPUTER(10,"Play against computer",EnumSet.of(eGameState.INITIALIZED, eGameState.LOADED)),
    EXIT(0, "Exit", EnumSet.allOf(eGameState.class));

    private int ID;
    private String description;
    private EnumSet<eGameState> displayedConditions;

    eMenuOption(int optionID, String description, EnumSet<eGameState> displayConditions) {
        this.ID = optionID;
        this.description = description;
        this.displayedConditions = displayConditions;
    }

    public Boolean isVisibleAtGameState(eGameState gameState){
        return displayedConditions.contains(gameState);
    }

    public static eMenuOption valueOf(int optionID) {
        eMenuOption value = null;

        for (eMenuOption menuOption : eMenuOption.values()) {
            if (optionID == menuOption.ID) {
                value = menuOption;
                break;
            }
        }

        return value;
    }

    @Override
    public String toString() {
        return ID + ") " + description;
    }
}
