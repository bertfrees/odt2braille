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

package be.docarch.odt2braille;

import be.docarch.odt2braille.setup.Configuration.PageNumberFormat;

import java.io.File;
import java.util.List;
import org.w3c.dom.Element;

public interface PEF {
    
  //public Element getDOMElement();
    
    public List<Volume> getVolumes();
        
    public File getSinglePEF();

    public File[] getPEFs();
    
    public void close();
    
    public interface Volume {
    
        public String getName();
        
      //public Element getDOMElement();
        
        public List<Section> getSections();
        
    }
    
    public interface Section {
    
        public String getName();
        
        public int getFirstPageNumber();
        
        public PageNumberFormat getPageNumberFormat();
        
        public Element getDOMElement();
        
    }
}