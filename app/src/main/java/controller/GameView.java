package controller;

public interface GameView {
    void setLog(String message);
    void appendLog(String message);
    void refresh();
    void showGameOverDialog(boolean victory);
    String getLogText();
}
