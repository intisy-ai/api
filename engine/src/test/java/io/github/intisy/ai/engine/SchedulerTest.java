package io.github.intisy.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SchedulerTest {

    @Test
    void theNeverSchedulerAcceptsAndDiscardsWork() {
        final List<String> ran = new ArrayList<String>();
        Scheduler.Cancellable handle = Scheduler.NEVER.schedule(new Runnable() {
            @Override
            public void run() {
                ran.add("ran");
            }
        }, 10L);
        handle.cancel();
        assertEquals(0, ran.size());
    }

    @Test
    void aManualSchedulerFiresWhenTold() {
        ManualScheduler scheduler = new ManualScheduler();
        final List<String> ran = new ArrayList<String>();
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                ran.add("ran");
            }
        }, 10L);
        assertEquals(0, ran.size());
        scheduler.fireAll();
        assertEquals(1, ran.size());
    }

    @Test
    void aCancelledTaskDoesNotFire() {
        ManualScheduler scheduler = new ManualScheduler();
        final List<String> ran = new ArrayList<String>();
        Scheduler.Cancellable handle = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                ran.add("ran");
            }
        }, 10L);
        handle.cancel();
        scheduler.fireAll();
        assertTrue(ran.isEmpty());
    }
}
