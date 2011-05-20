package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

/**
 *
 * @author Bert Frees
 */
public class DutchTest extends Odt2BrailleTest {

    @Test
    public void generalTest() throws Exception {

        String fileName = "dutch_general";

        File correctODT = new File(resources + fileName + ".braille.odt");
        File testODT = new File(resources + fileName + ".odt");

        File correctPEF = simpleODT2PEF(correctODT);
        File testPEF = simpleODT2PEF(testODT);

        comparePEFs(correctPEF, testPEF);
    }

    @Test
    public void italicTest() throws Exception {

        String fileName = "dutch_italic";

        File correctODT = new File(resources + fileName + ".braille.odt");
        File testODT = new File(resources + fileName + ".odt");

        File correctPEF = simpleODT2PEF(correctODT);
        File testPEF = simpleODT2PEF(testODT);

        comparePEFs(correctPEF, testPEF);
    }
}
