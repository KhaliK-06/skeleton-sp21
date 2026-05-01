package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 * the structure of the .gitlet:
 * .gitlet/
 * ├── objects/         (对象区)
 * │   ├── commits/     (存放所有的 Commit 快照，文件名是哈希值)
 * │   └── blobs/       (存放所有的文件内容快照，文件名是哈希值)
 * ├── refs/            (指针区)
 * │   ├── heads/       (存放各个分支的最新 Commit哈希值从commit文件夹中查找)
 * │   │   └── master   (初始分支)
 * │   └── ...          (分支)
 * ├── HEAD             (记录当前你在哪个分支上，在refs/heads中查找文件名即可获得各分支最新的commit)
 * └── staging/         (暂存区)
 *     └── staging_area (存储Staging实例)
 * method:
 *   setupRepo()
 *      initial the repo
 *   getCommit()
 *      get the commit with id
 *   saveCommit()
 *      save the commit
 *  @author TODO
 *
 */
public class Repository {
    /*
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECT_DIR = join(CWD, ".gitlet", "objects");
    public static final File COMMIT_DIR = join(CWD, ".gitlet", "objects", "commits");
    public static final File BLOB_DIR = join(CWD, ".gitlet", "objects", "blobs");
    public static final File REF_DIR = join(CWD, ".gitlet", "refs");
    public static final File HEAD_DIR = join(CWD, ".gitlet", "refs", "heads");
    public static final File STAGING_DIR = join(CWD, ".gitlet", "staging");
    public static final File STAGE_FILE = join(STAGING_DIR, "staging_area");
    private static File HEAD = join(GITLET_DIR, "HEAD");


    public static void setupRepo() throws IOException {
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        REF_DIR.mkdir();
        HEAD_DIR.mkdir();
        STAGING_DIR.mkdir();
        HEAD.createNewFile();
        writeContents(HEAD, "master");
        Staging staging = new Staging();
        File stagingF = join(STAGE_FILE);
        writeObject(stagingF, staging);
    }

    public static Commit getCommit(String id) {
        if (id == null) {
            return null;
        }
        File commit = join(COMMIT_DIR, id);
        if (commit.exists()) {
            return readObject(commit, Commit.class);
        }
        return null;
    }

    public static void saveCommit (Commit commit) {
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

    public static Staging getCurrentStage() {
        return readObject(STAGE_FILE, Staging.class);
    }

    public static void add(String fileName) {
        // initial the staging_area
        File stageFile = join(STAGE_FILE);
        Staging currentStage = getCurrentStage();

        if (currentStage.removal.contains(fileName)) {
            currentStage.removal.remove(fileName);
        }

        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        byte[] content = readContents(targetFile);
        String fileHash = sha1(content);
        Commit currentCommit = getCurrentCommit();
        String commitHash = currentCommit.trackedFiles.get(fileName);

        if (fileHash.equals(commitHash)) {
            currentStage.addition.remove(fileName);
        } else {
            currentStage.addition.put(fileName, fileHash);
            //save in the blobs
            File blobFile = join(BLOB_DIR, fileHash);
            writeContents(blobFile, content);
        }
        // put it back
        writeObject(stageFile, currentStage);
    }

    public static void commit(String message) {
        //get current state
        Commit currentCommit = getCurrentCommit();
        Staging currentStage = getCurrentStage();
        HashMap<String, String> oldTrack = currentCommit.trackedFiles;
        HashMap<String, String> track = new HashMap<>(oldTrack);
        HashSet<String> rm = currentStage.removal;
        HashMap<String, String> add = currentStage.addition;
        if (rm.isEmpty() && add.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        //edit trackFiles
        for (String item : rm) {
            track.remove(item);
        }
        track.putAll(add);
        //create new commit
        String parentHash = sha1(serialize(currentCommit));
        Commit newCommit = new Commit(new Date(), message, parentHash, track);
        //update HEAD and refs
        saveCommit(newCommit);
        String currentBranch = readContentsAsString(HEAD);
        File refs = join(HEAD_DIR, currentBranch);
        String newCommitHash = sha1(serialize(newCommit));
        writeContents(refs, newCommitHash);

        currentStage.clear();
    }

    public static void rm(String fileName) {
        //initial
        Staging currentStage = getCurrentStage();
        HashSet<String> rm = currentStage.removal;
        HashMap<String, String> add = currentStage.addition;
        Commit currentCommit = getCurrentCommit();
        HashMap<String, String> track = currentCommit.trackedFiles;

        if (!track.containsKey(fileName) && !add.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        } else if (track.containsKey(fileName)) {
            //if file has been commit, rm it
            rm.add(fileName);
            restrictedDelete(join(CWD, fileName));
        }
        //if add has rm file, rm it
        add.remove(fileName);
        currentStage.update();
    }

    public static void log() {
        // format
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        Commit commit = getCurrentCommit();
        while (commit != null) {
            String hash = sha1(serialize(commit));
            System.out.println("===");
            System.out.println("commit " + hash);
            // TODO : merge situation
            System.out.println("Date: " + sdf.format(commit.time));
            System.out.println(commit.message);
            System.out.println();
            if (commit.prev == null) {
                commit = null;
            } else {
                commit = getCommit(commit.prev);
            }
        }
    }

    public static void g_log() {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        assert commits != null;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        for (String commit : commits) {
            Commit c = getCommit(commit);
            String hash = commit;
            System.out.println("===");
            System.out.println("commit " + hash);
            // TODO : merge situation
            System.out.println("Date: " + sdf.format(c.time));
            System.out.println(c.message);
            System.out.println();
        }
    }

    public static void find(String message) {
        boolean isFind = false;
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        assert commits != null;
        for (String commit : commits) {
            Commit c = getCommit(commit);
            if (c.message.equals(message)) {
                System.out.println(commit);
                    isFind = true;
            }
        }
        if (!isFind) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        //branch
        System.out.println("=== Branches ===");
        List<String> branches = plainFilenamesIn(HEAD_DIR);
        Collections.sort(branches);
        for (String branch : branches) {
            if (branch.equals(readContentsAsString(HEAD))) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        //staging
        Staging stage = getCurrentStage();
        HashSet<String> rm = stage.removal;
        HashMap<String, String> add = stage.addition;
        List<String> rmList = new ArrayList<>(rm);
        List<String> addList = new ArrayList<>(add.keySet());
        Collections.sort(rmList);
        Collections.sort(addList);
        System.out.println("=== Staged Files ===");
        for (String key : addList) {
            System.out.println(key);
        }
        System.out.println();
        //rm
        System.out.println("=== Removed Files ===");
        for (String key : rmList) {
            System.out.println(key);
        }
        System.out.println();
        // modified but not staged
        List<String> modified = new ArrayList<>();
        List<String> untracked = new ArrayList<>();
        List<String> cwd = plainFilenamesIn(CWD);
        Commit currentCommit = getCurrentCommit();
        HashMap<String, String> track = currentCommit.trackedFiles;
        //untrack
        for (String file : cwd) {
            File file_ = join(CWD, file);
            String fileHash = sha1(readContents(file_));
            if (!addList.contains(file) && !track.containsKey(file)) {
                untracked.add(file);
            } else if (rmList.contains(file)) {
                untracked.add(file);
            } else if (addList.contains(file) && !fileHash.equals(add.get(file))) {
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
        Collections.sort(untracked);
        Collections.sort(modified);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String file : modified) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String file : untracked) {
            System.out.println(file);
        }
        System.out.println();
    }


}
