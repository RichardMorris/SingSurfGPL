package org.singsurf.singsurf.calculators;

import java.util.List;

import org.lsmp.djep.mrpe.MRpCommandList;
import org.lsmp.djep.mrpe.MRpRes;
import org.singsurf.singsurf.jepwrapper.EvaluationException;

public class ChainedEvaluator extends Evaluator {

	Evaluator ingrEvaluator;
	int jepVarRef;
	List<Integer> normVarRefs =null;
	List<Integer> derivMrpeRefs = null;
	/** Translate number of derivative to reference in ingredient */
	List<Integer> derivTrans;

	public ChainedEvaluator(Evaluator supercalc, 
			Evaluator ingrCE, int jvr,List<Integer> nvr,
			List<Integer> dmvr,List<Integer> dt) {
		super(supercalc);
		ingrEvaluator = ingrCE;
		jepVarRef = jvr;
		normVarRefs = nvr;
		derivMrpeRefs = dmvr;
		derivTrans = dt;

	}

    @Override
    public double[] evalTop(double[] in) throws EvaluationException {
        double[] ingrRes = ingrEvaluator.evalTop(in);
        try {
            mrpe.setVarValue(jepVarRef, ingrRes);
            
			for(int i=0; i< normVarRefs.size();++i) {
				if(normVarRefs.get(i)!=null)
					mrpe.setVarValue(normVarRefs.get(i), in[i]);
			}

            for (int i = 0; i < this.derivMrpeRefs.size(); ++i) {
                double[] derivRes = ingrEvaluator.evalDerivative(this.derivTrans
                        .get(i));
                mrpe.setVarValue(this.derivMrpeRefs.get(i), derivRes);
            }

            for (MRpCommandList com : allComs)
                mrpe.evaluate(com);

            MRpRes res = mrpe.evaluate(topCom);
            double v[] = (double[]) res.toArray();
            return v;
        } catch (Exception e) {
            throw new EvaluationException(e);
        }

    }
/* Jep 3.5 code
	@Override
	public double[] evalTop(double[] in) throws EvaluationException {
		double[] ingrRes = ingrEvaluator.evalTop(in);

		try {
			if (ingrRes.length == 1) {
				mrpe.setVarValue(jepVarRef, ingrRes[0]);
			} else {
				mrpe.setVarValue(jepVarRef, ingrRes);
			}
			for(int i=0; i< normVarRefs.size();++i) {
				if(normVarRefs.get(i)!=null)
					mrpe.setVarValue(normVarRefs.get(i), in[i]);
			}
			
			for (int i = 0; i < this.derivMrpeRefs.size(); ++i) {
				double[] derivRes = ingrEvaluator.evalDerivative(this.derivTrans.get(i));
				mrpe.setVarValue(this.derivMrpeRefs.get(i), derivRes);
			}

			for (MrpCommandList com : allComs) {
				mrpe.evaluate(com);
			}

			MrpRes res = mrpe.evaluate(topCom);
			double v[] = resultAsVector(res);
			return v;
		} catch (Exception e) {
			throw new EvaluationException(e);
		}

	}
*/
	
	
}
