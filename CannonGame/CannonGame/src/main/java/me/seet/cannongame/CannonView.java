package me.seet.cannongame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by dseet on 3/25/2014.
 */
public class CannonView extends SurfaceView
    implements SurfaceHolder.Callback
{
    public static final String TAG = "CannonView";  // for logging error
    private CannonThread mCannonThread; // controls the game loop
    private Activity mActivity;         // to display Game Over dialog in the GUI thread
    private boolean mDialogIsDisplayed = false;

    // constants for game play
    public static final int TARGET_PIECES = 7;  // sections in the target
    public static final int MISS_PENALTY = 2;  // seconds deducted on a miss
    public static final int HIT_REWARD = 2;  // seconds added on a hit

    // constants for game play and tracking statistics
    private boolean mGameOver; // is the game over?
    private double mTimeLeft;  // time remaining in seconds
    private int mShotsFired;    // shots the user has fired
    private double mTotalElapsedTime;    // elapsed seconds

    // variable for the blocker and target
    private Line mBlocker;       // start and end points of the blocker
    private int mBlockerDistance;    // blocker distance from left
    private int mBlockerBeginning;    // blocker top-edge distance from top
    private int mBlockerEnd;    // blocker top-edge distance from bottom
    private int mInitialBlockerVelocity;    // initial blocker speed multiplier
    private float mBlockerVelocity; // blocker speed multiplier during game

    private Line mTarget;   // start and end points of the target
    private int mTargetDistance;    // target distance from left
    private int mTargetBeginning;    // target distance from top
    private double mPieceLength; // length of a target piece
    private int mTargetEnd;     // target bottom's distance from top
    private int mInitialTargetVelocity; // initial target speed multiplier
    private float mTargetVelocity;  // target speed multiplier
    private int mLineWidth;  // width of the target and blocker
    private boolean[] mHitStates;   // is each target piece hit?
    private int mTargetPiecesHit;   // number of target pieces hit (out of 7)

    // variables for the cannon and cannonball
    private Point mCannonball;  // cannonball image's upper-left corner
    private int mCannonballVelocityX;    // cannonball's X velocity
    private int mCannonballVelocityY;    // cannonball's Y velocity
    private boolean mCannonballOnScreen;    // whether cannonball on the screen
    private int mCannonballRadius;  // cannonball's radius
    private int mCannonballSpeed;  // cannonball's speed
    private int mCannonBaseRadius;  // cannon base's radius
    private int mCannonLength;  // cannon barrel's length
    private Point mBarrelEnd;   // the endpoint of the cannon's barrel
    private int mScreenWidth;
    private int mScreenHeight;

    // constants and variables for managing sounds
    private static final int TARGET_SOUND_ID = 0;
    private static final int CANNON_SOUND_ID = 1;
    private static final int BLOCKER_SOUND_ID = 2;

    private SoundPool mSoundPool;   // plays sound effects
    private SparseIntArray mSoundMap;   // maps IDs to SoundPool

    // Paint variables used when drawing each item on the screen
    private Paint mTextPaint;   // Paint used to draw text
    private Paint mCannonballPaint;   // Paint used to draw the cannonball
    private Paint mCannonPaint;   // Paint used to draw the cannon
    private Paint mBlockerPaint;   // Paint used to draw the blocker
    private Paint mTargetPaint;   // Paint used to draw the target
    private Paint mBackgroundPaint;   // Paint used to clear the drawing area

    // public constructor
    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);  // call superclass constructor

        mActivity = (Activity)context;  // store reference to MainActivity

        // register SurfaceHolder.Callback listener
        getHolder().addCallback(this);

        // initialize Lines and Point representing game items
        mBlocker = new Line();  // create the blocker as a Line
        mTarget = new Line();  // create the target as a Line
        mCannonball = new Point(); // create the cannonball as a Point

        // initialize hitStates as a boolean array
        mHitStates = new boolean[TARGET_PIECES];

        // initialize hitStates as a boolean array
        mHitStates = new boolean[TARGET_PIECES];

        // initialize SoundPool to play the app's three sound effects
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);

        // create Map of sounds and pre-load sounds
        mSoundMap = new SparseIntArray(3);  // create new SparseIntArray
        mSoundMap.put(TARGET_SOUND_ID, mSoundPool.load(context, R.raw.target_hit, 1));
        mSoundMap.put(CANNON_SOUND_ID, mSoundPool.load(context, R.raw.cannon_fire, 1));
        mSoundMap.put(BLOCKER_SOUND_ID, mSoundPool.load(context, R.raw.blocker_hit, 1));

        // construct Paints for drawing text, cannonball, cannon,
        // blocker and target; these are configured in method onSizeChanged
        mTextPaint = new Paint();
        mCannonPaint = new Paint();
        mCannonballPaint = new Paint();
        mBlockerPaint = new Paint();
        mTargetPaint = new Paint();
        mBackgroundPaint = new Paint();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenWidth = w;   // store CannonView's width
        mScreenHeight = h;   // store CannonView's height
        mCannonBaseRadius = h / 18;  // cannon base radius 1/18 screen height
        mCannonLength = w / 8;  // cannon length 1/8 screen width

        mCannonballRadius = w / 36;   // cannonball radius 1/36 screen width
        mCannonballSpeed = w * 3 / 2;   // cannonball speed multiplier

        mLineWidth = w / 24;    // target and blocker 1 / 24 screen width

        // configure instance variables related to the blocker
        mBlockerDistance = w * 5 / 8;   // blocker 5/8 screen width from left
        mBlockerBeginning = h / 8;   // distance from top 1/8 screen height
        mBlockerEnd = h * 3 / 8;   // distance from top 3/8 screen height
        mInitialBlockerVelocity = h / 2; // initial blocker speed multiplier
        mBlocker.setStart(new Point(mBlockerDistance, mBlockerBeginning));
        mBlocker.setEnd(new Point(mBlockerDistance, mBlockerEnd));

        // configure instance variables related to the target
        mTargetDistance = w * 7 / 8;    // target 7 / 8 screen width from left
        mTargetBeginning = h / 8;   // distance from top 1/8 screen height
        mTargetEnd = h * 7 / 8;   // distance from top 7/8 screen height
        mPieceLength = (mTargetEnd - mTargetBeginning) / TARGET_PIECES;
        mInitialTargetVelocity = -h / 4; // initial target speed multiplier
        mTarget.setStart(new Point(mTargetDistance, mTargetBeginning));
        mTarget.setEnd(new Point(mTargetDistance, mTargetEnd));

        // endpoint of the cannon's barrel initially points horizontally
        mBarrelEnd = new Point(mCannonLength, h / 2);

        // configure Paint objects for drawing game elements
        mTextPaint.setTextSize(w / 20); // text size 1/20 of screen width
        mTextPaint.setAntiAlias(true);  // smoothes the text
        mCannonballPaint.setStrokeWidth(mLineWidth * 1.5f); // set line thickness
        mBlockerPaint.setStrokeWidth(mLineWidth); // set line thickness
        mTargetPaint.setStrokeWidth(mLineWidth); // set line thickness
        mBackgroundPaint.setColor(Color.WHITE); // set background color

        newGame();  // set up and start a new game
    }

    // stops the game; called by CannonGameFragment's onPause method
    public void stopGame() {
        if(mCannonThread != null)
            mCannonThread.setRunning(false);    // tell thread to terminate
    }

    // releases resources; called by CannonGame's onDestroy method
    public void releaseResources() {
        mSoundPool.release();   // release all resources used by the SoundPool
        mSoundPool = null;
    }

    // reset all the screen elements and start a new game
    private void newGame() {
         // set the every element of hitStates to false--restores target pieces
        for (int i = 0; i < TARGET_PIECES; i++) {
            mHitStates[i] = false;
        }

        mTargetPiecesHit = 0;   // no target pieces have been hit
        mBlockerVelocity = mInitialBlockerVelocity; // set initial velocity
        mTargetVelocity = mInitialTargetVelocity; // set initial velocity
        mTimeLeft = 10; // start the countdown at 10 seconds
        mCannonballOnScreen = false;    // the cannonball is not on the screen
        mShotsFired = 0;    // set the initial number of shots fired
        mTotalElapsedTime = 0.0;    // set the time elapsed to zero

        // set the start and end Points of the blocker and target
        mBlocker.setStart(new Point(mBlockerDistance, mBlockerBeginning));
        mBlocker.setEnd(new Point(mBlockerDistance, mBlockerEnd));
        mTarget.setStart(new Point(mTargetDistance, mTargetBeginning));
        mTarget.setEnd(new Point(mTargetDistance, mTargetEnd));

        if(mGameOver)   // starting a new game after the last game ended
        {
            mGameOver = false;  // the game is not over
            mCannonThread = new CannonThread(getHolder());   // create thread
            mCannonThread.start();  // start the game loop thread
        }
    }

    // called repeatedly by the CannonThread to update game elements
    private void updatePositions(double elapsedTimeMs) {
        double interval  = elapsedTimeMs / 1000.0; // convert to seconds
        if(mCannonballOnScreen) // if there is currently a shot fired
        {
            // update cannonball position
            mCannonball.x += interval * mCannonballVelocityX;
            mCannonball.y += interval * mCannonballVelocityY;

            // check for collision with blocker
            if(mCannonball.x + mCannonballRadius > mBlockerDistance
                && mCannonball.x - mCannonballRadius < mBlockerDistance
                && mCannonball.y + mCannonballRadius > mBlocker.getStart().y
                && mCannonball.y - mCannonballRadius < mBlocker.getEnd().y) {
                mCannonballVelocityX *= -1; // reverse cannonball's direction
                mTimeLeft -= MISS_PENALTY; // penalize the user

                // play blocker sound
                mSoundPool.play(mSoundMap.get(BLOCKER_SOUND_ID), 1, 1, 1, 0, 1f);
            }
            // check for collisions with left and right walls
            else if(mCannonball.x + mCannonballRadius > mScreenWidth || mCannonball.x - mCannonballRadius < 0) {
                mCannonballOnScreen = false;    // remove cannonball from screen
            }
            // check for collisions with top and bottom walls
            else if(mCannonball.y + mCannonballRadius > mScreenHeight || mCannonball.y - mCannonballRadius < 0) {
                mCannonballOnScreen = false;    // remove cannonball from screen
            }
            // check for cannonball collision with target
            else if(mCannonball.x + mCannonballRadius > mTargetDistance
                    && mCannonball.x - mCannonballRadius < mTargetDistance
                    && mCannonball.y - mCannonballRadius > mTarget.getStart().y
                    && mCannonball.x - mCannonballRadius < mTarget.getEnd().y) {
                // determine target section number (0 is the top)
                int section = (int)((mCannonball.y - mTarget.getStart().y) / mPieceLength);

                // check if the piece hasn't been hit yet
                if((section >= 0 && section < TARGET_PIECES) && !mHitStates[section]) {
                    mHitStates[section] = true; // section was hit
                    mCannonballOnScreen = false;    // remove cannonball
                    mTimeLeft += HIT_REWARD;    // add reward to remaining time
                    // play target hit sound
                    mSoundPool.play(mSoundMap.get(TARGET_SOUND_ID), 1, 1, 1, 0, 1f);

                    // if all pieces have been hit
                    if(++mTargetPiecesHit == TARGET_PIECES) {
                        mCannonThread.setRunning(false);    // terminate thread
                        showGameOverDialog(R.string.win);   // show winning dialog
                        mGameOver = true;
                    }
                }
            }
        }

        // update the blocker's position
        double blockerUpdate = interval * mBlockerVelocity;
        mBlocker.getStart().y += blockerUpdate;
        mBlocker.getEnd().y += blockerUpdate;

        // update the target's position
        double targetUpdate = interval * mTargetVelocity;
        mTarget.getStart().y += targetUpdate;
        mTarget.getEnd().y += targetUpdate;

        // if the blocker hit the top or bottom, reverse direction
        if(mBlocker.getStart().y < 0 || mBlocker.getEnd().y > mScreenHeight) {
            mBlockerVelocity *= -1;
        }

        // if the target hit the top or bottom, reverse direction
        if(mTarget.getStart().y < 0 || mTarget.getEnd().y > mScreenHeight) {
            mTargetVelocity *= -1;
        }

        mTimeLeft -= interval; // subtract from time left

        // if the time reached zero
        if(mTimeLeft <= 0.0) {
            mTimeLeft = 0.0;
            mGameOver = true;   // the game is over
            mCannonThread.setRunning(false);    // terminate thread
            showGameOverDialog(R.string.lose);   // show the losing dialog
        }
    }

    // display an AlertDialog when the game ends
    private void showGameOverDialog(final int messageId) {
        // DialogFragment to display quiz stats and start new quiz
        final DialogFragment gameResult = new DialogFragment() {
            // create an AlertDialog and return it
            @Override
            public Dialog onCreateDialog(Bundle bundle) {
                // create dialog displaying String resource for messageId
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(messageId));

                // display number of shots fired and total time elapsed
                builder.setMessage(getResources().getString(R.string.results_format, mShotsFired, mTotalElapsedTime));
                builder.setPositiveButton(R.string.reset_game,
                        new DialogInterface.OnClickListener() {
                            // called when "Reset Game" Button is pressed
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                mDialogIsDisplayed = false;
                                newGame();  // set up and start a new game
                            }
                        });

                return builder.create();    // return the AlertDialog
            }
        };

        // in GUI thread, use FragmentManager to display the DialogFragment
        mActivity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        mDialogIsDisplayed = true;
                        gameResult.setCancelable(false);    // modal dialog
                        gameResult.show(mActivity.getFragmentManager(), "results");
                    }
                }
        );
    }

    // fires a cannonball
    public void fireCannonball(MotionEvent event){
        if(mCannonballOnScreen) // if a cannonball is already on the screen
            return; // do nothing

        double angle = alignCannon(event);  // get the cannon barrel's angle

        // move the cannonball to be inside the cannon
        mCannonball.x = mCannonballRadius;  // align x-coordinate with cannot
        mCannonball.y = mScreenHeight / 2;  // centers ball vertically

        // get the x-component of the total velocity
        mCannonballVelocityX = (int)(mCannonballSpeed * Math.sin(angle));

        // get the y-component of the total velocity
        mCannonballVelocityY = (int)(-mCannonballSpeed * Math.cos(angle));
        mCannonballOnScreen = true; // the cannonball is on the screen
        ++mShotsFired;  // increment shotsFired

        // play cannon fired sound
        mSoundPool.play(mSoundMap.get(CANNON_SOUND_ID), 1, 1, 1, 0, 1f);
    }

    // aligns the cannon in response to a user touch
    private double alignCannon(MotionEvent event) {
        // get the location of the touch in this view
        Point touchPoint = new Point((int)event.getX(), (int)event.getY());

        // compute the touch's distance from center of the screen
        // on the y-axis
        double centerMinusY = (mScreenHeight / 2 - touchPoint.y);

        double angle = 0; // initialize angle to 0

        // calculate the angle the barrel makes with the horizontal
        if(centerMinusY != 0)   // prevent division by 0
            angle = Math.atan((double)touchPoint.x / centerMinusY);

        // if the touch is on the lower half of the screen
        if(touchPoint.y > mScreenHeight / 2) {
            angle += Math.PI; // adjust the angle
        }

        // calculate the endpoint of the cannon barrel
        mBarrelEnd.x = (int)(mCannonLength * Math.sin(angle));
        mBarrelEnd.y = (int)(-mCannonLength * Math.cos(angle) + mScreenHeight / 2);

        return angle; // return the computed angle
    }

    // draws the game to the given Canvas
    public void drawGameElements(Canvas canvas) {
        // clear the background
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);

        // display time remaining
        canvas.drawText(getResources().getString(R.string.time_remaining_format, mTimeLeft), 30, 50, mTextPaint);

        // if a cannonball is currently on the screen, draw i
        if(mCannonballOnScreen)
            canvas.drawCircle(mCannonball.x, mCannonball.y, mCannonballRadius, mCannonballPaint);

        // draw the cannon barrel
        canvas.drawLine(0, mScreenHeight / 2, mBarrelEnd.x, mBarrelEnd.y, mCannonPaint);

        // draw the cannon base
        canvas.drawCircle(0, (int)mScreenHeight / 2, (int)mCannonBaseRadius, mCannonPaint);

        // draw the blocker
        canvas.drawLine(mBlocker.getStart().x, mBlocker.getStart().y, mBlocker.getEnd().x, mBlocker.getEnd().y, mBlockerPaint);

        Point currentPoint = new Point();   // start of current target section

        // initialize currentPoint to the starting point of the target
        currentPoint.x = mTarget.getStart().x;
        currentPoint.y = mTarget.getStart().y;

        // draw the target
        for (int i = 0; i < TARGET_PIECES; i++) {
              // if this target piece is not hit draw it
            if(!mHitStates[i]) {
                // alternate coloring the pieces
                if(i % 2 != 0)
                    mTargetPaint.setColor(Color.BLUE);
                else
                    mTargetPaint.setColor(Color.YELLOW);

                canvas.drawLine(currentPoint.x, currentPoint.y, mTarget.getEnd().x, (int)(currentPoint.y + mPieceLength), mTargetPaint);
            }

            // move currentPoint to the start of the next piece
            currentPoint.y += mPieceLength;
        }
    }

    // called when surface is first created
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(!mDialogIsDisplayed) {
            mCannonThread = new CannonThread(holder);    // create thread
            mCannonThread.setRunning(true); // start game running
            mCannonThread.start();  // start the game loop thread
        }
    }

    // called when surface changes size
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    // called when the surface is destroyed
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // ensure that thread terminates properly
        boolean retry = true;
        mCannonThread.setRunning(false);    // terminate cannonThread
        while(retry) {
            try {
                mCannonThread.join();   // wait for cannonThread to finish
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    }

    // called when the user touches the screen in this Activity
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get int representing the type of action which caused this event
        int action = event.getAction();

        // the user touched the screen or dragged along the screen
        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            fireCannonball(event);  // fire the cannonball toward the touch point
        }
        return true;
    }

    // Thread subclass to control the game loop
    public class CannonThread extends Thread {
        private SurfaceHolder mSurfaceHolder;   // for manipulating canvas
        private boolean mThreadIsRunning = true;    // running by default

        // initializes the surface holder
        public CannonThread(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            setName("CannonThread");
        }

        // changes running state
        public void setRunning(boolean running) {
            mThreadIsRunning = running;
        }

        // controls the game loop

        @Override
        public void run() {
            Canvas canvas = null;
            long previousFrameTime = System.currentTimeMillis();

            while (mThreadIsRunning) {
                try {
                    // get Canvas for exclusive drawing from this thread
                    canvas = mSurfaceHolder.lockCanvas(null);

                    // lock the surfaceHolder for drawing
                    synchronized (mSurfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMs = currentTime - previousFrameTime;
                        mTotalElapsedTime += elapsedTimeMs / 1000.0;
                        updatePositions(elapsedTimeMs); // update game state
                        drawGameElements(canvas);   // draw using the canvas
                        previousFrameTime = currentTime; // update previous time
                    }
                } finally {
                    // display canva's contents on the CannonView
                    // and enable other threads to use the Canvas
                    if(canvas != null)
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

}
