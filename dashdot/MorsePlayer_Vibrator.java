package sam.dashdot;

/*
Name:           MorsePlayer_Vibrator
Description:    Use phone vibration to output a Morse message
Note:
Author:         Sam George
Methods:
 */

import android.content.Context;
import android.os.Vibrator;

public class MorsePlayer_Vibrator implements MorsePlayerListener {

    Context myContext;
    Vibrator myV;
    boolean hasV;
    //0 ms delay before pattern begins, play for 500 ms
    //Likely will be interrupted by turnOff
    long[] vibrationPattern={0, 500};

    //Constructor
    MorsePlayer_Vibrator(Context nContext) {
        super();
        myContext=nContext;
        if (myContext.getSystemService(myContext.VIBRATOR_SERVICE) !=null) {
            hasV=true;
        }
        else
            hasV=false;
    }

    private void turnOn() {
        myV=(Vibrator) this.myContext.getSystemService(myContext.VIBRATOR_SERVICE);
        //Play pattern and delay 0ms before repeating the pattern
        myV.vibrate(vibrationPattern, 0);
    }

    private void turnOff() {
        if (myV!=null)
            myV.cancel();
    }


    @Override
    public void elementStarted() {
        if (hasV && myV!=null)
            turnOn();
        else if (hasV) {
            myV=(Vibrator) this.myContext.getSystemService(myContext.VIBRATOR_SERVICE);
            turnOn();
        }
    }

    @Override
    public void elementStopped() {
        if (hasV && myV!=null) turnOff();
    }

    @Override
    public void releaseAll() {
        myV=null;
    }
}
