package com.example.musicalsafe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    int numClicks;
    private Button tapButton;
    PressAuthenticator pressAuth;
    private float[] lastTouchLocation;

    boolean isAllFabsVisible;
    boolean enrollmentOngoing;
    FloatingActionButton baseFab, enrollmentFab, endEnrollmentFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO, restart PressAuthenticator per enrollment instance (see same message bellow)
        pressAuth = new PressAuthenticator(getApplicationContext());
        pressAuth.readClickEnrollment(); //probably not safe, should be done differently
        lastTouchLocation = new float[2];

        tapButton = findViewById(R.id.tapButton);
        tapButton.setOnTouchListener(touchButtonListener);
        tapButton.setOnClickListener(tapButtonListener);

        baseFab = findViewById(R.id.add_fab);
        enrollmentFab = findViewById(R.id.add_alarm_fab); //TODO: rename fabs from example @ -> https://www.geeksforgeeks.org/floating-action-button-fab-in-android-with-example/
        endEnrollmentFab = findViewById(R.id.add_person_fab);

        enrollmentFab.setVisibility(View.GONE);
        endEnrollmentFab.setVisibility(View.GONE);

        isAllFabsVisible = false;

        baseFab.setOnClickListener(baseFabListener);
        enrollmentFab.setOnClickListener(enrollmentFabListener);
        endEnrollmentFab.setOnClickListener(endEnrollmentFabListener);
    }

    View.OnClickListener baseFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isAllFabsVisible) {
                enrollmentFab.show();
                endEnrollmentFab.show();
                isAllFabsVisible = true;
            }
            else {
                enrollmentFab.hide();
                endEnrollmentFab.hide();
                isAllFabsVisible = false;
            };
        }
    };

    View.OnClickListener enrollmentFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(MainActivity.this, "Enrollmentgood Added", Toast.LENGTH_SHORT
            ).show();
            enrollmentOngoing = true;
        }
    };

    //Not yet needed so no yet implemented
    View.OnClickListener endEnrollmentFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            pressAuth.saveClickEnrollment();
            enrollmentOngoing = false;
        }
    };

    //View.OnClickListener

    View.OnTouchListener touchButtonListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastTouchLocation[0] = motionEvent.getX();
                lastTouchLocation[1] = motionEvent.getY();
            }

            return false;
        }
    };

    View.OnClickListener tapButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //No record of user not enrollment not initiated
            //Will change later to enter password phase and enrollment triggered later
//            if (!enrollmentOngoing) //tested and good
//                return;

            numClicks += 1;
            if (pressAuth.getNumPresses() > 0) {
                pressAuth.updateLastPress(System.currentTimeMillis());
            }

            PressRecord newRecord = new PressRecord (
                    lastTouchLocation[0],
                    lastTouchLocation[1],
                    System.currentTimeMillis()
            );

            pressAuth.addPressRecord(newRecord); //TODO, restart PressAuthenticator per enrollment instance

            if (pressAuth.getNumPresses() >= 3) //TODO, take this out fter interrupt is done.
                if (pressAuth.authEnrollmentInstance())
                    Toast.makeText(getApplicationContext(), "Authorized", Toast.LENGTH_SHORT).show();
        }
    };
}