package club.max.helium.dispatch;

import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * Holder class for registered listener methods
 */
public class RegisteredMethod<T> {
    /** Event class */
    public final Class<? extends T> event;
    /** ASM reflection method */
    public final MethodAccess method;
    /** Method name */
    public final String name;

    public RegisteredMethod(Class<? extends T> event, MethodAccess method, String name){
        this.event = event;
        this.method = method;
        this.name = name;
    }
}