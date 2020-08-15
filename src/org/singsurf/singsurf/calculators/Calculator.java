package org.singsurf.singsurf.calculators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Parameter;
import org.singsurf.singsurf.jep.CDiv;
import org.singsurf.singsurf.jep.CExp;
import org.singsurf.singsurf.jep.CIm;
import org.singsurf.singsurf.jep.CMul;
import org.singsurf.singsurf.jep.CRe;
import org.singsurf.singsurf.jep.ExternalPartialDerivative;
import org.singsurf.singsurf.jep.ExternalVariable;
import org.singsurf.singsurf.jep.Pad3;

import com.singularsys.extensions.djep.DJep;
import com.singularsys.extensions.djep.DPrintVisitor;
import com.singularsys.extensions.djep.DVariableTable;
import com.singularsys.extensions.fastmatrix.MrpCommandList;
import com.singularsys.extensions.fastmatrix.MrpEval;
import com.singularsys.extensions.fastmatrix.MrpVarRef;
import com.singularsys.extensions.matrix.DimensionVisitor;
import com.singularsys.extensions.matrix.Dimensions;
import com.singularsys.extensions.matrix.MatrixFactoryI;
import com.singularsys.extensions.matrix.MatrixFunctionTable;
import com.singularsys.extensions.matrix.MatrixOperatorTable;
import com.singularsys.extensions.matrix.doublemat.DoubleMatrixFactory;
import com.singularsys.extensions.matrix.doublemat.DoubleMatrixField;
import com.singularsys.extensions.matrixdiff.DDimensionVisitor;
import com.singularsys.extensions.matrixdiff.MDJep;
import com.singularsys.extensions.matrixdiff.MatrixDifferentiationVisitor;
import com.singularsys.extensions.xjep.XAssign;
import com.singularsys.extensions.xjep.XVariable;

import org.singsurf.singsurf.jepwrapper.EvaluationException;
import com.singularsys.jep.JepException;
import com.singularsys.jep.NodeFactory;
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.PrintVisitor;
import com.singularsys.jep.Variable;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.parser.Node;

public class Calculator {
	/** The MatrixJep instance */
	protected DJep mj = null;
	/** The mrpe instance */
	MrpEval mrpe = null;

	DimensionVisitor dimV = null;
	/** The definition of the mapping */
	Definition definition;

	/** The top node of the equation set */
	Node top = null;

	/** The command list for the top eqn */
	MrpCommandList topCom;
	/** The command list for derivatives */
	List<MrpCommandList> derivComs = new ArrayList<>();
	/** The command list for subsequent equations */
	List<MrpCommandList> allComs = new ArrayList<>();

	/** whether first or second derivative are required */
	public int derivDepth = 0;

	/** The raw list of equations */
	List<Node> rawEqns;
	/** The raw list of equations */
	List<Node> preprocessedEqns;
	/** All variables found in equations */
	List<XVariable> depVars;

	/**
	 * mrpe reference numbers for each DefVariable, indexed by posn in definition
	 */
	MrpVarRef variableRefs[];

	/** Jep variable, indexed by posn in definition */
	protected Variable[] jepVars = null;

	/** mrpe reference numbers for each parameter */
	Map<String, MrpVarRef> paramRefs = new HashMap<String, MrpVarRef>();

	/** Index for derivatives */
	Map<StringList, Integer> derivativesIndex = new HashMap<>();

	/** Simple class to ensure the derivIndex map finds matching arrays
	 * 
	 */
	static class StringList {
		String[] eles;

		public StringList(String[] eles) {
			super();
			this.eles = eles;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(eles);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StringList other = (StringList) obj;
			if (!Arrays.equals(eles, other.eles))
				return false;
			return true;
		}



	}
	/** Equations for derivatives */
	List<Node> derivivativeEquations = new ArrayList<>();

	/** The dimension the input required */
	int inputDim = 0;

	/** Implicit means top equation can be of form x^2+y^2=r^2 */
	boolean isImplicit = false;

	/** Can the calculator be used? */
	boolean good = false;
	/** Error message on error */
	protected String msg = null;
	private DoubleMatrixField mfield;

	@SuppressWarnings("unused")
	private Calculator() {
	}

	public Calculator(Definition def, int nderiv) {
		definition = def;
		System.out.println("nderiv " + nderiv);
		MatrixFactoryI mfact = new DoubleMatrixFactory();
		mfield = new DoubleMatrixField(mfact);

		StandardConfigurableParser cp = new StandardConfigurableParser();
		MatrixFunctionTable mfun = new MatrixFunctionTable(mfact, getField());
		MatrixOperatorTable ot = new MatrixOperatorTable(mfact, getField());
		ot.getAssign().setPFMC(new XAssign());
		ot.getSubtract().addAltSymbol("\u2212");
		ot.getSubtract().addAltSymbol("\u2013");
		
		MatrixDifferentiationVisitor diffV = new MatrixDifferentiationVisitor(mfact);

		mj = new MDJep(cp, mfun, mfield, ot, diffV, mfact);
		mj.getPrintVisitor().setMode(DPrintVisitor.PRINT_PARTIAL_EQNS, false);
		mj.addStandardConstants();
		mj.addStandardDiffRules();
		mrpe = new MrpEval(mj, mfact);
		dimV = new DDimensionVisitor(mj);
		// simpV = new SimplificationVisitor();
		variableRefs = new MrpVarRef[def.getNumVars()];
		jepVars = new Variable[def.getNumVars()];
		derivDepth = nderiv;
		CMul cmul = new CMul(mj, mfact);
		CDiv cdiv = new CDiv(mj, mfact, cmul);
		CRe cre = new CRe(mj, mfact);
		CIm cim = new CIm(mj, mfact);
		CExp cexp = new CExp(mj, mfact,cmul);
		Pad3 pad3 = new Pad3(mj, mfact);
		mj.addFunction("cmul", cmul);
		mj.addFunction("cdiv", cdiv);
		mj.addFunction("cexp", cexp);
		mj.addFunction("re", cre);
		mj.addFunction("im", cim);
		mj.addFunction("pad3", pad3);
		mj.reinitializeComponents();
		System.out.println(mj.getVariableTable().toString());
		// if(nderiv==1)
		// firstDerivs = new MatrixNodeI[def.getNumVars()];
	}

	/**
	 * Builds all the necessary components from a definition.
	 */
	public void build() {

		DefType type = this.definition.getType();
		this.inputDim = type.getInputDims().getFirstDim();
		this.isImplicit = type.getOutputDimensions().is0D();
//		if (type == DefType.acurve || type == DefType.asurf || type == DefType.intersect || type == DefType.clip
//				|| type == DefType.genInt || type == DefType.biInt) {
//			this.isImplicit = true;
//		} else {
//			this.isImplicit = false;
//		}

		try {
			parseDef();
			if (this.rawEqns.size() == 0)
				return;
			buildKeyEqns();
			buildDepVars();
			buildCommands();
			buildDerivatives();    
			good = true;
		} catch (JepException e) {
			msg = e.getMessage();
		}
		// printEquationsAndVariables();
	}

	/**
	 * Parse the definition producing a list of equations
	 * 
	 * @throws JepException
	 */
	void parseDef() throws JepException {
		mj.initMultiParse(definition.getEquation());
		// mj.getPrintVisitor().setMode(DPrintVisitor.PRINT_PARTIAL_EQNS,false);

		List<Node> v1 = new ArrayList<>();
		Node n;
		while ((n = mj.continueParsing()) != null)
			v1.add(n);
		rawEqns = new ArrayList<>();
		preprocessedEqns = new ArrayList<>();
		for (int i = v1.size() - 1; i >= 0; --i) {
			Node n1 = v1.get(i);
			// System.out.print("Node i: ");
			if (i == 0 && isImplicit) {
				OperatorTableI ot = mj.getOperatorTable();
				NodeFactory nf = this.mj.getNodeFactory();
				if (n1.getOperator() == ot.getAssign()) {
					Node rep = nf.buildOperatorNode(ot.getSubtract(), n1.jjtGetChild(0), n1.jjtGetChild(1));
					n1 = rep;
				}
			}
			preprocessedEqns.add(n1);
			Node processed2 = mj.preprocess(n1);
			Node simp2 = mj.clean(processed2);
			Dimensions d = dimV.visit(simp2);
			rawEqns.add(simp2);
			System.out.print(""+d.toString()+":\t");
			mj.println(n1);
		}
	}

	/**
	 * Finds the top equation and equations for first derivatives if required.
	 */
	void buildKeyEqns() {
		top = rawEqns.get(rawEqns.size() - 1);
	}

	void printEquationsAndVariables() {
		if (!good) {
			System.out.println("Calculator is corrupt: " + msg);
			return;
		}
		System.out.print("Top\t");
		mj.println(top);
		PrintVisitor pv = mj.getPrintVisitor();
		pv.setMode(DPrintVisitor.PRINT_PARTIAL_EQNS, false);
		for (StringList names : derivativesIndex.keySet()) {
			int index = this.derivativesIndex.get(names);
			for (String name:names.eles) {
				System.out.print("D" + name);
			}
			System.out.print("\t");
			mj.println(this.derivivativeEquations.get(index));
		}
		DVariableTable st = (DVariableTable) mj.getVariableTable();
		st.print(pv);

		pv.setMode(DPrintVisitor.PRINT_PARTIAL_EQNS, true);
	}

	/**
	 * Finds the dependency table for variable.
	 * 
	 * @throws ParseException
	 */
	void buildDepVars() throws ParseException {
		List<XVariable> set = new ArrayList<>();
		depVars = mj.recursiveGetVarsInEquation(top, set);
	}

	void extendDepVars(Node eqn) throws ParseException {
		Set<XVariable> oldVars = new HashSet<>(depVars);
		depVars = mj.recursiveGetVarsInEquation(eqn, depVars);
		for (XVariable var : depVars) {
			if (oldVars.contains(var)) {
				continue;
			}
			if (var.hasEquation()) {
				Dimensions dim = dimV.visit(var.getEquation());
				var.setHook(DimensionVisitor.DIM_KEY, dim);
				MrpCommandList com = mrpe.compile(var, var.getEquation());
				allComs.add(com);
			}
		}
	}

	/**
	 * Finds the MRpCommandLists, assigns references to definitions variables and
	 * parameters.
	 * 
	 * @throws ParseException
	 */
	void buildCommands() throws ParseException {
		this.allComs.clear();
		this.paramRefs.clear();

		for (XVariable var : depVars) {
			DefVariable defVariable = definition.getVariable(var.getName());
			if (defVariable != null) {
				int index = definition.getVariableIndex(defVariable);
				variableRefs[index] = mrpe.getVarRef(var);
				jepVars[index] = var;
			} else if (var.isConstant()) {
			} else if (var.hasEquation()) {
				Dimensions varDim = dimV.visit(var.getEquation());
				var.setHook(DimensionVisitor.DIM_KEY, varDim);
				MrpCommandList com = mrpe.compile(var, var.getEquation());
				allComs.add(com);
			} else if (var instanceof ExternalPartialDerivative || var instanceof ExternalVariable) {
				// These are handles but the subclass
			} else {
				// Not a variable, does not have an equation so must be a
				// parameter
				// Check if this parameter already exists
				Parameter p = definition.getParameter(var.getName());
				if (p == null)
					p = definition.addParameter(var.getName());
				var.setValue(Double.valueOf(p.getVal()));
				MrpVarRef ref = mrpe.getVarRef(var);
				paramRefs.put(p.getName(), ref);
			}
		}

		dimV.visit(top);
		topCom = mrpe.compile(top);

		definition.setParamNames(paramRefs.keySet().toArray());
	}


	void buildDerivatives() throws ParseException {
		this.derivativesIndex.clear();
		this.derivComs.clear();
		this.derivivativeEquations.clear();

		switch(derivDepth) {
		case 0:
			break;
		case 1:
			for (int i = 0; i < getNumNormalInputVariables(); ++i)
				requireDerivative(new String[] { getNormalInputVariableName(i) });
			break;
		case 2:
			for (int i = 0; i < getNumNormalInputVariables(); ++i)
				requireDerivative(new String[] { getNormalInputVariableName(i) });
			for (int i = 0; i < getNumNormalInputVariables(); ++i)
				for (int j = i; j < getNumNormalInputVariables(); ++j)
					requireDerivative(new String[] { 
							getNormalInputVariableName(i), 
							getNormalInputVariableName(j) });
			break;
		default:
			throw new ParseException("Only first and second derivatives supported");
		}

	}


	/** Sets the name of the definition */
	public void setName(String name) {
		definition.setName(name);
	}

	/** Sets the main text of the definition */
	public void setEquation(String s) {
		definition.setEquation(s);
		reset();
		build();
	}

	protected void reset() {
		good = false;
		msg = null;
		VariableTable vt = mj.getVariableTable();
		vt.removeNonConstants();
	}

	/** Sets the value of an input variable */
	public void setVarValue(int n, double val) {
		if (isGood())
			try {
				mrpe.setVarValue(variableRefs[n], val);
			} catch (EvaluationException e) {
				System.out.println(e);
			}
	}

	public void setVarBounds(int n, double min, double max, int steps) {
		definition.getVar(n).setBounds(min, max, steps);
	}

	public void setParamValue(String name, double val) {
		if (isGood())
			try {
				MrpVarRef ref = paramRefs.get(name);
				if(ref!=null) 
					mrpe.setVarValue(ref, val);
			} catch (EvaluationException e) {
				System.out.println(e);
			}
		definition.setParameterValue(name, val);
	}

	public DefVariable getDefVariable(int i) {
		return definition.getVar(i).duplicate();
	}

	public int getNumInputVariables() {
		return definition.getNumVars();
	}

	public String getInputVariableName(int i) {
		return definition.getVar(i).getName();
	}

	public int getNumNormalInputVariables() {
		List<DefVariable> normalVars = this.definition.getVariablesByType(DefType.none);
		return normalVars.size();
	}

	public String getNormalInputVariableName(int i) {
		List<DefVariable> normalVars = this.definition.getVariablesByType(DefType.none);
		return normalVars.get(i).getName();
	}

	public int getNParam() {
		return definition.getNumParams();
	}

	public Parameter getParam(int i) {
		return definition.getParam(i); //.duplicate();
	}

	public List<Parameter> getParams() {
		return definition.getParams();
	}

	public boolean isGood() {
		return good;
	}

	public String getMsg() {
		return msg;
	}

	public Definition getDefinition() {
		return definition;
	}

	/**
	 * Requires that the calculator can evaluate the given derivative. If necessary
	 * will calculate the required derivative.
	 * 
	 * @param names
	 * @return the index number for the derivative
	 * @throws ParseException
	 */
	public int requireDerivative(String[] names) throws ParseException {
		Integer index = getDerivative(names);
		return index;
	}

	int getDerivative(String[] names) throws ParseException {
		Integer index = derivativesIndex.get(new StringList(names));
		if (index != null)
			return index;

		Node node;
		if (names.length == 1) {
			//System.out.println("pre diff " + mj.toString(top));
			node = mj.differentiate(top, names[0]);
		} else {
			String[] subNames = new String[names.length - 1];
			System.arraycopy(names, 0, subNames, 0, names.length - 1);
			int lowerIndex = getDerivative(subNames);
			Node lower = derivivativeEquations.get(lowerIndex);
			//	    System.out.println("pre diff " + mj.toString(lower));

			node = mj.differentiate(lower, names[names.length - 1]);
		}
		Node simp = mj.clean(node);
		dimV.visit(simp);
		derivivativeEquations.add(simp);
		int i = derivivativeEquations.size() - 1;
		derivativesIndex.put(new StringList(names), i);
		extendDepVars(simp);
		MrpCommandList com = mrpe.compile(simp);
		derivComs.add(i, com);
		return i;
	}

	public List<Node> getRawEqns() {
		return rawEqns;
	}

	public List<Node> getPreprocessedEqns() {
		return preprocessedEqns;
	}

	public DJep getJep() {
		return mj;
	}

	public void setGood(boolean b) {
		this.good = b;
	}

	public DoubleMatrixField getField() {
		return mfield;
	}

	public Number getSimpleAssignment(String str) {
		Operator assignOp = mj.getOperatorTable().getAssign();
		for(Node n:rawEqns) {
			if(assignOp.equals(n.getOperator())) {
				if(str.equals(n.jjtGetChild(0).getName())) {
					Object val = n.jjtGetChild(1).getValue();
					if(val instanceof Number)
						return (Number) val;
					else return null;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("JepVars\n");
		for(Variable var:jepVars) {
			sb.append(var);
			sb.append("\n");
		}
		sb.append("Mrpe var ref\n");
		if(variableRefs==null)
			sb.append("null\n");
		else {
			for(MrpVarRef ref:variableRefs) {
				sb.append(ref);
				sb.append("\n");
			}
		}
		sb.append("Param Refs\n");
		for(Entry<String, MrpVarRef> ref:paramRefs.entrySet()) {
			sb.append(ref.getKey()+": "+ref.getValue()+"\n");
		}

		sb.append("Intermediate variables\n");
		for(XVariable var:depVars) {
			sb.append(var);
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * Create evaluator to use in separate threads
	 * @return
	 */
	public Evaluator createEvaluator() {

		MrpVarRef[] vr = new MrpVarRef[variableRefs.length];
		for(int i=0;i<variableRefs.length;++i) {
			vr[i] = variableRefs[i] ==null 
					? null
							: variableRefs[i].duplicate();
		}

		List<MrpCommandList> derivs = new ArrayList<MrpCommandList>(derivComs);

		List<MrpCommandList> allcom = new ArrayList<MrpCommandList>(allComs);

		Evaluator ce = new  Evaluator(inputDim,
				(MrpEval) mrpe.getLightWeightInstance(), 
				vr, 
				topCom,
				derivs,
				allcom);
		return ce;
	}

	public void setDerivDepth(int dorder) {
		if(dorder != derivDepth) {
			derivDepth = dorder;
			good = false;
		}
	}

}
