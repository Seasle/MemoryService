package org.seasle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class Diagram {
    private List<Double> points = null;
    private int size = 0;
    private int offset = 0;
    private int tempOffset = 0;

    public JPanel canvas = new JPanel() {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            int width = this.getWidth();
            int height = this.getHeight();

            int additional = 4;
            int count = points.size() + additional;
            double step = Math.max(5, (double) width / (count - additional - 1));
            size = (int) ((count - additional) * step);
            offset = Utils.clamp(-size + canvas.getWidth(), 0, offset);

            // Set antialiasing
            Graphics2D graphics2D = (Graphics2D) graphics;
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Clear canvas
            graphics2D.setColor(new Color(255, 255, 255));
            graphics2D.fillRect(0, 0, width, height);

            // Draw diagram
            int[] xPoints = new int[count];
            int[] yPoints = new int[count];

            xPoints[0] = 0;
            yPoints[0] = height;
            xPoints[1] = 0;
            yPoints[1] = (int) (height - points.get(0) * height);
            for (int index = 0; index < count - additional; index++) {
                xPoints[index + 2] = (int) (index * step) + offset;
                yPoints[index + 2] = (int) (height - points.get(index) * height);
            }
            xPoints[count - 2] = width;
            yPoints[count - 2] = (int) (height - points.get(points.size() - 1) * height);
            xPoints[count - 1] = width;
            yPoints[count - 1] = height;

            graphics2D.setColor(new Color(53, 216, 222));
            graphics2D.fillPolygon(xPoints, yPoints, count);

            // Draw grid
            int columns = (int) Math.ceil(width / 10.0);
            int rows = (int) Math.ceil(height / 10.0);
            double percent = Math.min(1, (double) width / size);
            double scrollPosition = Math.round(-offset * percent);

            graphics2D.setColor(new Color(0, 0, 0, 35));
            for (int column = 1; column <= columns; column++) {
                int position = column * 10;
                int offset = (int) (scrollPosition / percent) % 10;

                graphics2D.drawLine(
                    position - offset,
                    0,
                    position - offset,
                    height
                );
            }

            for (int row = 1; row <= rows; row++) {
                graphics2D.drawLine(0, row * 10, width, row * 10);
            }

            // Draw scroll
            if (percent < 1) {
                graphics2D.setColor(new Color(0, 0, 0, 100));
                graphics2D.fillRect((int) scrollPosition, height - 4, (int) Math.round(width * percent), 4);
            }
        }
    };

    public Diagram() {
        MouseTracker mouseTracker = new MouseTracker(canvas);

        canvas.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                super.mousePressed(event);

                tempOffset = offset;
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                super.mouseReleased(event);

                tempOffset = 0;
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                super.mouseDragged(event);

                offset = Utils.clamp(-size + canvas.getWidth(), 0, tempOffset + event.getX() - mouseTracker.getPressed().x);
                canvas.repaint();
            }
        });
    }

    public void draw(List<Double> points) {
        this.points = points;

        canvas.repaint();
    }
}