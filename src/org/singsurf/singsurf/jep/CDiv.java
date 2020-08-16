package org.singsurf.singsurf.jep;

import java.util.Stack;

import org.lsmp.djep.djep.DJep;
import org.lsmp.djep.djep.DiffRulesI;
import org.lsmp.djep.vectorJep.Dimensions;
import org.lsmp.djep.vectorJep.function.BinaryOperatorI;
import org.lsmp.djep.vectorJep.values.MatrixValueI;
import org.lsmp.djep.vectorJep.values.Tensor;
import org.lsmp.djep.xjep.NodeFactory;
import org.nfunk.jep.ASTFunNode;
import org.nfunk.jep.Node;
import org.nfunk.jep.Operator;
import org.nfunk.jep.OperatorSet;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

public class CDiv  extends PostfixMathCommand implements DiffRulesI, BinaryOperatorI {
    CMul cmul;
    
    
    public CDiv(CMul cmul) {
        super();
        this.cmul = cmul;
        numberOfParameters = 2;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void run(Stack inStack)        throws ParseException 
    {
        checkStack(inStack); // check the stack
        
        Object param2 = inStack.pop();
        Object param1 = inStack.pop();
        
        inStack.push(cdiv(param1, param2));

        return;
    }

    public Object cdiv(Object param1, Object param2)  throws ParseException {
        if(param1 instanceof MatrixValueI && param2 instanceof MatrixValueI)
        {
            return cdiv((MatrixValueI) param1,(MatrixValueI) param2);
        }
        throw new ParseException("Complex mul both arguments should be vectors");
    }

    public Object cdiv(MatrixValueI p, MatrixValueI q) throws ParseException {
        int n1 = p.getNumEles();
        int n2 = q.getNumEles();
        if(n1<2 || n1>3 || n2 < 2 || n2 > 3) 
            throw new ParseException("Wrong dimensions for complex mul "+n1+" "+n2);
        MatrixValueI res = Tensor.getInstance(Dimensions.TWO);
        double a = (Double) p.getEle(0);
        double b = (Double) p.getEle(1);
        double c = (Double) q.getEle(0);
        double d = (Double) q.getEle(1);
        
        double den = c*c + d*d;
                
        res.setEle(0, ( a * c + b * d ) / den );
        res.setEle(1, ( b * c - a * d ) / den );
        res.setEle(2, 0.0);
        return res;
    }

    @Override
    public Node differentiate(ASTFunNode node, String var, Node[] children,
            Node[] dchildren, DJep djep) throws ParseException {
        
        
        NodeFactory nf = djep.getNodeFactory();
        OperatorSet opset = djep.getOperatorSet();
        Operator sub = opset.getSubtract();
        int nchild = node.jjtGetNumChildren();
        if(nchild==2) {
            return nf.buildFunctionNode("cdiv",this,
                    new Node[]{nf.buildOperatorNode(sub,
                      nf.buildFunctionNode("cmul",cmul,
                        new Node[]{dchildren[0],
                        djep.deepCopy(children[1])}),
                        nf.buildFunctionNode("cmul",cmul,
                             new Node[]{djep.deepCopy(children[0]),
                        dchildren[1]})),
                        nf.buildFunctionNode("cmul",cmul,
                            new Node[]{djep.deepCopy(children[1]),
                      djep.deepCopy(children[1])})});

            
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
    public Dimensions calcDim(Dimensions ldim, Dimensions rdim)
            throws ParseException {
        return Dimensions.TWO;
    }

    @Override
    public MatrixValueI calcValue(MatrixValueI res, MatrixValueI p,
            MatrixValueI q) throws ParseException {
        int n1 = p.getNumEles();
        int n2 = q.getNumEles();
        if(n1<2 || n1>3 || n2 < 2 || n2 > 3) 
            throw new ParseException("Wrong dimensions for complex mul "+n1+" "+n2);
        double a = (Double) p.getEle(0);
        double b = (Double) p.getEle(1);
        double c = (Double) q.getEle(0);
        double d = (Double) q.getEle(1);
        
        double den = c*c + d*d;
                
        res.setEle(0, ( a * c + b * d ) / den );
        res.setEle(0, ( b * c - a * d ) / den );
        return res;
    }

}
