/*
Created 26 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public enum Key3D {
	NONE,
	VERTEX,
	X_AXIS,Y_AXIS,Z_AXIS,
	FACE_LL,FACE_RR,FACE_FF,FACE_BB,FACE_DD,FACE_UU,
	EDGE_LD, EDGE_LU, EDGE_LF, EDGE_LB,
	EDGE_RD, EDGE_RU, EDGE_RF, EDGE_RB,
	EDGE_FD, EDGE_FU, EDGE_BD, EDGE_BU,
	BOX;

	public boolean isFace() {
		
		return this.compareTo(FACE_LL)>=0 && this.compareTo(FACE_UU)<=0;
	}

    public boolean isEdge() {
        
        return this.compareTo(X_AXIS)>=0 && this.compareTo(Z_AXIS)<=0;
    }

    Key3D oppositeFace() {
    	switch(this) {
		case FACE_BB:
			return FACE_FF;
		case FACE_DD:
			return FACE_UU;
		case FACE_FF:
			return FACE_BB;
		case FACE_LL:
			return FACE_RR;
		case FACE_RR:
			return FACE_LL;
		case FACE_UU:
			return FACE_DD;
		default:
			throw new IllegalArgumentException("Bad key "+this);    	
    	}
    }
    
    Key3D axis() {
    	switch(this) {
		case EDGE_BD:
			return X_AXIS;
		case EDGE_BU:
			return X_AXIS;
		case EDGE_FD:
			return X_AXIS;
		case EDGE_FU:
			return X_AXIS;
		case EDGE_LB:
			return Z_AXIS;
		case EDGE_LD:
			return Y_AXIS;
		case EDGE_LF:
			return Z_AXIS;
		case EDGE_LU:
			return Y_AXIS;
		case EDGE_RB:
			return Z_AXIS;
		case EDGE_RD:
			return Y_AXIS;
		case EDGE_RF:
			return Z_AXIS;
		case EDGE_RU:
			return Y_AXIS;
		default:
			break;
    	
    	}
    	return null;
    }
}
