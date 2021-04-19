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
import java.math.MathContext;


public class FragmentTan extends Fragment {

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputEditText tanDegEditText;
    private TextInputEditText tanMinEditText;
    private TextView tanTextView;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_tan, container, false);
        scrollView = rootView.findViewById(R.id.fragment_tan_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_tan_progress_bar);
        loadScreen();

        return rootView;
    }

    public void mainMethod() {

        try {

            tanDegEditText = (TextInputEditText) rootView.findViewById(R.id.tanDegEditText);
            tanMinEditText = (TextInputEditText) rootView.findViewById(R.id.tanMinEditText);
            tanTextView = (TextView) rootView.findViewById(R.id.tanTextView);

            // This is useful when the app reloads
            calculate();

            tanDegEditText.addTextChangedListener(new TextWatcher() {
                @Override public void afterTextChanged(Editable s) { }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculate();
                }
            });

            tanMinEditText.addTextChangedListener(new TextWatcher() {
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

            String degString = tanDegEditText.getText().toString();
            String minString = tanMinEditText.getText().toString();

            if (degString.contains("."))
                ((TextInputLayout) tanMinEditText.getParent().getParent()).setVisibility(View.GONE);
            else
                ((TextInputLayout) tanMinEditText.getParent().getParent()).setVisibility(View.VISIBLE);

            // Check if argument textfield is empty and/or base textfield is empty
            if ((degString.length() == 0 && minString.length() == 0) || degString.length() == 0) {
                tanTextView.setText("");
                tanTextView.setVisibility(View.GONE);
            }
            // If not empty, perform calculations
            else {

                tanTextView.setVisibility(View.VISIBLE);

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
                double funcDegVal = calculateDegVal(degVal);
                double funcMinVal = calculateMinVal(minVal);
                double sinAns, cosAns;

                if (funcDegVal == 90 || (funcDegVal == 89 && funcMinVal == 60)) {
                    tanTextView.setText("The result is: Infinity");
                } else if (funcDegVal == -90 || (funcDegVal == -89 && funcMinVal == -60)) {
                    tanTextView.setText("The result is: - Infinity");
                } else {

                    if (funcMinVal == 60) {
                        sinAns = Math.sin(Math.toRadians(funcDegVal + 1.0000));
                        cosAns = Math.cos(Math.toRadians(funcDegVal + 1.0000));
                    } else {
                        sinAns = Math.sin(Math.toRadians(funcDegVal + funcMinVal / 60));
                        cosAns = Math.cos(Math.toRadians(funcDegVal + funcMinVal / 60));
                    }

                    BigDecimal ans = new BigDecimal(sinAns).divide(new BigDecimal(cosAns), new MathContext(MainActivity.contextPrecision));
                    // Multiple bigdecimal by 10^MainActivity.decimalPrecision
                    ans = ans.multiply(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                    // Convert that value to integer with toBigInteger and store it back
                    ans = new BigDecimal(ans.toBigInteger());
                    // Divide the value by 10^MainActivity.decimalPrecision
                    ans = ans.divide(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                    //Finally we have the ans upto MainActivity.decimalPrecision places
                    ans = ans.round(new MathContext(MainActivity.contextPrecision));

                    tanTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans));

                    final String ansString = ans + "";
                    tanTextView.setOnLongClickListener(new View.OnLongClickListener() {
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

        } catch (Exception e) {

            // Print exception
            e.printStackTrace();
            // Show error as it occured during calculation
            tanTextView.setVisibility(View.VISIBLE);
            tanTextView.setText("Some error occured while calculating, please try changing the values once again!");
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