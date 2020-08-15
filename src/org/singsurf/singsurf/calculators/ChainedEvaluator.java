package org.singsurf.singsurf.calculators;

import java.util.List;

import com.singularsys.extensions.fastmatrix.MrpCommandList;
import com.singularsys.extensions.fastmatrix.MrpRes;
import com.singularsys.extensions.fastmatrix.MrpVarRef;
import org.singsurf.singsurf.jepwrapper.EvaluationException;

public class ChainedEvaluator extends Evaluator {

	Evaluator ingrEvaluator;
	MrpVarRef jepVarRef;
	List<MrpVarRef> normVarRefs =null;
	List<MrpVarRef> derivMrpeRefs = null;
	/** Translate number of derivative to reference in ingredient */
	List<Integer> derivTrans;

	public ChainedEvaluator(Evaluator supercalc, 
			Evaluator ingrCE, MrpVarRef jvr,List<MrpVarRef> nvr,
			List<MrpVarRef> dmvr,List<Integer> dt) {
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

}
