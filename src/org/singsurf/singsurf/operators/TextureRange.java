package org.singsurf.singsurf.operators;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.project.PgGeometryIf;
import jv.rsrc.PsGeometryInfo;
import jv.vecmath.PdVector;

public class TextureRange {
	final double xmin;
	final double xmax;
	final double ymin;
	final double ymax;
	
	
	public TextureRange(double xmin, double xmax, double ymin, double ymax) {
		super();
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}


	static public TextureRange findFrom(PgGeometryIf geom) throws EvaluationException {
		PsGeometryInfo info = geom.getGeometryInfo();
		String details = info.getDetail();
	    // { "name": "textureXmin", "value": "-1.0" },

		Pattern pat = Pattern.compile("\"texture(Xmin|Xmax|Ymin|Ymax)\",\\s*\"value\":\\s*\"([^\"]+)\"");
		Matcher matcher = pat.matcher(details);
		int count=0;
		double xl=0,xh=1,yl=0,yh=1;
		while(matcher.find()) {
			MatchResult mr = matcher.toMatchResult();
			String which = mr.group(1);
			String sval = mr.group(2);
			switch(which) {
			case "Xmin":
				xl = Double.parseDouble(sval);
				break;
			case "Xmax":
				xh = Double.parseDouble(sval);
				break;
			case "Ymin":
				yl = Double.parseDouble(sval);
				break;
			case "Ymax":
				yh = Double.parseDouble(sval);
				break;
			}
			++count;
		}
		if(count!=2 && count!=4) {
			throw new EvaluationException("Did not find texture bounds, found "+count+" matches");
		}
		return new TextureRange(xl,xh,yl,yh);
	}
	
	public static TextureRange UnitRange = new TextureRange(0,1,0,1);


	public PdVector scale(PdVector in) {
		double x = in.getFirstEntry() * (xmax-xmin) + xmin;
		double y = in.getLastEntry() * (ymax-ymin) + ymin;
		return new PdVector(x,y,0);
	}


	@Override
	public String toString() {
		return "TextureRange [xmin=" + xmin + ", xmax=" + xmax + ", ymin=" + ymin + ", ymax=" + ymax + "]";
	}
}
