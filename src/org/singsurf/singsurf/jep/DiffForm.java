package org.singsurf.singsurf.jep;

import java.util.ArrayList;
import java.util.List;

import com.singularsys.extensions.djep.DJep;
import com.singularsys.extensions.djep.DVariable;
import com.singularsys.extensions.djep.PartialDerivative;
import com.singularsys.extensions.xjep.CommandVisitorI;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepComponent;
import com.singularsys.jep.NodeFactory;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.Variable;
import com.singularsys.jep.functions.PostfixMathCommand;
import com.singularsys.jep.parser.ASTVarNode;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.parser.Node.HookKey;

public class DiffForm extends PostfixMathCommand  implements CommandVisitorI, JepComponent {
	private static final long serialVersionUID = 1L;

	DJep djep;
	NodeFactory nf;
	OperatorTableI ot;
	
	public HookKey RankKey = new HookKey() {};

	public DiffForm() {
		super(-1);
	}

	@Override
	public void init(Jep jep) {
		djep = (DJep) jep;
		nf = jep.getNodeFactory();
		ot = jep.getOperatorTable();
	}

	@Override
	public JepComponent getLightWeightInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node process(Node node, Node[] children) throws ParseException {
		Node lhs = children[0];
		
		if(lhs instanceof ASTVarNode ) {
			DVariable lhsvar = (DVariable) lhs.getVar();
			Node eqn = lhsvar.getEquation();
			Object rank = eqn.getHook(RankKey);
			if(rank==null)			
				return process0Form(node,children);
			else
				return diffHigherForm(lhs,eqn,children);			
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
			Variable var = rhs.getVar();
			if (var ==null) {
				throw new ParseException(
					"Format should be " + name + "(f,x) where x is a variables and 1,2 are constants");
			}
			rhsnames[i] = var.getName();
		}

		
		int lhsnchild = eqn.jjtGetNumChildren();
		PartialDerivative pd0 = (PartialDerivative) eqn.jjtGetChild(0).getVar();
		DVariable base = pd0.getRoot();
		int nderiv = pd0.getDnames().length;
		
		for(int i=0;i<lhsnchild;++i) {
			if(eqn.jjtGetChild(i).getVar() instanceof PartialDerivative) {
				PartialDerivative pd = (PartialDerivative) eqn.jjtGetChild(i).getVar();
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
		res.setHook(RankKey, nderiv+1);
		return res;
	}

	private void findAllDerivatives(DVariable base, String[] curnames,int depth, String[] names, List<DVariable> list) throws ParseException {
		if(depth==0) {
				DVariable var = djep.differentiate(base, curnames);
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
					"Format should be " + name + "(f,x) where x is a variables and 1,2 are constants");
			}
			ASTVarNode var;
			try {
				var = (ASTVarNode) rhs;
			} catch (ClassCastException e) {
				throw new ParseException(
					"Format should be " + name + "(f,x) where x is a variables and 1,2 are constants");
			}

			Node ele =  djep.differentiate(lhs, var.getName());
			res.jjtAddChild(ele, i);	
		}
		res.jjtClose();
		res.setHook(RankKey, 1);
		return res;
	}

	
	
}
