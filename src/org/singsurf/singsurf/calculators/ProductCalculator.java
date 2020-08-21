package org.singsurf.singsurf.calculators;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.lsmp.djep.djep.DSymbolTable;
import org.lsmp.djep.djep.PartialDerivative;
import org.lsmp.djep.matrixJep.MatrixJep;
import org.lsmp.djep.matrixJep.MatrixVariableI;
import org.lsmp.djep.vectorJep.Dimensions;
import org.lsmp.djep.xjep.XVariable;
import org.nfunk.jep.ParseException;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.jep.ExternalPartialDerivative;
import org.singsurf.singsurf.jep.ExternalVariable;

public class ProductCalculator extends Calculator {
	Calculator ingredient;
	ExternalVariable ingrOutputVar = null;
	int ingrOutputVarRef;
	List<Integer> ingrInputVarRefs =null;
	List<Integer> localVarRefs=null;

	List<Integer> derivMrpeRefs = null;
	/** Translate number of derivative to reference in ingredient */
	List<Integer> derivTrans;
	private SpecialVariableFactory varFac;

	public ProductCalculator(Definition def, int nderiv) {
		super(def, nderiv);
		varFac = new SpecialVariableFactory();
		mj = (MatrixJep) mj.newInstance(new DSymbolTable(varFac));
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
		varFac.set(ingrOutputVar);
		super.build();
		if (!good)
			return;

		try {
			ingrInputVarRefs = new ArrayList<>();
			List<DefVariable> ingrInputVars = this.definition
					.getVariablesByType(DefType.ingrVar);
			for(DefVariable inputVars:ingrInputVars) {
				XVariable normVar1 = (XVariable) mj.addVariable(inputVars.getName());
//				normVar1.setHook(DimensionVisitor.DIM_KEY,Dimensions.ONE);	
				ingrInputVarRefs.add(mrpe.getVarRef(normVar1));
			}

			localVarRefs = new ArrayList<>();
			List<DefVariable> localVars = 
					definition.getVariablesByType(DefType.localVar);
			for(DefVariable inputVars:localVars) {
				XVariable localVar1 = (XVariable) mj.addVariable(inputVars.getName());
//				localVar1.setHook(DimensionVisitor.DIM_KEY,Dimensions.SCALER);	
				localVarRefs.add(mrpe.getVarRef(localVar1));
			}


			derivTrans = new ArrayList<>();
			ingrOutputVarRef = mrpe.getVarRef((MatrixVariableI)ingrOutputVar); // ,type.getDimensions());
			derivMrpeRefs = new ArrayList<>();
			int dnum = 0;
			
			 Enumeration allDerivatives = ingrOutputVar.allDerivatives();
			while (allDerivatives.hasMoreElements()) {
				ExternalPartialDerivative diff = (ExternalPartialDerivative) allDerivatives.nextElement();
				int ref = mrpe.getVarRef((MatrixVariableI)diff);
				derivMrpeRefs.add(dnum, ref);
				++dnum;
			}

			if (ingredient == null) {
				this.msg = "Ingredient is null";
				this.good = false;
				return;
			}
			dnum = 0;
			allDerivatives = ingrOutputVar.allDerivatives();
			while (allDerivatives.hasMoreElements()) {
				PartialDerivative diff = (PartialDerivative) allDerivatives.nextElement();
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

/*
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
*/
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
//		sb.append("DefVariable\n");
//		sb.append(dependentVariable);
		sb.append("LocalVariable\n");
		for(Integer ref:localVarRefs) {
			sb.append(ref.toString());
			sb.append("\n");
		}
		sb.append("ExternalVariable\n");
		sb.append(ingrOutputVar);
		sb.append("\nMVarRef\n");
		sb.append(ingrOutputVarRef);
		sb.append("\nderivMrpeRefs\n");
		for(Integer ref:derivMrpeRefs) {
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
		List<Integer> drefs = new ArrayList<>();
		derivMrpeRefs.forEach(ref -> drefs.add(ref));
		List<Integer> iivr = new ArrayList<>();
		ingrInputVarRefs.forEach(ref -> iivr.add(ref));
		List<Integer> lvr = new ArrayList<>();
		localVarRefs.forEach(ref -> lvr.add(ref));
		
		
		List<Integer> dt = new ArrayList<Integer>(derivTrans);
		return new ProductEvaluator(
				super.createEvaluator(),
				ingredient.createEvaluator(), ingrOutputVarRef, iivr, drefs, dt,lvr);
	}

}
