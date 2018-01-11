package com.undead_pixels.dungeon_bots.script;


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
            while (thread.getState() != Thread.State.TERMINATED)
                if(Thread.currentThread().isInterrupted()) {
                    thread.interrupt();
                    return;
                }
        });
    }
}
