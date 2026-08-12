# Repository Guide

## Build and verification

- This is a plain Java 8 IntelliJ project: there is no Maven/Gradle wrapper, test suite, linter, formatter, CI, or codegen task. Do not invent project commands.
- `src/` is the only source root. Compile all sources with a JDK (a JRE alone is insufficient):
  `javac -encoding UTF-8 -cp "lib/*" -d out/classes $(find src -name '*.java')`
  On Windows PowerShell, use:
  `javac -encoding UTF-8 -cp "lib/*" -d out/classes (Get-ChildItem src -Recurse -Filter *.java).FullName`
- The tracked JDBC drivers under `lib/` are required at runtime and intentionally committed despite `.gitignore`'s general `*.jar` rule.
- There are no isolated automated tests. Do not run either main class or the shell launchers as a smoke test: they connect to configured databases, and QTB can `DELETE`, `TRUNCATE`, alter partitions, and insert data.

## Architecture

- The two production entrypoints are default-package classes: `QuantumTransitionBridge` (QTB data migration) and `MemoryAnchorStake` (MAS DDL/data backup plus `MASCreateFile` reconstruction mode). Their manifests are `src/QTB/META-INF/MANIFEST.MF` and `src/MAS/META-INF/MANIFEST.MF`; they are packaged as separate JARs.
- Shared code is under `src/ink/qicq/`: `utils/SQLUtils.java` owns database-dialect SQL, `utils/ResultSetUtils.java` owns JDBC type conversion, `thread/InitQTBThread.java` plans migration work, and `thread/QTBThread.java` consumes the shared queue and writes batches.
- Database names are behavior selectors, not labels. Exact strings such as `GaussDB`, `GBase8AMPP`, `DM8`, and `OushuDB` drive branches in SQL and backup code; when adding a database, audit both `SQLUtils` and all task/result-set dialect branches.
- QTB is two-phase: all table-cleanup/task planning completes before queued data-copy work starts. Preserve that ordering; cleanup may be destructive and `ParamUtils.queue` is static process-wide state.

## Configuration and deployment

- `resources/` contains deployment templates, not Java classpath resources. Deployment rearranges them into sibling `bin/`, `config/`, `lib/`, `table/`, `logs/`, and `backups/` directories as documented in `README.md`; scripts derive every path from that layout.
- `resources/System.conf` and `<DB>.conf` are sourced by Bash and pass positional arguments to Java. If an argument is added or reordered, update the launcher and the corresponding parser in `InitUtils` together.
- Optional session initialization is loaded by string concatenation as `<propertiesUrl><DBName>parameter.conf`; the supplied path must end in `/` or `\\`, and the filename must exactly match the database selector.
- Multi-table launchers read whitespace-delimited rows from `table/<name>` in the format `id schema.table direction deleteType`. Single-table launchers expect `schema.table deleteType`; delete type `3` additionally requires `yyyyMMdd` and evaluation ID.
- The launch scripts require Linux/GNU behavior (`bash`, `readlink -e`, GNU `date`, `find`). `MemoryAnchorStake.sh` removes the matching one-month-old backup directory, and QTB removes logs older than 14 days.

## Change safety

- Example `resources/*.conf` files contain connection fields. Never replace placeholders or examples with real credentials, hosts, or environment-specific secrets.
- Keep repository text at LF as required by `.gitattributes`; only `.bat` and `.cmd` use CRLF.
