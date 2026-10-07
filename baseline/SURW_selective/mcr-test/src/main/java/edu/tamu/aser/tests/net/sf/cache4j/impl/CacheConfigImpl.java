/* =========================================================================
 * File: CacheConfigImpl.java$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package edu.tamu.aser.tests.net.sf.cache4j.impl;

import edu.tamu.aser.tests.net.sf.cache4j.CacheConfig;

import java.util.Comparator;

/**
 * ����� CacheConfigImpl �������� ������������ ����
 *
 * @version $Revision: 1.1 $ $Date: 2010/06/18 17:01:11 $
 * @author Yuriy Stepovoy. <a href="mailto:stepovoy@gmail.com">stepovoy@gmail.com</a>
 **/

public class CacheConfigImpl implements CacheConfig {
// ----------------------------------------------------------------------------- ���������
// ----------------------------------------------------------------------------- �������� ������
    /** ������������� ���� */
    private Object _cacheId;
    /** �������� ���� */
    private String _cacheDesc;
    /** ������������ ����� ����� �������� � ���� */
    private long _ttl;
    /** ������������ ����� ����������� �������� � ���� */
    private long _idleTime;
    /** ������������ ����� ���������� ��������� ���� */
    private long _maxMemorySize;
    /** ������������ ���������� �������� � ���� */
    private int _maxSize;
    /** ��� ���� */
    private String _type;
    /** �������� ���������� �������� */
    private String _algorithm;
    /** ��� ������ �� �������� ������ */
    private String _reference;
    /** ��� ������ �� �������� ������ */
    private int _referenceInt;
// ----------------------------------------------------------------------------- ����������� ����������

    /** �������� ���������� �������� - LRU */
    static final String LRU = "lru";
    /** �������� ���������� �������� - LFU */
    static final String LFU = "lfu";
    /** �������� ���������� �������� - FIFO */
    static final String FIFO = "fifo";

    /** ��� ����� � �������� - STRONG */
    static final int STRONG = 1;
    /** ��� ����� � �������� - SOFT */
    static final int SOFT = 2;

// ----------------------------------------------------------------------------- ������������
    /**
     * �����������
     * @param cacheId ������������� ����
     * @param cacheDesc �������� ����
     * @param ttl ������������ ����� ����� �������� � ����
     * @param idleTime ������������ ����� ����������� �������� � ����
     * @param maxMemorySize ������������ ����� ���������� ��������� ����
     * @param maxSize ������������ ���������� �������� � ����
     * @param type ��� ����
     * @param algorithm �������� ���������� ��������
     * @param reference ��� ������ �� �������� ������
     */
    public CacheConfigImpl(Object cacheId,
                           String cacheDesc,
                           long ttl,
                           long idleTime,
                           long maxMemorySize,
                           int maxSize,
                           String type,
                           String algorithm,
                           String reference) {
        _cacheId = cacheId;
        _cacheDesc = cacheDesc;
        _ttl = ttl<0 ? 0 : ttl;
        _idleTime = idleTime<0 ? 0 : idleTime;
        _maxMemorySize = maxMemorySize<0 ? 0 : maxMemorySize;
        _maxSize = maxSize<0 ? 0 : maxSize;
        _type = type;
        _algorithm = algorithm;
        _reference = reference;

        if(_reference.equalsIgnoreCase("strong")){
            _referenceInt = STRONG;
        } else if(_reference.equalsIgnoreCase("soft")){
            _referenceInt = SOFT;
        }
    }
// ----------------------------------------------------------------------------- Public ������

    public Object getCacheId() {
        return _cacheId;
    }
    public String getCacheDesc() {
        return _cacheDesc;
    }
    public long getTimeToLive() {
        return _ttl;
    }
    public long getIdleTime() {
        return _idleTime;
    }
    public long getMaxMemorySize() {
        return _maxMemorySize;
    }
    public int getMaxSize() {
        return _maxSize;
    }
    public String getType() {
        return _type;
    }
    public String getAlgorithm() {
        return _algorithm;
    }
    public String getReference(){
        return _reference;
    }
// ----------------------------------------------------------------------------- Package scope ������
    /**
     * ������ �������� ��� �������� ��������
     * @param objId ������������� �������
     * @return ���������� ����� ������
     */
    CacheObject newCacheObject(Object objId){
        return _referenceInt==CacheConfigImpl.STRONG ? new CacheObject(objId) : new SoftCacheObject(objId);
    }

    /**
     * ���������� ���������� � ������ ��������� ����������
     */
    Comparator getAlgorithmComparator(){
        if(_algorithm.equals(CacheConfigImpl.LRU)) {
            return new LRUComparator();
        } else if(_algorithm.equals(CacheConfigImpl.LFU)) {
            return new LFUComparator();
        } else if(_algorithm.equals(CacheConfigImpl.FIFO)) {
            return new FIFOComparator();
        } else {
            throw new RuntimeException("Unknown algorithm:"+_algorithm);
        }
    }
// ----------------------------------------------------------------------------- Protected ������
// ----------------------------------------------------------------------------- Private ������
// ----------------------------------------------------------------------------- Inner ������
}

/*
$Log: CacheConfigImpl.java,v $
Revision 1.1  2010/06/18 17:01:11  smhuang
*** empty log message ***

*/
