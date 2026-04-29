package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 * the structure of the .gitlet:
 * .gitlet/
 * ├── objects/        (对象区)
 * │   ├── commits/    (存放所有的 Commit 快照，文件名是哈希值)
 * │   └── blobs/      (存放所有的文件内容快照，文件名是哈希值)
 * ├── refs/           (指针区)
 * │   ├── heads/      (存放各个分支的最新 Commit 哈希值)
 * │   │   └── master  (初始分支)
 * │   └── ...         (分支)
 * ├── HEAD            (记录当前你在哪个分支上，通常存一个字符串，比如 "ref: refs/heads/master")
 * └── staging/        (暂存区)
 *     ├── addition    (准备新加入的文件列表/内容)
 *     └── removal     (准备删除的文件列表)
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
    public static final File ADDITION_DIR = join(CWD, ".gitlet", "staging");
    public static final File REMOVAL_DIR = join(CWD, ".gitlet", "staging");
    private static File HEAD = join(GITLET_DIR, "HEAD");


    public static void setupRepo() throws IOException {
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        REF_DIR.mkdir();
        STAGING_DIR.mkdir();
        ADDITION_DIR.mkdir();
        REMOVAL_DIR.mkdir();
        HEAD.createNewFile();
        writeContents(HEAD, "master");
    }

    public static Commit getCommit(String id) {
        File commit = join(COMMIT_DIR, "id");
        if (commit.exists()) {
            return readObject(commit, Commit.class);
        }
        return null;
    }

    public static void saveCommit (Commit commit) {
        String name = sha1(commit);
        File in = join(COMMIT_DIR, name);
        writeObject(in, commit);
    }

    public static Commit getCurrentCommit() {
        return getCommit(readContentsAsString(HEAD));
    }

}
