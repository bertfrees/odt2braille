package be.docarch.odt2braille.setup;

public interface TranslationTableProperties {

	public static enum Dots { SIXDOTS, EIGHTDOTS };

	public String getLocale();
	public int    getGrade();
	public Dots   getDots();
}
