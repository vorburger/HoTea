Hot Chai (tea) with Java coffee (ch.vorburger.hotea)
====

__*Please also check out the follow-up projects [ch.vorburger.osgi.gradle](https://github.com/vorburger/ch.vorburger.osgi.gradle) and [ch.vorburger.minecraft.osgi](https://github.com/vorburger/ch.vorburger.minecraft.osgi).*__

__*The [hotea/watchdir](ch.vorburger.hotea/src/main/java/ch/vorburger/hotea/watchdir) utility is now a separate project, see https://github.com/vorburger/ch.vorburger.fswatch.*__

The Minecraft specific part was in [minecraft/ module](ch.vorburger.hotea.minecraft). What follows here is a general library that is not Minecraft related. (But the Minecraft module used this in 2017. Since changes made when picking up this project in 2022, the original Minecraft demo is broken; however work has now started to add the equivalent funcationality into https://github.com/OASIS-learn-study/minecraft-storeys-maker, see [its issue #58](https://github.com/OASIS-learn-study/minecraft-storeys-maker/issues/58).

Hotea is a "kept it simple and stupid" (KISS) Java Mod-ules/Plug-In mini framework with HOT Class reloading to "support dynamic script like source code" (through isolated ClassLoader/s).  This may be of interest and useful to you to build things ranging from e.g. plugins for game engines to perhaps some sort of runtime changeable coded out "business rule" stuff.

Watch this https://www.youtube.com/watch?v=oMhY075hx9k and this https://www.youtube.com/watch?v=mibW8MhenGc vidoe, or see e.g. the [HotSwingExampleMain](ch.vorburger.hotea/src/test/java/ch/vorburger/hotea/examples/swing/HotSwingExampleMain.java) or [the Tests](ch.vorburger.hotea/src/test/java/ch/vorburger/hotea/tests/), for illustrations regarding what this is really all about.

Caveat emptor: This is intended for and best works with simple plugin-like scenarios, where the classpath of each such plugin does not overlap nor need to share instances among different plugins. In more interesting use cases, by experience, Very Weired Things (VWT) may happen if you don't fully understand what you are actually doing when several class loaders are involved in Java. You have been warned.

Use of Java 8 is recommended to avoid PermGen memory issues due to Class reloading.

Features: (what it does do)
* Load Java byte code for additional *.classes from directory/-ies and/or JARs, through a child ClassLoader
* Hot reload such extensions/modules/plugins/bundles whenever any files in the watched classpath directory are updated
* Notify your code whenever it hot reloaded, so that you can do what you need to do in your code (say cleanly shut down previous instances of your classes, and re-instantiate objects using your new class definitions)
* _TODO Build *.class files from *.java sources files, and auto. rebuild on any changes (ch.vorburger.hotea.compilewatchedfiles)_
* No need to follow a particular "Plugin API" at all (of Hotea, there is none) - you can hot (re)load any class implementing any of your own "API" interface

Non-Features, what it does *NOT* do, not today and not planned:
* Hot-reload classes on your (already loaded) "main" primary classpath - no JVM Agent
* Dependencies between modules/plugins/bundles (perhaps you want to look at OSGi?)
* Versioning of modules/plugins/bundles (perhaps you want to look at OSGi?)
* Repositories, remote provisioning, etc.
* Remote API export etc.

Hotea is thus somewhat similar yet quite different from:

Class A: Approaches which hot-reload classes on your (already loaded) "main" primary classpath typically through a custom JVM Agent (with more or less limitations):

* http://hotswapagent.org
* http://dcevm.github.io
* https://github.com/spring-projects/spring-loaded
* http://zeroturnaround.com/software/jrebel/
* https://code.google.com/p/jreloader/
* Standard JVM Hot Deploy in Debug mode

Class B: Other extensions/modules/plugins/bundles
* OSGi (eclipse.org/rt: eclipse.org/equinox, eclipse.org/virgo, eclipse.org/gemini; felix.apache.org & karaf.apache.org; bndtools.org BND Tools; Spring Dynamic Modulees DM)
* [JBoss Modules](https://github.com/jboss-modules/jboss-modules) (and [Doc](https://docs.jboss.org/author/display/MODULES/Home))
* [NucleusPowered/QuickStartModuleLoader](https://github.com/NucleusPowered/QuickStartModuleLoader)
* http://jpf.sourceforge.net
* https://code.google.com/p/jspf/ : No hot unload
* https://code.google.com/p/impala/

Class C: Approaches between A/B
* https://github.com/jmarranz/relproxy/ : Seems to be intended to (re)load one class, only, e.g. for a single Web View

Class D: Other related stuff
* http://commons.apache.org/proper/commons-jci
* http://docs.codehaus.org/display/JANINO/Home

Class E: Related articles
* http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html
* http://www.javaworld.com/article/2071777/design-patterns/add-dynamic-java-code-to-your-application.html
* https://blogs.oracle.com/sundararajan/entry/dynamic_source_code_in_java
* http://stackoverflow.com/questions/26291254/dynamically-recompile-and-reload-a-class
