package view;

import controller.GameController;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
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

public class GameWindow extends JFrame {
    private static final String MENU_CARD = "menu";
    private static final String GAME_CARD = "game";
    /** Pixels the player moves per timer tick (~20ms); balances responsiveness with control. */
    private static final int PLAYER_MOVE_SPEED = 8;

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final GameController controller;

    private final java.util.Set<Integer> pressedKeys = new java.util.HashSet<>();
    private javax.swing.Timer movementTimer;

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
        setupMovementTimer();

        setContentPane(contentPanel);
        setLocationRelativeTo(null);
        cardLayout.show(contentPanel, MENU_CARD);
    }

    private void setupKeyBindings(JPanel panel) {
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        int[] keyCodes = {
            java.awt.event.KeyEvent.VK_UP, java.awt.event.KeyEvent.VK_W,
            java.awt.event.KeyEvent.VK_DOWN, java.awt.event.KeyEvent.VK_S,
            java.awt.event.KeyEvent.VK_LEFT, java.awt.event.KeyEvent.VK_A,
            java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.KeyEvent.VK_D,
            java.awt.event.KeyEvent.VK_O
        };

        for (int keyCode : keyCodes) {
            String pressedKey = "pressed " + keyCode;
            String releasedKey = "released " + keyCode;

            inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), pressedKey);
            actionMap.put(pressedKey, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pressedKeys.add(keyCode);
                    if (keyCode == java.awt.event.KeyEvent.VK_O) {
                        controller.interact();
                    }
                }
            });

            inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, true), releasedKey);
            actionMap.put(releasedKey, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pressedKeys.remove(keyCode);
                }
            });
        }
    }

    private void setupMovementTimer() {
        movementTimer = new javax.swing.Timer(20, e -> {
            updateAnimations();
            handleMovementInput();
        });
    }

    /**
     * Advances door animations and repaints only while the player is moving
     * or doors are mid-animation. This avoids full repaints every tick when
     * the player is idle, which was the main GPU-usage cause on high-res displays.
     */
    private void updateAnimations() {
        boolean needsUpdate = !pressedKeys.isEmpty() || controller.hasPendingAnimations();
        if (needsUpdate) {
            controller.update();
        }
    }

    private void handleMovementInput() {
        if (pressedKeys.isEmpty()) return;

        int dx = 0;
        int dy = 0;

        if (isKeyPressed(java.awt.event.KeyEvent.VK_UP, java.awt.event.KeyEvent.VK_W)) dy -= PLAYER_MOVE_SPEED;
        if (isKeyPressed(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.KeyEvent.VK_S)) dy += PLAYER_MOVE_SPEED;
        if (isKeyPressed(java.awt.event.KeyEvent.VK_LEFT, java.awt.event.KeyEvent.VK_A)) dx -= PLAYER_MOVE_SPEED;
        if (isKeyPressed(java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.KeyEvent.VK_D)) dx += PLAYER_MOVE_SPEED;

        if (dx != 0 || dy != 0) {
            controller.movePlayer(dx, dy);
        }
    }

    private boolean isKeyPressed(int... keyCodes) {
        for (int code : keyCodes) {
            if (pressedKeys.contains(code)) return true;
        }
        return false;
    }

    public void showGameOverDialog(boolean victory) {
        movementTimer.stop();
        pressedKeys.clear();
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
        movementTimer.start();
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
