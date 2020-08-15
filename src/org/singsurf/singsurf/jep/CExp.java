package org.singsurf.singsurf.jep;

import com.singularsys.extensions.djep.DiffRulesI;
import com.singularsys.extensions.matrix.Dimensions;
import com.singularsys.extensions.matrix.MatrixFactoryI;
import com.singularsys.extensions.matrix.MatrixFunctionI;
import com.singularsys.extensions.xjep.XJep;
import org.singsurf.singsurf.jepwrapper.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepComponent;
import com.singularsys.jep.JepException;
import com.singularsys.jep.NodeFactory;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.functions.UnaryFunction;
import com.singularsys.jep.parser.ASTFunNode;
import com.singularsys.jep.parser.Node;

public class CExp extends UnaryFunction implements MatrixFunctionI, DiffRulesI {
	private static final long serialVersionUID = 1L;

    MatrixFactoryI mfac;
    NodeFactory nf;
    OperatorTableI ot;
    XJep djep;
    CMul cmul;

    public CExp(XJep djep, MatrixFactoryI mfac, CMul cmul) {
        this.djep = djep;
        this.mfac = mfac;
        this.cmul = cmul;
        nf = djep.getNodeFactory();
        ot = djep.getOperatorTable();
    }
 
	@Override
	public void init(Jep jep) {
        djep = (XJep) jep;
        nf = jep.getNodeFactory();
        ot = jep.getOperatorTable();
        mfac = (MatrixFactoryI) jep.getAdditionalComponent(MatrixFactoryI.class );
//        cmul = (CMul) jep.getFunctionTable().getFunction("cmul");
	}

	@Override
	public JepComponent getLightWeightInstance() {
		return null;
	}

	/**
	 * d e^(i f(x)) = i f'(x) * e^( i f(x)) 
	 * = cmul( [0,f'(x)] , cexp( f(x) )
	 */
	@Override
	public Node differentiate(ASTFunNode node, String var, Node[] children, Node[] dchildren)
			throws ParseException, JepException {
		return nf.buildFunctionNode(cmul.getName(),cmul,
				nf.buildOperatorNode(ot.getList(), 
						nf.buildConstantNode(0.0),
						dchildren[0]),
				nf.buildFunctionNode(name, this, children[0]));
	}

	@Override
	public Dimensions calcDims(Dimensions... inDims) throws ParseException {
		return Dimensions.TWO;
	}

	@Override
	public Object eval(Object arg) throws EvaluationException {
		Double dval = (Double) arg;
		return mfac.newVector(Math.cos(dval),Math.sin(dval));
	}

}
