package org.seasle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MouseTracker {
    private final JComponent component;
    private final Point pressed = new Point();

    public MouseTracker(JComponent component) {
        this.component = component;

        bindEvents();
    }

    private void bindEvents() {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                super.mousePressed(event);

                pressed.setLocation(event.getX(), event.getY());
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                super.mouseReleased(event);

                pressed.setLocation(0, 0);
            }
        });
    }

    public Point getPressed() {
        return pressed;
    }
}