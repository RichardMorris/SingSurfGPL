/*
Created 5 Feb 2007 - Richard Morris
*/
package org.singsurf.singsurf;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public class RepeatButton extends Button {
	private static final long serialVersionUID = 1L;

	static int period = 500;
	static int delay = 100;
	
	TimerTask tt = null;
	static Timer tim = new Timer();
	/**
	 * @throws HeadlessException
	 */
	public RepeatButton() throws HeadlessException {
		super();
		init();
	}
	/**
	 * @param arg0
	 * @throws HeadlessException
	 */
	public RepeatButton(String arg0) throws HeadlessException {
		super(arg0);
		init();
	}

	private void init() {
		this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		tt = new TimerTask() {
			@Override
			public void run() {
				//System.out.println("run");
				RepeatButton.this.processActionEvent(
						new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""));
			}
		};
		
	}


	@Override
	protected void processEvent(AWTEvent e) {
		if(e.getID() == MouseEvent.MOUSE_PRESSED) {
			//System.out.println("mouse pressed"); 
			this.processActionEvent(
					new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""));
			if(tt!=null)
				tt.cancel();
			tt = new TimerTask() {
				@Override
				public void run() {
					//System.out.println("run");
					RepeatButton.this.processActionEvent(
							new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""));
					//Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
					//		new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"")
					//		);
				}};
			tim.schedule(tt,period,delay);
		}
		else if(e.getID() == MouseEvent.MOUSE_RELEASED) {
			//System.out.println("mouse released"); 
			tt.cancel();
		}
//		else if(e.getID() == MouseEvent.MOUSE_EXITED) {
//			//System.out.println("mouse released"); 
//			tt.cancel();
//		}
	}

}
