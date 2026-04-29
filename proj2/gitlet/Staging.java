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
        addition = new HashMap<>();
        removal = new HashSet<>();
        writeObject(Repository.STAGING_DIR, this);
    }

}
