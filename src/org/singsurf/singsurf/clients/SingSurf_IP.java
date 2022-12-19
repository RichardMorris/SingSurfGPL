package org.singsurf.singsurf.clients;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;

import javax.swing.JScrollPane;

import org.singsurf.singsurf.PuVariable;

import jv.object.PsPanel;
import jv.object.PsUpdateIf;
import jv.objectGui.PsTabPanel;
import jv.project.PjProject_IP;

public abstract class SingSurf_IP extends PjProject_IP {
	private static final long serialVersionUID = 1L;
	/** Reference to main PjPsurfJepNew class */
	AbstractProject project;
	boolean has_steps;
	private PsPanel northPanel;

	public SingSurf_IP(boolean has_steps) {
		super();
		this.has_steps = has_steps;
	}

	public void superSetParent(PsUpdateIf par) {
		super.setParent(par);
		project = (AbstractProject) par;
		project.m_IP = this;
		setFont(project.basicFont);
		getTitle().setFont(project.basicFont.deriveFont(Font.BOLD));
		setLayout(new BorderLayout());		
	}
	
	@Override
	public void setParent(PsUpdateIf par) {
		// System.out.println("IP setParent");
		super.setParent(par);
		project = (AbstractProject) par;
		project.m_IP = this;
		setFont(project.basicFont);
		getTitle().setFont(project.basicFont.deriveFont(Font.BOLD));
		setLayout(new BorderLayout());
		
		PsPanel p1, p2, p3, p4;
		p1 = getDefinitionPanel();
		p2 = getDomainPanel();
		p3 = getParamPanel();
		p4 = getOptionPanel();

		PsTabPanel tabPanel = new PsTabPanel();
		tabPanel.setFont(project.basicFont);
		add(tabPanel); // add tabbed panel like any other panel
		if(p1 != null) {
			p1.setFont(project.basicFont);
			tabPanel.addPanel("Definition", p1);
		}
		if (p2 != null) {
			p2.setFont(project.basicFont);
			tabPanel.addPanel("Domain", p2);
		}
		if (p3 != null) {
			p3.setFont(project.basicFont);
			tabPanel.addPanel("Parameters", p3);
		}
		if( project instanceof AbstractOperatorProject) {
			final PsPanel ip = getInputsPanel();
			ip.setFont(project.basicFont);
			tabPanel.addPanel("Inputs", ip);
		}
		
		if (p4 != null) {
			p4.setFont(project.basicFont);
			tabPanel.addPanel("Options", p4);
		}
		tabPanel.setVisible("Definition"); // select initially active panel

		add(tabPanel, BorderLayout.CENTER);

		PsPanel p5 = getSouthPanel();
		p5.setFont(project.basicFont);
		add(p5, BorderLayout.SOUTH);

		PsPanel p6 = getNorthPanel();
		p6.setFont(project.basicFont);
		add(p6,BorderLayout.NORTH);

	}

	protected PsPanel getNorthPanel() {
		northPanel = new PsPanel();
		northPanel.addTitle(project.getName());
		return northPanel;
	}
	
	
	@Override
	public void setTitle(String title) {
		northPanel.setTitle(title);
	}

	protected PsPanel getSouthPanel() {
		PsPanel southPanel = new PsPanel(new GridLayout(0, 1));
		southPanel.add(project.m_go);
		return southPanel;
	}

	protected abstract PsPanel getOptionPanel();

	protected PsPanel getDomainPanel() {
		if (project.displayVars == null)
			return null;
		if (project.displayVars.length == 0)
			return null;

		PsPanel p2a = new PsPanel();
		GridBagLayout gridbag = new GridBagLayout();
		p2a.setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(1, 1, 1, 1);

		for (PuVariable var : project.displayVars) {
			c.weightx=0.3;
			p2a.add(var.getMinLabel(), c);
			c.gridx++;
			c.weightx=1;
			p2a.add(var.getMinPanel(), c);
			c.gridy++;
			c.weightx=0.3;
			c.gridx = 0;
			p2a.add(var.getMaxLabel(), c);
			c.gridx++;
			p2a.add(var.getMaxPanel(), c);

			c.gridy++;
			c.gridx = 0;
		}

		if (has_steps) {
			c.gridx = 0;
			p2a.add(new Label(""),c);
			c.gridy++;
			for (PuVariable var : project.displayVars) {
				c.gridx = 0;
				p2a.add(var.getStepsLabel(),c);
				c.gridx++;
				p2a.add(var.getStepsPanel(), c);
				c.gridy++;
			}
		}
		c.gridx = 0;
		p2a.add(new Label(""),c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		p2a.add(new Label("Decimal Places"), c);
		c.gridx++;
		p2a.add(project.chDP, c);
		c.gridx = 0;
		c.gridy++;
		p2a.add(new Label("Scale"), c);
		c.gridx++;
		p2a.add(project.chScale, c);

		GridBagConstraints gbc2 = (GridBagConstraints) c.clone();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weighty = 1;
		p2a.add(new Panel(), gbc2); // expands to fill rest of box

		return p2a;
	}

	protected PsPanel getDefinitionPanel() {
		PsPanel p1 = new PsPanel();
		p1.setLayout(new BorderLayout());
		JScrollPane scroll = new JScrollPane(project.taDef);
		p1.add(scroll, BorderLayout.CENTER);

		if (project.chDefs != null) {
			p1.addSubTitle("Pre-defined surfaces:");
			p1.add(project.chDefs);
		}
		return p1;
	}

	protected PsPanel getParamPanel() {
		return project.newParams;
	}
	
	protected PsPanel getInputsPanel() {
		PsPanel p1 = new PsPanel();
		Component comp = ((AbstractOperatorProject) project).activeInputNames;
		p1.add(comp);
		p1.add(new Label("With selected:"));
		p1.add(project.cbShowFace);
		p1.add(project.cbShowEdge);
		p1.add(project.cbShowVert);
		p1.add(project.cbShowCurves);
		p1.add(project.cbShowPoints);
		p1.add(project.cbShowBoundary);

		p1.addLabelComponent("surf colours: ",project.chSurfColours);
		p1.addLabelComponent("curves colours: ",project.chCurveColours);

		
		p1.add(((AbstractOperatorProject) project).removeInputButton);
		p1.add(((AbstractOperatorProject) project).removeInputGeomButton);
		p1.add(((AbstractOperatorProject) project).removeInputDepButton);
		
		return p1;
	}
}