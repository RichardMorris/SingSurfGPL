package org.singsurf.singsurf.jepwrapper;

public class EvaluationException extends Exception {
	private static final long serialVersionUID = 1L;

	public EvaluationException(String string) {
		super(string);
	}

	public EvaluationException(Exception e) {
		super(e);
	}

}
