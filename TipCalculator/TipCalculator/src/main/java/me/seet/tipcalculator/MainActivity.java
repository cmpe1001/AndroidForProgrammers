// Calculate bills using 15% and custom percentage tips

package me.seet.tipcalculator;

import android.app.Activity;    // base class for activities
import android.os.Bundle;   // for saving state information
import android.text.Editable;   // for EditText event handling
import android.text.TextWatcher;    // EditText listener
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText; // for bill amount input
import android.widget.SeekBar;  // for changing custom tip percentage
import android.widget.SeekBar.OnSeekBarChangeListener;  // SeekBar listener
import android.widget.TextView; // for displaying text

import java.text.NumberFormat;  // for currency formatting

public class MainActivity extends Activity {
    // currency and percent formatters
    private static final NumberFormat mCurrencyFormat = NumberFormat.getCurrencyInstance();
    private static final NumberFormat mPercentFormat = NumberFormat.getPercentInstance();

    private double mBillAmount = 0.0;     // bill amount entered by the user
    private double mCustomPercent = 0.18; // initial custom tip percentage

    private TextView mAmountDisplayTextView;    // shows formatted bill amount
    private TextView mPercentCustomTextView;    // shows custom tip percentage
    private TextView mTip15TextView;    // shows 15% tip
    private TextView mTotal15TextView;    // shows total with 15% tip
    private TextView mTipCustomTextView;    // shows custom tip amount
    private TextView mTotalCustomTextView;    // shows total with custom tip

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     // call super class instance
        setContentView(R.layout.activity_main); // inflate the GUI

        // get references to the TextViews
        // that MainActivity interacts with programmatically
        mAmountDisplayTextView = (TextView) findViewById(R.id.amountDisplayTextView);
        mPercentCustomTextView = (TextView) findViewById(R.id.percentCustomTextView);
        mTip15TextView = (TextView) findViewById(R.id.tip15TextView);
        mTotal15TextView = (TextView) findViewById(R.id.total15TextView);
        mTipCustomTextView = (TextView) findViewById(R.id.tipCustomTextView);
        mTotalCustomTextView = (TextView) findViewById(R.id.totalCustomTextView);

        // Update GUI based on mBillAmount and mCustomPercent
        mAmountDisplayTextView.setText(mCurrencyFormat.format(mBillAmount));

        updateStandard();   // update the 15% tip TextViews   // update the 15% tip TextViews
        updateCustom();   // update the custom tip TextViews

        // set amountEditText's TextWatcher
        EditText amountEditText = (EditText) findViewById(R.id.amountEditText);
        amountEditText.addTextChangedListener(mAmountEditTextWatcher);

        // set customTipSeekBar's OnSeekBarChangeListener
        SeekBar customTipSeekBar = (SeekBar) findViewById(R.id.customTipSeekBar);
        customTipSeekBar.setOnSeekBarChangeListener(mCustomSeekBarListener);
    }

    // updates 15% tip and total TextViews
    private void updateStandard() {
        // calculate 15% tip and total
        double fifteenPercentTip = mBillAmount * 0.15;
        double fifteenPercentTotal = mBillAmount + fifteenPercentTip;

        // display 15% tip and total formatted as currency
        mTip15TextView.setText(mCurrencyFormat.format(fifteenPercentTip));
        mTotal15TextView.setText(mCurrencyFormat.format(fifteenPercentTotal));
    }

    // updates custom tip and total TextViews
    private void updateCustom() {
        // calculate custom tip and total
        double customPercentTip = mBillAmount * mCustomPercent;
        double customPercentTotal = mBillAmount + customPercentTip;

        // display custom tip and total formatted as currency
        mTipCustomTextView.setText(mCurrencyFormat.format(customPercentTip));
        mTotalCustomTextView.setText(mCurrencyFormat.format(customPercentTotal));
    }

    // event-handling object that responds to amountEditText's events
    private TextWatcher mAmountEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

        }

        // called when the user enters a number
        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // convert amountEditText's text to a double
            try {
                mBillAmount = Double.parseDouble(charSequence.toString()) / 100.0;
            } catch(NumberFormatException e) {
                mBillAmount = 0.0;  // default if an exception occurs
            }

            // display currency formatted bill amount
            mAmountDisplayTextView.setText(mCurrencyFormat.format(mBillAmount));
            updateStandard();   // update the 15% tip TextViews
            updateCustom(); // update the custom tip TextViews
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    // called when the user changes the position of SeekBar
    private OnSeekBarChangeListener mCustomSeekBarListener = new OnSeekBarChangeListener() {
        // update customPercent, then call updateCustom
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // sets customPercent to position of the SeekBar's thumb
            mCustomPercent = progress/100.0;
            updateCustom(); // update the custom tipTextViews
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
