When processing huge logfiles (most of real-world logs **are** huge) people usually have to resort to unix text utilities like grep, awk etc. They are great but they do not offer any interactivity and require hackery to do non-trivial things, let alone do any visualizations.

This project aims to fill this gap.
The main goals are:
  * Capability to process log files of unlimited size that leave no hope of reading them into memory
  * Extensibility in terms of log format: the details of particular formats should be responsibilities of plugins
  * Extensibility in terms of analyses and visualizations: the program core should be a compact and efficient API for access to log files plus a plugin infrastructure, and everything else should be pluggable
  * Extensibility in terms of input sources: it should be possible to process logs from simple files, from concatenations of gzipped files, from sockets and from anything else