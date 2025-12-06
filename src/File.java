
// Just a standard file that holds some text and knows how big it is
public class File extends Node {
    private int size;
    private String content;

    // Creates a blank file with a specific size placeholder
    public File(String name, Directory parent, int size) {
        super(name, parent);
        this.size = size;
        this.content = "";
    }

    // Creates a file with actual text in it
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
