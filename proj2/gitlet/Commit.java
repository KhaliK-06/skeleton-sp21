package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import static gitlet.Utils.*;

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

    public Date time;
    public String message;
    public String prev;
    public HashMap<String, String> trackedFiles;

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


}
