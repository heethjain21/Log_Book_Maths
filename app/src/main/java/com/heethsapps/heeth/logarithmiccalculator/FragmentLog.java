package com.heethsapps.heeth.logarithmiccalculator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.math.MathContext;


public class FragmentLog extends Fragment {

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputEditText logArgEditText;
    private TextInputEditText logBaseEditText;
    private TextView logTextView;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_log, container, false);
        scrollView = rootView.findViewById(R.id.fragment_log_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_log_progress_bar);
        loadScreen();

        return rootView;
    }

    public void mainMethod() {

        try {

            logArgEditText = (TextInputEditText) rootView.findViewById(R.id.logArgEditText);
            logBaseEditText = (TextInputEditText) rootView.findViewById(R.id.logBaseEditText);
            logTextView = (TextView) rootView.findViewById(R.id.logTextView);

            // This is useful when app reloads
            calculate();

            logArgEditText.addTextChangedListener(new TextWatcher() {
                @Override public void afterTextChanged(Editable s) { }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculate();
                }
            });

            logBaseEditText.addTextChangedListener(new TextWatcher() {
                @Override public void afterTextChanged(Editable s) { }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculate();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            ((MainActivity) getActivity()).showSnackBar(rootView, R.string.error);
        }

    }

    // Show loading screen spinner
    private void loadScreen() {
        scrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new CountDownTimer(MainActivity.loadingTime, 1000) {
            public void onTick(long millisUntilFinished) { }
            public void onFinish() {
                scrollView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                mainMethod();
            }
        }.start();
    }

    private void calculate() {

        try {

            String argString = logArgEditText.getText().toString();
            String baseString = logBaseEditText.getText().toString();

            // Check if argument textfield is empty and/or base textfield is empty
            if ((argString.length() == 0 && baseString.length() == 0) || argString.length() == 0) {
                logTextView.setText("");
                logTextView.setVisibility(View.GONE);
            }
            // If not empty, perform calculations
            else {

                logTextView.setVisibility(View.VISIBLE);

                BigDecimal argVal;
                BigDecimal baseVal;

                // Get arg value
                argVal = new BigDecimal(argString);

                // Get base value
                if (baseString.length() == 0)
                    baseVal = BigDecimal.TEN;
                else
                    baseVal = new BigDecimal(baseString);

                // Calculate result
                if (baseVal.equals(BigDecimal.ONE)) {
                    logTextView.setText("Error: Base should not be equal to 1");
                } else {
                    //int decimalPrecision = 4;
                    // Get ans

                    double argLogVal = calcuateLogVal(argVal);
                    double baseLogVal = calcuateLogVal(baseVal);

                    BigDecimal ans = new BigDecimal(argLogVal).divide(new BigDecimal(baseLogVal), new MathContext(MainActivity.contextPrecision));

                    ans = ans.multiply(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                    // Convert that value to integer with toBigInteger and store it back
                    ans = new BigDecimal(ans.toBigInteger());
                    // Divide the value by 10^decimalPrecision
                    ans = ans.divide(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                    //Finally we have the ans upto decimalPrecision places
                    ans = ans.round(new MathContext(MainActivity.contextPrecision));

                    logTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans));

                    final String ansString = ans.toString();
                    logTextView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            // Copy contents
                            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Result", ansString);
                            clipboard.setPrimaryClip(clip);
                            // Log print
                            System.out.println("Copied Result: " + ansString);
                            // Show snackbar that result is copied
                            ((MainActivity) getActivity()).showSnackBar(((MainActivity) getActivity()).getDrawerLayout(), R.string.copied);
                            return false;
                        }
                    });
                }
            }

        } catch (ArithmeticException ae) {

            logTextView.setText("Error: Base value very close to or equal to 1");
        } catch (NumberFormatException nfe) {
            logTextView.setText("The result is: Undefined");
        } catch (Exception e) {

            // Print exception
            e.printStackTrace();
            // Show error as it occured during calculation
            logTextView.setVisibility(View.VISIBLE);
            logTextView.setText("Some error occured while calculating, please try changing the values once again!");
        }

    }

    private double calcuateLogVal(BigDecimal val) {

        int n = val.toString().length();
        BigDecimal shortVal = val.movePointLeft(n - 1);
        double logVal = Math.log10(shortVal.doubleValue());
        logVal += n - 1;

        return logVal;

    }

}