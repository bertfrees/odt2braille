package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

/**
 *
 * @author Bert Frees
 */
public class BrailleCharactersTest extends Odt2BrailleTest {

    @Test
    public void butterflyTest() throws Exception {

        String fileName = "butterfly";

        File correctPEF = new File(resources + fileName + ".pef");
        File testODT = new File(resources + fileName + ".odt");
        File testPEF = simpleODT2PEF(testODT);

        comparePEFs(correctPEF, testPEF);
    }
}
