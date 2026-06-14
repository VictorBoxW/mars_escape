package view;

import controller.GameController;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;
import javax.swing.JPanel;
import model.Castle;
import model.Enemy;
import model.Floor;
import model.Player;
import model.Room;

public class ScenePanel extends JPanel {
    private final GameController controller;
    private final Random random = new Random(42);

    public ScenePanel(GameController controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(880, 600)); // Increased height for exploration
        setBackground(new Color(10, 10, 15));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Enemy enemy = controller.getCurrentEnemy();

        if (enemy != null && enemy.isAlive()) {
            drawCombatScene(g2d, enemy);
        } else {
            drawExplorationScene(g2d);
        }
    }

    private void drawExplorationScene(Graphics2D g2d) {
        drawBackground(g2d);
        
        Floor floor = controller.getCastle().getCurrentFloor();
        if (floor != null) {
            for (model.Room room : floor.getRooms()) {
                drawRoom(g2d, room);
            }
        }

        Player player = controller.getPlayer();
        if (player != null) {
            drawTopDownAstronaut(g2d, player.getX(), player.getY());
        }

        drawLocation(g2d, controller.getCastle());
    }

    private void drawCombatScene(Graphics2D g2d, Enemy enemy) {
        drawBackground(g2d);
        drawLocation(g2d, controller.getCastle());
        Player player = controller.getPlayer();

        if (player != null) {
            drawCharacter(g2d, 150, 200, true, "Astronaut", player.getHealth(), player.getMaxHealth(), player.getShield(), false);
        }
        drawCharacter(g2d, 650, 200, false, "Enemy", enemy.getHealth(), enemy.getMaxHealth(), enemy.getShield(), enemy.isBoss());
    }

    private void drawRoom(Graphics2D g2d, model.Room room) {
        g2d.setColor(new Color(45, 45, 70, 180));
        g2d.fillRoundRect(room.getX(), room.getY(), room.getWidth(), room.getHeight(), 15, 15);
        g2d.setColor(room.isCleared() ? new Color(0, 255, 100) : new Color(255, 50, 50));
        g2d.setStroke(new java.awt.BasicStroke(2));
        g2d.drawRoundRect(room.getX(), room.getY(), room.getWidth(), room.getHeight(), 15, 15);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        g2d.drawString(room.getName(), room.getX() + 10, room.getY() + 20);

        if (!room.isCleared()) {
            drawTopDownAlien(g2d, room.getX() + room.getWidth()/2, room.getY() + room.getHeight()/2);
        }
    }

    private void drawTopDownAstronaut(Graphics2D g2d, int x, int y) {
        // Body (Circle from top) - Scaled up
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - 20, y - 20, 40, 40);
        // Backpack
        g2d.setColor(new Color(180, 180, 180));
        g2d.fillRect(x - 15, y + 12, 30, 10);
        // Visor
        g2d.setColor(new Color(80, 200, 255));
        g2d.fillRoundRect(x - 12, y - 18, 24, 10, 6, 6);
    }

    private void drawTopDownAlien(Graphics2D g2d, int x, int y) {
        // Alien head from top
        g2d.setColor(new Color(40, 180, 40));
        g2d.fillOval(x - 22, y - 22, 44, 44);
        // Eyes
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x - 14, y - 12, 10, 14);
        g2d.fillOval(x + 4, y - 12, 10, 14);
    }

    private void drawBackground(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();

        // 1. Dark Castle Walls (Stone Texture)
        g2d.setColor(new Color(20, 20, 25));
        g2d.fillRect(0, 0, w, h);
        
        // Draw stone bricks for texture
        g2d.setColor(new Color(30, 30, 40));
        random.setSeed(123);
        for (int i = 0; i < 60; i++) {
            int bx = random.nextInt(w);
            int by = random.nextInt(h);
            g2d.drawRect(bx, by, 60, 30);
        }

        // 2. Torches with Glow
        drawTorch(g2d, 100, 80);
        drawTorch(g2d, w - 100, 80);
        drawTorch(g2d, 100, h - 100);
        drawTorch(g2d, w - 100, h - 100);

        // 3. Central pathway hints
        g2d.setColor(new Color(25, 25, 35));
        g2d.fillRoundRect(50, 50, w - 100, h - 100, 30, 30);
    }

    private void drawTorch(Graphics2D g, int x, int y) {
        // Torch stick
        g.setColor(new Color(70, 45, 30));
        g.fillRect(x - 4, y, 8, 35);
        
        // Flame glow
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {new Color(255, 160, 0, 120), new Color(255, 80, 0, 0)};
        java.awt.RadialGradientPaint glow = new java.awt.RadialGradientPaint(x, y, 60, dist, colors);
        g.setPaint(glow);
        g.fillOval(x - 60, y - 60, 120, 120);
        
        // Flame core
        g.setColor(new Color(255, 220, 80));
        g.fillOval(x - 6, y - 12, 12, 18);
    }

    private void drawLocation(Graphics2D g2d, Castle castle) {
        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        g2d.setColor(new Color(50, 180, 255)); // Sci-fi blue
        g2d.drawString("DARK ALIEN FORTRESS", 25, 35);

        if (castle == null) return;

        Floor floor = castle.getCurrentFloor();
        Room room = floor == null ? null : floor.getCurrentRoom();
        
        String levelInfo = "FLOOR LEVEL: " + castle.getCurrentFloorNumber() + " / " + castle.getFloorCount();
        String roomInfo = (room == null) ? "CORE SECURED" : floor.getName().toUpperCase() + " > " + room.getName().toUpperCase();

        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString(levelInfo, 25, 60);
        g2d.drawString(roomInfo, 25, 85);
    }

    private void drawCharacter(Graphics2D g, int x, int y, boolean isPlayer, String name, int health, int maxHealth, int shield, boolean isBoss) {
        if (isPlayer) {
            drawAstronaut(g, x, y);
        } else {
            drawAlien(g, x, y, isBoss);
        }

        // Name and Health Bar (Adjusted position)
        int barY = y + 25;
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g.drawString(name, x - 20, y - 120);

        // Shield Bar (Blue, above Health Bar)
        if (shield > 0) {
            g.setColor(new Color(40, 40, 40));
            g.fillRect(x - 20, barY - 12, 120, 8);
            int shieldWidth = Math.min(120, shield * 2); 
            g.setColor(new Color(0, 160, 255));
            g.fillRect(x - 20, barY - 12, shieldWidth, 8);
            g.setColor(Color.WHITE);
            g.drawRect(x - 20, barY - 12, 120, 8);
        }

        g.setColor(new Color(40, 40, 40));
        g.fillRect(x - 20, barY, 120, 10);

        int barWidth = (int) Math.round(120.0 * health / maxHealth);
        g.setColor(new Color(100, 255, 120));
        g.fillRect(x - 20, barY, barWidth, 10);

        g.setColor(Color.WHITE);
        g.drawRect(x - 20, barY, 120, 10);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        g.drawString(health + "/" + maxHealth + (shield > 0 ? " (+" + shield + ")" : ""), x + 10, barY + 26);
    }

    private void drawAstronaut(Graphics2D g, int x, int y) {
        // Astronaut Side View (Scaled up)
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y - 75, 50, 75, 12, 12); // Body
        g.fillOval(x + 5, y - 105, 40, 40); // Helmet
        g.setColor(new Color(100, 220, 255));
        g.fillRoundRect(x + 12, y - 98, 30, 20, 10, 10); // Visor
        g.setColor(Color.WHITE);
        g.fillRect(x + 8, y, 14, 18); // Legs
        g.fillRect(x + 28, y, 14, 18);
        g.setColor(new Color(40, 40, 40));
        g.fillRect(x + 5, y + 15, 20, 6); // Boots
        g.fillRect(x + 25, y + 15, 20, 6);
    }

    private void drawAlien(Graphics2D g, int x, int y, boolean isBoss) {
        if (isBoss) {
            g.setColor(new Color(30, 30, 30));
        } else {
            g.setColor(new Color(60, 190, 60));
        }
        // Alien Side View (Scaled down to match Astronaut)
        g.fillOval(x, y - 70, 50, 75); // Body
        g.fillOval(x - 5, y - 100, 60, 45); // Head
        
        if (isBoss) {
            g.setColor(new Color(255, 0, 0)); // Red eyes for boss
        } else {
            g.setColor(Color.BLACK);
        }
        g.fillOval(x + 8, y - 88, 16, 22); // Eyes
        g.fillOval(x + 36, y - 88, 16, 22);
    }

    private void drawCrystal(Graphics2D g2d) {
        g2d.setColor(new Color(0, 255, 255, 200));
        int[] xPoints = {440, 460, 480, 460};
        int[] yPoints = {100, 70, 100, 130};
        g2d.fillPolygon(xPoints, yPoints, 4);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        g2d.drawString("FUEL CRYSTAL SECURED", 400, 160);
    }

    private void drawEmptyRoom(Graphics2D g2d) {
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        g2d.drawString("AREA CLEAR", 420, 110);
    }
}
