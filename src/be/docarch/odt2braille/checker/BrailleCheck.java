package be.docarch.odt2braille.checker;

import java.util.Locale;
import java.util.ResourceBundle;

import be.docarch.accessibility.Check;

/**
 *
 * @author Bert Frees
 */
public class BrailleCheck extends Check {

    private static Locale locale = Locale.getDefault();
    private static ResourceBundle names = ResourceBundle.getBundle("be/docarch/odt2braille/checker/l10n/names", locale);
    private static ResourceBundle descriptions = ResourceBundle.getBundle("be/docarch/odt2braille/checker/l10n/descriptions", locale);
    private static ResourceBundle suggestions = ResourceBundle.getBundle("be/docarch/odt2braille/checker/l10n/suggestions", locale);

    public static enum ID {

        A_NoBrailleToc,
        A_NotInBrailleVolume,       // ***
        A_OmittedInBraille,         // voor captions: ok
        A_TransposedInBraille,      // ***
        A_UnnaturalVolumeBreak      // *** info pas beschikbaar na split-volumes.xsl

                // Table or image without caption ?

    }

    private ID identifier;

    public BrailleCheck(ID identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier.name();
    }

    @Override
    public Status getStatus() {

        switch (identifier) {
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

    @Override
    public Category getCategory() {

        switch (identifier) {
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

    @Override
    public String getName() {

        if (identifier == null) {
            return null;
        } else if (names.containsKey(identifier.name())) {
            return names.getString(identifier.name());
        } else {
            return identifier.name();
        }
    }

    @Override
    public String getDescription() {

        if (identifier == null) {
            return null;
        } else if (descriptions.containsKey(identifier.name())) {
            return descriptions.getString(identifier.name());
        } else {
            return identifier.name();
        }
    }

    @Override
    public String getSuggestion() {

        if (identifier == null) {
            return null;
        } else if (suggestions.containsKey(identifier.name())) {
            return suggestions.getString(identifier.name());
        } else {
            return identifier.name();
        }
    }

    @Override
    public RepairMode getRepairMode() {
        return RepairMode.MANUAL;
    }

}
