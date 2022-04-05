/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.watchdir;

import ch.vorburger.hotea.watchdir.DirectoryWatcher.ChangeKind;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.ExceptionHandler;
import ch.vorburger.hotea.watchdir.DirectoryWatcher.Listener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * main() for DirectoryWatcher.
 *
 * @author Michael Vorburger
 */
public class DirectoryWatcherMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            System.err.println("USAGE: <root-directory-to-watch-for-changes>");
            return;
        }

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");

        File dir = new File(args[0]);
        DirectoryWatcherImpl dw = (DirectoryWatcherImpl) new DirectoryWatcherBuilder().path(dir)
                // Using explicit anonymous inner classes instead of Lambdas for clarity to readers
                .listener(new Listener() {
                    @Override public void onChange(Path path, ChangeKind changeKind) throws Throwable {
                        System.out.println(changeKind.toString() + " " + path.toString());
                    }

                }).exceptionHandler(new ExceptionHandler() {
                    @Override public void onException(Throwable t) {
                        t.printStackTrace();
                    }

                }).build();

        // This is just because it's a main(), you normally would NOT do this:
        dw.thread.join();

        // You must close() a DirectoryWatcher when you don't need it anymore
        // (In this main() scenario this will unlikely ever actually get reached; this is just an illustration.)
        dw.close();
    }

}
