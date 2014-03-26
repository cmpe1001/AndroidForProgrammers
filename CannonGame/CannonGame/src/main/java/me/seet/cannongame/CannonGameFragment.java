// CannonGameFragment.java
// CannonGameFragment creates and manages a CannonView
package me.seet.cannongame;


import android.app.Fragment;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CannonGameFragment extends Fragment {
    private CannonView mCanonView;  // custom view to display the game
    public CannonGameFragment() {
        // Required empty public constructor
    }

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        // get the CannonView
        mCanonView = (CannonView)view.findViewById(R.id.cannonView);
        return view;
    }

    // set up volume control once Activity is created
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // allow volume keys to set game volume
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    // when MainActivity is paused, CannonGameFragment terminates the game
    @Override
    public void onPause() {
        super.onPause();
        mCanonView.stopGame();  // terminates the game
    }

    // when MainActivity is destroyed, CannonGameFragment releases resources
    @Override
    public void onDestroy() {
        super.onDestroy();
        mCanonView.releaseResources();
    }
}
