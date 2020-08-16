package org.singsurf.singsurf.jep;


import org.lsmp.djep.djep.DiffRulesI;
import org.lsmp.djep.vectorJep.Dimensions;
import org.lsmp.djep.vectorJep.function.BinaryOperatorI;
import org.lsmp.djep.xjep.NodeFactory;
import org.lsmp.djep.xjep.XJep;
import org.nfunk.jep.OperatorSet;
import org.nfunk.jep.function.PostfixMathCommand;
import org.singsurf.singsurf.jepwrapper.EvaluationException;

/**
 * Simulates re(z) function by assuming a 2D vector is a complex number.
 */
public class CIm   extends PostfixMathCommand  implements DiffRulesI, BinaryOperatorI {
    private static final long serialVersionUID = 350L;
    NodeFactory nf;
    OperatorSet ot;
    XJep djep;
        
    
    public CIm(XJep djep) {
        this.djep = djep;
        nf = djep.getNodeFactory();
        ot = djep.getOperatorSet();
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
