package sam.dashdot;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**********************************************
 MorseReference class

 Name:           InputToMorse
 Description:    Display Morse code with alphanumeric equivalent as saved in project resource file (values.strings)
 Note:
 Author:         Sam George

 **********************************************/

public class MorseReference extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set text to display
        setContentView(R.layout.activity_morse_reference);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
