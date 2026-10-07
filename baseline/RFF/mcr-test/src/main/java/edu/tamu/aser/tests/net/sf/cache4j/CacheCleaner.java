/* =========================================================================
 * File: CacheCleaner.java$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package edu.tamu.aser.tests.net.sf.cache4j;


/**
 * ����� CacheCleaner ��������� ������� ���������� ��������
 */

public class CacheCleaner extends Thread {
// ----------------------------------------------------------------------------- ���������
// ----------------------------------------------------------------------------- �������� ������

    /**
     * �������� �������
     */
    private long _cleanInterval;

    /**
     * true ���� ����� ��������� � ������
     */
    private boolean _sleep = false;

// ----------------------------------------------------------------------------- ����������� ����������
// ----------------------------------------------------------------------------- ������������

    /**
     * �����������
     * @param cleanInterval ��������(� �������������) � ������� ����� ��������� �������
     */
    public CacheCleaner(long cleanInterval) {
        _cleanInterval = cleanInterval;

        setName(this.getClass().getName());
        setDaemon(true);
        //������������� ����������� ��������� �� ����� ������ ��� �������� ����������
        //�������� �� ����� ������ ������
        //setPriority(Thread.MIN_PRIORITY);
    }

// ----------------------------------------------------------------------------- Public ������

    /**
     * ������������� �������� �������
     * @param cleanInterval ��������(� �������������) � ������� ����� ��������� �������
     */
    public void setCleanInterval(long cleanInterval) {
        _cleanInterval = cleanInterval;

        synchronized(this){
            if(_sleep){
                interrupt();
            }
        }
    }

    /**
     * �������� �����. ��� ���� ����� ���������� ����� <code>clean</code>
     */
    public void run() {
        while(true)  {
            try {
                CacheFactory cacheFactory = CacheFactory.getInstance();
                Object[] objIdArr = cacheFactory.getCacheIds();
                for (int i = 0, indx = objIdArr==null ? 0 : objIdArr.length; i<indx; i++) {
                    ManagedCache cache = (ManagedCache)cacheFactory.getCache(objIdArr[i]);
                    if(cache!=null){
                        cache.clean();
                    }
                    yield();
                }
            } catch (Throwable t){
                t.printStackTrace();
            }

            _sleep = true;
            try {
                sleep(_cleanInterval);
            } catch (Throwable t){
            } finally {
                _sleep = false;
            }
        }
    }

// ----------------------------------------------------------------------------- Package scope ������
// ----------------------------------------------------------------------------- Protected ������
// ----------------------------------------------------------------------------- Private ������
// ----------------------------------------------------------------------------- Inner ������

}

/*
$Log: CacheCleaner.java,v $
Revision 1.1  2010/06/18 17:01:12  smhuang
*** empty log message ***

*/
