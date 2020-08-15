package org.singsurf.singsurf.asurf;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.singsurf.singsurf.acurve.AsurfException;

public class ThreadedKnitter extends Knitter {

	ConcurrentLinkedDeque<Box_info> inQueue = new ConcurrentLinkedDeque<Box_info>();
	Thread receiverThread;

	/**
	 * Whether the gen box phase has finished.
	 */
	private AtomicBoolean genBoxes_done = new AtomicBoolean(false);

	/** boxes left in queue */
	private AtomicInteger inBoxes = new AtomicInteger(0);

	public ThreadedKnitter(BoxClevA boxclev) {
		super(boxclev);
	}

	
	@Override
	public void queuebox(Box_info box) throws AsurfException {
		if(box.lfd != null) {
			queuebox(box.lfd);
			queuebox(box.lfu);
			queuebox(box.lbd);
			queuebox(box.lbu);
			queuebox(box.rfd);
			queuebox(box.rfu);
			queuebox(box.rbd);
			queuebox(box.rbu);
			return;
		}
		submit(box);
	}

	/**
	 * @param box
	 */
	private void submit(Box_info box) {
		inBoxes.incrementAndGet();
		inQueue.offer(box);
	}

	@Override
	public void fini() {
		System.out.println(getProgress());
		genBoxes_done.set(true);

		try {
			receiverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(getProgress());
	}


	class Receiver implements Runnable {
		BoxClevA boxclev;
		public Receiver(BoxClevA boxclev) {
			this.boxclev = boxclev;
		}

		@Override
		public void run() {

			while (true) {
				synchronized(this) {
					while (inQueue.isEmpty() && ! genBoxes_done.get()) {
						try {
							wait(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				Box_info box = inQueue.pollFirst();
				if(box!=null) {
					try {
						inBoxes.decrementAndGet();
						queuebox_leaf(box);
					} catch (AsurfException e) {
						e.printStackTrace();
					}
				}
				if( genBoxes_done.get() && inQueue.isEmpty() ) {
					break;
				}
			}
			System.out.println("Receiver done");
		}
	}


	@Override
	public void init(BoxClevA boxclev2) {
		super.init(boxclev2);

		receiverThread = new Thread(new Receiver(boxclev2),"Knitter");
		receiverThread.start();

	}

	public String getProgress() {
		return "Knitter done "+knitter_out+" holding "+(knitter_in.get()-knitter_out.get())+" queued "+inBoxes.get();
	}

}
