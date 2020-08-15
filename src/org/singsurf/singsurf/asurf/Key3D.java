/*
Created 26 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public enum Key3D {
	NONE,
	VERTEX,
	X_AXIS,Y_AXIS,Z_AXIS,
	FACE_LL,FACE_RR,FACE_FF,FACE_BB,FACE_DD,FACE_UU,
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
}
