// LineWidthDialogFragment.java
// Allows user to set the drawing color on the DoodleView
package me.seet.doodlz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

// class for the Select Color dialog
public class LineWidthDialogFragment extends DialogFragment {
    private ImageView mWidthImageView;
    private SeekBar.OnSeekBarChangeListener mLineWidthChanged = new SeekBar.OnSeekBarChangeListener() {
        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); // associate with Canvas

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // configure a Paint object for the current SeekBar value
            Paint p = new Paint();
            p.setColor(getDoodleFragment().getDoodleView().getDrawingColor());
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(progress);

            // erase the bitmap and redraw the line
            bitmap.eraseColor(getResources().getColor(android.R.color.transparent));
            canvas.drawLine(30, 50, 370, 50, p);
            mWidthImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public LineWidthDialogFragment() {
        // Required empty public constructor
    }

    // create an AlertDialog and return it
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        View lineWidthDialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_line_width, null);
        builder.setView(lineWidthDialogView);   // add GUI to dialog

        // set the AlertDialog's message
        builder.setTitle(R.string.title_line_width_dialog);
        builder.setCancelable(true);

        // get the ImageView
        mWidthImageView = (ImageView)lineWidthDialogView.findViewById(R.id.widthImageView);

        // configure widthSeekBar
        final DoodleView doodleView = getDoodleFragment().getDoodleView();
        final SeekBar widthSeekBar = (SeekBar)lineWidthDialogView.findViewById(R.id.widthSeekBar);
        widthSeekBar.setOnSeekBarChangeListener(mLineWidthChanged);
        widthSeekBar.setProgress(doodleView.getLineWidth());

        // add Set Line Width Button
        builder.setPositiveButton(R.string.button_set_line_width,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doodleView.setLineWidth(widthSeekBar.getProgress());
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

    // gets a reference to the DoodleFragment
    private DoodleFragment getDoodleFragment() {
        return (DoodleFragment)getFragmentManager().findFragmentById(R.id.doodleFragment);
    }
}
