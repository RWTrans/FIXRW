package org.apache.commons.pool;
public interface ObjectPool {
    Object borrowObject() throws Exception;
    void returnObject(Object obj) throws Exception;
    void invalidateObject(Object obj) throws Exception;
    void addObject() throws Exception;
    int getNumIdle() throws UnsupportedOperationException;
    int getNumActive() throws UnsupportedOperationException;
    void clear() throws Exception, UnsupportedOperationException;
    void close() throws Exception;
    void setFactory(PoolableObjectFactory factory) throws IllegalStateException, UnsupportedOperationException;
}
