package sam.dashdot;

/*
Name:           MorsePlayerListener
Description:    Implemented by objects which output morse code
Note:
Author:         Sam George
Methods:
 */

public interface MorsePlayerListener {
    //Start playing a Morse element; ensure object is available
    void elementStarted();
    //Stop playing a Morse element; ensure object is available
    void elementStopped();
    //Release all the objects created by the listener
    void releaseAll();
}
