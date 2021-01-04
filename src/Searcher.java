/*
 * Searcher.java
 */

import java.io.File;

/**
 * A searcher thread. Searches for files with a given pattern in all directories
 * listed in a directory queue.
 */
public class Searcher implements Runnable {

    private String pattern;
    SynchronizedQueue<File> directoryQueue;
    SynchronizedQueue<File> resultsQueue;
    
    /**
     * Constructor. Initializes the searcher thread.
     * @param pattern Pattern to look for
     * @param directoryQueue A queue with directories to search in (as listed
     *                       by the scouter)
     * @param resultsQueue A queue for files found (to be copied by a copier)
     */
    public Searcher(String pattern, SynchronizedQueue<File> directoryQueue,
                    SynchronizedQueue<File> resultsQueue) {
        this.pattern = pattern;
        this.directoryQueue = directoryQueue;
        this.resultsQueue = resultsQueue;
    }

    /**
     * Runs the searcher thread. Thread will fetch a directory to search in
     * from the directory queue, then search all files inside it (but will
     * not recursively search subdirectories!). Files that are found to
     * contain the pattern are enqueued to the results queue. This method
     * begins by registering to the results queue as a producer and when
     * finishes, it unregisters from it.
     */
    @Override
    public void run() {
        File dir;
        this.resultsQueue.registerProducer();
        while((dir = this.directoryQueue.dequeue()) != null) {
            File[] listOfFiles = dir.listFiles();
            try {
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        if (containsPattern(file, this.pattern)) {
                            this.resultsQueue.enqueue(file);
                        }
                    }
                }
            } catch (NullPointerException e) {
                System.err.println("An exception has occurred while trying to " +
                        "search pattern.");
                return;
            }
        }
        this.resultsQueue.unregisterProducer();
    }

    /**
     * This method checks if a filename contains a given pattern
     * @param file The file we want to check if contains the pattern
     * @param pattern the pattern
     * @return true if the filename contains the pattern. Otherwise, false.
     */
    private boolean containsPattern(File file, String pattern) {
        String fileName = file.getName().toLowerCase();
        return fileName.matches("(.*)" + pattern + "(.*)");
    }
}
