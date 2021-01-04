/*
 * Copier.java
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A copier thread. Reads files to copy from a queue and copies them to the
 * given destination.
 */
public class Copier implements Runnable {
    public static final int COPY_BUFFER_SIZE = 4096;
    private SynchronizedQueue<File> resultsQueue;
    private File destDir;

    /**
     * Constructor. Initializes the worker with a destination directory and a
     * queue of files to copy.
     * @param destination Destination directory
     * @param resultsQueue Queue of files found, to be copied
     */
    public Copier(File destination, SynchronizedQueue<File> resultsQueue){
        this.destDir = destination;
        if (!this.destDir.isDirectory()) {
            System.err.println("There is no destination directory to copy to." +
                    " exiting.");
            return;
        }
        this.resultsQueue = resultsQueue;
    }

    /**
     * Runs the copier thread. Thread will fetch files from queue and copy
     * them, one after each other, to the destination directory. When the
     * queue has no more files, the thread finishes.
     */
    @Override
    public void run() {
        byte[] buffer = new byte[COPY_BUFFER_SIZE];
        File fileToCopy;
        int src;
        FileInputStream input;
        FileOutputStream output;
        while((fileToCopy = this.resultsQueue.dequeue()) != null) {
            try {
                input = new FileInputStream(fileToCopy);
                File copiedFile = new File(this.destDir, fileToCopy.getName());
                output = new FileOutputStream(copiedFile);
                while ((src = input.read()) > 0) {
                    output.write(buffer, 0, src);
                }
                input.close();
                output.close();
            } catch (IOException e) {
                System.err.println("An exception has occurred while copying " +
                        "files.");
                return;
            }
        }
    }
}
