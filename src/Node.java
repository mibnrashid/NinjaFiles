

/**
 * Abstract base class representing a node in the file system.
 * This can be either a File or a Directory.
 */
public abstract class Node {
    protected String name;
    protected Directory parent;

    /**
     * Constructor for Node.
     * @param name The name of the node.
     * @param parent The parent directory of the node.
     */
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

    /**
     * Returns the size of the node.
     * For a file, it is the file size.
     * For a directory, it is the sum of sizes of its children.
     */
    public abstract int getSize();

    /**
     * Checks if the node is a directory.
     * @return false by default, overridden in Directory class.
     */
    public boolean isDirectory() {
        return false;
    }
}
