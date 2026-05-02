package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.RepositoryHelper.*;

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
    static File HEAD = join(GITLET_DIR, "HEAD");


    public static void setupRepo()  {
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        REF_DIR.mkdir();
        HEAD_DIR.mkdir();
        STAGING_DIR.mkdir();
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeContents(HEAD, "master");
        Staging staging = new Staging();
        File stagingF = join(STAGE_FILE);
        writeObject(stagingF, staging);
    }

    public static void add(String fileName) {
        // initial the staging_area
        File stageFile = join(STAGE_FILE);
        Staging currentStage = getCurrentStage();

        if (currentStage.removal().contains(fileName)) {
            currentStage.removal().remove(fileName);
        }

        File targetFile = join(CWD, fileName);
        if (!targetFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        byte[] content = readContents(targetFile);
        String fileHash = sha1(content);
        Commit currentCommit = getCurrentCommit();
        String commitHash = currentCommit.trackedFiles().get(fileName);

        if (fileHash.equals(commitHash)) {
            currentStage.addition().remove(fileName);
        } else {
            currentStage.addition().put(fileName, fileHash);
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
        HashMap<String, String> oldTrack = currentCommit.trackedFiles();
        HashMap<String, String> track = new HashMap<>(oldTrack);
        HashSet<String> rm = currentStage.removal();
        HashMap<String, String> add = currentStage.addition();
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
        String branchName = readContentsAsString(HEAD);
        File branchFile = join(HEAD_DIR, branchName);
        String parentHash = readContentsAsString(branchFile);
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
        HashSet<String> rm = currentStage.removal();
        HashMap<String, String> add = currentStage.addition();
        Commit currentCommit = getCurrentCommit();
        HashMap<String, String> track = currentCommit.trackedFiles();

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
        String branchName = readContentsAsString(HEAD);
        File branchFile = join(HEAD_DIR, branchName);
        String hash = readContentsAsString(branchFile);
        Commit commit = getCommit(hash);
        while (commit != null) {
            System.out.println("===");
            System.out.println("commit " + hash);
            if (commit.isMerge()) {
                String first = commit.prev().substring(0, 7);
                String second = commit.mergePrev().substring(0, 7);
                System.out.println("Merge: " + first + second);
            }
            System.out.println("Date: " + sdf.format(commit.time()));
            System.out.println(commit.message());
            System.out.println();
            hash = commit.prev();
            if (commit.prev() == null) {
                commit = null;
            } else {
                commit = getCommit(commit.prev());
            }
        }
    }

    public static void gLog() {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        assert commits != null;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        for (String commit : commits) {
            Commit c = getCommit(commit);
            String hash = commit;
            System.out.println("===");
            System.out.println("commit " + hash);
            if (c.isMerge()) {
                String first = c.prev().substring(0, 7);
                String second = c.mergePrev().substring(0, 7);
                System.out.println("Merge: " + first + second);
            }
            System.out.println("Date: " + sdf.format(c.time()));
            System.out.println(c.message());
            System.out.println();
        }
    }

    public static void find(String message) {
        boolean isFind = false;
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        assert commits != null;
        for (String commit : commits) {
            Commit c = getCommit(commit);
            if (c.message().equals(message)) {
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
        HashSet<String> rm = stage.removal();
        HashMap<String, String> add = stage.addition();
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
        // modified but not staged and untrack
        List<String> modified = getModifiedFiles();
        List<String> untracked = getUntrackedFiles();
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

    public static void checkout(Commit commit, String file) {
        HashMap<String, String> track = commit.trackedFiles();
        if (!track.containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String fileHash = track.get(file);
        File fFile = join(CWD, file);
        File fileBlob = join(BLOB_DIR, fileHash);
        writeContents(fFile, readContents(fileBlob));
    }

    public static void checkout(String branch) {
        File bBranch = join(HEAD_DIR, branch);
        if (!bBranch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (branch.equals(readContentsAsString(HEAD))) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit branchCommit = getBranchCommit(branch);
        HashMap<String, String> track = branchCommit.trackedFiles();
        untrackCheck(track);
        for (String file : track.keySet()) {
            File fFile = join(CWD, file);
            File blob = join(BLOB_DIR, track.get(file));
            writeContents(fFile, readContents(blob));
        }
        Commit currentCommit = getCurrentCommit();
        HashMap<String, String> currentTrack = currentCommit.trackedFiles();
        for (String file : currentTrack.keySet()) {
            if (!track.keySet().contains(file)) {
                File fFile = join(CWD, file);
                restrictedDelete(fFile);
            }
        }
        Staging stage = getCurrentStage();
        stage.clear();
        writeContents(HEAD, branch);
    }

    public static void branch(String branch) {
        File branchFile = join(HEAD_DIR, branch);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        String currentBranch = readContentsAsString(HEAD);
        File curBranch = join(HEAD_DIR, currentBranch);
        String currentCommit = readContentsAsString(curBranch);
        writeContents(branchFile, currentCommit);
    }

    public static void rmBranch(String branch) {
        File branchFile = join(HEAD_DIR, branch);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branch.equals(readContentsAsString(HEAD))) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        restrictedDelete(branchFile);
    }

    public static void reset(String id) {
        String commitId = getId(id);
        Commit commit = getCommit(commitId);
        HashMap<String, String> track = commit.trackedFiles();
        untrackCheck(track);
        List<String> cwd = plainFilenamesIn(CWD);
        for (String file : cwd) {
            if (!track.containsKey(file)) {
                File fFile = join(CWD, file);
                restrictedDelete(fFile);
            }
        }
        for (String file : track.keySet()) {
            checkout(commit, file);
        }
        File curBranch = join(HEAD_DIR, readContentsAsString(HEAD));
        writeContents(curBranch, commitId);
        Staging stage = getCurrentStage();
        stage.clear();
    }

    public static void merge(String branch) {
        Staging stage = getCurrentStage();
        File branchFile = join(HEAD_DIR, branch);

        if (!stage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (branch.equals(readContentsAsString(HEAD))) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        String curBranchName = readContentsAsString(HEAD);
        String curCommitHash = readContentsAsString(join(HEAD_DIR, curBranchName));
        String givenCommitHash = readContentsAsString(branchFile);
        String splitCommitHash = findSplit(branch);

        if (curCommitHash.equals(splitCommitHash)) {
            checkout(branch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        } else if (givenCommitHash.equals(splitCommitHash)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        Commit currentCommit = getCurrentCommit();
        Commit givenCommit = getBranchCommit(branch);
        Commit splitCommit = getCommit(splitCommitHash);

        HashMap<String, String> currentTrack = new HashMap<>(currentCommit.trackedFiles());
        HashMap<String, String> givenTrack = new HashMap<>(givenCommit.trackedFiles());
        HashMap<String, String> splitTrack = new HashMap<>(splitCommit.trackedFiles());

        mergeUntrackCheck(splitTrack, givenTrack);

        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(currentTrack.keySet());
        allFiles.addAll(givenTrack.keySet());
        allFiles.addAll(splitTrack.keySet());

        boolean isConflicted = false;

        for (String file : allFiles) {
            String fileInSplit = splitTrack.get(file);
            String fileInCur = currentTrack.get(file);
            String fileInGiven = givenTrack.get(file);


            if (Objects.equals(fileInSplit, fileInCur) &&
                    !Objects.equals(fileInSplit, fileInGiven)) {
                if (fileInGiven != null) {
                    checkout(givenCommit, file);
                    stage.addition().put(file, fileInGiven);
                } else {
                    rm(file);
                }
            }
            // conflict
            else if (!Objects.equals(fileInSplit, fileInCur) &&
                    !Objects.equals(fileInSplit, fileInGiven)
                    && !Objects.equals(fileInCur, fileInGiven)) {
                isConflicted = true;

                String currContent = (fileInCur == null) ? "" :
                        readContentsAsString(join(BLOB_DIR, fileInCur));
                String givenContent = (fileInGiven == null) ? "" :
                        readContentsAsString(join(BLOB_DIR, fileInGiven));

                String conflictContent = "<<<<<<< HEAD\n" + currContent +
                        "=======\n" + givenContent + ">>>>>>>\n";

                File targetFile = join(CWD, file);
                writeContents(targetFile, conflictContent);

                String newHash = sha1(conflictContent);
                File blobFile = join(BLOB_DIR, newHash);
                writeContents(blobFile, conflictContent.getBytes());
                stage.addition().put(file, newHash);
            }
        }

        writeObject(STAGE_FILE, stage);

        String msg = "Merged " + branch + " into " + curBranchName + ".";
        commitMerge(msg, givenCommitHash);

        if (isConflicted) {
            System.out.println("Encountered a merge conflict.");
        }
    }

}
