package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

//@org.junit.Ignore
public class MathematicsTest extends Odt2BrailleTest {

    @Test
    public void woluweTest() throws Exception {

        String fileName = "woluwe_code";

        File correctODT = new File(resources + fileName + ".braille.odt");
        File testODT = new File(resources + fileName + ".odt");
        
        ConversionResult correctResult = convertODT2PEF(correctODT);
        ConversionResult testResult = convertODT2PEF(testODT);

        if (comparePEFs(correctResult.getPEFFile(), testResult.getPEFFile())) {
            correctResult.cleanUp();
            testResult.cleanUp();
        }
    }
}
