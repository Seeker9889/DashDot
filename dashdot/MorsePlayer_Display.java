package sam.dashdot;

/*
Name:           MorsePlayer_Display
Description:    Used to display the output of a Morse message on screen
Note:
Author:         Sam George
Methods:
 */

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.content.Context;

public class MorsePlayer_Display implements MorsePlayerListener  {

    ImageView myV;
    Drawable on, off;
    Context myContext;


    //Constructor
    MorsePlayer_Display(ImageView nIV, Context nContext) {
        super();
        myV=nIV;
        myContext=nContext;
    }


    //Start signal for element
    private void turnOn() {
        on=ContextCompat.getDrawable(myContext, R.drawable.shapes_bright);
        myV.setImageDrawable(on);
        on=null;
    }

    //Stop signal for element
    private void turnOff() {
        off=ContextCompat.getDrawable(myContext, R.drawable.shapes);
        myV.setImageDrawable(off);
        off=null;
    }


    @Override
    public void elementStarted() {
        if (myV !=null)
            turnOn();
        else {
            on=ContextCompat.getDrawable(myContext, R.drawable.shapes_bright);
            myV.setImageDrawable(on);
        }
    }

    @Override
    public void elementStopped() {
        if (myV !=null) turnOff();
    }

    @Override
    public void releaseAll() {
        myV=null;
    }
}
