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

package be.docarch.odt2braille.ooo.dialog;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.PEF;
import be.docarch.odt2braille.ooo.UnoUtils;
import be.docarch.odt2braille.setup.PEFConfiguration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.setup.EmbossConfiguration;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.Configuration.PageNumberFormat;
import be.docarch.odt2braille.utils.NumberFormatter;

import java.util.Locale;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.xpath.XPathAPI;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.BrailleConverter;

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

/**
 *
 * @author Bert Frees
 */
public class PreviewDialog implements XItemListener,
                                      XActionListener,
                                      XWindowListener {

    private final static Logger logger = Constants.getLogger();
    private final static String OOO_L10N = Constants.OOO_L10N_PATH;

    private static final String FONT_6_DOT = "odt2braille 6 dot";
    private static final String FONT_8_DOT = "odt2braille 8 dot";
    private static String FONT_DOTS;

    private static final short DEFAULT_FONT_SIZE = 18;
    private static final short MIN_FONTSIZE = 10;
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
    private boolean duplex;
    private PageNumberFormat preliminaryPageFormat;
    private BrailleConverter table;
    private short charset;

    private final List<PEF.Volume> volumes;
    private List<PEF.Section> sections = null;

    private int currentVolume;
    private int currentSection;
    private int currentPage;

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


    public PreviewDialog(XComponentContext xContext,
                         PEF pef,
                         Configuration settings,
                         PEFConfiguration pefSettings)
                  throws ParserConfigurationException,
                         SAXException,
                         IOException {

        this.xContext = xContext;
        this.fontSize = DEFAULT_FONT_SIZE;

        cellsPerLine = pefSettings.getColumns();
        linesPerPage = pefSettings.getRows();

        duplex = pefSettings.getDuplex();
        FONT_DOTS = pefSettings.getEightDots() ? FONT_8_DOT : FONT_6_DOT;

        marginInner = 0;
        marginOuter = 0;
        marginTop = 0;
        marginBottom = 0;

        if (pefSettings instanceof ExportConfiguration) {

            String id = ((ExportConfiguration)pefSettings).getFileFormat().getIdentifier();
            if (id.equals(ExportConfiguration.BRF) ||
                id.equals(ExportConfiguration.BRA)) {
                Table t = ((ExportConfiguration)pefSettings).getCharSet();
                if (t != null) {
                    table = t.newBrailleConverter();
                }
            }

        } else if (pefSettings instanceof EmbossConfiguration) {

            EmbossConfiguration.MarginSettings margins = ((EmbossConfiguration)pefSettings).getMargins();

            marginInner = margins.getInner();
            marginOuter = margins.getOuter();
            marginTop = margins.getTop();
            marginBottom = margins.getBottom();
        }

        preliminaryPageFormat = settings.getPreliminaryPageNumberFormat();

        volumes = pef.getVolumes();

        // L10N

        Locale oooLocale;
            try { oooLocale = UnoUtils.getUILocale(xContext); } catch (Exception e) {
                  oooLocale = Locale.ENGLISH; }

        ResourceBundle bundle = ResourceBundle.getBundle(OOO_L10N, oooLocale);

        L10N_windowTitle = bundle.getString("previewDialogTitle");

        // Load PEF file
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        docBuilder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId,
                                             String systemId)
                                      throws SAXException,
                                             IOException {
                return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
            }
        });
        Document contentDoc = docBuilder.parse(pef.getSinglePEF().getAbsolutePath());
        root = contentDoc.getDocumentElement();

    }

    private void buildDialog() throws com.sun.star.uno.Exception {

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

        int windowWidth = MIN_WIDTH;
        int windowHeight = MIN_HEIGHT;

        XWindowPeer windowPeer = createWindow(toolkit, parentWindowPeer, 2*100, 2*100, windowWidth, windowHeight);

        window = (XWindow)UnoRuntime.queryInterface(XWindow.class, windowPeer);
        frame = (XFrame)UnoRuntime.queryInterface(XFrame.class, xMCF.createInstanceWithContext("com.sun.star.frame.Frame", xContext));
        frame.initialize(window);
        window.addWindowListener(this);

        Rectangle windowRectangle = window.getPosSize();
        windowWidth = windowRectangle.Width;
        windowHeight = windowRectangle.Height;

        XControl nextButtonControl = createControl(xMCF, xContext, toolkit, windowPeer, "Button", X_NEXT_BUTTON, 2*7, 2*24, 2*14);
        XControl backButtonControl = createControl(xMCF, xContext, toolkit, windowPeer, "Button", X_BACK_BUTTON, 2*7, 2*24, 2*14);
        XControl increaseFontSizeButtonControl = createControl(xMCF, xContext, toolkit, windowPeer, "Button", 2*1, 2*30, 2*16, 2*14);
        XControl decreaseFontSizeButtonControl = createControl(xMCF, xContext, toolkit, windowPeer, "Button", 2*17, 2*30, 2*16, 2*14);
        XControl volumesListBoxControl = createControl(xMCF, xContext, toolkit, windowPeer, "ListBox", X_VOLUMES_LISTBOX, 2*8, 2*69, 2*12);
        XControl sectionsListBoxControl = createControl(xMCF, xContext, toolkit, windowPeer, "ListBox", X_SECTIONS_LISTBOX, 2*8, 2*68, 2*12);
        XControl pagesListBoxControl = createControl(xMCF, xContext, toolkit, windowPeer, "ListBox", X_PAGES_LISTBOX, 2*8, 2*46, 2*12);
        XControl previewFieldControl = createControl(xMCF, xContext, toolkit, windowPeer, "Edit", 0, Y_PREVIEW_FIELD, windowWidth, windowHeight - Y_PREVIEW_FIELD);

        nextButton = (XButton)UnoRuntime.queryInterface(XButton.class, nextButtonControl);
        backButton = (XButton)UnoRuntime.queryInterface(XButton.class, backButtonControl);
        increaseFontSizeButton = (XButton)UnoRuntime.queryInterface(XButton.class, increaseFontSizeButtonControl);
        decreaseFontSizeButton = (XButton)UnoRuntime.queryInterface(XButton.class, decreaseFontSizeButtonControl);
        volumesListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, volumesListBoxControl);
        sectionsListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, sectionsListBoxControl);
        pagesListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, pagesListBoxControl);
        previewField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, previewFieldControl);

        nextButtonWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, nextButtonControl);
        backButtonWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, backButtonControl);
        volumesListBoxWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, volumesListBoxControl);
        sectionsListBoxWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, sectionsListBoxControl);
        pagesListBoxWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, pagesListBoxControl);
        previewFieldWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, previewFieldControl);

        nextButtonProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, nextButtonControl.getModel());
        backButtonProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, backButtonControl.getModel());
        increaseFontSizeButtonProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, increaseFontSizeButtonControl.getModel());
        decreaseFontSizeButtonProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, decreaseFontSizeButtonControl.getModel());
        previewFieldProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, previewFieldControl.getModel());
        
        if (table != null) {
            XControl charsetListBoxControl = createControl(xMCF, xContext, toolkit, windowPeer, "ListBox", X_CHARSET_LISTBOX, 2*30, 2*38, 2*12);
            charsetListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, charsetListBoxControl);
            charsetListBoxWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, charsetListBoxControl);
        }
        
        nextButtonProperties.setPropertyValue("Label", "\u003E");
        backButtonProperties.setPropertyValue("Label", "\u003C");
        increaseFontSizeButtonProperties.setPropertyValue("Label", "+");
        decreaseFontSizeButtonProperties.setPropertyValue("Label", "-");
        
        font = (FontDescriptor)AnyConverter.toObject(FontDescriptor.class, previewFieldProperties.getPropertyValue("FontDescriptor"));
        font.Height = fontSize;
        previewFieldProperties.setPropertyValue("FontDescriptor", font);
        font.Name   = FONT_DOTS;
        previewFieldProperties.setPropertyValue("FontDescriptor", font);

    }

    private XWindowPeer createWindow(XToolkit toolkit,
                                     XWindowPeer parent,
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
            aWindowDescriptor.Type = WindowClass.SIMPLE;
            aWindowDescriptor.WindowServiceName = "dialog";
            aWindowDescriptor.ParentIndex = -1;
            aWindowDescriptor.Bounds = rectangle;
            aWindowDescriptor.Parent = parent;
            aWindowDescriptor.WindowAttributes = WindowAttribute.CLOSEABLE +
                                                 WindowAttribute.BORDER +
                                                 WindowAttribute.SIZEABLE +
                                                 WindowAttribute.MOVEABLE +
                                                 WindowAttribute.SHOW;

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

        buildDialog();
        
        currentVolume = 1;
        currentSection = 1;
        currentPage = 1;
        charset = (short)0;
        
        sections = volumes.get(currentVolume-1).getSections();

        updateVolumesListBox();
        updateSectionsListBox();
        updatePagesListBox();
        updateButtons();

        sectionsListBox.addItemListener(this);
        pagesListBox.addItemListener(this);
        backButton.addActionListener(this);
        nextButton.addActionListener(this);
        increaseFontSizeButton.addActionListener(this);
        decreaseFontSizeButton.addActionListener(this);

        if (charsetListBox != null) {
            charsetListBox.addItem("Dots", (short)0);
            charsetListBox.addItem("Text", (short)1);
            charsetListBox.selectItemPos(charset, true);
            charsetListBox.addItemListener(this);
        }

        showPage();

    }

    public void dispose() {}

    private void showPage() throws TransformerException {

        String previewText = "";
        String line = null;
        Node row = null;
        
        //NodeList rows = XPathAPI.selectNodeList(sections.get(currentSection-1).getDOMElement(), "page[" + currentPage + "]/row"); WAAROM WERKT DIT NIET?
        NodeList rows = XPathAPI.selectNodeList(root, "/pef/body/volume[" + currentVolume + "]/section[" + currentSection + "]/page[" + currentPage + "]/row");
        
        int width = cellsPerLine + marginInner + marginOuter;
        int height = linesPerPage + marginTop + marginBottom;
        int offset = ((currentPage % 2 == 0) && duplex) ? marginOuter : marginInner;

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

        volumesListBox.removeItemListener(this);
        volumeCount = volumes.size();

        for (int i=0;i<volumeCount;i++) {
            volumesListBox.addItem(volumes.get(i).getName(), (short)i);
        }

        volumesListBox.selectItemPos((short)(currentVolume-1), true);
        volumesListBox.addItemListener(this);
    }

    private void updateSectionsListBox() throws TransformerException {

        sectionsListBox.removeItemListener(this);
        sectionsListBox.removeItems((short)0, Short.MAX_VALUE);
        sectionCount = sections.size();
        
        for (int i=0;i<sectionCount;i++) {
            sectionsListBox.addItem(sections.get(i).getName(), (short)i);
        }

        sectionsListBox.selectItemPos((short)(currentSection-1), true);
        sectionsListBox.addItemListener(this);

    }

    private void updatePagesListBox() throws TransformerException {

        String pag = null;
        pagesListBox.removeItemListener(this);
        pagesListBox.removeItems((short)0, Short.MAX_VALUE);
        
        //pageCount = XPathAPI.(sections.get(currentSection-1).getDOMElement(), "page").getLength(); WAAROM WERKT DIT NIET?
        pageCount = XPathAPI.selectNodeList(root, "/pef/body/volume[" + currentVolume + "]/section[" + currentSection + "]/page").getLength();
        
        PEF.Volume v = volumes.get(currentVolume-1);
        PageNumberFormat format = sections.get(currentSection-1).getPageNumberFormat();
        int firstpage = sections.get(currentSection-1).getFirstPageNumber();

        for (int i=0; i<pageCount;i++) {
            switch (format) {
                case P:
                    pag = "p" + (firstpage + i);
                    break;
                case ROMAN:
                    pag = NumberFormatter.format(firstpage + i, "i");
                    break;
                default:
                    pag = NumberFormatter.format(firstpage + i, "1");
            }
            pagesListBox.addItem(pag, (short)i);
        }

        pagesListBox.selectItemPos((short)(currentPage-1), true);
        pagesListBox.addItemListener(this);

    }

    private void updateButtons() throws com.sun.star.uno.Exception,
                                        TransformerException {

        backButtonProperties.setPropertyValue("Enabled",
        //        XPathAPI.eval(sections.get(currentSection-1).getDOMElement(), "page[" + currentPage + "]/preceding::page").bool());
	            XPathAPI.eval(root, "/pef/body/volume[" + currentVolume + "]/section[" + currentSection + "]/page[" + currentPage + "]/preceding::page").bool());

        nextButtonProperties.setPropertyValue("Enabled",
        //        XPathAPI.eval(sections.get(currentSection-1).getDOMElement(), "page[" + currentPage + "]/following::page").bool());
                XPathAPI.eval(root, "/pef/body/volume[" + currentVolume + "]/section[" + currentSection + "]/page[" + currentPage + "]/following::page").bool());

        decreaseFontSizeButtonProperties.setPropertyValue("Enabled", fontSize > MIN_FONTSIZE);
        increaseFontSizeButtonProperties.setPropertyValue("Enabled", fontSize < MAX_FONTSIZE);

    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        Object source = actionEvent.Source;

        try {

            if (source.equals(backButton)) {

                if (currentPage > 1) {
                    currentPage--;
                    pagesListBox.selectItemPos((short)(currentPage-1), true);
                    updateButtons();
                    showPage();
                } else if (currentSection > 1) {
                    currentSection--;
                    sectionsListBox.selectItemPos((short)(currentSection-1), true);
                    updatePagesListBox();
                    currentPage = pageCount;
                    pagesListBox.selectItemPos((short)(currentPage-1), true);
                    updateButtons();
                    showPage();
                } else if (currentVolume > 1) {
                    currentVolume--;
                    volumesListBox.selectItemPos((short)(currentVolume-1), true);
                    sections = volumes.get(currentVolume-1).getSections();
                    updateSectionsListBox();
                    currentSection = sectionCount;
                    sectionsListBox.selectItemPos((short)(currentSection-1), true);
                    updatePagesListBox();
                    currentPage = pageCount;
                    pagesListBox.selectItemPos((short)(currentPage-1), true);
                    updateButtons();
                    showPage();
                }

            } else if (source.equals(nextButton)) {

                if (currentPage < pageCount) {
                    currentPage++;
                    pagesListBox.selectItemPos((short)(currentPage-1), true);
                    updateButtons();
                    showPage();
                } else if (currentSection < sectionCount) {
                    currentSection++;
                    sectionsListBox.selectItemPos((short)(currentSection-1), true);
                    updatePagesListBox();
                    currentPage = 1;
                    pagesListBox.selectItemPos((short)(currentPage-1), true);
                    updateButtons();
                    showPage();
                } else if (currentVolume < volumeCount) {
                    currentVolume++;
                    volumesListBox.selectItemPos((short)(currentVolume-1), true);
                    sections = volumes.get(currentVolume-1).getSections();
                    updateSectionsListBox();
                    currentSection = 1;
                    sectionsListBox.selectItemPos((short)(currentSection-1), true);
                    updatePagesListBox();
                    currentPage = 1;
                    pagesListBox.selectItemPos((short)(currentPage-1), true);
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

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {

        Object source = itemEvent.Source;

        try {

            if (source.equals(volumesListBox)) {
                currentVolume = volumesListBox.getSelectedItemPos() + 1;
                sections = volumes.get(currentVolume-1).getSections();
                currentSection = 1;
                updateSectionsListBox();
                currentPage = 1;
                updatePagesListBox();
                updateButtons();
                showPage();
            } else if (source.equals(sectionsListBox)) {
                currentSection = sectionsListBox.getSelectedItemPos() + 1;
                currentPage = 1;
                updatePagesListBox();
                updateButtons();
                showPage();
            } else if (source.equals(pagesListBox)) {
                currentPage = pagesListBox.getSelectedItemPos() + 1;
                updateButtons();
                showPage();
            } else if (charsetListBox != null && source.equals(charsetListBox)) {
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

    @Override
    public void disposing(EventObject event) { // frame.dispose() veroorzaakt RuntimeException

//        if (frame != null) {
//            frame.dispose();
//        }
    }

    @Override
    public void windowResized(WindowEvent event) {

        Rectangle windowRectangle = window.getPosSize();
        
        int newWidth = Math.max(windowRectangle.Width, MIN_WIDTH);
        int newHeight = Math.max(windowRectangle.Height, MIN_HEIGHT);
        int xOffset = (newWidth - MIN_WIDTH) / 2;

        backButtonWindow.setPosSize(X_BACK_BUTTON + xOffset, 0, 0, 0, PosSize.X);
        volumesListBoxWindow.setPosSize(X_VOLUMES_LISTBOX + xOffset, 0, 0, 0, PosSize.X);
        sectionsListBoxWindow.setPosSize(X_SECTIONS_LISTBOX + xOffset, 0, 0, 0, PosSize.X);
        pagesListBoxWindow.setPosSize(X_PAGES_LISTBOX + xOffset, 0, 0, 0, PosSize.X);
        nextButtonWindow.setPosSize(X_NEXT_BUTTON + xOffset, 0, 0, 0, PosSize.X);
        previewFieldWindow.setPosSize(0, 0, newWidth, newHeight - Y_PREVIEW_FIELD, PosSize.SIZE);
        window.setPosSize(0, 0, newWidth, newHeight, PosSize.SIZE);
        
        if (charsetListBoxWindow != null) {
            charsetListBoxWindow.setPosSize(X_CHARSET_LISTBOX + 2*xOffset, 0, 0, 0, PosSize.X);
        }
    }

    @Override
    public void windowMoved(WindowEvent event) {}
    @Override
    public void windowShown(EventObject event) {}
    @Override
    public void windowHidden(EventObject event) {}
    
}
