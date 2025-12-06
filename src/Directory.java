
import java.util.HashMap;
import java.util.Collection;

// Basically a folder that can hold other files or folders inside it
public class Directory extends Node {
    // We use a HashMap here to quickly find items by their name
    private HashMap<String, Node> children;

    // Sets up an empty folder ready to be filled
    public Directory(String name, Directory parent) {
        super(name, parent);
        this.children = new HashMap<>();
    }

    // Yup, this is definitely a folder
    @Override
    public boolean isDirectory() {
        return true;
    }

    // Looks for a specific file or folder inside this one
    public Node getChild(String name) {
        return children.get(name);
    }

    // Puts a new item into this folder
    public void addChild(Node child) {
        children.put(child.getName(), child);
    }

    // Takes an item out of this folder
    public Node removeChild(String name) {
        return children.remove(name);
    }

    // Gives us a list of everything inside
    public Collection<Node> getChildren() {
        return children.values();
    }

    // To find the size, we have to add up the sizes of everything inside recursively
    @Override
    public int getSize() {
        int totalSize = 0;
        for (Node child : children.values()) {
            totalSize += child.getSize();
        }
        return totalSize;
    }
}
