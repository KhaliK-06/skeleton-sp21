package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

// Driver class for Gitlet, a subset of the Git version-control system.

public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 2);
                File gitlet = join(".gitlet");
                if (gitlet.exists()) {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    break;
                }
                Repository.setupRepo();
                Commit init = new Commit();
                Repository.saveCommit(init);
                String initSha1 = sha1(init);
                File master = join(Repository.HEAD_DIR, "master");
                writeContents(master, initSha1);
                break;
            case "add":
                initialCheck();
                validateNumArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                initialCheck();
                if (args.length == 1) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                validateNumArgs(args, 2);
                String message = args[1];
                // from Gemini:avoid space message
                if (message.trim().isEmpty()) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.commit(message);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void initialCheck() {
        if (!Repository.GITLET_DIR.exists() || !Repository.GITLET_DIR.isDirectory()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
