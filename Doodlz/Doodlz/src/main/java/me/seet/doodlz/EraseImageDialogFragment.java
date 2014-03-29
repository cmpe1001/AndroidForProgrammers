// EraseImageDialogFragment.java
// Allows user to erase image
package me.seet.doodlz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

// class for the Select Color dialog
public class EraseImageDialogFragment extends DialogFragment {
    // create an AlertDialog and return it

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // set the AlertDialog's message
        builder.setMessage(R.string.message_erase);
        builder.setCancelable(false);

        // add Erase button
        builder.setPositiveButton(R.string.button_erase, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDoodleFragment().getDoodleView().clear();    // clear image
            }
        });

        return builder.create();    // return dialog
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
