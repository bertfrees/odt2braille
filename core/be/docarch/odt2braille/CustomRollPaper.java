package be.docarch.odt2braille;

import org.daisy.braille.tools.Length;
import org.daisy.paper.RollPaper;
import be.docarch.odt2braille.CustomPaperProvider.PaperType;

public class CustomRollPaper extends RollPaper {

    private Length across;

    public CustomRollPaper(String name, String desc) {
        super(name, desc, PaperType.ROLL, Length.newMillimeterValue(297d));
        across = super.getLengthAcrossFeed();
    }

    public void setLengthAcrossFeed(Length across) {
        this.across = across;
    }

    @Override
    public Length getLengthAcrossFeed() {
        return across;
    }
}