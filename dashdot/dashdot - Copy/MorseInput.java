package sam.dashdot;

import java.util.Timer;
import java.util.TimerTask;
/*
Name:           MorseInput
Description:    Used to translate spaced input into Morse Code
Note:           Now using java.util.Timer instead of javax.swing.timer
Author:         Sam George
Methods:
To-Do:          
 */

public class MorseInput {

    private class HeldTask extends TimerTask {
        @Override
        public void run() {
            heldLength();
        }
    }

    private class SpaceTask extends TimerTask {
        @Override
        public void run() {
            spaceLength();
        }
    }


    Timer sinceHold;
    Timer held;
    HeldTask heldTask= new HeldTask();
    SpaceTask spaceTask= new SpaceTask();
    boolean hasPressed;
    boolean checkDone;
    long bI; //base interval
    int timeHeld; //time of a certain element
    int timeSpace; //time not signaled after a certain element
    MorseMessage.mCode[] elementList=new MorseMessage.mCode[] {};
    MorseMessage.mCode lastElement;

    //Constructors
    MorseInput(int newInterval) {
        bI= (long) newInterval;
        checkDone=false;
        hasPressed=false;
        timeHeld=0;
        timeSpace=0;


        held=new Timer();
        sinceHold=new Timer();

        /*
        held = new Timer(bI,new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heldLength(evt);
            }
        } );

        sinceHold = new Timer(bI,new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spaceLength(evt);
            }
        } );
*/
    }

    //Public methods to start and stop elements

    //Start an element
    public void startHold() {
        held.cancel();
        held=new Timer();
        held.schedule(new HeldTask(), bI, bI);
        sinceHold.cancel();
        hasPressed=true;
        if (lastElement==MorseMessage.mCode.LESPACE || lastElement==MorseMessage.mCode.WOSPACE) {
            addElement();
        }
    }

    //End an element
    public void endHold() {
        held.cancel();
        held.purge();

        switch (timeHeld) {
            case 0:
            case 1:
            case 2:
                lastElement=MorseMessage.mCode.DOT;
                break;
            case 3:
                lastElement=MorseMessage.mCode.DASH;
                break;
            default:
                lastElement=MorseMessage.mCode.DASH;
                break;
        }

        addElement();
        timeHeld=0;
    }

    //Start a space
    public void startSpace() {
        if (hasPressed==true) {
            sinceHold.cancel();
            sinceHold= new Timer();
            sinceHold.schedule(new SpaceTask(), bI, bI);
        }
    }

    //End a space
    public void endSpace() {
        if (hasPressed==false) {
            ;//Do nothing
        }
        else {
            hasPressed=false;
            switch (timeSpace) {
                //If there is 1 or less units between input, there is no LESPACE inserted
                case 0:
                case 1:
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    lastElement=MorseMessage.mCode.LESPACE;
                    break;
                case 7:
                    lastElement=MorseMessage.mCode.WOSPACE;
                    break;
                default:
                    lastElement=MorseMessage.mCode.WOSPACE;
                    //If MorseInput is not receiving input, add WOSPACE
                    addElement();
                    //If there have been 2 WOSPACE in a row, assume that the input is done
                    checkEnd();
                    break;
            }

            timeSpace=0;

            //If input is done, end timers
            if (checkDone) {
                deleteMe();
            }
        }
    }




    //Timer handlers

    //Length of hold
    private void heldLength() {
        timeHeld+=1;
    }

    //Length of space
    private void spaceLength() {
        timeSpace+=1;
        if (timeSpace>=14) {
            //If MorseInput is not receiving input, add WOSPACE
            lastElement=MorseMessage.mCode.WOSPACE;
            addElement();
            //If there have been 2 WOSPACE in a row, assume that the input is done
            checkEnd();
        }

        //If input is done, end timers
        if (checkDone) {
            deleteMe();
        }
    }

    //Check to see if signal has likely ended
    private void checkEnd() {
        if (elementList.length>=2) {
            if (elementList[elementList.length-2] == MorseMessage.mCode.WOSPACE && elementList[elementList.length-1] == MorseMessage.mCode.WOSPACE) {
                checkDone=true;
            }
        }
        else if (timeSpace >=14)
            checkDone=true;
    }

    //End both timers
    private void deleteMe() {
        held.cancel();
        sinceHold.cancel();
    }



    //Manipulation of elementList

    //Add the last element into elementList
    private void addElement() {
        elementList=push(elementList, lastElement);
    }


    //Internal utility methods

    //Push an int onto the end of an int array
    private MorseMessage.mCode[] push(MorseMessage.mCode[] a, MorseMessage.mCode n) {
        MorseMessage.mCode[] result=new MorseMessage.mCode[a.length+1];
        for (int m=0; m<a.length; m++) {
            result[m]=a[m];
        }

        result[a.length]=n;

        return result;
    }



    //Public accessor methods

    public boolean isEnded() {
        return checkDone;
    }

    public int[] getElementList() {
        int [] result=new int[elementList.length];
        int index=0;
        for (MorseMessage.mCode each: elementList) {
            result[index]=each.gV();
            index++;
        }

        return result;
    }


}