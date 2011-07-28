package be.docarch.odt2braille.setup;

import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.util.Locale;
import java.beans.XMLEncoder;

import be.docarch.odt2braille.setup.style.*;
import be.docarch.odt2braille.setup.style.TocStyle.TocLevelStyle;
import be.docarch.odt2braille.setup.Configuration.Volume;
import be.docarch.odt2braille.setup.Configuration.SectionVolume;
import be.docarch.odt2braille.setup.EmbossConfiguration.MarginSettings;

public class ConfigurationEncoder {

    /*
     * @param object  An instance of Configuration, EmbossConfiguration or ExportConfiguration
     */
    public static void writeObject(Object object,
                                   OutputStream output) {

        BufferedOutputStream bos = new BufferedOutputStream(output);
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
        xmlEncoder.setPersistenceDelegate(Volume.class,              delegate);
        xmlEncoder.setPersistenceDelegate(SectionVolume.class,       delegate);
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
    }
}
