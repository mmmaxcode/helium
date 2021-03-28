package club.max.helium.test;

import club.max.helium.dispatch.Subscriber;

public class Test2 {

    @Subscriber
    public void onEvent(TestEvent event){
        Main.dispatched ++;
    }
}
