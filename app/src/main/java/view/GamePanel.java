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

public class GamePanel extends JPanel {
    private final GameController controller;
    private final ScenePanel scenePanel;
    private final JTextArea logArea;
    private final JLabel statusLabel;
    private final JComboBox<String> inventoryBox;
    private final JButton attackButton;
    private final JButton dodgeButton;
    private final JButton useItemButton;
    private final JButton continueButton;
    private final JButton restartButton;

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
        this.continueButton = new JButton("Continue");
        this.restartButton = new JButton("Restart");

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
        continueButton.addActionListener(event -> { controller.continueExploring(); requestFocusInWindow(); });
        restartButton.addActionListener(event -> { controller.restartGame(); requestFocusInWindow(); });

        // Make buttons non-focusable so they don't capture arrow key events
        attackButton.setFocusable(false);
        dodgeButton.setFocusable(false);
        useItemButton.setFocusable(false);
        continueButton.setFocusable(false);
        restartButton.setFocusable(false);
        inventoryBox.setFocusable(false);
    }

    private JPanel buildSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout(8, 8));
        JPanel actionPanel = new JPanel();

        actionPanel.add(attackButton);
        actionPanel.add(dodgeButton);
        actionPanel.add(inventoryBox);
        actionPanel.add(useItemButton);
        actionPanel.add(continueButton);
        actionPanel.add(restartButton);

        southPanel.add(statusLabel, BorderLayout.NORTH);
        southPanel.add(actionPanel, BorderLayout.CENTER);

        return southPanel;
    }

    private void updateInventory() {
        Player player = controller.getPlayer();
        int selectedIndex = inventoryBox.getSelectedIndex();

        inventoryBox.removeAllItems();
        if (player == null) {
            return;
        }

        List<Item> inventory = player.getInventory();
        for (Item item : inventory) {
            inventoryBox.addItem(item.getName());
        }

        if (!inventory.isEmpty()) {
            inventoryBox.setSelectedIndex(Math.max(0, Math.min(selectedIndex, inventory.size() - 1)));
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
        String weapon = player.getWeapon() == null ? "none" : player.getWeapon().getName();
        String armor = player.getArmor() == null ? "none" : player.getArmor().getName();
        String location = room == null
                ? "Mission complete"
                : floor.getName() + " / " + room.getName();

        statusLabel.setText("Astronaut HP " + player.getHealth() + "/" + player.getMaxHealth()
                + " [Shield: " + player.getShield() + "]"
                + " | ATK " + player.getAttackPower()
                + " | DEF " + player.getDefensePower()
                + " | Weapon: " + weapon
                + " | Armor: " + armor
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
        continueButton.setEnabled(controller.canContinue());
        restartButton.setEnabled(true);
    }
}
