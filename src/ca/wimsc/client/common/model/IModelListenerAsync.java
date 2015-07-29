package ca.wimsc.client.common.model;

public interface IModelListenerAsync<T> {

    /**
     * Invoked only if a remote call is required in order to retrieve the object
     */
    public abstract void startLoadingObject();

    /**
     * Invoked once an object is loaded
     * 
     * @param theObject
     *            The loaded object
     * @param theRequiredAsyncLoad
     *            True if the object required a call to the server (and {@link #startLoadingObject()} was invoked)
     */
    public abstract void objectLoaded(T theObject, boolean theRequiredAsyncLoad);

}