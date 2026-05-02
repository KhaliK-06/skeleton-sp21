package gitlet;


import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  Head pointer
 *  the main tree
 *  does at a high level.
 */
public class Commit implements Serializable {
    /*
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    private Date time;
    private String message;
    private String prev;
    private HashMap<String, String> trackedFiles;
    private String mergePrev = null;

    public Commit() {
        time = new Date(0);
        message = "initial commit";
        prev = null;
        trackedFiles = new HashMap<>();
    }

    public Commit(Date time, String message, String prev, HashMap<String, String> files) {
        this.time = time;
        this.message = message;
        this.prev = prev;
        trackedFiles = files;
    }

    public Commit(Date time, String message, String prev,
                  String mergePrev, HashMap<String, String> files) {
        this.time = time;
        this.message = message;
        this.prev = prev;
        this.mergePrev = mergePrev;
        trackedFiles = files;
    }

    public Date time() {
        return time;
    }

    public String message() {
        return message;
    }

    public String prev() {
        return prev;
    }

    public String mergePrev() {
        return mergePrev;
    }

    public HashMap<String, String> trackedFiles() {
        return trackedFiles;
    }

    public boolean isMerge() {
        return mergePrev != null;
    }
}
