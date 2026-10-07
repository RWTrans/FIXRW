/* =========================================================================
 * File: $Id: FIFOComparator.java,v 1.1 2010/06/18 17:01:09 smhuang Exp $FIFOComparator.java,v$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package edu.tamu.aser.tests.net.sf.cache4j.impl;

import edu.tamu.aser.tests.net.sf.cache4j.impl.CacheObject;

import java.util.Comparator;

/**
 * ����� FIFOComparator ������������ ��� ��������� �������� � ������������ �
 * ���������� FIFO (First In First Out).
 * ���� ��� ��������� ������ ������� ��� �������������, �� ������� ��� ����������
 * ��������, �� ��������� ������ ������� ��� ������� ������.
 *
 * @version $Revision: 1.1 $ $Date: 2010/06/18 17:01:09 $
 * @author Yuriy Stepovoy. <a href="mailto:stepovoy@gmail.com">stepovoy@gmail.com</a>
 **/

public class FIFOComparator implements Comparator {
// ----------------------------------------------------------------------------- ���������
// ----------------------------------------------------------------------------- �������� ������
// ----------------------------------------------------------------------------- ����������� ����������
// ----------------------------------------------------------------------------- ������������
// ----------------------------------------------------------------------------- Public ������

    public int compare(Object o1, Object o2) {
        CacheObject co1 = (CacheObject)o1;
        CacheObject co2 = (CacheObject)o2;
        return co1.getCreateTime()<co2.getCreateTime() ?
                  -1
                : co1.getCreateTime()==co2.getCreateTime() ?
                    ( co1.getId()<co2.getId() ? -1 : (co1.getId()==co2.getId() ? 0 : 1) )
                     : 1;
    }


    public boolean equals(Object obj) {
        return obj==null ? false : (obj instanceof FIFOComparator);
    }

// ----------------------------------------------------------------------------- Package scope ������
// ----------------------------------------------------------------------------- Protected ������
// ----------------------------------------------------------------------------- Private ������
// ----------------------------------------------------------------------------- Inner ������
}

/*
$Log: FIFOComparator.java,v $
Revision 1.1  2010/06/18 17:01:09  smhuang
*** empty log message ***

*/
