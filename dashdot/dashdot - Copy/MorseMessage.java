package sam.dashdot;
import java.util.ArrayList;

/*
Name:           MorseMessage
Description:    Class to hold a message in Morse code (as an in array) and translate messages
Note:           Message String characters are entirely in unicode (since Java stores chars as unicode anyway, this is not relevent except for special chars)
                Morse as int array:
                    Certain values indicate a dot ("di"), dash ("dah"), or wait between characters.
                        Encoding of ints (defined as mCode enum):
                            -1: letter space (LESPACE)
                            1: dot ("di") (DOT)
                            3: dash ("dah") (DASH)
                            7: word space (WOSPACE)
                    Thus, the length of the array does NOT, in itself, indicate how long it will take the message to transmit.
                    Instead, the sum of the absolute values of the elements will give the time, in units, that the message takes to transmit
                        Taken from http://morsecode.scphillips.com/morse.html (retrieved 10/14/15):
                            "If the duration of a dot is taken to be one unit then that of a dash is three units.
                            The space between the components of one character is one unit, between characters is three units and between words seven units.
                            To indicate that a mistake has been made and for the receiver to delete the last word, send di-di-di-di-di-di-di-dit (eight dots).
                            "The prosigns are combinations of two letters sent together with no space in between.
                            The other abbreviations and Q codes are sent with the normal spacing."
                Prosigns: assumed to be preceded and followed by escape char "\"
Author:         Sam George
Members:        int aMorse[]                        Morse code in an int array
                ArrayList<Integer> parsedMorse      Morse code divided into arrays based on word spaces
                String sMessage                     Translation of aMorse into String
                enum mCode                          Enumerations used for Morse code elements
                        LESPACE: -1, end of letter
                        DOT: 1, dot/di
                        DASH: 3, dash/dah
                        WOSPACE: 7, end of word
                        access method: getValue()
Methods:        
TO-DO:          Handle "ch"
                Handle spaces in morse -> String converter
 */

public class MorseMessage {

    //Raw Morse code int array
    private int[] aMorse=new int[0];

    //Morse code divided into words represented by Integer arrays
    //Used internally to allow word deletion
    private ArrayList<int[]> parsedMorse=new ArrayList<> ();

    //String equivalent of Morse int array
    private String sMessage="";

    public enum mCode {
        LESPACE(-1),
        DOT(1),
        DASH(3),
        WOSPACE(7);

        private int value;
        private mCode(int value) {
            this.value=value;
        }

        public int gV() {
            return value;
        }
    }

    //Constructors
    //Constructor given a morse message
    MorseMessage(int[] newMorse) {
        aMorse=newMorse;
        sMessage=mTranslate(aMorse);
    }

    //Constructor given a String message
    MorseMessage(String newMessage) {
        sMessage=newMessage;
        aMorse=mTranslate(sMessage);
    }



    //Public, static translation methods
    //Translate an entire Morse message into a String
    public static String mTranslate(int[] aMorse) {
        String result="";

        ArrayList<int[]> letterMorse=mParse(aMorse);

        for (int[] each : letterMorse) {
            result+=toMessageLetter(each);
        }

        //Cut off space at end of last word; assumes WOSPACE is last morse element
        /*if (result.length() > 2) {
            result=result.substring(0, result.length()-1);
        }*/

        return result;
    }

    //Parse a Morse message into its component letters
    private static ArrayList<int[]> mParse(int[] aMorse) {
        ArrayList<int[]> result = new ArrayList<>();

        int wordStart=0;
        int letterStart=0;
        int[] toAdd = new int[] {};

        for (int i=0; i<aMorse.length; i++) {
            if (aMorse[i]==mCode.LESPACE.gV()) {
                for (int n=letterStart; n<i; n++) {
                    toAdd=push(toAdd, aMorse[n]);
                }
                letterStart=i+1;
                result.add(toAdd);
                toAdd=new int[] {};
            }
            else if (aMorse[i]==mCode.WOSPACE.gV()) {
                for (int n=letterStart; n<i; n++) {
                    toAdd=push(toAdd, aMorse[n]);
                }
                letterStart=i+1;
                result.add(toAdd);
                //Add space after word
                toAdd=new int[] {aMorse[i]}; //should be WOSPACE
                result.add(toAdd);
                toAdd=new int[] {};
            }
        }

        for (int n=letterStart; n<aMorse.length; n++) {
            toAdd=push(toAdd, aMorse[n]);
        }
        result.add(toAdd);

        return result;
    }


    //Translate and entire String message into Morse
    public static int[] mTranslate(String message) {
        int[] result=new int[0];

        //Morse-encoded equivalent to a single char of message
        int[] letter=new int[0];

        //Offset used to read prosigns
        int offset=0;
        char[] prosign=new char[0];

        //For every char in message...
        int n=0;
        while (n<message.length()) {
            //If an escape character is used, look for a prosign
            if (message.charAt(n) == '\\') {
                offset=1;
                prosign=new char[0];
                while (n+offset < message.length() && message.charAt(n+offset) != '\\') {
                    prosign=push(prosign, message.charAt(n+offset));
                    offset++;
                }
                letter=toProsign(prosign);
                //Add letter space
                letter=push(letter, mCode.LESPACE.gV());
            }
            //If a space is used, add on word space
            else if (message.charAt(n) == ' ') {
                letter= new int[] { mCode.WOSPACE.gV() };
            }
            else { //Translate char into Morse-encoded equivalent
                letter=toMorseLetter(message.charAt(n));
                //Add letter space
                letter=push(letter, mCode.LESPACE.gV());
            }
            //For each element in letter, push element onto result
            for (int m=0; m<letter.length; m++) {
                result=push(result, letter[m]);
            }
            n+=1+offset;
            offset=0;
        }

        return result;
    }



    //Private, letter-by-letter translation methods

    //String to Morse
    //Translate individual letters and prosigns

    private static int[] toMorseLetter(char msgLetter) {
        int[] result=new int[0];

        switch (msgLetter) {
            case 'A':
            case 'a':
                result= new int[] {mCode.DOT.gV(), mCode.DASH.gV()};
                break;
            case 'B':
            case 'b':
                result= new int[] {mCode.DASH.gV(), mCode.DOT.gV(), mCode.DOT.gV(), mCode.DOT.gV()};
                break;
            case 'C':
            case 'c':
                result= new int[] {mCode.DASH.gV(), mCode.DOT.gV(), mCode.DASH.gV(), mCode.DOT.gV()};
                break;
            case 'D':
            case 'd':
                result= new int[] {mCode.DASH.gV(), mCode.DOT.gV(), mCode.DOT.gV()};
                break;
            case 'E':
            case 'e':
                result= new int[] {mCode.DOT.gV()};
                break;
            case 'F':
            case 'f':
                result= new int[] {mCode.DOT.gV(), mCode.DOT.gV(),mCode.DASH.gV(), mCode.DOT.gV()};
                break;
            case 'G':
            case 'g':
                result= new int[] {mCode.DASH.gV(), mCode.DASH.gV(), mCode.DOT.gV()};
                break;
            case 'H':
            case 'h':
                result= new int[] {mCode.DOT.gV(), mCode.DOT.gV(), mCode.DOT.gV(), mCode.DOT.gV()};
                break;
            case 'I':
            case 'i':
                result= new int[] {mCode.DOT.gV(), mCode.DOT.gV()};
                break;
            case 'J':
            case 'j':
                result= new int[] {mCode.DOT.gV(), mCode.DASH.gV(), mCode.DASH.gV(), mCode.DASH.gV()};
                break;
            case 'K':
            case 'k':
                result= new int[] {mCode.DASH.gV(), mCode.DOT.gV(), mCode.DASH.gV()};
                break;
            case 'L':
            case 'l':
                result= new int[] {mCode.DOT.gV(), mCode.DASH.gV(), mCode.DOT.gV(), mCode.DOT.gV()};
                break;
            case 'M':
            case 'm':
                result= new int[] {mCode.DASH.gV(), mCode.DASH.gV()};
                break;
            case 'N':
            case 'n':
                result= new int[] {mCode.DASH.gV(), mCode.DOT.gV()};
                break;
            case 'O':
            case 'o':
                result= new int[] {mCode.DASH.gV(), mCode.DASH.gV(), mCode.DASH.gV()};
                break;
            case 'P':
            case 'p':
                result= new int[] {mCode.DOT.gV(), mCode.DASH.gV(), mCode.DASH.gV(), mCode.DOT.gV()};
                break;
            case 'Q':
            case 'q':
                result= new int[] {mCode.DASH.gV(), mCode.DASH.gV(), mCode.DOT.gV(), mCode.DASH.gV()};
                break;
            case 'R':
            case 'r':
                result= new int[] {mCode.DOT.gV(), mCode.DASH.gV(), mCode.DOT.gV()};
                break;
            case 'S':
            case 's':
                result= new int[] {mCode.DOT.gV(), mCode.DOT.gV(), mCode.DOT.gV()};
                break;
            case 'T':
            case 't':
                result= new int[] {mCode.DASH.gV()};
                break;
            case 'U':
            case 'u':
                result= new int[] {mCode.DOT.gV(), mCode.DOT.gV(), mCode.DASH.gV()};
                break;
            case 'V':
            case 'v':
                result= new int[] {mCode.DOT.gV(), mCode.DOT.gV(), mCode.DOT.gV(), mCode.DASH.gV()};
                break;
            case 'W':
            case 'w':
                result= new int[] {mCode.DOT.gV(), mCode.DASH.gV(), mCode.DASH.gV()};
                break;
            case 'X':
            case 'x':
                result= new int[] {mCode.DASH.gV(), mCode.DOT.gV(), mCode.DOT.gV(), mCode.DASH.gV()};
                break;
            case 'Y':
            case 'y':
                result= new int[] {mCode.DASH.gV(), mCode.DOT.gV(), mCode.DASH.gV(), mCode.DASH.gV()};
                break;
            case 'Z':
            case 'z':
                result= new int[] {mCode.DASH.gV(), mCode.DASH.gV(), mCode.DOT.gV(), mCode.DOT.gV()};
                break;
            case '\u0040': //@ symbol
                result= new int[] {mCode.DOT.gV(), mCode.DASH.gV(), mCode.DASH.gV(), mCode.DOT.gV(), mCode.DASH.gV(), mCode.DOT.gV()};
                break;

        }

        return result;
    }

    private static int[] toProsign(char[] pros) {
        int[] result=new int[0];
        String check=new String(pros);
        check=check.toUpperCase();
        switch (check) {
            case "AA":
                result= new int[] {mCode.DOT.gV(), mCode.DASH.gV(), mCode.DOT.gV(), mCode.DASH.gV()};
                break;
            case "SOS":
                result= new int[] {mCode.DOT.gV(), mCode.DOT.gV(), mCode.DOT.gV(), mCode.DASH.gV(), mCode.DASH.gV(), mCode.DASH.gV(), mCode.DOT.gV(), mCode.DOT.gV(), mCode.DOT.gV()};
                break;
        }
        return result;
    }

    //Morse to String
    //Translate individual letters and prosigns
    private static String toMessageLetter(int[] msgLetter) {
        String result="";

        switch (msgLetter.length) {
            case 1:
                if (msgLetter[0]==mCode.DOT.gV())
                    result="E";
                else if (msgLetter[0]==mCode.DASH.gV())
                    result="T";
                    //Used to add space at the end of a word
                else if (msgLetter[0]==mCode.WOSPACE.gV())
                    result=" ";
                break;
            case 2:
                if (msgLetter[0]==mCode.DOT.gV()) {
                    if (msgLetter[1]==mCode.DOT.gV())
                        result="I";
                    else if (msgLetter[1]==mCode.DASH.gV())
                        result="A";
                } else if (msgLetter[0]==mCode.DASH.gV()) {
                    if (msgLetter[1]==mCode.DOT.gV())
                        result="N";
                    else if (msgLetter[1]==mCode.DASH.gV())
                        result="M";
                }
                break;
            case 3:
                if (msgLetter[0]==mCode.DOT.gV()) {
                    if (msgLetter[1]==mCode.DOT.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV())
                            result="S";
                        else if (msgLetter[2]==mCode.DASH.gV())
                            result="U";
                    } else if (msgLetter[1]==mCode.DASH.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV())
                            result="R";
                        else if (msgLetter[2]==mCode.DASH.gV())
                            result="W";
                    }
                } else if (msgLetter[0]==mCode.DASH.gV()) {
                    if (msgLetter[1]==mCode.DOT.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV())
                            result="D";
                        else if (msgLetter[2]==mCode.DASH.gV())
                            result="K";
                    } else if (msgLetter[1]==mCode.DASH.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV())
                            result="G";
                        else if (msgLetter[2]==mCode.DASH.gV())
                            result="O";
                    }
                }
                break;
            case 4:
                if (msgLetter[0]==mCode.DOT.gV()) {
                    if (msgLetter[1]==mCode.DOT.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                result="H";
                            else if (msgLetter[3]==mCode.DASH.gV())
                                result="V";
                        } else if (msgLetter[2]==mCode.DASH.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                result="F";
                            else if (msgLetter[3]==mCode.DASH.gV())
                                result="\u00dc"; //Ü
                        }
                    } else if (msgLetter[1]==mCode.DASH.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                result="L";
                            else if (msgLetter[3]==mCode.DASH.gV())
                                result="\u00c4"; //Ä
                        } else if (msgLetter[2]==mCode.DASH.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                result="P";
                            else if (msgLetter[3]==mCode.DASH.gV())
                                result="J";
                        }
                    }
                } else if (msgLetter[0]==mCode.DASH.gV()) {
                    if (msgLetter[1]==mCode.DOT.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                result="B";
                            else if (msgLetter[3]==mCode.DASH.gV())
                                result="X";
                        } else if (msgLetter[2]==mCode.DASH.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                result="C";
                            else if (msgLetter[3]==mCode.DASH.gV())
                                result="Y";
                        }
                    } else if (msgLetter[1]==mCode.DASH.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                result="Z";
                            else if (msgLetter[3]==mCode.DASH.gV())
                                result="Q";
                        } else if (msgLetter[2]==mCode.DASH.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                result="\u00d6"; //Ö
                            else if (msgLetter[3]==mCode.DASH.gV())
                                result="CH";
                        }
                    }
                }
                break;
            case 5:
                if (msgLetter[0]==mCode.DOT.gV()) {
                    if (msgLetter[1]==mCode.DOT.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                ;
                            else if (msgLetter[3]==mCode.DASH.gV())
                                ;
                        } else if (msgLetter[2]==mCode.DASH.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                ;
                            else if (msgLetter[3]==mCode.DASH.gV())
                                ;
                        }
                    } else if (msgLetter[1]==mCode.DASH.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                ;
                            else if (msgLetter[3]==mCode.DASH.gV())
                                ;
                        } else if (msgLetter[2]==mCode.DASH.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                ;
                            else if (msgLetter[3]==mCode.DASH.gV())
                                ;
                        }
                    }
                } else if (msgLetter[0]==mCode.DASH.gV()) {
                    if (msgLetter[1]==mCode.DOT.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                ;
                            else if (msgLetter[3]==mCode.DASH.gV())
                                ;
                        } else if (msgLetter[2]==mCode.DASH.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                ;
                            else if (msgLetter[3]==mCode.DASH.gV())
                                ;
                        }
                    } else if (msgLetter[1]==mCode.DASH.gV()) {
                        if (msgLetter[2]==mCode.DOT.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                ;
                            else if (msgLetter[3]==mCode.DASH.gV()) {
                                if (msgLetter[4]==mCode.DOT.gV())
                                    ;
                                else if (msgLetter[4]==mCode.DASH.gV())
                                    result="\u00d1"; //Ñ
                            }
                        } else if (msgLetter[2]==mCode.DASH.gV()) {
                            if (msgLetter[3]==mCode.DOT.gV())
                                ;
                            else if (msgLetter[3]==mCode.DASH.gV())
                                ;
                        }
                    }
                }
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            default:
                break;
        }

        return result;
    }



    //Public accessor methods

    //Set new message from Morse code
    public void setMessage(int[] newMorse) {
        aMorse=newMorse;
        sMessage=mTranslate(aMorse);
    }

    //Set new message from String
    public void setMessage(String newMorse) {
        sMessage=newMorse;
        aMorse=mTranslate(sMessage);
    }

    //Directly add an element to Morse code array
    //Remember that spaces must manually be pushed onto aMorse
    public void pushElement(mCode nEl) {
        aMorse=push(aMorse, nEl.gV());
        sMessage=mTranslate(aMorse);
}

    //Get Morse code array
    public int[] getAMorse() {
        return aMorse;
    }

    //Get String
    public String getString() {
        return sMessage;
    }


    //Internally methods to support above

    //Push an int onto the end of an int array
    private static int[] push(int[] a, int n) {
        int[] result=new int[a.length+1];
        for (int m=0; m<a.length; m++) {
            result[m]=a[m];
        }

        result[a.length]=n;

        return result;
    }

    //Push an char onto the end of a char array
    private static char[] push(char[] a, char n) {
        char[] result=new char[a.length+1];
        for (int m=0; m<a.length; m++) {
            result[m]=a[m];
        }

        result[a.length]=n;

        return result;
    }
}
