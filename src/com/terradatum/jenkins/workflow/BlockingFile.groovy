package com.terradatum.jenkins.workflow;

import com.cloudbees.groovy.cps.NonCPS;

/**
 * Created by rbellamy on 8/15/16.
 */
public class BlockingFile {
    String path
    
    def processFileWithLock(file, processStream) {
        def random = new RandomAccessFile(file, "rw")
        def lock = random.channel.lock() // acquire exclusive lock
        processStream(random)
        lock.release()
        random.close()
    }
}
