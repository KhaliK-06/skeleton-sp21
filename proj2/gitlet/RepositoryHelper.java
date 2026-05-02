package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static gitlet.Utils.*;
import static gitlet.Repository.*;

public class RepositoryHelper {
    public static Commit getCommit(String id) {
        if (id == null) {
            return null;
        }
        File commit;
        if (id.length() < 40) {
            String shortId = getId(id);
            commit = join(COMMIT_DIR, shortId);
        } else {
            commit = join(COMMIT_DIR, id);
        }

        if (!commit.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return readObject(commit, Commit.class);
    }

    public static String getId(String id) {
        int length = id.length();
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        for (String commit : commitList) {
            String subCommit = commit.substring(0, length);
            if (subCommit.equals(id)) {
                return commit;
            }
        }
        return id;
    }

    public static void saveCommit(Commit commit) {
        String name = sha1(serialize(commit));
        File in = join(COMMIT_DIR, name);
        writeObject(in, commit);
    }

    public static Commit getCurrentCommit() {
        String branchName = readContentsAsString(HEAD);
        File branchFile = join(HEAD_DIR, branchName);
        String commitHash = readContentsAsString(branchFile);
        return getCommit(commitHash);
    }

    public static Commit getBranchCommit(String branch) {
        File branchFile = join(HEAD_DIR, branch);
        String commitHash = readContentsAsString(branchFile);
        return getCommit(commitHash);
    }

    public static Staging getCurrentStage() {
        return readObject(STAGE_FILE, Staging.class);
    }

    public static List<String> getModifiedFiles() {
        Commit currentCommit = getCurrentCommit();
        HashMap<String, String> track = currentCommit.trackedFiles();
        Staging stage = getCurrentStage();
        HashSet<String> rm = stage.removal();
        HashMap<String, String> add = stage.addition();
        List<String> rmList = new ArrayList<>(rm);
        List<String> addList = new ArrayList<>(add.keySet());
        List<String> cwd = plainFilenamesIn(CWD);
        List<String> modified = new ArrayList<>();
        for (String file : cwd) {
            File fFile = join(CWD, file);
            String fileHash = sha1(readContents(fFile));
            if (addList.contains(file) && !fileHash.equals(add.get(file))) {
                modified.add(file + " (modified)");
            } else if (track.containsKey(file) && !addList.contains(file) && !fileHash.equals(track.get(file))) {
                modified.add(file + " (modified)");
            }
        }
        for (String file : addList) {
            if (!cwd.contains(file)) {
                modified.add(file + " (deleted)");
            }
        }
        for (String file : track.keySet()) {
            if (!rmList.contains(file) && !cwd.contains(file)) {
                modified.add(file + " (deleted)");
            }
        }
        return modified;
    }

    public static List<String> getUntrackedFiles() {
        Commit currentCommit = getCurrentCommit();
        HashMap<String, String> track = currentCommit.trackedFiles();
        Staging stage = getCurrentStage();
        HashSet<String> rm = stage.removal();
        HashMap<String, String> add = stage.addition();
        List<String> rmList = new ArrayList<>(rm);
        List<String> addList = new ArrayList<>(add.keySet());
        List<String> untracked = new ArrayList<>();
        List<String> cwd = plainFilenamesIn(CWD);
        for (String file : cwd) {
            File fFile = join(CWD, file);
            String fileHash = sha1(readContents(fFile));
            if (!addList.contains(file) && !track.containsKey(file)) {
                untracked.add(file);
            } else if (rmList.contains(file)) {
                untracked.add(file);
            }
        }
        return untracked;
    }

    public static void untrackCheck(HashMap<String, String> track) {
        List<String> untrack = getUntrackedFiles();
        for (String file : untrack) {
            if (track.containsKey(file)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }
}
