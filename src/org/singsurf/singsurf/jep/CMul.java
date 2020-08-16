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
import org.nfunk.jep.OperatorSet;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.Add;
import org.nfunk.jep.function.Multiply;
import org.nfunk.jep.function.PostfixMathCommand;
import org.nfunk.jep.function.Subtract;

public class CMul  extends PostfixMathCommand  implements DiffRulesI, BinaryOperatorI {
    protected Add add = new Add();
    protected Subtract sub = new Subtract();
    protected Multiply mul = new Multiply();
    
    public CMul()
    {
        numberOfParameters = 2;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void run(Stack inStack)        throws ParseException 
    {
        checkStack(inStack); // check the stack
        
        Object param2 = inStack.pop();
        Object param1 = inStack.pop();
        
        inStack.push(cmul(param1, param2));

        return;
    }

    private Object cmul(Object param1, Object param2)  throws ParseException {
        if(param1 instanceof MatrixValueI && param2 instanceof MatrixValueI)
        {
            return cmul((MatrixValueI) param1,(MatrixValueI) param2);
        }
        throw new ParseException("Complex mul both arguments should be vectors");
    }

    private Object cmul(MatrixValueI p, MatrixValueI q) throws ParseException {
        MatrixValueI res = Tensor.getInstance(Dimensions.TWO);
        return calcValue(res,p,q);
    }

    @Override
    public Node differentiate(ASTFunNode node, String var, Node[] children,
            Node[] dchildren, DJep djep) throws ParseException {

        OperatorSet opset = djep.getOperatorSet();
        NodeFactory nf = djep.getNodeFactory();


        return nf.buildOperatorNode(opset.getAdd(), 
                nf.buildFunctionNode("cmul",this,
                        new Node[]{
                dchildren[0], djep.deepCopy(children[1])}), 
                nf.buildFunctionNode("cmul",this,
                        new Node[]{ djep.deepCopy(children[0]),
                dchildren[1]}));

    
    }

    @Override
    public String getName() {
        return "cmul";
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
        res.setEle(2, 0.0);
        return res;
    }

}
