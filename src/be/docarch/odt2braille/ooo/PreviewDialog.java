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

package be.docarch.odt2braille.ooo;

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
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.WindowDescriptor;
import com.sun.star.awt.WindowClass;
import com.sun.star.awt.WindowAttribute;
import com.sun.star.awt.PosSize;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowListener;
import com.sun.star.awt.WindowEvent;
import com.sun.star.awt.FontDescriptor;
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
import be.docarch.odt2braille.BrailleFileExporter.BrailleFileType;
import com.versusoft.packages.jodl.RomanNumbering;
import org_pef_text.AbstractTable;
import org_pef_text.TableFactory;


/**
 *
 * @author Bert Frees
 */
public class PreviewDialog implements XItemListener,
                                      XActionListener,
                                      XWindowListener {

    private final static Logger logger = Logger.getLogger("be.docarch.odt2braille");

    private static final String FONT_6_DOT = "odt2braille 6 dot";
    private static final String FONT_8_DOT = "odt2braille 8 dot";
    private static String FONT_DOTS;

    private static final short DEFAULT_FONT_SIZE = 20;
    private static final short MIN_FONTSIZE = 16;
    private static final short MAX_FONTSIZE = 50;

    private static final int X_BACK_BUTTON = 2*7;
    private static final int X_VOLUMES_LISTBOX = 2*38;
    private static final int X_SECTIONS_LISTBOX = 2*112;
    private static final int X_PAGES_LISTBOX = 2*186;
    private static final int X_NEXT_BUTTON = 2*239;
    private static final int X_CHARSET_LISTBOX = 2*230;
    private static final int Y_PREVIEW_FIELD = 2*45;
    private static final int MIN_WIDTH = 2*270;
    private static final int MIN_HEIGHT = 2*200;

    private XComponentContext xContext = null;
    private XFrame frame = null;
    private XWindow window = null;
    private Element root = null;
    private FontDescriptor font = null;

    private short fontSize;
    private int cellsPerLine;
    private int linesPerPage;
    private int marginInner;
    private int marginOuter;
    private int marginTop;
    private int marginBottom;
    private int numberOfVolumes;
    private int numberOfSupplements;
    private boolean preliminaryVolumeEnabled;
    private boolean duplex;
    private boolean preliminaryPagesPresent;
    private PageNumberFormat preliminaryPageFormat;
    private AbstractTable table;
    private short charset;
    private int beginningBraillePageNumber;

    private int volume;
    private int section;
    private int page;

    private int volumeCount;
    private int sectionCount;
    private int pageCount;

    private XButton increaseFontSizeButton = null;
    private XButton decreaseFontSizeButton = null;
    private XButton backButton = null;
    private XButton nextButton = null;
    private XListBox volumesListBox = null;
    private XListBox sectionsListBox = null;
    private XListBox pagesListBox = null;
    private XListBox charsetListBox = null;
    private XTextComponent previewField = null;

    private XPropertySet backButtonProperties = null;
    private XPropertySet nextButtonProperties = null;
    private XPropertySet previewFieldProperties = null;
    private XPropertySet increaseFontSizeButtonProperties = null;
    private XPropertySet decreaseFontSizeButtonProperties = null;

    private XWindow nextButtonWindow = null;
    private XWindow backButtonWindow = null;
    private XWindow volumesListBoxWindow = null;
    private XWindow sectionsListBoxWindow = null;
    private XWindow pagesListBoxWindow = null;
    private XWindow charsetListBoxWindow = null;
    private XWindow previewFieldWindow = null;

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
                         IOException {

        logger.entering("PreviewDialog", "<init>");

        this.xContext = xContext;
        this.fontSize = DEFAULT_FONT_SIZE;
        this.cellsPerLine = settings.getCellsPerLine();
        this.linesPerPage = settings.getLinesPerPage();
        this.marginInner = settings.getMarginInner();
        this.marginOuter = settings.getMarginInner();
        this.marginTop = settings.getMarginTop();
        this.marginBottom = settings.getMarginBottom();
        this.duplex = settings.getDuplex();
        this.numberOfVolumes = settings.getNumberOfVolumes();
        this.numberOfSupplements = settings.getNumberOfSupplements();
        this.preliminaryVolumeEnabled = settings.getPreliminaryVolumeEnabled();
        this.preliminaryPagesPresent = settings.getPreliminaryPagesPresent();
        this.preliminaryPageFormat = settings.getPreliminaryPageFormat();
        this.beginningBraillePageNumber = settings.getBeginningBraillePageNumber();

        if (settings.getBrailleFileType() == BrailleFileType.BRF ||
            settings.getBrailleFileType() == BrailleFileType.BRA) {
            this.table = new TableFactory().newTable(settings.getTable());
        }

        FONT_DOTS = settings.getEightDots() ? FONT_8_DOT : FONT_6_DOT;

        // L10N

        Locale oooLocale = null;
        try {
            oooLocale = new Locale(UnoUtils.getUILocale(xContext));
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            oooLocale = Locale.getDefault();
        }

        L10N_windowTitle = ResourceBundle.getBundle("be/docarch/odt2braille/ooo/l10n/Bundle", oooLocale).getString("previewDialogTitle");
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
        root = contentDoc.getDocumentElement();

        logger.exiting("PreviewDialog", "<init>");

    }

    private void buildDialog() throws com.sun.star.uno.Exception {

        logger.entering("PreviewDialog", "buildDialog");

        XMultiComponentFactory xMCF = (XMultiComponentFactory) UnoRuntime.queryInterface(
                                       XMultiComponentFactory.class, xContext.getServiceManager());
        Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
        XComponent xDesktopComponent = (XComponent) xDesktop.getCurrentComponent();
        XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xDesktopComponent);
        XController xController = xModel.getCurrentController();
        XFrame parentFrame = xController.getFrame();
        XWindow parentWindow = parentFrame.getContainerWindow();
        XWindowPeer parentWindowPeer = (XWindowPeer) UnoRuntime.queryInterface (
                                        XWindowPeer.class, parentWindow);
        XToolkit toolkit = parentWindowPeer.getToolkit();

        XWindowPeer windowPeer = createWindow(toolkit, parentWindowPeer, WindowClass.SIMPLE, "dialog",
                       WindowAttribute.CLOSEABLE + WindowAttribute.BORDER + WindowAttribute.SIZEABLE + WindowAttribute.MOVEABLE + WindowAttribute.SHOW,
                       2*100, 2*100, MIN_WIDTH, MIN_HEIGHT);

        window = (XWindow)UnoRuntime.queryInterface(XWindow.class, windowPeer);
        frame = (XFrame)UnoRuntime.queryInterface(XFrame.class, xMCF.createInstanceWithContext("com.sun.star.frame.Frame", xContext));
        frame.initialize(window);
        window.addWindowListener(this);

        XControl nextButtonControl = createControl(xMCF, xContext, toolkit, windowPeer, "Button", X_NEXT_BUTTON, 2*7, 2*24, 2*14);
        XControl backButtonControl = createControl(xMCF, xContext, toolkit, windowPeer, "Button", X_BACK_BUTTON, 2*7, 2*24, 2*14);
        XControl increaseFontSizeButtonControl = createControl(xMCF, xContext, toolkit, windowPeer, "Button", 2*1, 2*30, 2*16, 2*14);
        XControl decreaseFontSizeButtonControl = createControl(xMCF, xContext, toolkit, windowPeer, "Button", 2*17, 2*30, 2*16, 2*14);
        XControl volumesListBoxControl = createControl(xMCF, xContext, toolkit, windowPeer, "ListBox", X_VOLUMES_LISTBOX, 2*8, 2*69, 2*12);
        XControl sectionsListBoxControl = createControl(xMCF, xContext, toolkit, windowPeer, "ListBox", X_SECTIONS_LISTBOX, 2*8, 2*68, 2*12);
        XControl pagesListBoxControl = createControl(xMCF, xContext, toolkit, windowPeer, "ListBox", X_PAGES_LISTBOX, 2*8, 2*46, 2*12);
        XControl charsetListBoxControl = createControl(xMCF, xContext, toolkit, windowPeer, "ListBox", X_CHARSET_LISTBOX, 2*30, 2*38, 2*12);
        XControl previewFieldControl = createControl(xMCF, xContext, toolkit, windowPeer, "Edit", 0, Y_PREVIEW_FIELD, MIN_WIDTH, MIN_HEIGHT - Y_PREVIEW_FIELD);

        nextButton = (XButton)UnoRuntime.queryInterface(XButton.class, nextButtonControl);
        backButton = (XButton)UnoRuntime.queryInterface(XButton.class, backButtonControl);
        increaseFontSizeButton = (XButton)UnoRuntime.queryInterface(XButton.class, increaseFontSizeButtonControl);
        decreaseFontSizeButton = (XButton)UnoRuntime.queryInterface(XButton.class, decreaseFontSizeButtonControl);
        volumesListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, volumesListBoxControl);
        sectionsListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, sectionsListBoxControl);
        pagesListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, pagesListBoxControl);
        charsetListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, charsetListBoxControl);
        previewField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, previewFieldControl);

        nextButtonWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, nextButtonControl);
        backButtonWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, backButtonControl);
        volumesListBoxWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, volumesListBoxControl);
        sectionsListBoxWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, sectionsListBoxControl);
        pagesListBoxWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, pagesListBoxControl);
        charsetListBoxWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, charsetListBoxControl);
        previewFieldWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, previewFieldControl);

        nextButtonProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, nextButtonControl.getModel());
        backButtonProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, backButtonControl.getModel());
        increaseFontSizeButtonProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, increaseFontSizeButtonControl.getModel());
        decreaseFontSizeButtonProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, decreaseFontSizeButtonControl.getModel());
        previewFieldProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, previewFieldControl.getModel());

        nextButtonProperties.setPropertyValue("Label", "\u003E");
        backButtonProperties.setPropertyValue("Label", "\u003C");
        increaseFontSizeButtonProperties.setPropertyValue("Label", "+");
        decreaseFontSizeButtonProperties.setPropertyValue("Label", "-");
        
        font = (FontDescriptor)AnyConverter.toObject(FontDescriptor.class, previewFieldProperties.getPropertyValue("FontDescriptor"));
        font.Name   = FONT_DOTS;
        font.Height = fontSize;
        previewFieldProperties.setPropertyValue("FontDescriptor", font);

        logger.exiting("PreviewDialog", "buildDialog");

    }

    private XWindowPeer createWindow(XToolkit toolkit,
                                     XWindowPeer parent,
                                     WindowClass type,
                                     String service,
                                     int attrs,
                                     int x,
                                     int y,
                                     int width,
                                     int height) {

        try {

            Rectangle rectangle = new Rectangle();
            rectangle.X = x;
            rectangle.Y = y;
            rectangle.Width = width;
            rectangle.Height = height;

            WindowDescriptor aWindowDescriptor = new WindowDescriptor();
            aWindowDescriptor.Type = type;
            aWindowDescriptor.WindowServiceName = service;
            aWindowDescriptor.ParentIndex = -1;
            aWindowDescriptor.Bounds = rectangle;
            aWindowDescriptor.Parent = parent;
            aWindowDescriptor.WindowAttributes = attrs;

            return toolkit.createWindow(aWindowDescriptor);

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private XControl createControl(XMultiComponentFactory xMCF,
                                   XComponentContext xContext,
                                   XToolkit toolkit,
                                   XWindowPeer windowPeer,
                                   String type,
                                   int x,
                                   int y,
                                   int height,
                                   int width) {

        try {

            XControl control = (XControl)UnoRuntime.queryInterface(
                                XControl.class, xMCF.createInstanceWithContext("com.sun.star.awt.UnoControl" + type, xContext));
            XControlModel controlModel = (XControlModel)UnoRuntime.queryInterface(
                                          XControlModel.class, xMCF.createInstanceWithContext("com.sun.star.awt.UnoControl" + type + "Model", xContext));
            control.setModel(controlModel);
            XMultiPropertySet properties = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, control.getModel());
            if (type.equals("ListBox")) {                
                properties.setPropertyValues(new String[] { "Dropdown",        "LineCount" },
                                             new Object[] { new Boolean(true), new Short((short)20)});
            } else if (type.equals("Edit")){
                properties.setPropertyValues(new String[] { "AutoHScroll",     "AutoVScroll",     "HScroll",         "MultiLine",       "ReadOnly",        "VScroll"},
                                             new Object[] { new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
            }
            control.createPeer(toolkit, windowPeer);
            XWindow controlWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, control);
            controlWindow.setPosSize(x, y, height, width, PosSize.POSSIZE);

            return control;

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void execute() throws com.sun.star.uno.Exception,
                                 TransformerException {

        logger.entering("PreviewDialog", "execute");

        buildDialog();

        volume = 1;
        section = 1;
        page = 1;
        charset = (short)0;

        charsetListBox.addItem("Dots", (short)0);
        if (table != null) {
            charsetListBox.addItem("Text", (short)1);
        }
        charsetListBox.selectItemPos(charset, true);

        updateVolumesListBox();
        updateSectionsListBox();
        updatePagesListBox();
        updateButtons();

        sectionsListBox.addItemListener(this);
        pagesListBox.addItemListener(this);
        charsetListBox.addItemListener(this);
        backButton.addActionListener(this);
        nextButton.addActionListener(this);
        increaseFontSizeButton.addActionListener(this);
        decreaseFontSizeButton.addActionListener(this);

        showPage();

        logger.exiting("PreviewDialog", "execute");

    }

    public void dispose() {}

    private void showPage() throws TransformerException {

        String previewText = "";
        String line = null;
        Node row = null;
        NodeList rows = XPathAPI.selectNodeList(root,
                "/pef/body/volume[" + volume + "]/section[" + section + "]/page[" + page + "]/row");

        int width = cellsPerLine + marginInner + marginOuter;
        int height = linesPerPage + marginTop + marginBottom;
        int offset = ((page % 2 == 0) && duplex) ? marginOuter : marginInner;

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
            if (charset == (short)1) {
                line = table.toText(line);
            }
            previewText += line;
            x += line.length();
            while (x < offset + cellsPerLine) {
                if (charset == (short)1) {
                    previewText += " ";
                } else {
                    previewText += "\u2800";
                }
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
            firstpage = beginningBraillePageNumber +
                    Integer.parseInt(XPathAPI.eval(root,
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
        decreaseFontSizeButtonProperties.setPropertyValue("Enabled", fontSize > MIN_FONTSIZE);
        increaseFontSizeButtonProperties.setPropertyValue("Enabled", fontSize < MAX_FONTSIZE);

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

            } else if (source.equals(increaseFontSizeButton)) {

                if (fontSize < MAX_FONTSIZE) {
                    fontSize ++;
                    font.Height = (short)fontSize;
                    previewFieldProperties.setPropertyValue("FontDescriptor", font);
                    updateButtons();
                }

            } else if (source.equals(decreaseFontSizeButton)) {

                if (fontSize > MIN_FONTSIZE) {
                    fontSize --;
                    font.Height = (short)fontSize;
                    previewFieldProperties.setPropertyValue("FontDescriptor", font);
                    updateButtons();
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
            } else if (source.equals(charsetListBox)) {
                charset = charsetListBox.getSelectedItemPos();
                previewFieldProperties.setPropertyValue("FontDescriptor", font);
                showPage();
            }

        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void disposing(EventObject event) { // frame.dispose() veroorzaakt RuntimeException

//        if (frame != null) {
//            frame.dispose();
//        }
    }

    public void windowResized(WindowEvent event) {

        Rectangle windowRectangle = window.getPosSize();
        
        int newWidth = Math.max(windowRectangle.Width, MIN_WIDTH);
        int newHeight = Math.max(windowRectangle.Height, MIN_HEIGHT);
        int xOffset = (newWidth - MIN_WIDTH) / 2;

        backButtonWindow.setPosSize(X_BACK_BUTTON + xOffset, 0, 0, 0, PosSize.X);
        volumesListBoxWindow.setPosSize(X_VOLUMES_LISTBOX + xOffset, 0, 0, 0, PosSize.X);
        sectionsListBoxWindow.setPosSize(X_SECTIONS_LISTBOX + xOffset, 0, 0, 0, PosSize.X);
        pagesListBoxWindow.setPosSize(X_PAGES_LISTBOX + xOffset, 0, 0, 0, PosSize.X);
        charsetListBoxWindow.setPosSize(X_CHARSET_LISTBOX + 2*xOffset, 0, 0, 0, PosSize.X);
        nextButtonWindow.setPosSize(X_NEXT_BUTTON + xOffset, 0, 0, 0, PosSize.X);
        previewFieldWindow.setPosSize(0, 0, newWidth, newHeight - Y_PREVIEW_FIELD, PosSize.SIZE);
        window.setPosSize(0, 0, newWidth, newHeight, PosSize.SIZE);

    }

    public void windowMoved(WindowEvent event) {}
    public void windowShown(EventObject event) {}
    public void windowHidden(EventObject event) {}
    
}