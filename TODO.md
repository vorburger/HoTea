
- [ ] DirectoryWatcher probably doesn't handle e.g. renamed directories nicely yet - test! Use keys from https://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java ?

- [ ] Minecraft!
  - [ ] Forwarding Mod using Hot Chai
  - [ ] Look into server src and patch
  - [ ] Update https://forums.spongepowered.org/t/plugin-reloader/10666/7 when I have a new Minecraft demo working again

- [ ] Cool Down wait period, avoid too frequent reloads, and discovering a new or changed file before it was fully written: When an event is reported to indicate that a file in a watched directory has been modified then there is no guarantee that the program (or programs) that have modified the file have completed. Care should be taken to coordinate access with other programs that may be updating the file. The FileChannel class defines methods to lock regions of a file against access by other programs.
