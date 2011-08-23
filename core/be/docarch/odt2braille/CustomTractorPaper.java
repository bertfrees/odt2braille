package be.docarch.odt2braille;

import org.daisy.braille.tools.Length;
import org.daisy.paper.TractorPaper;
import be.docarch.odt2braille.CustomPaperProvider.PaperType;

public class CustomTractorPaper extends TractorPaper {

    private Length across;
    private Length along;

    public CustomTractorPaper(String name, String desc) {
        super(name, desc, PaperType.TRACTOR, Length.newMillimeterValue(210d), Length.newInchValue(11d));
        across = super.getLengthAcrossFeed();
        along = super.getLengthAlongFeed();
    }

    public void setLengthAcrossFeed(Length across) {
        this.across = across;
    }

    public void setLengthAlongFeed(Length along) {
        this.along = along;
    }

    @Override
    public Length getLengthAcrossFeed() {
        return across;
    }

    @Override
    public Length getLengthAlongFeed() {
        return along;
    }
}