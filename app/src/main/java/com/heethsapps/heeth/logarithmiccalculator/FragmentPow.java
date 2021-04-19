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
import java.math.BigInteger;
import java.math.MathContext;


public class FragmentPow extends Fragment {

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputEditText powBaseEditText;
    private TextInputEditText powExpEditText;
    private TextView powTextView;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_pow, container, false);
        scrollView = rootView.findViewById(R.id.fragment_pow_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_pow_progress_bar);
        loadScreen();

        return rootView;
    }

    public void mainMethod() {

        try {

            powBaseEditText = (TextInputEditText) rootView.findViewById(R.id.powBaseEditText);
            powExpEditText = (TextInputEditText) rootView.findViewById(R.id.powExpEditText);
            powTextView = (TextView) rootView.findViewById(R.id.powTextView);

            // This is useful when the app reloads
            calculate();

            powBaseEditText.addTextChangedListener(new TextWatcher() {
                @Override public void afterTextChanged(Editable s) { }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculate();
                }
            });

            powExpEditText.addTextChangedListener(new TextWatcher() {
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

        BigDecimal baseVal, shortVal, ans;
        BigDecimal expVal = BigDecimal.ZERO;
        BigDecimal log = BigDecimal.ONE;

        String baseString, expString;
        double logVal;
        boolean negative = false;

        try {

            baseString = powBaseEditText.getText().toString();
            expString = powExpEditText.getText().toString();

            if (baseString.contains("-")) {
                negative = true;
                powExpEditText.setInputType(4098);
                baseString = baseString.substring(1);
            } else {
                negative = false;
                powExpEditText.setInputType(8194 | 4098);
            }

            // Check if argument textfield is empty and/or base textfield is empty
            if ((baseString.length() == 0 && expString.length() == 0) || baseString.length() == 0) {
                powTextView.setText("");
                powTextView.setVisibility(View.GONE);
            }
            // If not empty, perform calculations
            else {

                powTextView.setVisibility(View.VISIBLE);

                // Get arg value
                baseVal = new BigDecimal(baseString);

                // Get base value
                if (expString.length() == 0)
                    expVal = BigDecimal.ONE;
                else
                    expVal = new BigDecimal(expString);

                // Calculate result
                int n = baseVal.toString().length();
                shortVal = baseVal.movePointLeft(n - 1);
                logVal = Math.log10(shortVal.doubleValue());
                logVal += n - 1;

                log = expVal.multiply(BigDecimal.valueOf(logVal));
                ans = new BigDecimal(Math.pow(10, log.doubleValue()), new MathContext(MainActivity.contextPrecision));

                ans = ans.multiply(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                ans = new BigDecimal(ans.toBigInteger());
                ans = ans.divide(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)), new MathContext(MainActivity.contextPrecision ));

                if (negative && expVal.toBigInteger().remainder(BigInteger.valueOf(2)).intValue() == 1)
                    powTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans.multiply(BigDecimal.valueOf(-1))));
                else
                    powTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans));

                final String ansString = ans.toString();
                powTextView.setOnLongClickListener(new View.OnLongClickListener() {
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
                    ans = BigDecimal.TEN.pow(log.intValue(), new MathContext(MainActivity.contextPrecision));
                    ans = ans.multiply(BigDecimal.valueOf(Math.pow(10, log.subtract(new BigDecimal(log.intValue())).doubleValue())), new MathContext(MainActivity.contextPrecision));
                    // Set value in textView
                    powTextView.setVisibility(View.VISIBLE);

                    if (negative && expVal.toBigInteger().remainder(BigInteger.valueOf(2)).intValue() == 1)
                        powTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans.multiply(BigDecimal.valueOf(-1))));
                    else
                        powTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans));

                } catch (NumberFormatException nfe2) {
                    System.out.println(nfe2.getMessage());
                    if (message.equals("Infinity or NaN: Infinity"))
                        powTextView.setText("Maximum limit reached");
                } catch (ArithmeticException ae) {
                    // Beyond maximum limit
                    powTextView.setText("Maximum limit reached");

                } catch (Exception e) {

                    // Print exception
                    e.printStackTrace();
                    // Show error as it occured during calculation
                    powTextView.setVisibility(View.VISIBLE);
                    powTextView.setText("Some error occured while calculating, please try changing the values once again!");
                }
            }

        } catch (Exception e) {

            // Print exception
            e.printStackTrace();
            // Show error as it occured during calculation
            powTextView.setVisibility(View.VISIBLE);
            powTextView.setText("Some error occured while calculating, please try changing the values once again!");
        }

    }

}