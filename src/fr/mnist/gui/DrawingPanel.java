package fr.mnist.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.JPanel;

public class DrawingPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final int rows, cols, cellSize;
    private final int[][] grid;
    private boolean drawing = false;

    public DrawingPanel(int rows, int cols, int cellSize) {
        this.rows = rows;
        this.cols = cols;
        this.cellSize = cellSize;
        grid = new int[rows][cols];
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        setBackground(Color.WHITE);
        MouseAdapter adapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) { drawing = true; draw(e); }
            public void mouseDragged(MouseEvent e) { draw(e); }
            public void mouseReleased(MouseEvent e) { drawing = false; }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    private void draw(MouseEvent e) {
        if (!drawing) return;
        int x = e.getX() / cellSize;
        int y = e.getY() / cellSize;
        if (x >= 0 && x < cols && y >= 0 && y < rows) {
            grid[y][x] = 255;
            repaint();
        }
    }

    public void clear() {
        for (int i = 0; i < rows; i++) Arrays.fill(grid[i], 0);
        repaint();
    }

    public double[] getPixelValues() {
        double[] pixels = new double[rows * cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                pixels[i * cols + j] = grid[i][j] / 255.0;
        return pixels;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int intensity = grid[i][j];
                g.setColor(new Color(intensity, intensity, intensity));
                g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(j * cellSize, i * cellSize, cellSize, cellSize);
            }
        }
    }
}
