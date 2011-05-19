package be.docarch.odt2braille;

import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Bert Frees
 */
@Ignore
public class DutchTest extends TranslationTest {

    @Test
    public void specialCasesTest() throws Exception {

        simpleTest("dutch special cases");

    }

    @Test
    public void italicTest() throws Exception {

        simpleTest("dutch italic");

    }
}
