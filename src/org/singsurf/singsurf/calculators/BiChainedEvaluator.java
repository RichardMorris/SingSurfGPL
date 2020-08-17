package org.singsurf.singsurf.calculators;

import java.util.List;

import org.lsmp.djep.mrpe.MRpCommandList;
import org.lsmp.djep.mrpe.MRpRes;
import org.singsurf.singsurf.jepwrapper.EvaluationException;

public class BiChainedEvaluator extends Evaluator {

	Evaluator ingredient1;
	Evaluator ingredient2;
	private int jepNormVarRef1 = -1;
	private int jepNormVarRef2 = -1;
	private int jepVarRef1;
	private int jepVarRef2;
	
    List<Integer> derivMrpeRefs1 = null;
    List<Integer> derivMrpeRefs2 = null;
    /** Translate number of derivative to reference in ingredient */
    List<Integer> derivTrans1;
    List<Integer> derivTrans2;

	
	public BiChainedEvaluator(Evaluator supercalc,Evaluator ingr1,Evaluator ingr2,
			int jepVarRef1, int jepNormVarRef1,
			List<Integer> derivMrpeRefs1, List<Integer> derivTrans1, 
			int jepVarRef2, int jepNormVarRef2, 
			List<Integer> derivMrpeRefs2, List<Integer> derivTrans2) {
		super(supercalc);
		
		this.ingredient1 = ingr1;
		this.ingredient2 = ingr2;
		
		this.jepVarRef1 = jepVarRef1;
		this.jepNormVarRef1 = jepNormVarRef1;
		this.derivMrpeRefs1 = derivMrpeRefs1;
		this.derivTrans1 = derivTrans1;
		this.jepNormVarRef2 = jepNormVarRef2;
		this.jepVarRef2 = jepVarRef2;
		this.derivMrpeRefs2 = derivMrpeRefs2;
		this.derivTrans2 = derivTrans2;
	}

    @Override
    public double[] evalTop(double[] in) throws EvaluationException {
        double igr1in[] = new double[1];
        double igr2in[] = new double[2];

        try {
            igr1in[0] = in[0];
            igr2in[0] = in[1];
            if (jepNormVarRef1 != -1)
                mrpe.setVarValue(jepNormVarRef1, in[0]);
            if (jepNormVarRef2 != -1)
                mrpe.setVarValue(jepNormVarRef2, in[1]);
            
            double[] ingrRes1 = ingredient1.evalTop(igr1in);
            mrpe.setVarValue(jepVarRef1, ingrRes1);
            for (int i = 0; i < this.derivMrpeRefs1.size(); ++i) {
                double[] derivRes = ingredient1.evalDerivative(this.derivTrans1
                        .get(i));
                mrpe.setVarValue(this.derivMrpeRefs1.get(i), derivRes);
            }

            double[] ingrRes2 = ingredient2.evalTop(igr2in);
            mrpe.setVarValue(jepVarRef2, ingrRes2);
            for (int i = 0; i < this.derivMrpeRefs2.size(); ++i) {
                double[] derivRes = ingredient2.evalDerivative(this.derivTrans2
                        .get(i));
                mrpe.setVarValue(this.derivMrpeRefs2.get(i), derivRes);
            }

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
