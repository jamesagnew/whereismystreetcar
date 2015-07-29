package ca.wimsc.client.common.model;

public class AbstractModelListener<T> implements IModelListenerAsync<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void startLoadingObject() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void objectLoaded(T theObject, boolean theRequiredAsyncLoad) {

    }

}
