package org.singsurf.singsurf.calculators;

import java.util.List;

import com.singularsys.extensions.fastmatrix.MrpCommandList;
import com.singularsys.extensions.fastmatrix.MrpRes;
import com.singularsys.extensions.fastmatrix.MrpVarRef;
import org.singsurf.singsurf.jepwrapper.EvaluationException;

public class RidgeEvaluator extends Evaluator {
	

	Evaluator ingredient1;
	private MrpVarRef jepXVarRef;
	private MrpVarRef jepYVarRef;
	private MrpVarRef jepZVarRef;
	private MrpVarRef jepSurfVarRef;
	private MrpVarRef jepPVarRef;
	private MrpVarRef jepQVarRef;
	
    List<MrpVarRef> derivMrpeRefs = null;
    /** Translate number of derivative to reference in ingredient */
    List<Integer> derivTrans;

	
	public RidgeEvaluator(Evaluator supercalc,Evaluator ingr1,
			MrpVarRef jepVarRef1, MrpVarRef jepVarRef2, MrpVarRef jepVarRef3, 
			MrpVarRef jepNormVarRef1, MrpVarRef jepNormVarRef2, MrpVarRef jepNormVarRef3,
			List<MrpVarRef> derivMrpeRefs1, List<Integer> derivTrans1) { 
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

            for (MrpCommandList com : allComs)
                mrpe.evaluate(com);

            MrpRes res = mrpe.evaluate(topCom);
            double v[] = resultAsVector(res);
            return v;

        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(e);
        }

    }

}
