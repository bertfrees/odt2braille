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

    enum PaperSize { CUSTOM };

    private final ArrayList<Paper> papers;

    public CustomPaperProvider() {
        papers = new ArrayList<Paper>();
        papers.add(new CustomPaper());
    }

    public Collection<Paper> list() {
        return papers;
    }
}
