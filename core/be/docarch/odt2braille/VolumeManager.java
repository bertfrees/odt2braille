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

    public VolumeManager(Configuration settings) throws IOException,
                                                        TransformerException,
                                                        SAXException,
                                                        ConversionException {

        preliminaryVolume = new PreliminaryVolume(settings.getPreliminaryVolume());
        singleBodyVolume = new Volume(settings.getBodyMatterVolume());
        singleRearVolume = new Volume(settings.getRearMatterVolume());
        for (Configuration.SectionVolume v : settings.getSectionVolumeList().values()) {
            Volume volume = new Volume(v);
            manualVolumes.add(volume);
            sectionToVolume.put(v.getSection(), volume);
        }

        settings.odtTransformer.configure(settings);
        settings.odtTransformer.transform(singleBodyVolume, singleRearVolume, sectionToVolume);

        if (settings.getPreliminaryVolumeEnabled()) {
            volumes.add(preliminaryVolume);
        }
        switch (settings.getBodyMatterMode()) {
            case SINGLE:
                volumes.add(singleBodyVolume);
                break;
            case AUTOMATIC:
                volumes.addAll(VolumeSplitter.splitBodyMatterVolume(settings));
                break;
        }
        volumes.addAll(manualVolumes);
        if (settings.getRearMatterSection() != null &&
            settings.getRearMatterMode() == VolumeManagementMode.SINGLE) {
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
