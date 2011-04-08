package be.docarch.odt2braille;

/**
 *
 * @author Bert Frees
 */
public class SectionVolume extends Volume {

    private String sectionName;

    public SectionVolume(Type type,
                         String sectionName) {

        super(type);
        this.sectionName = sectionName;
        switch (type) {
            case NORMAL:
            case SUPPLEMENTARY:
                break;
            default:
                this.type = Type.NORMAL;
        }
    }

    public SectionVolume(SectionVolume copyVolume) {

        this(copyVolume.getType(), copyVolume.getSectionName());
        copyVolume(copyVolume);
    }

    public String getSectionName() {
        return sectionName;
    }
}
