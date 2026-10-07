/* =========================================================================
 * File: $Id: BlockingCache.java,v 1.1 2010/06/18 17:01:09 smhuang Exp $BlockingCache.java,v$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package edu.tamu.aser.tests.net.sf.cache4j.impl;

import edu.tamu.aser.tests.net.sf.cache4j.CacheException;
import edu.tamu.aser.tests.net.sf.cache4j.Cache;
import edu.tamu.aser.tests.net.sf.cache4j.CacheConfig;
import edu.tamu.aser.tests.net.sf.cache4j.CacheInfo;
import edu.tamu.aser.tests.net.sf.cache4j.ManagedCache;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.io.IOException;

/**
 * ����� BlockingCache ��� ���������� ���������� {@link net.sf.cache4j.Cache}
 * � ������������� �� ������ ��������. ����� �������� ������ <code>get, put, remove</code>
 * �������� � ���������� �������������� ������� � ����� � ������ ����������
 * (� �������� ��������� ������). ���� ����� <code>get</code> ������ null ��
 * ���������� � �������������� �� ���������. �����, ������� ������������
 * �������������, ������ ��������� ������ � ��������� � ���, ������ ����� �����
 * ���������� ����� ����� � ��� �������������� ������ ������ ���������� ������.
 * ����� ��������� ��������� ���������(���������) ������, �������� ��� � ����,
 * ������ � ����� ������.
 * <br>
 * ��������� ���������� ����:
 * <pre>
 *     Cache _personCache = CacheFactory.getInstance().getCache("Person");
 * </pre>
 * ���������\��������� �������:
 * <pre>
 *     Long id = ... ;
 *     Person person = null;
 *     try {
 *         person = (Person)_personCache.get(id);
 *     } catch (CacheException ce) {
 *         //throw new Exception(ce);
 *     }
 *     if (person != null) {
 *         return person;
 *     }
 *     try {
 *         person = loadPersonFromDb(id);
 *     } catch (Exception e) {
 *         //throw new Exception(e);
 *     } finally {
 *         try {
 *             _personCache.put(id, person);
 *         } catch (CacheException ce) {
 *             //throw new Exception(ce);
 *         }
 *     }
 * </pre>
 * �������� �������:
 * <pre>
 *     Person person = ... ;
 *     Long id = person.getId();
 *     removePersonFromDb(id);
 *     try {
 *         _personCache.remove(id);
 *     } catch (CacheException ce) {
 *         //throw new Exception(ce);
 *     }
 * </pre>
 *
 * @version $Revision: 1.1 $ $Date: 2010/06/18 17:01:09 $
 * @author Yuriy Stepovoy. <a href="mailto:stepovoy@gmail.com">stepovoy@gmail.com</a>
 **/

public class BlockingCache implements Cache, ManagedCache {
// ----------------------------------------------------------------------------- ���������
// ----------------------------------------------------------------------------- �������� ������
    /**
     * ����� � ����������� ���������
     */
    private Map _map;

    /**
     * ������ � ��������������� ������ ����\�������� � ����������� �� ��������� ��������
     */
    private TreeMap _tmap;

    /**
     * ������������ ����
     */
    private CacheConfigImpl _config;

    /**
     * ������ �������� ���� � ������
     */
    private long _memorySize;

    /**
     * ���������� � ����
     */
    private CacheInfoImpl _cacheInfo;

    /**
     * ThreadLocal ������������ ��� �������� CacheObject ����� ��������
     * get() - put()\remove(). ���� ����� get() ������ null, ������ CacheObject
     * � ��������� ��������������� ������� ����������� ������� ������� � ��� ���
     * ��������� �������.
     * ���� ����� �� ������ �� �������� ������������� deadlock.
     * ������:
     *   - �����1 ������� CacheObject1 c ������ 1, �����2 ��� ��������� ��
     *     CacheObject1 c ������ 1, �����3 ��� ��������� �� CacheObject1 c ������ 1.
     *   - �����1 ������ ������ CacheObject1 � ���� ���������. �����2 �������
     *     ��������� �� ������ CacheObject1 ����1 � ������� null ������ �������
     *     (������ ��� ������ �� ���������������� ������ �������)
     *   - �����2 �������� ������ � ������ 1 � �������� ��� � ���, �� ��� ���
     *     CacheObject1 ����1 ��� ����� �������1 �������� ����� ������
     *     CacheObject2 � ������ 1. � ����� CacheObject2
     *     ���������� ���������������� ������ � ��������� ���������.
     *   - �����3 ���������� ����� ������ ��������� � ������� ������� CacheObject1
     *     � ������� 1. �����3 ������� �� ��������. deadlock !!!
     */
    private ThreadLocal _tl = new ThreadLocal();

// ----------------------------------------------------------------------------- ����������� ����������
// ----------------------------------------------------------------------------- ������������
// ----------------------------------------------------------------------------- Public ������

    //-------------------------------------------------------------------------- Cache interface
    /**
     * �������� ������ � ���. ���� ����� ������� put() ��� ������ ����� get()
     * � �� ������ null �� � put() ����� �������� ����� �� objId ��� � � ����� get()
     * ����� ��������� CacheException.
     * @param objId ������������� �������
     * @param obj ������
     * @throws CacheException ���� �������� ��������, �������� ���������� ������� �������
     * @throws NullPointerException ���� objId==null
     */
    public void put(Object objId, Object obj) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

        //������ CacheObject �� �������� ������
        CacheObject tlCO = (CacheObject)_tl.get();
        CacheObject co = null;

        int objSize = 0;
        try {
            objSize = _config.getMaxMemorySize()>0 ? Utils.size(obj) : 0;
        } catch (IOException e) {
            throw new CacheException(e.getMessage());
        }

        //��������� �� ��������� �� ������������ ����
        checkOverflow(objSize);

        if (tlCO==null) {
            //���� � ������� ������ ��� CacheObject ������ ����� get() ����� ����
            //��� �� ��������� ��� ������ ������
            co = getCacheObject(objId);
        } else {
            //���� � ������� ������ ���� ������ ������ ����� ���� ����� get() ������ null

            //������������� �� �������� ������ � ������������� ����������� ������� ������ ����
            //�����������. ���� ��� �� ��� �� ��� ������ ������ get() ��� ������� ���� ����
            //� ��� ������ ������ put() ��� ������� ������ ����, � ��� �����������.
            if(tlCO.getObjectId().equals(objId)){
                //����� �� ��������� ����������� � ������ get() ������
                //����� ������ ���������� ������ ������������ ��� ���������� ���������
                co = tlCO;
                //������� ������ �� �������� ������ ����� ������ ����� �����������
                //������ ������ put()
                _tl.set(null);
            } else {
                tlCO.unlock();
                throw new CacheException("Cache:"+_config.getCacheId()+" wait for call put() with objId="+tlCO.getObjectId());
            }
        }

        co.lock();
        _cacheInfo.incPut();
        try {
            //��� ��������� � ������� ������������ ���������� co.reset()
            //� �������������� ���������� ���� � ������� ������ ��� ������� � _tmap
            //������� ������ ����� ������� �� _tmap
            //?:��� �������� ������� �� ���� � ���� ��� ������ ��������
            tmapRemove(co);

            //�������� CacheObject � ������������ �������� ����
            resetCacheObject(co);

            co.setObject(obj);
            co.setObjectSize(objSize);

            synchronized(this) {
                _memorySize = _memorySize + objSize;
            }

            tmapPut(co);
        } finally {
            co.unlock();
        }
    }

    /**
     * ���������� ������ �� ����. ���� ����� ������ null �� ���������� ������������
     * ������ � ���������� objId. ����� ����� � ��� ����� ��������� ������ �
     * ������� ����� objId ����� ��� ������ ������ ������ ����� ���������
     * CacheException.
     * @param objId ������������� �������
     * @return ������ ������������ ������ � ��� ������, ���� ������ ������
     * � ����� ����� ������� �� ����������� � �� ��������� ����� �����������.
     * @throws CacheException ���� �������� ��������
     * @throws NullPointerException ���� objId==null
     */
    public Object get(Object objId) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

        CacheObject tlCO = (CacheObject)_tl.get();
        if (tlCO!=null) {
            throw new CacheException("Cache:"+_config.getCacheId()+" wait for call put() with objId="+tlCO.getObjectId());
        }

        CacheObject co = getCacheObject(objId);
        co.lock();
        Object o = co.getObject();
        if(o!=null){
            tmapRemove(co);

            if(!valid(co)) {
                resetCacheObject(co);
                //�������� ������ � ������� �����
                _tl.set(co);

                _cacheInfo.incMisses();
                //������ �� �������� ������� ��������� �� �������
                return null;
            } else {
                co.updateStatistics();
                tmapPut(co);

                //������ ���������� � �� �������� ������� ������� ���������� � �������
                co.unlock();
                _cacheInfo.incHits();

                //?:return copy(o);
                return o;
            }
        } else {
            //�������� ������ � ������� ����� ����� ����� ������� � ������ put()
            _tl.set(co);
            _cacheInfo.incMisses();
            //���� ������� ��� �� ��������� �� �������
            return null;
        }
    }

    /**
     * ������� ������ �� ����.
     * @param objId ������������� �������
     * @throws CacheException ���� ����� ������� remove() ��� ������ ����� get()
     * � �� ������ null �� ��� ������ remove() ��������� CacheException.
     * @throws NullPointerException ���� objId==null
     */
    public void remove(Object objId) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

        //���� �� ������� ������� �������� ������ ������ ����� put() �� ��� ������
        //����� ���� ��� get() ������ null
        CacheObject tlCO = (CacheObject)_tl.get();
        if (tlCO!=null) {
            throw new CacheException("Cache:"+_config.getCacheId()+" wait for call put() with objId="+tlCO.getObjectId());
        }

        CacheObject co = null;//getCacheObject(objId);
        synchronized (this) {
            co = (CacheObject)_map.get(objId);
        }

        if (co==null) {
            return;
        }

        co.lock();
        _cacheInfo.incRemove();
        try {
            synchronized (this) {
                tmapRemove(co);

                //����� ��������� ��������� ������� ��� ����� �� ���� � ����
                //��� � ���� ����� ���� ������ ������ � ����� �� objId
                CacheObject co2 = (CacheObject)_map.get(co.getObjectId());
                if(co2!=null && co2==co){
                    _map.remove(co.getObjectId());
                    resetCacheObject(co);
                }
            }
        } finally {
            co.unlock();
        }
    }

    /**
     * ���������� ���������� �������� � ����
     */
    public int size() {
        return _map.size();
    }

    /**
     * ������� ��� ������� �� ����
     * @throws CacheException ���� ����� ������� clear() ��� ������ ����� get()
     * � �� ������ null �� ��� ������ clear() ����� ���������� CacheException.
     */
    public void clear() throws CacheException {
        Object[] objArr = null;
        synchronized (this) {
            objArr = _map.values().toArray();
        }

        //����� �� ������ deadlock ����� ������� �� �������� �� _map � ����� ����� remove()
        for (int i = 0, indx = objArr==null ? 0 : objArr.length; i<indx; i++) {
            remove(((CacheObject)objArr[i]).getObjectId());
        }
    }

    /**
     * ���������� ���������� � ����
     */
    public CacheInfo getCacheInfo() {
        return _cacheInfo;
    }

    /**
     * ���������� ����������� ����
     */
    public CacheConfig getCacheConfig() {
        return _config;
    }
    //-------------------------------------------------------------------------- Cache interface

    //-------------------------------------------------------------------------- ManagedCache interface

    /**
     * ������������� ������������ ����. ��� ��������� ������������ ��� �������
     * ���� ��������.
     * @param config ������������
     * @throws CacheException ���� �������� ��������
     * @throws NullPointerException ���� config==null
     */
    public synchronized void setCacheConfig(CacheConfig config) throws CacheException {
        if(config==null) {
            throw new NullPointerException("config is null");
        }

        _config = (CacheConfigImpl)config;

        _map = _config.getMaxSize()>1000 ? new HashMap(1024) : new HashMap();
        _memorySize = 0;
        _tmap = new TreeMap(_config.getAlgorithmComparator());
        _cacheInfo = new CacheInfoImpl();
        _tl.set(null);
    }

    /**
     * ��������� ������� ����. ��������� ������� � ������� ����������� �����
     * ����� ��� �������� ������ �������� ��� ���� ������ ����� null.
     * @throws CacheException ���� ����� ������� clean() ��� ������ ����� get()
     * � �� ������ null �� ��� ������ clean() ����� ���������� CacheException.
     */
    public void clean() throws CacheException {
        //������� �� ���� ����� ������� �� ������� ?
        if(_config.getTimeToLive()==0 && _config.getIdleTime()==0){
            return;
        }

        Object[] objArr = null;
        synchronized (this) {
            objArr = _map.values().toArray();
        }

        for (int i = 0, indx = objArr==null ? 0 : objArr.length; i<indx; i++) {
            CacheObject co = (CacheObject)objArr[i];
            //���� ������ �� �������� �� ������ ����� �������
            if ( !valid(co) ) {
                remove(co.getObjectId());
            }
        }
    }

    //-------------------------------------------------------------------------- ManagedCache interface

// ----------------------------------------------------------------------------- Package scope ������
// ----------------------------------------------------------------------------- Protected ������
// ----------------------------------------------------------------------------- Private ������

    /**
     * ���� ��� ����������, �� ���������� �������� ��� �� �������, ��
     * ��������� ���� ������ � ����������� � ���������� LFU, LRU, FIFO, ...
     */
    private void checkOverflow(int objSize) {
        synchronized(this) {
            //��������� ������������ �� ���������� ��� ������� ?
            //���� ��������� ������� ������ ��, ��������,  �� ���� ����� ������� ���������
            //�������� �������� ������� while
            while( (_config.getMaxSize() > 0 &&  _map.size()+1>_config.getMaxSize() ) ||
                    (_config.getMaxMemorySize()  > 0 && _memorySize+objSize > _config.getMaxMemorySize())  ) {

                //���� � tmap ��� �� ���� ������� ������ �������
                //��� ������ ���������� ��� ����� ����������� �������
                //��� LRU ��� ����� ����� ������ ������������� ������
                Object firstKey = _tmap.size()==0 ? null : _tmap.firstKey();
                CacheObject fco = firstKey==null ? null : (CacheObject)_tmap.remove(firstKey);

                if(fco!=null) {
                    //����� ��������� ��������� ������� ��� ����� �� ���� � ����
                    //��� � ���� ����� ���� ������ ������ � ����� �� objId
                    CacheObject co = (CacheObject)_map.get(fco.getObjectId());
                    if(co!=null && co==fco){
                        _map.remove(fco.getObjectId());
                        resetCacheObject(fco);
                    }
                }
            } //while
        }
    }
    /**
     * ������� ������ �� _tmap
     */
    private void tmapRemove(CacheObject co){
        synchronized(this) {
            _tmap.remove(co);
        }
    }
    /**
     * �������� ���������� ������ � _tmap
     * @param co ���������� ������
     */
    private void tmapPut(CacheObject co){
        synchronized(this) {
            //���� � ��������, ������� �������� _tmap, ������ ���� ���������
            //_tmap ����� ��������� ������ �� �������� ������� ���� � _map
            Object mapO = _map.get(co.getObjectId());
            if(mapO!=null && mapO==co){
                _tmap.put(co, co);
            }
        }
    }


    /**
     * ���������� ������ CacheObject ��� �������������� objId.
     * ���� ������� CacheObject ��� �� �� ��������.
     * @param objId ������������� �������
     */
    private CacheObject getCacheObject(Object objId) {
        synchronized (this) {
            CacheObject co = (CacheObject)_map.get(objId);
            if (co == null) {
                co = _config.newCacheObject(objId);
                _map.put(objId, co);
            }
            return co;
        }
    }
    /**
     * ���������� true ���� ������ ��������.
     * ����������� ����� ����� ������� � ����� ����������� �������
     * @param co ���������� ������
     */
    private boolean valid(CacheObject co){
        long curTime = System.currentTimeMillis();
        return  (_config.getTimeToLive()==0 || (co.getCreateTime()  + _config.getTimeToLive()) >= curTime) &&
                (_config.getIdleTime()==0 || (co.getLastAccessTime() + _config.getIdleTime()) >= curTime) &&
                //���� ������������ soft ������ �� �������� �������� ����� �������
                //������ CacheObject ����� ��� �� ����
                co.getObject()!=null;
    }
    /**
     *  ���� ������ ��� � ���� ��:
     *   - ������������� ������ ����
     *   - ������� ������ �� ������, �������� ������ �������, ���������� ����������
     * @param co ���������� ������
     */
    private void resetCacheObject(CacheObject co){
        synchronized(this) {
            _memorySize = _memorySize - co.getObjectSize();
        }
        co.reset();
    }
// ----------------------------------------------------------------------------- Inner ������
    private class CacheInfoImpl implements CacheInfo {
        private long _hit;
        private long _miss;
        private long _put;
        private long _remove;

        synchronized void incHits(){
            _hit++;
        }
        synchronized void incMisses(){
            _miss++;
        }
        synchronized void incPut(){
            _put++;
        }
        synchronized void incRemove(){
            _remove++;
        }
        public long getCacheHits(){
            return _hit;
        }
        public long getCacheMisses(){
            return _miss;
        }
        public long getTotalPuts() {
            return _put;
        }
        public long getTotalRemoves() {
            return _remove;
        }
        public synchronized void reset() {
            _hit = 0;
            _miss = 0;
            _put = 0;
            _remove = 0;
        }
        public long getMemorySize() {
            return _memorySize;
        }
        public String toString(){
            return "hit:"+_hit+" miss:"+_miss+" memorySize:"+_memorySize;
            //return "hit:"+_hit+" miss:"+_miss+" memorySize:"+_memorySize+" size:"+_map.size()+" tsize:"+_tmap.size();
        }
    }
}

/*
$Log: BlockingCache.java,v $
Revision 1.1  2010/06/18 17:01:09  smhuang
*** empty log message ***

*/
