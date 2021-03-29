import club.max.helium.dispatch.Event;
import club.max.helium.dispatch.EventHandler;
import club.max.helium.dispatch.Subscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class EventBusTests {

    private EventHandler<Event> eventBus;
    private int eventCounter;

    @BeforeEach
    public void setup() {
        eventBus = new EventHandler<>();
        eventCounter = 0;
    }

    @Test
    public void testDispatchCount() {
        eventBus.setThreadCount(1);
        LogHandler handler = new LogHandler();
        eventBus.getLogger().addHandler(handler);

        eventBus.open(this);
        for (int i = 0; i < 10; i++) {
            eventBus.dispatch(new TestEvent());
        }

        Assertions.assertEquals(eventCounter, 10, "Event was not dispatched 10 times");
    }

    @Test
    public void testDispatchLog() {
        eventBus.setThreadCount(1);
        LogHandler handler = new LogHandler();
        eventBus.getLogger().addHandler(handler);

        eventBus.open(this);
        eventBus.dispatch(new TestEvent());

        Assertions.assertNotEquals(handler.recordList.size(), 0, "There was no logger output");
    }

    @Test
    public void testDispatchLogSilent() {
        eventBus.setThreadCount(1);
        LogHandler handler = new LogHandler();
        eventBus.getLogger().addHandler(handler);
        eventBus.getLogger().setLevel(Level.OFF);

        eventBus.open(this);
        eventBus.dispatch(new TestEvent());

        Assertions.assertEquals(handler.recordList.size(), 0, "There was logger output");
    }

    @Test
    public void testDispatchDisable() {
        eventBus.setThreadCount(1);
        eventBus.open(this);
        eventBus.dispatch(new TestEvent());
        eventBus.close(this);
        eventBus.dispatch(new TestEvent());
        Assertions.assertEquals(eventCounter, 1, "Listener object was not closed");
    }

    // TODO test multiple threads and async stuff

    @Test
    public void benchmark() {
        int events = 50000000;

        eventBus.setThreadCount(1);
        eventBus.getLogger().setLevel(Level.OFF);
        eventBus.open(this);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < events; i++) {
            eventBus.dispatch(new TestEvent());
        }
        Logger.getLogger("Benchmark").info(String.format("Benchmark took %d", (System.currentTimeMillis() - startTime)));
        Assertions.assertEquals(eventCounter, events, "Events were not dispatched");
    }

    @Subscriber
    public void eventSubscriber(TestEvent event) {
        eventCounter++;
    }

    public static class LogHandler extends Handler {
        public final List<LogRecord> recordList = new ArrayList<>();
        @Override
        public void publish(LogRecord logRecord) {
            recordList.add(logRecord);
        }
        @Override public void flush() {}
        @Override public void close() throws SecurityException {}
    }

}
