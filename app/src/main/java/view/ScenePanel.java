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
        g2d.setColor(new Color(40, 40, 60, 150));
        g2d.fillRect(room.getX(), room.getY(), room.getWidth(), room.getHeight());
        g2d.setColor(room.isCleared() ? Color.GREEN : Color.RED);
        g2d.drawRect(room.getX(), room.getY(), room.getWidth(), room.getHeight());
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        g2d.drawString(room.getName(), room.getX() + 5, room.getY() + 15);

        if (!room.isCleared()) {
            drawAlien(g2d, room.getX() + room.getWidth()/2, room.getY() + room.getHeight()/2, room.getEnemies().stream().anyMatch(Enemy::isBoss));
        }
    }

    private void drawTopDownAstronaut(Graphics2D g2d, int x, int y) {
        // Body (Circle from top)
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - 15, y - 15, 30, 30);
        // Backpack
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillRect(x - 12, y + 10, 24, 8);
        // Visor (Facing up by default, or could rotate)
        g2d.setColor(new Color(100, 220, 255));
        g2d.fillRoundRect(x - 8, y - 12, 16, 6, 4, 4);
    }

    private void drawBackground(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();

        // 1. Dark Castle Walls (Stone Texture)
        g2d.setColor(new Color(25, 25, 30));
        g2d.fillRect(0, 0, w, h - 60);
        
        // Draw some "stone bricks" for texture
        g2d.setColor(new Color(35, 35, 45));
        random.setSeed(123);
        for (int i = 0; i < 40; i++) {
            int bx = random.nextInt(w);
            int by = random.nextInt(h - 80);
            g2d.drawRect(bx, by, 40, 20);
        }

        // 2. Torches with Glow
        drawTorch(g2d, 100, 80);
        drawTorch(g2d, w - 100, 80);
        drawTorch(g2d, w/2, 60);

        // 3. Ground (Darker stone floor)
        GradientPaint floorGradient = new GradientPaint(0, h-60, new Color(40, 40, 50), 0, h, new Color(20, 20, 25));
        g2d.setPaint(floorGradient);
        g2d.fillRect(0, h-60, w, 60);
        
        // 4. Entrance/Archway (Darker)
        g2d.setColor(new Color(15, 15, 20));
        g2d.fillRoundRect(w/2 - 80, h - 140, 160, 80, 40, 40);
        g2d.setColor(new Color(0, 30, 100)); // Very dark blue rim
        g2d.setStroke(new java.awt.BasicStroke(2));
        g2d.drawRoundRect(w/2 - 80, h - 140, 160, 80, 40, 40);
    }

    private void drawTorch(Graphics2D g, int x, int y) {
        // Torch stick
        g.setColor(new Color(60, 40, 30));
        g.fillRect(x - 3, y, 6, 30);
        
        // Flame glow
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {new Color(255, 150, 0, 100), new Color(255, 100, 0, 0)};
        java.awt.RadialGradientPaint glow = new java.awt.RadialGradientPaint(x, y, 40, dist, colors);
        g.setPaint(glow);
        g.fillOval(x - 40, y - 40, 80, 80);
        
        // Flame core
        g.setColor(new Color(255, 200, 50));
        g.fillOval(x - 5, y - 10, 10, 15);
    }

    private void drawLocation(Graphics2D g2d, Castle castle) {
        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        g2d.setColor(new Color(0, 150, 255)); // Sci-fi blue
        g2d.drawString("ALIEN FORTRESS", 18, 28);

        if (castle == null) return;

        Floor floor = castle.getCurrentFloor();
        Room room = floor == null ? null : floor.getCurrentRoom();
        
        // Restoring the Level / Floor info as before
        String levelInfo = "Floor " + castle.getCurrentFloorNumber() + " / " + castle.getFloorCount();
        String roomInfo = (room == null) ? "Energy crystal secured" : floor.getName() + " - " + room.getName();

        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString(levelInfo, 18, 50);
        g2d.drawString(roomInfo, 18, 70);
    }

    private void drawCharacter(Graphics2D g, int x, int y, boolean isPlayer, String name, int health, int maxHealth, int shield, boolean isBoss) {
        if (isPlayer) {
            drawAstronaut(g, x, y);
        } else {
            drawAlien(g, x, y, isBoss);
        }

        // Name and Health Bar (Adjusted position)
        int barY = y + 15;
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        g.drawString(name, x - 20, y - 90);

        // Shield Bar (Blue, above Health Bar)
        if (shield > 0) {
            g.setColor(new Color(50, 50, 50));
            g.fillRect(x - 20, barY - 10, 100, 6);
            int shieldWidth = Math.min(100, shield * 2); // 50 shield = 100% bar
            g.setColor(new Color(0, 150, 255));
            g.fillRect(x - 20, barY - 10, shieldWidth, 6);
            g.setColor(Color.WHITE);
            g.drawRect(x - 20, barY - 10, 100, 6);
        }

        g.setColor(new Color(50, 50, 50));
        g.fillRect(x - 20, barY, 100, 8);

        int barWidth = (int) Math.round(100.0 * health / maxHealth);
        g.setColor(new Color(85, 209, 107));
        g.fillRect(x - 20, barY, barWidth, 8);

        g.setColor(Color.WHITE);
        g.drawRect(x - 20, barY, 100, 8);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        g.drawString(health + "/" + maxHealth + (shield > 0 ? " (+" + shield + ")" : ""), x + 10, barY + 22);
    }

    private void drawAstronaut(Graphics2D g, int x, int y) {
        // Scaled down version for Scene
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y - 60, 40, 60, 10, 10); // Body
        g.fillOval(x + 5, y - 85, 30, 30); // Helmet
        g.setColor(new Color(100, 220, 255));
        g.fillRoundRect(x + 10, y - 80, 25, 15, 8, 8); // Visor
        g.setColor(Color.WHITE);
        g.fillRect(x + 5, y, 12, 15); // Legs
        g.fillRect(x + 23, y, 12, 15);
        g.setColor(new Color(50, 50, 50));
        g.fillRect(x + 3, y + 12, 16, 5); // Boots
        g.fillRect(x + 21, y + 12, 16, 5);
    }

    private void drawAlien(Graphics2D g, int x, int y, boolean isBoss) {
        if (isBoss) {
            g.setColor(Color.BLACK);
        } else {
            g.setColor(new Color(50, 200, 50));
        }
        // Scaled up to match astronaut's height (total height approx 75-80px)
        g.fillOval(x, y - 55, 45, 65); // Body (wider and taller)
        g.fillOval(x - 5, y - 80, 55, 35); // Head (positioned higher)
        
        if (isBoss) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.BLACK);
        }
        g.fillOval(x + 7, y - 72, 14, 18); // Eyes (scaled up proportionally)
        g.fillOval(x + 32, y - 72, 14, 18);
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
