package org.singsurf.singsurf.definitions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.nfunk.jep.ParseException;

import jv.object.PsDebug;

/** Contains static methods to parse an XML file containg a set of LsmPDefs. */

public class DefinitionReader {

	static String getAttribute(String s, String att) {
		String search = att + "=\"";
		int nameIndex = s.indexOf(search);
		if (nameIndex == -1) {
			// PsDebug.warning("Didn't find atribute "+att+" ("+search+") in string "+s);
			return null;
		} else {
			int quoteIndex = s.indexOf('\"', nameIndex + att.length() + 2);
			if (quoteIndex == -1)
				return null;
			// System.out.println(s+" Att " +att+" nameIndex "+nameIndex+" quoteIndex
			// "+quoteIndex);
			return s.substring(nameIndex + att.length() + 2, quoteIndex);
		}
	}

	public DefinitionReader(String filename) throws IOException, MalformedURLException, SecurityException {
		if (filename.startsWith("http:") || filename.startsWith("file:")) {
			URL url = new URL(filename);
			InputStream in = url.openStream();
			br = new BufferedReader(new InputStreamReader(in));
		} else {
			FileInputStream f = new FileInputStream(filename);
			br = new BufferedReader(new InputStreamReader(f,Charset.forName("UTF-8")));
		}
	}

	public DefinitionReader(URL url) throws IOException, MalformedURLException, SecurityException {
		InputStream in = url.openStream();
		br = new BufferedReader(new InputStreamReader(in));
	}

	public static Definition findDefByName(Definition[] defs, String name) {
		for (int i = 0; i < defs.length; ++i)
			if (defs[i].getName().equals(name))
				return defs[i];
		return null;
	}

	BufferedReader br = null;

	public DefinitionReader(BufferedReader in) {
		br = in;
	}

	List<Definition> defs = new ArrayList<Definition>();
	List<ProjectComponents> projComp = new ArrayList<>();
	ProjectComponents curProjComp;
	private TreeNode root;
	
	public static class TreeNode {
		private List<TreeNode> children = new ArrayList<>();
		List<Definition> defs = new ArrayList<>();
		TreeNode parent;
		String name;
		public TreeNode(TreeNode parent, String name) {
			super();
			this.parent = parent;
			this.name = name;
		}
		public List<TreeNode> getChildren() {
			return children;
		}
		public void setChildren(List<TreeNode> children) {
			this.children = children;
		}
		public String getName() {
			return name;
		}
		public List<Definition> getDefs() {
			return defs;
		}
		
		
	}
	public List<ProjectComponents> getProjComp() {
		return projComp;
	}

	public void read() {
		root = new TreeNode(null,"All");
		TreeNode current=getRoot();
		String line;
		StringBuffer buf = null;
		int state = 0;
		String lname = null;
		String ltype = null;
		String lopType = null;
		List<DefVariable> vars = null;
		List<Parameter> params = null;
		List<Option> opts = null;
		curProjComp = null;
		projComp.clear();
		String inputName = "";
		try {
			while ((line = br.readLine()) != null) {
				String trim = line.trim();
				try {
					switch (state) {
					case 0: /* not in a definition */
						if (trim.startsWith("<definition ")) {
							lname = getAttribute(trim, "name");
							ltype = getAttribute(trim, "type");
							lopType = getAttribute(trim, "opType");
							buf = new StringBuffer();
							vars = new ArrayList<DefVariable>();
							params = new ArrayList<Parameter>();
							opts = new ArrayList<Option>();
							state = 1;
						} else if (trim.startsWith("<projectComponents")) {
							lname = getAttribute(trim, "name");
							curProjComp = new ProjectComponents(lname);
							state = 2;
						} else if (trim.startsWith("<group")) {
							lname = getAttribute(trim, "name");
							TreeNode node = new TreeNode(current,lname);
							current.getChildren().add(node);
							current = node;
							state = 0;
						} else if (trim.startsWith("</group")) {
							current = current.parent;
							state = 0;
						} else if (trim.startsWith("<definitions>")) {
						} else if (trim.startsWith("</definitions>")) {
						} else if (trim.startsWith("<dependancies>")) {
						} else if (trim.startsWith("</dependancies>")) {
						} else if (trim.startsWith("<")) {
							PsDebug.error("LsmpDefs.readDef bad tag '" + trim + "'");
						}
						break;
					case 1: /* Inside a definition */
						if ("</definition>".equals(trim)) {
							Definition def = new Definition(lname, ltype, buf.toString(), lopType, vars, params, opts);
							defs.add(def);
							current.defs.add(def);
							state = 0;
						} else if (trim.startsWith("<variable")) {
							vars.add(DefVariable.parseTag(trim));
						} else if (trim.startsWith("<parameter")) {
							params.add(Parameter.parseTag(trim));
						} else if (trim.startsWith("<option")) {
							opts.add(new Option(trim));
						} else if (trim.startsWith("<")) {
							PsDebug.error("LsmpDefs.readDef bad tag '" + trim + "'");
						} else // not a tag must be normal line
							buf.append(line + "\n");
						break;
					case 2:
						if (trim.startsWith("</projectComponents>")) {
							projComp.add(curProjComp);
							curProjComp = null;
							state = 0;
						} else if (trim.startsWith("<input")) {
							inputName = getAttribute(trim, "name");
							curProjComp.addInput(inputName);
							
							if(trim.endsWith("/>")) {
								
							} else if(trim.endsWith(">")) {
								state = 3;
							} else {
								System.out.println("Bad input tag " + trim);
							}
						} else if (trim.startsWith("<ingredient")) {
							String ingrName = getAttribute(trim, "name");
							curProjComp.addIngredient(ingrName);
						}
						break;
					case 3:
						if (trim.startsWith("</input>")) {
							state = 2;
						} else if (trim.startsWith("<inputOpt")) {
							String optName = getAttribute(trim, "name");
							String optValue = getAttribute(trim, "value");
							curProjComp.addInputOption(inputName, optName, optValue);
						}
						
						break;
					}
				} catch (ParseException e) {
					PsDebug.error("LsmpDefs.readDef line '" + trim + "' " + e.getMessage());
					state = 0;
				}
			}
		} catch (IOException e) {
			PsDebug.error("LsmpDefs.readDef " + e.getMessage());
		}
	}

	/**
	 * Creates an LsmpDef for a string in XML format.
	 * 
	 * @return and LsmpDef if string contains exactly 1 definition null or on error
	 *         otherwise.
	 */
	public static Definition createLsmpDef(String def) {
		List<Definition> alldefs = null;
		try {
			StringReader sr = new StringReader(def);
			BufferedReader br = new BufferedReader(sr);
			DefinitionReader ldr = new DefinitionReader(br);
			ldr.read();
			alldefs = ldr.getDefs();
			br.close();
			sr.close();
		} catch (IOException e) {
			return null;
		}
		if (alldefs == null || alldefs.size() != 1)
			return null;
		else
			return alldefs.get(0);
	}

	public List<Definition> getDefs() {
		return defs;
	}

	public TreeNode getRoot() {
		return root;
	}

} // end if class
