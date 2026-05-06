package fr.mnist.gui;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;

public class MnistDrawingGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private DrawingPanel drawingPanel;
    private JLabel resultLabel;
    private Classifier classifier;
    private Instances dataHeader;

    public MnistDrawingGUI() throws Exception {
        initUI();
        loadModel();
    }

    private void initUI() {
        setTitle("Reconnaissance MNIST - Dessin à la main");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel(28, 28, 10);
        add(drawingPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton predictBtn = new JButton("Prédire");
        JButton clearBtn = new JButton("Effacer");
        resultLabel = new JLabel("Prédiction : --", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        bottomPanel.add(predictBtn);
        bottomPanel.add(clearBtn);
        add(bottomPanel, BorderLayout.SOUTH);
        add(resultLabel, BorderLayout.NORTH);

        predictBtn.addActionListener(e -> predict());
        clearBtn.addActionListener(e -> drawingPanel.clear());
    }

    private void loadModel() throws Exception {
        File modelFile = new File("mnist_rf.model");
        if (!modelFile.exists())
            throw new Exception("Modèle mnist_rf.model introuvable. Lancez d'abord WekaClassifier.");
        classifier = (Classifier) SerializationHelper.read(modelFile.getAbsolutePath());
        DataSource ds = new DataSource("mnist_train.arff");
        dataHeader = ds.getDataSet();
        dataHeader.setClassIndex(dataHeader.numAttributes() - 1);
        System.out.println("Modèle RandomForest chargé.");
    }

    // Recentrage et redimensionnement du dessin pour qu'il remplisse une zone 20x20 centrée
    private double[] preprocess(double[] pixels, int rows, int cols) {
        // 1. Inverser les couleurs : trait noir (valeur 1) -> trait blanc (valeur 1)
        //    mais MNIST attend blanc sur fond noir : notre trait doit devenir 1, fond 0.
        //    Comme grid est déjà 1 pour trait noir, 0 pour fond blanc, on ne change rien ici.
        //    En réalité, il faut que le trait ait une valeur forte (proche de 1) et fond 0.
        //    C'est déjà le cas. Pas d'inversion supplémentaire.
        
        // 2. Épaissir le trait (pour ressembler aux images MNIST)
        double[] thick = pixels.clone();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (pixels[y * cols + x] > 0.5) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            int ny = y + dy;
                            int nx = x + dx;
                            if (ny >= 0 && ny < rows && nx >= 0 && nx < cols) {
                                thick[ny * cols + nx] = 1.0;
                            }
                        }
                    }
                }
            }
        }
        
        // 3. Trouver les limites du chiffre
        int minX = cols, maxX = -1, minY = rows, maxY = -1;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (thick[y * cols + x] > 0.5) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (minX > maxX) return thick; // aucun trait
        
        int w = maxX - minX + 1;
        int h = maxY - minY + 1;
        // Taille cible 20x20 (comme dans MNIST)
        int target = 20;
        double scale = Math.min((double) target / w, (double) target / h);
        int newW = (int) (w * scale);
        int newH = (int) (h * scale);
        int startX = (cols - newW) / 2;
        int startY = (rows - newH) / 2;
        
        double[] centered = new double[rows * cols];
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (thick[y * cols + x] > 0.5) {
                    int nx = startX + (int) ((x - minX) * scale);
                    int ny = startY + (int) ((y - minY) * scale);
                    if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                        centered[ny * cols + nx] = 1.0;
                    }
                }
            }
        }
        return centered;
    }

    private void predict() {
        if (classifier == null || dataHeader == null) return;
        try {
            double[] raw = drawingPanel.getPixelValues(); // 0..1, 1 = trait noir
            // Prétraitement : recentrage + épaississement
            double[] processed = preprocess(raw, 28, 28);
            // Création instance Weka (valeur 0-255)
            DenseInstance instance = new DenseInstance(784);
            for (int i = 0; i < 784; i++) {
                instance.setValue(i, processed[i] * 255.0);
            }
            instance.setDataset(dataHeader);
            
            double pred = classifier.classifyInstance(instance);
            int digit = (int) pred;
            double[] dist = classifier.distributionForInstance(instance);
            double confidence = dist[digit] * 100;
            resultLabel.setText(String.format("Prédiction : %d  (confiance : %.1f%%)", digit, confidence));
        } catch (Exception e) {
            resultLabel.setText("Prédiction : erreur");
            e.printStackTrace();
        }
    }

    // ================== Panneau de dessin ==================
    class DrawingPanel extends JPanel {
        /**
		 * 
		 */
	
		/**
		 * 
		 */
		private static final long serialVersionUID = -1409795457934675135L;
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

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            try { new MnistDrawingGUI().setVisible(true); }
            catch (Exception e) { e.printStackTrace(); }
        });
    }
}