package club.max.helium.dispatch;

import com.esotericsoftware.reflectasm.MethodAccess;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EventHandler <T> {

    /** System's event dispatcher */
    public static final EventHandler<Event> SYSTEM_EVENT_DISPATCHER = new EventHandler<>();
    /** Caching unregistered classes */
    private boolean caching = true;
    /** Logger */
    private Logger logger = Logger.getLogger("Helium");
    /** Listeners hash map */
    private HashMap<Object, List<RegisteredMethod<T>>> listeners = new HashMap<>();
    /** Executor thread count, default of 5 */
    private int threadCount = 5;
    /** Executor thread pool */
    private ExecutorService service = Executors.newFixedThreadPool(threadCount);

    /**
     * Registers all the listeners in an object
     * @param o listener object
     */
    public void open(Object o) {

        List<Method> methods = Arrays.stream(o.getClass().getDeclaredMethods())
                .filter(it -> it.isAnnotationPresent(Subscriber.class))
                .collect(Collectors.toList());

        List<RegisteredMethod<T>> registeredMethods = new ArrayList<>();

        for (Method method : methods) {
            logger.info("Registered item");

            @SuppressWarnings("unchecked")
            Class<? extends T> eventClass = (Class<? extends T>) method.getParameterTypes()[0];

            registeredMethods.add(new RegisteredMethod<T>(eventClass, MethodAccess.get(o.getClass()), method.getName()));
        }
        listeners.put(o, registeredMethods);
    }

    /**
     * Dispatch an event
     * @param event event
     */
    void dispatchEvent(T event){
        logger.info("Dispatching event");
        for (Map.Entry<Object, List<RegisteredMethod<T>>> entry : listeners.entrySet()) {
            for (RegisteredMethod<T> registeredMethod : entry.getValue()) {
                if (registeredMethod.event == event.getClass()) {
                    try {
                        registeredMethod.method.invoke(entry.getKey(), registeredMethod.name, event);
                        logger.info("Dispatched event");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Dispatch an event
     * @param event event
     */
    public void dispatch(T event){
        dispatchEvent(event);
    }

    /**
     * Dispatches an event synchronously
     * @param event event
     */
    public void dispatchSync(T event) {
        service.submit(() -> dispatchEvent(event));
    }

    /**
     * Remove a listener
     * @param o listener
     */
    public void close(Object o){
        listeners.remove(o);
    }

    /**
     * Shutdown executor thread pool
     */
    public void shutdown() {
        service.shutdown();
    }

    /**
     * @return caching?
     */
    public boolean isCaching() {
        return caching;
    }

    /**
     * Set if caching
     * @param caching caching?
     */
    public void setCaching(boolean caching) {
        this.caching = caching;
    }

    /**
     * Set executor thread count and creates new thread pool
     * @param threadCount thread count
     */
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        service = Executors.newFixedThreadPool(threadCount);
    }

    /**
     * @return thread count
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * @return listeners hashmap
     */
    public HashMap<Object, List<RegisteredMethod<T>>> getListeners() {
        return listeners;
    }

    /**
     * Sets listeners
     * @param listeners listeners hashmap
     */
    public void setListeners(HashMap<Object, List<RegisteredMethod<T>>> listeners) {
        this.listeners = listeners;
    }

    /**
     * Sets logger
     * @param logger logger
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * @return logger
     */
    public Logger getLogger() {
        return logger;
    }

}
