package be.docarch.odt2braille;

import java.util.List;
import java.util.ArrayList;


/**
 *
 * @author Bert Frees
 */
public class Section {

    private String name = null;
    private Section parent = null;
    private List<Section> children = null;

    public Section(Section copySection) {

        this(copySection.name);
        for (Section s : copySection.children) {
            Section child = new Section(s);
            child.parent = this;
            children.add(child);
        }        
    }

    public Section(String name) {
        this();
        this.name = name;
    }

    public Section() {
        this.children = new ArrayList<Section>();
    }

    public String getName() {
        return name;
    }

    public Section getParent() {
        return parent;
    }

    public List<Section> getChildren() {
        return children;
    }

    public List<Section> getDescendants() {

        List<Section> descendants = new ArrayList<Section>();
        for (Section child : children) {
            descendants.add(child);
            descendants.addAll(child.getDescendants());
        }
        return descendants;
    }

    public List<Section> getAncestors() {

        List<Section> ancestors = new ArrayList<Section>();
        if (parent != null) {
            ancestors.add(parent);
            ancestors.addAll(parent.getAncestors());
        }
        return ancestors;
    }

    public Section addChild(String childName) {

        Section child = new Section(childName);
        children.add(child);
        child.parent = this;
        return child;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Section that = (Section)obj;
        return (this.name.equals(that.name));
    }
}
