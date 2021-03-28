package club.max.helium.test;

import club.max.helium.dispatch.Event;
import club.max.helium.dispatch.EventHandler;
import club.max.helium.dispatch.Subscriber;

public class Main {

    static int dispatched = 0;

    public static void main(String[] args) {

        int dispatcherAmount = 100000;

        int registeredItems = 100;

        EventHandler<Event> EVENT_BUS = EventHandler.getSystemEventDispatcher();
        EVENT_BUS.setDebug(false);
        EVENT_BUS.setThreadCount(1);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < registeredItems; i++){
            EVENT_BUS.open(new Main());
         //   EVENT_BUS.register(new Test2());
        }

        long startTimeMain = System.currentTimeMillis();


        for (int i = 0; i < dispatcherAmount; i++){
            EVENT_BUS.dispatch(new TestEvent());
        }

        long endTime = System.currentTimeMillis();

        System.out.println("{FINISHED} Dispatched " + dispatcherAmount * registeredItems + " events in " + (endTime - startTime) + " ms including registering");
        System.out.println("{FINISHED} Dispatched " + dispatcherAmount * registeredItems + " events in " + (endTime - startTimeMain) + " ms without registering");

        System.out.println("Dispatched: " + dispatched);


        EVENT_BUS.shutdown();
    }


    @Subscriber
    public void onDispatch(TestEvent event){
        dispatched ++;
    }


}
