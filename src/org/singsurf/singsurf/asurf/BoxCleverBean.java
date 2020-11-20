package org.singsurf.singsurf.asurf;

import java.util.List;

public interface BoxCleverBean {
	
	List<Double> getCoeffsAsList();
	void setCoeffsAsList(List<Double> co);
		
	int getDegX();
	void setDegX(int n);
	
	int getDegY();
	void setDegY(int n);

	int getDegZ();
	void setDegZ(int n);
	
	RegionBean getRegionBean();
	void setRegionBean(RegionBean bean);
	
	int getCourseRes();
	void setCourseRes(int x);
	
	int getFineRes();
	void setFineRes(int x);
	
	int getFaceRes();
	void setFaceRes(int x);
	
	int getEdgeRes();
	void setEdgeRes(int x);

	int getGlobal_selx();
	void setGlobal_selx(int global_selx);

	int getGlobal_sely();
	void setGlobal_sely(int global_sely);

	int getGlobal_selz();
	void setGlobal_selz(int global_selz);

	int getGlobal_denom();
	void setGlobal_denom(int global_denom);

	boolean isLittleFacets();

	void setLittleFacets(boolean global_lf);

	int getTriangulate();

	void setTriangulate(int triangulate);

	int getCleanmesh();

	void setCleanmesh(int cleanmesh);

	int getTagbad();

	void setTagbad(int tagbad);

	int getTagSing();

	void setTagSing(int tagSing);

	int getBlowup();

	void setBlowup(int blowup);

	double getConvtol();

	void setConvtol(double convtol);

	int getColortype();

	void setColortype(int colortype);

	float getColourMin();

	void setColourMin(float colourMin);

	float getColourMax();

	void setColourMax(float colourMax);

	boolean isKnitFacets();

	void setKnitFacets(boolean knitFacets);

	boolean isRefineCurvature();

	void setRefineCurvature(boolean refineCurvature);

	double getCurvatureLevel1();

	void setCurvatureLevel1(double curvatureLevel1);

	double getCurvatureLevel2();

	void setCurvatureLevel2(double curvatureLevel2);

	double getCurvatureLevel3();

	void setCurvatureLevel3(double curvatureLevel3);

	double getCurvatureLevel4();

	void setCurvatureLevel4(double curvatureLevel4);

	double getNormlenlevel1();

	void setNormlenlevel1(double normlenlevel1);

	double getNormlenlevel2();

	void setNormlenlevel2(double normlenlevel2);

	double getNormlenlevel3();

	void setNormlenlevel3(double normlenlevel3);

	Double getNormlenlevel4();

	void setNormlenlevel4(double normlenlevel4);

	int getParallel();

	void setParallel(int parallel);

	String getDescription();

	void setDescription(String description);
}