package be.docarch.odt2braille;

import java.util.ArrayList;
import java.util.Collection;
import org.daisy.paper.Paper;
import org.daisy.paper.PaperProvider;

/**
 *
 * @author Bert Frees
 */
public class CustomPaperProvider implements PaperProvider {

    enum PaperType { SHEET, TRACTOR, ROLL };

    private final ArrayList<Paper> papers;

    public CustomPaperProvider() {
        papers = new ArrayList<Paper>();
        papers.add(new CustomSheetPaper("Custom sheet...", "Sheet paper with adjustable width and height"));
        papers.add(new CustomTractorPaper("Custom tractor paper...", "Tractor paper with adjustable width and height"));
        papers.add(new CustomRollPaper("Custom roll...", "Roll paper with adjustable width"));
    }

    public Collection<Paper> list() {
        return papers;
    }
}
