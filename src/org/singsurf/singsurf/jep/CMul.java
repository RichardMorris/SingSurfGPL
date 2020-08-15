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
import com.singularsys.jep.functions.Add;
import com.singularsys.jep.functions.BinaryFunction;
import com.singularsys.jep.functions.Multiply;
import com.singularsys.jep.functions.Subtract;
import com.singularsys.jep.parser.ASTFunNode;
import com.singularsys.jep.parser.Node;

/**
 * Simulates complex multiplication by assuming a 2D vector is a complex number.
 */
public class CMul  extends BinaryFunction  implements DiffRulesI,MatrixFunctionI {
    private static final long serialVersionUID = 350L;
    protected Add add = new Add();
    protected Subtract sub = new Subtract();
    protected Multiply mul = new Multiply();
    NodeFactory nf;
    OperatorTableI ot;
    XJep djep;
    
    MatrixFactoryI mfac;
    
    
    
    public CMul(XJep djep, MatrixFactoryI mfac) {
        this.djep = djep;
        nf = djep.getNodeFactory();
        ot = djep.getOperatorTable();
        this.mfac = mfac;
    }

    
    @Override
    public Object eval(Object l, Object r) throws EvaluationException {
        return cmul(l,r);
    }


    private Object cmul(Object param1, Object param2)  throws EvaluationException {
        if(param1 instanceof VectorI && param2 instanceof VectorI)
        {
            return cmul((VectorI) param1,(VectorI) param2);
        }
        throw new EvaluationException("Complex mul both arguments should be vectors");
    }

    private Object cmul(VectorI p, VectorI q) throws EvaluationException {
        VectorI res = mfac.zeroVec(2);
        return calcValue(res,p,q);
    }

    /**
     * d cmul(u,v)/dx = cmul(du/dx,v) + cmul(u,dv/dx) 
     */
    @Override
    public Node differentiate(ASTFunNode node, String var, Node[] children,
            Node[] dchildren) throws ParseException, JepException {

        return nf.buildOperatorNode(ot.getAdd(), 
                nf.buildFunctionNode(name,this,
                dchildren[0], djep.deepCopy(children[1])), 
                nf.buildFunctionNode(name,this,
                        djep.deepCopy(children[0]),
                        dchildren[1]));    
    }

    @Override
    public String getName() {
        return "cmul";
    }


    public VectorI calcValue(VectorI res, VectorI p, VectorI q) throws EvaluationException {
        int n1 = p.getNEles();
        int n2 = q.getNEles();
        if(n1<2 || n1>3 || n2 < 2 || n2 > 3) 
            throw new EvaluationException("Wrong dimensions for complex mul "+n1+" "+n2);
        res.setEle(0, 
                sub.sub(
                        mul.mul(p.getEle(0), q.getEle(0)),
                        mul.mul(p.getEle(1), q.getEle(1))
                ));
        res.setEle(1, 
                add.add(
                        mul.mul(p.getEle(0), q.getEle(1)),
                        mul.mul(p.getEle(1), q.getEle(0))
                ));
        return res;
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
        if(inDims.length != 2)
            throw new ParseException("Complex mul must have two arguments");
        if(!inDims[0].is1D() || inDims[0].getFirstDim() > 3 
            || !inDims[1].is1D() || inDims[1].getFirstDim() > 3 ) 
            throw new ParseException("Complex mul requires 2 or 3 dimension arguments found "+inDims[0]+" and "+inDims[1]);

           return Dimensions.TWO;
    }


    @Override
    public JepComponent getLightWeightInstance() {
	return null;
    }

}
