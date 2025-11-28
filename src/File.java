

/**
 * Represents a file in the file system.
 * Stores content and size.
 */
public class File extends Node {
    private int size;
    private String content;

    /**
     * Constructor that initializes size, with empty content.
     */
    public File(String name, Directory parent, int size) {
        super(name, parent);
        this.size = size;
        this.content = "";
    }

    /**
     * Constructor that initializes content, size is derived from content length.
     */
    public File(String name, Directory parent, String content) {
        super(name, parent);
        this.content = content;
        this.size = content.length();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.size = content.length();
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int getSize() {
        return size;
    }
}
