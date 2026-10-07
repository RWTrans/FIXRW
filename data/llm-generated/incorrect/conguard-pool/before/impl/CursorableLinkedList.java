package org.apache.commons.pool.impl;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.lang.ref.WeakReference;
class CursorableLinkedList implements List, Serializable {
    private static final long serialVersionUID = 8836393098519411393L;
    public boolean add(Object o) {
        insertListable(_head.prev(),null,o);
        return true;
    }
    public void add(int index, Object element) {
        if(index == _size) {
            add(element);
        } else {
            if(index < 0 || index > _size) {
                throw new IndexOutOfBoundsException(String.valueOf(index) + " < 0 or " + String.valueOf(index) + " > " + _size);
            }
            Listable succ = (isEmpty() ? null : getListableAt(index));
            Listable pred = (null == succ ? null : succ.prev());
            insertListable(pred,succ,element);
        }
    }
    public boolean addAll(Collection c) {
        if(c.isEmpty()) {
            return false;
        }
        Iterator it = c.iterator();
        while(it.hasNext()) {
            insertListable(_head.prev(),null,it.next());
        }
        return true;
    }
    public boolean addAll(int index, Collection c) {
        if(c.isEmpty()) {
            return false;
        } else if(_size == index || _size == 0) {
            return addAll(c);
        } else {
            Listable succ = getListableAt(index);
            Listable pred = (null == succ) ? null : succ.prev();
            Iterator it = c.iterator();
            while(it.hasNext()) {
                pred = insertListable(pred,succ,it.next());
            }
            return true;
        }
    }
    public boolean addFirst(Object o) {
        insertListable(null,_head.next(),o);
        return true;
    }
    public boolean addLast(Object o) {
        insertListable(_head.prev(),null,o);
        return true;
    }
    public void clear() {
        Iterator it = iterator();
        while(it.hasNext()) {
            it.next();
            it.remove();
        }
    }
    public boolean contains(Object o) {
        for(Listable elt = _head.next(), past = null; null != elt && past != _head.prev(); elt = (past = elt).next()) {
            if((null == o && null == elt.value()) || 
               (o != null && o.equals(elt.value()))) {
                return true;
            }
        }
        return false;
    }
    public boolean containsAll(Collection c) {
        Iterator it = c.iterator();
        while(it.hasNext()) {
            if(!this.contains(it.next())) {
                return false;
            }
        }
        return true;
    }
    public Cursor cursor() {
        return new Cursor(0);
    }
    public Cursor cursor(int i) {
        return new Cursor(i);
    }
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof List)) {
            return false;
        }
        Iterator it = ((List)o).listIterator();
        for(Listable elt = _head.next(), past = null; null != elt && past != _head.prev(); elt = (past = elt).next()) {
            if(!it.hasNext() || (null == elt.value() ? null != it.next() : !(elt.value().equals(it.next()))) ) {
                return false;
            }
        }
        return !it.hasNext();
    }
    public Object get(int index) {
        return getListableAt(index).value();
    }
    public Object getFirst() {
        try {
            return _head.next().value();
        } catch(NullPointerException e) {
            throw new NoSuchElementException();
        }
    }
    public Object getLast() {
        try {
            return _head.prev().value();
        } catch(NullPointerException e) {
            throw new NoSuchElementException();
        }
    }
    public int hashCode() {
        int hash = 1;
        for(Listable elt = _head.next(), past = null; null != elt && past != _head.prev(); elt = (past = elt).next()) {
            hash = 31*hash + (null == elt.value() ? 0 : elt.value().hashCode());
        }
        return hash;
    }
    public int indexOf(Object o) {
        int ndx = 0;
        if (null == o) {
            for(Listable elt = _head.next(), past = null; null != elt && past != _head.prev(); elt = (past = elt).next()) {
                if (null == elt.value()) {
                    return ndx;
                }
                ndx++;
            }
        } else {
            for(Listable elt = _head.next(), past = null; null != elt && past != _head.prev(); elt = (past = elt).next()) {
                if (o.equals(elt.value())) {
                    return ndx;
                }
                ndx++;
            }
        }
        return -1;
    }
    public boolean isEmpty() {
        return(0 == _size);
    }
    public Iterator iterator() {
        return listIterator(0);
    }
    public int lastIndexOf(Object o) {
        int ndx = _size-1;
        if (null == o) {
            for(Listable elt = _head.prev(), past = null; null != elt && past != _head.next(); elt = (past = elt).prev()) {
                if (null == elt.value()) {
                    return ndx;
                }
                ndx--;
            }
        } else {
            for(Listable elt = _head.prev(), past = null; null != elt && past != _head.next(); elt = (past = elt).prev()) {
                if (o.equals(elt.value())) {
                    return ndx;
                }
                ndx--;
            }
        }
        return -1;
    }
    public ListIterator listIterator() {
        return listIterator(0);
    }
    public ListIterator listIterator(int index) {
        if(index<0 || index > _size) {
            throw new IndexOutOfBoundsException(index + " < 0 or > " + _size);
        }
        return new ListIter(index);
    }
    public boolean remove(Object o) {
        for(Listable elt = _head.next(), past = null; null != elt && past != _head.prev(); elt = (past = elt).next()) {
            if(null == o && null == elt.value()) {
                removeListable(elt);
                return true;
            } else if(o != null && o.equals(elt.value())) {
                removeListable(elt);
                return true;
            }
        }
        return false;
    }
    public Object remove(int index) {
        Listable elt = getListableAt(index);
        Object ret = elt.value();
        removeListable(elt);
        return ret;
    }
    public boolean removeAll(Collection c) {
        if(0 == c.size() || 0 == _size) {
            return false;
        } else {
            boolean changed = false;
            Iterator it = iterator();
            while(it.hasNext()) {
                if(c.contains(it.next())) {
                    it.remove();
                    changed = true;
                }
            }
            return changed;
        }
    }
    public Object removeFirst() {
        if(_head.next() != null) {
            Object val = _head.next().value();
            removeListable(_head.next());
            return val;
        } else {
            throw new NoSuchElementException();
        }
    }
    public Object removeLast() {
        if(_head.prev() != null) {
            Object val = _head.prev().value();
            removeListable(_head.prev());
            return val;
        } else {
            throw new NoSuchElementException();
        }
    }
    public boolean retainAll(Collection c) {
        boolean changed = false;
        Iterator it = iterator();
        while(it.hasNext()) {
            if(!c.contains(it.next())) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }
    public Object set(int index, Object element) {
        Listable elt = getListableAt(index);
        Object val = elt.setValue(element);
        broadcastListableChanged(elt);
        return val;
    }
    public int size() {
        return _size;
    }
    public Object[] toArray() {
        Object[] array = new Object[_size];
        int i = 0;
        for(Listable elt = _head.next(), past = null; null != elt && past != _head.prev(); elt = (past = elt).next()) {
            array[i++] = elt.value();
        }
        return array;
    }
    public Object[] toArray(Object a[]) {
        if(a.length < _size) {
            a = (Object[])Array.newInstance(a.getClass().getComponentType(), _size);
        }
        int i = 0;
        for(Listable elt = _head.next(), past = null; null != elt && past != _head.prev(); elt = (past = elt).next()) {
            a[i++] = elt.value();
        }
        if(a.length > _size) {
            a[_size] = null; 
        }
        return a;
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for(Listable elt = _head.next(), past = null; null != elt && past != _head.prev(); elt = (past = elt).next()) {
            if(_head.next() != elt) {
                buf.append(", ");
            }
            buf.append(elt.value());
        }
        buf.append("]");
        return buf.toString();
    }
    public List subList(int i, int j) {
        if(i < 0 || j > _size || i > j) {
            throw new IndexOutOfBoundsException();
        } else if(i == 0 && j == _size) {
            return this;
        } else {
            return new CursorableSubList(this,i,j);
        }
    }
    protected Listable insertListable(Listable before, Listable after, Object value) {
        _modCount++;
        _size++;
        Listable elt = new Listable(before,after,value);
        if(null != before) {
            before.setNext(elt);
        } else {
            _head.setNext(elt);
        }
        if(null != after) {
            after.setPrev(elt);
        } else {
            _head.setPrev(elt);
        }
        broadcastListableInserted(elt);
        return elt;
    }
    protected void removeListable(Listable elt) {
        _modCount++;
        _size--;
        if(_head.next() == elt) {
            _head.setNext(elt.next());
        }
        if(null != elt.next()) {
            elt.next().setPrev(elt.prev());
        }
        if(_head.prev() == elt) {
            _head.setPrev(elt.prev());
        }
        if(null != elt.prev()) {
            elt.prev().setNext(elt.next());
        }
        broadcastListableRemoved(elt);
    }
    protected Listable getListableAt(int index) {
        if(index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException(String.valueOf(index) + " < 0 or " + String.valueOf(index) + " >= " + _size);
        }
        if(index <=_size/2) {
            Listable elt = _head.next();
            for(int i = 0; i < index; i++) {
                elt = elt.next();
            }
            return elt;
        } else {
            Listable elt = _head.prev();
            for(int i = (_size-1); i > index; i--) {
                elt = elt.prev();
            }
            return elt;
        }
    }
    protected void registerCursor(Cursor cur) {
        for (Iterator it = _cursors.iterator(); it.hasNext(); ) {
            WeakReference ref = (WeakReference) it.next();
            if (ref.get() == null) {
                it.remove();
            }
        }
        _cursors.add( new WeakReference(cur) );
    }
    protected void unregisterCursor(Cursor cur) {
        for (Iterator it = _cursors.iterator(); it.hasNext(); ) {
            WeakReference ref = (WeakReference) it.next();
            Cursor cursor = (Cursor) ref.get();
            if (cursor == null) {
                it.remove();
            } else if (cursor == cur) {
                ref.clear();
                it.remove();
                break;
            }
        }
    }
    protected void invalidateCursors() {
        Iterator it = _cursors.iterator();
        while (it.hasNext()) {
            WeakReference ref = (WeakReference) it.next();
            Cursor cursor = (Cursor) ref.get();
            if (cursor != null) {
                cursor.invalidate();
                ref.clear();
            }
            it.remove();
        }
    }
    protected void broadcastListableChanged(Listable elt) {
        Iterator it = _cursors.iterator();
        while (it.hasNext()) {
            WeakReference ref = (WeakReference) it.next();
            Cursor cursor = (Cursor) ref.get();
            if (cursor == null) {
                it.remove(); 
            } else {
                cursor.listableChanged(elt);
            }
        }
    }
    protected void broadcastListableRemoved(Listable elt) {
        Iterator it = _cursors.iterator();
        while (it.hasNext()) {
            WeakReference ref = (WeakReference) it.next();
            Cursor cursor = (Cursor) ref.get();
            if (cursor == null) {
                it.remove(); 
            } else {
                cursor.listableRemoved(elt);
            }
        }
    }
    protected void broadcastListableInserted(Listable elt) {
        Iterator it = _cursors.iterator();
        while (it.hasNext()) {
            WeakReference ref = (WeakReference) it.next();
            Cursor cursor = (Cursor) ref.get();
            if (cursor == null) {
                it.remove();  
            } else {
                cursor.listableInserted(elt);
            }
        }
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(_size);
        Listable cur = _head.next();
        while (cur != null) {
            out.writeObject(cur.value());
            cur = cur.next();
        }
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        _size = 0;
        _modCount = 0;
        _cursors = new ArrayList();
        _head = new Listable(null,null,null);
        int size = in.readInt();
        for (int i=0;i<size;i++) {
            this.add(in.readObject());
        }
    }
    protected transient int _size = 0;
    protected transient Listable _head = new Listable(null,null,null);
    protected transient int _modCount = 0;
    protected transient List _cursors = new ArrayList();
    static class Listable implements Serializable {
        private Listable _prev = null;
        private Listable _next = null;
        private Object _val = null;
        Listable(Listable prev, Listable next, Object val) {
            _prev = prev;
            _next = next;
            _val = val;
        }
        Listable next() {
            return _next;
        }
        Listable prev() {
            return _prev;
        }
        Object value() {
            return _val;
        }
        void setNext(Listable next) {
            _next = next;
        }
        void setPrev(Listable prev) {
            _prev = prev;
        }
        Object setValue(Object val) {
            Object temp = _val;
            _val = val;
            return temp;
        }
    }
    class ListIter implements ListIterator {
        Listable _cur = null;
        Listable _lastReturned = null;
        int _expectedModCount = _modCount;
        int _nextIndex = 0;
        ListIter(int index) {
            if(index == 0) {
                _cur = new Listable(null,_head.next(),null);
                _nextIndex = 0;
            } else if(index == _size) {
                _cur = new Listable(_head.prev(),null,null);
                _nextIndex = _size;
            } else {
                Listable temp = getListableAt(index);
                _cur = new Listable(temp.prev(),temp,null);
                _nextIndex = index;
            }
        }
        public Object previous() {
            checkForComod();
            if(!hasPrevious()) {
                throw new NoSuchElementException();
            } else {
                Object ret = _cur.prev().value();
                _lastReturned = _cur.prev();
                _cur.setNext(_cur.prev());
                _cur.setPrev(_cur.prev().prev());
                _nextIndex--;
                return ret;
            }
        }
        public boolean hasNext() {
            checkForComod();
            return(null != _cur.next() && _cur.prev() != _head.prev());
        }
        public Object next() {
            checkForComod();
            if(!hasNext()) {
                throw new NoSuchElementException();
            } else {
                Object ret = _cur.next().value();
                _lastReturned = _cur.next();
                _cur.setPrev(_cur.next());
                _cur.setNext(_cur.next().next());
                _nextIndex++;
                return ret;
            }
        }
        public int previousIndex() {
            checkForComod();
            if(!hasPrevious()) {
                return -1;
            }
            return _nextIndex-1;
        }
        public boolean hasPrevious() {
            checkForComod();
            return(null != _cur.prev() && _cur.next() != _head.next());
        }
        public void set(Object o) {
            checkForComod();
            try {
                _lastReturned.setValue(o);
            } catch(NullPointerException e) {
                throw new IllegalStateException();
            }
        }
        public int nextIndex() {
            checkForComod();
            if(!hasNext()) {
                return size();
            }
            return _nextIndex;
        }
        public void remove() {
            checkForComod();
            if(null == _lastReturned) {
                throw new IllegalStateException();
            } else {
                _cur.setNext(_lastReturned == _head.prev() ? null : _lastReturned.next());
                _cur.setPrev(_lastReturned == _head.next() ? null : _lastReturned.prev());
                removeListable(_lastReturned);
                _lastReturned = null;
                _nextIndex--;
                _expectedModCount++;
            }
        }
        public void add(Object o) {
            checkForComod();
            _cur.setPrev(insertListable(_cur.prev(),_cur.next(),o));
            _lastReturned = null;
            _nextIndex++;
            _expectedModCount++;
        }
        protected void checkForComod() {
            if(_expectedModCount != _modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
    public class Cursor extends ListIter implements ListIterator {
        boolean _valid = false;
        Cursor(int index) {
            super(index);
            _valid = true;
            registerCursor(this);
        }
        public int previousIndex() {
            throw new UnsupportedOperationException();
        }
        public int nextIndex() {
            throw new UnsupportedOperationException();
        }
        public void add(Object o) {
            checkForComod();
            Listable elt = insertListable(_cur.prev(),_cur.next(),o);
            _cur.setPrev(elt);
            _cur.setNext(elt.next());
            _lastReturned = null;
            _nextIndex++;
            _expectedModCount++;
        }
        protected void listableRemoved(Listable elt) {
            if(null == _head.prev()) {
                _cur.setNext(null);
            } else if(_cur.next() == elt) {
                _cur.setNext(elt.next());
            }
            if(null == _head.next()) {
                _cur.setPrev(null);
            } else if(_cur.prev() == elt) {
                _cur.setPrev(elt.prev());
            }
            if(_lastReturned == elt) {
                _lastReturned = null;
            }
        }
        protected void listableInserted(Listable elt) {
            if(null == _cur.next() && null == _cur.prev()) {
                _cur.setNext(elt);
            } else if(_cur.prev() == elt.prev()) {
                _cur.setNext(elt);
            }
            if(_cur.next() == elt.next()) {
                _cur.setPrev(elt);
            }
            if(_lastReturned == elt) {
                _lastReturned = null;
            }
        }
        protected void listableChanged(Listable elt) {
            if(_lastReturned == elt) {
                _lastReturned = null;
            }
        }
        protected void checkForComod() {
            if(!_valid) {
                throw new ConcurrentModificationException();
            }
        }
        protected void invalidate() {
            _valid = false;
        }
        public void close() {
            if(_valid) {
                _valid = false;
                unregisterCursor(this);
            }
        }
    }
}
class CursorableSubList extends CursorableLinkedList implements List {
    CursorableSubList(CursorableLinkedList list, int from, int to) {
        if(0 > from || list.size() < to) {
            throw new IndexOutOfBoundsException();
        } else if(from > to) {
            throw new IllegalArgumentException();
        }
        _list = list;
        if(from < list.size()) {
            _head.setNext(_list.getListableAt(from));
            _pre = (null == _head.next()) ? null : _head.next().prev();
        } else {
            _pre = _list.getListableAt(from-1);
        }
        if(from == to) {
            _head.setNext(null);
            _head.setPrev(null);
            if(to < list.size()) {
                _post = _list.getListableAt(to);
            } else {
                _post = null;
            }
        } else {
            _head.setPrev(_list.getListableAt(to-1));
            _post = _head.prev().next();
        }
        _size = to - from;
        _modCount = _list._modCount;
    }
    public void clear() {
        checkForComod();
        Iterator it = iterator();
        while(it.hasNext()) {
            it.next();
            it.remove();
        }
    }
    public Iterator iterator() {
        checkForComod();
        return super.iterator();
    }
    public int size() {
        checkForComod();
        return super.size();
    }
    public boolean isEmpty() {
        checkForComod();
        return super.isEmpty();
    }
    public Object[] toArray() {
        checkForComod();
        return super.toArray();
    }
    public Object[] toArray(Object a[]) {
        checkForComod();
        return super.toArray(a);
    }
    public boolean contains(Object o) {
        checkForComod();
        return super.contains(o);
    }
    public boolean remove(Object o) {
        checkForComod();
        return super.remove(o);
    }
    public Object removeFirst() {
        checkForComod();
        return super.removeFirst();
    }
    public Object removeLast() {
        checkForComod();
        return super.removeLast();
    }
    public boolean addAll(Collection c) {
        checkForComod();
        return super.addAll(c);
    }
    public boolean add(Object o) {
        checkForComod();
        return super.add(o);
    }
    public boolean addFirst(Object o) {
        checkForComod();
        return super.addFirst(o);
    }
    public boolean addLast(Object o) {
        checkForComod();
        return super.addLast(o);
    }
    public boolean removeAll(Collection c) {
        checkForComod();
        return super.removeAll(c);
    }
    public boolean containsAll(Collection c) {
        checkForComod();
        return super.containsAll(c);
    }
    public boolean addAll(int index, Collection c) {
        checkForComod();
        return super.addAll(index,c);
    }
    public int hashCode() {
        checkForComod();
        return super.hashCode();
    }
    public boolean retainAll(Collection c) {
        checkForComod();
        return super.retainAll(c);
    }
    public Object set(int index, Object element) {
        checkForComod();
        return super.set(index,element);
    }
    public boolean equals(Object o) {
        checkForComod();
        return super.equals(o);
    }
    public Object get(int index) {
        checkForComod();
        return super.get(index);
    }
    public Object getFirst() {
        checkForComod();
        return super.getFirst();
    }
    public Object getLast() {
        checkForComod();
        return super.getLast();
    }
    public void add(int index, Object element) {
        checkForComod();
        super.add(index,element);
    }
    public ListIterator listIterator(int index) {
        checkForComod();
        return super.listIterator(index);
    }
    public Object remove(int index) {
        checkForComod();
        return super.remove(index);
    }
    public int indexOf(Object o) {
        checkForComod();
        return super.indexOf(o);
    }
    public int lastIndexOf(Object o) {
        checkForComod();
        return super.lastIndexOf(o);
    }
    public ListIterator listIterator() {
        checkForComod();
        return super.listIterator();
    }
    public List subList(int fromIndex, int toIndex) {
        checkForComod();
        return super.subList(fromIndex,toIndex);
    }
    protected Listable insertListable(Listable before, Listable after, Object value) {
        _modCount++;
        _size++;
        Listable elt = _list.insertListable((null == before ? _pre : before), (null == after ? _post : after),value);
        if(null == _head.next()) {
            _head.setNext(elt);
            _head.setPrev(elt);
        }
        if(before == _head.prev()) {
            _head.setPrev(elt);
        }
        if(after == _head.next()) {
            _head.setNext(elt);
        }
        broadcastListableInserted(elt);
        return elt;
    }
    protected void removeListable(Listable elt) {
        _modCount++;
        _size--;
        if(_head.next() == elt && _head.prev() == elt) {
            _head.setNext(null);
            _head.setPrev(null);
        }
        if(_head.next() == elt) {
            _head.setNext(elt.next());
        }
        if(_head.prev() == elt) {
            _head.setPrev(elt.prev());
        }
        _list.removeListable(elt);
        broadcastListableRemoved(elt);
    }
    protected void checkForComod() throws ConcurrentModificationException {
        if(_modCount != _list._modCount) {
            throw new ConcurrentModificationException();
        }
    }
    protected CursorableLinkedList _list = null;
    protected Listable _pre = null;
    protected Listable _post = null;
}
