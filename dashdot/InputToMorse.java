package sam.dashdot;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.content.Intent;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.List;

/**********************************************
 DashDot
 A Morse Code translating, sending, and receiving app for Android

 Author: Sam George
 Date: 11/17/15
 Language: Java

 TO-DO:

 InputToMorse class

 Name:           InputToMorse
 Description:    Process Morse message input translate Morse messages
 Note:
 Author:         Sam George
 Structure:      Global Variables
                 Methods
                    Timer methods: used when user begins manual Morse message input; checks periodically to see if the message is over
                    View event handlers (primarily buttons)
                    Text output
                    Morse input processing (begin and end Morse elements)
                    Morse output (primarily visual blinking)
                    Network connection: register activity as a DashDot instance on a network, send Morse messages over network to/from registered device

 Current functionality:
                Morse-To-Text Translation
                    *User touches BEEP button to begin a Morse Message
                        *All playerListener objects display that a signal is being sent (square changes color, camera flashes, etc.)
                        *mi (MorseInput instance) keeps track of how long button is held
                    *User releases BEEP button to end a Morse element (a dot or dash)
                        *playerListener objects stop signals
                        *mi adds element to an array of current Morse elements
                        *mi keeps track of how long BEEP button is not pressed again; length determines if the next press is part of the same letter or same word
                    *When BEEP is not touched for the length of two word spaces, message is translated
                        *mi uses an instance of MorseMessage to translate dots and dashes into text
                        *translated message is displayed for user
                Text-To-Morse Translation
                    *User types a message into a field
                    *User touches TRANSLATE button
                        *A static MorseMessage method is used to translate the text into Morse elements
                        *playerListener objects are used to display Morse elements to user (square changes color, camera flashes, etc.)

 **********************************************/

public class InputToMorse extends AppCompatActivity {

    //Global variables

    //Accept and process Morse input and String input
    MorseInput mi;
    //Holds the objects used to playback a Morse message
    List<MorsePlayerListener> playerListeners;
    //Controls playback of Morse message
    MorsePlayerInitiator init;

    //Views
        Button beep, translate, ref;
        CheckBox chDisplay, chFlash, chVibrate;
        EditText input;
        String sInput;
        ImageView iOutput;
        Timer checkInputEnd;
        Timer checkPlaying;
        TextView output;
        Spinner sSpinner;

    //State information
        //Ready to accept a Morse message
        boolean messageStart=true;
        //Are Morse Players Listener Objects are being used to output a message
        boolean isPlayingMorse=false;
        //Is a given Morse message finished being inputed by user
        final Handler checkEnd=new Handler();
        //Is a Morse message currently being played
         final Handler checkPlayingDone=new Handler();
        //Is the given morse player being used to output Morse messages
        boolean usingDisplay=true;
        boolean usingFlash=false;
        boolean usingVibrate=false;



    //Needed values
        //Hard=coded element interval values
        int DEFAULT_INTERVAL=200;
        int MEDIUM_VALUE=100;
        int FAST_VALUE=80;
        //Currently-used interval
        int currentInterval=DEFAULT_INTERVAL;


    //Methods

    //Timer methods
        //Is a Morse message finished being input?
        //Runs each timer tick
        private Runnable checkEndRun = new Runnable() {
            @Override
            public void run() {
                if (mi.checkDone == true) {
                    readText();
                    messageStart=true;
                    isPlayingMorse=false;
                    //checkInputEnd.cancel();
                    //checkInputEnd.purge();
                }
            }
        };

        //Holds the task for each timer tick
        public class CheckHeld extends TimerTask {
            @Override
            public void run() {
                if (mi.checkDone == true) {
                    checkEnd.post(checkEndRun);
                    checkInputEnd.cancel();
                    checkInputEnd.purge();
                }
            }
        }

        //Is a Morse message finished playing?
        //If so, enable checkboxes
        //Runs each timer tick
        private Runnable checkPlayingRun = new Runnable() {
            @Override
            public void run() {
                if (init !=null && !init.isPlaying()) {
                    enableCheckboxes();
                    checkPlaying.cancel();
                    checkPlaying.purge();
                }
            }
        };

        //Holds the task for each timer tick
        public class CheckPlaying extends TimerTask {
            @Override
            public void run() {
                    checkPlayingDone.post(checkPlayingRun);
            }
        }


    //View event handlers

        //Translate the text put into the box
        public void onClickbTranslate(View v) {

            translate= (Button) v;
            //If (MorseMessageInitiator) init is not currently playing a Morse message
            if (init  !=null && init.isPlaying())
                return;
            else {
                hideKB(input);
                sInput = input.getText().toString();
                isPlayingMorse = true;
                playMorseMessage(MorseMessage.mTranslate(sInput));
            }
        }

        //Handle soft keyboard input
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent ev) {
            switch (keyCode) {
                case EditorInfo.IME_ACTION_DONE:
                    if (init  !=null && init.isPlaying())
                        return true;
                    else {
                        hideKB(input);
                        sInput = input.getText().toString();
                        isPlayingMorse = true;
                        playMorseMessage(MorseMessage.mTranslate(sInput));
                    }
                    return true;
                //In case the keyboard options are changed; for now, enter key is not allowed
                case KeyEvent.KEYCODE_ENTER:
                    if (init  !=null && init.isPlaying())
                        return true;
                    else {
                        hideKB(input);
                        sInput = input.getText().toString();
                        isPlayingMorse = true;
                        playMorseMessage(MorseMessage.mTranslate(sInput));
                    }
                    return true;
                default:
                    return super.onKeyUp(keyCode, ev);
            }
        }

        //Hide soft keyboard
        public void hideKB(View v) {
            if (v.requestFocus()) {
                InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }

        //bBeep listener
        //Input Morse code
        public class BeepListener implements View.OnTouchListener {

            public boolean onTouch(View v, MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    if (messageStart==true) {
                        mi=new MorseInput(currentInterval);
                        beepStart();
                        //if (isPlayingMorse==false) {
                            for (MorsePlayerListener lstn : playerListeners) {
                                lstn.elementStarted();
                            }
                        //}
                        messageStart=false;
                    } else {
                        beepStart();
                        //if (isPlayingMorse==false) {
                            for (MorsePlayerListener lstn : playerListeners) {
                                lstn.elementStarted();
                            }
                        //}
                    }
                } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                   beepEnd();
                    //if (isPlayingMorse==false) {
                        for (MorsePlayerListener lstn : playerListeners) {
                            lstn.elementStopped();
                        }
                    //}
                }
                return true;
            }

        }


        //Open MorseReference to display Morse dictionary
        public void onClickbReference(View v) {
            Intent i=new Intent(this, MorseReference.class);
            startActivity(i);
        }

        //NOT FUNCTIONAL YET
        //Find existing service on network
        //Look at http://www.jayrambhia.com/blog/android-wireless-connection-2/ for process
        public void onClickbConnect(View v) {
            //if (nc == null)
            //    nc=new NetworkConnection(SERVICE_NAME, this);
            //nc.discoverService();
        }


        //Check Box listeners
        //Resets playerListerners based on current selection(s)
        //Disabled when a message is playing
        public class CheckListener implements View.OnClickListener{
            public void onClick(View v) {
                //If a message is currently being played, stop playing
                if (init!=null && init.isPlaying()) {
                    init.stop();
                }
                //Assume none of the options are selected
                usingDisplay=false;
                usingFlash=false;
                usingVibrate=false;
                //As selected options are found, set usingX to true
                if (chDisplay.isChecked()) {
                    usingDisplay=true;
                }
                if (chFlash.isChecked()) {
                    usingFlash=true;
                }
                if (chVibrate.isChecked()) {
                    usingVibrate=true;
                }
                //Recreate playerListeners
                setMorsePlayers();
            }
        }

    //Display text

        //Display the translation of the input Morse message
        public void readText() {
            output.setText(MorseMessage.mTranslate(mi.getElementList()));
        }


    //Morse Message input

        //Start a Morse element
        public void beepStart() {
            mi.endSpace();
            mi.startHold();
        }

        //End a Morse element
        public void beepEnd() {
            mi.endHold();
            mi.startSpace();

            //If message is over, stop checkInputEnd (that is, stop checking to see if message is over)
            if (checkInputEnd != null) {
                checkInputEnd.cancel();
                checkInputEnd.purge();
            }
            checkInputEnd=new Timer();

            //If message is not over, start the gap for a new element
            if (mi.checkDone==false) {
                checkInputEnd.schedule(new CheckHeld(), currentInterval, currentInterval);
            }

        }


    //Morse Message output

        //Set Morse Message listeners to MorsePlayerInitiator
        private void setMorsePlayers() {
            //Release all resources currently used by playerListeners, if any
            if (playerListeners != null && !playerListeners.isEmpty())
                for (MorsePlayerListener lstn: playerListeners) {
                    lstn.releaseAll();
                }
            //Create playerListeners
            playerListeners=new ArrayList<>();
            //Set playerListeners
            if (usingDisplay) {
                MorsePlayer_Display playerDisplay = new MorsePlayer_Display(iOutput, this);
                playerListeners.add(playerDisplay);
            }
            if (usingFlash) {
                MorsePlayer_Flash playerFlash = new MorsePlayer_Flash(this);
                playerListeners.add(playerFlash);
            }
            if (usingVibrate) {
                MorsePlayer_Vibrator playerVibrate = new MorsePlayer_Vibrator(this);
                playerListeners.add(playerVibrate);
            }
        }

        //Play the given MorseMessage in the current playerListeners
        private void playMorseMessage(int[]morseMessage) {
            setMorsePlayers();
            init = new MorsePlayerInitiator(playerListeners, morseMessage, currentInterval);

            //While message is playing, disable check boxes
            disableCheckboxes();
            checkPlaying=new Timer();
            checkPlaying.schedule(new CheckPlaying(), currentInterval, 1);

            //Start playing the message
            init.play();
        }

        //Disable checkboxes to prevent alterations to playerListener while messages are playing
        public void disableCheckboxes() {
            chDisplay.setEnabled(false);
            chFlash.setEnabled(false);
            chVibrate.setEnabled(false);
        }

        //Enable checkboxes to allow alteration to playerListener once a message is finished playing
        public void enableCheckboxes() {
            chDisplay.setEnabled(true);
            chFlash.setEnabled(true);
            chVibrate.setEnabled(true);
        }

    //Connect to LAN network (NOT FUNCTIONAL YET)
    //Maybe create a timer loop that periodically checks for new input?

        //Network members
        NetworkConnection nc;
        String SERVICE_NAME="DashDot";
        String incomingSignal;


        //Send data to target over network
        public void sendData(String data) {
            try {
                if (nc != null && nc.getPort() != -1) {
                    SendSignal ss = new SendSignal();
                    ss.sendSignal(new Socket(nc.getAddr(), nc.getPort()), data);
                }
            } catch (IOException ex) {
                output.setText("Error in sending message");
            }
        }

        //Register service on network and start listening for messages
        //MUST FIGURE OUT HOW TO RETRIEVE SIGNAL WITHOUT HALTING PROGRAM
        public void registerToReceive() {
            if (nc == null)
                nc=new NetworkConnection(SERVICE_NAME, this);
            nc.registerService();
            ReceiveSignal rs=new ReceiveSignal();
            rs.receiveSignal(nc.getMyServerSocket());
        }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_to_morse);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //New code starts here

        //Set iBlink to iOutput
        iOutput= (ImageView) findViewById(R.id.iBlink);

        //Initialize mi
        mi=new MorseInput(200);


        //Set bBeep to its listener
        beep= (Button) findViewById(R.id.bBeep);
        beep.setOnTouchListener(new BeepListener());

        //Set tOutput to output
        output=(TextView) findViewById(R.id.tOutput);

        //Set tTranslateInput to input
        input=(EditText) findViewById(R.id.tTranslateInput);

        //Set bRef to ref MIGHT NOT BE NECESSARY
        //ref=(Button) findViewById(R.id.bRef);

        //Set sSpinner to sSpeed and set listener
        sSpinner=(Spinner) findViewById(R.id.sSpeed);
        //Speed options hard-coded in
        sSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String chosen = (String) parent.getItemAtPosition(position);
                switch (chosen) {
                    case "Slow":
                        currentInterval = DEFAULT_INTERVAL;
                        break;
                    case "Medium":
                        currentInterval = MEDIUM_VALUE;
                        break;
                    case "Fast":
                        currentInterval = FAST_VALUE;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });

        //Set check boxes (ch[display, flash, vibrate, etc]) to views and set default choices
        //All options that must exist in hardware to be used are by default not checked
        chDisplay=(CheckBox) findViewById(R.id.cDisplay);
        chFlash=(CheckBox) findViewById(R.id.cFlash);
        chVibrate=(CheckBox) findViewById(R.id.cVibrate);
        chDisplay.setOnClickListener(new CheckListener());
        chFlash.setOnClickListener(new CheckListener());
        chVibrate.setOnClickListener(new CheckListener());
        chDisplay.setChecked(true);
        chFlash.setChecked(false);
        chVibrate.setChecked(false);


        //Create necessary Morse Players and add them to playerListeners
        setMorsePlayers();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_input_to_morse, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (init != null) {
            init.stop();
        }
        if (playerListeners!=null && !playerListeners.isEmpty())
            for(MorsePlayerListener lstn:playerListeners)
                lstn.releaseAll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (init != null) {
            init.stop();
        }
        if (playerListeners!=null && !playerListeners.isEmpty())
            for(MorsePlayerListener lstn:playerListeners)
                lstn.releaseAll();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setMorsePlayers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMorsePlayers();
    }
}
