package io.github.humbleui.jwm.examples;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import io.github.humbleui.jwm.*;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.*;

public class PanelMouse extends Panel {
    public EventMouseMove lastMouseMove = null;
    public Point scroll = new Point(0, 0);
    public List<MouseButton> buttons = Collections.synchronizedList(new ArrayList<MouseButton>());
    public boolean lastInside = false;

    public PanelMouse(Window window) {
        super(window);
    }

    @Override
    public void accept(Event e) {
        if (e instanceof EventMouseMove ee) {
            lastMouseMove = ee;
            var inside = contains(ee.getX(), ee.getY());
            if (inside || lastInside) {
                lastInside = inside;
                window.requestFrame();
            }
        } else if (e instanceof EventMouseScroll ee) {
            scroll = scroll.offset(ee.getDeltaX(), ee.getDeltaY());
            window.requestFrame();
        } else if (e instanceof EventMouseButton ee) {
            var button = ee.getButton();
            if (ee.isPressed() == true) {
                if (!buttons.contains(button))
                    buttons.add(button);
            } else
                buttons.remove(button);
            window.requestFrame();
        }
    }

    @Override
    public void paintImpl(Canvas canvas, int width, int height, float scale) {
        var capHeight = Example.FONT12.getMetrics().getCapHeight();

        // position
        if (lastInside && lastMouseMove != null) {
            try (var paint = new Paint().setColor(0x40FFFFFF)) {
                var x = lastMouseMove.getX() - lastX;
                var y = lastMouseMove.getY() - lastY;
                canvas.drawRect(Rect.makeXYWH(0, y - 1 * scale, width, 2 * scale), paint);
                canvas.drawRect(Rect.makeXYWH(x - 1 * scale, 0, 2 * scale, height), paint);

                canvas.save();
                canvas.translate(x + 3 * scale, y - 5 * scale);
                canvas.drawString(x + ", " + y, 0, 0, Example.FONT12, paint);
                canvas.translate(0, 10 * scale + capHeight);
                for (var button: MouseButton.values())
                    if (lastMouseMove.isButtonDown(button)) {
                        canvas.drawString(capitalize(button.toString()), 0, 0, Example.FONT12, paint);
                        canvas.translate(0, 2 * capHeight);
                    }
                for (var modifier: KeyModifier.values())
                    if (lastMouseMove.isModifierDown(modifier)) {
                        canvas.drawString(capitalize(modifier.toString()), 0, 0, Example.FONT12, paint);
                        canvas.translate(0, 2 * capHeight);
                    }
                canvas.restore();
            }
        }

        // scroll
        try (var paint = new Paint().setMode(PaintMode.STROKE).setStrokeWidth(2 * scale).setColor(0x40FFFFFF)) {
            for (int i = 0; i < 100; ++i) {
                var x = scroll.getX() + i * 10 * scale;
                if (0 <= x && x <= width)
                    canvas.drawLine(x, 0, x, i * scale, paint);
                var y = scroll.getY() + i * 10 * scale;
                if (0 <= y && y <= height)
                    canvas.drawLine(0, y, i * scale, y, paint);
            }
        }

        // buttons

        var lines = Arrays.stream(MouseButton._values).map((button) -> TextLine.make(capitalize(button.toString()), Example.FONT12)).collect(Collectors.toList());
        try (var paint = new Paint();) {
            

            var padding = (int) 8 * scale;
            int y = Example.PADDING;
            for (var button: MouseButton._values) {
                try (var line = TextLine.make(capitalize(button.toString()), Example.FONT12); ) {
                    var pressed = buttons.contains(button);
                    if (pressed) {
                        paint.setColor(0x40000000);
                        canvas.drawRRect(RRect.makeXYWH(Example.PADDING, y, line.getWidth() + 2 * padding, capHeight + 2 * padding, 4 * scale), paint);
                    }

                    paint.setColor(pressed ? 0xFFFFFFFF : 0x40FFFFFF);
                    canvas.drawTextLine(line, Example.PADDING + padding, y + capHeight + padding, paint);

                    y += capHeight + 2 * padding + 1 * scale;
                }
            }
        }
    }
}
