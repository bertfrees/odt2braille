package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

//@org.junit.Ignore
public class DutchTest extends Odt2BrailleTest {

    @Test
    public void generalTest() throws Exception {

        String fileName = "dutch_general";

        File correctODT = new File(resources + fileName + ".braille.odt");
        File testODT = new File(resources + fileName + ".odt");

        ConversionResult correctResult = convertODT2PEF(correctODT);
        ConversionResult testResult = convertODT2PEF(testODT);

        if (comparePEFs(correctResult.getPEFFile(), testResult.getPEFFile())) {
            correctResult.cleanUp();
            testResult.cleanUp();
        }
    }

    @Test
    public void italicTest() throws Exception {

        String fileName = "dutch_italic";

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
