package be.docarch.odt2braille.ooo.checker;

import java.util.Collection;
import java.util.HashSet;

import be.docarch.odt2braille.checker.BrailleCheck;
//import be.docarch.accessibility.Check;
//import be.docarch.accessibility.Issue;
//import be.docarch.accessibility.Repairer;

/**
 *
 * @author Bert Frees
 */
public class BrailleRepairer /* implements Repairer */ {

    private Collection<BrailleCheck> supportedChecks;

    public BrailleRepairer() {

        supportedChecks = new HashSet<BrailleCheck>();
        // ...
    }

 /* @Override
    public String getIdentifier() {
        return "be.docarch.odt2braille.ooo.checker.BrailleRepairer";
    }

    @Override
    public boolean supports(Check check) {
        return supportedChecks.contains(check);
    }

    @Override
    public RepairMode getRepairMode(Check check)
                             throws IllegalArgumentException {

        if (supports(check)) {
            // ...
        }

        throw new java.lang.IllegalArgumentException("Check is not supported");
    }

    @Override
    public boolean repair(Issue issue) {

        // ...

        return false;
    } */
}
