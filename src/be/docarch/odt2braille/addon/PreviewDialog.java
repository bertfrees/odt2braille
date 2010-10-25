/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
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

package be.docarch.odt2braille.addon;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.AnyConverter;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.EventObject;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.FontDescriptor;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.org.apache.xpath.internal.XPathAPI;

import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;

import be.docarch.odt2braille.PEF;
import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.Settings.PageNumberFormat;
import com.versusoft.packages.jodl.RomanNumbering;


/**
 *
 * @author Bert Frees
 */
public class PreviewDialog implements XItemListener,
                                      XActionListener {

    private final static Logger logger = Logger.getLogger("be.docarch.odt2braille.addon");

    private static final String FONT_6_DOT = "odt2braille 6 dot";
    private static final short SIZE_6_DOT = 18;
    private static final double CHAR_WIDTH_6_DOT = 5.85d;
    private static final double CHAR_HEIGHT_6_DOT = 10.66d;

    private static final String FONT_8_DOT = "odt2braille 8 dot";
    private static final short SIZE_8_DOT = 18;
    private static final double CHAR_WIDTH_8_DOT = 5.85d;
    private static final double CHAR_HEIGHT_8_DOT = 13.35d;

    private XDialog dialog = null;

    private Element root = null;

    private int cellsPerLine;
    private int linesPerPage;
    private int marginLeft;
    private int marginRight;
    private int marginTop;
    private int marginBottom;
    private int numberOfVolumes;
    private int numberOfSupplements;
    private boolean preliminaryVolumeEnabled;
    private boolean duplex;
    private boolean mirrorAlign;
    private boolean eightDots;
    private boolean preliminaryPagesPresent;
    private PageNumberFormat preliminaryPageFormat;

    private int volume;
    private int section;
    private int page;

    private int volumeCount;
    private int sectionCount;
    private int pageCount;

    private XButton backButton = null;
    private XButton nextButton = null;
    private XListBox volumesListBox = null;
    private XListBox sectionsListBox = null;
    private XListBox pagesListBox = null;
    private XTextComponent previewField = null;

    private XPropertySet backButtonProperties = null;
    private XPropertySet nextButtonProperties = null;

    private static String _backButton = "CommandButton1";
    private static String _nextButton = "CommandButton2";
    private static String _volumesListBox = "ListBox1";
    private static String _sectionsListBox = "ListBox2";
    private static String _pagesListBox = "ListBox3";
    private static String _previewField = "TextField1";

    private String L10N_windowTitle = null;
    private String L10N_volume = null;
    private String L10N_preliminary_volume = null;
    private String L10N_supplement = null;
    private String L10N_preliminary_section = null;
    private String L10N_main_section = null;


    public PreviewDialog(XComponentContext xContext,
                         PEF pef,
                         Settings settings)
                  throws ParserConfigurationException,
                         SAXException,
                         IOException,
                         TransformerException,
                         com.sun.star.uno.Exception {

        logger.entering("PreviewDialog", "<init>");

        this.cellsPerLine = settings.getCellsPerLine();
        this.linesPerPage = settings.getLinesPerPage();
        this.marginLeft = settings.getMarginLeft();
        this.marginRight = settings.getMarginRight();
        this.marginTop = settings.getMarginTop();
        this.marginBottom = settings.getMarginBottom();
        this.duplex = settings.getDuplex();
        this.mirrorAlign = settings.getMirrorAlign();
        this.eightDots = settings.getEightDots();
        this.numberOfVolumes = settings.getNumberOfVolumes();
        this.numberOfSupplements = settings.getNumberOfSupplements();
        this.preliminaryVolumeEnabled = settings.getPreliminaryVolumeEnabled();
        this.preliminaryPagesPresent = settings.getPreliminaryPagesPresent();
        this.preliminaryPageFormat = settings.getPreliminaryPageFormat();

        // L10N

        Locale oooLocale = null;
        try {
            oooLocale = new Locale(UnoUtils.getUILocale(xContext));
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            oooLocale = Locale.getDefault();
        }

        L10N_windowTitle = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("previewDialogTitle");
        L10N_volume = ResourceBundle.getBundle("be/docarch/odt2braille/l10n/Bundle", oooLocale).getString("volume");
        L10N_preliminary_volume = ResourceBundle.getBundle("be/docarch/odt2braille/l10n/Bundle", oooLocale).getString("preliminary");
        L10N_supplement = ResourceBundle.getBundle("be/docarch/odt2braille/l10n/Bundle", oooLocale).getString("supplement");
        L10N_preliminary_section = "Preliminary Section";
        L10N_main_section = "Main Section";

        L10N_volume = L10N_volume.substring(0, 1).toUpperCase() + L10N_volume.substring(1);
        L10N_preliminary_volume = L10N_preliminary_volume.substring(0, 1).toUpperCase() + L10N_preliminary_volume.substring(1);
        L10N_supplement = L10N_supplement.substring(0, 1).toUpperCase() + L10N_supplement.substring(1);

        // Load PEF file
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        docBuilder.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId)
                    throws SAXException, java.io.IOException {
                return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
            }
        });
        Document contentDoc = docBuilder.parse(pef.getSinglePEF().getAbsolutePath());

        // Make dialog

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation("be.docarch.odt2braille.addon.Odt2BrailleAddOn") + "/dialogs/PreviewDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        XControlContainer dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        XControl dialogControl = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);

        // Dialog items

        backButton = (XButton) UnoRuntime.queryInterface(XButton.class, dialogControlContainer.getControl(_backButton));
        nextButton = (XButton) UnoRuntime.queryInterface(XButton.class, dialogControlContainer.getControl(_nextButton));
        volumesListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class, dialogControlContainer.getControl(_volumesListBox));
        sectionsListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class, dialogControlContainer.getControl(_sectionsListBox));
        pagesListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class, dialogControlContainer.getControl(_pagesListBox));
        previewField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class, dialogControlContainer.getControl(_previewField));

        backButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, backButton)).getModel());
        nextButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, nextButton)).getModel());

        XPropertySet windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, dialogControl.getModel());
        windowProperties.setPropertyValue("Title", L10N_windowTitle);

        XPropertySet volumesListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, volumesListBox)).getModel());
        XPropertySet sectionsListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, sectionsListBox)).getModel());
        XPropertySet pagesListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, pagesListBox)).getModel());
        XPropertySet previewFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, previewField)).getModel());

        FontDescriptor font = (FontDescriptor)AnyConverter.toObject(FontDescriptor.class, previewFieldProperties.getPropertyValue("FontDescriptor"));
        font.Name   = eightDots ? FONT_8_DOT : FONT_6_DOT;
        font.Height = eightDots ? SIZE_8_DOT : SIZE_6_DOT;
        previewFieldProperties.setPropertyValue("FontDescriptor", font);

        int origWidth = (int)AnyConverter.toLong(previewFieldProperties.getPropertyValue("Width"));
        int origHeight = (int)AnyConverter.toLong(previewFieldProperties.getPropertyValue("Height"));
        int newWidth  = 10 + 2 + (int)Math.ceil((cellsPerLine + marginLeft + marginRight)*(eightDots ? CHAR_WIDTH_8_DOT  : CHAR_WIDTH_6_DOT));
        int newHeight = 10 + 1 + (int)Math.ceil((linesPerPage + marginTop + marginBottom)*(eightDots ? CHAR_HEIGHT_8_DOT : CHAR_HEIGHT_6_DOT));

        previewFieldProperties.setPropertyValue("Width", newWidth);
        previewFieldProperties.setPropertyValue("Height", newHeight);

        if (newWidth > origWidth) {

            volumesListBoxProperties.setPropertyValue("PositionX", (int)Math.floor((newWidth - origWidth)/2)
                    + (int)AnyConverter.toLong(volumesListBoxProperties.getPropertyValue("PositionX")));
            sectionsListBoxProperties.setPropertyValue("PositionX", (int)Math.floor((newWidth - origWidth)/2)
                    + (int)AnyConverter.toLong(sectionsListBoxProperties.getPropertyValue("PositionX")));
            pagesListBoxProperties.setPropertyValue("PositionX", (int)Math.floor((newWidth - origWidth)/2)
                    + (int)AnyConverter.toLong(pagesListBoxProperties.getPropertyValue("PositionX")));
            backButtonProperties.setPropertyValue("PositionX", (int)Math.floor((newWidth - origWidth)/2)
                    + (int)AnyConverter.toLong(backButtonProperties.getPropertyValue("PositionX")));
            nextButtonProperties.setPropertyValue("PositionX", (int)Math.floor((newWidth - origWidth)/2)
                    + (int)AnyConverter.toLong(nextButtonProperties.getPropertyValue("PositionX")));

            try{

                XMultiPropertySet xMultiPropertySet = (XMultiPropertySet) UnoRuntime.queryInterface(
                                                       XMultiPropertySet.class, dialogControl.getModel());
                xMultiPropertySet.setPropertyValues(
                    new String[] { "Height", "Width" },
                    new Object[] {
                        new Integer(newHeight - origHeight + (int)AnyConverter.toLong(windowProperties.getPropertyValue("Height"))),
                        new Integer(newWidth - origWidth + (int)AnyConverter.toLong(windowProperties.getPropertyValue("Width")))
                    }
                );

            } catch (com.sun.star.uno.Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }

        } else {

            previewFieldProperties.setPropertyValue("PositionX", (int)Math.floor((origWidth - newWidth)/2));
            windowProperties.setPropertyValue("Height", newHeight - origHeight + (int)AnyConverter.toLong(windowProperties.getPropertyValue("Height")));

        }

        root = contentDoc.getDocumentElement();

        volume = 1;
        section = 1;
        page = 1;

        updateVolumesListBox();
        updateSectionsListBox();
        updatePagesListBox();
        updateButtons();

        sectionsListBox.addItemListener(this);
        pagesListBox.addItemListener(this);
        backButton.addActionListener(this);
        nextButton.addActionListener(this);

        showPage();

        logger.exiting("PreviewDialog", "<init>");

    }

    public void execute() {

        logger.entering("PreviewDialog", "execute");
        dialog.execute();
        logger.exiting("PreviewDialog", "execute");

    }

    public void dispose() {

        XComponent dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);

        if (dialogComponent != null) {
            dialogComponent.dispose();
        }
    }

    private void showPage() throws TransformerException {

        String previewText = "";
        String line = null;
        Node row = null;
        NodeList rows = XPathAPI.selectNodeList(root,
                "/pef/body/volume[" + volume + "]/section[" + section + "]/page[" + page + "]/row");

        int width = cellsPerLine + marginLeft + marginRight;
        int height = linesPerPage + marginTop + marginBottom;
        int offset = ((page % 2 == 0) && duplex && mirrorAlign) ? marginRight : marginLeft;

        int x,y;

        y = 0;
        while (y < marginTop) {
            x = 0;
            while (x < width) {
                previewText += " ";
                x++;
            }
            previewText += "\n";
            y++;
        }
        for (int i=0; i<rows.getLength(); i++) {
            x = 0;
            while (x < offset) {
                previewText += " ";
                x++;
            }
            row = (Element)rows.item(i);
            line = row.getTextContent();
            previewText += line;
            x += line.length();
            while (x < offset + cellsPerLine) {
                previewText += "\u2800";
                x++;
            }
            while (x < width) {
                previewText += " ";
                x++;
            }
            y++;
            if (y < height) { previewText += "\n"; }
        }
        while (y < height) {
            x = 0;
            while (x < width) {
                previewText += " ";
                x++;
            }
            y++;
            if (y < height) { previewText += "\n"; }
        }

        previewField.setText(previewText);

    }

    private void updateVolumesListBox() {

        String vol;
        volumesListBox.removeItemListener(this);
        volumeCount = Math.max(1, numberOfVolumes) + numberOfSupplements + (preliminaryVolumeEnabled?1:0);

        for (int i=0;i<volumeCount;i++) {
            if (i==0 && preliminaryVolumeEnabled) {
                vol = L10N_preliminary_volume;
            } else if (i < Math.max(1, numberOfVolumes) + (preliminaryVolumeEnabled?1:0)) {
                vol = L10N_volume + " " + (i + 1 - (preliminaryVolumeEnabled?1:0));
            } else {
                vol = L10N_supplement + " " + (i + 1 - Math.max(1, numberOfVolumes) - (preliminaryVolumeEnabled?1:0));
            }
            volumesListBox.addItem(vol, (short)i);
        }

        volumesListBox.selectItemPos((short)(volume-1), true);
        volumesListBox.addItemListener(this);

    }

    private void updateSectionsListBox() {

        sectionCount = 0;
        sectionsListBox.removeItemListener(this);
        sectionsListBox.removeItems((short)0, Short.MAX_VALUE);

        if (preliminaryPagesPresent) {
            sectionsListBox.addItem(L10N_preliminary_section, (short)0);
            sectionCount++;
        }
        if (!(volume==1 && preliminaryVolumeEnabled)) {
            sectionsListBox.addItem(L10N_main_section, (short)sectionCount);
            sectionCount++;
        }

        sectionsListBox.selectItemPos((short)(section-1), true);
        sectionsListBox.addItemListener(this);

    }

    private void updatePagesListBox() throws TransformerException {

        int firstpage = 1;
        String pag = null;
        pagesListBox.removeItemListener(this);
        pagesListBox.removeItems((short)0, Short.MAX_VALUE);
        pageCount = Integer.parseInt(XPathAPI.eval(root,
                "count(/pef/body/volume[" + volume + "]/section[" + section + "]/page)").str());

        if (!(preliminaryPagesPresent && section == 1)) {
            firstpage += Integer.parseInt(XPathAPI.eval(root,
                    "count(/pef/body/volume[" + volume + "]/preceding-sibling::*/section[position()=" + section + "]/page)").str());
        }
        for (int i=0; i<pageCount;i++) {
            if (preliminaryPagesPresent && section == 1) {
                if (preliminaryPageFormat == PageNumberFormat.P) {
                    pag = "p" + (firstpage + i);
                } else {
                    pag = RomanNumbering.toRoman(firstpage + i);
                }
            } else {
                pag = Integer.toString(firstpage + i);
            }
            pagesListBox.addItem(pag, (short)i);
        }

        pagesListBox.selectItemPos((short)(page-1), true);
        pagesListBox.addItemListener(this);

    }

    private void updateButtons() throws com.sun.star.uno.Exception,
                                        TransformerException {

        backButtonProperties.setPropertyValue("Enabled", XPathAPI.eval(root,
                "/pef/body/volume[" + volume + "]/section[" + section + "]/page[" + page + "]/preceding::page").bool());
        nextButtonProperties.setPropertyValue("Enabled", XPathAPI.eval(root,
                "/pef/body/volume[" + volume + "]/section[" + section + "]/page[" + page + "]/following::page").bool());

    }

    public void actionPerformed(ActionEvent actionEvent) {

        Object source = actionEvent.Source;

        try {

            if (source.equals(backButton)) {

                if (page > 1) {
                    page--;
                    pagesListBox.selectItemPos((short)(page-1), true);
                    updateButtons();
                    showPage();
                } else if (section > 1) {
                    section--;
                    sectionsListBox.selectItemPos((short)(section-1), true);
                    updatePagesListBox();
                    page = pageCount;
                    pagesListBox.selectItemPos((short)(page-1), true);
                    updateButtons();
                    showPage();
                } else if (volume > 1) {
                    volume--;
                    volumesListBox.selectItemPos((short)(volume-1), true);
                    updateSectionsListBox();
                    section = sectionCount;
                    sectionsListBox.selectItemPos((short)(section-1), true);
                    updatePagesListBox();
                    page = pageCount;
                    pagesListBox.selectItemPos((short)(page-1), true);
                    updateButtons();
                    showPage();
                }

            } else if (source.equals(nextButton)) {

                if (page < pageCount) {
                    page++;
                    pagesListBox.selectItemPos((short)(page-1), true);
                    updateButtons();
                    showPage();
                } else if (section < sectionCount) {
                    section++;
                    sectionsListBox.selectItemPos((short)(section-1), true);
                    updatePagesListBox();
                    page = 1;
                    pagesListBox.selectItemPos((short)(page-1), true);
                    updateButtons();
                    showPage();
                } else if (volume < volumeCount) {
                    volume++;
                    volumesListBox.selectItemPos((short)(volume-1), true);
                    updateSectionsListBox();
                    section = 1;
                    sectionsListBox.selectItemPos((short)(section-1), true);
                    updatePagesListBox();
                    page = 1;
                    pagesListBox.selectItemPos((short)(page-1), true);
                    updateButtons();
                    showPage();
                }
            }

        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void itemStateChanged(ItemEvent itemEvent) {

        Object source = itemEvent.Source;

        try {

            if (source.equals(volumesListBox)) {
                volume = volumesListBox.getSelectedItemPos() + 1;
                section = 1;
                updateSectionsListBox();
                page = 1;
                updatePagesListBox();
                updateButtons();
                showPage();
            } else if (source.equals(sectionsListBox)) {
                section = sectionsListBox.getSelectedItemPos() + 1;
                page = 1;
                updatePagesListBox();
                updateButtons();
                showPage();
            } else if (source.equals(pagesListBox)) {
                page = pagesListBox.getSelectedItemPos() + 1;
                updateButtons();
                showPage();
            }

        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void disposing(EventObject event) {}

}
