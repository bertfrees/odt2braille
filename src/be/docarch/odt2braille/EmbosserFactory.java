package be.docarch.odt2braille;

import java.io.OutputStream;

import org_pef_text.TableFactory;
import org_pef_text.pef2text.AbstractEmbosser;
import org_pef_text.pef2text.ConfigurableEmbosser;
import org_pef_text.pef2text.EmbosserFactoryException;
import org_pef_text.pef2text.LineBreaks;
import org_pef_text.pef2text.LinePreamble;
import org_pef_text.pef2text.Paper;
import org_pef_text.pef2text.Paper.PaperSize;
import org_pef_text.pef2text.UnsupportedPaperException;

/**
 * 
 * The embosser factory can build settings for the
 * EmbosserTypes.
 * 
 * @author  Joel Hakansson, TPB
 * @version 10 okt 2008
 * @since 1.0
 */
public class EmbosserFactory extends org_pef_text.pef2text.EmbosserFactory {

	private Settings settings;
        private int numberOfCopies;
        private int pageCount;
        private Paper paper;
	
	public EmbosserFactory(Settings setting) {
		this.settings = setting;
                if (setting.getPaperSize() == PaperSize.CUSTOM) {
                    paper = new Paper(setting.getPaperWidth(), setting.getPaperHeight());
                } else {
                    paper = Paper.newPaper(setting.getPaperSize());
                }
                numberOfCopies = 1;
                pageCount = 0;
	}

        public void setNumberOfCopies(int numberOfCopies) {
            this.numberOfCopies = numberOfCopies;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }
	
	@Override
        public AbstractEmbosser newEmbosser(OutputStream os) throws UnsupportedPaperException, EmbosserFactoryException {
		ConfigurableEmbosser.Builder b;
		TableFactory btb = new TableFactory();
                btb.setTable(settings.getTable());
                switch (settings.getEmbosser()) {
                        case NONE:
                            b = new ConfigurableEmbosser.Builder(os, btb.newTable())
                                .breaks(LineBreaks.Type.DOS)
                                .padNewline(ConfigurableEmbosser.Padding.NONE)
                                .supportsDuplex(settings.getDuplex())
                                .supports8dot(false)
                                .supportsAligning(true)
                                .setPaper(paper);
                            return b.build();

                        case INDEX_BASIC_BLUE_BAR:
                            b = new ConfigurableEmbosser.Builder(os, btb.newTable())
                                .breaks(LineBreaks.Type.DOS)
                                .padNewline(ConfigurableEmbosser.Padding.NONE)
                                .supportsDuplex(false)
                                .supportsAligning(true)
                                .setPaper(paper)
                                .linePreamble(new LinePreamble() {
                                    public byte[] getBytes(int lineLength) {
                                        if (lineLength == 0) {
                                            return new byte[0];
                                        } else {
                                            return new byte[]{ 0x1b, 0x5c, (byte)lineLength, 0x00 };
                                        }
                                    }
                                 });
                            return b.build();

                        case INDEX_BASIC_S_V2:
                        case INDEX_BASIC_D_V2:
                        case INDEX_EVEREST_D_V2:
                        case INDEX_4X4_PRO_V2:
                            if (numberOfCopies > 999 || numberOfCopies < 1) {
                                throw new EmbosserFactoryException("Invalid number of copies: " + numberOfCopies + " is not in [1, 999]");
                            }
                            if (settings.getSaddleStitch() && pageCount > 200) {
                                throw new EmbosserFactoryException("Number of pages = " + pageCount +  "; cannot exceed 200 when in magazine style mode");
                            }
                            b = new ConfigurableEmbosser.Builder(os, btb.newTable())
                                .breaks(LineBreaks.Type.DOS)
				.padNewline(ConfigurableEmbosser.Padding.NONE)
				.supportsDuplex(settings.getDuplex())
				.supportsAligning(true)
                                .supports8dot(true)
                                .setWidth(settings.getCellsInWidth())
                                .setHeight(settings.getLinesInHeight())
				.header(getIndexV2Header(settings, numberOfCopies))
                                .footer(new byte[]{0x1a})
                                .linePreamble(new LinePreamble() {
                                    public byte[] getBytes(int lineLength) {
                                        if (lineLength == 0) {
                                            return new byte[0];
                                        } else {
                                            return new byte[]{ 0x1b, 0x5c, (byte)lineLength, 0x00 };
                                        }
                                    }
                                });
                            return b.build();

                        case INDEX_EVEREST_D_V3:
                        case INDEX_BASIC_D_V3:
                        case INDEX_4X4_PRO_V3:
                        case INDEX_4WAVES_PRO_V3:
                            if (numberOfCopies > 10000 || numberOfCopies < 1) {
                                throw new EmbosserFactoryException("Invalid number of copies: " + numberOfCopies + " is not in [1, 10000]");
                            }
                            b = new ConfigurableEmbosser.Builder(os, btb.newTable())
                                .breaks(LineBreaks.Type.DOS)
                                .padNewline(ConfigurableEmbosser.Padding.NONE)
                                .supportsDuplex(settings.getDuplex())
                                .supportsAligning(true)
                                .supports8dot(true)
                                .setWidth(settings.getCellsPerLine())
                                .setHeight(settings.getLinesPerPage())
                                .header(getIndexV3Header(settings, numberOfCopies));
                            if (settings.getEightDots()) {
                                 b = b.linePreamble(new LinePreamble() {
                                    public byte[] getBytes(int lineLength) {
                                        if (lineLength == 0) {
                                            return new byte[0];
                                        } else {
                                            return new byte[]{ 0x1b, 0x5c, (byte)lineLength, 0x00 };
                                        }
                                    }
                                 });
                            }
                            return b.build();

                        case IMPACTO_TEXTO:
                        case IMPACTO_600:
                            if (numberOfCopies > 32767 || numberOfCopies < 1) {
                                throw new EmbosserFactoryException("Invalid number of copies: " + numberOfCopies + " is not in [1, 32767]");
                            }
                            b = new ConfigurableEmbosser.Builder(os, btb.newTable())
                                .breaks(LineBreaks.Type.IMPACTO)
                                .padNewline(ConfigurableEmbosser.Padding.NONE)
                                .supportsDuplex(settings.getDuplex())
                                .supportsAligning(true)
                                .supports8dot(false)
                                .setWidth(settings.getCellsInWidth())
                                .setHeight(settings.getLinesInHeight())
                                .header(getImpactoHeader(settings, pageCount, numberOfCopies))
                                .footer(new byte[]{0x1b,0x54});
                            return b.build();

                        case PORTATHIEL_BLUE:
                            b = new ConfigurableEmbosser.Builder(os, btb.newTable())
                                .breaks(LineBreaks.Type.PORTATHIEL)
                                .padNewline(ConfigurableEmbosser.Padding.NONE)
                                .supportsDuplex(settings.getDuplex())
                                .supportsAligning(true)
                                .supports8dot(false)
                                .setWidth(settings.getCellsInWidth())
                                .setHeight(settings.getLinesInHeight())
                                .header(getPortathielHeader(settings))
                                .footer(new byte[]{0x1b,0x54});
                            return b.build();

			case BRAILLO_200: case BRAILLO_400_S: case BRAILLO_400_SR:
                            b = new ConfigurableEmbosser.Builder(os, btb.newTable())
                                    .breaks(LineBreaks.Type.DOS)
                                    .padNewline(ConfigurableEmbosser.Padding.BEFORE)
                                    .supportsDuplex(true)
                                    .supportsAligning(true)
                                    .setPaper(paper);
                            b.header(getBrailloHeader(b.getWidth(), paper));
                            return b.build();

		}
		throw new IllegalArgumentException("Cannot find embosser type " + settings.getEmbosser());
	}

        private static byte[] getIndexV2Header(Settings settings,
                                               int numberOfCopies)
                                        throws EmbosserFactoryException {

            boolean eightDots = settings.getEightDots();
            boolean duplex = settings.getDuplex();
            boolean saddleStitch = settings.getSaddleStitch();
            boolean zFolding = settings.getZFolding();
            int cellsInWidth = settings.getCellsInWidth();

            StringBuffer header = new StringBuffer();

            header.append((char)0x1b);
            header.append((char)0x0f);
            header.append((char)0x02);
            header.append("x,");                            // 0: Activated braille code
            header.append("0,");                            // 1: Type of braille code      = Computer
            header.append("1,");                            // 2: 6/8 dot braille           = 8 dot
            header.append("x,");                            // 3: Capital prefix
            header.append("x,");                            // 4: Baud rate
            header.append("x,");                            // 5: Number of data bits
            header.append("x,");                            // 6: Parity
            header.append("x,");                            // 7: Number of stop bits
            header.append("x,");                            // 8: Handshake
            byte[] w = toBytes(cellsInWidth-23, 2);
            header.append((char)w[0]);
            header.append((char)w[1]);                      // 9: Characters per line
            header.append(",");
            header.append("0,");                            // 10: Left margin              = 0 characters
            header.append("0,");                            // 11: Binding margin           = 0 characters
            header.append("0,");                            // 12: Top margin               = 0 lines
            header.append("0,");                            // 13: Bottom margin            = 0 lines
            header.append(eightDots?'4':'0');               // 14: Line spacing             = 2.5 mm (6 dot) or 5 mm (8 dot)
            header.append(",");            
            if (saddleStitch) { header.append('4'); } else
            if (zFolding)     { header.append('3'); } else
            if (duplex)       { header.append('2'); } else
                              { header.append('1'); }       // 15: Page mode                = 1,2 or 4 pages per sheet or z-folding (3)
            header.append(",");
            header.append("0,");                            // 16: Print mode               = normal
            header.append("0,");                            // 17: Page number              = off
            header.append("x,");                            // 18: N/A
            header.append("0,");                            // 19: Word wrap                = off
            header.append("1,");                            // 20: Auto line feed           = on
            header.append("x,");                            // 21: Form feed
            header.append("x,");                            // 22: Volume
            header.append("x,");                            // 23: Impact level
            header.append("x,");                            // 24: Delay
            header.append("x,");                            // 25: Print quality
            header.append("x,");                            // 26: Graphic dot distance
            header.append("0,");                            // 27: Text dot distance        = normal (2.5 mm)
            header.append("x,");                            // 28: Setup
            header.append("x,x,x,x,x,x,x,x,x,x,x");         // 29-39: N/A
            header.append((char)0x1b);
            header.append((char)0x0f);

            if (numberOfCopies > 1) {
                header.append((char)0x1b);
                header.append((char)0x12);
                byte[] c = toBytes(numberOfCopies, 3);
                header.append((char)c[0]);
                header.append((char)c[1]);
                header.append((char)c[2]);
                header.append((char)0x1b);
                header.append((char)0x12);
            }
            
            return header.toString().getBytes();
            
	}

        private static byte[] getIndexV3Header(Settings settings,
                                               int numberOfCopies) {

            EmbosserType type = settings.getEmbosser();
            boolean eightDots = settings.getEightDots();
            boolean duplex = settings.getDuplex();
            boolean saddleStitch = settings.getSaddleStitch();
            boolean zFolding = settings.getZFolding();
            double paperWidth = settings.getPaperWidth();
            double paperLenght = settings.getPaperHeight();
            int cellsInWidth = settings.getCellsInWidth();
            int marginInner = settings.getMarginInner();
            int marginOuter = settings.getMarginOuter();
            int marginTop = settings.getMarginTop();

            byte[] xx;
            byte y;
            double iPart;
            double fPart;

            StringBuffer header = new StringBuffer();

            header.append((char)0x1b);
            header.append("D");                                         // Activate temporary formatting properties of a document
            header.append(",BT0");                                      // Default braille table
            header.append("TD0");                                       // Text dot distance = 2.5 mm
            header.append(",LS");
            header.append(eightDots?'4':'0');                           // Line spacing = 2.5 mm or 5 mm
            header.append(",DP");
            if (saddleStitch)       { header.append('4'); } else
            if (zFolding && duplex) { header.append('3'); } else
            if (zFolding)           { header.append('5'); } else
            if (duplex)             { header.append('2'); } else
                                    { header.append('1'); }             // Page mode
            if (numberOfCopies > 1) {
                header.append(",MC");
                header.append(String.valueOf(numberOfCopies));          // Multiple copies
            }
            //header.append(",MI1");                                    // Multiple impact = 1
            header.append(",PN0");                                      // No page number
            switch (type) {
                case INDEX_BASIC_D_V3:
                    iPart = Math.floor(paperLenght/Paper.INCH_IN_MM);
                    fPart = (paperLenght/Paper.INCH_IN_MM - iPart);
                                         xx = toBytes((int)iPart, 2);
                    if (fPart > 0.75)  { xx = toBytes((int)(iPart + 1), 2);
                                         y = '0'; } else
                    if (fPart > 2d/3d) { y = '5'; } else
                    if (fPart > 0.5)   { y = '4'; } else
                    if (fPart > 1d/3d) { y = '3'; } else
                    if (fPart > 0.25)  { y = '2'; } else
                    if (fPart > 0)     { y = '1'; } else
                                       { y = '0'; }
                    header.append(",PL");
                    header.append((char)xx[0]);
                    header.append((char)xx[1]);
                    header.append((char)y);                             // Paper length
                case INDEX_4WAVES_PRO_V3:
                    iPart = Math.floor(paperWidth/Paper.INCH_IN_MM);
                    fPart = (paperWidth/Paper.INCH_IN_MM - iPart);
                                         xx = toBytes((int)iPart, 2);
                    if (fPart > 0.75)  { xx = toBytes((int)(iPart + 1), 2);
                                         y = '0'; } else
                    if (fPart > 2d/3d) { y = '5'; } else
                    if (fPart > 0.5)   { y = '4'; } else
                    if (fPart > 1d/3d) { y = '3'; } else
                    if (fPart > 0.25)  { y = '2'; } else
                    if (fPart > 0)     { y = '1'; } else
                                       { y = '0'; }
                    header.append(",PW");
                    header.append((char)xx[0]);
                    header.append((char)xx[1]);
                    header.append((char)y);                             // Paper width
                    break;
                case INDEX_EVEREST_D_V3:
                case INDEX_4X4_PRO_V3:
                    header.append(",PL");
                    header.append(String.valueOf(
                            (int)Math.ceil(paperLenght)));              // Paper length
                    header.append(",PW");
                    header.append(String.valueOf(
                            (int)Math.ceil(paperWidth)));               // Paper width
                    break;
                default:
            }
            header.append(",CH");
            header.append(String.valueOf(cellsInWidth));                // Characters per line
            header.append(",IM");
            header.append(String.valueOf(marginInner));                 // Inner margin
            header.append(",OM");
            header.append(String.valueOf(marginOuter));                 // Outer margin
            header.append(",TM");
            header.append(String.valueOf(marginTop));                   // Top margin
            header.append(",BM0");                                      // Bottom margin = 0
            header.append(";");

            return header.toString().getBytes();

        }

        private static byte[] getImpactoHeader(Settings settings,
                                               int pageCount,
                                               int numberOfCopies)
                                        throws UnsupportedPaperException {

            boolean eightDots = settings.getEightDots();
            boolean duplex = settings.getDuplex();
            int linesPerPage = settings.getLinesInHeight();                             // in cells
            int charsPerLine = settings.getCellsInWidth();                              // in cells
            int pageLength = (int)Math.ceil(settings.getPageHeight()/Paper.INCH_IN_MM); // in inches

//            int marginInner = settings.getMarginInner();                                                          // in cells
//            int marginOuter = settings.getMarginOuter();                                                          // in cells
//            int topMargin = (int)Math.floor(settings.getMarginTop()*getCellHeight()/(0.1*Paper.INCH_IN_MM));      // in tenths of an inch
//            int bottomMargin = pageLength*10 - topMargin
//                          - (int)Math.floor(settings.getLinesInHeight()*getCellHeight()/(0.1*Paper.INCH_IN_MM));  // in tenths of an inch      

            if (pageLength   < 6  || pageLength   > 13) { throw new UnsupportedPaperException("Paper height = " + pageLength + " inches, must be in [6,13]"); }
            if (charsPerLine < 12 || charsPerLine > 42) { throw new UnsupportedPaperException("Characters per line = " + charsPerLine + ", must be in [12,42]"); }
            if (linesPerPage < 12 || linesPerPage > 43) { throw new UnsupportedPaperException("Lines per page = " + linesPerPage + ", must be in [12,43]"); }
//            if (topMargin    < 0  || topMargin    > 99) { throw new EmbosserFactoryException("Top margin = " + topMargin + " x 0.1\", must be in [0,99]"); }
//            if (bottomMargin < 0  || bottomMargin > 99) { throw new EmbosserFactoryException("Bottom margin = " + bottomMargin + " x 0.1\", must be in [0,99]"); }

            StringBuffer header = new StringBuffer();

            header.append((char)0x1b); header.append(')');                          // Transparent mode
            header.append((char)0x1b); header.append(eightDots?'+':'*');            // 6- or 8-dot matrix
            header.append((char)0x1b); header.append('.');
                                       header.append((char)(0x30 + pageLength));    // Page length in inches
            header.append((char)0x1b); header.append("/1");                         // Line spacing in tenths of an inch
            header.append((char)0x1b); header.append('0');
                                       header.append((char)(0x30 + charsPerLine));  // Characters per line
            header.append((char)0x1b); header.append('1');
                                       header.append((char)(0x30 + linesPerPage));  // Lines per page
            header.append((char)0x1b); header.append('3');                          // Cut off words
            header.append((char)0x1b); header.append(duplex?'Q':'P');               // Front-side of double-sided embossing
            header.append((char)0x1b); header.append("EP");
                                       header.append(String.valueOf(pageCount));
                                       header.append('\n');                         // Number of last page to emboss
            header.append((char)0x1b); header.append("GU0\n");                      // Gutter (binding margin) = 0
            header.append((char)0x1b); header.append("IN0\n");                      // Indent first line of paragraph = 0
            header.append((char)0x1b); header.append("MB0\n");                      // Bottom margin in tenths of an inch = 0
            header.append((char)0x1b); header.append("ML0\n");                      // Left margin in characters = 0
            header.append((char)0x1b); header.append("MR0\n");                      // Right margin in characters = 0
            header.append((char)0x1b); header.append("MT0\n");                      // Top margin in tenths of an inch = 0
            header.append((char)0x1b); header.append("NC1");
                                       header.append(String.valueOf(numberOfCopies));
                                       header.append('\n');                         // Number of copies
            header.append((char)0x1b); header.append("NC1\n");                      // Number of copies
            header.append((char)0x1b); header.append("PM0\n");                      // Embossing mode
            header.append((char)0x1b); header.append("PN0\n");                      // Number pages
            header.append((char)0x1b); header.append("PI0\n");                      // Parameter influence
            header.append((char)0x1b); header.append("SP1\n");                      // Number of first page to emboss

            return header.toString().getBytes();

        }

        private static byte[] getPortathielHeader(Settings settings)
                                           throws EmbosserFactoryException {

        boolean eightDots = settings.getEightDots();
        boolean duplex = settings.getDuplex();
        int linesPerPage = settings.getLinesInHeight();                             // in cells
        int charsPerLine = settings.getCellsInWidth();                              // in cells
        int pageLength = (int)Math.ceil(settings.getPageHeight()/Paper.INCH_IN_MM); // in inches

        if (pageLength   < 8  || pageLength   > 13) { throw new UnsupportedPaperException("Paper height = " + pageLength + " inches, must be in [8,13]"); }
        if (charsPerLine < 12 || charsPerLine > 42) { throw new UnsupportedPaperException("Characters per line = " + charsPerLine + ", must be in [12,42]"); }
        if (linesPerPage < 10 || linesPerPage > 31) { throw new UnsupportedPaperException("Lines per page = " + linesPerPage + ", must be in [10,31]"); }

        StringBuffer header = new StringBuffer();

        header.append(  "\u001b!TP");                                             // Transparent mode ON
        header.append("\r\u001b!DT");  header.append(eightDots?'6':'8');          // 6 or 8 dots
        header.append("\r\u001b!DS");  header.append(duplex?'1':'0');             // Front-side or double-sided embossing
        header.append("\r\u001b!LM0");                                            // Left margin
        header.append("\r\u001b!SL1");                                            // Interline space = 1/10 inch
        header.append("\r\u001b!PL");  header.append(toBytes(pageLength, 2));     // Page length in inches
        header.append("\r\u001b!LP");  header.append(toBytes(linesPerPage, 2));   // Lines per page
        header.append("\r\u001b!CL");  header.append(toBytes(charsPerLine, 2));   // Characters per line
        header.append("\r\u001b!CT1");                                            // Cut off words
        header.append("\r\u001b!NI1");                                            // No indent
        header.append("\r\u001b!JB0");                                            // Jumbo mode OFF
        header.append("\r\u001b!FF1");                                            // Form feeds ON
        header.append('\r');

        return header.toString().getBytes();
    }
}
