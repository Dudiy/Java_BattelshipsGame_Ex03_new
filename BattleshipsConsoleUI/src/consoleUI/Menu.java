package consoleUI;

import gameLogic.game.Game;
import gameLogic.game.eGameState;

import java.util.Scanner;

class Menu {
    private final String MENU_TOP = "\n╔═══════════════════════ Menu ═══════════════════════╗";
    private final String MENU_BOTTOM = "╚════════════════════════════════════════════════════╝";
    private final String MENU_VERTICAL = "║";
    private final int MENU_WIDTH = MENU_BOTTOM.length();
    private eGameState gameState;

    public eMenuOption display(Game game) {
        this.gameState = game == null ? eGameState.INVALID : game.getGameState();

        printMenu();
        String activePlayerName =
                (game == null || game.getActivePlayer() == null) ?
                        "" :
                        game.getActivePlayer().getName();

        return getUserSelection(activePlayerName);
    }

    private void printMenu() {
        System.out.println(MENU_TOP);

        for (eMenuOption menuOption : eMenuOption.values()) {
            if (menuOption.isVisibleAtGameState(gameState)) {
                int numSpaces = MENU_WIDTH - menuOption.toString().length() - 2;
                String spacesAfter = String.format("%" + numSpaces + "s", " ");
                System.out.println(MENU_VERTICAL + menuOption + spacesAfter + MENU_VERTICAL);
            }
        }

        System.out.println(MENU_BOTTOM);
    }

    private eMenuOption getUserSelection(String activePlayerName) {
        Scanner scanner = new Scanner(System.in);
        eMenuOption userSelection = null;
        boolean isValidSelection = false;
        int userIntSelection;

        String title = "Please select one of the options above" +
                (activePlayerName.isEmpty() || gameState != eGameState.STARTED ?
                        ": " :
                        " (active player is " + activePlayerName + "): ");
        System.out.print(title);
        do {
            try {
                userIntSelection = scanner.nextInt();
                userSelection = eMenuOption.valueOf(userIntSelection);
                if (userSelection != null && userSelection.isVisibleAtGameState(gameState)) {
                    isValidSelection = true;
                } else {
                    System.out.print("Invalid selection please select one of the options above: ");
                    scanner.nextLine();
                }
            } catch (Exception ex) {
                System.out.print("Invalid input, please input an integer representing one of the options above: ");
                scanner.nextLine();
            }
        } while (!isValidSelection);

        System.out.println();
        return userSelection;
    }
}
