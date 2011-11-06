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
import java.util.TreeMap;

import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.Configuration.SplittableVolume;
import be.docarch.odt2braille.setup.Configuration.VolumeManagementMode;

public abstract class VolumeSplitter {

    public static List<AutoGeneratedVolume> splitBodyMatterVolume(ODT odt)
                                                           throws IOException,
                                                                  SAXException,
                                                                  TransformerConfigurationException,
                                                                  TransformerException,
                                                                  ConversionException,
                                                                  Exception {

        List<AutoGeneratedVolume> volumes = new ArrayList<AutoGeneratedVolume>();

        Configuration configuration = odt.getConfiguration();

        if ((configuration.getBodyMatterMode() == VolumeManagementMode.AUTOMATIC)) {

            SplittableVolume volumeSettings = configuration.getBodyMatterVolume();

            int[] allPages = odt.extractDocumentOutline();
            int[] optimalVolumes = computeOptimalVolumes(allPages,
                                                         volumeSettings.getMinVolumeSize(),
                                                         volumeSettings.getMaxVolumeSize(),
                                                         volumeSettings.getPreferredVolumeSize(),
                                                         volumeSettings.getMinLastVolumeSize());

            for (int i=0; i<optimalVolumes.length; i++) {
                volumes.add(new AutoGeneratedVolume(volumeSettings, optimalVolumes[i]+1));
            }

            odt.splitInAutomaticVolumes(volumes);
        }

        return volumes;
    }

    private static int[] computeOptimalVolumes(int[] pages,
                                               int min,
                                               int max,
                                               int preferred,
                                               int minLast) {

        int total = Math.max(1,pages.length);
        int[] weigths = new int[] { 512, 0, 1, 2, 4, 8, 16, 32, 64, 128, 256 };
        Map<Integer, List<Integer>> optimalpartitions = new TreeMap<Integer, List<Integer>>();
        int[] minerror1 = new int[total+1];
        int[] minerror2 = new int[total+1];
        boolean[] ok = new boolean[total+1];
        int previouspage;
        int currentpage;
        List<Integer> previouspartition;
        List<Integer> currentpartition;

        max =   Math.max(1,max);
        min =   Math.max(1,Math.min(min,max));
        preferred = Math.max(min, Math.min(max, preferred));
        int maxMinLast = Math.min(min, total);
        int lower = max;
        int upper = 2*min;
        while (true) {
            if (total <= lower) {
                break;
            } else if (total < upper) {
                maxMinLast = total - lower;
                break;
            }
            lower += max;
            upper += min;
        }
        minLast = Math.max(1,Math.min(minLast,maxMinLast));

        for (int i=0; i<=total; i++) {
            ok[i] = false;
            minerror1[i] = Integer.MAX_VALUE;
            minerror2[i] = Integer.MAX_VALUE;
        }
        for (int i=total-minLast; i>=total-max && i>=0; i--) {
            ok[i] = true;
        }
        minerror1[0] = 0;
        minerror2[0] = 0;

        currentpartition = new ArrayList<Integer>();
        currentpartition.add(0);
        optimalpartitions.put(0, currentpartition);

        for (int j=0; j<total; j++) {
            if (optimalpartitions.containsKey(j)) {
                previouspartition = optimalpartitions.get(j);
                previouspage = previouspartition.get(previouspartition.size()-1);
                for (int i=min; i<max; i++) {
                    currentpage = previouspage + i;
                    if (currentpage >= total) { break; }
                    currentpartition = new ArrayList<Integer>(previouspartition);
                    currentpartition.add(currentpage);
                    int e1 = minerror1[previouspage] + weigths[pages[currentpage]];
                    int e2 = minerror2[previouspage] + Math.abs(i-preferred);
                    if (e1<minerror1[currentpage]) {
                        minerror1[currentpage] = e1;
                        minerror2[currentpage] = Integer.MAX_VALUE;
                        optimalpartitions.put(currentpage, currentpartition);
                    } else if (e1==minerror1[currentpage]) {
                        if (e2<minerror2[currentpage]) {
                            minerror2[currentpage] = e2;
                            optimalpartitions.put(currentpage, currentpartition);
                        }
                    }
                    if (ok[currentpage]) {
                        if (e1<minerror1[total-1]) {
                            minerror1[total-1] = e1;
                            minerror2[total-1] = Integer.MAX_VALUE;
                            optimalpartitions.put(total, currentpartition);
                        } else if (e1==minerror1[total-1]) {
                            e2 += Math.abs(total-currentpage-preferred);
                            if (e2<minerror2[total-1]) {
                                minerror2[total-1] = e2;
                                optimalpartitions.put(total, currentpartition);
                            }
                        }
                    }
                }
            }
        }

        if (optimalpartitions.containsKey(total)) {
            List<Integer> optimalpartition = optimalpartitions.get(total);
            int[] r = new int[optimalpartition.size()];
            int j = 0;
            for (int i : optimalpartition) {
                r[j] = i;
                j++;
            }
            return r;
        } else {
            return new int[]{0};
        }
    }
}
