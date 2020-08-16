package org.singsurf.singsurf.jepwrapper;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommandI;

public class XAssign implements PostfixMathCommandI {

	@Override
	public void run(Stack aStack) throws ParseException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumberOfParameters() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setCurNumberOfParameters(int n) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean checkNumberOfParameters(int n) {
		// TODO Auto-generated method stub
		return false;
	}

}
