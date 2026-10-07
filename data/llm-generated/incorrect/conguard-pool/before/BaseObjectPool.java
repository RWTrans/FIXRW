package org.apache.commons.pool;
public abstract class BaseObjectPool implements ObjectPool {
    public abstract Object borrowObject() throws Exception;
    public abstract void returnObject(Object obj) throws Exception;
    public abstract void invalidateObject(Object obj) throws Exception;
    public int getNumIdle() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    public int getNumActive() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    public void clear() throws Exception, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    public void addObject() throws Exception, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    public void close() throws Exception {
        assertOpen();
        closed = true;
    }
    public void setFactory(PoolableObjectFactory factory) throws IllegalStateException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    protected final boolean isClosed() {
        return closed;
    }
    protected final void assertOpen() throws IllegalStateException {
        if(isClosed()) {
            throw new IllegalStateException("Pool not open");
        }
    }
    private boolean closed = false;
}
