# Gitlet Design Document


## Classes and Data Structures

### Commit
代表 Gitlet 仓库中的一个提交快照（Snapshot）。实现了 `Serializable` 接口。
#### Fields
* `Date time`: 提交被创建的时间戳。初始提交为 Unix 元年（1970-01-01 00:00:00），其他提交为当前真实时间。
* `String message`: 该次提交的描述信息（Commit message）。
* `String prev`: 父节点（上一次提交）的 SHA-1 哈希值。初始提交的父节点为 `null`。
* `HashMap<String, String> trackedFiles`: 记录该次提交追踪的所有文件。Key 为文件名（如 `"wug.txt"`），Value 为该文件内容对应的 Blob 的 SHA-1 哈希值。

### StagingArea
代表当前的暂存区状态，作为一个对象被整体序列化保存。实现了 `Serializable` 接口。
#### Fields
* `HashMap<String, String> added`: 记录准备被新增或修改的文件。Key 为文件名，Value 为新文件内容对应的 Blob 的 SHA-1 哈希值。
* `HashSet<String> removed`: 记录准备在下一次提交中被删除的文件名（被 `rm` 命令标记）。

### Repository
整个 Gitlet 系统的“中央控制室”，全部由静态变量和静态方法组成，负责执行所有的命令逻辑和底层文件读写。
#### Fields
* `static final File CWD`: 当前工作目录。
* `static final File GITLET_DIR`: `.gitlet` 隐藏文件夹的路径。
* `static final File COMMIT_DIR`: 存放 Commit 序列化对象的目录 (`.gitlet/objects/commits/`)。
* `static final File BLOB_DIR`: 存放文件内容快照的目录 (`.gitlet/objects/blobs/`)。
* `static final File HEAD_DIR`: 存放分支指针的目录 (`.gitlet/refs/heads/`)。
* `static final File HEAD_FILE`: 记录当前所处分支的指针文件 (`.gitlet/HEAD`)。
* `static final File STAGE_FILE`: 存放序列化 `StagingArea` 对象的文件 (`.gitlet/staging_area`)。


## Algorithms

### `init()`
1. 检查 `GITLET_DIR` 是否已存在。如果存在，打印错误并退出。
2. 使用 `mkdir()` 创建所有必要的目录结构（对象区、分支区等）。
3. 实例化一个空的 `StagingArea` 并序列化保存到 `STAGE_FILE`。
4. 调用 `Commit` 的无参构造函数生成 Initial Commit。
5. 计算 Initial Commit 的 SHA-1 并将其序列化保存到 `COMMIT_DIR`。
6. 创建 `master` 分支文件，将 Initial Commit 的哈希值写入其中。
7. 创建 `HEAD` 文件，写入字符串 `"master"`。

### `add(String filename)`
1. 检查工作区是否存在该文件。不存在则报错。
2. 读取该文件内容，并计算其内容的 SHA-1 哈希值。
3. 获取当前的 `Commit` 对象，检查该文件是否已被追踪，且哈希值是否与当前工作区完全相同：
    * 如果完全相同：说明文件未被修改。如果该文件目前在暂存区的 `added` 或 `removed` 集合中，将其从中移除。
    * 如果不同（或之前未被追踪）：将文件内容作为 Blob 保存到 `BLOB_DIR`。反序列化 `StagingArea` 对象，将 `<filename, SHA-1>` 键值对存入 `added` 集合，最后将 `StagingArea` 重新序列化保存。

### `commit(String message)`
1. 反序列化读取 `STAGE_FILE` 拿到当前的 `StagingArea` 对象。如果 `added` 和 `removed` 均为空，报错并退出。
2. 获取当前的 `Commit` 对象（通过读取 `HEAD` -> `master` 拿到哈希值），以此作为父节点。
3. 克隆父节点的 `trackedFiles` 映射表。
4. 根据 `StagingArea` 更新克隆的映射表：
    * 将 `added` 中的键值对加入映射表（如果有同名文件则直接覆盖哈希值）。
    * 将 `removed` 中的文件从映射表中移除。
5. 使用带参构造函数创建一个新的 `Commit` 对象。
6. 计算新 `Commit` 的 SHA-1 值并将其序列化保存到 `COMMIT_DIR`。
7. 更新当前分支文件（如 `master`）的内容为这个新的 SHA-1 哈希值。
8. 清空 `StagingArea` 并重新保存。


## Persistence

Gitlet 采用无状态（Stateless）设计，每次命令执行完毕后程序结束，所有数据依赖硬盘上的 `.gitlet` 目录进行持久化。

### 目录结构 (Directory Structure)
```text
.gitlet/
├── objects/
│   ├── commits/      (存储序列化后的 Commit 对象，文件名为该对象的 SHA-1)
│   └── blobs/        (存储真实文件的字节数据快照，文件名为文件内容的 SHA-1)
├── refs/
│   └── heads/        (存储各个分支指针，文件名为分支名，文件内容为指向的 Commit SHA-1 纯文本)
├── HEAD              (纯文本文件，存储当前所在的分支名称，如 "master")
└── staging_area      (存储序列化后的 StagingArea 对象)