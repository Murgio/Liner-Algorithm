package ch.muriz.liner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * Liner Algorithm
 * Finde die dunkelste Linie zwischen zwei beliebigen Pins.
 * Füge diese Linie dem neuen Bild hinzu.
 * Entferne diese Linie vom input Bild.
 * Wiederhole beliebig oft.
 * @author  Muriz Serifovic
 * @version 1.0
 * @since   2016-09-04
 */

public class Liner {

    final String imageName = "face.png";
    final int numberOfPins = 200;
    final int totalLines = 6000;
    int lineAlpha = 45;
    final int ignoreNeighbors = 10;
    boolean paused = false;

    final int numberLines = numberOfPins * numberOfPins / 2;
    float[] intensities = new float[numberOfPins];
    double[] px = new double[numberOfPins];
    double[] py = new double[numberOfPins];
    double[] lengths = new double[numberOfPins];
    BufferedImage img;
    BufferedImage resultImage;

    int linesDrawn = 0;
    int currentPin = 0;

    JFrame frame;
    JProgressBar progressBar;

    Graphics2D g;

    public Liner() {
        frame = new JFrame();
        init();
        openWindow(img.getWidth() * 2 + 20, img.getHeight() + 100);
        frame.add(new JLabel(new ImageIcon(img)), BorderLayout.EAST);

        resultImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        g = resultImage.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, resultImage.getWidth(), resultImage.getHeight());

        frame.add(new JLabel(new ImageIcon(resultImage)), BorderLayout.WEST);

        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Wechsle zwichen paused und unpaused.
                paused = paused ? false : true;
                System.out.println("Paused: " + paused);
            }
        });
        // Initialize Progress Bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        progressBar.setMinimum(0);
        progressBar.setMaximum(totalLines);
        frame.add(progressBar, BorderLayout.SOUTH);
        frame.setVisible(true);
        updatePicture();
    }

    public static void main(String[] args) {
        Liner liner = new Liner();
    }

    // Screensize: (image.width*2, image.height)
    public void openWindow(int width, int height) {
        frame.setLayout(new BorderLayout());
        frame.setSize(width, height);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((dim.width / 2) - (frame.getSize().width / 2),
                (dim.height / 2) - (frame.getSize().height / 2));
        frame.setTitle("Liner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void init() {
        try {
            img = ImageIO.read(new File(imageName));
        } catch (IOException e) {
            // Handle exception...
            e.printStackTrace();
        }

        BufferedImage image = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g2 = image.getGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        img = image;

        // Berechne den Kreis und die Punkte am Rande.
        double rad;
        if (img.getWidth() > img.getHeight()) rad = img.getHeight()/2;
        else rad = img.getWidth()/2;

        for (int i = 0; i < numberOfPins; ++i) {
            double d = Math.PI * 2.0 * (double)i/(double)numberOfPins;
            px[i] = img.getWidth()/2 + Math.sin(d) * rad;
            py[i] = img.getHeight()/2 + Math.cos(d) * rad;
        }

        // Lookup table.
        for(int i = 0; i < numberOfPins; ++i) {
            double dx = px[i] - px[0];
            double dy = py[i] - py[0];
            lengths[i] = Math.floor(Math.sqrt(dx*dx+dy*dy));
        }
    }

    private void updatePicture() {
        long startTime = System.currentTimeMillis();
        while(linesDrawn < totalLines) {
            if(!paused) {
                calculateLine();
                updateBar(linesDrawn); // Loading bar
                linesDrawn++;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time: " + ((endTime-startTime)/1000f) + " seconds");
        g.dispose();
    }

    public void updateBar(int newValue) {
        progressBar.setValue(newValue);
    }

    // Let's do the math ¯\_(ツ)_/¯
    void calculateLine() {
        double limit = 1000000;
        int bestFirstPin = 0;
        int bestSecondPin = 0;
        int i = currentPin;

        //for(int i = 0; i < numberOfPins; ++i) // Gehe alle Linien durch. Too slow
        {
            for(int j = 1 + ignoreNeighbors; j < numberOfPins - ignoreNeighbors; ++j) {
                int nextPin = (i + j) % numberOfPins;
                if(nextPin == i) continue;
                double dx = px[nextPin] - px[i];
                double dy = py[nextPin] - py[i];
                double len = lengths[j]; // => Lookup table

                // Wie dunkel ist die Linie unter dem Bild:
                double intensity = 0;
                for(int k = 0; k < len; ++k) {
                    double s = (double)k/len;
                    double fx = px[i] + dx * s;
                    double fy = py[i] + dy * s;
                    intensity += img.getRGB((int)fx, (int)fy);
                }
                double currIntensity = intensity / len;
                if(limit > currIntensity) {
                    limit = currIntensity;
                    bestFirstPin = i;
                    bestSecondPin = nextPin;
                }
            }
        }

        //System.out.println("From " + bestFirstPin + " to " + bestSecondPin);
        // maxIndex: Dunkelste Linie auf dem Bild.
        // Entferne Linie vom source Bild.
        currentPin = bestFirstPin;
        int nextPin = bestSecondPin;
        double dx = px[nextPin] - px[currentPin];
        double dy = py[nextPin] - py[currentPin];
        double len = Math.floor(Math.sqrt(dx*dx+dy*dy));
        for(int k = 0; k < len; ++k) {
            double s = (double)k/len;
            double fx = px[currentPin] + dx * s;
            double fy = py[currentPin] + dy * s;
            int color = img.getRGB((int)fx, (int)fy);
            float red = color >> 16 & 0xFF;
            if(red < 255 - lineAlpha) red += lineAlpha;
            else red = 255;
            img.setRGB((int)fx, (int)fy, calcColor(red));
        }

        // Zeichne die dunkelste Linie
        g.setColor(new Color(0f,0f,0f, lineAlpha/255.0f));
        g.drawLine((int)px[currentPin],(int)py[currentPin],(int)px[nextPin],(int)py[nextPin]);
        frame.repaint();

        // Neuer Punkt ist der letzte Punkt.
        currentPin = nextPin;
    }

    public int calcColor(float col) {
        int gray = (int) col;
        if (gray > 255) gray = 255;
        else if (gray < 0) gray = 0;
        return 0xff000000 | (gray << 16) | (gray << 8) | gray;
    }
}