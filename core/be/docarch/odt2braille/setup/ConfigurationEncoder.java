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
 
 package be.docarch.odt2braille.setup;

import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.util.Locale;
import java.beans.XMLEncoder;

import be.docarch.odt2braille.setup.style.*;
import be.docarch.odt2braille.setup.style.TocStyle.TocLevelStyle;
import be.docarch.odt2braille.setup.Configuration.Volume;
import be.docarch.odt2braille.setup.Configuration.SectionVolume;
import be.docarch.odt2braille.setup.Configuration.SplittableVolume;
import be.docarch.odt2braille.setup.EmbossConfiguration.MarginSettings;
import org.daisy.braille.tools.Length;

public class ConfigurationEncoder {

    /*
     * @param object  An instance of Configuration, EmbossConfiguration or ExportConfiguration
     */
    public static void writeObject(Object object,
                                   OutputStream output) {

        BufferedOutputStream bos = new BufferedOutputStream(output);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Configuration.class.getClassLoader()); {

            XMLEncoder xmlEncoder = new XMLEncoder(bos);

            PersistenceDelegate delegate = new PersistenceDelegate();

            xmlEncoder.setPersistenceDelegate(Setting.class,             delegate);
            xmlEncoder.setPersistenceDelegate(SettingMap.class,          delegate);
            xmlEncoder.setPersistenceDelegate(SettingList.class,         delegate);
            xmlEncoder.setPersistenceDelegate(Configuration.class,       delegate);
            xmlEncoder.setPersistenceDelegate(EmbossConfiguration.class, delegate);
            xmlEncoder.setPersistenceDelegate(ExportConfiguration.class, delegate);
            xmlEncoder.setPersistenceDelegate(TranslationTable.class,    delegate);
            xmlEncoder.setPersistenceDelegate(MarginSettings.class,      delegate);
            xmlEncoder.setPersistenceDelegate(Locale.class,              delegate);
            xmlEncoder.setPersistenceDelegate(Length.class,              delegate);
            xmlEncoder.setPersistenceDelegate(Volume.class,              delegate);
            xmlEncoder.setPersistenceDelegate(SectionVolume.class,       delegate);
            xmlEncoder.setPersistenceDelegate(SplittableVolume.class,    delegate);
            xmlEncoder.setPersistenceDelegate(SpecialSymbol.class,       delegate);
            xmlEncoder.setPersistenceDelegate(CharacterStyle.class,      delegate);
            xmlEncoder.setPersistenceDelegate(ParagraphStyle.class,      delegate);
            xmlEncoder.setPersistenceDelegate(HeadingStyle.class,        delegate);
            xmlEncoder.setPersistenceDelegate(ListStyle.class,           delegate);
            xmlEncoder.setPersistenceDelegate(TableStyle.class,          delegate);
            xmlEncoder.setPersistenceDelegate(TocStyle.class,            delegate);
            xmlEncoder.setPersistenceDelegate(TocLevelStyle.class,       delegate);
            xmlEncoder.setPersistenceDelegate(FootnoteStyle.class,       delegate);
            xmlEncoder.setPersistenceDelegate(FrameStyle.class,          delegate);
            xmlEncoder.setPersistenceDelegate(PictureStyle.class,        delegate);

            xmlEncoder.writeObject(object);
            xmlEncoder.close();

        } Thread.currentThread().setContextClassLoader(cl);
    }
}
