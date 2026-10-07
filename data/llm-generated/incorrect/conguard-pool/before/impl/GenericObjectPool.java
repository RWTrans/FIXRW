package org.apache.commons.pool.impl;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.pool.BaseObjectPool;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
public class GenericObjectPool extends BaseObjectPool implements ObjectPool {
    public static final byte WHEN_EXHAUSTED_FAIL   = 0;
    public static final byte WHEN_EXHAUSTED_BLOCK  = 1;
    public static final byte WHEN_EXHAUSTED_GROW   = 2;
    public static final int DEFAULT_MAX_IDLE  = 8;
    public static final int DEFAULT_MIN_IDLE = 0;
    public static final int DEFAULT_MAX_ACTIVE  = 8;
    public static final byte DEFAULT_WHEN_EXHAUSTED_ACTION = WHEN_EXHAUSTED_BLOCK;
    public static final long DEFAULT_MAX_WAIT = -1L;
    public static final boolean DEFAULT_TEST_ON_BORROW = false;
    public static final boolean DEFAULT_TEST_ON_RETURN = false;
    public static final boolean DEFAULT_TEST_WHILE_IDLE = false;
    public static final long DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = -1L;
    public static final int DEFAULT_NUM_TESTS_PER_EVICTION_RUN = 3;
    public static final long DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS = 1000L * 60L * 30L;
    public GenericObjectPool() {
        this(null,DEFAULT_MAX_ACTIVE,DEFAULT_WHEN_EXHAUSTED_ACTION,DEFAULT_MAX_WAIT,DEFAULT_MAX_IDLE,DEFAULT_MIN_IDLE,DEFAULT_TEST_ON_BORROW,DEFAULT_TEST_ON_RETURN,DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,DEFAULT_NUM_TESTS_PER_EVICTION_RUN,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_TEST_WHILE_IDLE);
    }
    public GenericObjectPool(PoolableObjectFactory factory) {
        this(factory,DEFAULT_MAX_ACTIVE,DEFAULT_WHEN_EXHAUSTED_ACTION,DEFAULT_MAX_WAIT,DEFAULT_MAX_IDLE,DEFAULT_MIN_IDLE,DEFAULT_TEST_ON_BORROW,DEFAULT_TEST_ON_RETURN,DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,DEFAULT_NUM_TESTS_PER_EVICTION_RUN,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_TEST_WHILE_IDLE);
    }
    public GenericObjectPool(PoolableObjectFactory factory, Config config) {
        this(factory,config.maxActive,config.whenExhaustedAction,config.maxWait,config.maxIdle,config.minIdle,config.testOnBorrow,config.testOnReturn,config.timeBetweenEvictionRunsMillis,config.numTestsPerEvictionRun,config.minEvictableIdleTimeMillis,config.testWhileIdle);
    }
    public GenericObjectPool(PoolableObjectFactory factory, int maxActive) {
        this(factory,maxActive,DEFAULT_WHEN_EXHAUSTED_ACTION,DEFAULT_MAX_WAIT,DEFAULT_MAX_IDLE,DEFAULT_MIN_IDLE,DEFAULT_TEST_ON_BORROW,DEFAULT_TEST_ON_RETURN,DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,DEFAULT_NUM_TESTS_PER_EVICTION_RUN,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_TEST_WHILE_IDLE);
    }
    public GenericObjectPool(PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait) {
        this(factory,maxActive,whenExhaustedAction,maxWait,DEFAULT_MAX_IDLE,DEFAULT_MIN_IDLE,DEFAULT_TEST_ON_BORROW,DEFAULT_TEST_ON_RETURN,DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,DEFAULT_NUM_TESTS_PER_EVICTION_RUN,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_TEST_WHILE_IDLE);
    }
    public GenericObjectPool(PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, boolean testOnBorrow, boolean testOnReturn) {
        this(factory,maxActive,whenExhaustedAction,maxWait,DEFAULT_MAX_IDLE,DEFAULT_MIN_IDLE,testOnBorrow,testOnReturn,DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,DEFAULT_NUM_TESTS_PER_EVICTION_RUN,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_TEST_WHILE_IDLE);
    }
    public GenericObjectPool(PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, int maxIdle) {
        this(factory,maxActive,whenExhaustedAction,maxWait,maxIdle,DEFAULT_MIN_IDLE,DEFAULT_TEST_ON_BORROW,DEFAULT_TEST_ON_RETURN,DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,DEFAULT_NUM_TESTS_PER_EVICTION_RUN,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_TEST_WHILE_IDLE);
    }
    public GenericObjectPool(PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, int maxIdle, boolean testOnBorrow, boolean testOnReturn) {
        this(factory,maxActive,whenExhaustedAction,maxWait,maxIdle,DEFAULT_MIN_IDLE,testOnBorrow,testOnReturn,DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,DEFAULT_NUM_TESTS_PER_EVICTION_RUN,DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,DEFAULT_TEST_WHILE_IDLE);
    }
    public GenericObjectPool(PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, int maxIdle, boolean testOnBorrow, boolean testOnReturn, long timeBetweenEvictionRunsMillis, int numTestsPerEvictionRun, long minEvictableIdleTimeMillis, boolean testWhileIdle) {
        this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, DEFAULT_MIN_IDLE, testOnBorrow, testOnReturn, timeBetweenEvictionRunsMillis, numTestsPerEvictionRun, minEvictableIdleTimeMillis, testWhileIdle);
    }
    public GenericObjectPool(PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, int maxIdle, int minIdle, boolean testOnBorrow, boolean testOnReturn, long timeBetweenEvictionRunsMillis, int numTestsPerEvictionRun, long minEvictableIdleTimeMillis, boolean testWhileIdle) {
        _factory = factory;
        _maxActive = maxActive;
        switch(whenExhaustedAction) {
            case WHEN_EXHAUSTED_BLOCK:
            case WHEN_EXHAUSTED_FAIL:
            case WHEN_EXHAUSTED_GROW:
                _whenExhaustedAction = whenExhaustedAction;
                break;
            default:
                throw new IllegalArgumentException("whenExhaustedAction " + whenExhaustedAction + " not recognized.");
        }
        _maxWait = maxWait;
        _maxIdle = maxIdle;
        _minIdle = minIdle;
        _testOnBorrow = testOnBorrow;
        _testOnReturn = testOnReturn;
        _timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        _numTestsPerEvictionRun = numTestsPerEvictionRun;
        _minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        _testWhileIdle = testWhileIdle;
        _pool = new CursorableLinkedList();
        startEvictor(_timeBetweenEvictionRunsMillis);
    }
    public synchronized int getMaxActive() {
        return _maxActive;
    }
    public synchronized void setMaxActive(int maxActive) {
        _maxActive = maxActive;
        notifyAll();
    }
    public synchronized byte getWhenExhaustedAction() {
        return _whenExhaustedAction;
    }
    public synchronized void setWhenExhaustedAction(byte whenExhaustedAction) {
        switch(whenExhaustedAction) {
            case WHEN_EXHAUSTED_BLOCK:
            case WHEN_EXHAUSTED_FAIL:
            case WHEN_EXHAUSTED_GROW:
                _whenExhaustedAction = whenExhaustedAction;
                notifyAll();
                break;
            default:
                throw new IllegalArgumentException("whenExhaustedAction " + whenExhaustedAction + " not recognized.");
        }
    }
    public synchronized long getMaxWait() {
        return _maxWait;
    }
    public synchronized void setMaxWait(long maxWait) {
        _maxWait = maxWait;
        notifyAll();
    }
    public synchronized int getMaxIdle() {
        return _maxIdle;
    }
    public synchronized void setMaxIdle(int maxIdle) {
        _maxIdle = maxIdle;
        notifyAll();
    }
    public synchronized void setMinIdle(int minIdle) {
        _minIdle = minIdle;
        notifyAll();
    }
    public synchronized int getMinIdle() {
        return _minIdle;
    }
    public synchronized boolean getTestOnBorrow() {
        return _testOnBorrow;
    }
    public synchronized void setTestOnBorrow(boolean testOnBorrow) {
        _testOnBorrow = testOnBorrow;
    }
    public synchronized boolean getTestOnReturn() {
        return _testOnReturn;
    }
    public synchronized void setTestOnReturn(boolean testOnReturn) {
        _testOnReturn = testOnReturn;
    }
    public synchronized long getTimeBetweenEvictionRunsMillis() {
        return _timeBetweenEvictionRunsMillis;
    }
    public synchronized void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        _timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        startEvictor(_timeBetweenEvictionRunsMillis);
    }
    public synchronized int getNumTestsPerEvictionRun() {
        return _numTestsPerEvictionRun;
    }
    public synchronized void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        _numTestsPerEvictionRun = numTestsPerEvictionRun;
    }
    public synchronized long getMinEvictableIdleTimeMillis() {
        return _minEvictableIdleTimeMillis;
    }
    public synchronized void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        _minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    public synchronized boolean getTestWhileIdle() {
        return _testWhileIdle;
    }
    public synchronized void setTestWhileIdle(boolean testWhileIdle) {
        _testWhileIdle = testWhileIdle;
    }
    public synchronized void setConfig(Config conf) {
        setMaxIdle(conf.maxIdle);
        setMinIdle(conf.minIdle);
        setMaxActive(conf.maxActive);
        setMaxWait(conf.maxWait);
        setWhenExhaustedAction(conf.whenExhaustedAction);
        setTestOnBorrow(conf.testOnBorrow);
        setTestOnReturn(conf.testOnReturn);
        setTestWhileIdle(conf.testWhileIdle);
        setNumTestsPerEvictionRun(conf.numTestsPerEvictionRun);
        setMinEvictableIdleTimeMillis(conf.minEvictableIdleTimeMillis);
        setTimeBetweenEvictionRunsMillis(conf.timeBetweenEvictionRunsMillis);
        notifyAll();
    }
    public Object borrowObject() throws Exception {
        long starttime = System.currentTimeMillis();
        boolean newlyCreated = false;
        for(;;) {
            ObjectTimestampPair pair = null;
            synchronized(this) {
                assertOpen();
                try {
                    pair = (ObjectTimestampPair)(_pool.removeFirst());
                } catch(NoSuchElementException e) {
                    ; 
                }
                if(null == pair) {
                    if(_maxActive <= 0 || _numActive < _maxActive) {
                    } else {
                        switch(_whenExhaustedAction) {
                            case WHEN_EXHAUSTED_GROW:
                                break;
                            case WHEN_EXHAUSTED_FAIL:
                                throw new NoSuchElementException();
                            case WHEN_EXHAUSTED_BLOCK:
                                    try {
                                        if(_maxWait <= 0) {
                                            wait();
                                        } else {
                                            wait(_maxWait);
                                        }
                                    } catch(InterruptedException e) {
                                    }
                                    if(_maxWait > 0 && ((System.currentTimeMillis() - starttime) >= _maxWait)) {
                                        throw new NoSuchElementException("Timeout waiting for idle object");
                                    } else {
                                        continue; 
                                    }
                            default:
                                throw new IllegalArgumentException("whenExhaustedAction " + _whenExhaustedAction + " not recognized.");
                        }
                    }
                }
                _numActive++;
            } 
            if(null == pair) {
                try {
                    Object obj = _factory.makeObject();
                    pair = new ObjectTimestampPair(obj);
                    newlyCreated = true;
                }
                catch (Exception e) {
                    synchronized(this) {
                        _numActive--;
                        notifyAll();
                    }
                    throw e;
                }
            }
            try {
                _factory.activateObject(pair.value);
                if(_testOnBorrow && !_factory.validateObject(pair.value)) {
                    throw new Exception("validateObject failed");
                }                
                return pair.value;
            } 
            catch (Exception e) {
                synchronized(this) {
                    _numActive--;
                    notifyAll();
                }
                try {
                    _factory.destroyObject(pair.value);
                } 
                catch (Exception e2) {
                }
                if(newlyCreated) {
                    throw new NoSuchElementException("Could not create a validated object, cause: " + e.getMessage());
                } 
                else {
                    continue; 
                }
            }
        }
    }
    public void invalidateObject(Object obj) throws Exception {
        assertOpen();
        try {
            _factory.destroyObject(obj);
        }
        finally {
            synchronized(this) {
                _numActive--;
                notifyAll(); 
            }
        }
    }
    public synchronized void clear() {
        assertOpen();
        for(Iterator it = _pool.iterator(); it.hasNext(); ) {
            try {
                _factory.destroyObject(((ObjectTimestampPair)(it.next())).value);
            } catch(Exception e) {
            }
            it.remove();
        }
        _pool.clear();
        notifyAll(); 
    }
    public synchronized int getNumActive() {
        assertOpen();
        return _numActive;
    }
    public synchronized int getNumIdle() {
        assertOpen();
        return _pool.size();
    }
    public void returnObject(Object obj) throws Exception {
        assertOpen();
        addObjectToPool(obj, true);
    }
    private void addObjectToPool(Object obj, boolean decrementNumActive) throws Exception {
        boolean success = true;
        if(_testOnReturn && !(_factory.validateObject(obj))) {
            success = false;
        } else {
            try {
                _factory.passivateObject(obj);
            } catch(Exception e) {
                success = false;
            }
        }
        boolean shouldDestroy = !success;
        synchronized(this) {
            if (decrementNumActive) {
                _numActive--;
            }
            if((_maxIdle >= 0) && (_pool.size() >= _maxIdle)) {
                shouldDestroy = true;
            } else if(success) {
                _pool.addFirst(new ObjectTimestampPair(obj));
            }
            notifyAll(); 
        }
        if(shouldDestroy) {
            try {
                _factory.destroyObject(obj);
            } catch(Exception e) {
            }
        }
    }
    public synchronized void close() throws Exception {
        clear();
        _pool = null;
        _factory = null;
        if(null != _evictionCursor) {
            _evictionCursor.close();
            _evictionCursor = null;
        }
        startEvictor(-1L);
        super.close();
    }
    public synchronized void setFactory(PoolableObjectFactory factory) throws IllegalStateException {
        assertOpen();
        if(0 < getNumActive()) {
            throw new IllegalStateException("Objects are already active");
        } else {
            clear();
            _factory = factory;
        }
    }
    public synchronized void evict() throws Exception {
        assertOpen();
        if(!_pool.isEmpty()) {
            if(null == _evictionCursor) {
                _evictionCursor = (_pool.cursor(_pool.size()));
            } else if(!_evictionCursor.hasPrevious()) {
                _evictionCursor.close();
                _evictionCursor = (_pool.cursor(_pool.size()));
            }
            for(int i=0,m=getNumTests();i<m;i++) {
                if(!_evictionCursor.hasPrevious()) {
                    _evictionCursor.close();
                    _evictionCursor = (_pool.cursor(_pool.size()));
                } else {
                    boolean removeObject = false;
                    ObjectTimestampPair pair = (ObjectTimestampPair)(_evictionCursor.previous());
                    if(_minEvictableIdleTimeMillis > 0 &&
                       System.currentTimeMillis() - pair.tstamp > _minEvictableIdleTimeMillis) {
                       removeObject = true;
                    } else if(_testWhileIdle) {
                        boolean active = false;
                        try {
                            _factory.activateObject(pair.value);
                            active = true;
                        } catch(Exception e) {
                            removeObject=true;
                        }
                        if(active) {
                            if(!_factory.validateObject(pair.value)) {
                                removeObject=true;
                            } else {
                                try {
                                    _factory.passivateObject(pair.value);
                                } catch(Exception e) {
                                    removeObject=true;
                                }
                            }
                        }
                    }
                    if(removeObject) {
                        try {
                            _evictionCursor.remove();
                            _factory.destroyObject(pair.value);
                        } catch(Exception e) {
                            ; 
                        }
                    }
                }
            }
        } 
    }
    protected void ensureMinIdle() throws Exception {
        int objectDeficit = calculateDeficit();
        for ( int j = 0 ; j < objectDeficit && calculateDeficit() > 0 ; j++ ) {
            addObject();
        }
    }
    private synchronized int calculateDeficit() {
        int objectDeficit = getMinIdle() - getNumIdle();
        if (_maxActive > 0) {
            int growLimit = Math.max(0, getMaxActive() - getNumActive() - getNumIdle());
            objectDeficit = Math.min(objectDeficit, growLimit);
        }
        return objectDeficit;
    } 
    public void addObject() throws Exception {
        Object obj = _factory.makeObject();
        addObjectToPool(obj, false);
    }
    protected synchronized void startEvictor(long delay) {
        if(null != _evictor) {
            _evictor.cancel();
            _evictor = null;
        }
        if(delay > 0) {
            _evictor = new Evictor(delay);
            Thread t = new Thread(_evictor);
            t.setDaemon(true);
            t.start();
        }
    }
    synchronized String debugInfo() {
        StringBuffer buf = new StringBuffer();
        buf.append("Active: ").append(getNumActive()).append("\n");
        buf.append("Idle: ").append(getNumIdle()).append("\n");
        buf.append("Idle Objects:\n");
        Iterator it = _pool.iterator();
        long time = System.currentTimeMillis();
        while(it.hasNext()) {
            ObjectTimestampPair pair = (ObjectTimestampPair)(it.next());
            buf.append("\t").append(pair.value).append("\t").append(time - pair.tstamp).append("\n");
        }
        return buf.toString();
    }
    private int getNumTests() {
        if(_numTestsPerEvictionRun >= 0) {
            return _numTestsPerEvictionRun;
        } else {
            return(int)(Math.ceil((double)_pool.size()/Math.abs((double)_numTestsPerEvictionRun)));
        }
    }
    class ObjectTimestampPair {
        Object value;
        long tstamp;
        ObjectTimestampPair(Object val) {
            this(val,System.currentTimeMillis());
        }
        ObjectTimestampPair(Object val, long time) {
            value = val;
            tstamp = time;
        }
    }
    class Evictor implements Runnable {
        private boolean _cancelled = false;
        private long _delay = 0L;
        public Evictor(long delay) {
            _delay = delay;
        }
        void cancel() {
            _cancelled = true;
        }
        public void run() {
            while(!_cancelled) {
                try {
                    Thread.sleep(_delay);
                } catch(Exception e) {
                }
                try {
                    evict();
                } catch(Exception e) {
                }
                try {
                    ensureMinIdle();
                } catch(Exception e) {
                }
            }
            synchronized(GenericObjectPool.this) {
                if(null != _evictionCursor) {
                    _evictionCursor.close();
                    _evictionCursor = null;
                }
            }
        }
    }
    public static class Config {
        public int maxIdle = GenericObjectPool.DEFAULT_MAX_IDLE;
        public int minIdle = GenericObjectPool.DEFAULT_MIN_IDLE;
        public int maxActive = GenericObjectPool.DEFAULT_MAX_ACTIVE;
        public long maxWait = GenericObjectPool.DEFAULT_MAX_WAIT;
        public byte whenExhaustedAction = GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION;
        public boolean testOnBorrow = GenericObjectPool.DEFAULT_TEST_ON_BORROW;
        public boolean testOnReturn = GenericObjectPool.DEFAULT_TEST_ON_RETURN;
        public boolean testWhileIdle = GenericObjectPool.DEFAULT_TEST_WHILE_IDLE;
        public long timeBetweenEvictionRunsMillis = GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
        public int numTestsPerEvictionRun =  GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;
        public long minEvictableIdleTimeMillis = GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    }
    private int _maxIdle = DEFAULT_MAX_IDLE;
   private int _minIdle = DEFAULT_MIN_IDLE;
    private int _maxActive = DEFAULT_MAX_ACTIVE;
    private long _maxWait = DEFAULT_MAX_WAIT;
    private byte _whenExhaustedAction = DEFAULT_WHEN_EXHAUSTED_ACTION;
    private boolean _testOnBorrow = DEFAULT_TEST_ON_BORROW;
    private boolean _testOnReturn = DEFAULT_TEST_ON_RETURN;
    private boolean _testWhileIdle = DEFAULT_TEST_WHILE_IDLE;
    private long _timeBetweenEvictionRunsMillis = DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
    private int _numTestsPerEvictionRun =  DEFAULT_NUM_TESTS_PER_EVICTION_RUN;
    private long _minEvictableIdleTimeMillis = DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    private CursorableLinkedList _pool = null;
    private PoolableObjectFactory _factory = null;
    private int _numActive = 0;
    private Evictor _evictor = null;
    private CursorableLinkedList.Cursor _evictionCursor = null;
}
