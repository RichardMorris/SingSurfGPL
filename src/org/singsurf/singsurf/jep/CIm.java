package org.singsurf.singsurf.jep;

import com.singularsys.extensions.djep.DiffRulesI;
import com.singularsys.extensions.matrix.Dimensions;
import com.singularsys.extensions.matrix.MatrixFactoryI;
import com.singularsys.extensions.matrix.MatrixFunctionI;
import com.singularsys.extensions.matrix.VectorI;
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

/**
 * Simulates re(z) function by assuming a 2D vector is a complex number.
 */
public class CIm  extends UnaryFunction  implements DiffRulesI,MatrixFunctionI {
    private static final long serialVersionUID = 350L;
    NodeFactory nf;
    OperatorTableI ot;
    XJep djep;
    
    MatrixFactoryI mfac;
    
    
    
    public CIm(XJep djep, MatrixFactoryI mfac) {
        this.djep = djep;
        nf = djep.getNodeFactory();
        ot = djep.getOperatorTable();
        this.mfac = mfac;
    }

    
    @Override
    public Object eval(Object l) throws EvaluationException {
        return im(l);
    }


    private Object im(Object param1)  throws EvaluationException {
        if(param1 instanceof VectorI)
        {
            return im((VectorI) param1);
        }
        throw new EvaluationException("Real mul both arguments should be vectors");
    }

    private Object im(VectorI p) throws EvaluationException {
        return p.getEle(1);
    }

    /**
     * d re(u,v)/dx = du/dx 
     */
    @Override
    public Node differentiate(ASTFunNode node, String var, Node[] children,
            Node[] dchildren) throws ParseException, JepException {

        return nf.buildFunctionNode(name,this, 
                dchildren[0]); 
    }

    @Override
    public String getName() {
        return "cim";
    }
    
    @Override
    public void init(Jep jep) {
        djep = (XJep) jep;
        nf = jep.getNodeFactory();
        ot = jep.getOperatorTable();
        mfac = (MatrixFactoryI) jep.getAdditionalComponent(MatrixFactoryI.class );
    }



    @Override
    public Dimensions calcDims(Dimensions... inDims) throws ParseException {
        if(inDims.length != 1)
            throw new ParseException("Complex im must have one arguments");
        if(!inDims[0].is1D() || inDims[0].getFirstDim() > 3 ) 
            throw new ParseException("Complex im requires 2 or 3 dimension arguments found "+inDims[0]);

           return Dimensions.SCALER;
    }


    @Override
    public JepComponent getLightWeightInstance() {
	return null;
    }

}
