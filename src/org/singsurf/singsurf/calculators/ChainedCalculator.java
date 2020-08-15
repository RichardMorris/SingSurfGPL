/*
Created 17 Sep 2006 - Richard Morris
 */
package org.singsurf.singsurf.calculators;

import java.util.ArrayList;
import java.util.List;

import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.jep.ExternalPartialDerivative;
import org.singsurf.singsurf.jep.ExternalVariable;

import com.singularsys.extensions.djep.DVariableFactory;
import com.singularsys.extensions.djep.PartialDerivative;
import com.singularsys.extensions.fastmatrix.MrpVarRef;
import com.singularsys.extensions.matrix.DimensionVisitor;
import com.singularsys.extensions.matrix.Dimensions;
import com.singularsys.extensions.xjep.XVariable;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.Variable;

/**
 * A calculator which depends on a set of ingredients.
 * 
 * @author Richard Morris
 *
 */
public class ChainedCalculator extends Calculator {
	Calculator ingredient;
//	DefVariable dependentVariable = null;
	ExternalVariable jepVar = null;
	MrpVarRef jepVarRef;
	List<MrpVarRef> derivMrpeRefs = null;
	/** Translate number of derivative to reference in ingredient */
	List<Integer> derivTrans;
	List<MrpVarRef> normVarRefs =null;

	public ChainedCalculator(Definition def, int nderiv) {
		super(def, nderiv);
		mj.setComponent(new ChainedVariableFactory());
		mj.reinitializeComponents();
	}


	@Override
	public void build() {
		DefType type = this.definition.getOpType();
		if (type == null) {
			this.msg = "OpType must be specified it is null";
			this.good = false;
			return;
		}
		List<DefVariable> var = this.definition.getVariablesByType(type);
		if (var.size() != 1) {
			this.msg = "Definition must have exactly one variable of type " + type.toString();
			this.good = false;
			return;
		}
		DefVariable dependentVariable = var.get(0);
		jepVar = new ExternalVariable(this, dependentVariable.getName(), type.getOutputDimensions());
		super.build();
		if (!good)
			return;
		try {
			normVarRefs = new ArrayList<>();
	           List<DefVariable> normalVars = this.definition
	                    .getVariablesByType(DefType.none);
	           for(DefVariable inputVars:normalVars) {
		            XVariable normVar1 = (XVariable) mj.addVariable(inputVars.getName());
		            normVar1.setHook(DimensionVisitor.DIM_KEY,Dimensions.SCALER);	
		            normVarRefs.add(mrpe.getVarRef(normVar1));
	           }
			
			derivTrans = new ArrayList<>();
			jepVarRef = mrpe.getVarRef(jepVar); // ,type.getDimensions());
			derivMrpeRefs = new ArrayList<>();
			int dnum = 0;
			for (PartialDerivative pd : jepVar.allDerivatives()) {
				ExternalPartialDerivative diff = (ExternalPartialDerivative) pd;
				MrpVarRef ref = mrpe.getVarRef(diff);
				derivMrpeRefs.add(dnum, ref);
				++dnum;
			}

			if (ingredient == null) {
				this.msg = "Ingredient is null";
				this.good = false;
				return;
			}
			dnum = 0;
			for (PartialDerivative diff : jepVar.allDerivatives()) {
				String dnames[] = diff.getDnames();
				String ingrNames[] = new String[dnames.length];
				/** translate names used here to those used by the ingredient */
				for (int i = 0; i < dnames.length; ++i) {
					int pos = normalVars.indexOf(definition.getVariable(dnames[i]));
					ingrNames[i] = ingredient.getInputVariableName(pos);
				}
				int ref = ingredient.requireDerivative(ingrNames);
				derivTrans.add(dnum, ref);
				++dnum;
			}

		} catch (ParseException e) {
			this.good = false;
			this.msg = e.getMessage();
		} catch (Exception e) {
			this.good = false;
			this.msg = e.getMessage();
		}

	}

	public Calculator getIngredient() {
		return ingredient;
	}

	public void setIngredient(Calculator ingredient) {
		this.ingredient = ingredient;
		reset();
		build();
	}

	private class ChainedVariableFactory extends DVariableFactory {
		private static final long serialVersionUID = 350L;

		@Override
		public Variable createVariable(String name, Object value) {
			if (jepVar != null && name.equals(jepVar.getName()))
				return jepVar;
			return super.createVariable(name, value);
		}

		@Override
		public Variable createVariable(String name) {
			if (jepVar != null && name.equals(jepVar.getName()))
				return jepVar;
			return super.createVariable(name);
		}

	}

	public Evaluator createEvaluator() {
		List<MrpVarRef> drefs = new ArrayList<>();
		derivMrpeRefs.forEach(ref -> drefs.add(ref.duplicate()));
		List<MrpVarRef> nvr = new ArrayList<>();
		normVarRefs.forEach(ref -> nvr.add(ref.duplicate()));
		
		
		List<Integer> dt = new ArrayList<Integer>(derivTrans);
		return new ChainedEvaluator(
				super.createEvaluator(),
				ingredient.createEvaluator(), jepVarRef.duplicate(), nvr, drefs, dt);
	}
	

	public boolean goodIngredient() {
		return this.ingredient != null && this.ingredient.isGood();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
//		sb.append("DefVariable\n");
//		sb.append(dependentVariable);
		sb.append("ExternalVariable\n");
		sb.append(jepVar);
		sb.append("\nMVarRef\n");
		sb.append(jepVarRef);
		sb.append("\nderivMrpeRefs\n");
		for(MrpVarRef ref:derivMrpeRefs) {
			sb.append(ref.toString());
			sb.append("\n");
		}
		sb.append("DerivTrans\n");
		if(derivTrans==null) {
			sb.append("null");
		} else {
			for(int i=0;i<derivTrans.size();++i) {
				sb.append(""+i+": "+derivTrans.get(i));
			}
		}
		sb.append("\n\nIngridient\n");
		sb.append(this.ingredient);
		return sb.toString();
	}
}
