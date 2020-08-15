package org.singsurf.singsurf.calculators;

import java.util.ArrayList;
import java.util.List;

import com.singularsys.extensions.fastmatrix.MrpCommandList;
import com.singularsys.extensions.fastmatrix.MrpEval;
import com.singularsys.extensions.fastmatrix.MrpRes;
import com.singularsys.extensions.fastmatrix.MrpVarRef;
import com.singularsys.extensions.matrix.Dimensions;
import org.singsurf.singsurf.jepwrapper.EvaluationException;


/**
 * Evaluator which allows thread safe evaluation.
 * For each evaluation a new class is created which will be run in a separate thread.
 */
public class Evaluator {

	int inputDim;
	MrpEval mrpe = null;
	MrpVarRef variableRefs[];
	/** The mrpe instance */
	/** The command list for the top eqn */
	MrpCommandList topCom;
	/** The command list for derivatives */
	List<MrpCommandList> derivComs = new ArrayList<>();
	/** The command list for subsequent equations */
	List<MrpCommandList> allComs = new ArrayList<>();

	public Evaluator(int inputDim, MrpEval mrpe, MrpVarRef[] variableRefs, 
			MrpCommandList topCom, List<MrpCommandList> derivComs, List<MrpCommandList> allComs) {
		super();
		this.inputDim = inputDim;
		this.mrpe = mrpe;
		this.variableRefs = variableRefs;
		this.topCom = topCom;
		this.derivComs = derivComs;
		this.allComs = allComs;
	}

	public Evaluator(Evaluator root) {
		this.inputDim = root.inputDim;
		this.mrpe = root.mrpe;
		this.variableRefs = root.variableRefs;
		this.topCom = root.topCom;
		this.derivComs = root.derivComs;
		this.allComs = root.allComs;		
	}
	/**
	 * Evaluate the top equation
	 * 
	 * @throws EvaluationException
	 **/
	public double[] evalTop(double in[]) throws EvaluationException {
		double v[];
		try {
			for (int i = 0; i < inputDim; ++i) {
				if(variableRefs[i] != null)
				mrpe.setVarValue(variableRefs[i], in[i]);
			}
			
			for (MrpCommandList com : allComs) {
				@SuppressWarnings("unused")
				MrpRes tmp = mrpe.evaluate(com);
			}

			MrpRes res = mrpe.evaluate(topCom);
			v = resultAsVector(res);

		} catch (Exception e) {
			throw new EvaluationException(e);
		}
		return v;
	}

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

	/**
	 * Evaluates a first derivative.
	 * 
	 * @throws EvaluationException
	 */
	public double[] evalDerivative(int i) throws EvaluationException {
		MrpRes res = mrpe.evaluate(derivComs.get(i));
		return resultAsVector(res);
	}

}
