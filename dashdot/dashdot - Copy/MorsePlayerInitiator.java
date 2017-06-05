package sam.dashdot;


import android.os.Handler;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*
Name:           MorsePlayerInitiator
Description:    Uses MorsePlayer implementations to output Morse code visually and in audio
Note:
Author:         Sam George
 */

public class MorsePlayerInitiator {

    //Global variables
        //Keeps the next code from being played before the previous has finished
        private Timer gapTimer;
        //List of objects that output Morse messages
        private List<MorsePlayerListener> listeners;
        //The message to play
        private int[] morseArray;
        //The next element which will be played
        private int nextCode;
        //Has the previous code finished playing?
        private boolean isGapTimerDone=true;
        //Has a needed delay between played elements occurred?
        private boolean hasDelayed=false;
        //Is this currently playing a Morse message?
        private boolean amIPlaying=false;
        //The time that the current element should stop playing
        private long elEndTime=0;
        //The length of one Morse dot element
        private int interval;

        //Non-changing private classes
        //Handler for event that occurs each timer tick
        private final Handler tickHandle=new Handler();
        //Runs each timer tick
        private Runnable tickRun = new Runnable() {
            @Override
            public void run() {
                if (isGapTimerDone && elEndTime<=System.nanoTime() && nextCode < morseArray.length) {
                    endSignal();

                    if (!hasDelayed) {
                        insertDelay(interval);
                        hasDelayed=true;
                    }
                    else {
                        isGapTimerDone=false;
                        hasDelayed=false;
                        doNextCode(interval, nextCode);
                        nextCode++;
                    }
                }
                else if (isGapTimerDone && elEndTime<=System.nanoTime() && nextCode >= morseArray.length) {
                    endSignal();
                    gapTimer.cancel();
                    amIPlaying=false;
                }
            }
        };
        //Holds the task for each timer tick
        private class Tick extends TimerTask {
            @Override
            public void run() {
                tickHandle.post(tickRun);
            }
        }


    //Methods

    //Constructor
        MorsePlayerInitiator(List<MorsePlayerListener> newListeners, int[] newMorseArray, int nInterval) {
            listeners=newListeners;
            morseArray=newMorseArray;
            interval=nInterval;
        }

    //Play one element of morseArray and calculate end time of that element
        private void doNextCode(int interval, int code) {

            switch (morseArray[code]) {
                case -1:
                    elEndTime=interval*1000000*3+System.nanoTime();
                    break;
                case 1:
                    for (MorsePlayerListener lstn: listeners) {
                        lstn.elementStarted();
                    }
                    elEndTime=interval*1000000+System.nanoTime();
                    break;
                case 3:
                    for (MorsePlayerListener lstn: listeners) {
                        lstn.elementStarted();
                    }
                    elEndTime=interval*1000000*3+System.nanoTime();
                    break;
                case 7:
                    elEndTime=interval*1000000*7+System.nanoTime();
                    break;
            }

        isGapTimerDone=true;
    }

    //Stop playing the current element
        public void endSignal() {
            for (MorsePlayerListener lstn: listeners) {
                lstn.elementStopped();
            }
        }

    //Delay the time before the next element
        private void insertDelay(int interval) {
            elEndTime+=1000000*interval;
        }

    //Release objects used by listeners
        void releaseListenerObjects() {
            for (MorsePlayerListener lstn: listeners) {
                lstn.releaseAll();
            }
        }

    //Start playing morseArray through listeners
        public void play() {

            //If there is not at least one element in morseArray, release listeners' objects and return
            if (morseArray.length>0) {
                amIPlaying=true;
                nextCode = 0;
                //If there is at least one element, instantiate gapTimer
                gapTimer = new Timer();

                //Check every millisecond if an element must be played or ended
                //The check should run as constantly as possible
                gapTimer.schedule(new Tick(), 1, 1);
            }
            else {
                //releaseListenerObjects();
                amIPlaying=false;
                return;
            }

        }

    //Prematurely stop playing morseArray through listeners
        public void stop() {
            if (gapTimer != null) {
                gapTimer.cancel();
                gapTimer.purge();
            }
            amIPlaying=false;
        }

    //Returns true if this is currently playing a Morse message
        public boolean isPlaying() {
            return amIPlaying;
        }
}
