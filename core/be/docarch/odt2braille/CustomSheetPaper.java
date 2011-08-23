package be.docarch.odt2braille;

import org.daisy.braille.tools.Length;
import org.daisy.paper.SheetPaper;
import be.docarch.odt2braille.CustomPaperProvider.PaperType;

public class CustomSheetPaper extends SheetPaper {

    private Length width;
    private Length height;

    public CustomSheetPaper(String name, String desc) {
        super(name, desc, PaperType.SHEET, Length.newMillimeterValue(210d), Length.newMillimeterValue(297d));
        width = super.getPageWidth();
        height = super.getPageHeight();
    }

    public void setPageWidth(Length width) {
        this.width = width;
    }

    public void setPageHeight(Length height) {
        this.height = height;
    }

    @Override
    public Length getPageHeight() {
        return height;
    }

    @Override
    public Length getPageWidth() {
        return width;
    }
}