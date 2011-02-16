package be.docarch.odt2braille.checker;

import be.docarch.accessibility.Check;

/**
 *
 * @author Bert Frees
 */
public class BrailleCheck extends Check {

    public static enum ID {

        A_NoPreliminarySection,
        A_NoTitlePage,
        A_NoBrailleToc,
        A_NotInBrailleVolume,
        A_OmittedInBraille,
        A_TransposedInBraille,
        A_UnnaturalVolumeBreak

    }

    private ID identifier;

    public BrailleCheck(ID identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier.name();
    }

    public Status getStatus() {

        switch (identifier) {
            case A_NoPreliminarySection:
            case A_NoTitlePage:
            case A_NoBrailleToc:
            case A_NotInBrailleVolume:
            case A_OmittedInBraille:
            case A_TransposedInBraille:
            case A_UnnaturalVolumeBreak:
                return Status.ALERT;
            default:
                return null;
        }
    }

    public Category getCategory() {

        switch (identifier) {
            case A_NoPreliminarySection:
            case A_NoTitlePage:
            case A_NoBrailleToc:
            case A_NotInBrailleVolume:
            case A_OmittedInBraille:
            case A_TransposedInBraille:
            case A_UnnaturalVolumeBreak:
                return Category.BRAILLE;
            default:
                return null;
        }
    }

    public String getName() {
        return "";
    }

    public String getDescription() {
        return "";
    }

    public String getSuggestion() {
        return "";
    }

    public RepairMode getRepairMode() {
        return RepairMode.MANUAL;
    }

}
