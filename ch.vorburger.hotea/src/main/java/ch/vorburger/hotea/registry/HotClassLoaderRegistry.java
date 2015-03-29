/**
 * Copyright (C) 2015 by Michael Vorburger
 */
package ch.vorburger.hotea.registry;

import java.util.List;

import ch.vorburger.hotea.HotClassLoaderBuilder;

/**
 * Registry for typical use case of having to keep track of a number of
 * extensions/modules/plugins/bundles (each added through their own
 * HotClassLoaderBuilder with different class path entries), and being able to
 * find implementations of some API interfaces among all of them.
 * 
 * @author Michael Vorburger
 */
public interface HotClassLoaderRegistry {

	// TODO how to find WHICH classLoader to replace on reload.. implement some sensible equals() - in a new HotClassLoaderProvider?
	// TODO need to remember from which "bundle" (no, no interface for this!) we obtained a given class?
	// TODO this will need a cache, for performance alone! Use Spring??

//	protected List<ClassLoader> classLoaders; // TODO use a copy-on-write impl. to avoid concurrency issues
	
	public void addHotClassLoaderBuilder(HotClassLoaderBuilder builder);
		// TODO add addListener() so that get notified and can swap out the implementations
	
	/**
	 * Get all Class which implement/extend the apiClass.
	 * This is currently internally implemented using (TODO, some Spring? util), but that is an implementation detail.
	 * 
	 * @param apiClass Java interface (or parent class) who's implementations/subclasses should be found
	 * @return instance. For hot reloading to work, you CAN NOT "hang on" (i.e. store in a class member instance field) what this returns, but should ask for it every time
	 * 
	 * @throws HotClassLoaderRegistryException if more than one implementation/subclass was available
	 */
	public <T> List<Class<? extends T>> getImplementationClasses(Class<T> apiClass) throws HotClassLoaderRegistryException;
	
	/**
	 * Get a single implementation.
	 * This is just a convenience method. If you want to initialize your instances yourself given a class (e.g. for DI dependency injection), then don't use this variant, but the one that just returns Class.
	 * 
	 * @param apiClass Java interface (or parent class) who's implementations/subclasses should be found
	 * @return instance. For hot reloading to work, you CAN NOT "hang on" (i.e. store in a class member instance field) what this returns, but should ask for it every time
	 * 
	 * @throws HotClassLoaderRegistryException if more than one implementation/subclass was available
	 */
	public <T> List<T> getImplementations(Class<T> apiClass) throws HotClassLoaderRegistryException;

	/**
	 * Get a single implementation.
	 * This is just a convenience method. If you want to initialize your instances yourself given a class (e.g. for DI dependency injection), then don't use this variant, but the one that just returns Class.
	 * 
	 * @param apiClass Java interface (or parent class) who's implementation/subclass should be found
	 * @return instance. For hot reloading to work, you CAN NOT "hang on" (i.e. store in a class member instance field) what this returns, but should ask for it every time
	 * 
	 * @throws HotClassLoaderRegistryException if more than one implementation/subclass was available
	 */
	public <T> T getImplementation(Class<T> apiClass) throws HotClassLoaderRegistryException;

}
