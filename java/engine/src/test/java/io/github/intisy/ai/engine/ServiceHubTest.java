package io.github.intisy.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ServiceHubTest {

    private static ServiceHub hub(ManualScheduler scheduler) {
        ServiceHub hub = new ServiceHub(scheduler, null);
        hub.wellKnown(Arrays.asList("accounts"));
        return hub;
    }

    @Test
    void registersAndReadsAService() {
        ServiceHub hub = hub(new ManualScheduler());
        hub.forPlugin("store").register("store:thing", "the service");
        assertEquals("the service", hub.get("store:thing"));
        assertEquals(Arrays.asList("store:thing"), hub.ids());
    }

    @Test
    void refusesAnIdBelongingToAnotherPlugin() {
        final ServiceHub hub = hub(new ManualScheduler());
        PluginException failure = assertThrows(PluginException.class, new Executable() {
            @Override
            public void execute() {
                hub.forPlugin("squatter").register("store:thing", "nope");
            }
        });
        assertTrue(failure.getDetail().contains("belongs to another plugin"));
    }

    @Test
    void allowsAWellKnownBareId() {
        ServiceHub hub = hub(new ManualScheduler());
        hub.forPlugin("anyone").register("accounts", "the store");
        assertEquals("the store", hub.get("accounts"));
    }

    @Test
    void refusesABareIdWhenTheVocabularyDoesNotKnowIt() {
        final ServiceHub hub = new ServiceHub(new ManualScheduler(), null);
        assertThrows(PluginException.class, new Executable() {
            @Override
            public void execute() {
                hub.forPlugin("anyone").register("accounts", "the store");
            }
        });
    }

    @Test
    void refusesASecondRegistrationOfTheSameId() {
        final ServiceHub hub = hub(new ManualScheduler());
        hub.forPlugin("first").register("first:thing", "one");
        PluginException failure = assertThrows(PluginException.class, new Executable() {
            @Override
            public void execute() {
                hub.forPlugin("first").register("first:thing", "two");
            }
        });
        assertTrue(failure.getDetail().contains("already registered"));
    }

    @Test
    void wantResolvesImmediatelyWhenTheServiceIsAlreadyThere() throws Exception {
        ServiceHub hub = hub(new ManualScheduler());
        hub.forPlugin("store").register("store:thing", "the service");
        assertEquals("the service", hub.forPlugin("reader").want("store:thing").toCompletableFuture().get());
    }

    @Test
    void wantResolvesWhenTheServiceArrivesLater() throws Exception {
        ServiceHub hub = hub(new ManualScheduler());
        CompletableFuture<Object> pending = hub.forPlugin("reader").want("store:thing").toCompletableFuture();
        assertFalse(pending.isDone());
        hub.forPlugin("store").register("store:thing", "the service");
        assertEquals("the service", pending.get());
    }

    @Test
    void wantRejectsOnTheTimeout() {
        ManualScheduler scheduler = new ManualScheduler();
        ServiceHub hub = hub(scheduler);
        final CompletableFuture<Object> pending = hub.forPlugin("reader").want("store:thing", 50L).toCompletableFuture();
        scheduler.fireAll();
        ExecutionException failure = assertThrows(ExecutionException.class, new Executable() {
            @Override
            public void execute() throws Exception {
                pending.get();
            }
        });
        assertTrue(failure.getCause() instanceof PluginException);
        assertTrue(((PluginException) failure.getCause()).getDetail().contains("50"));
    }

    @Test
    void aRegistrationCancelsAPendingTimeout() throws Exception {
        ManualScheduler scheduler = new ManualScheduler();
        ServiceHub hub = hub(scheduler);
        CompletableFuture<Object> pending = hub.forPlugin("reader").want("store:thing", 50L).toCompletableFuture();
        hub.forPlugin("store").register("store:thing", "the service");
        scheduler.fireAll();
        assertEquals("the service", pending.get());
    }

    @Test
    void releasingAPluginUnregistersWhatItProvided() {
        ServiceHub hub = hub(new ManualScheduler());
        hub.forPlugin("store").register("store:thing", "the service");
        hub.releasePlugin("store");
        assertNull(hub.get("store:thing"));
        assertTrue(hub.ids().isEmpty());
    }

    @Test
    void releasingAPluginRejectsTheWantsItWasWaitingOn() {
        ServiceHub hub = hub(new ManualScheduler());
        final CompletableFuture<Object> pending = hub.forPlugin("reader").want("store:thing").toCompletableFuture();
        hub.releasePlugin("reader");
        ExecutionException failure = assertThrows(ExecutionException.class, new Executable() {
            @Override
            public void execute() throws Exception {
                pending.get();
            }
        });
        assertTrue(((PluginException) failure.getCause()).getDetail().contains("stopped while waiting"));
    }

    @Test
    void notifiesWatchersOfArrivalAndDeparture() {
        ServiceHub hub = hub(new ManualScheduler());
        final List<String> seen = new ArrayList<String>();
        hub.forPlugin("reader").watch("store:thing", new ServiceHub.Listener() {
            @Override
            public void changed(Object service, boolean registered) {
                seen.add(registered ? "register:" + service : "unregister");
            }
        });
        Scheduler.Cancellable registration = hub.forPlugin("store").register("store:thing", "the service");
        registration.cancel();
        assertEquals(Arrays.asList("register:the service", "unregister"), seen);
    }

    @Test
    void aWatcherThatThrowsIsReportedAndTheRestStillRun() {
        ServiceHub hub = hub(new ManualScheduler());
        final List<String> reported = new ArrayList<String>();
        final List<String> seen = new ArrayList<String>();
        Diagnostics.setSink(new Diagnostics.Sink() {
            @Override
            public void accept(String message) {
                reported.add(message);
            }
        });
        try {
            hub.forPlugin("angry").watch("store:thing", new ServiceHub.Listener() {
                @Override
                public void changed(Object service, boolean registered) {
                    throw new IllegalStateException("no");
                }
            });
            hub.forPlugin("calm").watch("store:thing", new ServiceHub.Listener() {
                @Override
                public void changed(Object service, boolean registered) {
                    seen.add("ok");
                }
            });
            hub.forPlugin("store").register("store:thing", "the service");
        } finally {
            Diagnostics.setSink(null);
        }
        assertEquals(1, seen.size());
        assertEquals(1, reported.size());
        assertTrue(reported.get(0).contains("threw"));
    }

    @Test
    void releasingAPluginStopsTheWatchersItInstalled() {
        ServiceHub hub = hub(new ManualScheduler());
        final List<String> seen = new ArrayList<String>();
        hub.forPlugin("reader").watch("store:thing", new ServiceHub.Listener() {
            @Override
            public void changed(Object service, boolean registered) {
                seen.add("seen");
            }
        });
        hub.releasePlugin("reader");
        hub.forPlugin("store").register("store:thing", "the service");
        assertTrue(seen.isEmpty());
    }

    @Test
    void recordsProvisionsAndConsumptionsForTheLedger() {
        final List<String> provided = new ArrayList<String>();
        final List<String> consumed = new ArrayList<String>();
        ServiceHub hub = new ServiceHub(new ManualScheduler(), new ServiceHub.Recorder() {
            @Override
            public void provided(String pluginId, String serviceId) {
                provided.add(pluginId + "/" + serviceId);
            }

            @Override
            public void consumed(String pluginId, String serviceId) {
                consumed.add(pluginId + "/" + serviceId);
            }
        });
        hub.forPlugin("store").register("store:thing", "the service");
        hub.forPlugin("reader").get("store:thing");
        assertEquals(Arrays.asList("store/store:thing"), provided);
        assertEquals(Arrays.asList("reader/store:thing"), consumed);
    }
}
