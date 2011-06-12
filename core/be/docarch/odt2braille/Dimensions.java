package be.docarch.odt2braille;

public class Dimensions implements org.daisy.paper.Dimensions {

    private double width;
    private double height;

    public Dimensions(double width,
                      double height) {

        this.width = width;
        this.height = height;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getWidth() {
        return width;
    }
}