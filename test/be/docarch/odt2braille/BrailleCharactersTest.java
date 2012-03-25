package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

//@org.junit.Ignore
public class BrailleCharactersTest extends Odt2BrailleTest {

    @Test
    public void butterflyTest() throws Exception {

        String fileName = "butterfly";

        File correctPEF = new File(resources + fileName + ".pef");
        File testODT = new File(resources + fileName + ".odt");
        
        ConversionResult result = convertODT2PEF(testODT);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }
}
