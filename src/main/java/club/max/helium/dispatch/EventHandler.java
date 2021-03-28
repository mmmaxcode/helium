package club.max.helium.dispatch;

import com.esotericsoftware.reflectasm.MethodAccess;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EventHandler <T>{
    private static final EventHandler<Event> SYSTEM_EVENT_DISPATCHER = new EventHandler<Event>();

    /**
     * This returns the system event dispatcher
     * @return
     */

    public static EventHandler<Event> getSystemEventDispatcher() { return SYSTEM_EVENT_DISPATCHER; }

    boolean debug = false;

    /**
     * This is whether or not it prints out what is going on
     * @return
     */

    public boolean isDebug() {
        return debug;
    }

    /**
     * This sets whether or not it prints out what is going on
     * @return
     */

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    PrintStream out = System.out;

    boolean caching = true;

    /**
     * This is whether or not it saves unregistered classes information to a cache
     * @return
     */

    public boolean isCaching() {
        return caching;
    }

    /**
     * This sets whether or not it saves unregistered classes information to a cache
     * @return
     */

    public void setCaching(boolean caching) {
        this.caching = caching;
    }

    int threadCount = 5;

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        service = Executors.newFixedThreadPool(threadCount);
    }

    public int getThreadCount() {
        return threadCount;
    }

    private PrintStream stream = System.out;

    public PrintStream getStream() {
        return stream;
    }

    public void setStream(PrintStream stream) {
        this.stream = stream;
    }

    HashMap<Object, ArrayList<RegisteredMethod>> listeners = new HashMap<>();

    public HashMap<Object, ArrayList<RegisteredMethod>> getListeners() {
        return listeners;
    }

    public void setListeners(HashMap<Object, ArrayList<RegisteredMethod>> listeners) {
        this.listeners = listeners;
    }

    private ExecutorService service = Executors.newFixedThreadPool(threadCount);
    public void shutdown() { service.shutdown(); }

    public void open(Object o){

        List<Method> methods = Arrays.stream(o.getClass().getDeclaredMethods())
                .filter(it -> it.isAnnotationPresent(Subscriber.class))
      //          .filter(it -> it.getParameterCount() == 1 && it.getParameterTypes()[0].getSuperclass().isAssignableFrom())
                .collect(Collectors.toList());

        ArrayList<RegisteredMethod> registeredMethods = new ArrayList<>();

        for (Method method : methods) {

            if (debug){
                out.println("{EVENT} Registered item");
            }
            Class<? extends T> eventClass = (Class<? extends T>) method.getParameterTypes()[0];


            registeredMethods.add(new RegisteredMethod(eventClass, MethodAccess.get(o.getClass()), method.getName()));
        }
        listeners.put(o, registeredMethods);
    }

    public void close(Object o){
        listeners.remove(o);
    }

    void dispatchEvent(T event){
        if (debug){
            out.println("{EVENT} Dispatching event");
        }
        for (Map.Entry<Object, ArrayList<RegisteredMethod>> entry : listeners.entrySet()){
            for (RegisteredMethod registeredMethod : entry.getValue()){
                if (registeredMethod.getEvent() == event.getClass()){
                    try {
                        registeredMethod.getMethod().invoke(entry.getKey(), registeredMethod.getMethodName(), event);
                        if (debug){
                            out.println("{EVENT} Dispatched event");
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }
    }

    public void dispatch(T event){
        dispatchEvent(event);
    }

    public void dispatchSync(T event) {
        service.submit(() -> dispatchEvent(event));
    }

    public class RegisteredMethod {
        Class<? extends T> event;
        MethodAccess method;
        String methodName;
        public RegisteredMethod(Class<? extends T> event, MethodAccess method, String methodName){
            this.event = event;
            this.method = method;
            this.methodName = methodName;
        }

        public Class<? extends T> getEvent() {
            return event;
        }

        public MethodAccess getMethod() {
            return method;
        }

        public String getMethodName() {
            return methodName;
        }
    }
}
