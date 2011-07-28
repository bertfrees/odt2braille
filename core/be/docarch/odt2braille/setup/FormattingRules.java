package be.docarch.odt2braille.setup;

public interface FormattingRules {

    public String getName();

    public String getDescription();

    public void applyTo(Configuration configuration);

}
