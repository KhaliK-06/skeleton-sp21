package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// Driver class for Gitlet, a subset of the Git version-control system.

public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                File gitlet = join(".gitlet");
                if (gitlet.exists()) {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    System.exit(0);
                }
                Repository.setupRepo();
                Commit init = new Commit();
                RepositoryHelper.saveCommit(init);
                File master = join(Repository.HEAD_DIR, "master");
                writeContents(master, sha1(serialize(init)));
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
            case "rm":
                initialCheck();
                validateNumArgs(args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                initialCheck();
                validateNumArgs(args, 1);
                Repository.log();
                break;
            case "global-log":
                initialCheck();
                validateNumArgs(args, 1);
                Repository.gLog();
                break;
            case "find":
                initialCheck();
                validateNumArgs(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                initialCheck();
                validateNumArgs(args, 1);
                Repository.status();
                break;
            case "checkout":
                initialCheck();
                checkout(args);
                break;
            case "branch":
                initialCheck();
                validateNumArgs(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                initialCheck();
                validateNumArgs(args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                initialCheck();
                validateNumArgs(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                initialCheck();
                validateNumArgs(args, 2);
                Repository.merge(args[1]);
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

    public static void checkout(String[] args) {
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            String fileName = args[2];
            Repository.checkout(RepositoryHelper.getCurrentCommit(), fileName);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            String commitId = args[1];
            String fileName = args[3];
            Repository.checkout(RepositoryHelper.getCommit(commitId), fileName);
        } else if (args.length == 2) {
            Repository.checkout(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

}
