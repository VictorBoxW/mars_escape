package view;

import controller.GameController;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;

public class GameWindow extends JFrame {
    private static final String MENU_CARD = "menu";
    private static final String GAME_CARD = "game";

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final GameController controller;

    public GameWindow() {
        super("Mars Escape");

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        controller = new GameController();

        GamePanel panel = new GamePanel(controller);
        MenuPanel menuPanel = new MenuPanel(this::playGame, this::quitGame);
        controller.setGamePanel(panel);

        contentPanel.add(menuPanel, MENU_CARD);
        contentPanel.add(panel, GAME_CARD);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitGame();
            }
        });

        setupKeyBindings(panel);

        setContentPane(contentPanel);
        setLocationRelativeTo(null);
        cardLayout.show(contentPanel, MENU_CARD);
    }

    private void setupKeyBindings(JPanel panel) {
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        int moveAmount = 15; // Increased slightly for better feel

        String[] keys = {"UP", "W", "DOWN", "S", "LEFT", "A", "RIGHT", "D"};
        int[][] dirs = {{0, -1}, {0, -1}, {0, 1}, {0, 1}, {-1, 0}, {-1, 0}, {1, 0}, {1, 0}};

        for (int i = 0; i < keys.length; i++) {
            final int dx = dirs[i][0] * moveAmount;
            final int dy = dirs[i][1] * moveAmount;
            String key = keys[i];
            
            inputMap.put(KeyStroke.getKeyStroke(key), key);
            actionMap.put(key, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    controller.movePlayer(dx, dy);
                }
            });
        }
    }

    public void showGameOverDialog(boolean victory) {
        String title = victory ? "Mission Success!" : "Mission Failed";
        String message = victory ? "You secured the energy crystal and escaped Mars!" : "The astronaut fell in the fortress.";
        
        Object[] options = {"Restart", "Quit to Menu"};
        int choice = JOptionPane.showOptionDialog(
            this,
            message + "\nWhat would you like to do?",
            title,
            JOptionPane.YES_NO_OPTION,
            victory ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice == JOptionPane.YES_OPTION) {
            playGame();
        } else {
            cardLayout.show(contentPanel, MENU_CARD);
        }
    }

    private void playGame() {
        controller.restartGame();
        cardLayout.show(contentPanel, GAME_CARD);
        requestFocusInWindow(); // Ensure keyboard input works
    }

    private void quitGame() {
        int response = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to quit the mission?",
            "Exit Mars Escape",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
}
