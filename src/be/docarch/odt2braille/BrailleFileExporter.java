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

package be.docarch.odt2braille;

import org_pef_text.pef2text.AbstractEmbosser;
import java.io.OutputStream;

import java.io.IOException;

import org_pef_text.AbstractTable;
import org_pef_text.TableFactory;
import org_pef_text.TableFactory.TableType;

/**
 * Implementation of AbstractEmbosser for generating generic braille files:
 * 
 * <ul>
 * <li>Braille Formatted (.brf)</li>
 * <li>Braille Formatted (.brf) - slightly modified for correct interpretation by wprint55</li>
 * <li>MicroBraille file (.brl)</li>
 * <li>Spanish Braille file (.bra)</li>
 * </ul>
 *
 * @author  Bert Frees
 */
public class BrailleFileExporter implements AbstractEmbosser {

    public enum BrailleFileType { NONE, BRF, BRL, BRF_INTERPOINT, BRA, PEF };

    private BrailleFileType fileType = null;
    private OutputStream os = null;
    private AbstractTable table = null;
    private String linebreaks;
    private byte[] header;
    //private byte[] footer;
    private int maxHeight;
    private int maxWidth;
    private int currentPage;
    private int charsOnRow;
    private int rowsOnPage;
    private boolean isOpen;
    private boolean isClosed;
    private boolean currentDuplex;
    private boolean duplexEnabled;

    /**
     * Creates a new <code>BrailleFileExporter</code> instance.
     *
     * @param  os               The <code>FileOutputStream</code>
     * @param  fileType         {@link BrailleFileType#BRF}, {@link BrailleFileType#BRF_INTERPOINT} or {@link BrailleFileType#BRL}.
     * @param  maxWidth         The maximum number of cells a line can contain.
     * @param  maxHeight        The maximum number of lines a page can contain.
     * @param  duplexEnabled    <code>true</code> if recto-verso is enabled.
     */
    public BrailleFileExporter (OutputStream os,
                                BrailleFileType fileType,
                                TableType charSet,
                                int maxWidth,
                                int maxHeight,
                                boolean duplexEnabled) {

        this.os = os;
        this.fileType = fileType;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.duplexEnabled = duplexEnabled;
        isOpen = false;
        isClosed = false;
        currentPage = 0;
        charsOnRow = 0;
        rowsOnPage = 0;

        TableFactory tf = new TableFactory();
        tf.setTable(charSet);
        table = tf.newTable();

        switch (fileType) {

            case BRF:
            case BRF_INTERPOINT:
                header = new byte[0];
                //footer = new byte[0];
                linebreaks = "\r\n";
                break;
            case BRA:
                header = new byte[0];
                linebreaks = "\n";
                break;
            case BRL:
                header = ("$" + maxHeight).getBytes();
                //footer = new byte[0];
                linebreaks = "\r\n";
                break;
            default:

        }
    }

    public void write(String braille)
               throws IOException {

        charsOnRow += braille.length();
        if (charsOnRow>getMaxWidth()) {
            throw new IOException("The maximum number of characters on a row was exceeded (page is too narrow).");
        }
        os.write(String.valueOf(table.toText(braille)).getBytes(table.getPreferredCharset().name()));

    }

    private void lineFeed() throws IOException {

        rowsOnPage++;
        charsOnRow = 0;
        os.write(linebreaks.getBytes());

    }

    public void newLine() throws IOException {
        lineFeed();
    }

    private void formFeed() throws IOException {

        charsOnRow = 0;
        rowsOnPage++;
        if (rowsOnPage>getMaxHeight()) {
                throw new IOException("The maximum number of rows on a page was exceeded (page is too short)");
        }
        switch (fileType) {

            case BRF:
                lineFeed();
                os.write((byte)0x0c);
                break;
            case BRF_INTERPOINT:
                os.write((byte)0x0c);
                break;
            case BRA:
                lineFeed();
                break;
            case BRL:
                lineFeed();
                if (currentPage==0) {
                    byte[] pageBreak = "----|---|---------------------------|+-".getBytes();
                    if (maxWidth<40) {
                        pageBreak[maxWidth-1] = '>';
                    }
                    os.write(pageBreak);
                }
                lineFeed();
                break;
            default:

        }
        rowsOnPage = 0;
        currentPage++;

    }

    public void newPage() throws IOException {

            if (supportsDuplex() && !currentDuplex && (currentPage % 2)==1) {
                formFeed();
            }
            formFeed();

    }

    public void newSectionAndPage(boolean duplex)
                           throws IOException {

            if (duplexEnabled && (currentPage %2)==1) {
                    formFeed();
            }
            newPage();
            currentDuplex = duplex;

    }

    public void newVolume() throws IOException {
        charsOnRow = 0;
    }

    public void open(boolean duplex)
              throws IOException {

        if (fileType==BrailleFileType.BRL) {
            os.write(header);
            os.write(linebreaks.getBytes());
        }
        //currentDuplex = duplex;
        isOpen=true;

    }

    public void close() throws IOException {

        //os.write(footer);
        os.close();
        isClosed=true;
        isOpen=false;

    }

    public void writePreamble(int length) {}

    public boolean isOpen() { return isOpen; }

    public boolean isClosed() { return isClosed; }

    public void setRowGap(int value) {}

    public int getRowGap() { return 0; }

    public boolean supportsVolumes() { return false; }

    public boolean supports8dot() { return false; }

    public boolean supportsDuplex() { return duplexEnabled; }

    public boolean supportsAligning() { return false; }

    public int getMaxWidth() { return maxWidth; }

    public int getMaxHeight() { return maxHeight; }

}
