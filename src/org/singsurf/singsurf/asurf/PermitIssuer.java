 package org.singsurf.singsurf.asurf;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;

public class PermitIssuer<T> {

	final BiPredicate<T,T> predicate;
	final Collection<T> current ; 
	Lock lock = new ReentrantLock();
	Condition hold = lock.newCondition();
	
	
	public PermitIssuer(BiPredicate<T, T> predicate,int size) {
		super();
		this.predicate = predicate;
		this.current = new ArrayBlockingQueue<T>(size);
	}

	boolean safeToIssuePermit(final T requester) {
		boolean safe = current.stream().allMatch(t -> predicate.test(t,requester));
		return safe;
	}
	
	public void aquire(final T requester) {
		while(true) {
			if(safeToIssuePermit(requester)) {
				current.add(requester);
				return;
			}
			lock.lock();
			try {
				hold.awaitUninterruptibly();
			} finally {
				lock.unlock();
			}
		}
	}
	
	public void release(final T requester) {
		current.remove(requester);
		lock.lock();
		try {
			hold.signalAll();
		} finally { 
			lock.unlock();
		}
	}
}
