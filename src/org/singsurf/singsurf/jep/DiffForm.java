package org.singsurf.singsurf.jep;

import java.util.ArrayList;
import java.util.List;

import org.lsmp.djep.djep.DJep;
import org.lsmp.djep.djep.DVariable;
import org.lsmp.djep.djep.PartialDerivative;
import org.lsmp.djep.xjep.CommandVisitorI;
import org.lsmp.djep.xjep.NodeFactory;
import org.lsmp.djep.xjep.XJep;
import org.nfunk.jep.ASTVarNode;
import org.nfunk.jep.Node;
import org.nfunk.jep.OperatorSet;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.Variable;
import org.nfunk.jep.function.PostfixMathCommand;


public class DiffForm extends PostfixMathCommand  implements CommandVisitorI {
	private static final long serialVersionUID = 1L;

	DJep djep;
	NodeFactory nf;
	OperatorSet ot;
	
//JepFix	public HookKey RankKey = new HookKey() {};

	public DiffForm() {
//JepFix		super(-1);
		this.numberOfParameters = -1;
	}

	public void init(DJep jep) {
		djep = (DJep) jep;
		nf = jep.getNodeFactory();
		ot = jep.getOperatorSet();
	}

	@Override
	public Node process(Node node, Node[] children, XJep xjep) throws ParseException {
		Node lhs = children[0];
		
		if(lhs instanceof ASTVarNode ) {
			DVariable lhsvar = (DVariable) ((ASTVarNode) lhs).getVar();
			Node eqn = lhsvar.getEquation();
//	JepFix		Object rank = eqn.getHook(RankKey);
//			if(rank==null)			
				return process0Form(node,children);
//			else
//	JepFix			return diffHigherForm(lhs,eqn,children);			
		}
		throw new ParseException("First argument should be a variable");
	}

	/**
	 * @param lhs
	 * @param eqn 
	 * @return 
	 * @throws ParseException
	 */
	Node diffHigherForm(Node lhs,Node eqn, Node[] children) throws ParseException {
		int nchild = children.length;
		String rhsnames[] = new String[nchild-1];
		
		for(int i=0;i<nchild-1;++i) {
			Node rhs = children[i+1];
			Variable var = ((ASTVarNode) rhs).getVar();
			if (var ==null) {
				throw new ParseException(
					"Format should be fun(f,x) where x is a variables and 1,2 are constants");
			}
			rhsnames[i] = var.getName();
		}

		
		int lhsnchild = eqn.jjtGetNumChildren();
		PartialDerivative pd0 = (PartialDerivative) ((ASTVarNode)eqn.jjtGetChild(0)).getVar();
		DVariable base = pd0.getRoot();
		int nderiv = pd0.getDnames().length;
		
		for(int i=0;i<lhsnchild;++i) {
			final Node child = eqn.jjtGetChild(i);
			if(child instanceof ASTVarNode && ((ASTVarNode)child).getVar() instanceof PartialDerivative) {
				PartialDerivative pd = (PartialDerivative) ((ASTVarNode)child).getVar();
				if(base != pd.getRoot()) {
					throw new ParseException("All partial derivatives should have the same root");
				}
				String dnames[] = pd.getDnames();
				if(dnames.length != nderiv) {
					throw new ParseException("All partial derivatives must have the same number of derivatives");
				}
				for(String name:dnames) {
					boolean found=false;
					for(String n2:rhsnames) {
						if(name.equals(n2)) { found = true; }
					}
					if(!found) {
						throw new ParseException("Partial derivative does not match suplied names");
					}
				}
			} else throw new ParseException("All parts of lhs array should be partial derivatives");
		}

		List<DVariable> parts = new ArrayList<>();
		findAllDerivatives(base,new String[0],nderiv+1,rhsnames,parts);	
		
		Node res = nf.buildUnfinishedOperatorNode(ot.getList());
		int pos=0;
		for(DVariable deriv:parts) {
			res.jjtAddChild(nf.buildVariableNode(deriv), pos++);
		}
		res.jjtClose();
//JepFix		res.setHook(RankKey, nderiv+1);
		return res;
	}

	private void findAllDerivatives(DVariable base, String[] curnames,int depth, String[] names, List<DVariable> list) throws ParseException {
		if(depth==0) {
				Node var_node = djep.getNodeFactory().buildVariableNode(base);
				Node deriv = (Node) djep.getDifferentationVisitor().visit((ASTVarNode)var_node, names);
				DVariable var = (DVariable) ((ASTVarNode)deriv).getVar();
				list.add(var);
		} else {
			for(int i=0;i<names.length;++i) {
				String newnames[] = new String[names.length-i];
				System.arraycopy(names, i, newnames, 0, names.length-i);
				String donenames[] = new String[curnames.length+1];
				System.arraycopy(curnames, 0, donenames, 0, curnames.length);
				donenames[curnames.length] = names[i];
				findAllDerivatives(base,donenames,depth-1,newnames,list);
			}
		}
	}
	
	private Node process0Form(Node node, Node[] children) throws ParseException {
		int nchild = children.length;
		Node res = nf.buildUnfinishedOperatorNode(ot.getList());
		Node lhs = children[0];

		for(int i=0;i<nchild-1;++i) {
			Node rhs = children[i+1];
			if (!djep.getTreeUtils().isVariable(rhs)) {
				throw new ParseException(
					"Format should be fun(f,x) where x is a variables and 1,2 are constants");
			}
			ASTVarNode var;
			try {
				var = (ASTVarNode) rhs;
			} catch (ClassCastException e) {
				throw new ParseException(
					"Format should be fun(f,x) where x is a variables and 1,2 are constants");
			}

			Node ele =  djep.differentiate(lhs, var.getName());
			res.jjtAddChild(ele, i);	
		}
		res.jjtClose();
//JepFix		res.setHook(RankKey, 1);
		return res;
	}	
}
