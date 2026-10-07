package org.apache.commons.pool.impl;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
public class TestGenericObjectPool {
    public static void main(String[] args) throws Exception {
        TestGenericObjectPool test = new TestGenericObjectPool();
        test.testEvictAddObjectsImproved();        
    }
    public void testEvictAddObjectsImproved() throws Exception {
        SimpleFactory factory = new SimpleFactory();
        factory.setMakeLatency(300);
        factory.setMaxActive(2);
        final GenericObjectPool pool = new GenericObjectPool(factory);
        pool.setMaxActive(2);
        pool.setMinIdle(1);
        pool.borrowObject(); 
        TestThread borrower = new TestThread(pool, 1, 150, false);
        Thread borrowerThread = new Thread(borrower);
        Thread minIdleThread = new Thread(new Runnable() {
            public void run() {
                try {
                    pool.evict();
                } catch (Exception e) {
                }
                try {
                    pool.ensureMinIdle();
                } catch (Exception e) {
                }
            }
        });
        minIdleThread.start();
        borrowerThread.start();
        minIdleThread.join();
        borrowerThread.join();
        assert(borrower.failed()==false);
        pool.close();
    }
    class TestThread implements Runnable {
        java.util.Random _random = new java.util.Random();
        ObjectPool _pool = null;
        boolean _complete = false;
        boolean _failed = false;
        int _iter = 100;
        int _delay = 50;
        boolean _randomDelay = true;
        public TestThread(ObjectPool pool) {
            _pool = pool;
        }
        public TestThread(ObjectPool pool, int iter) {
            _pool = pool;
            _iter = iter;
        }
        public TestThread(ObjectPool pool, int iter, int delay) {
            _pool = pool;
            _iter = iter;
            _delay = delay;
        }
        public TestThread(ObjectPool pool, int iter, int delay,
                boolean randomDelay) {
            _pool = pool;
            _iter = iter;
            _delay = delay;
            _randomDelay = randomDelay;
        }
        public boolean complete() {
            return _complete;
        }
        public boolean failed() {
            return _failed;
        }
        public void run() {
            for (int i = 0; i < _iter; i++) {
                long delay = _randomDelay ? (long) _random.nextInt(_delay)
                        : _delay;
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                }
                Object obj = null;
                try {
                    obj = _pool.borrowObject();
                } catch (Exception e) {
                    e.printStackTrace();
                    _failed = true;
                    _complete = true;
                    break;
                }
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                }
                try {
                    _pool.returnObject(obj);
                } catch (Exception e) {
                    _failed = true;
                    _complete = true;
                    break;
                }
            }
            _complete = true;
        }
    }
    public class SimpleFactory implements PoolableObjectFactory {
        public SimpleFactory() {
            this(true);
        }
        public SimpleFactory(boolean valid) {
            this(valid, valid);
        }
        public SimpleFactory(boolean evalid, boolean ovalid) {
            evenValid = evalid;
            oddValid = ovalid;
        }
        void setValid(boolean valid) {
            setEvenValid(valid);
            setOddValid(valid);
        }
        void setEvenValid(boolean valid) {
            evenValid = valid;
        }
        void setOddValid(boolean valid) {
            oddValid = valid;
        }
        public void setThrowExceptionOnPassivate(boolean bool) {
            exceptionOnPassivate = bool;
        }
        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }
        public void setDestroyLatency(long destroyLatency) {
            this.destroyLatency = destroyLatency;
        }
        public void setMakeLatency(long makeLatency) {
            this.makeLatency = makeLatency;
        }
        public Object makeObject() {
            synchronized (this) {
                activeCount++;
                if (activeCount > maxActive) {
                    throw new IllegalStateException(
                            "Too many active instances: " + activeCount);
                }
            }
            if (makeLatency > 0) {
                doWait(makeLatency);
            }
            return String.valueOf(makeCounter++);
        }
        public void destroyObject(Object obj) {
            if (destroyLatency > 0) {
                doWait(destroyLatency);
            }
            synchronized (this) {
                activeCount--;
            }
        }
        public boolean validateObject(Object obj) {
            if (enableValidation) {
                return validateCounter++ % 2 == 0 ? evenValid : oddValid;
            } else {
                return true;
            }
        }
        public void activateObject(Object obj) throws Exception {
            if (exceptionOnActivate) {
                if (!(validateCounter++ % 2 == 0 ? evenValid : oddValid)) {
                    throw new Exception();
                }
            }
        }
        public void passivateObject(Object obj) throws Exception {
            if (exceptionOnPassivate) {
                throw new Exception();
            }
        }
        int makeCounter = 0;
        int validateCounter = 0;
        int activeCount = 0;
        boolean evenValid = true;
        boolean oddValid = true;
        boolean exceptionOnPassivate = false;
        boolean exceptionOnActivate = false;
        boolean enableValidation = true;
        long destroyLatency = 0;
        long makeLatency = 0;
        int maxActive = Integer.MAX_VALUE;
        public boolean isThrowExceptionOnActivate() {
            return exceptionOnActivate;
        }
        public void setThrowExceptionOnActivate(boolean b) {
            exceptionOnActivate = b;
        }
        public boolean isValidationEnabled() {
            return enableValidation;
        }
        public void setValidationEnabled(boolean b) {
            enableValidation = b;
        }
        private void doWait(long latency) {
            try {
                Thread.sleep(latency);
            } catch (InterruptedException ex) {
            }
        }
    }
}
