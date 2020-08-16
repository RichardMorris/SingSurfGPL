package org.singsurf.singsurf.calculators;

import java.util.ArrayList;
import java.util.List;


import org.lsmp.djep.mrpe.MRpCommandList;
import org.lsmp.djep.mrpe.MRpEval;
import org.lsmp.djep.mrpe.MRpRes;

import org.singsurf.singsurf.jepwrapper.EvaluationException;



/**
 * Evaluator which allows thread safe evaluation.
 * For each evaluation a new class is created which will be run in a separate thread.
 */
public class Evaluator {


	private int inputDim;
	/** The mrpe instance */
	MRpEval mrpe = null;
	/** mrpe reference numbers for each DefVariable, indexed by posn in definition */
	int variableRefs[];
	private MRpCommandList topCom;
	private List<MRpCommandList> allComs;
	private List<MRpCommandList> derivComs;
	
	/* JepFix
	double[] resultAsVector(MrpRes res) throws EvaluationException {
		double[] v;
		Dimensions dim = res.getDimensions();
		if (dim.is0D()) {
			v = new double[] { res.doubleValue() };
		} else if (dim.is1D())
			v = res.toArrayVec();
		else if(dim.is2D()) {
			double[][] mat = res.toArrayMat();
			v = new double[dim.numEles()];
			int pos=0;
			for(int i=0;i<dim.getFirstDim();++i) {
				for(int j=0;j<dim.getLastDim();++j) {
					v[pos++] = mat[i][j];
				}
			}
		} else
			throw new EvaluationException("Result should either be a scaler or a vector");
		return v;
	}
	*/

	public Evaluator(int inputDim, MRpEval mrpe, int[] variableRefs, MRpCommandList topCom,
			List<MRpCommandList> allComs, List<MRpCommandList> derivComs) {
		super();
		this.inputDim = inputDim;
		this.mrpe = mrpe;
		this.variableRefs = variableRefs;
		this.topCom = topCom;
		this.allComs = allComs;
		this.derivComs = derivComs;
	}

	/** Evaluate the top equation 
	 * @throws EvaluationException
	 **/
	public double[] evalTop(double in[]) throws EvaluationException
	{
		double v[];
		try {
			for(int i=0;i<inputDim;++i) {
				if(variableRefs[i] >=0 )
					mrpe.setVarValue(variableRefs[i],in[i]);
			}

			for(MRpCommandList com:allComs)
				mrpe.evaluate(com);

			MRpRes res = mrpe.evaluate(topCom);
			v = (double []) res.toArray();
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
		return v;
	}

	/** Evaluates a first derivative. */
	public double[] evalDerivative(int i)
	{
		MRpRes res = mrpe.evaluate(derivComs.get(i));
		return (double []) res.toArray();
	}

}
