package it.unicam.cs.mpgc.rpg130901;

import javax.swing.SwingUtilities;
import view.GameWindow;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
