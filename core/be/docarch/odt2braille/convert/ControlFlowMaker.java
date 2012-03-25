package be.docarch.odt2braille.convert;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Bert Frees
 */
public class ControlFlowMaker extends XSLTransformer {
    
    private final Set<String> supportedParameters = new HashSet<String>();

    @Override
    public void setParameter(String key, Object value) {
        if (!supportedParameters.contains(key)) { throw new IllegalArgumentException("parameter '" + key + "' not supported"); }
        if ("styles-url".equals(key)) {
            super.setParameter(key, (URI)value);
        } else {
            super.setParameter("param" + capitalizeFirstLetter(key), value);
        }
    }

    public ControlFlowMaker() throws Exception {
        super("controller");
        supportedParameters.add("frontMatterSection");
        supportedParameters.add("repeatFrontMatterSection");
        supportedParameters.add("titlePageSection");
        supportedParameters.add("manualVolumeSections");
        supportedParameters.add("rearMatterSection");
        supportedParameters.add("styles-url");
    }
    
    private static String capitalizeFirstLetter(String in) {
        return in.substring(0,1).toUpperCase() + in.substring(1);
    }
}
