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
 * 
 * 
[ g1/d, g2/d, g3/d ];
d = g1^2 + g2^2 + g3^2;
g1 = -3/2 im( cdiv( cmul(w, one - w4) , denom));
g2 = -3/2 re( cdiv( cmul(w, one + w4) , denom));
g3 = im( cdiv( one + w6, denom) ) - 0.5;
denom = w6 + rt5 w3 - one;
rt5 = sqrt(5);
one = [1,0];
w6 = cmul(w2,w4);
w4 = cmul(w2,w2);
w3 = cmul(w,w2);
w2 = cmul(w,w);
w=[x,y];

 */
public class Pad3  extends UnaryFunction  implements DiffRulesI,MatrixFunctionI {
    private static final long serialVersionUID = 350L;
    NodeFactory nf;
    OperatorTableI ot;
    XJep djep;
    
    MatrixFactoryI mfac;
    
    
    
    public Pad3(XJep djep, MatrixFactoryI mfac) {
        this.djep = djep;
        nf = djep.getNodeFactory();
        ot = djep.getOperatorTable();
        this.mfac = mfac;
    }

    
    @Override
    public Object eval(Object l) throws EvaluationException {
        return pad3(l);
    }


    private Object pad3(Object param1)  throws EvaluationException {
        if(param1 instanceof VectorI)
        {
            return pad3((VectorI) param1);
        }
        throw new EvaluationException("Real mul both arguments should be vectors");
    }

    private Object pad3(VectorI p) throws EvaluationException {
        VectorI res = mfac.zeroVec(3);
        res.setEle(0, p.getEle(0));
        res.setEle(1, p.getEle(1));
        res.setEle(2, 0.0);
        return res;
    }

    /**
     * d (u,v,0)/dx = (du/dx,dv/dx,0) 
     */
    @Override
    public Node differentiate(ASTFunNode node, String var, Node[] children,
            Node[] dchildren) throws ParseException, JepException {

        return nf.buildFunctionNode(name,this, 
                dchildren[0]); 
    }

    @Override
    public String getName() {
        return "pad3";
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
            throw new ParseException("Complex im requires 2 or 3 dimension arguments found "+inDims[0]+" and "+inDims[1]);

           return Dimensions.THREE;
    }


    @Override
    public JepComponent getLightWeightInstance() {
	return null;
    }

}
