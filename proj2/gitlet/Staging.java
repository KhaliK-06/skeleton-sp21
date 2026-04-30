package gitlet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import static gitlet.Utils.*;


public class Staging implements Serializable {

    // <filename, sha1_value>
    public HashMap<String, String> addition = new HashMap<>();

    // <filename>
    public HashSet<String> removal = new HashSet<>();

    public void clear() {
        addition.clear();
        removal.clear();
        writeObject(Repository.STAGE_FILE, this);
    }

    public void update() {
        writeObject(Repository.STAGE_FILE, this);
    }

}
