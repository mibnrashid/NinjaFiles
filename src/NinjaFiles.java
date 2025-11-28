

import java.util.Scanner;

/**
 * Main entry point for the NinjaFiles application.
 * Handles user input and command parsing.
 */
public class NinjaFiles {

    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Welcome to NinjaFiles! Type 'exit' to quit.");

        while (true) {
            // Print prompt
            System.out.print(fs.getCurrentPath() + "$ ");
            
            if (!scanner.hasNextLine()) {
                break;
            }
            
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            // Simple command parsing
            String[] parts = line.split("\\s+");
            String command = parts[0];

            if (command.equals("exit")) {
                break;
            }

            switch (command) {
                case "mkdir":
                    boolean makeParents = false;
                    int startIndex = 1;
                    if (parts.length > 1 && parts[1].equals("-p")) {
                        makeParents = true;
                        startIndex = 2;
                    }
                    if (startIndex >= parts.length) {
                        System.out.println("Usage: mkdir [-p] <dir1> <dir2> ...");
                    } else {
                        for (int i = startIndex; i < parts.length; i++) {
                            fs.mkdir(parts[i], makeParents);
                        }
                    }
                    break;
                case "touch":
                    if (parts.length == 3) {
                        try {
                            int size = Integer.parseInt(parts[2]);
                            fs.touch(parts[1], size);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Size must be an integer.");
                        }
                    } else {
                        System.out.println("Usage: touch <name> <size>");
                    }
                    break;
                case "echo":
                    // Parse: echo "text" > path
                    int firstQuote = line.indexOf('"');
                    int lastQuote = line.lastIndexOf('"');
                    int greaterThan = line.lastIndexOf('>');
                    
                    if (firstQuote != -1 && lastQuote != -1 && lastQuote > firstQuote && greaterThan > lastQuote) {
                        String text = line.substring(firstQuote + 1, lastQuote);
                        String path = line.substring(greaterThan + 1).trim();
                        fs.echo(text, path);
                    } else {
                        System.out.println("Usage: echo \"<text>\" > <filename>");
                    }
                    break;
                case "ls":
                    fs.ls();
                    break;
                case "cd":
                    if (parts.length > 1) fs.cd(parts[1]);
                    else System.out.println("Usage: cd <path>");
                    break;
                case "pwd":
                    fs.pwd();
                    break;
                case "rm":
                    if (parts.length > 1) {
                        if (parts[1].equals("-r")) {
                            if (parts.length > 2) {
                                fs.rmRecursive(parts[2]);
                            } else {
                                System.out.println("Usage: rm -r <name>");
                            }
                        } else {
                            fs.rm(parts[1]);
                        }
                    } else {
                        System.out.println("Usage: rm <name> or rm -r <name>");
                    }
                    break;
                case "tree":
                    fs.tree();
                    break;
                case "grep":
                    // Parse: grep "pattern" filename
                    int firstQuoteGrep = line.indexOf('"');
                    int lastQuoteGrep = line.lastIndexOf('"');
                    
                    if (firstQuoteGrep != -1 && lastQuoteGrep != -1 && lastQuoteGrep > firstQuoteGrep) {
                        String pattern = line.substring(firstQuoteGrep + 1, lastQuoteGrep);
                        String filename = line.substring(lastQuoteGrep + 1).trim();
                        if (filename.isEmpty()) {
                             System.out.println("Usage: grep \"<pattern>\" <filename>");
                        } else {
                            fs.grep(pattern, filename);
                        }
                    } else {
                        // Fallback for simple patterns without quotes (optional, but good for usability)
                        if (parts.length > 2) {
                             // If user didn't use quotes, just take the second arg as pattern
                             fs.grep(parts[1], parts[2]);
                        } else {
                             System.out.println("Usage: grep \"<pattern>\" <filename>");
                        }
                    }
                    break;
                case "du":
                    fs.du();
                    break;
                default:
                    System.out.println("Unknown command: " + command);
            }
        }
        
        scanner.close();
        System.out.println("Goodbye!");
    }
}
