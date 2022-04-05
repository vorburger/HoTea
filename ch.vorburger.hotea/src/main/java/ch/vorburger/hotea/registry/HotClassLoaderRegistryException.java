package ch.vorburger.hotea.registry;

@SuppressWarnings("serial")
public class HotClassLoaderRegistryException extends Exception {

    public HotClassLoaderRegistryException(String message) {
        super(message);
    }

    public HotClassLoaderRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
