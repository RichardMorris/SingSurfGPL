package org.singsurf.singsurf.calculators;

import java.util.List;

import com.singularsys.extensions.fastmatrix.MrpCommandList;
import com.singularsys.extensions.fastmatrix.MrpRes;
import com.singularsys.extensions.fastmatrix.MrpVarRef;
import org.singsurf.singsurf.jepwrapper.EvaluationException;

public class ProductEvaluator extends Evaluator {

	Evaluator ingrEvaluator;
	MrpVarRef ingrOutputVarRef;
	List<MrpVarRef> ingrInputVarRefs =null;
	List<MrpVarRef> localVarRefs =null;
	List<MrpVarRef> derivMrpeRefs = null;
	/** Translate number of derivative to reference in ingredient */
	List<Integer> derivTrans;

	public ProductEvaluator(Evaluator supercalc, 
			Evaluator ingrCE, MrpVarRef jvr,List<MrpVarRef> nvr,
			List<MrpVarRef> dmvr,List<Integer> dt,List<MrpVarRef> lvr) {
		super(supercalc);
		ingrEvaluator = ingrCE;
		ingrOutputVarRef = jvr;
		ingrInputVarRefs = nvr;
		derivMrpeRefs = dmvr;
		derivTrans = dt;
		localVarRefs = lvr;
	}


	public double[] evalTop(double[] local,double[] in) throws EvaluationException {
		double[] ingrRes = ingrEvaluator.evalTop(in);

		try {
			if (ingrRes.length == 1) {
				mrpe.setVarValue(ingrOutputVarRef, ingrRes[0]);
			} else {
				mrpe.setVarValue(ingrOutputVarRef, ingrRes);
			}
			for(int i=0; i< ingrInputVarRefs.size();++i) {
				if(ingrInputVarRefs.get(i)!=null)
					mrpe.setVarValue(ingrInputVarRefs.get(i), in[i]);
			}
			for(int i=0; i< localVarRefs.size();++i) {
				if(localVarRefs.get(i)!=null)
					mrpe.setVarValue(localVarRefs.get(i), local[i]);
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


	@Override
	public double[] evalTop(double[] in) throws EvaluationException {
		int nlocal = localVarRefs.size();
		int ningr = ingrInputVarRefs.size();
		if(nlocal==1 && ningr==1) {
			return evalTop(new double[] {in[0]},new double[] {in[1]});
		}
		else throw new EvaluationException("Wrong size of input "+in.length+" did not fit "+nlocal+" local and "+ningr+" ingridient variables");
	}

}
