package org.singsurf.singsurf;

import jv.project.PvCameraIf;
import jv.project.PvDisplayIf;
import jv.vecmath.PdVector;

public class SingSurf2D extends SingSurf3D {
    private static final long serialVersionUID = 1L;

    @Override
    public void init() {
        // TODO Auto-generated method stub
        super.init();
        //m_viewer.getDisplay().
        PvDisplayIf display = m_viewer.getDisplay();
        PvCameraIf camera = display.getCamera();
        camera.setProjectionMode(PvCameraIf.CAMERA_ORTHO_XY);
        camera.setInterest(new PdVector(0,0,0));
        camera.setViewDir(new PdVector(0,0,-1));
        display.showScenegraph(false);
        display.setEnabled3DLook(false);
        display.setEnabledZBuffer(false);
    }

    public static void main(String args[]) {
        SingSurf2D va  = new SingSurf2D();
        commonMain(va,args);
    }

}
