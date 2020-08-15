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

public class ProductCalculator extends Calculator {
	Calculator ingredient;
	ExternalVariable ingrOutputVar = null;
	MrpVarRef ingrOutputVarRef;
	List<MrpVarRef> ingrInputVarRefs =null;
	List<MrpVarRef> localVarRefs=null;

	List<MrpVarRef> derivMrpeRefs = null;
	/** Translate number of derivative to reference in ingredient */
	List<Integer> derivTrans;

	public ProductCalculator(Definition def, int nderiv) {
		super(def, nderiv);
		mj.setComponent(new ProductVariableFactory());
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
		ingrOutputVar = new ExternalVariable(this, dependentVariable.getName(), type.getOutputDimensions());
		super.build();
		if (!good)
			return;

		try {
			ingrInputVarRefs = new ArrayList<>();
			List<DefVariable> ingrInputVars = this.definition
					.getVariablesByType(DefType.ingrVar);
			for(DefVariable inputVars:ingrInputVars) {
				XVariable normVar1 = (XVariable) mj.addVariable(inputVars.getName());
				normVar1.setHook(DimensionVisitor.DIM_KEY,Dimensions.SCALER);	
				ingrInputVarRefs.add(mrpe.getVarRef(normVar1));
			}

			localVarRefs = new ArrayList<>();
			List<DefVariable> localVars = 
					definition.getVariablesByType(DefType.localVar);
			for(DefVariable inputVars:localVars) {
				XVariable localVar1 = (XVariable) mj.addVariable(inputVars.getName());
				localVar1.setHook(DimensionVisitor.DIM_KEY,Dimensions.SCALER);	
				localVarRefs.add(mrpe.getVarRef(localVar1));
			}


			derivTrans = new ArrayList<>();
			ingrOutputVarRef = mrpe.getVarRef(ingrOutputVar); // ,type.getDimensions());
			derivMrpeRefs = new ArrayList<>();
			int dnum = 0;
			for (PartialDerivative pd : ingrOutputVar.allDerivatives()) {
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
			for (PartialDerivative diff : ingrOutputVar.allDerivatives()) {
				String dnames[] = diff.getDnames();
				String ingrNames[] = new String[dnames.length];
				/** translate names used here to those used by the ingredient */
				for (int i = 0; i < dnames.length; ++i) {
					int pos = ingrInputVars.indexOf(definition.getVariable(dnames[i]));
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
	
	public boolean goodIngredient() {
		return this.ingredient != null && this.ingredient.isGood();
	}


	private class ProductVariableFactory extends DVariableFactory {
		private static final long serialVersionUID = 350L;

		@Override
		public Variable createVariable(String name, Object value) {
			if (ingrOutputVar != null && name.equals(ingrOutputVar.getName()))
				return ingrOutputVar;
			return super.createVariable(name, value);
		}

		@Override
		public Variable createVariable(String name) {
			if (ingrOutputVar != null && name.equals(ingrOutputVar.getName()))
				return ingrOutputVar;
			return super.createVariable(name);
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
//		sb.append("DefVariable\n");
//		sb.append(dependentVariable);
		sb.append("LocalVariable\n");
		for(MrpVarRef ref:localVarRefs) {
			sb.append(ref.toString());
			sb.append("\n");
		}
		sb.append("ExternalVariable\n");
		sb.append(ingrOutputVar);
		sb.append("\nMVarRef\n");
		sb.append(ingrOutputVarRef);
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

	public Evaluator createEvaluator() {
		List<MrpVarRef> drefs = new ArrayList<>();
		derivMrpeRefs.forEach(ref -> drefs.add(ref.duplicate()));
		List<MrpVarRef> iivr = new ArrayList<>();
		ingrInputVarRefs.forEach(ref -> iivr.add(ref.duplicate()));
		List<MrpVarRef> lvr = new ArrayList<>();
		localVarRefs.forEach(ref -> lvr.add(ref.duplicate()));
		
		
		List<Integer> dt = new ArrayList<Integer>(derivTrans);
		return new ProductEvaluator(
				super.createEvaluator(),
				ingredient.createEvaluator(), ingrOutputVarRef.duplicate(), iivr, drefs, dt,lvr);
	}

}
