package org.singsurf.singsurf.jepwrapper;

import org.nfunk.jep.function.PostfixMathCommand;

public abstract class BinaryFunction extends PostfixMathCommand {

	public abstract Object eval(Object l, Object r) throws EvaluationException ;
}
