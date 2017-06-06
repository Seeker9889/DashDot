package sam.dashdot;

/*
Name:           MorsePlayer_Flash
Description:    Used to display the output of a Morse message with flash
Note:
Author:         Sam George
Methods:
 */

import android.content.pm.PackageManager;
import android.content.Context;
import android.hardware.Camera;

public class MorsePlayer_Flash implements MorsePlayerListener  {

    Context myContext;
    Camera myCam;
    Camera.Parameters myParam;
    boolean hasCam;


    //Constructor
    MorsePlayer_Flash(Context nContext) {
        super();
        myContext=nContext;
        //If context has a camera, get ready to use it
        if (myContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            hasCam=true;
            if (myCam!=null)
                myCam.release();
            initCam();
        }
        else
            hasCam=false;
    }


    //Start signal for element
    private void turnOn() {
        myParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        myCam.setParameters(myParam);
        myCam.startPreview();
    }

    //Stop signal for element
    private void turnOff() {
        if (myCam !=null) {
            myParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            myCam.setParameters(myParam);
            myCam.stopPreview();
        }
    }

    private void initCam() {
        myCam = Camera.open();
        myParam = myCam.getParameters();
        myParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        myCam.setParameters(myParam);
    }

    @Override
    public void elementStarted() {
        if (hasCam && myCam !=null)
            turnOn();
        else if (hasCam) {
           initCam();
            turnOn();
        }
    }

    @Override
    public void elementStopped() {
        if (hasCam  && myCam !=null) turnOff();
    }

    @Override
    public void releaseAll() {
        if (myCam !=null)
            myCam.release();
        myCam=null;
    }

}
