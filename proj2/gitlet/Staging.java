package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import static gitlet.Utils.*;


public class Staging implements Serializable {

    // <filename, sha1_value>
    private HashMap<String, String> addition = new HashMap<>();

    // <filename>
    private HashSet<String> removal = new HashSet<>();

    public void clear() {
        addition.clear();
        removal.clear();
        writeObject(Repository.STAGE_FILE, this);
    }

    public void update() {
        writeObject(Repository.STAGE_FILE, this);
    }

    public HashMap<String, String> addition() {
        return  addition;
    }

    public HashSet<String> removal() {
        return  removal;
    }

    public boolean isEmpty() {
        return addition.isEmpty() && removal.isEmpty();
    }

}
