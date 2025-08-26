import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class MapViewer extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {
    private static final int TILE_SIZE = 256;
    private final Map<Point, BufferedImage> images = new HashMap<>();
    private double scale = 1.0;
    private double offsetX = 0, offsetY = 0;
    private int lastMouseX, lastMouseY;

    public MapViewer(String folderPath) {
        loadImages(folderPath);
        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    private void loadImages(String folderPath) {
        File dir = new File(folderPath);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));

        if (files == null) return;

        int minCol = Integer.MAX_VALUE;
        int minRow = Integer.MAX_VALUE;

        //минимальные индексы координат
        for (File file : files) {
            String name = file.getName().replace(".png", "");
            String[] coords = name.split("_");
            if (coords.length != 2) continue;
            try {
                int col = Integer.parseInt(coords[0]);
                int row = Integer.parseInt(coords[1]);
                if (col < minCol) minCol = col;
                if (row < minRow) minRow = row;
            } catch (NumberFormatException ignored) {}
        }

        // загрузка изображения со смещением
        for (File file : files) {
            String name = file.getName().replace(".png", "");
            String[] coords = name.split("_");
            if (coords.length != 2) continue;
            try {
                int col = Integer.parseInt(coords[0]);
                int row = Integer.parseInt(coords[1]);

                int x = (col - (minCol+125)) * TILE_SIZE;    // изменение точки появления 
                int y = (row - (minRow+44)) * TILE_SIZE;

                BufferedImage img = ImageIO.read(file);
                images.put(new Point(x, y), img);
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        for (Map.Entry<Point, BufferedImage> entry : images.entrySet()) {
            Point p = entry.getKey();
            BufferedImage img = entry.getValue();
            g2.drawImage(img, p.x, p.y, null);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double zoomFactor = 1.05;                                     // шаг зума
        double mouseX = (e.getX() - offsetX) / scale;
        double mouseY = (e.getY() - offsetY) / scale;

        if (e.getPreciseWheelRotation() < 0) {
            scale *= zoomFactor;
        } else {
            scale /= zoomFactor;
        }

        // корректировка зума (зум от курсора)
        offsetX = e.getX() - mouseX * scale;
        offsetY = e.getY() - mouseY * scale;

        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - lastMouseX;
        int dy = e.getY() - lastMouseY;
        offsetX += dx;
        offsetY += dy;
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Map Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 900);              // изменение разрешения начального окна

            MapViewer viewer = new MapViewer("data"); // изменение названия папки
            frame.add(new JScrollPane(viewer));
            frame.setVisible(true);
        });
    }
}
