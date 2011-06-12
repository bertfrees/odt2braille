package be.docarch.odt2braille;

/**
 *
 * @author Bert Frees
 */
public class AutomaticVolume extends Volume {

    private String indentifier;
    private int startPage;

    public AutomaticVolume(int index, int startPage) {
        super(Volume.Type.NORMAL);
        this.indentifier = "auto-volume-" + index;
        this.startPage = Math.max(1,startPage);
    }

    public String getIdentifier() {
        return indentifier;
    }

    public int getStartPage() {
        return startPage;
    }
}
