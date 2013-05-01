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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.transform.TransformerException;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.Configuration.VolumeManagementMode;

public class VolumeManager {

    private final Volume singleBodyVolume;
    private final Volume singleRearVolume;
    private final PreliminaryVolume preliminaryVolume;
    private final List<Volume> manualVolumes = new ArrayList<Volume>();
    private final Map<String,Volume> sectionToVolume = new HashMap<String,Volume>();
    private final List<Volume> volumes = new ArrayList<Volume>();

    public VolumeManager(ODT odt)
                  throws IOException,
                         TransformerException,
                         SAXException,
                         ConversionException,
                         Exception {

        Configuration configuration = odt.getConfiguration();

        preliminaryVolume = new PreliminaryVolume(configuration.getPreliminaryVolume());
        singleBodyVolume = new Volume(configuration.getBodyMatterVolume());
        singleRearVolume = new Volume(configuration.getRearMatterVolume());
        for (Configuration.SectionVolume v : configuration.getSectionVolumeList().values()) {
            Volume volume = new Volume(v);
            manualVolumes.add(volume);
            sectionToVolume.put(v.getSection(), volume);
        }

        odt.transform(singleBodyVolume, singleRearVolume, sectionToVolume);

        if (configuration.getPreliminaryVolumeEnabled()) {
            volumes.add(preliminaryVolume);
        }
        switch (configuration.getBodyMatterMode()) {
            case SINGLE:
                volumes.add(singleBodyVolume);
                break;
            case AUTOMATIC:
                volumes.addAll(VolumeSplitter.splitBodyMatterVolume(odt));
                break;
        }
        volumes.addAll(manualVolumes);
        if (configuration.getRearMatterSection() != null &&
            configuration.getRearMatterMode() == VolumeManagementMode.SINGLE) {
            volumes.add(singleRearVolume);
        }

        int i = 1;
        for (Volume v : volumes) {
            String title = v.getTitle();
            if (title.contains("@i")) {
                v.setTitle(title.replaceFirst("@i", String.valueOf(i)));
                i++;
            }
        }
        for (Volume v : volumes) {
            if (v.getFrontMatter()) {
                v.setExtendedFrontMatter(true);
                break;
            }
        }
        for (Volume v : volumes) {
            if (v.getTableOfContent()) {
                v.setExtendedTableOfContent(true);
                break;
            }
        }
    }

    public List<Volume> getVolumes() {
        return new ArrayList<Volume>(volumes);
    }
}
