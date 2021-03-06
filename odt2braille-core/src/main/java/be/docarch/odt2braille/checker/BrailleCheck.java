/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010-2011 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.odt2braille.checker;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import be.docarch.accessodf.Check;

/**
 *
 * @author Bert Frees
 */
public class BrailleCheck extends Check {

    private static String L10N = "be/docarch/odt2braille/checker/l10n/checks";

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

    public String getIdentifier() {
        return identifier.name();
    }

    public Status getStatus() {

        if (identifier.name().startsWith("E_")) {
            return Status.ERROR;
        } else if (identifier.name().startsWith("A_")) {
            return Status.ALERT;
        } else {
            return null;
        }
    }

    public String getName(Locale locale) {

        if (identifier == null) { return null; }
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
    }

    public String getDescription(Locale locale) {

        if (identifier == null) { return null; }
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

    public String getSuggestion(Locale locale) {

        if (identifier == null) { return null; }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(L10N, locale);
            return bundle.getString("suggestion_" + identifier.name());
        } catch (MissingResourceException e) {
            return "";
        }
    }
}
