package ca.wimsc.client.common.model;

public interface IModelListenerSync<T> {

    /**
     * Invoked when an object changes or is reloaded
     * 
     * @param theObject
     *            The loaded object
     */
    public abstract void objectLoaded(T theObject);

}