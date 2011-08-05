package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

public class MathematicsTest extends Odt2BrailleTest {

    @Test
    public void woluweTest() throws Exception {

        String fileName = "woluwe_code";

        File correctODT = new File(resources + fileName + ".braille.odt");
        File testODT = new File(resources + fileName + ".odt");

        File correctPEF = simpleODT2PEF(correctODT);
        File testPEF = simpleODT2PEF(testODT);

        comparePEFs(correctPEF, testPEF);
    }
}
