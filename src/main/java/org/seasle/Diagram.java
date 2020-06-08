package org.seasle;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

class DiagramRange {
    int from;
    int to;
    int index;

    public DiagramRange(int from, int to, int index) {
        this.from = from;
        this.to = to;
        this.index = index;
    }
}

public class Diagram {
    // region Variables
    private List<Object> labels = new ArrayList<>();
    private List<Object> points = new ArrayList<>();
    private final List<Object> keys = new ArrayList<>();
    private final List<DiagramRange> ranges = new ArrayList<>();
    private final HashMap<Object, List<Object>> groups = new HashMap<>();

    private final Color[] colors = {
        new Color(227, 58, 58),
        new Color(30, 193, 84),
        new Color(61, 159, 234),
        new Color(174, 65, 232),
        new Color(227, 185, 57)
    };

    private final Point mousePosition = new Point();
    private double minStep = 0.0;
    private DiagramRange currentRange = null;
    private int width = 0;
    private int height = 0;
    private int previousY = 0;
    private int totalSize = 0;
    private int totalCount = 0;
    private int offset = 0;
    private int tempOffset = 0;
    private boolean mousePressed = false;
    // endregion

    // region Canvas
    public final JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D graphics2D = (Graphics2D) graphics;

            if (keys.size() > 0) {
                width = getWidth();
                height = getHeight();

                setupAntialiasing(graphics2D);
                setupFont(graphics2D);
                clearCanvas(graphics2D);
                prepareDraw();

                minStep = Math.max(10.0, (double) width / (double) points.size());

                int partsCount = keys.size();
                for (int partIndex = 0; partIndex < partsCount; partIndex++) {
                    graphics.setColor(colors[partIndex % colors.length]);

                    drawPart(graphics2D, keys.get(partIndex));
                }

                offset = Utils.clamp(Math.min(0, -totalSize + width), 0, offset);

                if (!mousePressed) {
                    drawHover(graphics2D);
                }

                drawGrid(graphics2D);
                drawScroll(graphics2D);
            } else {
                setupAntialiasing(graphics2D);
                clearCanvas(graphics2D);
                drawGrid(graphics2D);
            }
        }
    };
    // endregion

    // region Constructor
    public Diagram() {
        Point pressed = new Point(0, 0);

        panel.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                super.mousePressed(event);

                pressed.setLocation(event.getX(), event.getY());
                tempOffset = offset;
                mousePressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                super.mouseReleased(event);

                pressed.setLocation(0, 0);
                tempOffset = 0;
                mousePressed = false;
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                super.mouseMoved(event);

                mousePosition.setLocation(event.getX(), event.getY());
                panel.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                super.mouseDragged(event);

                offset = Utils.clamp(Math.min(0, -totalSize + width), 0, tempOffset + event.getX() - (int) pressed.getX());
                panel.repaint();
            }
        });
    }
    // endregion

    // region Private methods
    private void setupAntialiasing(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void setupFont(Graphics2D graphics2D) {
        graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    }

    private void clearCanvas(Graphics2D graphics2D) {
        graphics2D.setColor(new Color(255, 255, 255));
        graphics2D.fillRect(0, 0, width, height);
    }

    private void prepareDraw() {
        previousY = 0;
        totalSize = 0;
        totalCount = 0;
        ranges.clear();
    }

    private void drawPart(Graphics2D graphics2D, Object key) {
        List<Object> points = groups.get(key);

        int count = points.size();
        double step = Math.max(minStep, 100.0 / (double) count);
        int size = (int) (step * count);
        boolean needDraw = totalSize + size + offset >= 0 && totalSize + offset <= width;

        int tempPreviousY = 0;

        // Draw diagram
        if (needDraw) {
            int previousVertex = 0;

            Polygon polygon = new Polygon();
            polygon.addPoint(offset, height);
            for (int index = 0; index < count; index++) {
                double point = (double) points.get(index);
                int x = (int) (index * step) + offset;
                int y = (int) (height - point * height);

                polygon.addPoint(x, previousY != 0 && index == 0 ? previousY : y);

                if (index >= 1) {
                    if (index == 1) {
                        previousVertex = polygon.xpoints[polygon.npoints - 2];
                    }
                    int currentVertex = polygon.xpoints[polygon.npoints - 1];
                    int difference = currentVertex - previousVertex;

                    if (index == 1) {
                        ranges.add(new DiagramRange(totalSize + previousVertex, totalSize + currentVertex - difference / 2, totalCount + index));
                    } else {
                        ranges.add(new DiagramRange(totalSize + previousVertex - difference / 2, totalSize + currentVertex - difference / 2, totalCount + index));
                    }

                    previousVertex = currentVertex;
                }

                if (index == count - 1) {
                    polygon.addPoint(size + offset, y);

                    int currentVertex = polygon.xpoints[polygon.npoints - 1];
                    int difference = currentVertex - previousVertex;
                    ranges.add(new DiagramRange(totalSize + previousVertex - difference / 2, totalSize + currentVertex - difference / 2, totalCount + index));

                    tempPreviousY = y;
                    previousVertex = currentVertex - difference / 2;
                }
            }
            polygon.addPoint(size + offset, height);
            polygon.translate(totalSize, 0);

            ranges.add(new DiagramRange(totalSize + previousVertex, totalSize + polygon.xpoints[polygon.npoints - 1], totalCount + count - 1));

            graphics2D.fillPolygon(polygon);

            previousY = tempPreviousY;
        }

        // Draw label
        if (needDraw) {
            FontMetrics metrics = graphics2D.getFontMetrics(graphics2D.getFont());

            int textWidth = metrics.stringWidth(key.toString());
            int labelStart = Utils.clamp(-textWidth, 10, totalSize + size - 10 + offset - textWidth);
            int labelPosition = Utils.clamp(labelStart, totalSize + 10, totalSize + 10 + offset);

            graphics2D.setColor(new Color(17, 17, 17));
            graphics2D.drawString(key.toString(), labelPosition, height - 10);
        }

        totalSize += size;
        totalCount += count;
    }

    private void drawHover(Graphics2D graphics2D) {
        graphics2D.setColor(new Color(0, 0, 0, 51));
        for (DiagramRange range : ranges) {
            if (range.from  <= mousePosition.getX() && range.to >= mousePosition.getX()) {
                currentRange = range;

                graphics2D.fillRect(range.from, 0, range.to - range.from, height);

                break;
            }
        }
    }

    private void drawGrid(Graphics2D graphics2D) {
        int columns = (int) Math.ceil(width / 10.0);
        int rows = (int) Math.ceil(height / 10.0);
        double percent = Math.min(1, (double) width / totalSize);
        double scrollPosition = Math.round(-offset * percent);

        graphics2D.setColor(new Color(0, 0, 0, 26));
        for (int column = 1; column <= columns; column++) {
            int position = column * 10;
            int offset = (int) (scrollPosition / percent) % 10;

            graphics2D.drawLine(position - offset, 0, position - offset, height);
        }

        for (int row = 1; row <= rows; row++) {
            graphics2D.drawLine(0, row * 10, width, row * 10);
        }
    }

    private void drawScroll(Graphics2D graphics2D) {
        double percent = Math.min(1, (double) width / totalSize);
        double scrollPosition = Math.round(-offset * percent);

        if (percent < 1) {
            graphics2D.setColor(new Color(0, 0, 0, 100));
            graphics2D.fillRect((int) scrollPosition, height - 4, (int) Math.round(width * percent), 4);
        }
    }
    // endregion

    // region Public methods
    public void setLabels(List<Object> labels) {
        this.labels = labels;
    }

    public void setPoints(List<Object> points) {
        this.points = points;
    }

    public int getPointIndex() {
        if (currentRange != null) {
            return currentRange.index - 1;
        } else {
            return -1;
        }
    }

    public void draw() {
        int count = labels.size();

        keys.clear();
        groups.clear();
        for (int index = 0; index < count; index++) {
            Object label = labels.get(index);

            if (!groups.containsKey(label)) {
                keys.add(label);
                groups.put(label, new ArrayList<>());
            }

            groups.get(label).add(points.get(index));
        }

        panel.repaint();
    }

    public void clear() {
        labels.clear();
        points.clear();

        draw();
    }
    // endregion
}