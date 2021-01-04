/*
 * DiskSearcher.java
 */

import java.io.File;
import java.io.IOError;

/**
 * Main application class. This application searches for all files under some
 * given path that contain a given textual pattern. All files found are copied
 * to some specific directory.
 */
public class DiskSearcher {
    public static final int DIRECTORY_QUEUE_CAPACITY = 50;
    public static final int RESULTS_QUEUE_CAPACITY = 50;

    /**
     * Main method. Reads arguments from command line and starts the search.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Check if there are enough arguments
        if (args.length < 5) {
            System.err.println("Not enough arguments. exiting.");
            return;
        }
        String pattern = args[0];

        String rootDirName = args[1];
        String destDirName = args[2];
        int numOfSearchers = Integer.parseInt(args[3]);
        int numOfCopiers = Integer.parseInt(args[4]);

        File rootDir;
        File destDir;

        try {
            rootDir = new File(rootDirName);
            destDir = new File(destDirName);
            if (!rootDir.exists()) {
                System.err.println("Root directory not found. exiting.");
                return;
            }
            if (!destDir.exists()) {
                destDir.mkdir();
            }
        } catch (IOError e) {
            System.err.println("Not a valid directories " + args[1] + " " + args[2] + ".");
            return;
        }

        // Check if there are valid number of searchers and copiers
        if (numOfCopiers < 1 || numOfSearchers < 1) {
            System.err.println("Please enter valid number of searchers and " +
                    "copiers. exiting.");
            return;
        }

        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>
                (DIRECTORY_QUEUE_CAPACITY);
        SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<>
                (RESULTS_QUEUE_CAPACITY);

        Thread scouter = new Thread(new Scouter(directoryQueue, rootDir));
        scouter.start();


        Searcher[] searchers = new Searcher[numOfSearchers];
        Thread[] searcherThreads = new Thread[numOfSearchers];
        for (int i = 0; i < numOfSearchers; i++) {
            searchers[i] = new Searcher(pattern, directoryQueue, resultsQueue);
            searcherThreads[i] = new Thread(searchers[i]);
            searcherThreads[i].start();
        }

        Copier[] copiers = new Copier[numOfCopiers];
        Thread[] copierThreads = new Thread[numOfCopiers];
        for (int i = 0; i < numOfCopiers; i++) {
            copiers[i] = new Copier(destDir, resultsQueue);
            copierThreads[i] = new Thread(copiers[i]);
            copierThreads[i].start();
        }
        try {
            scouter.join();
        } catch (InterruptedException e) {
            System.err.println("Failed to join() the threads.");
            return;
        }

        try {
            for (int i = 0; i < searchers.length; i++) {
                searcherThreads[i].join();
            } for (int j = 0; j < copiers.length; j++) {
                copierThreads[j].join();
            }
        } catch (InterruptedException e) {
            System.err.println("Failed to join() the threads.");
            return;
        }
    }

}
