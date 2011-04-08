package be.docarch.odt2braille;

/**
 *
 * @author Bert Frees
 */
public class PreliminaryVolume extends Volume {

    public PreliminaryVolume() {
        super(Volume.Type.PRELIMINARY);
        setExtFrontMatter(true);
        setExtToc(true);
    }

    public PreliminaryVolume(PreliminaryVolume copyVolume) {
        this();
        copyVolume(copyVolume);
    }
}
