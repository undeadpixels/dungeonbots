package com.undead_pixels.dungeon_bots.script;


/**
 * A class that contains a static method for creating asynchronous threads that run without blocking the main thread.
 */
public class ThreadWrapper {
    /**
     * Static Method that creates a Thread that itself contains a thread that is monitoring if it should be interrupted.
     * @param toRun
     * @return
     */
    public static Thread create(Runnable toRun) {
        return new Thread(() -> {
            Thread thread = new Thread(toRun);
            thread.start();
            while (thread.getState() != Thread.State.TERMINATED) {
            		// TODO - this is a busy-wait and needs to be fixed.
                if(Thread.currentThread().isInterrupted()) {
                    thread.interrupt();
                    thread.stop();
                    return;
                }
            }
        });
    }
}
