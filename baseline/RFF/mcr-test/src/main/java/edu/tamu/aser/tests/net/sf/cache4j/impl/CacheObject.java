/* =========================================================================
 * File: $Id: CacheObject.java,v 1.1 2010/06/18 17:01:11 smhuang Exp $CacheObject.java,v$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package edu.tamu.aser.tests.net.sf.cache4j.impl;

import edu.tamu.aser.tests.net.sf.cache4j.CacheException;

/**
 * ����� CacheObject ��� �������� ��� ���������� ��������.
 *
 * @version $Revision: 1.1 $ $Date: 2010/06/18 17:01:11 $
 * @author Yuriy Stepovoy. <a href="mailto:stepovoy@gmail.com">stepovoy@gmail.com</a>
 **/

public class CacheObject {
// ----------------------------------------------------------------------------- ���������
// ----------------------------------------------------------------------------- �������� ������

    /**
     * ������������� �������
     */
    private Object _objId;

    /**
     * ���������� ������
     */
    protected Object _obj;

    /**
     * ���������� ��������� � �������
     */
    private long _accessCount;

    /**
     * ����� �������� �������
     */
    private long _createTime;

    /**
     * ����� ���������� ������� � �������
     */
    private long _lastAccessTime;

    /**
     * ������ ������� � ������
     */
    private int _objSize;

    /**
     * ����� ����������� ������
     */
    private Thread _lockThread;

    /**
     * ���������� ������������� �������
     */
    private long _id;

    //private long _version = 0; //������ �������
    //private int _priority = 0; //���������� �������
// ----------------------------------------------------------------------------- ����������� ����������
// ----------------------------------------------------------------------------- ������������

    /**
     * �����������
     * @param objId ������������� ����������� �������
     */
    CacheObject(Object objId) {
        _objId = objId;
        _obj = null;

        _createTime = System.currentTimeMillis();
        _accessCount = 1;
        _lastAccessTime = _createTime;
        _objSize = 0;
        _lockThread = null;

        _id = nextId();
    }

// ----------------------------------------------------------------------------- Public ������

    /**
     * ��������� ��� ������ ������ ������, ���� ����� �������������� ��� ������
     * ����� ���� �������. � �������� ������ ������ ���� ����� ����� ��������
     * ������������ ���������� ���, ��� �� ����� ��������� � ���������� ��������
     * ������.
     * @throws CacheException
     */
    synchronized void lock() throws CacheException {
        // � ������ Mutex ���� ����� ��������
        //if (Thread.interrupted()) throw new InterruptedException();
        //?
        //synchronized (this) {


        //���� ������ ������������ � ������� ����� ����� ������ ������� ������������
        //�� ������ �� ������. ��� c������ ��� ���� ����� ��� ��������� ������ lock
        //���������� ����� �� ������ � �������� ��������� ����.
        if(_lockThread!=null && Thread.currentThread()==_lockThread){
            return;
        }

        try {
            while (_lockThread!=null) {
                //System.out.println("" + this.hashCode() + " WAIT LOCK Thread:" + Thread.currentThread().getName() + " " + (_lockThread!=null));
                wait();
                //System.out.println("" + this.hashCode() + " WAKE UP LOCK Thread:" + Thread.currentThread().getName() + " " + (_lockThread!=null));
            }
            //System.out.println(""+this.hashCode()+" GET LOCK Thread:"+Thread.currentThread().getName()+" "+(_lockThread!=null));
            _lockThread = Thread.currentThread(); //������������� ����������� �����
        } catch (InterruptedException ex) {
            notify();      //��� � ��� ���� �������
            //notifyAll(); //��������� ��� ������
            throw new CacheException(ex.getMessage());
        }
    }

    /**
     * ������� ���������� � ������� � ����� ���� ����� ��������� ���������� ��
     * ������� ������.
     */
    synchronized void unlock() {
        _lockThread = null;

        //System.out.println("" + this.hashCode() + " RELEASE LOCK Thread:" + Thread.currentThread().getName());
        notify();      //��� � ��� ���� �������
        //notifyAll(); //��������� ��� ������
    }

    /**
     * ���������� ���������� ������
     */
    Object getObject() {
        return _obj;
    }

    /**
     * ������������� ���������� ������
     */
    void setObject(Object obj) {
        _obj = obj;
    }

    /**
     * ���������� ������������� ����������� �������
     */
    Object getObjectId(){
        return _objId;
    }

    /**
     * ���������� ���������� ��������� � �������
     */
    long getAccessCount() {
        return _accessCount;
    }

    /**
     * ���������� ����� �������� ������� � �������������
     */
    long getCreateTime() {
        return _createTime;
    }

    /**
     * ���������� ����� ���������� ������� � �������������
     */
    long getLastAccessTime() {
        return _lastAccessTime;
    }

    /**
     * ���������� ������ ������� � ������
     */
    long getObjectSize() {
        return _objSize;
    }

    /**
     * ������������� ������ ������� � ������
     */
    void setObjectSize(int objSize) {
        _objSize = objSize;
    }

    /**
     * ��������� ���������� �� �������
     */
    void updateStatistics() {
        _accessCount++;
        _lastAccessTime = System.currentTimeMillis();

        _id = nextId();
    }

    /**
     * ���������� ���������� �������
     */
    void reset(){
        _obj = null;
        _objSize = 0;
        _createTime = System.currentTimeMillis();
        _accessCount = 1;
        _lastAccessTime = _createTime;

        _id = nextId();
    }

    long getId() {
        return _id;
    }

    /**
     * ���������� ��������� ������������� �������
     */
    public String toString(){
        return "id:"+_objId+
                " createTime:"+_createTime+
                " lastAccess:"+_lastAccessTime+
                " accessCount:"+_accessCount+
                " size:"+_objSize+
                " object:"+_obj;
    }
// ----------------------------------------------------------------------------- Package scope ������
// ----------------------------------------------------------------------------- Protected ������
// ----------------------------------------------------------------------------- Private ������

    /**
     * �������
     */
    private static long ID = 0;
    /**
     * ���������� ��������� ���������� �������������
     */
    private static synchronized long nextId(){
        return ID++;
    }
// ----------------------------------------------------------------------------- Inner ������
}

/*
$Log: CacheObject.java,v $
Revision 1.1  2010/06/18 17:01:11  smhuang
*** empty log message ***

*/
