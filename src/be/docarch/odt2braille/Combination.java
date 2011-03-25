package be.docarch.odt2braille;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

/**
 *
 * @author Bert Frees
 */
public class Combination extends TreeSet<Integer>
                         implements SortedSet<Integer>  {

    private static int total;
    private static int min;
    private static int max;
    private static int minLast;

    private boolean ok = false;
    private boolean dividable = false;

    public static void setBoundaries(int total,
                                     int min,
                                     int max,
                                     int minLast) {

        Combination.total = Math.max(1,total);
        Combination.max =   Math.max(1,max);
        Combination.min =   Math.max(1,Math.min(min,Combination.max));

        int maxMinLast = Math.min(Combination.min, Combination.total);

        int lower = Combination.max;
        int upper = 2*Combination.min;
        while (true) {
            if (Combination.total <= lower) {
                break;
            } else if (Combination.total < upper) {
                maxMinLast = Combination.total - lower;
                break;
            }
            lower += Combination.max;
            upper += Combination.min;
        }

        Combination.minLast = Math.max(1,Math.min(minLast,maxMinLast));
    }

    public Combination() {
        super();
        ok = ok(0,total);
        dividable = dividable(1,total);
    }

    public Combination(int i) {

        super();
        add(i);
        boolean o = ok(0,i);
        boolean d = dividable(0,i);
        if (!o && !d) {
            ok = false;
            dividable = false;
            return;
        }
        ok = o && ok(i,total);
        dividable = d || dividable(i,total);
    }

    public Combination(Combination x,
                       Combination y) {
        super();
        addAll(x);
        addAll(y);

        dividable = false;
        ok = true;
        boolean o;
        boolean d;

        if (!x.dividable() || !y.dividable()) {
            dividable = false;
            ok = false;
            return;
        }

        Iterator<Integer> i = iterator();
        int start = 0;
        int end;
        while(i.hasNext()) {
            end = i.next();
            o = ok(start,end);
            d = dividable(start,end);
            if (!o && !d) {
                ok = false;
                dividable = false;
                return;
            }
            ok = ok && o;
            dividable = dividable || d;
            start = end;
        }
        ok = ok && ok(start,total);
        dividable = dividable || dividable(start,total);
    }

    public boolean ok() {
        return ok;
    }

    public boolean dividable() {
        return dividable;
    }

    public static boolean ok(int start, int end) {

        int d = end-start;
        if (end<total) {
            return (d>=min && d<=max);
        } else {
            return (d>=minLast && d<=max);
        }
    }

    public static boolean dividable(int start, int end) {

        int d = end-start;
        int lower = min+((end<total)?min:minLast);
        int upper = 2*max;

        while (true) {
            if (d<lower) {
                return false;
            } else if (d<=upper) {
                return true;
            }
            lower+=min;
            upper+=max;
        }
    }
}