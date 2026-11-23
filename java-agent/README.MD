# -- Lab 4 Readme --


### Risk Table
|---|---|---|---|---|---|---|
| Risk | Tool | Category | Likelihood | Impact | Current Control | Proposed Mitigation |
| Sandbox escape through symlinks/junctions | GetFileContent, WriteFile | Security | M | H | normalize() – only validates path text | Use toRealPath() to canonicalize paths |
| Unrestricted sandbox Maven execution defeats file-tool containment | RunJavaTests | Security | M | H | None | Use -offline mode, require confirmation on modified pom.xml |
| Sensitive file disclosure without approval or filtering | GetFileContent | Human Oversight | H | H | None | Require confirmation before logging, whitelist, filtering |
| Time-of-check/time-of-use race | GetFileContent | Security | L | M | Asynchronous path checking | Open FileChannel, then use FileLock (Windows only)  |
| UTF-8 and character-boundary failures | GetFileContent | Reliability | L | L | 10k Java character limit | Check if last character is first part of surrogate pair – if so, read next part also |
| Non-atomic destructive writes | WriteFile | Security | L | M | Asynchronous path checking | Write to temp file, rename to actual file, Confirmation on write |
| Error reporting loses diagnostic context | GetFileContent | Maintainabilty | M | L | Generic error message | Pass through the original error to allow identification of path and failure type |

### Implemented Safeguards
- Write confirmation:
    - WriteFileTool can be constructed with confirmation mode enabled.
    - The `--confirm-writes` / `-cw` command-line flag enables confirmation for tool-demo writes.
    - Confirmation accepts `y` or `yes`; empty, EOF, read errors, and every other response cancel the write.
- Sandbox escape through symbolic links:
    - AgentTool - Updated validatePath() to use toRealPath() instead of normalize(). This allows symbolic links to be expanded before sandbox containment checks.
    - WriteFileTool - Tweaked logic for path validation, as toRealPath() fails on nonexistent paths, but the tool should still create a new file in an existing directory. If the file exists, we can validate with toRealPath(), otherwise we validate its parent.
    - Minor updates to tests to accomodate the above changes.

### Risks Mischaracterized by Copilot
- UTF-8 Character boundary failures. Reading a file has a 10k Java character limit, enforced by manual counting and FileReader reads. It is possible for the final character to be the first part of a two-part character, meaning input would end with a malformed character. This one seemed to really throw Copilot. It initially characterized it as a medium-risk threat, possibly because it saw my arbitrarily imposed limit as very important? Even when asking it for the best way to ennforce this limit and not break characters, it seemed to argue with itself, proposing alternatives before rejecting them in turn.

### Planned Future Mitigations
- Some kind of whitelist or filtering for GetFileContent which prevents logging of sensitive files.
- Expansion of current file safety checks to include file locking and/or atomic writes to prevent the possibility of files being swapped between validation and reading/writing.
- Explore some level of containment for the sandbox maven program and files, like running in offline mode or a write blocklist.

## Production Readiness Checklist

| Area | Safe enough for experimentation | Required before real use |
|---|---|---|
| Data | Synthetic or deliberately public files | Data classification, allowlisted directories, redaction, backup, and retention policy |
| File containment | Fixed sandbox and basic traversal tests | Canonical and no-follow path checks, race-resistant open operations, hard-link and mount policy |
| File reading | Regular text files with size limit | Strict UTF-8 decoding, code-point-aware truncation, binary detection, read-only handles |
| File writing | Single-process, disposable files | Atomic temp-file/rename behavior, backup/recovery, overwrite confirmation, no direct symlink following |
| Maven/tests | Innocuous test project, 30-second timeout | Read-only build inputs, offline dependencies, isolated workspace, process-tree cleanup, resource limits |
| Human oversight | Manual invocation and review | Per-write confirmation, command preview, approval for network/build actions, and audit trail |
| Failure handling | Clear exceptions and basic tests | Structured errors, recovery behavior, bounded logs, interruption handling, and retry policy |
| Testing | Unit tests for normal and invalid operations | Symlink, race, malformed UTF-8, binary-file, interruption, timeout, permissions, and process-isolation tests |
| Monitoring | Console output | Correlation IDs, audit events, metrics, alerting, and no-sensitive-data logging |
| Deployment | Local classpath/Maven execution | Reproducible build, artifact signing, least-privilege service account, upgrade and rollback process |
