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
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;


public class FragmentLogSin extends Fragment {

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputEditText logsinDegEditText;
    private TextInputEditText logsinMinEditText;
    private TextView logsinTextView;

    private BigDecimal lastAnsVal;
    private String lastStringVal;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_log_sin, container, false);
        scrollView = rootView.findViewById(R.id.fragment_logsin_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_logsin_progress_bar);
        loadScreen();

        return rootView;
    }

    public void mainMethod() {

        try {

            logsinDegEditText = (TextInputEditText) rootView.findViewById(R.id.logsinDegEditText);
            logsinMinEditText = (TextInputEditText) rootView.findViewById(R.id.logsinMinEditText);
            logsinTextView = (TextView) rootView.findViewById(R.id.logsinTextView);

            // This is useful when the app reloads
            calculate();

            logsinDegEditText.addTextChangedListener(new TextWatcher() {
                @Override public void afterTextChanged(Editable s) { }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculate();
                }
            });

            logsinMinEditText.addTextChangedListener(new TextWatcher() {
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

            String degString = logsinDegEditText.getText().toString();
            String minString = logsinMinEditText.getText().toString();

            if (degString.contains("."))
                ((TextInputLayout) logsinMinEditText.getParent().getParent()).setVisibility(View.GONE);
            else
                ((TextInputLayout) logsinMinEditText.getParent().getParent()).setVisibility(View.VISIBLE);

            // Check if argument textfield is empty and/or base textfield is empty
            if ((degString.length() == 0 && minString.length() == 0) || degString.length() == 0) {
                logsinTextView.setText("");
                logsinTextView.setVisibility(View.GONE);
            }
            // If not empty, perform calculations
            else {

                logsinTextView.setVisibility(View.VISIBLE);

                BigDecimal degVal;
                BigDecimal minVal;

                // Get arg value
                degVal = new BigDecimal(degString);

                // Get base value
                if (minString.length() == 0)
                    minVal = BigDecimal.ZERO;
                else
                    minVal = new BigDecimal(minString);

                // Calculate result
                double logsinDegVal = calculateDegVal(degVal);
                double logsinMinVal = calculateMinVal(minVal);
                double ans;

                double rounding = Math.pow(10, MainActivity.decimalPrecision);

                if (logsinMinVal == 60) {
                    ans = Math.sin(Math.toRadians(logsinDegVal + 1));
                } else {
                    ans = Math.sin(Math.toRadians(logsinDegVal + logsinMinVal / 60));
                }

                String stringAns;
                if (ans == 0) {
                    logsinTextView.setText("The result is: -Infinity");
                    stringAns = "-Infinity";
                } else {
                    ans = Math.log10(ans);
                    if (MainActivity.decimalPrecision == 0) {
                        logsinTextView.setText("The result is: " + (int) ans);
                    } else {
                        ans = Math.round(ans * rounding) / rounding;
                        logsinTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans));
                    }
                    stringAns = ans + "";
                }

                final String ansString = stringAns;
                logsinTextView.setOnLongClickListener(new View.OnLongClickListener() {
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

        } catch (Exception e) {

            // Print exception
            e.printStackTrace();
            // Show error as it occured during calculation
            logsinTextView.setVisibility(View.VISIBLE);
            logsinTextView.setText("Some error occured while calculating, please try changing the values once again!");
        }

    }

    private double calculateDegVal(BigDecimal degVal) {
        BigDecimal maxDeg = new BigDecimal(360);
        BigDecimal n = degVal.divideToIntegralValue(maxDeg);
        degVal = degVal.subtract(n.multiply(maxDeg));
        return degVal.doubleValue();
    }

    private double calculateMinVal(BigDecimal minVal) {
        BigDecimal maxMin = new BigDecimal(60);
        BigDecimal n = minVal.divideToIntegralValue(maxMin);
        minVal = minVal.subtract(n.multiply(maxMin));
        return minVal.doubleValue();
    }

}