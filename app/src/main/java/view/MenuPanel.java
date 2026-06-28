package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MenuPanel extends JPanel {
    private static final Color PANEL_COLOR = new Color(20, 20, 25, 200);
    private static final Color PLAY_COLOR = new Color(46, 204, 113);
    private static final Color QUIT_COLOR = new Color(231, 76, 60);
    private final Random random = new Random(42); // Fixed seed for consistent stars

    public MenuPanel(Runnable playAction, Runnable quitAction) {
        super(new GridBagLayout());
        setBackground(new Color(10, 10, 15));

        JPanel menuCard = buildMenuCard(playAction, quitAction);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(menuCard, constraints);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Draw Space Gradient
        GradientPaint skyGradient = new GradientPaint(0, 0, new Color(5, 5, 10), 0, h, new Color(40, 20, 40));
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, w, h);

        // 2. Draw Stars
        g2d.setColor(Color.WHITE);
        random.setSeed(42); 
        for (int i = 0; i < 200; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            int size = random.nextInt(3);
            g2d.fillOval(x, y, size, size);
        }

        // 3. Draw Mars Horizon (Distant Hills)
        g2d.setColor(new Color(60, 20, 20));
        int[] xHills1 = {0, w/4, w/2, 3*w/4, w, w, 0};
        int[] yHills1 = {h-120, h-170, h-140, h-200, h-150, h, h};
        g2d.fillPolygon(xHills1, yHills1, 7);

        // 4. Draw Alien Fortress (Background Right)
        drawFortress(g2d, w - 600, h - 450);

        // 5. Draw Nearer Hills (Gradient)
        GradientPaint marsGradient = new GradientPaint(0, h-100, new Color(120, 40, 30), 0, h, new Color(60, 20, 15));
        g2d.setPaint(marsGradient);
        int[] xHills2 = {0, w/5, 2*w/5, 3*w/5, 4*w/5, w, w, 0};
        int[] yHills2 = {h-70, h-100, h-80, h-110, h-90, h-120, h, h};
        g2d.fillPolygon(xHills2, yHills2, 8);

        // 6. Draw Astronaut (Far Left, facing right)
        drawAstronaut(g2d, 100, h - 250);

        // 7. Draw Aliens (In front of the Fortress on the right)
        drawAliens(g2d, w - 450, h - 220);

        // 8. Draw a "Glow" on the horizon
        g2d.setPaint(new GradientPaint(0, h-150, new Color(200, 100, 50, 0), 0, h-50, new Color(200, 100, 50, 50)));
        g2d.fillRect(0, h-150, w, 100);
    }

    private void drawAstronaut(Graphics2D g, int x, int y) {
        // Body (White Suit)
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, 60, 90, 20, 20);
        
        // Helmet
        g.fillOval(x + 5, y - 40, 50, 50);
        
        // Visor (Cyan/Blue)
        g.setColor(new Color(100, 220, 255));
        g.fillRoundRect(x + 12, y - 32, 40, 25, 15, 15);
        
        // Details (Backpack)
        g.setColor(new Color(180, 180, 180));
        g.fillRect(x - 15, y + 10, 15, 60);
        
        // Limbs (Grounded)
        g.setColor(Color.WHITE);
        g.fillRect(x - 8, y + 20, 15, 45); // Left arm
        g.fillRect(x + 55, y + 20, 25, 15); // Right arm (pointing)
        
        // Legs (Touching the ground)
        g.fillRect(x + 8, y + 85, 18, 40); // Left leg
        g.fillRect(x + 35, y + 85, 18, 40); // Right leg
        
        // Boots
        g.setColor(new Color(50, 50, 50));
        g.fillRect(x + 5, y + 120, 25, 10);
        g.fillRect(x + 32, y + 120, 25, 10);
    }

    private void drawAliens(Graphics2D g, int x, int y) {
        // Main Alien (Scaled up)
        drawSingleAlien(g, x, y, 50, 70);
        
        // Smaller Aliens (Group)
        drawSingleAlien(g, x + 100, y + 40, 35, 50);
        drawSingleAlien(g, x - 80, y + 30, 35, 50);
    }

    private void drawSingleAlien(Graphics2D g, int x, int y, int bodyW, int bodyH) {
        g.setColor(new Color(50, 200, 50));
        g.fillOval(x, y, bodyW, bodyH); // Body
        
        int headW = (int)(bodyW * 1.3);
        int headH = (int)(bodyH * 0.6);
        g.fillOval(x - (headW-bodyW)/2, y - headH + 10, headW, headH); // Head
        
        // Eyes (Black/Large)
        g.setColor(Color.BLACK);
        int eyeSize = headW / 4;
        g.fillOval(x + 2, y - headH + 18, eyeSize, (int)(eyeSize * 1.2));
        g.fillOval(x + headW - eyeSize - 12, y - headH + 18, eyeSize, (int)(eyeSize * 1.2));
    }

    private void drawFortress(Graphics2D g, int x, int y) {
        // 1. Dark metallic/stone structure base
        g.setColor(new Color(35, 35, 45));
        int[] xBase = {x, x + 500, x + 400, x + 100};
        int[] yBase = {y + 350, y + 350, y, y};
        g.fillPolygon(xBase, yBase, 4);
        
        // 2. Spikes/Antennas (Draw behind everything else)
        g.setColor(new Color(25, 25, 35));
        int[] xSpike = {x + 230, x + 270, x + 250};
        int[] ySpike = {y, y, y - 250};
        g.fillPolygon(xSpike, ySpike, 3);

        // 3. Towers
        g.setColor(new Color(35, 35, 45));
        g.fillRect(x + 60, y - 120, 70, 150);
        g.fillRect(x + 370, y - 120, 70, 150);
        
        // 4. Balcony Floor
        g.setColor(new Color(25, 25, 35));
        g.fillRect(x + 120, y + 60, 260, 12); 
        
        // 5. Aliens on Balcony (Drawn before railing to hide feet)
        drawSingleAlien(g, x + 160, y + 30, 20, 30);
        drawSingleAlien(g, x + 240, y + 25, 22, 32);
        drawSingleAlien(g, x + 320, y + 30, 20, 30);

        // 6. Balcony Railing (Drawn OVER the aliens' bottom part)
        g.setColor(new Color(45, 45, 55));
        g.fillRect(x + 120, y + 48, 260, 14); 
        
        // 7. Dark Blue Glow Windows
        g.setColor(new Color(0, 50, 150, 180));
        g.fillRect(x + 85, y - 90, 20, 20);
        g.fillRect(x + 395, y - 90, 20, 20);
        g.fillRect(x + 150, y + 150, 200, 15);
    }

    private JPanel buildMenuCard(Runnable playAction, Runnable quitAction) {
        JPanel menuCard = new JPanel(new BorderLayout(0, 30));
        menuCard.setOpaque(true);
        menuCard.setBackground(PANEL_COLOR);
        menuCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 120), 2),
                BorderFactory.createEmptyBorder(40, 60, 40, 60)));

        JLabel title = new JLabel("MARS ESCAPE", SwingConstants.CENTER);
        title.setForeground(new Color(255, 120, 80));
        title.setFont(new Font("Orbitron", Font.BOLD, 52));
        if (title.getFont().getName().equals("Dialog")) {
            title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 52));
        }

        JLabel subtitle = new JLabel("Collect Fuel Gems • Survive the Aliens", SwingConstants.CENTER);
        subtitle.setForeground(new Color(180, 180, 200));
        subtitle.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 18));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 10));
        titlePanel.setOpaque(false);
        titlePanel.add(title, BorderLayout.CENTER);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);

        JButton playButton = createMenuButton("LAUNCH MISSION", PLAY_COLOR);
        playButton.addActionListener(event -> playAction.run());

        JButton quitButton = createMenuButton("ABORT", QUIT_COLOR);
        quitButton.addActionListener(event -> quitAction.run());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 15, 0);
        buttonPanel.add(playButton, constraints);

        constraints.gridy = 1;
        constraints.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(quitButton, constraints);

        menuCard.add(titlePanel, BorderLayout.NORTH);
        menuCard.add(buttonPanel, BorderLayout.CENTER);
        return menuCard;
    }

    private JButton createMenuButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(300, 60));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        
        return button;
    }
}
