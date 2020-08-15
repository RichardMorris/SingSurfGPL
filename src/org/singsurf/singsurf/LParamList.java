/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.singsurf.singsurf.clients.AbstractClient;
import org.singsurf.singsurf.definitions.Parameter;

import jv.object.PsPanel;

/**
 * Holds a list of interactive parameters. To save unnecessary deletion and
 * creation of parameters use
 * 
 * <pre>
 * LParamList lpl = new LParamList(parent);
 * ...
 * lpl.reset();
 * addParameter(a,1);
 * addParameter(b,2);
 * ...
 * lpl.rebuild();
 * </pre>
 * 
 * @author Rich Morris Created on 31-Mar-2005
 */

public class LParamList extends PsPanel implements ItemListener {
	private static final long serialVersionUID = 1L;

	private boolean changed;
	AbstractClient parentClient;
	List<PuParameter> v = new ArrayList<PuParameter>();
	ScrollPane pane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
	PsPanel inner = new PsPanel();
	GridBagConstraints gbc = null;
	Choice chDP;
	
	public LParamList(AbstractClient parent) {
//		System.out.println("LParamList "+parent.getName()+" changed"+changed);
		this.parentClient = parent;
		this.setFont(parent.basicFont);
		this.setLayout(new BorderLayout());
		this.add(pane);
		pane.add(inner);
		inner.setBorderType(PsPanel.BORDER_NONE);
		//inner.setInsetSizeVertical(1);
		GridBagLayout gridbag = new GridBagLayout();
		inner.setLayout(gridbag);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		// gbc.ipadx = 1;
		//gbc.ipady = 1;
		gbc.insets = new Insets(1,1,1,1);
		chDP = new Choice();
		for(int i=0;i<=9;++i) {
			chDP.addItem(""+i);
		}
		chDP.select(1);
		chDP.addItemListener(this);
		this.changed = true;
		rebuild();
	}

	public PuParameter getParameter(Parameter p) {
		for(PuParameter q:v) {
			if(p.getName().equals(q.getName()))
				return q;
		}
		return null;
	}
	
	public PuParameter addParameter(Parameter p) {
		for (int i = 0; i < v.size(); ++i) {
			PuParameter q = v.get(i);
			int res = p.getName().compareTo(q.getName());
			if (res < 0) {
				PuParameter r = new PuParameter(parentClient, p);
				r.setRef(1);
				v.add(i, r);
				changed = true;
				return r;
			}
			if (res == 0) {
				q.setVal(p.getVal());
				q.setRef(1);
				return q;
			}
		}
		PuParameter r = new PuParameter(parentClient, p);
		r.setRef(1);
		v.add(r);
		changed = true;
		return r;
	}

//	public PuParameter addParameter(Parameter p, double val) {
//		PuParameter q = addParameter(p);
//		if (q == null)
//			return null;
//		q.setVal(val);
//		return q;
//	}

	public void reset() {
		for (PuParameter p : v) {
			p.setRef(-1);
		}
		changed = false;
	}

	public void rebuild() {
		Iterator<PuParameter> it = v.iterator();
		while (it.hasNext()) {
			PuParameter p = it.next();
			if (p.getRef() < 0) {
				it.remove();
				changed = true;
			}
		}
//		System.out.println("rebuild "+parentClient.getName()+" changed"+changed);
		if (changed) {
			inner.removeAll();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;

			for (PuParameter p : v) {
				// p.control.addComponents(inner,gbc);
				inner.add(p.getLabel(), gbc);
				gbc.gridx++;
				inner.add(p.getControlPanel(), gbc);
				gbc.gridx = 0;
				gbc.gridy++;
			}
			gbc.gridx = 0;
			inner.add(new Label("DP"),gbc);
			gbc.gridx++;
			inner.add(chDP,gbc);

			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 2;
			gbc.gridheight = 1;
			
			GridBagConstraints gbc2 = (GridBagConstraints) gbc.clone();
			gbc2.fill = GridBagConstraints.BOTH;
			gbc2.weighty = 1;
			inner.add(new Panel(), gbc2); // expands to fill rest of box
//			this.invalidate();
		}
	}

	public void setDP(int dp) {
		for (PuParameter param : v) {
			param.setDP(dp);
		}
		chDP.select(dp);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();
		if( itSel == chDP) {
			int dp = chDP.getSelectedIndex();
			for (PuParameter param : v) {
				param.setDP(dp);
			}
			parentClient.setDP(dp);
		}
	}

}