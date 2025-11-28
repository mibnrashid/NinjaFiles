

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core class for the NinjaFiles file system simulator.
 * Manages the root directory and the current working directory.
 */
public class FileSystem {
    private Directory root;
    private Directory current;

    public FileSystem() {
        // Initialize root directory with no parent
        this.root = new Directory("/", null);
        // Start at root
        this.current = root;
    }

    /**
     * Builds the absolute path of the current directory.
     * @return String representation of the current path (e.g., "/home/user")
     */
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

    /**
     * Helper to resolve a path to a Node.
     * Handles absolute paths, relative paths, ".", and "..".
     */
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
                // Cannot traverse through a file
                return null;
            }
        }
        return temp;
    }

    /**
     * Helper to resolve a path to a Directory.
     * Returns null if path doesn't exist or is not a directory.
     */
    private Directory resolveDirectory(String path) {
        Node node = resolveNode(path);
        if (node instanceof Directory) {
            return (Directory) node;
        }
        return null;
    }

    /**
     * Helper to get the parent directory of a path and the filename.
     * Returns an array where [0] is the parent Directory and [1] is the name.
     * Returns null if parent path is invalid.
     */
    private Object[] resolveParentAndName(String path) {
        String parentPath;
        String name;
        
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return new Object[]{current, path};
        } else if (lastSlash == 0) {
            // Path is like "/name"
            return new Object[]{root, path.substring(1)};
        } else {
            parentPath = path.substring(0, lastSlash);
            name = path.substring(lastSlash + 1);
            Directory parent = resolveDirectory(parentPath);
            return new Object[]{parent, name};
        }
    }

    public void mkdir(String path, boolean makeParents) {
        if (makeParents) {
            // mkdir -p behavior
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
                        // Create directory
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
            // Normal mkdir behavior (in current directory)
            // The spec says "create each named directory directly inside the current directory"
            // But if the user passes a path like "a/b", standard mkdir fails without -p.
            // However, the prompt says "mkdir home" (name) or "mkdir -p a/b/c" (path).
            // I will assume without -p, we only support names in current dir, OR strict path checking.
            // "create each named directory directly inside the current directory" suggests names only.
            // But let's support paths if they are direct children or valid? 
            // Actually, standard mkdir fails if parent doesn't exist.
            // Let's stick to creating in 'current' if it's just a name, or resolve parent if it's a path?
            // "create each named directory directly inside the current directory" -> implies arguments are names.
            // But if I do `mkdir a/b` without -p, it usually fails if a doesn't exist.
            // I will implement: resolve parent, try to create child.
            
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

    public void touch(String name, int size) {
        // touch creates in current directory usually, unless path provided?
        // Prompt says "Create a new empty file ... in the current directory".
        // So we assume 'name' is just a name, not a path.
        
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

    public void echo(String content, String path) {
        Object[] result = resolveParentAndName(path);
        if (result == null || result[0] == null) {
            // If parent doesn't exist, we can't create the file (unless we imply -p, but spec says "assume needed directories already exist")
            // Wait, "assume needed directories already exist" means we don't need to create them.
            // But if they DON'T exist, we should probably fail or just error.
            // "If file does not exist, create it (assume needed directories already exist)."
            // This implies we just resolve parent.
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

    public void ls() {
        List<Node> children = new ArrayList<>(current.getChildren());
        // Sort by name for consistent output
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

    public void pwd() {
        System.out.println(getCurrentPath());
    }

    public void rm(String name) {
        Node child = current.getChild(name);
        if (child == null) {
            System.out.println("Error: '" + name + "' not found.");
            return;
        }
        
        if (child instanceof Directory) {
            if (child.getSize() > 0) { // Directory size is sum of children, if > 0 it might have children? 
                // Wait, empty directory size is 0. 
                // But what if it contains empty files? Size is 0.
                // Better to check if children collection is empty.
                if (!((Directory) child).getChildren().isEmpty()) {
                    System.out.println("Error: Cannot remove directory '" + name + "'. It is not empty.");
                    return;
                }
            }
        }
        current.removeChild(name);
    }

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

    private void removeRecursiveHelper(Directory dir) {
        // Create a copy to avoid concurrent modification
        List<Node> children = new ArrayList<>(dir.getChildren());
        for (Node child : children) {
            if (child instanceof Directory) {
                removeRecursiveHelper((Directory) child);
            }
            dir.removeChild(child.getName());
        }
    }

    /**
     * Implements the 'tree' command.
     * Recursively displays the directory structure.
     * Uses the hash-table children from Directory to traverse the tree.
     */
    public void tree() {
        System.out.println(".");
        printTree(current, "");
    }

    /**
     * Recursive helper for tree.
     * @param dir The directory to print.
     * @param prefix The string prefix for indentation and tree branches.
     */
    private void printTree(Directory dir, String prefix) {
        List<Node> children = new ArrayList<>(dir.getChildren());
        // Sort children by name for consistent output
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

    /**
     * Implements the 'grep' command using the KMP algorithm.
     * Searches for a pattern in a file's content.
     */
    public void grep(String pattern, String filename) {
        // Resolve the file path (filename can be a path)
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
        
        // KMP Search
        if (kmpSearch(content, pattern)) {
            System.out.println("Pattern \"" + pattern + "\" found in " + name + ".");
        } else {
            System.out.println("Pattern \"" + pattern + "\" not found in " + name + ".");
        }
    }

    /**
     * KMP Search Algorithm.
     * Checks if pattern exists in text.
     * Time Complexity: O(N + M) where N is text length, M is pattern length.
     */
    private boolean kmpSearch(String text, String pattern) {
        if (pattern.isEmpty()) return true;
        
        int[] lps = buildLPS(pattern);
        int i = 0; // index for text
        int j = 0; // index for pattern
        int n = text.length();
        int m = pattern.length();
        
        while (i < n) {
            if (pattern.charAt(j) == text.charAt(i)) {
                j++;
                i++;
            }
            if (j == m) {
                return true; // Pattern found
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

    /**
     * Builds the Longest Prefix Suffix (LPS) array for KMP.
     * lps[i] stores the length of the longest proper prefix of pattern[0..i]
     * that is also a suffix of pattern[0..i].
     */
    private int[] buildLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0; // length of the previous longest prefix suffix
        int i = 1;
        lps[0] = 0; // lps[0] is always 0
        
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

    /**
     * Implements the 'du' command.
     * Calculates the total size of the current directory recursively.
     */
    public void du() {
        // Directory.getSize() is already recursive, summing children sizes.
        int totalSize = current.getSize();
        System.out.println("Total size: " + totalSize + "B");
    }
}
