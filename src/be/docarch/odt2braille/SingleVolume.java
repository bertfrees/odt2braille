package be.docarch.odt2braille;

/**
 *
 * @author Bert Frees
 */
public class SingleVolume extends Volume {

    public SingleVolume() {
        super(Volume.Type.NORMAL);
    }

    public SingleVolume(SingleVolume copyVolume) {
        this();
        copyVolume(copyVolume);
    }
}
