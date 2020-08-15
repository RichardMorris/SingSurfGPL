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
 * A calculator which depends on two ingredients.
 * 
 * @author Richard Morris
 *
 */
public class RidgeCalculator extends Calculator {
    Calculator ingredient1;
    
    ExternalVariable jepSurfVar = null;
    Variable jepPVar = null;
    Variable jepQVar = null;
    
    MrpVarRef jepSurfVarRef;
    MrpVarRef jepPVarRef;
    MrpVarRef jepQVarRef;

    MrpVarRef jepXVarRef;
    MrpVarRef jepYVarRef;
    MrpVarRef jepZVarRef;

    List<MrpVarRef> xDerivRefs = null;
    List<MrpVarRef> yDerivRefs = null;
    List<MrpVarRef> zDerivRefs = null;

    public RidgeCalculator(Definition def, int nderiv) {
        super(def, nderiv);
        mj.setComponent(new RidgeVariableFactory());
        mj.reinitializeComponents();
    }

    @Override
    public void build() {
        DefType optype = this.definition.getOpType();

        List<DefVariable> var = this.definition.getVariablesByType(optype);
        if (var.size() != 1) {
            this.msg = "Definition must have exactly one variable of type "
                    + optype.toString();
            this.good = false;
            return;
        }
        DefVariable defVariableSurf = var.get(0);
        
        List<DefVariable> fieldvars = this.definition.getVariablesByType(DefType.vfield);
        if (fieldvars.size() != 2) {
            this.msg = "Definition must have exactly two variables of type field";
            this.good = false;
            return;
        }
        DefVariable defVariableP = fieldvars.get(0);
        DefVariable defVariableQ = fieldvars.get(1);

        jepSurfVar = new ExternalVariable(this, defVariableSurf.getName(), optype.getOutputDimensions());
        jepPVar = mj.addVariable(defVariableP.getName());
        jepQVar = mj.addVariable(defVariableQ.getName());
        jepPVar.setHook(DimensionVisitor.DIM_KEY,Dimensions.THREE);
        jepQVar.setHook(DimensionVisitor.DIM_KEY,Dimensions.THREE);
        
        super.build();
        if (!good)
            return;
        try {
            List<DefVariable> normalVars = this.definition.getVariablesByType(DefType.none);

            XVariable normVar1 = (XVariable) mj.addVariable(normalVars.get(0).getName());
            normVar1.setHook(DimensionVisitor.DIM_KEY,Dimensions.SCALER);
            jepXVarRef = mrpe.getVarRef(normVar1);//,Dimensions.ONE);
            XVariable normVar2 = (XVariable) mj.addVariable(normalVars.get(1).getName());
            normVar2.setHook(DimensionVisitor.DIM_KEY,Dimensions.SCALER);
            jepYVarRef = mrpe.getVarRef(normVar2);
            XVariable normVar3 = (XVariable) mj.addVariable(normalVars.get(2).getName());
            normVar3.setHook(DimensionVisitor.DIM_KEY,Dimensions.SCALER);
            jepZVarRef = mrpe.getVarRef(normVar3);
            
            jepSurfVarRef = mrpe.getVarRef(jepSurfVar);
            jepPVarRef = mrpe.getVarRef(jepPVar);
            jepQVarRef = mrpe.getVarRef(jepQVar);
            
            xDerivRefs = new ArrayList<>();
            yDerivRefs = new ArrayList<>();
            zDerivRefs = new ArrayList<>();
            
            int dnum1 = 0;
            for(PartialDerivative pd:jepSurfVar.allDerivatives()) {
                ExternalPartialDerivative diff = (ExternalPartialDerivative) pd;
                MrpVarRef ref = mrpe.getVarRef(diff);
                xDerivRefs.add(dnum1, ref);
                ++dnum1;
            }
            if (ingredient1 != null)
                buildIngr1();
        } catch (ParseException e) {
            this.good = false;
            this.msg = e.getMessage();
        }

    }

    public Calculator getIngredient1() {
        return ingredient1;
    }

    /** Translate number of derivative to reference in ingredient */
    List<Integer> derivTrans1;

    public void buildIngr1() throws ParseException {
        derivTrans1 = new ArrayList<>();
        int dnum = 0;
        for(PartialDerivative pd:jepSurfVar.allDerivatives()) {
            ExternalPartialDerivative diff = (ExternalPartialDerivative) pd;
            String dnames[] = diff.getDnames();
            String ingrNames[] = new String[dnames.length];
            List<DefVariable> normalVars = this.definition
                    .getVariablesByType(DefType.none);
            /** translate names used here to those used by the ingredient */
            for (int i = 0; i < dnames.length; ++i) {
                int pos = normalVars.indexOf(definition.getVariable(dnames[i]));
                ingrNames[i] = ingredient1.getInputVariableName(pos);
            }
            int ref = ingredient1.requireDerivative(ingrNames);
            derivTrans1.add(dnum, ref);
            ++dnum;
        }
    }

    public void setIngredient1(Calculator ingredient) {
        this.ingredient1 = ingredient;
        reset();
        build();
    }

    class RidgeVariableFactory extends DVariableFactory {
        private static final long serialVersionUID = 350L;

        @Override
        public Variable createVariable(String name, Object value) {
            if (jepSurfVar != null
                    && name.equals(jepSurfVar.getName()))
                return jepSurfVar;
//            if (jepPVar != null
//                    && name.equals(jepPVar.getName()))
//                return jepPVar;
//            if (jepQVar != null
//                    && name.equals(jepQVar.getName()))
//                return jepQVar;
            
			return super.createVariable(name, value);
        }

        @Override
        public Variable createVariable(String name) {
            if (jepSurfVar != null
                    && name.equals(jepSurfVar.getName()))
                return jepSurfVar;
//            if (defVariableSurf != null
//                    && name.equals(defVariableSurf.getName()))
//                return jepSurfVar;
//            if (defVariableP != null
//                    && name.equals(defVariableP.getName()))
//                return jepDirVar1;
			return super.createVariable(name);
        }

    }

    public boolean goodIngredients() {
        boolean g0 = super.isGood();
        boolean g1 = this.ingredient1 == null ? false : this.ingredient1
                .isGood();
//        System.out.println("BiCh " + g0 + " " + g1 + " " + g2);
        return g0 && g1;
    }
        
	public Evaluator createEvaluator() {
		List<MrpVarRef> drefs1 = new ArrayList<>();
		xDerivRefs.forEach(ref -> drefs1.add(ref.duplicate()));
		List<Integer> dt1 = new ArrayList<Integer>(derivTrans1);

		List<MrpVarRef> drefs2 = new ArrayList<>();
		yDerivRefs.forEach(ref -> drefs2.add(ref.duplicate()));
//		List<Integer> dt2 = new ArrayList<Integer>(derivTrans2);

		return new RidgeEvaluator(
				super.createEvaluator(),
				ingredient1.createEvaluator(), 
				jepSurfVarRef.duplicate(), 
				jepPVarRef.duplicate(), 
				jepQVarRef.duplicate(), 
				jepXVarRef.duplicate(),
				jepYVarRef.duplicate(),
				jepZVarRef.duplicate(),
				drefs1, dt1);	
	}

    
}
