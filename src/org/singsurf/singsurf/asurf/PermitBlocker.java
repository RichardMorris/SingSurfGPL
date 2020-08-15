package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PermitBlocker {

	static class Position {
		int x,y,z;

		public Position(int x, int y, int z) {
			super();
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			result = prime * result + z;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			Position other = (Position) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			if (z != other.z)
				return false;
			return true;
		}
		
	}

	ConcurrentHashMap<Position,List<Condition>> map = new ConcurrentHashMap<>();
	private final List<Condition> empty;
	
	public PermitBlocker() {
		super();
		empty = Collections.emptyList();
	}

	class Pair {
		Position pos;
		Condition cond;
		public Pair(Position pos, Condition cond) {
			super();
			this.pos = pos;
			this.cond = cond;
		}
		
	}
	public List<Pair> aquire(Box_info box) {
		List<Pair> issued = new ArrayList<>();
		Position key = new Position(box.xl,box.yl,box.zl);
		List<Condition> list = map.get(key);
		if(list!=null) {
			for(Condition cond:list) {
				try {
					cond.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		synchronized(this) {
		// clear to proceed
			Lock lock = new ReentrantLock();
			for(int i=-1;i<=1;++i) {
				for(int j=-1;j<=1;++j) {
					for(int k=-1;k<=1;++k) {
						Position pos = new Position(box.xl+i,box.yl+j,box.zl+k);
						final Condition cond = lock.newCondition();
						issued.add(new Pair(pos,cond));
						map.computeIfAbsent(pos, p -> new ArrayList<Condition>() ); 
						map.computeIfPresent(pos, (p,v) -> {v.add(cond); return v; });
					}
				}
			}
			lock.lock();
		}
		return issued;
	}
		
	public void release(final List<Pair> issued) {
		for(Pair pair:issued) {
			pair.cond.signalAll();
			map.compute(pair.pos, (k,v) -> {v.remove(pair.cond); return v;});
			map.remove(pair.pos,empty);
		}
	}
}
