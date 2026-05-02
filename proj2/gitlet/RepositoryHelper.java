package gitlet;

import java.io.File;
import java.util.*;
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
            } else if (track.containsKey(file) && !addList.contains(file) &&
                    !fileHash.equals(track.get(file))) {
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
                System.out.println("There is an untracked file in the way; " +
                        "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    public static String findSplit(String branch) {
        // get current commit
        String currentBranch = readContentsAsString(HEAD);
        File curBranchFile = join(HEAD_DIR, currentBranch);
        String currentHash = readContentsAsString(curBranchFile);
        // get given commit
        File givenBranchFile = join(HEAD_DIR, branch);
        String givenHash = readContentsAsString(givenBranchFile);
        // add current commit path ot the initial
        Queue<String> queue = new LinkedList<>();
        HashSet<String> ancestors = new HashSet<>();

        queue.add(currentHash);
        ancestors.add(currentHash);

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            Commit currCommit = getCommit(curr);
            // has prev and not recorded, add in the queue and set
            if (currCommit.prev() != null && !ancestors.contains(currCommit.prev())) {
                queue.add(currCommit.prev());
                ancestors.add(currCommit.prev());
            }
            if (currCommit.mergePrev() != null && !ancestors.contains(currCommit.mergePrev())) {
                queue.add(currCommit.mergePrev());
                ancestors.add(currCommit.mergePrev());
            }
        }
        // search in the given commit path
        Queue<String> givenQueue = new LinkedList<>();
        givenQueue.add(givenHash);

        while (!givenQueue.isEmpty()) {
            String curr = givenQueue.poll();
            if (ancestors.contains(curr)) {
                return curr;
            }
            Commit currCommit = getCommit(curr);
            if (currCommit.prev() != null) {
                givenQueue.add(currCommit.prev());
            }
            if (currCommit.mergePrev() != null) {
            givenQueue.add(currCommit.mergePrev());
            }
        }
        return null;
    }

    public static void mergeUntrackCheck(HashMap<String, String> splitTrack,
                                         HashMap<String, String> givenTrack) {
        List<String> untrack = getUntrackedFiles();
        for (String file : untrack) {
            if (givenTrack.containsKey(file) && !splitTrack.containsKey(file)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            } else if (givenTrack.containsKey(file) && splitTrack.containsKey(file)) {
                if (!givenTrack.get(file).equals(splitTrack.get(file))) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }

    public static void commitMerge(String message, String secondParentHash) {
        Commit currentCommit = getCurrentCommit();
        Staging currentStage = getCurrentStage();
        HashMap<String, String> track = new HashMap<>(currentCommit.trackedFiles());
        HashSet<String> rm = currentStage.removal();
        HashMap<String, String> add = currentStage.addition();

        for (String item : rm) {
            track.remove(item);
        }
        track.putAll(add);

        String branchName = readContentsAsString(HEAD);
        File branchFile = join(HEAD_DIR, branchName);
        String parentHash = readContentsAsString(branchFile);

        Commit newCommit = new Commit(new Date(), message, parentHash, secondParentHash, track);

        saveCommit(newCommit);
        String newCommitHash = sha1(serialize(newCommit));
        writeContents(branchFile, newCommitHash);

        currentStage.clear();
    }

}
