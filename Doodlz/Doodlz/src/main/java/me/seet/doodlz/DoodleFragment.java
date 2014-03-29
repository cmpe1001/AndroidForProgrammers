package me.seet.doodlz;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.*;

// DoodleFragment.java
// Fragment in which the DoodleView is displayed
public class DoodleFragment extends Fragment {
    private DoodleView mDoodleView; // handles touch events and draws
    private float mAcceleration;
    private float mCurrentAcceleration;
    private float mLastAcceleration;
    private boolean mDialogOnScreen = false;

    // value used to determine whether user shook the device to erase
    private static final int ACCELERATION_THRESHOLD = 100000;

    // event handler for accelerometer events
    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        // use accelerometer to determine whether user shook device
        @Override
        public void onSensorChanged(SensorEvent event) {
            // ensure that other dialogs are not displayed
            if(!mDialogOnScreen) {
                // get x, y, and z values for the SensorEvent
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                // save previous acceleration value
                mLastAcceleration = mCurrentAcceleration;

                // calculate the current acceleration
                mCurrentAcceleration = x * x + y * y + z * z;

                // calculate the change in acceleration
                mAcceleration = mCurrentAcceleration * (mCurrentAcceleration - mLastAcceleration);

                // if the acceleration is above a certain threshold
                if(mAcceleration > ACCELERATION_THRESHOLD)
                    confirmErase();
            }
        }

        // required method of interface SensorEventListener
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    public DoodleFragment() {
        // Required empty public constructor
    }

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =  inflater.inflate(R.layout.fragment_doodle, container, false);

        setHasOptionsMenu(true);;

        // get reference to the DoodleView
        mDoodleView = (DoodleView)view.findViewById(R.id.doodleView);

        // initialize acceleration values
        mAcceleration = 0.00f;
        mCurrentAcceleration = SensorManager.GRAVITY_EARTH;
        mLastAcceleration = SensorManager.GRAVITY_EARTH;

        return view;
    }

    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.doodle_fragment_menu, menu);
    }

    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // switch based on the MenuItem id
        switch (item.getItemId()) {
            case R.id.color:
                ColorDialogFragment colorDialog = new ColorDialogFragment();
                colorDialog.show(getFragmentManager(), "color dialog");
                return true;    // consume the menu event
            case R.id.lineWidth:
                LineWidthDialogFragment widthDialog = new LineWidthDialogFragment();
                widthDialog.show(getFragmentManager(), "line width dialog");
                return true;    // consume the menu event
            case R.id.eraser:
                mDoodleView.setDrawingColor(Color.WHITE);   // line color white
                return true;    // consume the menu event
            case R.id.clear:
                confirmErase();
                return true;    // consume the menu event
            case R.id.save:
                mDoodleView.saveImage();    // save the current image
                return true;    // consume the menu event
            case R.id.print:
                mDoodleView.printImage();   // print the current images
                return true;    // consume the menu event
        }

        return super.onOptionsItemSelected(item);   // call super's method
    }

    // start listening for sensor events
    @Override
    public void onStart() {
        super.onStart();
        enableAccelerometerListening(); // listen for shake
    }

    // stop listening for sensor events
    @Override
    public void onPause() {
        super.onPause();
        disableAccelerometerListening();    // stop listening for shake
    }

    // returns the DoodleView
    public DoodleView getDoodleView() {
        return mDoodleView;
    }

    // indicates whether a dialog is displayed
    public void setDialogOnScreen(boolean visible) {
        mDialogOnScreen = visible;
    }

    // confirm whether image should be erased
    private void confirmErase() {
        EraseImageDialogFragment fragment = new EraseImageDialogFragment();
        fragment.show(getFragmentManager(), "erase dialog");
    }

    // enable listening for accelerometer events
    private void enableAccelerometerListening() {
        // get the SensorManager
        SensorManager sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);

        // register to listen for accelerometer events
        sensorManager.registerListener(mSensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    // disable listening for accelerometer events
    private void disableAccelerometerListening() {
        // get the SensorManager
        SensorManager sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);

        // stop listening for accelerometer events
        sensorManager.unregisterListener(mSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }
}
