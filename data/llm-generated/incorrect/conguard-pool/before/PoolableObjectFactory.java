package org.apache.commons.pool;
public interface PoolableObjectFactory {
  Object makeObject() throws Exception;
  void destroyObject(Object obj) throws Exception;
  boolean validateObject(Object obj);
  void activateObject(Object obj) throws Exception;
  void passivateObject(Object obj) throws Exception;
}
