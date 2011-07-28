package be.docarch.odt2braille;

public class ConversionException extends Exception {

    private String error = null;

    public ConversionException() {
        super();
        this.error = "odt2braille conversion exception";
    }

    public ConversionException(String error) {
        super(error);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
