package be.docarch.odt2braille;

import java.util.ArrayList;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Iterator;

/**
 *
 * @author Bert Frees
 */
public class Combinations extends AbstractCollection<Combination> {

    private Collection<Combination> ok = new ArrayList<Combination>();
    private Collection<Combination> all = new ArrayList<Combination>();

    public Combinations(Collection<Integer> integers) {

        super();
        Combination n;
        Collection<Combination> m = new ArrayList<Combination>();
        for (Integer i : integers) {
            n = new Combination(i);
            if (n.ok() || n.dividable()) {
                m.clear();
                for (Combination c : all) {
                    m.add(new Combination(c, n));
                }
                addAll(m);
                add(n);
            }
        }
    }

    public Combinations(Combinations x,
                        Combinations y) {
        super();
        addAll(x);
        addAll(y);
        for (Combination xx : x) {
            for (Combination yy : y) {
                add(new Combination(xx,yy));
            }
        }
    }

    public Collection<Combination> getOK() {
        return ok;
    }

    public Iterator<Combination> iterator() {
        return all.iterator();
    }

    @Override
    public boolean add(Combination c) {

        if (c.ok()) {
            ok.add(c);
            all.add(c);
            return true;
        } else if (c.dividable()) {
            all.add(c);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int size() {
        return all.size();
    }
}

