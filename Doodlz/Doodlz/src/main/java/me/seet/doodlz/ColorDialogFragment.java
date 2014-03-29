// ColorDialogFragment.java
// Allows user to set the drawing color on the DoodleView
package me.seet.doodlz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

// class for the Select Color dialog
public class ColorDialogFragment extends DialogFragment {
    private SeekBar mAlphaSeekBar;
    private SeekBar mRedSeekBar;
    private SeekBar mGreenSeekBar;
    private SeekBar mBlueSeekBar;
    private View mColorView;
    private int mColor;
    // OnSeekBarChangeListener for the SeekBars in the color dialog
    private SeekBar.OnSeekBarChangeListener mColorChangedListener = new SeekBar.OnSeekBarChangeListener() {
        // display the updated color
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser)    // user, not program, changed SeekBar progress
                mColor = Color.argb(mAlphaSeekBar.getProgress()
                        , mRedSeekBar.getProgress()
                        , mGreenSeekBar.getProgress()
                        , mBlueSeekBar.getProgress());

            mColorView.setBackgroundColor(mColor);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public ColorDialogFragment() {
        // Required empty public constructor
    }

    // create an AlertDialog and return it
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View colorDialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_color, null);
        builder.setView(colorDialogView);   // add GUI to dialog

        // set the AlertDialog's message
        builder.setTitle(R.string.title_color_dialog);
        builder.setCancelable(true);

        // get the color SeekBars and set their onChange listeners
        mAlphaSeekBar = (SeekBar)colorDialogView.findViewById(R.id.alphaSeekBar);
        mRedSeekBar = (SeekBar)colorDialogView.findViewById(R.id.redSeekBar);
        mGreenSeekBar = (SeekBar)colorDialogView.findViewById(R.id.greenSeekBar);
        mBlueSeekBar = (SeekBar)colorDialogView.findViewById(R.id.blueSeekBar);
        mColorView = colorDialogView.findViewById(R.id.colorView);

        // register SeekBar event listeners
        mAlphaSeekBar.setOnSeekBarChangeListener(mColorChangedListener);
        mRedSeekBar.setOnSeekBarChangeListener(mColorChangedListener);
        mGreenSeekBar.setOnSeekBarChangeListener(mColorChangedListener);
        mBlueSeekBar.setOnSeekBarChangeListener(mColorChangedListener);

        // use current drawing color to set SeekBar values
        final DoodleView doodleView = getDoodleFragment().getDoodleView();
        mColor = doodleView.getDrawingColor();
        mAlphaSeekBar.setProgress(Color.alpha(mColor));
        mRedSeekBar.setProgress(Color.red(mColor));
        mGreenSeekBar.setProgress(Color.green(mColor));
        mBlueSeekBar.setProgress(Color.blue(mColor));

        // add Set Color Button
        builder.setPositiveButton(R.string.button_set_color,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doodleView.setDrawingColor(mColor);
                    }
                }
        );
        return builder.create();
    }

    // tell DoodleFragment that dialog is now displayed
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        DoodleFragment fragment = getDoodleFragment();

        if(fragment != null)
            fragment.setDialogOnScreen(true);
    }

    // tell DoodleFragment that dialog is no longer displayed
    @Override
    public void onDetach() {
        super.onDetach();
        DoodleFragment fragment = getDoodleFragment();

        if(fragment != null)
            fragment.setDialogOnScreen(false);
    }

    private DoodleFragment getDoodleFragment() {
        return (DoodleFragment)getFragmentManager().findFragmentById(R.id.doodleFragment);
    }
}
