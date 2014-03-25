package me.seet.flagquiz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class QuizFragment extends Fragment {
    // String used when logging error messages
    private static final String TAG = "FlagQuiz Activity";

    private static final int FLAGS_IN_QUIZ = 10;

    private List<String> mFileNameList; // flag file names
    private List<String> mQuizCountriesList; // countries in current quiz
    private Set<String> mRegionsSet;    // world regions in current quiz
    private String mCorrectAnswer;      // correct country for the current flag

    private int mTotalGuesses;    // number of guesses made
    private int mCorrectAnswers;  // number of correct guesses
    private int mGuessRows;       // number of rows displaying guess Buttons
    private SecureRandom mRandom;    // used to randomize the quiz
    private Handler mHandler;       // used to delay loading next flag
    private Animation mShakeAnimation;  // animation for incorrect guess

    private TextView mQuestionNumberTextView;   // shows current question #
    private ImageView mFlagImageView;           // displays a flag

    private LinearLayout[] mGuessLinearLayouts;  // rows of answer Buttons
    private TextView mAnswerTextView;           // displays Correct! or Incorrect!

    // called when a guess Button is touched
    private View.OnClickListener mGuessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button guessButton = ((Button)view);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(mCorrectAnswer);
            ++mTotalGuesses;    // increment number of guesses the user has made

            if(guess.equals(answer))    // if the guess is correct
            {
                ++mCorrectAnswers;  // increment the number of correct answers

                // display correct answer in green text
                mAnswerTextView.setText(answer + "!");
                mAnswerTextView.setTextColor(getResources().getColor(R.color.correct_answer));

                disableButtons();   // disable all guess Buttons

                // if the user has correctly identified FLAGS_IN_QUIZ flags
                if(mCorrectAnswers == FLAGS_IN_QUIZ) {
                    // DialogFragment to display quiz stats and start a new quiz
                    DialogFragment quizResults = new DialogFragment() {
                        // create an AlertDialog and return it
                        @Override
                        public Dialog onCreateDialog(Bundle bundle) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setCancelable(false);
                            builder.setMessage(getResources().getString(R.string.results, mTotalGuesses, (1000 / (double)mTotalGuesses)));

                            // "Reset Quiz" Button
                            builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int id) {
                                    resetQuiz();
                                }
                            });
                            return builder.create();
                        }
                    };

                    // user FragmentManager to display the DialogFragment
                    quizResults.show(getFragmentManager(), "quiz results");
                }
                else  // answer is correct but quiz is not over
                {
                    // load the next flag after a 1-second delay
                    mHandler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    loadNextFlag();
                                }
                            }, 2000); // 2000 milliseconds for 2-second delay
                }
            }
            else    // guess was incorrect
            {
                mFlagImageView.startAnimation(mShakeAnimation); // play shake

                // display "Incorrect!" in red
                mAnswerTextView.setText(R.string.incorrect_answer);
                mAnswerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
                guessButton.setEnabled(false);  // disable incorrect answer
            }
        }
    };

    // utility method that disable all answer buttons
    private void disableButtons() {
        for (int row = 0; row < mGuessRows; row++) {
            LinearLayout guessRow = mGuessLinearLayouts[row];
            for (int column = 0; column < guessRow.getChildCount(); column++) {
                guessRow.getChildAt(column).setEnabled(false);
            }
        }

    }

    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_quiz,container,false);

        mFileNameList = new ArrayList<String>();
        mQuizCountriesList = new ArrayList<String>();
        mRandom = new SecureRandom();
        mHandler = new Handler();

        // load the shake animation that's used for incorrect answers
        mShakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.incorrect_shake);
        mShakeAnimation.setRepeatCount(3);  // animation repeats 3 times

        // get references to GUI components
        mQuestionNumberTextView = (TextView)view.findViewById(R.id.questionNumberTextView);
        mFlagImageView = (ImageView)view.findViewById(R.id.flagImageView);
        mGuessLinearLayouts = new LinearLayout[3];
        mGuessLinearLayouts[0] = (LinearLayout)view.findViewById(R.id.row1LinearLayout);
        mGuessLinearLayouts[1] = (LinearLayout)view.findViewById(R.id.row2LinearLayout);
        mGuessLinearLayouts[2] = (LinearLayout)view.findViewById(R.id.row3LinearLayout);
        mAnswerTextView = (TextView)view.findViewById(R.id.answerTextView);

        // configure listeners for the guess Buttons
        for(LinearLayout row : mGuessLinearLayouts) {
            for(int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button)row.getChildAt(column);
                button.setOnClickListener(mGuessButtonListener);
            }
        }

        mQuestionNumberTextView.setText(getResources().getString(R.string.question,1,FLAGS_IN_QUIZ));
        return view;
    }

    // update mGuessRows based on value in SharedPreferences
    public void updateGuessRows(SharedPreferences sharedPreferences) {
        // get the number of guess buttons that should be displayed
        String choices = sharedPreferences.getString(MainActivity.CHOICES, null);
        mGuessRows = Integer.parseInt(choices) / 3;

        // hide all guess button LinearLayouts
        for (LinearLayout layout: mGuessLinearLayouts) {
            layout.setVisibility(View.INVISIBLE);
        }

        // display appropriate guess button LinearLayouts
        for(int row = 0; row < mGuessRows; row++) {
            mGuessLinearLayouts[row].setVisibility(View.VISIBLE);
        }
    }

    // update world regions for quiz based on values in SharedPreferences
    public void updateRegions(SharedPreferences sharedPreferences) {
        mRegionsSet = sharedPreferences.getStringSet(MainActivity.REGIONS, null);
    }

    // set up and start the next quiz
    public void resetQuiz() {
        // use AssetManager to get image file names for enabled regions
        AssetManager asserts = getActivity().getAssets();
        mFileNameList.clear();  // empty list of image file names

        try {
            // loop through each region
            // this list includes only the selected regions stored in the preferences
            for(String region : mRegionsSet) {
                String[] paths = asserts.list(region);

                for (String path : paths) {
                    mFileNameList.add(path.replace(".png", ""));
                }
            }
        } catch(IOException exception) {
            Log.e(TAG, "Error loading image file names", exception);
        }

        mCorrectAnswers = 0;    // reset the number of correct answers made
        mTotalGuesses = 0;      // reset the total number of guesses the user made
        mQuizCountriesList.clear(); // clear prior list

        int flagCounter = 1;
        int numberOfFlags = mFileNameList.size();

        // add FLAGS_IN_QUIZ random file names to the quizCountriesList
        while (flagCounter <= FLAGS_IN_QUIZ) {
            int randomIndex = mRandom.nextInt(numberOfFlags);

            // get the random file name
            String fileName = mFileNameList.get(randomIndex);

            // if the region is enabled and it hasn't already been chosen
            if(!mQuizCountriesList.contains(fileName)) {
                mQuizCountriesList.add(fileName);   // add the file to the list
                ++flagCounter;
            }
        }

        loadNextFlag(); // start the quiz by loading the first flag
    }

    // after the user guesses a correct flag, load the next flag
    private void loadNextFlag() {
        // get file name of the next flag and remove if from the list
        String nextImage = mQuizCountriesList.remove(0);
        mCorrectAnswer = nextImage; // update the correct answer
        mAnswerTextView.setText("");    // clear answerTextView

        // display current question number
        mQuestionNumberTextView.setText(getResources().getString(R.string.question, (mCorrectAnswers + 1), FLAGS_IN_QUIZ));

        // extract the region from the next image'S name
        String region = nextImage.substring(0, nextImage.indexOf('-'));

        // use AssetManager to load next image from assets folder
        AssetManager assets = getActivity().getAssets();

        try
        {
            // get an InputStream to the asset representing the next flag
            InputStream stream = assets.open(region + "/" + nextImage + ".png");

            // load the asset as a Drawable and display on the flagImageView
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            mFlagImageView.setImageDrawable(flag);
        }
        catch (IOException exception)
        {
            Log.e(TAG, "Error loading " + nextImage, exception);
        }

        Collections.shuffle(mFileNameList); // shuffle file names

        // put the correct answer at the end of the fileNameList
        int correct = mFileNameList.indexOf(mCorrectAnswer);
        mFileNameList.add(mFileNameList.remove(correct));

        // add 3, 6, or 9 guess Buttons based on the value of guessRows
        for (int row = 0; row < mGuessRows; row++) {
            // place Buttons in currentTableRow
            for (int column = 0; column < mGuessLinearLayouts[row].getChildCount(); column++) {
                  // get reference to Button to configure
                Button newGuessButton = (Button)mGuessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // get country name and set it as newGuessButton's text
                String fileName = mFileNameList.get((row * 3) + column);
                newGuessButton.setText(getCountryName(fileName));
            }
        }

        // randomly replace one Button with the correct answer
        int row = mRandom.nextInt(mGuessRows);  // pick random row
        int column = mRandom.nextInt(3);  // pick random column
        LinearLayout randomRow = mGuessLinearLayouts[row]; // get the row
        String countryName = getCountryName(mCorrectAnswer);
        ((Button)randomRow.getChildAt(column)).setText(countryName);
    }

    // parses the country flag file name and returns the country name
    private String getCountryName(String name) {
        return name.substring(name.indexOf('-') + 1).replace('_',' ');
    }
}
