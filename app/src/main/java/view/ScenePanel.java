package view;

import controller.GameController;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
        setPreferredSize(new Dimension(880, 600)); 
        setBackground(new Color(10, 10, 15));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics;
        Enemy enemy = controller.getCurrentEnemy();

        if (enemy != null && enemy.isAlive()) {
            drawCombatScene(g2d, enemy);
        } else {
            drawExplorationScene(g2d);
        }
    }

    private void drawExplorationScene(Graphics2D g2d) {
        Player player = controller.getPlayer();
        Floor floor = controller.getCastle().getCurrentFloor();
        if (player == null || floor == null) return;

        int w = getWidth();
        int h = getHeight();

        int camX = Math.max(0, Math.min(player.getX() - w / 2, floor.getWidth() - w));
        int camY = Math.max(0, Math.min(player.getY() - h / 2, floor.getHeight() - h));

        g2d.translate(-camX, -camY);

        drawBackground(g2d, floor);
        
        for (model.Room room : floor.getRooms()) {
            drawRoom(g2d, room);
        }

        for (model.PickableItem pi : floor.getItems()) {
            if (!pi.isPickedUp()) {
                drawPickableItem(g2d, pi);
            }
        }

        drawTopDownAstronaut(g2d, player.getX(), player.getY(), player.getRotation(), player.getWalkPhase());
        
        drawFogOfWar(g2d, player.getX(), player.getY(), floor.getWidth(), floor.getHeight());

        g2d.translate(camX, camY); 
        drawLocation(g2d, controller.getCastle());
    }

    private void drawFogOfWar(Graphics2D g2d, int px, int py, int fw, int fh) {
        int visionRadius = 350;
        
        // Create a mask for the fog
        java.awt.geom.Area fog = new java.awt.geom.Area(new java.awt.Rectangle(0, 0, fw, fh));
        
        // We use a radial gradient to create a smooth transition
        float[] dist = {0.0f, 0.7f, 1.0f};
        Color[] colors = {new Color(0, 0, 0, 0), new Color(0, 0, 0, 150), new Color(0, 0, 0, 255)};
        
        java.awt.RadialGradientPaint p = new java.awt.RadialGradientPaint(
            px, py, visionRadius, dist, colors);
        
        g2d.setPaint(p);
        g2d.fill(fog);
    }

    private void drawCombatScene(Graphics2D g2d, Enemy enemy) {
        drawBackground(g2d, null);
        drawLocation(g2d, controller.getCastle());
        Player player = controller.getPlayer();

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        if (player != null) {
            drawCharacter(g2d, centerX - 290, centerY + 50, true, "Astronaut", player.getHealth(), player.getMaxHealth(), player.getShield(), false);
        }
        drawCharacter(g2d, centerX + 210, centerY + 50, false, "Enemy", enemy.getHealth(), enemy.getMaxHealth(), enemy.getShield(), enemy.isBoss());
    }

    private void drawRoom(Graphics2D g2d, model.Room room) {
        if (!room.isCleared()) {
            // Boxes and names removed as requested
            boolean isBoss = room.getName().equals("Core Chamber");
            drawTopDownAlien(g2d, room.getX() + room.getWidth()/2, room.getY() + room.getHeight()/2, isBoss);
        }
    }

    private void drawTopDownAstronaut(Graphics2D g2d, int x, int y, double rotation, double walkPhase) {
        g2d.translate(x, y);
        g2d.rotate(Math.toRadians(rotation));

        // Legs/Boots (from combat style)
        int footOffset = (int) (Math.sin(walkPhase) * 12);
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(-18, 10 + footOffset, 12, 15); // Left boot
        g2d.fillRect(6, 10 - footOffset, 12, 15);  // Right boot
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(-15, 0 + footOffset, 6, 12); // Left leg
        g2d.fillRect(9, 0 - footOffset, 6, 12);   // Right leg

        // Backpack/Body (White suit)
        g2d.setColor(new Color(220, 220, 220));
        g2d.fillRoundRect(-22, -10, 44, 28, 8, 8);
        
        // Suit Body
        g2d.setColor(Color.WHITE);
        g2d.fillOval(-20, -15, 40, 35);
        
        // Helmet (White circle)
        g2d.fillOval(-16, -28, 32, 32);
        
        // Visor (Glowing blue visor)
        g2d.setColor(new Color(100, 220, 255));
        g2d.fillRoundRect(-12, -24, 24, 12, 6, 6);

        g2d.rotate(-Math.toRadians(rotation));
        g2d.translate(-x, -y);
    }

    private void drawTopDownAlien(Graphics2D g2d, int x, int y, boolean isBoss) {
        // Alien Glow
        float[] dist = {0.0f, 1.0f};
        Color[] colors;
        if (isBoss) {
            colors = new Color[]{new Color(50, 0, 50, 150), new Color(0, 0, 0, 0)};
        } else {
            colors = new Color[]{new Color(0, 255, 0, 100), new Color(0, 255, 0, 0)};
        }
        java.awt.RadialGradientPaint glow = new java.awt.RadialGradientPaint(x, y, 75, dist, colors);
        g2d.setPaint(glow);
        g2d.fillOval(x - 75, y - 75, 150, 150);

        // Alien Body (Visible from above)
        if (isBoss) {
            g2d.setColor(new Color(20, 20, 20));
        } else {
            g2d.setColor(new Color(45, 150, 45));
        }
        g2d.fillOval(x - 20, y - 5, 40, 30);
        
        // Large Alien Head (Combat style)
        if (isBoss) {
            g2d.setColor(new Color(30, 30, 30));
        } else {
            g2d.setColor(new Color(60, 190, 60));
        }
        g2d.fillOval(x - 28, y - 35, 56, 45); 
        
        // Almond Eyes (Combat style)
        if (isBoss) {
            g2d.setColor(new Color(255, 0, 0));
        } else {
            g2d.setColor(Color.BLACK);
        }
        g2d.fillOval(x - 20, y - 22, 15, 22); // Left eye
        g2d.fillOval(x + 5, y - 22, 15, 22);  // Right eye
    }

    private void drawBackground(Graphics2D g2d, Floor floor) {
        int w = floor != null ? floor.getWidth() : getWidth();
        int h = floor != null ? floor.getHeight() : getHeight();

        g2d.setColor(new Color(20, 20, 25));
        g2d.fillRect(0, 0, w, h);
        
        g2d.setColor(new Color(30, 30, 40));
        for (int i = 0; i < w; i += 100) {
            for (int j = 0; j < h; j += 100) {
                g2d.drawRect(i, j, 100, 100);
            }
        }

        if (floor != null) {
            g2d.setColor(new Color(60, 60, 80));
            for (java.awt.Rectangle wall : floor.getWalls()) {
                g2d.fillRect(wall.x, wall.y, wall.width, wall.height);
                g2d.setColor(new Color(80, 80, 100));
                g2d.drawRect(wall.x, wall.y, wall.width, wall.height);
                g2d.setColor(new Color(60, 60, 80));
            }
        }

        if (floor != null) {
            for (int i = 200; i < w; i += 400) {
                for (int j = 200; j < h; j += 400) {
                    // Skip torches in the new upper corridor to keep it clean
                    if (i > 1240 && j < 1100) continue;
                    drawTorch(g2d, i, j);
                }
            }
        } else {
            drawTorch(g2d, 100, 100);
            drawTorch(g2d, getWidth() - 100, 100);
        }
    }

    private void drawTorch(Graphics2D g, int x, int y) {
        g.setColor(new Color(70, 45, 30));
        g.fillRect(x - 4, y, 8, 35);
        
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {new Color(255, 160, 0, 120), new Color(255, 80, 0, 0)};
        java.awt.RadialGradientPaint glow = new java.awt.RadialGradientPaint(x, y, 60, dist, colors);
        g.setPaint(glow);
        g.fillOval(x - 60, y - 60, 120, 120);
        
        g.setColor(new Color(255, 220, 80));
        g.fillOval(x - 6, y - 12, 12, 18);
    }

    private void drawLocation(Graphics2D g2d, Castle castle) {
        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        g2d.setColor(new Color(50, 180, 255)); 
        g2d.drawString("DARK ALIEN FORTRESS", 25, 35);

        if (castle == null) return;

        Floor floor = castle.getCurrentFloor();
        
        String levelInfo = "FLOOR LEVEL: " + castle.getCurrentFloorNumber() + " / " + castle.getFloorCount();
        // Room name removed as requested
        String floorName = (floor == null) ? "CORE SECURED" : floor.getName().toUpperCase();

        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString(levelInfo, 25, 60);
        g2d.drawString(floorName, 25, 85);
    }

    private void drawCharacter(Graphics2D g, int x, int y, boolean isPlayer, String name, int health, int maxHealth, int shield, boolean isBoss) {
        if (isPlayer) {
            drawAstronaut(g, x, y);
        } else {
            drawAlien(g, x, y, isBoss);
        }

        int barY = y + 25;
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g.drawString(name, x - 20, y - 120);

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
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y - 75, 50, 75, 12, 12); 
        g.fillOval(x + 5, y - 105, 40, 40); 
        g.setColor(new Color(100, 220, 255));
        g.fillRoundRect(x + 12, y - 98, 30, 20, 10, 10); 
        g.setColor(Color.WHITE);
        g.fillRect(x + 8, y, 14, 18); 
        g.fillRect(x + 28, y, 14, 18);
        g.setColor(new Color(40, 40, 40));
        g.fillRect(x + 5, y + 15, 20, 6); 
        g.fillRect(x + 25, y + 15, 20, 6);
    }

    private void drawAlien(Graphics2D g, int x, int y, boolean isBoss) {
        if (isBoss) {
            g.setColor(new Color(30, 30, 30));
        } else {
            g.setColor(new Color(60, 190, 60));
        }
        g.fillOval(x, y - 70, 50, 75); 
        g.fillOval(x - 5, y - 100, 60, 45); 
        
        if (isBoss) {
            g.setColor(new Color(255, 0, 0)); 
        } else {
            g.setColor(Color.BLACK);
        }
        g.fillOval(x + 8, y - 88, 16, 22); 
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

    private void drawPickableItem(Graphics2D g, model.PickableItem pi) {
        int x = pi.getX();
        int y = pi.getY();
        String name = pi.getItem().getName();

        if (name.contains("Med Kit")) {
            // Green rectangle for Med Kit
            g.setColor(new Color(40, 200, 40));
            g.fillRect(x, y, 30, 20);
            g.setColor(Color.WHITE);
            g.fillRect(x + 12, y + 4, 6, 12);
            g.fillRect(x + 9, y + 7, 12, 6);
        } else if (name.contains("Shield Cell")) {
            // Dark blue cylinder for Shield Cell
            g.setColor(new Color(0, 50, 150));
            g.fillOval(x, y, 20, 10); // top
            g.fillRect(x, y + 5, 20, 20); // body
            g.fillOval(x, y + 20, 20, 10); // bottom
            g.setColor(new Color(0, 150, 255, 150));
            g.fillRect(x + 4, y + 8, 12, 14); // glow
        }
    }
}
