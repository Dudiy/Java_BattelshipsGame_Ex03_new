package gameLogic.game;

public class ChatMessage {
    private String playerName;
    private String messageString;
    private String messageTime;

    public ChatMessage(String playerName, String message, String messageTime) {
        this.playerName = playerName;
        this.messageString = message;
        this.messageTime = messageTime;
    }

    public void setUserName(String playerName) {
        this.playerName = playerName;
    }

    public void setMessage(String message) {
        this.messageString = message;
    }

    public void setTime(String messageTime) {
        this.messageTime = messageTime;
    }
}
