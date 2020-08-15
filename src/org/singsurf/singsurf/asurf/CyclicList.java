package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.Iterator;

public class CyclicList<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;

	int curPos = 0;
	public void start() {
		curPos = 0;
		
	}

	int cyclicIndex(int pos) {
		pos = pos%size();
		pos = pos>=0 ? pos : pos+size();
		return pos;
	}
		
	public T getCyclic(int index) {
		return get(cyclicIndex(index));
	}
	
	T nextCyclic(T cur) {
		int pos = this.indexOf(cur);
		return getCyclic(pos+1);
	}
	
	T prevCyclic(T cur) {
		int pos = this.indexOf(cur);
		return getCyclic(pos-1);
	}
	
	public enum Direction {
		Forward, Backward, Separate,NoFound,Identical
	}
	
	public Direction adjacent(T first,T second) {
		int indA = indexOf(first);
		int indB = indexOf(second);
		if(indA<0 || indB<0)
			return Direction.NoFound;
		if(indA==indB)
			return Direction.Identical;
		
		if(indA+1==indB)
			return Direction.Forward;
		if(indA==indB+1)
			return Direction.Backward;

		if(indA==size()-1 && indB==0)
			return Direction.Forward;
		if(indA==0 && indB==size()-1)
			return Direction.Backward;
		
		return Direction.Separate;
	}
	
	public forwardIterator forwardIteratorFrom(T start) {
		return new forwardIterator(start);
	}

	public forwardIterator forwardIteratorFromTo(T start,T end) {
		return new forwardIterator(start,end);
	}

	public backwardIterator backwardIteratorFrom(T start) {
		return new backwardIterator(start);
	}

	public backwardIterator backwardIteratorFromTo(T start,T end) {
		return new backwardIterator(start,end);
	}

	class  forwardIterator implements Iterator<T> {
		int startPos;
		int endPos;
		int curpos= Integer.MIN_VALUE;
		forwardIterator(T start) {
			startPos = indexOf(start);
			endPos = startPos;
		}
		
		/**
		 * All the elements between the two
		 * @param start (inclusive)
		 * @param end (exclusive)
		 */
		forwardIterator(T start,T end) {
			startPos = indexOf(start);
			endPos = indexOf(end);
		}

		@Override
		public boolean hasNext() {
			if(startPos == -1) return false;
			if(curpos == Integer.MIN_VALUE) {
				return size()>0;
			}
			return cyclicIndex(curpos+1) != endPos;
		}

		@Override
		public T next() {
			if(curpos == Integer.MIN_VALUE) {
				curpos = startPos;
			} else {
				curpos = cyclicIndex(curpos+1);
			}
			return get(curpos);
		}
	}

	class  backwardIterator implements Iterator<T> {
		int startPos;
		int curpos;
		int endPos;
		backwardIterator(T start) {
			startPos = indexOf(start);
			curpos = Integer.MAX_VALUE;
			endPos = startPos;
		}

		backwardIterator(T start,T end) {
			startPos = indexOf(start);
			curpos = Integer.MAX_VALUE;
			endPos = indexOf(end);
		}

		@Override
		public boolean hasNext() {
			if(curpos == Integer.MAX_VALUE) {
				return size()>0;
			}
			return cyclicIndex(curpos-1) != endPos;
		}

		@Override
		public T next() {
			if(curpos == Integer.MAX_VALUE) {
				curpos = startPos;
			} else {
				curpos = cyclicIndex(curpos-1);
			}
			return get(curpos);
		}
	}

	public CyclicList<T> subListCyclic(int start, int end) {
		CyclicList<T> list = new CyclicList<T>();
		for(int i=start;i<end;++i) {
			list.add(this.getCyclic(i));
		}
		return list;
	}

}
