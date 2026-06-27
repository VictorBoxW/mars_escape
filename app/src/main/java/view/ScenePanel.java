package view;

import controller.GameController;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

import model.Castle;
import model.Enemy;
import model.Floor;
import model.Player;

public class ScenePanel extends JPanel {
    private final GameController controller;

    // Design resolution — all combat-scene coordinates are authored against this size
    private static final double BASE_WIDTH = 880.0;
    private static final double BASE_HEIGHT = 600.0;

    private static final int DOOR_INTERACTION_RANGE = 100;
    private static final int BOSS_GATE_WARN_DISTANCE = 300;
    private static final int VISION_RADIUS = 350;
    private static final int TORCH_CULL_MARGIN = 80;
    private static final int GRID_CELL_SIZE = 100;

    public ScenePanel(GameController controller) {
        this.controller = controller;
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

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int camX = Math.max(0, Math.min(player.getX() - panelWidth / 2, floor.getWidth() - panelWidth));
        int camY = Math.max(0, Math.min(player.getY() - panelHeight / 2, floor.getHeight() - panelHeight));

        // Clip to visible viewport to avoid painting off-screen content
        g2d.setClip(0, 0, panelWidth, panelHeight);
        g2d.translate(-camX, -camY);

        drawBackground(g2d, floor, camX, camY, panelWidth, panelHeight);

        for (model.Room room : floor.getRooms()) {
            drawRoom(g2d, room);
        }

        for (model.PickableItem pi : floor.getItems()) {
            if (!pi.isPickedUp()) {
                drawPickableItem(g2d, pi);
            }
        }

        for (model.Door door : floor.getDoors()) {
            drawDoor(g2d, door);
        }

        drawTopDownAstronaut(g2d, player.getX(), player.getY(), player.getRotation(), player.getWalkPhase());

        drawDoorPrompts(g2d, floor, player);
        drawBossGatePrompt(g2d, floor, player);

        drawFogOfWar(g2d, player.getX(), player.getY(), camX, camY, panelWidth, panelHeight);

        g2d.translate(camX, camY);
        g2d.setClip(null);
        drawLocation(g2d, controller.getCastle());
    }

    /** Draws the "open/close/unlock" prompt above the first door the player is standing near. */
    private void drawDoorPrompts(Graphics2D g2d, Floor floor, Player player) {
        for (model.Door door : floor.getDoors()) {
            if (door.isNear(player.getX(), player.getY(), DOOR_INTERACTION_RANGE)) {
                drawPrompt(g2d, doorPromptText(door, player), player.getX(), player.getY() - 60);
                break;
            }
        }
    }

    private String doorPromptText(model.Door door, Player player) {
        if (!door.isLocked()) {
            String action = door.isOpen() ? "Close" : "Open";
            return "Press 'O' to " + action + " Door";
        }

        model.DoorUnlockStrategy strategy = door.getUnlockStrategy();
        return (strategy != null) ? strategy.getPromptText(player) : "Door is Locked";
    }

    /** Draws the boss-gate warning when the player approaches the boss room before clearing the floor. */
    private void drawBossGatePrompt(Graphics2D g2d, Floor floor, Player player) {
        if (floor.canAccessBoss()) return;

        for (model.Room room : floor.getRooms()) {
            if (!room.isBossRoom()) continue;

            int bx = room.getX() + room.getWidth() / 2;
            int by = room.getY() + room.getHeight() / 2;
            double dist = Math.sqrt(Math.pow(player.getX() - bx, 2) + Math.pow(player.getY() - by, 2));
            if (dist < BOSS_GATE_WARN_DISTANCE) {
                drawPrompt(g2d, "You have to defeat both Aliens\nbefore confronting the Dark Alien boss!", bx, by - 80);
            }
        }
    }

    /**
     * Draw fog of war clipped to the visible viewport only (not the entire floor).
     * This is the key GPU optimisation — previously we filled a 2400×2400 rect
     * with a RadialGradientPaint every frame; now we only fill the viewport.
     */
    private void drawFogOfWar(Graphics2D g2d, int px, int py, int camX, int camY, int vpW, int vpH) {
        // Only fill the visible viewport rectangle (camX..camX+vpW, camY..camY+vpH)
        // instead of the entire floor (0..floorWidth, 0..floorHeight)
        java.awt.Rectangle viewport = new java.awt.Rectangle(camX, camY, vpW, vpH);

        float[] dist = {0.0f, 0.7f, 1.0f};
        Color[] colors = {new Color(0, 0, 0, 0), new Color(0, 0, 0, 150), new Color(0, 0, 0, 255)};

        java.awt.RadialGradientPaint p = new java.awt.RadialGradientPaint(
                px, py, VISION_RADIUS, dist, colors);

        g2d.setPaint(p);
        g2d.fill(viewport);
    }

    /**
     * Draw the combat scene scaled to the current panel size.
     * All coordinates are authored for BASE_WIDTH×BASE_HEIGHT (880×600)
     * and then uniformly scaled up so they look the same on any resolution.
     */
    private void drawCombatScene(Graphics2D g2d, Enemy enemy) {
        int w = getWidth();
        int h = getHeight();

        // Compute uniform scale factor to fit the design into the current panel
        double scaleX = w / BASE_WIDTH;
        double scaleY = h / BASE_HEIGHT;
        double scale = Math.min(scaleX, scaleY);

        // Center the scaled content if aspect ratios differ
        double offsetX = (w - BASE_WIDTH * scale) / 2.0;
        double offsetY = (h - BASE_HEIGHT * scale) / 2.0;

        // Draw the background at full panel size (no scaling needed for bg)
        drawBackground(g2d, null, 0, 0, w, h);

        // Apply the scaling transform for all combat-scene content
        java.awt.geom.AffineTransform savedTransform = g2d.getTransform();
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);

        drawLocation(g2d, controller.getCastle());
        Player player = controller.getPlayer();

        // These coordinates are now in the 880×600 design space
        int centerX = (int) (BASE_WIDTH / 2);
        int centerY = (int) (BASE_HEIGHT / 2);

        if (player != null) {
            drawCharacter(g2d, centerX - 290, centerY + 50, true, "Astronaut", player.getHealth(), player.getMaxHealth(), player.getShield(), false);
        }
        drawCharacter(g2d, centerX + 210, centerY + 50, false, enemy.getName(), enemy.getHealth(), enemy.getMaxHealth(), enemy.getShield(), enemy.isBoss());

        // Restore the original transform
        g2d.setTransform(savedTransform);
    }

    private void drawRoom(Graphics2D g2d, model.Room room) {
        if (!room.isCleared()) {
            // Boxes and names removed as requested
            boolean isBoss = room.isBossRoom();
            drawTopDownAlien(g2d, room.getX() + room.getWidth() / 2, room.getY() + room.getHeight() / 2, isBoss);
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

        // Arms (swing opposite the same-side leg for a natural walking motion)
        int armOffset = (int) (Math.sin(walkPhase) * 8);
        g2d.fillRect(-28, -5 - armOffset, 8, 20); // Left arm
        g2d.fillRect(20, -5 + armOffset, 8, 20);  // Right arm

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

        // Alien Body (Visible from above) — slightly darker shade than the head
        g2d.setColor(isBoss ? new Color(20, 20, 20) : new Color(45, 150, 45));
        g2d.fillOval(x - 20, y - 5, 40, 30);

        // Large Alien Head (Combat style)
        g2d.setColor(getAlienBodyColor(isBoss));
        g2d.fillOval(x - 28, y - 35, 56, 45);

        // Almond Eyes (Combat style)
        g2d.setColor(getAlienEyeColor(isBoss));
        g2d.fillOval(x - 20, y - 22, 15, 22); // Left eye
        g2d.fillOval(x + 5, y - 22, 15, 22);  // Right eye
    }

    private Color getAlienBodyColor(boolean isBoss) {
        return isBoss ? new Color(30, 30, 30) : new Color(60, 190, 60);
    }

    private Color getAlienEyeColor(boolean isBoss) {
        return isBoss ? new Color(255, 0, 0) : Color.BLACK;
    }

    /**
     * Draw the background grid and walls.
     * Now accepts viewport bounds so we only draw grid cells visible on screen.
     */
    private void drawBackground(Graphics2D g2d, Floor floor, int vpX, int vpY, int vpW, int vpH) {
        int floorWidth = floor != null ? floor.getWidth() : getWidth();
        int floorHeight = floor != null ? floor.getHeight() : getHeight();

        g2d.setColor(new Color(20, 20, 25));
        g2d.fillRect(vpX, vpY, vpW, vpH);

        // Only draw grid cells that overlap the viewport
        int gridStartX = Math.max(0, (vpX / GRID_CELL_SIZE) * GRID_CELL_SIZE);
        int gridStartY = Math.max(0, (vpY / GRID_CELL_SIZE) * GRID_CELL_SIZE);
        int gridEndX = Math.min(floorWidth, vpX + vpW + GRID_CELL_SIZE);
        int gridEndY = Math.min(floorHeight, vpY + vpH + GRID_CELL_SIZE);

        g2d.setColor(new Color(30, 30, 40));
        for (int i = gridStartX; i < gridEndX; i += GRID_CELL_SIZE) {
            for (int j = gridStartY; j < gridEndY; j += GRID_CELL_SIZE) {
                g2d.drawRect(i, j, GRID_CELL_SIZE, GRID_CELL_SIZE);
            }
        }

        if (floor != null) {
            java.awt.Rectangle vpRect = new java.awt.Rectangle(vpX, vpY, vpW, vpH);
            g2d.setColor(new Color(60, 60, 80));
            for (java.awt.Rectangle wall : floor.getWalls()) {
                // Only draw walls that overlap the viewport
                if (wall.intersects(vpRect)) {
                    g2d.fillRect(wall.x, wall.y, wall.width, wall.height);
                    g2d.setColor(new Color(80, 80, 100));
                    g2d.drawRect(wall.x, wall.y, wall.width, wall.height);
                    g2d.setColor(new Color(60, 60, 80));
                }
            }
        }

        if (floor != null) {
            for (int i = 200; i < floorWidth; i += 400) {
                for (int j = 200; j < floorHeight; j += 400) {
                    // Skip torches in the new upper corridor to keep it clean
                    if (i > 1240 && j < 1100) continue;
                    // Skip torch overlapping with Med Kit door
                    if (i == 200 && j == 1800) continue;
                    // Only draw torches near the viewport (glow radius ~60px)
                    if (i >= vpX - TORCH_CULL_MARGIN && i <= vpX + vpW + TORCH_CULL_MARGIN
                            && j >= vpY - TORCH_CULL_MARGIN && j <= vpY + vpH + TORCH_CULL_MARGIN) {
                        drawTorch(g2d, i, j);
                    }
                }
            }
        } else {
            drawTorch(g2d, 100, 100);
            drawTorch(g2d, floorWidth / 2, 100);
            drawTorch(g2d, floorWidth - 100, 100);
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
        g.setColor(getAlienBodyColor(isBoss));
        g.fillOval(x, y - 70, 50, 75);
        g.fillOval(x - 5, y - 100, 60, 45);

        g.setColor(getAlienEyeColor(isBoss));
        g.fillOval(x + 8, y - 88, 16, 22);
        g.fillOval(x + 36, y - 88, 16, 22);
    }

    /**
     * Draws a radial glow that fades from {@code glowColor} to fully transparent.
     *
     * @param gradientRadius radius over which the gradient fades out
     * @param fillRadius      radius of the filled circle the glow is painted into
     */
    private void drawGlow(Graphics2D g, int centerX, int centerY, int gradientRadius, int fillRadius, Color glowColor) {
        float[] dist = {0.0f, 1.0f};
        Color transparent = new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0);
        Color[] colors = {glowColor, transparent};
        g.setPaint(new java.awt.RadialGradientPaint(centerX, centerY, gradientRadius, dist, colors));
        g.fillOval(centerX - fillRadius, centerY - fillRadius, fillRadius * 2, fillRadius * 2);
    }

    private void drawPickableItem(Graphics2D g, model.PickableItem pickable) {
        int x = pickable.getX();
        int y = pickable.getY();
        model.ItemType type = pickable.getItem().getItemType();

        if (type == model.ItemType.CONSUMABLE) {
            // Green rectangle for Med Kit
            g.setColor(new Color(40, 200, 40));
            g.fillRect(x, y, 30, 20);
            g.setColor(Color.WHITE);
            g.fillRect(x + 12, y + 4, 6, 12);
            g.fillRect(x + 9, y + 7, 12, 6);
        } else if (type == model.ItemType.SHIELD) {
            // Dark blue cylinder for Shield Cell
            g.setColor(new Color(0, 50, 150));
            g.fillOval(x, y, 20, 10); // top
            g.fillRect(x, y + 5, 20, 20); // body
            g.fillOval(x, y + 20, 20, 10); // bottom
            g.setColor(new Color(0, 150, 255, 150));
            g.fillRect(x + 4, y + 8, 12, 14); // glow
        } else if (type == model.ItemType.KEY) {
            // Glowing Access Key
            long time = System.currentTimeMillis();
            int pulse = (int) (Math.sin(time / 200.0) * 10) + 15;

            drawGlow(g, x + 10, y + 15, 30 + pulse / 2, 30, new Color(255, 215, 0, 100 + pulse));

            // Key Shape (Golden)
            g.setColor(new Color(255, 215, 0));
            g.fillOval(x, y, 15, 15); // head
            g.fillRect(x + 12, y + 5, 15, 4); // shaft
            g.fillRect(x + 22, y + 8, 4, 6);  // bit 1
            g.fillRect(x + 17, y + 8, 4, 4);  // bit 2
        } else if (type == model.ItemType.DIAMOND) {
            // Glowing Energy Diamond
            long time = System.currentTimeMillis();
            int pulse = (int) (Math.sin(time / 150.0) * 15) + 20;

            drawGlow(g, x + 15, y + 15, 40 + pulse / 2, 40, new Color(0, 255, 255, 120 + pulse));

            // Diamond Shape (Cyan)
            g.setColor(new Color(0, 255, 255));
            int[] diamondX = {x + 15, x + 30, x + 15, x};
            int[] diamondY = {y, y + 15, y + 30, y + 15};
            g.fillPolygon(diamondX, diamondY, 4);

            // Sparkle
            g.setColor(Color.WHITE);
            g.fillRect(x + 13, y + 10, 4, 4);
        }
    }

    private void drawDoor(Graphics2D g, model.Door door) {
        int x = door.getX();
        int y = door.getY();
        int w = door.getWidth();
        int h = door.getHeight();
        double progress = door.getOpenProgress();

        // Slide two panels out from the center
        int panelW = w / 2;
        int slideOffset = (int) (panelW * progress);

        g.setColor(new Color(101, 67, 33)); // Dark Brown

        // Left Panel
        g.fillRect(x - slideOffset, y, panelW, h);
        g.setColor(new Color(60, 40, 20));
        g.drawRect(x - slideOffset, y, panelW, h);
        g.drawRect(x - slideOffset + 5, y + 5, panelW - 10, h - 10);

        // Right Panel
        g.setColor(new Color(101, 67, 33));
        g.fillRect(x + panelW + slideOffset, y, panelW, h);
        g.setColor(new Color(60, 40, 20));
        g.drawRect(x + panelW + slideOffset, y, panelW, h);
        g.drawRect(x + panelW + slideOffset + 5, y + 5, panelW - 10, h - 10);

        // Lock (only visible when closed or partially open)
        if (progress < 0.8) {
            int alpha = (int) (255 * (1.0 - progress));
            g.setColor(new Color(255, 255, 0, alpha));
            g.fillOval(x + panelW - 4 - slideOffset, y + (h / 2) - 4, 8, 8);
            g.fillOval(x + panelW - 4 + slideOffset, y + (h / 2) - 4, 8, 8);
        }
    }

    private void drawPrompt(Graphics2D g, String text, int x, int y) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        String[] lines = text.split("\n");
        int maxW = 0;
        for (String line : lines) {
            maxW = Math.max(maxW, g.getFontMetrics().stringWidth(line));
        }
        int lineH = g.getFontMetrics().getHeight();
        int totalH = lineH * lines.length;

        // Draw bubble centered above y
        int bx = x - (maxW / 2) - 10;
        int by = y - totalH - 10;
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(bx, by, maxW + 20, totalH + 15, 10, 10);
        g.setColor(new Color(50, 180, 255));
        g.drawRoundRect(bx, by, maxW + 20, totalH + 15, 10, 10);

        // Draw text lines
        g.setColor(Color.WHITE);
        for (int i = 0; i < lines.length; i++) {
            int lineW = g.getFontMetrics().stringWidth(lines[i]);
            g.drawString(lines[i], x - (lineW / 2), by + lineH * (i + 1));
        }
    }
}
