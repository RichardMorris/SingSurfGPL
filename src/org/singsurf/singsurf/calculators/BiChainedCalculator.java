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
public class BiChainedCalculator extends Calculator {
    Calculator ingredient1;
    Calculator ingredient2;
    DefVariable dependentVariable1 = null;
    DefVariable dependentVariable2 = null;
    ExternalVariable jepVar1 = null;
    ExternalVariable jepVar2 = null;
    MrpVarRef jepVarRef1;
    MrpVarRef jepVarRef2;

    public BiChainedCalculator(Definition def, int nderiv) {
        super(def, nderiv);
        mj.setComponent(new BiChainedVariableFactory());
        mj.reinitializeComponents();
    }

    List<MrpVarRef> derivMrpeRefs1 = null;
    List<MrpVarRef> derivMrpeRefs2 = null;
    MrpVarRef jepNormVarRef1;
    MrpVarRef jepNormVarRef2;

    @Override
    public void build() {
        DefType optype = this.definition.getOpType();

        List<DefVariable> var = this.definition.getVariablesByType(optype);
        if (var.size() != 2) {
            this.msg = "Definition must have exactly two variable of type "
                    + optype.toString();
            this.good = false;
            return;
        }
        dependentVariable1 = var.get(0);
        dependentVariable2 = var.get(1);
        jepVar1 = new ExternalVariable(this, dependentVariable1.getName(), optype.getOutputDimensions());
        jepVar2 = new ExternalVariable(this, dependentVariable2.getName(), optype.getOutputDimensions());
        super.build();
        if (!good)
            return;
        try {
            List<DefVariable> normalVars = this.definition
                    .getVariablesByType(DefType.none);

            XVariable normVar1 = (XVariable) mj.addVariable(normalVars.get(0).getName());
            normVar1.setHook(DimensionVisitor.DIM_KEY,Dimensions.SCALER);
            jepNormVarRef1 = mrpe.getVarRef(normVar1);//,Dimensions.ONE);
            XVariable normVar2 = (XVariable) mj.addVariable(normalVars.get(1).getName());
            normVar2.setHook(DimensionVisitor.DIM_KEY,Dimensions.SCALER);
                jepNormVarRef2 = mrpe.getVarRef(normVar2);
            jepVarRef1 = mrpe.getVarRef(jepVar1);
            jepVarRef2 = mrpe.getVarRef(jepVar2);
            derivMrpeRefs1 = new ArrayList<>();
            derivMrpeRefs2 = new ArrayList<>();
            
            int dnum1 = 0;
            for(PartialDerivative pd:jepVar1.allDerivatives()) {
                ExternalPartialDerivative diff = (ExternalPartialDerivative) pd;
                MrpVarRef ref = mrpe.getVarRef(diff);
                derivMrpeRefs1.add(dnum1, ref);
                ++dnum1;
            }
            int dnum2 = 0;
            for(PartialDerivative pd:jepVar2.allDerivatives()) {
                ExternalPartialDerivative diff = (ExternalPartialDerivative) pd;
                MrpVarRef ref = mrpe.getVarRef(diff);
                derivMrpeRefs2.add(dnum2, ref);
                ++dnum2;
            }
            if (ingredient1 != null)
                buildIngr1();
            if (ingredient2 != null)
                buildIngr2();
        } catch (ParseException e) {
            this.good = false;
            this.msg = e.getMessage();
        }

    }

    public Calculator getIngredient1() {
        return ingredient1;
    }

    public Calculator getIngredient2() {
        return ingredient2;
    }

    /** Translate number of derivative to reference in ingredient */
    List<Integer> derivTrans1;
    List<Integer> derivTrans2;

    public void buildIngr1() throws ParseException {
        derivTrans1 = new ArrayList<>();
        int dnum = 0;
        for(PartialDerivative pd:jepVar1.allDerivatives()) {
            ExternalPartialDerivative diff = (ExternalPartialDerivative) pd;
            String dnames[] = diff.getDnames();
            String ingrNames[] = new String[dnames.length];
            List<DefVariable> normalVars = this.definition
                    .getVariablesByType(DefType.none);
            /** translate names used here to those used by the ingredient */
            for (int i = 0; i < dnames.length; ++i) {
                int pos = normalVars.indexOf(definition.getVariable(dnames[i]));
                if (pos != 0) {
                    System.out.println("Bad deriv varialbe");
                    good = false;
                }
                ingrNames[i] = ingredient1.getInputVariableName(0);
            }
            int ref = ingredient1.requireDerivative(ingrNames);
            derivTrans1.add(dnum, ref);
            ++dnum;
        }
    }

    public void buildIngr2() throws ParseException {

        derivTrans2 = new ArrayList<>();
        int dnum = 0;
        for(PartialDerivative pd:jepVar2.allDerivatives()) {
            ExternalPartialDerivative diff = (ExternalPartialDerivative) pd;
            String dnames[] = diff.getDnames();
            String ingrNames[] = new String[dnames.length];
            List<DefVariable> normalVars = this.definition
                    .getVariablesByType(DefType.none);
            /** translate names used here to those used by the ingredient */
            for (int i = 0; i < dnames.length; ++i) {
                int pos = normalVars.indexOf(definition.getVariable(dnames[i]));
                if (pos != 1) {
                    System.out.println("Bad deriv varialbe");
                    good = false;
                }
                ingrNames[i] = ingredient2.getInputVariableName(0);
            }
            int ref = ingredient2.requireDerivative(ingrNames);
            derivTrans2.add(dnum, ref);
            ++dnum;
        }
    }

    public void setIngredient1(Calculator ingredient) {
        this.ingredient1 = ingredient;
        reset();
        build();
    }

    public void setIngredient2(Calculator ingredient) {
        this.ingredient2 = ingredient;
        reset();
        build();
    }

    class BiChainedVariableFactory extends DVariableFactory {
        private static final long serialVersionUID = 350L;

        @Override
        public Variable createVariable(String name, Object value) {
            if (dependentVariable1 != null
                    && name.equals(dependentVariable1.getName()))
                return jepVar1;
            if (dependentVariable2 != null
                    && name.equals(dependentVariable2.getName()))
                return jepVar2;
			return super.createVariable(name, value);
        }

        @Override
        public Variable createVariable(String name) {
            if (dependentVariable1 != null
                    && name.equals(dependentVariable1.getName()))
                return jepVar1;
            if (dependentVariable2 != null
                    && name.equals(dependentVariable2.getName()))
                return jepVar2;
			return super.createVariable(name);
        }

    }



    public boolean goodIngredients() {
        boolean g0 = super.isGood();
        boolean g1 = this.ingredient1 == null ? false : this.ingredient1
                .isGood();
        boolean g2 = this.ingredient2 == null ? false : this.ingredient2
                .isGood();
//        System.out.println("BiCh " + g0 + " " + g1 + " " + g2);
        return g0 && g1 && g2;
    }
        
	public Evaluator createEvaluator() {
		List<MrpVarRef> drefs1 = new ArrayList<>();
		derivMrpeRefs1.forEach(ref -> drefs1.add(ref.duplicate()));
		List<Integer> dt1 = new ArrayList<Integer>(derivTrans1);

		List<MrpVarRef> drefs2 = new ArrayList<>();
		derivMrpeRefs2.forEach(ref -> drefs2.add(ref.duplicate()));
		List<Integer> dt2 = new ArrayList<Integer>(derivTrans2);

		return new BiChainedEvaluator(
				super.createEvaluator(),
				ingredient1.createEvaluator(), 
				ingredient2.createEvaluator(),
				jepVarRef1.duplicate(), jepNormVarRef1.duplicate(),
				drefs1, dt1,
				jepVarRef2.duplicate(), jepNormVarRef2.duplicate(),
				drefs2, dt2);
	}

    
}
