

import java.util.HashMap;
import java.util.Collection;

/**
 * Represents a directory in the file system.
 * Uses a HashMap to store children nodes for efficient lookup.
 */
public class Directory extends Node {
    // The hash table required by the project spec to store children.
    // Key: child name (String), Value: Node object.
    private HashMap<String, Node> children;

    public Directory(String name, Directory parent) {
        super(name, parent);
        this.children = new HashMap<>();
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    /**
     * Retrieves a child node by name.
     * @param name The name of the child to retrieve.
     * @return The child Node, or null if not found.
     */
    public Node getChild(String name) {
        return children.get(name);
    }

    /**
     * Adds a child node to this directory.
     * @param child The node to add.
     */
    public void addChild(Node child) {
        children.put(child.getName(), child);
    }

    /**
     * Removes a child node by name.
     * @param name The name of the child to remove.
     * @return The removed Node, or null if not found.
     */
    public Node removeChild(String name) {
        return children.remove(name);
    }

    /**
     * Returns a collection of all children nodes.
     * @return Collection of children.
     */
    public Collection<Node> getChildren() {
        return children.values();
    }

    /**
     * Calculates the total size of the directory by summing the sizes of all children.
     * Recursive implementation.
     */
    @Override
    public int getSize() {
        int totalSize = 0;
        for (Node child : children.values()) {
            totalSize += child.getSize();
        }
        return totalSize;
    }
}
