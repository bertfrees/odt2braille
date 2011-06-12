package be.docarch.odt2braille.checker;

import java.util.Locale;
import java.util.ResourceBundle;

import java.util.MissingResourceException;

import be.docarch.accessibility.Check;

/**
 *
 * @author Bert Frees
 */
public class BrailleCheck extends Check {

    private static Locale locale = Locale.getDefault();
    private static ResourceBundle bundle = ResourceBundle.getBundle("be/docarch/odt2braille/checker/l10n/Bundle", locale);

    public static enum ID {

        A_NoBrailleToc,
        A_NotInBrailleVolume,       // ***
        A_OmittedInBraille,         // voor captions: ok
        A_TransposedInBraille,      // ***
        A_UnnaturalVolumeBreak,      // *** info pas beschikbaar na split-volumes.xsl

        // Table or image without caption ?

        A_VolumesTooLong,
        A_VolumesTooShort,
        A_VolumesDifferTooMuch,
        A_PreliminaryVolumeRequired,
        A_PreliminaryVolumeTooShort,
        A_VolumeDoesntBeginWithHeading,
        A_OmissionsInsideVolume,
        A_OmissionsOutsideVolume,
        A_Transpositions,
        A_PageWidthTooSmall

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
            case A_VolumesTooLong:
            case A_VolumesTooShort:
            case A_VolumesDifferTooMuch:
            case A_PreliminaryVolumeRequired:
            case A_PreliminaryVolumeTooShort:
            case A_VolumeDoesntBeginWithHeading:
            case A_OmissionsInsideVolume:
            case A_OmissionsOutsideVolume:
            case A_Transpositions:
            case A_PageWidthTooSmall:
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
            case A_VolumesTooLong:
            case A_VolumesTooShort:
            case A_VolumesDifferTooMuch:
            case A_PreliminaryVolumeRequired:
            case A_PreliminaryVolumeTooShort:
            case A_VolumeDoesntBeginWithHeading:
            case A_OmissionsInsideVolume:
            case A_OmissionsOutsideVolume:
            case A_Transpositions:
            case A_PageWidthTooSmall:
                return Category.BRAILLE;
            default:
                return null;
        }
    }

    @Override
    public String getName() {

        if (identifier == null) {
            return null;
        }
        try {
            return bundle.getString("name_" + identifier.name());
        } catch (MissingResourceException e) {
            return identifier.name();
        }
    }

    @Override
    public String getDescription() {

        if (identifier == null) {
            return null;
        }
        try {
            return bundle.getString("description_" + identifier.name());
        } catch (MissingResourceException e) {
            return identifier.name();
        }
    }

    @Override
    public String getSuggestion() {

        if (identifier == null) {
            return null;
        }
        try {
            return bundle.getString("suggestion_" + identifier.name());
        } catch (MissingResourceException e) {
            return identifier.name();
        }
    }
}
