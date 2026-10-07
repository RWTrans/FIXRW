/* =========================================================================
 * File: $Id: EmptyCache.java,v 1.1 2010/06/18 17:01:12 smhuang Exp $EmptyCache.java,v$
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

/**
 * ����� EmptyCache ��� ���������� ���������� {@link net.sf.cache4j.Cache}
 * ��� ����� ���� �������� ����������������. ����� <code>get()</code> ������
 * ���������� <code>null</code>. ��� ���������� ���� ����� ������������ ���
 * ������������� ��������� �����������.
 * @version $Revision: 1.1 $ $Date: 2010/06/18 17:01:12 $
 * @author Yuriy Stepovoy. <a href="mailto:stepovoy@gmail.com">stepovoy@gmail.com</a>
 **/
public class EmptyCache implements Cache, ManagedCache {
// ----------------------------------------------------------------------------- ���������
// ----------------------------------------------------------------------------- �������� ������

    /**
     * ������������ ����
     */
    private CacheConfigImpl _config;

    /**
     * ���������� � ����
     */
    private CacheInfo _cacheInfo;

// ----------------------------------------------------------------------------- ����������� ����������
// ----------------------------------------------------------------------------- ������������
// ----------------------------------------------------------------------------- Public ������

    //-------------------------------------------------------------------------- Cache interface
    /**
     * �������� ������ � ���.
     * @param objId ������������� �������
     * @param obj ������
     * @throws NullPointerException ���� objId==null
     */
    public void put(Object objId, Object obj) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }
    }

    /**
     * ���������� ������ �� ����. ������ ���������� null.
     * @param objId ������������� �������
     * @return ������ ���������� null
     * @throws NullPointerException ���� objId==null
     */
    public Object get(Object objId) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }
        return null;
    }

    /**
     * ������� ������ �� ����.
     * @param objId ������������� �������
     * @throws NullPointerException ���� objId==null
     */
    public void remove(Object objId) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }
    }

    /**
     * ���������� ���������� �������� � ����
     * @return ������ ���������� 0.
     */
    public int size() {
        return 0;
    }

    /**
     * ������� ��� ������� �� ����
     */
    public void clear() throws CacheException {
    }

    /**
     * ���������� ���������� � ����
     * @return ������ ���������� null
     */
    public CacheInfo getCacheInfo() {
        return _cacheInfo;
    }

    /**
     * ���������� ����������� ����
     * @return ���������� ������������ ����
     */
    public CacheConfig getCacheConfig() {
        return _config;
    }

    //-------------------------------------------------------------------------- Cache interface

    //-------------------------------------------------------------------------- ManagedCache interface

    /**
     * ������������� ������������ ����.
     * @param config ������������
     * @throws NullPointerException ���� config==null
     */
    public void setCacheConfig(CacheConfig config) {
        if(config==null) {
            throw new NullPointerException("config is null");
        }
        _config = (CacheConfigImpl)config;
        _cacheInfo = new CacheInfoImpl();
    }

    /**
     * ��������� ������� ���� �� ���������� ��������.
     */
    public void clean() throws CacheException {
    }

    //-------------------------------------------------------------------------- ManagedCache interface

// ----------------------------------------------------------------------------- Package scope ������
// ----------------------------------------------------------------------------- Protected ������
// ----------------------------------------------------------------------------- Private ������
// ----------------------------------------------------------------------------- Inner ������
    private class CacheInfoImpl implements CacheInfo {
        public long getCacheHits(){
            return 0;
        }
        public long getCacheMisses(){
            return 0;
        }
        public long getTotalPuts() {
            return 0;
        }
        public long getTotalRemoves() {
            return 0;
        }
        public void reset() {

        }
        public long getMemorySize() {
            return 0;
        }
        public String toString(){
            return "hit:0 miss:0 memorySize:0";
        }
    }
}

/*
$Log: EmptyCache.java,v $
Revision 1.1  2010/06/18 17:01:12  smhuang
*** empty log message ***

*/
