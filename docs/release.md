# Release

```bash
mvn versions:set -DnewVersion=[release_version] -DgenerateBackupPoms=false
mvn versions:set -DnewVersion=0.5.1 -DgenerateBackupPoms=false
git commit -a -m "Set release version"
git tag v0.5.1
git push origin v0.5.1
mvn clean deploy -P release
mvn versions:set -DnewVersion=v0.5.2-SNAPSHOT -DgenerateBackupPoms=false
git commit -a -m "Set snapshot version"
git push
```
