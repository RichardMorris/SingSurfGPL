/*
Created 17 Sep 2006 - Richard Morris
 */
package org.singsurf.singsurf.calculators;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.lsmp.djep.djep.DSymbolTable;
import org.lsmp.djep.djep.PartialDerivative;
import org.lsmp.djep.matrixJep.MatrixJep;
import org.lsmp.djep.matrixJep.MatrixVariable;
import org.lsmp.djep.matrixJep.MatrixVariableI;
import org.lsmp.djep.vectorJep.Dimensions;
import org.lsmp.djep.xjep.XVariable;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.Variable;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.jep.ExternalPartialDerivative;
import org.singsurf.singsurf.jep.ExternalVariable;



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
    
    int jepSurfVarRef;
    int jepPVarRef;
    int jepQVarRef;

    int jepXVarRef;
    int jepYVarRef;
    int jepZVarRef;

    List<Integer> xDerivRefs = null;
    List<Integer> yDerivRefs = null;
    List<Integer> zDerivRefs = null;

	private SpecialVariableFactory varFac;

    public RidgeCalculator(Definition def, int nderiv) {
        super(def, nderiv);
		varFac = new SpecialVariableFactory();
		mj = (MatrixJep) mj.newInstance(new DSymbolTable(varFac));

//JepFix        mj.setComponent(new RidgeVariableFactory());
//        mj.reinitializeComponents();
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
        jepPVar = addVariable(defVariableP.getName(),Dimensions.THREE);
        jepQVar = addVariable(defVariableQ.getName(),Dimensions.THREE);
        varFac.clear();
        varFac.add(jepSurfVar);
        varFac.add(jepPVar);
        varFac.add(jepQVar);
        
        super.build();
        if (!good)
            return;
        try {
            List<DefVariable> normalVars = this.definition.getVariablesByType(DefType.none);

            XVariable normVar1 = addVariable(normalVars.get(0).getName(),Dimensions.ONE);
            jepXVarRef = mrpe.getVarRef(normVar1);//,Dimensions.ONE);
            XVariable normVar2 = addVariable(normalVars.get(1).getName(),Dimensions.ONE);
            jepYVarRef = mrpe.getVarRef(normVar2);
            XVariable normVar3 = addVariable(normalVars.get(2).getName(),Dimensions.ONE);
            jepZVarRef = mrpe.getVarRef(normVar3);
            
            jepSurfVarRef = mrpe.getVarRef((MatrixVariableI) jepSurfVar);
            jepPVarRef = mrpe.getVarRef((MatrixVariableI) jepPVar);
            jepQVarRef = mrpe.getVarRef((MatrixVariableI) jepQVar);
            
            xDerivRefs = new ArrayList<>();
            yDerivRefs = new ArrayList<>();
            zDerivRefs = new ArrayList<>();
            
            int dnum1 = 0;
            final Enumeration<?> allDerivatives = jepSurfVar.allDerivatives();
			while(allDerivatives.hasMoreElements()) {
                ExternalPartialDerivative diff = (ExternalPartialDerivative) allDerivatives.nextElement();
                int ref = mrpe.getVarRef((MatrixVariableI) diff);
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

        final Enumeration<?> allDerivatives = jepSurfVar.allDerivatives();
		while(allDerivatives.hasMoreElements()) {
            ExternalPartialDerivative diff = (ExternalPartialDerivative) allDerivatives.nextElement();
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

    public boolean goodIngredients() {
        boolean g0 = super.isGood();
        boolean g1 = this.ingredient1 == null ? false : this.ingredient1
                .isGood();
//        System.out.println("BiCh " + g0 + " " + g1 + " " + g2);
        return g0 && g1;
    }
        
	public Evaluator createEvaluator() {
		List<Integer> drefs1 = new ArrayList<>();
		xDerivRefs.forEach(ref -> drefs1.add(ref));
		List<Integer> dt1 = new ArrayList<Integer>(derivTrans1);

		List<Integer> drefs2 = new ArrayList<>();
		yDerivRefs.forEach(ref -> drefs2.add(ref));
//		List<Integer> dt2 = new ArrayList<Integer>(derivTrans2);

		return new RidgeEvaluator(
				super.createEvaluator(),
				ingredient1.createEvaluator(), 
				jepSurfVarRef, 
				jepPVarRef, 
				jepQVarRef, 
				jepXVarRef,
				jepYVarRef,
				jepZVarRef,
				drefs1, dt1);	
	}

    
}
