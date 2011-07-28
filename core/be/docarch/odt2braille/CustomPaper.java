package be.docarch.odt2braille;

import org.daisy.paper.Paper;
import org.daisy.factory.AbstractFactory;

public class CustomPaper extends AbstractFactory
                         implements Paper {

    private double width;
    private double height;

    public CustomPaper() {
        super("Custom...", "Paper with adjustable width and height", CustomPaperProvider.PaperSize.CUSTOM);
        width = 210d;
        height = 297d;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public Object getFeature(String key) {
        throw new IllegalArgumentException("Unknown feature: " + key);
    }

    @Override
    public Object getProperty(String key) {
        return null;
    }

    @Override
    public void setFeature(String key, Object value) {
        throw new IllegalArgumentException("Unknown feature: " + key);
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getWidth() {
        return width;
    }

    public Shape getShape() {
        if (getWidth()<getHeight()) {
            return Shape.PORTRAIT;
        } else if (getWidth()>getHeight()) {
            return Shape.LANDSCAPE;
        } else {
            return Shape.SQUARE;
        }
    }
}