package Tetris.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Random;

import Tetris.mino.*;

public class PlayManager {
    // Main play area
    final int WIDTH = 360;
    final int HEIGHT = 600;
    public static int left_x;
    public static int right_x;
    public static int top_y;
    public static int bottom_y;

    // Assistance features
    private boolean showGrid = false;
    private boolean showLandingPreview = false;
    private final Color GRID_COLOR = new Color(255, 255, 255, 30);
    private final Color PREVIEW_COLOR = new Color(255, 255, 255, 50);

    // Tetriminos
    Mino currentMino;
    final int MINO_START_X;
    final int MINO_START_Y;
    Mino nextMino;
    final int NEXTMINO_X;
    final int NEXTMINO_Y;
    public static ArrayList<Block> staticBlocks = new ArrayList<>();
    public static int dropInterval = 60;

    // Animation and effects
    private boolean isAnimating = false;
    private int animationFrames = 10;
    private int animationCounter = 0;
    private ArrayList<Block> blocksToMove = new ArrayList<>();
    private int moveDownAmount = 0;
    private boolean isFlashing = false;
    private int flashFrames = 20;
    private int flashCounter = 0;
    private ArrayList<Integer> rowsToDelete = new ArrayList<>();
    private Color bgColor1 = new Color(2, 2, 23);
    private Color bgColor2 = new Color(22, 22, 129);
    private Font gameFont, titleFont;
    private ArrayList<Particle> particles = new ArrayList<>();

    // Game state
    boolean gameOver;
    int level = 1;
    int lines;
    int score;

    private final Color[] minoColors = {
        Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW,
        Color.MAGENTA, Color.GREEN, Color.RED,
    };

    private ToggleButton gridButton;
    private ToggleButton previewButton;

    public PlayManager() {
        left_x = (GamePanel.WIDTH/2) - (WIDTH/2);
        right_x = left_x + WIDTH;
        top_y = 50;
        bottom_y = top_y + HEIGHT;

        MINO_START_X = left_x + (WIDTH/2) - Block.SIZE;
        MINO_START_Y = top_y + Block.SIZE;

        NEXTMINO_X = right_x + 175;
        NEXTMINO_Y = top_y + 505;

        currentMino = pickMino();
        currentMino.setXY(MINO_START_X, MINO_START_Y);

        nextMino = pickMino();
        nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);

        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, 
                getClass().getResourceAsStream("/Tetris/resources/fonts/PixeloidSans.ttf")).deriveFont(30f);
            titleFont = Font.createFont(Font.TRUETYPE_FONT, 
                getClass().getResourceAsStream("/Tetris/resources/fonts/PixeloidSans-Bold.ttf")).deriveFont(30f);
        }
        catch(Exception e) {
            gameFont = new Font("Arial", Font.PLAIN, 30);
        }

        gridButton = new ToggleButton(35, 80, 200, 40, "Grid", gameFont.deriveFont(20f));
        previewButton = new ToggleButton(35, 150, 200, 40, "Landing Preview", gameFont.deriveFont(20f));
    }

    // Toggle methods for assistance features
    public void toggleGrid() {
        showGrid = !showGrid;
    }
    
    public void toggleLandingPreview() {
        showLandingPreview = !showLandingPreview;
    }

    private void drawGrid(Graphics2D g2) {
        if (!showGrid) return;
        
        g2.setColor(GRID_COLOR);
        
        // Draw vertical lines
        for (int x = left_x; x <= right_x; x += Block.SIZE) {
            g2.drawLine(x, top_y, x, bottom_y);
        }
        
        // Draw horizontal lines
        for (int y = top_y; y <= bottom_y; y += Block.SIZE) {
            g2.drawLine(left_x, y, right_x, y);
        }
    }

    private void drawLandingPreview(Graphics2D g2) {
        if (!showLandingPreview || currentMino == null || !currentMino.active) return;
        
        // Create temporary blocks for preview
        Block[] previewBlocks = new Block[4];
        for (int i = 0; i < 4; i++) {
            previewBlocks[i] = new Block(PREVIEW_COLOR);
            previewBlocks[i].x = currentMino.b[i].x;
            previewBlocks[i].y = currentMino.b[i].y;
        }
        
        // Move preview blocks down until collision
        boolean collision;
        do {
            collision = false;
            
            // Check for bottom boundary
            for (Block block : previewBlocks) {
                if (block.y + Block.SIZE >= bottom_y) {
                    collision = true;
                    break;
                }
            }
            
            // Check for static block collision
            if (!collision) {
                for (Block preview : previewBlocks) {
                    for (Block staticBlock : staticBlocks) {
                        if (preview.x == staticBlock.x && 
                            preview.y + Block.SIZE == staticBlock.y) {
                            collision = true;
                            break;
                        }
                    }
                    if (collision) break;
                }
            }
            
            // Move preview down if no collision
            if (!collision) {
                for (Block block : previewBlocks) {
                    block.y += Block.SIZE;
                }
            }
        } while (!collision);
        
        // Draw preview blocks
        g2.setColor(PREVIEW_COLOR);
        for (Block block : previewBlocks) {
            g2.fillRect(block.x + 2, block.y + 2, 
                       Block.SIZE - 4, Block.SIZE - 4);
        }
    }

    private Mino pickMino() {
        Mino mino = null;
        int i = new Random().nextInt(7);

        switch(i) {
            case 0: mino = new Mino_L1(); break;
            case 1: mino = new Mino_L2(); break;
            case 2: mino = new Mino_Square(); break;
            case 3: mino = new Mino_Bar(); break;
            case 4: mino = new Mino_T(); break;
            case 5: mino = new Mino_Z1(); break;
            case 6: mino = new Mino_Z2(); break;
        }
        return mino;
    }

    public void update() {
        if (isFlashing) {
            flashAndDelete();
        } else if (isAnimating) {
            animatedBlockMovement();
        } else {
            if (currentMino.active == false) {
                staticBlocks.add(currentMino.b[0]);
                staticBlocks.add(currentMino.b[1]);
                staticBlocks.add(currentMino.b[2]);
                staticBlocks.add(currentMino.b[3]);

                // Check if game is over
                if(currentMino.b[0].x == MINO_START_X && currentMino.b[0].y == MINO_START_Y){
                    gameOver = true;
                    GamePanel.bgMusic.stop();
                    GamePanel.soundEffect.playSound(1, false);
                }

                currentMino.deactivating = false;

                currentMino = nextMino;
                currentMino.setXY(MINO_START_X, MINO_START_Y);

                nextMino = pickMino();
                nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);

                checkDelete();
            } else {
                currentMino.update();
            }
        }
    }

    private void checkDelete() {
        int x = left_x;
        int y = top_y;
        int blockCount = 0;
        rowsToDelete.clear();

        while (x < right_x && y < bottom_y) {
            for (int i = 0; i < staticBlocks.size(); i++) {
                if (staticBlocks.get(i).x == x && staticBlocks.get(i).y == y) {
                    blockCount++;
                }
            }

            x += Block.SIZE;

            if (x == right_x) {
                if (blockCount == 12) {
                    rowsToDelete.add(y);
                }
                blockCount = 0;
                x = left_x;
                y += Block.SIZE;
            }
        }

        if (!rowsToDelete.isEmpty()) {
            isFlashing = true;
            flashCounter = 0;
        }
    }

    private void flashAndDelete() {
        int lineCount = 0;

        flashCounter++;

        if (flashCounter <= flashFrames) {
            // Flash the blocks
            for (int row : rowsToDelete) {
                for (Block block : staticBlocks) {
                    if (block.y == row) {
                        block.c = (flashCounter % 2 == 0) ? Color.white : block.c;
                    }
                }
            }
        } else {
            // Delete the rows and prepare for animation
            for (int row : rowsToDelete) {
                for (int i = staticBlocks.size() - 1; i >= 0; i--) {
                    if (staticBlocks.get(i).y == row) {
                        Block block = staticBlocks.remove(i);
                        addParticles(block.x, block.y);
                    }
                }

                lineCount++;
                lines++;

                if(lines % 10 == 0 && dropInterval > 1){
                    level++;

                    if(dropInterval > 10){
                        dropInterval -= 10;
                    }
                    else{
                        dropInterval -= 1;
                    }
                }
            }

            for (Block block : staticBlocks) {
                if (block.y < rowsToDelete.get(0)) {
                    blocksToMove.add(block);
                }
            }

            isFlashing = false;
            isAnimating = true;
            animationCounter = 0;
            moveDownAmount = Block.SIZE * rowsToDelete.size();
        }

        if(lineCount > 0){
            GamePanel.soundEffect.playSound(0, false);
            int singleLineScore = 10 * level;
            score += singleLineScore * lineCount;
        }
    }

    private void animatedBlockMovement() {
        animationCounter++;

        for (Block block : blocksToMove) {
            block.y += moveDownAmount / animationFrames;
        }

        if (animationCounter >= animationFrames) {
            for (Block block : blocksToMove) {
                block.y += moveDownAmount % animationFrames;
            }

            isAnimating = false;
            blocksToMove.clear();
            moveDownAmount = 0;
        }
    }

    public void handleMouseClick(int x, int y) {
        if (gridButton.contains(x, y)) {
            gridButton.toggle();
            showGrid = gridButton.isToggled();
        }
        if (previewButton.contains(x, y)) {
            previewButton.toggle();
            showLandingPreview = previewButton.isToggled();
        }
    }

    public void draw(Graphics2D g2) {
        // Draw main play area background
        GradientPaint gradient = new GradientPaint(left_x, top_y, bgColor1, 
                                                 right_x, bottom_y, bgColor2);
        g2.setPaint(gradient);
        g2.fillRect(left_x, top_y, WIDTH, HEIGHT);

        // Draw assistance features
        drawGrid(g2);
        drawLandingPreview(g2);

        // Draw border
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(left_x - 4, top_y - 4, WIDTH + 8, HEIGHT + 8);

        // Draw next frame
        int x = right_x + 100;
        int y = bottom_y - 200;
        g2.drawRect(x, y, 200, 200);
        g2.setFont(gameFont.deriveFont(40f));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString("NEXT", x + 50, y + 60);

        // Draw score frame
        g2.setFont(gameFont.deriveFont(25f));
        g2.drawRect(x, top_y, 200, 350);
        x += 20;
        y = top_y + 90;
        g2.drawString("LEVEL", x, y);
        g2.drawString("   :  " + level, x + 80, y);
        y += 90;
        g2.drawString("LINES", x, y);
        g2.drawString("   :  " + lines, x + 80, y);
        y += 90;
        g2.drawString("SCORE", x, y);
        g2.drawString("   :  " + score, x + 80, y);

        // Draw toggle buttons
        gridButton.draw(g2);
        previewButton.draw(g2);

        // Draw mino
        if (currentMino != null) {
            currentMino.draw(g2);
        }

        // Draw next Mino
        nextMino.draw(g2);

        // Draw Static blocks
        for (int i = 0; i < staticBlocks.size(); i++) {
            staticBlocks.get(i).draw(g2);
        }

        // Draw particles
        for(int i = particles.size() - 1; i >= 0; i--){
            Particle p = particles.get(i);

            p.update();
            p.draw(g2);

            if(p.alpha <= 0){
                particles.remove(i);
            }
        }

        g2.setFont(gameFont.deriveFont(70f));
        if(gameOver){
            x = left_x - 25;
            y = top_y + 320;
            g2.setColor(Color.cyan);
            g2.drawString("GAME OVER", x + 3, y + 3);
            g2.setColor(Color.white);
            g2.drawString("GAME OVER", x, y);
        }
        if (KeyHandler.pausePressed) {
            x = left_x + 45;
            y = top_y + 320;
            g2.setColor(Color.cyan);
            g2.drawString("PAUSED", x + 3, y + 3);
            g2.setColor(Color.white);
            g2.drawString("PAUSED", x, y);
        }

        x = 35;
        y = top_y + 320;
        g2.setFont(titleFont.deriveFont(80f));
        g2.setColor(Color.cyan);
        g2.drawString("TETRIS", x + 3, y + 5);
        g2.setColor(Color.white);
        g2.drawString("TETRIS", x, y);
    }

    private void addParticles(int x, int y){
        for(int i = 0; i < 20; i++){
            particles.add(new Particle(x, y));
        }
    }

    private class Particle{
        int x, y;
        int speedX, speedY;
        int size;
        int alpha = 255;
        Color color;

        public Particle(int x, int y){
            this.x = x;
            this.y = y;
            this.speedX = new Random().nextInt(5) - 2;
            this.speedY = new Random().nextInt(5) - 2;
            this.size = new Random().nextInt(10) + 5;
            this.color = minoColors[new Random().nextInt(minoColors.length)];
        }

        public void update(){
            x += speedX;
            y += speedY;
            alpha -= 5;

            if(alpha < 0){
                alpha = 0;
            }
        }

        public void draw(Graphics2D g2){
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2.fillOval(x, y, size, size);
        }
    }
}