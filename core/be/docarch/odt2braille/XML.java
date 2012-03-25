package be.docarch.odt2braille;

import java.io.File;
import java.util.List;

/**
 *
 * @author Bert Frees
 */
public interface XML {
    
    public List<Volume> getVolumes();
    
    public void close();
    
    public interface Volume {
    
        public String getTitle();
        
        public File getBodySection() throws Exception;

        public File getPreliminarySection() throws Exception;
        
        public boolean getFrontMatterEnabled();
        
        public boolean getTableOfContentEnabled();
        
        public boolean getTranscribersNotesPageEnabled();
        
        public boolean getSpecialSymbolListEnabled();
        
        public int getFirstBraillePage();
        
        public int getLastBraillePage();
        
        public void setBraillePagesStart(int value);
                
        public void setNumberOfBraillePages(int value);
        
        public void setNumberOfPreliminaryPages(int value);

    }
}
