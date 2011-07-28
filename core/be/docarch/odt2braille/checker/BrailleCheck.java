package be.docarch.odt2braille.checker;

import java.util.Locale;
import java.util.ResourceBundle;

import java.util.MissingResourceException;

//import be.docarch.accessibility.Check;

/**
 *
 * @author Bert Frees
 */
public class BrailleCheck /* extends Check */ {

    private static String L10N = "be/docarch/odt2braille/checker/l10n/Bundle";

    public static enum ID {

        A_NoBrailleToc,
        A_UnnaturalVolumeBreak,      // *** info pas beschikbaar na split-volumes.xsl
        A_VolumesTooLong,
        A_VolumesTooShort,
        A_VolumesDifferTooMuch,
        A_PreliminaryVolumeRequired,
        A_PreliminaryVolumeTooShort,
        A_VolumeDoesntBeginWithHeading,
        A_OmittedInBraille,
        A_OmittedCaption,
        A_NotInBrailleVolume,
        A_TransposedInBraille,
        A_PageWidthTooSmall,

        A_EmbosserDoesNotSupport8Dot,
        A_FileFormatDoesNotSupport8Dot

    }

    private ID identifier;

    public BrailleCheck(ID identifier) {
        this.identifier = identifier;
    }

  //@Override
    public String getIdentifier() {
        return identifier.name();
    }

 /* @Override
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
            case A_EmbosserDoesNotSupport8Dot:
            case A_FileFormatDoesNotSupport8Dot:
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
            case A_EmbosserDoesNotSupport8Dot:
            case A_FileFormatDoesNotSupport8Dot:
                return Category.BRAILLE;
            default:
                return null;
        }
    }

    @Override
    public String getName(Locale locale) {

        if (identifier == null) {
            return null;
        }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(L10N, locale);
            return bundle.getString("name_" + identifier.name());
        } catch (MissingResourceException e) {
            switch (identifier) {
                case A_EmbosserDoesNotSupport8Dot:
                case A_FileFormatDoesNotSupport8Dot:
                    return "8-dot Braille not supported";
                default:
                    return identifier.name();
            }
        }
    } */

  //@Override
    public String getDescription(Locale locale) {

        if (identifier == null) {
            return null;
        }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(L10N, locale);
            return bundle.getString("description_" + identifier.name());
        } catch (MissingResourceException e) {
            switch (identifier) {
                case A_EmbosserDoesNotSupport8Dot:
                    return "The selected embosser doesn't support 8-dot Braille. Dots 7 and 8 will be ignored.";
                case A_FileFormatDoesNotSupport8Dot:
                    return "The selected file format doesn't support 8-dot Braille. Dots 7 and 8 will be ignored.";
                default:
                    return "";
            }
        }
    }

 /* @Override
    public String getSuggestion(Locale locale) {

        if (identifier == null) {
            return null;
        }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(L10N, locale);
            return bundle.getString("suggestion_" + identifier.name());
        } catch (MissingResourceException e) {
            return "";
        }
    } */
}
