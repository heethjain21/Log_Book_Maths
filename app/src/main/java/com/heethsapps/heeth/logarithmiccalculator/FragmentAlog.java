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


public class FragmentAlog extends Fragment {

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputEditText alogEditText;
    private TextView alogTextView;

    private BigDecimal lastAnsVal;
    private String lastStringVal;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_alog, container, false);
        scrollView = rootView.findViewById(R.id.fragment_alog_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_alog_progress_bar);
        loadScreen();

        return rootView;
    }

    public void mainMethod() {

        try {

            alogEditText = (TextInputEditText) rootView.findViewById(R.id.alogEditText);
            alogTextView = (TextView) rootView.findViewById(R.id.alogTextView);

            // This is useful if app has been reloaded
            calculate();

            alogEditText.addTextChangedListener(new TextWatcher() {
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

        String alogNumString = "";

        try {
            alogNumString = alogEditText.getText().toString();
            // Check if argument textfield is empty and/or base textfield is empty
            if (alogNumString.length() == 0) {
                alogTextView.setText("");
                alogTextView.setVisibility(View.GONE);
            }
            // If not empty, perform calculations
            else {
                alogTextView.setVisibility(View.VISIBLE);
                // Get ans in bigdecimal
                BigDecimal ans = new BigDecimal(Math.pow(10, Double.parseDouble(alogNumString)));
                // Multiple bigdecimal by 10^MainActivity.decimalPrecision
                ans = ans.multiply(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                // Convert that value to integer with toBigInteger and store it back
                ans = new BigDecimal(ans.toBigInteger());
                // Divide the value by 10^MainActivity.decimalPrecision
                ans = ans.divide(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                //Finally we have the ans upto MainActivity.decimalPrecision places
                ans = ans.round(new MathContext(10));

                alogTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans));

                final String ansString = ans.toString();
                alogTextView.setOnLongClickListener(new View.OnLongClickListener() {
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

        } catch (NumberFormatException nfe) {

            String message = nfe.getMessage();
            if (message.equals("Infinity or NaN: Infinity")) {

                try {
                    // Calculate additional value
                    int val = (int) Double.parseDouble(alogNumString);
                    double diff = val - Double.parseDouble(alogNumString);
                    BigDecimal ans = BigDecimal.TEN.pow(val, new MathContext(10));
                    if (diff != 0) {
                        ans = ans.multiply(BigDecimal.valueOf(Math.pow(10, diff)), new MathContext(10));
                    }
                    // Set value in textView
                    alogTextView.setVisibility(View.VISIBLE);
                    alogTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans));

                } catch (NumberFormatException nfe2) {
                    System.out.println(nfe2.getMessage());
                    if (message.equals("Infinity or NaN: Infinity"))
                        alogTextView.setText("Maximum limit reached");

                } catch (ArithmeticException ae) {
                    // Beyond maximum limit
                    ae.printStackTrace();
                    alogTextView.setText("Maximum limit reached");

                } catch (Exception e) {

                    // Print exception
                    e.printStackTrace();
                    // Show error as it occured during calculation
                    alogTextView.setVisibility(View.VISIBLE);
                    alogTextView.setText("Some error occured while calculating, please try changing the values once again!");
                }

            } else if (alogNumString.equals("."))
                alogTextView.setVisibility(View.GONE);

        } catch (Exception e) {

            // Print exception
            e.printStackTrace();
            // Show error as it occured during calculation
            alogTextView.setVisibility(View.VISIBLE);
            alogTextView.setText("Some error occured while calculating, please try changing the values once again!");
        }

    }

}