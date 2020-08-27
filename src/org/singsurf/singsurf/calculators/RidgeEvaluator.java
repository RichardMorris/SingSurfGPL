package org.singsurf.singsurf.calculators;

import java.util.List;

import org.lsmp.djep.mrpe.MRpCommandList;
import org.lsmp.djep.mrpe.MRpRes;
import org.singsurf.singsurf.jepwrapper.EvaluationException;

public class RidgeEvaluator extends Evaluator {
	

	Evaluator ingredient1;
	private Integer jepXVarRef;
	private Integer jepYVarRef;
	private Integer jepZVarRef;
	private Integer jepSurfVarRef;
	private Integer jepPVarRef;
	private Integer jepQVarRef;
	
    List<Integer> derivMrpeRefs = null;
    /** Translate number of derivative to reference in ingredient */
    List<Integer> derivTrans;

	
	public RidgeEvaluator(Evaluator supercalc,Evaluator ingr1,
			Integer jepVarRef1, Integer jepVarRef2, Integer jepVarRef3, 
			Integer jepNormVarRef1, Integer jepNormVarRef2, Integer jepNormVarRef3,
			List<Integer> derivMrpeRefs1, List<Integer> derivTrans1) { 
		super(supercalc);
		
		this.ingredient1 = ingr1;
		
		this.jepSurfVarRef = jepVarRef1;
		this.jepPVarRef = jepVarRef2;
		this.jepQVarRef = jepVarRef3;
		
		this.jepXVarRef = jepNormVarRef1;
		this.jepYVarRef = jepNormVarRef2;
		this.jepZVarRef = jepNormVarRef3;
		this.derivMrpeRefs = derivMrpeRefs1;
		this.derivTrans = derivTrans1;
	}

    @Override
    public double[] evalTop(double[] in) throws EvaluationException {
//        double igr1in[] = new double[1];
//        double igr2in[] = new double[2];

        try {
//            igr1in[0] = in[0];
//            igr2in[0] = in[1];
            if (jepXVarRef != null)
                mrpe.setVarValue(jepXVarRef, in[0]);
            if (jepYVarRef != null)
                mrpe.setVarValue(jepYVarRef, in[1]);
            if (jepZVarRef != null)
                mrpe.setVarValue(jepZVarRef, in[2]);
            
            double[] ingrRes1 = ingredient1.evalTop(in);
            mrpe.setVarValue(jepSurfVarRef, ingrRes1);
            for (int i = 0; i < this.derivMrpeRefs.size(); ++i) {
                double[] derivRes = ingredient1.evalDerivative(this.derivTrans
                        .get(i));
                mrpe.setVarValue(this.derivMrpeRefs.get(i), derivRes);
            }

            double[] pVal = new double[] { in[3],in[4],in[5] };
            double[] qVal = new double[] { in[6],in[7],in[8] };
            
            mrpe.setVarValue(jepPVarRef, pVal);
            mrpe.setVarValue(jepQVarRef, qVal);
//            double[] ingrRes2 = ingredient2.evalTop(igr2in);
//            mrpe.setVarValue(jepPVarRef, ingrRes2);
//            for (int i = 0; i < this.derivMrpeRefs2.size(); ++i) {
//                double[] derivRes = ingredient2.evalDerivative(this.derivTrans2
//                        .get(i));
//                mrpe.setVarValue(this.derivMrpeRefs2.get(i), derivRes);
//            }

            for (MRpCommandList com : allComs)
                mrpe.evaluate(com);

            MRpRes res = mrpe.evaluate(topCom);
            double v[] = resultAsVector(res);
            return v;

        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(e);
        }

    }

}
