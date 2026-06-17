package view;

import controller.GameController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import model.Floor;
import model.Item;
import model.Player;
import model.Room;

public class GamePanel extends JPanel implements controller.GameView {
    private final GameController controller;
    private final ScenePanel scenePanel;
    private final JTextArea logArea;
    private final JLabel statusLabel;
    private final JComboBox<String> inventoryBox;
    private final JButton attackButton;
    private final JButton dodgeButton;
    private final JButton useItemButton;
    private final JButton restartButton;
    private final JButton saveButton;
    private final JButton loadButton;

    public GamePanel(GameController controller) {
        super(new BorderLayout(8, 8));
        this.controller = controller;
        this.scenePanel = new ScenePanel(controller);
        this.logArea = new JTextArea();
        this.statusLabel = new JLabel("Preparing mission...");
        this.inventoryBox = new JComboBox<>();
        this.attackButton = new JButton("Attack");
        this.dodgeButton = new JButton("Dodge");
        this.useItemButton = new JButton("Use Item");
        this.restartButton = new JButton("Restart");
        this.saveButton = new JButton("Save Game");
        this.loadButton = new JButton("Load Game");

        setPreferredSize(new Dimension(1280, 720));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        configureLog();
        configureControls();

        add(scenePanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(buildSouthPanel(), BorderLayout.SOUTH);
    }

    public void setLog(String message) {
        logArea.setText(message);
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void appendLog(String message) {
        logArea.append(System.lineSeparator() + message + System.lineSeparator());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void refresh() {
        updateInventory();
        updateStatus();
        updateButtons();
        scenePanel.repaint();
    }

    private void configureLog() {
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
    }

    private void configureControls() {
        attackButton.addActionListener(event -> { controller.attack(); requestFocusInWindow(); });
        dodgeButton.addActionListener(event -> { controller.dodge(); requestFocusInWindow(); });
        useItemButton.addActionListener(event -> { controller.useItem(inventoryBox.getSelectedIndex()); requestFocusInWindow(); });
        restartButton.addActionListener(event -> { controller.restartGame(); requestFocusInWindow(); });
        saveButton.addActionListener(event -> { saveGameAction(); requestFocusInWindow(); });
        loadButton.addActionListener(event -> { loadGameAction(); requestFocusInWindow(); });

        // Make buttons non-focusable so they don't capture arrow key events
        attackButton.setFocusable(false);
        dodgeButton.setFocusable(false);
        useItemButton.setFocusable(false);
        restartButton.setFocusable(false);
        saveButton.setFocusable(false);
        loadButton.setFocusable(false);
        inventoryBox.setFocusable(false);
    }

    private JPanel buildSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout(8, 8));
        JPanel actionPanel = new JPanel();

        actionPanel.add(attackButton);
        actionPanel.add(dodgeButton);
        actionPanel.add(inventoryBox);
        actionPanel.add(useItemButton);
        actionPanel.add(restartButton);
        actionPanel.add(saveButton);
        actionPanel.add(loadButton);

        southPanel.add(statusLabel, BorderLayout.NORTH);
        southPanel.add(actionPanel, BorderLayout.CENTER);

        return southPanel;
    }

    private void updateInventory() {
        Player player = controller.getPlayer();
        if (player == null) {
            inventoryBox.removeAllItems();
            return;
        }

        List<Item> inventory = player.getInventory();
        
        // Only update if the number of items or the items themselves changed
        boolean needsUpdate = inventoryBox.getItemCount() != inventory.size();
        if (!needsUpdate) {
            for (int i = 0; i < inventory.size(); i++) {
                if (!inventory.get(i).getName().equals(inventoryBox.getItemAt(i))) {
                    needsUpdate = true;
                    break;
                }
            }
        }

        if (needsUpdate) {
            int selectedIndex = inventoryBox.getSelectedIndex();
            inventoryBox.removeAllItems();
            for (Item item : inventory) {
                inventoryBox.addItem(item.getName());
            }
            if (!inventory.isEmpty()) {
                inventoryBox.setSelectedIndex(Math.max(0, Math.min(selectedIndex, inventory.size() - 1)));
            }
        }
    }

    private void updateStatus() {
        Player player = controller.getPlayer();
        if (player == null) {
            statusLabel.setText("Preparing mission...");
            return;
        }

        Floor floor = controller.getCastle().getCurrentFloor();
        Room room = floor == null ? null : floor.getCurrentRoom();
        String location = room == null
                ? "Mission complete"
                : floor.getName() + " / " + room.getName();

        statusLabel.setText("Astronaut HP " + player.getHealth() + "/" + player.getMaxHealth()
                + " [Shield: " + player.getShield() + "]"
                + " | ATK " + player.getAttackPower()
                + " | DEF " + player.getDefensePower()
                + " | " + location);
    }

    private void updateButtons() {
        Player player = controller.getPlayer();
        boolean alive = player != null && player.isAlive();
        boolean inCombat = controller.getCurrentEnemy() != null && controller.getCurrentEnemy().isAlive();
        boolean gameOver = controller.isGameOver();

        attackButton.setEnabled(alive && inCombat && !gameOver);
        dodgeButton.setEnabled(alive && inCombat && !gameOver);
        useItemButton.setEnabled(alive && inventoryBox.getItemCount() > 0 && !gameOver);
        restartButton.setEnabled(true);
        saveButton.setEnabled(alive && !gameOver);
        loadButton.setEnabled(true);
    }

    @Override
    public String getLogText() {
        return logArea.getText();
    }

    @Override
    public void showGameOverDialog(boolean victory) {
        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (window instanceof GameWindow) {
            ((GameWindow) window).showGameOverDialog(victory);
        }
    }

    private void saveGameAction() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Save Game Progress");
        if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try {
                controller.saveGame(file.getAbsolutePath());
                javax.swing.JOptionPane.showMessageDialog(this, "Game progress saved successfully!", "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Failed to save game: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadGameAction() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Load Game Progress");
        if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try {
                controller.loadGame(file.getAbsolutePath());
                javax.swing.JOptionPane.showMessageDialog(this, "Game progress loaded successfully!", "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Failed to load game: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
