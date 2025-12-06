

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// This is the brain of our operation. It holds the whole file tree and knows where we are currently standing.
public class FileSystem {
    private Directory root;
    private Directory current;

    // Starts everything off with a root folder and puts us there
    public FileSystem() {
        this.root = new Directory("/", null);
        this.current = root;
    }

    // Figures out the full path string like '/home/user' from where we are right now
    public String getCurrentPath() {
        if (current == root) {
            return "/";
        }
        
        StringBuilder path = new StringBuilder();
        Node temp = current;
        while (temp != null && temp != root) {
            path.insert(0, "/" + temp.getName());
            temp = temp.getParent();
        }
        return path.toString();
    }

    // The workhorse for navigation. Takes a path string and finds the actual node it points to.
    private Node resolveNode(String path) {
        if (path == null || path.isEmpty()) return current;
        
        String[] parts = path.split("/");
        Node temp = path.startsWith("/") ? root : current;
        
        for (String part : parts) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                if (temp.getParent() != null) {
                    temp = temp.getParent();
                }
                continue;
            }
            
            if (temp instanceof Directory) {
                Node child = ((Directory) temp).getChild(part);
                if (child == null) return null;
                temp = child;
            } else {
                return null;
            }
        }
        return temp;
    }

    // Like resolveNode, but makes sure we end up at a folder, not a file
    private Directory resolveDirectory(String path) {
        Node node = resolveNode(path);
        if (node instanceof Directory) {
            return (Directory) node;
        }
        return null;
    }

    // Splits a path into 'the folder it's in' and 'the name of the file/folder itself'
    private Object[] resolveParentAndName(String path) {
        String parentPath;
        String name;
        
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return new Object[]{current, path};
        } else if (lastSlash == 0) {
            return new Object[]{root, path.substring(1)};
        } else {
            parentPath = path.substring(0, lastSlash);
            name = path.substring(lastSlash + 1);
            Directory parent = resolveDirectory(parentPath);
            return new Object[]{parent, name};
        }
    }

    // Creates a new folder. Can handle nested paths if you tell it to.
    public void mkdir(String path, boolean makeParents) {
        if (makeParents) {
            String[] parts = path.split("/");
            Node temp = path.startsWith("/") ? root : current;
            
            for (String part : parts) {
                if (part.isEmpty() || part.equals(".")) continue;
                if (part.equals("..")) {
                    if (temp.getParent() != null) temp = temp.getParent();
                    continue;
                }
                
                if (temp instanceof Directory) {
                    Directory dir = (Directory) temp;
                    Node child = dir.getChild(part);
                    if (child == null) {
                        Directory newDir = new Directory(part, dir);
                        dir.addChild(newDir);
                        temp = newDir;
                    } else if (child instanceof Directory) {
                        temp = child;
                    } else {
                        System.out.println("Error: '" + part + "' already exists.");
                        return;
                    }
                } else {
                    System.out.println("Error: '" + temp.getName() + "' is a file.");
                    return;
                }
            }
        } else {
            Object[] result = resolveParentAndName(path);
            if (result == null || result[0] == null) {
                System.out.println("Error: Path not found.");
                return;
            }
            Directory parent = (Directory) result[0];
            String name = (String) result[1];
            
            if (parent.getChild(name) != null) {
                System.out.println("Error: '" + name + "' already exists.");
            } else {
                parent.addChild(new Directory(name, parent));
            }
        }
    }

    // Creates an empty file with a specific size
    public void touch(String name, int size) {
        Node existing = current.getChild(name);
        if (existing != null) {
            if (existing instanceof Directory) {
                System.out.println("Error: '" + name + "' is a directory.");
            } else {
                File file = (File) existing;
                file.setSize(size);
                file.setContent("");
            }
        } else {
            current.addChild(new File(name, current, size));
        }
    }

    // Writes text into a file, creating it if it doesn't exist
    public void echo(String content, String path) {
        Object[] result = resolveParentAndName(path);
        if (result == null || result[0] == null) {
            System.out.println("Error: Path not found.");
            return;
        }
        
        Directory parent = (Directory) result[0];
        String name = (String) result[1];
        
        Node existing = parent.getChild(name);
        if (existing != null) {
            if (existing instanceof Directory) {
                System.out.println("Error: '" + name + "' is a directory.");
            } else {
                File file = (File) existing;
                file.setContent(content);
            }
        } else {
            parent.addChild(new File(name, parent, content));
        }
    }

    // Lists everything in the current folder, sorting them alphabetically
    public void ls() {
        List<Node> children = new ArrayList<>(current.getChildren());
        Collections.sort(children, (n1, n2) -> n1.getName().compareTo(n2.getName()));
        
        StringBuilder sb = new StringBuilder();
        for (Node child : children) {
            if (child instanceof Directory) {
                sb.append(child.getName()).append("/ ");
            } else {
                sb.append(child.getName()).append(" (").append(child.getSize()).append("B) ");
            }
        }
        if (sb.length() > 0) {
            System.out.println(sb.toString().trim());
        }
    }

    // Changes our current location to somewhere else
    public void cd(String path) {
        Node node = resolveNode(path);
        if (node == null) {
            System.out.println("Error: Path '" + path + "' not found.");
        } else if (node instanceof File) {
            System.out.println("Error: '" + node.getName() + "' is not a directory.");
        } else {
            current = (Directory) node;
        }
    }

    // Prints out where we currently are
    public void pwd() {
        System.out.println(getCurrentPath());
    }

    // Deletes a file or an empty folder
    public void rm(String name) {
        Node child = current.getChild(name);
        if (child == null) {
            System.out.println("Error: '" + name + "' not found.");
            return;
        }
        
        if (child instanceof Directory) {
            if (child.getSize() > 0) { 
                if (!((Directory) child).getChildren().isEmpty()) {
                    System.out.println("Error: Cannot remove directory '" + name + "'. It is not empty.");
                    return;
                }
            }
        }
        current.removeChild(name);
    }

    // Deletes a folder and everything inside it
    public void rmRecursive(String name) {
        Node child = current.getChild(name);
        if (child == null) {
            System.out.println("Error: '" + name + "' not found.");
            return;
        }
        
        if (child instanceof Directory) {
            removeRecursiveHelper((Directory) child);
        }
        current.removeChild(name);
    }

    // Helper that actually goes down the tree and deletes things bottom-up
    private void removeRecursiveHelper(Directory dir) {
        List<Node> children = new ArrayList<>(dir.getChildren());
        for (Node child : children) {
            if (child instanceof Directory) {
                removeRecursiveHelper((Directory) child);
            }
            dir.removeChild(child.getName());
        }
    }

    // Shows a pretty visual hierarchy of the current folder and its subfolders
    public void tree() {
        System.out.println(".");
        printTree(current, "");
    }

    // The recursive part that draws the tree structure
    private void printTree(Directory dir, String prefix) {
        List<Node> children = new ArrayList<>(dir.getChildren());
        Collections.sort(children, (n1, n2) -> n1.getName().compareTo(n2.getName()));

        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            boolean isLast = (i == children.size() - 1);
            
            String connector = isLast ? "└── " : "├── ";
            String childPrefix = isLast ? "    " : "│   ";
            
            if (child instanceof Directory) {
                System.out.println(prefix + connector + child.getName() + "/");
                printTree((Directory) child, prefix + childPrefix);
            } else {
                System.out.println(prefix + connector + child.getName() + " (" + child.getSize() + "B)");
            }
        }
    }

    // Searches for a text pattern inside a file
    public void grep(String pattern, String filename) {
        Object[] result = resolveParentAndName(filename);
        if (result == null || result[0] == null) {
            System.out.println("Error: Path not found.");
            return;
        }
        
        Directory parent = (Directory) result[0];
        String name = (String) result[1];
        Node node = parent.getChild(name);
        
        if (node == null) {
            System.out.println("Error: '" + name + "' not found.");
            return;
        }
        
        if (node instanceof Directory) {
            System.out.println("Error: '" + name + "' is not a file.");
            return;
        }
        
        File file = (File) node;
        String content = file.getContent();
        
        if (kmpSearch(content, pattern)) {
            System.out.println("Pattern \"" + pattern + "\" found in " + name + ".");
        } else {
            System.out.println("Pattern \"" + pattern + "\" not found in " + name + ".");
        }
    }

    // Standard KMP algorithm to find the text pattern efficiently
    private boolean kmpSearch(String text, String pattern) {
        if (pattern.isEmpty()) return true;
        
        int[] lps = buildLPS(pattern);
        int i = 0; 
        int j = 0; 
        int n = text.length();
        int m = pattern.length();
        
        while (i < n) {
            if (pattern.charAt(j) == text.charAt(i)) {
                j++;
                i++;
            }
            if (j == m) {
                return true; 
            } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    // Pre-processes the pattern for KMP search
    private int[] buildLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0; 
        int i = 1;
        lps[0] = 0; 
        
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    // Calculates how much space the current folder takes up
    public void du() {
        int totalSize = current.getSize();
        System.out.println("Total size: " + totalSize + "B");
    }
}
