/*
 * Scouter.java
 */

import java.io.File;

/**
 * A scouter thread. This thread lists all sub-directories from a given root
 * path. Each sub-directory is enqueued to be searched for files by Searcher
 * threads.
 */

public class Scouter implements Runnable {

    private SynchronizedQueue<File> directoryQueue;
    private File rootDir;

    /**
     * Construnctor. Initializes the scouter with a directoryQueue for the directories
     * to be searched and a root directory to start from.
     * @param directoryQueue A directoryQueue for directories to be searched
     * @param root Root directory to start from
     */
    public Scouter(SynchronizedQueue<File> directoryQueue, File root) {
        this.directoryQueue = directoryQueue;
        this.rootDir = root;
    }

    /**
     * Starts the scouter thread. Lists directories under root directory and
     * adds them to directoryQueue, then lists directories in the next level and enqueues
     * them and so on. This method begins by registering to the directory directoryQueue
     * as a producer and when finishes, it unregisters from it.
     */
    @Override
    public void run() {
        this.directoryQueue.registerProducer();
        this.directoryQueue.enqueue(this.rootDir);
        if (!this.rootDir.isDirectory()) {
            System.err.println("There is no source directory to search in " +
                    "exiting.");
            return;
        }
        addDirsAndSubdirs(this.rootDir);
        this.directoryQueue.unregisterProducer();
    }

    /**
     * This method lists all directories and subdirectories of a given file
     * @param path the file as a root
     * @return a list of all subdirectories
     */
    private void addDirsAndSubdirs(File path) {
        try {
            File[] files = path.listFiles();
            for (File subfile : files) {
                if (subfile.isDirectory()) {
                    this.directoryQueue.enqueue(subfile);
                    addDirsAndSubdirs(subfile);
                }
            }
        } catch (NullPointerException e){
            System.err.println("Failed to retrieve all directories and " +
                    "subdirectories.");
            return;
        }
    }
}
