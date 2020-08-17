/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.calculators;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.lsmp.djep.djep.DSymbolTable;
import org.lsmp.djep.matrixJep.MatrixJep;
import org.lsmp.djep.matrixJep.MatrixPartialDerivative;
import org.lsmp.djep.matrixJep.MatrixVariableFactory;
import org.lsmp.djep.matrixJep.MatrixVariableI;
import org.lsmp.djep.mrpe.MRpCommandList;
import org.lsmp.djep.mrpe.MRpEval;
import org.lsmp.djep.mrpe.MRpRes;
import org.lsmp.djep.vectorJep.Dimensions;
import org.lsmp.djep.xjep.XVariable;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.Variable;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.jep.ExternalPartialDerivative;
import org.singsurf.singsurf.jep.ExternalVariable;
import org.singsurf.singsurf.jepwrapper.DimensionVisitor;
import org.singsurf.singsurf.jepwrapper.EvaluationException;

/**
 * A calculator which depends on a set of ingredients.
 * @author Richard Morris
 *
 */
public class ChainedCalculator extends Calculator {
	Calculator ingredient;
	DefVariable dependentVariable=null;
	ExternalVariable jepVar=null;
	int jepVarRef;
	List<Integer> normVarRefs =null;

	public ChainedCalculator(Definition def, int nderiv) {
		super(def, nderiv);
		mj = (MatrixJep) mj.newInstance(new DSymbolTable(new ChainedVariableFactory()));
		mj.setAllowAssignment(true);
		mj.setAllowUndeclared(true);
		mj.setImplicitMul(true);
		mj.addComplex();
		mj.addStandardConstants();
		mj.addStandardFunctions();
		mj.addStandardDiffRules();
		mrpe = new MRpEval(mj);
	}

	
	List<Integer> derivMrpeRefs=null;
	@Override
	public void build() {
		DefType type = this.definition.getOpType();
		if(type == null) {
            this.msg = "OpType must be specified it is null";
            this.good = false;
            return;
		}
		List<DefVariable> var = this.definition.getVariablesByType(type);
		if(var.size() != 1) {
			this.msg = "Definition must have exactly one variable of type " + type.toString();
			this.good = false;
			return;
		}
		dependentVariable = var.get(0);
		if(type == DefType.psurf)
		    jepVar = new ExternalVariable(this,dependentVariable.getName(),3);
		else if(type == DefType.asurf)
            jepVar = new ExternalVariable(this,dependentVariable.getName(),1);
		else {
		    this.msg = "OpType must be asurf or psurf its is "+type;
            this.good = false;
            return;
		}
		
		super.build();
		if(!good) return;
		try {
			normVarRefs = new ArrayList<>();
	           List<DefVariable> normalVars = this.definition
	                    .getVariablesByType(DefType.none);
	           for(DefVariable inputVars:normalVars) {
	        	   mj.addVariable(inputVars.getName(),0.0);
		            XVariable normVar1 = (XVariable) mj.getVar(inputVars.getName());
//JEPFIX            normVar1.setHook(DimensionVisitor.DIM_KEY,Dimensions.ONE);	
		            normVarRefs.add(mrpe.getVarRef(normVar1));
	           }

			
			
			
			jepVarRef = mrpe.getVarRef((MatrixVariableI)jepVar);
			derivMrpeRefs = new ArrayList<Integer>();
			int dnum=0;
			for(Enumeration<?> en=jepVar.allDerivatives();en.hasMoreElements();)
			{
				Object o = en.nextElement();
				ExternalPartialDerivative diff = (ExternalPartialDerivative) o;
					int ref = mrpe.getVarRef((MatrixVariableI) diff);
					derivMrpeRefs.add(dnum,ref);
					++dnum;
		}
		} catch (ParseException e) {
			this.good = false;
			this.msg = e.getMessage();
		}
		
		if(ingredient == null) { 
		    this.good = false;
		    return;
		}
	      Enumeration<?> e = jepVar.allDerivatives();
	        derivTrans = new ArrayList<Integer>();
	        int dnum=0;
	        while(e.hasMoreElements()) { /* for each derivative ... */
	            Object o = e.nextElement();
	            MatrixPartialDerivative diff = (MatrixPartialDerivative) o;
	            String dnames[] = diff.getDnames();
	            String ingrNames[] = new String[dnames.length];
	            List<DefVariable> normalVars = this.definition.getVariablesByType(DefType.none);
	            /** translate names used here to those used by the ingredient */
	            for(int i=0;i<dnames.length;++i){
	                int pos = normalVars.indexOf(definition.getVariable(dnames[i]));
	                ingrNames[i] = ingredient.getInputVariableName(pos);
	            }
	            int ref =ingredient.requireDerivative(ingrNames);
	            derivTrans.add(dnum,ref);
	            ++dnum;
	        }
	        
	}

	public Calculator getIngredient() {
		return ingredient;
	}
	/** Translate number of derivative to reference in ingredient */
	List<Integer> derivTrans;
	public void setIngredient(Calculator ingredient) {
		this.ingredient = ingredient;
		build();
	}

	private class ChainedVariableFactory extends MatrixVariableFactory {

		@Override
		public Variable createVariable(String name, Object value) {
			if(dependentVariable!=null && name.equals(dependentVariable.getName()))
				return jepVar;
			else
				return super.createVariable(name, value);
		}

		@Override
		public Variable createVariable(String name) {
			if(dependentVariable!=null && name.equals(dependentVariable.getName()))
				return jepVar;
			else
				return super.createVariable(name);
		}
		
	}
	
	public boolean goodIngredient() {
		return super.isGood() && this.ingredient != null && this.ingredient.isGood();
	}

	@Override
	public Evaluator createEvaluator() {
		List<Integer> drefs = new ArrayList<>();
		derivMrpeRefs.forEach(ref -> drefs.add(ref));
		List<Integer> nvr = new ArrayList<>();
		normVarRefs.forEach(ref -> nvr.add(ref));
		
		
		List<Integer> dt = new ArrayList<Integer>(derivTrans);
		return new ChainedEvaluator(
				super.createEvaluator(),
				ingredient.createEvaluator(), jepVarRef, nvr, drefs, dt);
	}
	
	
	
}
