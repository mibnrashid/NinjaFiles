
// This is the blueprint for everything in our file system - both files and folders start here
public abstract class Node {
    protected String name;
    protected Directory parent;

    // Sets up the basic info like name and where it lives
    public Node(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Directory getParent() {
        return parent;
    }

    public void setParent(Directory parent) {
        this.parent = parent;
    }

    // Each type of item figures out its own size differently
    public abstract int getSize();

    // Simple check to see if this is a folder or just a file
    public boolean isDirectory() {
        return false;
    }
}
