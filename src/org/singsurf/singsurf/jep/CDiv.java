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
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.functions.BinaryFunction;
import com.singularsys.jep.parser.ASTFunNode;
import com.singularsys.jep.parser.Node;


public class CDiv  extends BinaryFunction  implements DiffRulesI,MatrixFunctionI {
    private static final long serialVersionUID = 350L;
    CMul cmul;
    MatrixFactoryI mfac;
    NodeFactory nf;
    OperatorTableI ot;
    XJep djep;

    
    public CDiv(XJep djep, MatrixFactoryI mfac, CMul cmul) {
        this.djep = djep;
        this.mfac = mfac;
        this.cmul = cmul;
        nf = djep.getNodeFactory();
        ot = djep.getOperatorTable();
    }
    
    @Override
    public Object eval(Object l, Object r) throws EvaluationException {
        return cdiv(l,r);
    }


    public Object cdiv(Object param1, Object param2)  throws EvaluationException {
        if(param1 instanceof VectorI && param2 instanceof VectorI)
        {
            return cdiv((VectorI) param1,(VectorI) param2);
        }
        throw new EvaluationException("Complex div both arguments should be vectors");
    }

    public Object cdiv(VectorI p, VectorI q) throws EvaluationException {
        int n1 = p.getNEles();
        int n2 = q.getNEles();
        
        if(n1<2 || n1>3 || n2 < 2 || n2 > 3) 
            throw new EvaluationException("Wrong dimensions for complex mul "+n1+" "+n2);
        VectorI res = mfac.zeroVec(2);
        double a = (Double) p.getEle(0);
        double b = (Double) p.getEle(1);
        double c = (Double) q.getEle(0);
        double d = (Double) q.getEle(1);
        
        double den = c*c + d*d;
                
        res.setEle(0, ( a * c + b * d ) / den );
        res.setEle(1, ( b * c - a * d ) / den );
        return res;
    }

    @Override
    public Node differentiate(ASTFunNode node, String var, Node[] children,
            Node[] dchildren) throws ParseException, JepException {
        
        
        Operator sub = ot.getSubtract();
        int nchild = node.jjtGetNumChildren();
        if(nchild==2) {
            return nf.buildFunctionNode("cdiv",this,
                    nf.buildOperatorNode(sub,
                      nf.buildFunctionNode("cmul",cmul,
                        dchildren[0],
                        djep.deepCopy(children[1])),
                        nf.buildFunctionNode("cmul",cmul,
                             djep.deepCopy(children[0]),
                        dchildren[1])),
                        nf.buildFunctionNode("cmul",cmul,
                            djep.deepCopy(children[1]),
                      djep.deepCopy(children[1])));

            
//            return 
//                  nf.buildOperatorNode(div,
//                    nf.buildOperatorNode(sub,
//                      nf.buildOperatorNode(mul,
//                        dchildren[0],
//                        djep.deepCopy(children[1])),
//                      nf.buildOperatorNode(mul,
//                        djep.deepCopy(children[0]),
//                        dchildren[1])),
//                    nf.buildOperatorNode(mul,
//                      djep.deepCopy(children[1]),
//                      djep.deepCopy(children[1])));
      }

        return null;
    }

    @Override
    public String getName() {
        return "CDiv";
    }

    @Override
    public void init(Jep jep) {
    }

    @Override
    public JepComponent getLightWeightInstance() {
        return null;
    }



    @Override
    public Dimensions calcDims(Dimensions... inDims) throws ParseException {
        if(inDims.length != 2)
            throw new ParseException("Complex div must have two arguments");
        if(!inDims[0].is1D() || inDims[0].getFirstDim() > 3 
            || !inDims[1].is1D() || inDims[1].getFirstDim() > 3 ) 
            throw new ParseException("Complex div requires 2 or 3 dimension arguments found "+inDims[0]+" and "+inDims[1]);
            
        return Dimensions.TWO;
    }

}
