package org.singsurf.singsurf.junit;

import org.junit.Test;
import org.singsurf.singsurf.jep.CDiv;
import org.singsurf.singsurf.jep.CMul;

import com.singularsys.extensions.djep.DJep;
import com.singularsys.extensions.fastmatrix.MrpCommandList;
import com.singularsys.extensions.fastmatrix.MrpEval;
import com.singularsys.extensions.fastmatrix.MrpRes;
import com.singularsys.extensions.matrix.DimensionVisitor;
import com.singularsys.extensions.matrix.MatrixFactoryI;
import com.singularsys.extensions.matrix.MatrixFunctionTable;
import com.singularsys.extensions.matrix.MatrixOperatorTable;
import com.singularsys.extensions.matrix.VectorI;
import com.singularsys.extensions.matrix.doublemat.DoubleMatrixFactory;
import com.singularsys.extensions.matrix.doublemat.DoubleMatrixField;
import com.singularsys.extensions.matrixdiff.MatrixDifferentiationVisitor;
import org.singsurf.singsurf.jepwrapper.EvaluationException;
import com.singularsys.jep.JepException;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.parser.Node;

import junit.framework.TestCase;

public class ComplexMulTest extends TestCase {

    DJep jep;
    private MatrixFactoryI mfact;
    private CMul cmul;
    private CDiv cdiv;
    private MrpEval mrpe;
    private DimensionVisitor dimV;
    
    @Override
    protected void setUp() throws Exception {
        mfact = new DoubleMatrixFactory();
        DoubleMatrixField mfield = new DoubleMatrixField(mfact);

        StandardConfigurableParser cp = new StandardConfigurableParser();
        MatrixFunctionTable mfun = new MatrixFunctionTable(mfact,mfield);
        MatrixOperatorTable ot = new MatrixOperatorTable(mfact,mfield);
        MatrixDifferentiationVisitor diffV = new MatrixDifferentiationVisitor(mfact);

        jep = new DJep(cp,mfun,ot,diffV);
        mrpe = new MrpEval(jep,mfact);       
        dimV = new DimensionVisitor(jep);

        jep.addStandardConstants();
        jep.addStandardDiffRules();
        cmul = new CMul(jep,mfact);
        cdiv = new CDiv(jep,mfact,cmul);
        jep.addFunction("cmul", cmul);
        jep.addFunction("cdiv", cdiv);
        jep.reinitializeComponents();
    }

    @Test
    public void testOperations() throws EvaluationException {
        VectorI one = mfact.newVector(new Object[]{1.0,0.0});
        VectorI i = mfact.newVector(new Object[]{0.0,1.0});
        VectorI minusone = mfact.newVector(new Object[]{-1.0,0.0});
        VectorI minusi = mfact.newVector(new Object[]{0.0,-1.0});
        VectorI zero = mfact.newVector(new Object[]{0.0,0.0});
        
        Object res = cmul.eval(one, one);
        assertEquals(one,res);

        res = cdiv.eval(one, one);
        assertEquals(one,res);
        
        res = cmul.eval(i, i);
        assertEquals(minusone,res);
        
        res = cdiv.eval(i, i);
        assertEquals(one,res);
        
        res = cmul.eval(one, i);
        assertEquals(i,res);

        res = cdiv.eval(one, i);
        assertEquals(minusi,res);

        res = cmul.eval(i,one);
        assertEquals(i,res);

        res = cdiv.eval(i,one);
        assertEquals(i,res);

        res = cmul.eval(zero,zero);
        assertEquals(zero,res);

        res = cdiv.eval(zero,one);
        assertEquals(zero,res);

        VectorI vres = (VectorI) cdiv.eval(one,zero);
        assertTrue(Double.isNaN((Double) vres.getEle(0)));
        assertTrue(Double.isNaN((Double) vres.getEle(1)));

        vres = (VectorI) cdiv.eval(zero,zero);
        assertTrue(Double.isNaN((Double) vres.getEle(0)));
        assertTrue(Double.isNaN((Double) vres.getEle(1)));

    }
    
    @Test
    public void testJepEval() throws JepException {
        VectorI one = mfact.newVector(new Object[]{1.0,0.0});
        VectorI i = mfact.newVector(new Object[]{0.0,1.0});
        VectorI minusone = mfact.newVector(new Object[]{-1.0,0.0});

        Node n = jep.parse("cmul(x,x)");
        jep.setVariable("x",i);
        Object res = jep.evaluate(n);
        assertEquals(minusone,res);
        
        n = jep.parse("cdiv(x,x)");
        jep.setVariable("x",i);
        res = jep.evaluate(n);
        assertEquals(one,res);

    }

    @Test
    public void testMRepEval() throws JepException {
       // VectorI one = mfact.newVector(new Object[]{1.0,0.0});
        VectorI i = mfact.newVector(new Object[]{0.0,1.0});
        VectorI minusone = mfact.newVector(new Object[]{-1.0,0.0});

        jep.setVariable("x",i);
        Node n = jep.parse("cmul(x,x)");
        dimV.visit(n);
        MrpCommandList coms = mrpe.compile(n);
        MrpRes res = mrpe.evaluate(coms);
        
        VectorI vres = mfact.zeroVec(2);
        res.copyToVec(vres);
        assertEquals(minusone,vres);
        
//        n = jep.parse("cdiv(x,x)");
//        jep.setVariable("x",i);
//        res = jep.evaluate(n);
//        assertEquals(one,res);

    }

    @Test
    public void testJepDiff() throws JepException {
        Node n = jep.parse("cmul(x,x)");
        Node diff = jep.differentiate(n, "x");
        String s = jep.toString(diff);
        assertEquals("cmul(1.0,x)+cmul(x,1.0)",s);

        n = jep.parse("cdiv(x,x)");
        diff = jep.differentiate(n, "x");
        s = jep.toString(diff);
        assertEquals("cdiv(cmul(1.0,x)-cmul(x,1.0),cmul(x,x))",s);

    }
}
