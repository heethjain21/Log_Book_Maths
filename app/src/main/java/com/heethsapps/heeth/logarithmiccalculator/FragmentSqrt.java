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


public class FragmentSqrt extends Fragment {

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputEditText sqrtEditText;
    private TextView sqrtTextView;

    private BigDecimal lastAnsVal;
    private String lastStringVal;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_sqrt, container, false);
        scrollView = rootView.findViewById(R.id.fragment_sqrt_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_sqrt_progress_bar);
        loadScreen();

        return rootView;
    }

    public void mainMethod() {

        try {

            sqrtEditText = (TextInputEditText) rootView.findViewById(R.id.sqrtEditText);
            sqrtTextView = (TextView) rootView.findViewById(R.id.sqrtTextView);

            // This is useful when the app reloads
            calculate();

            sqrtEditText.addTextChangedListener(new TextWatcher() {
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

        BigDecimal ans = BigDecimal.ZERO;
        BigDecimal shortVal;
        BigDecimal log = BigDecimal.ZERO;

        try {

            String sqrtNumString = sqrtEditText.getText().toString();

            // Check if argument textfield is empty and/or base textfield is empty
            if (sqrtNumString.length() == 0) {
                sqrtTextView.setText("");
                sqrtTextView.setVisibility(View.GONE);
            }
            // If not empty, perform calculations
            else {

                sqrtTextView.setVisibility(View.VISIBLE);

                /******************* Calculate result *******************/
                // Get ans in bigdecimal
                BigDecimal sqrtVal = new BigDecimal(sqrtNumString);
                // Calculate square

                int n = sqrtVal.toString().length();
                shortVal = sqrtVal.movePointLeft(n - 1);
                double logVal = Math.log10(shortVal.doubleValue());
                logVal += n - 1;

                log = BigDecimal.valueOf(0.5).multiply(BigDecimal.valueOf(logVal));
                ans = new BigDecimal(Math.pow(10, log.doubleValue()), new MathContext(MainActivity.contextPrecision));

                ans = ans.multiply(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                ans = new BigDecimal(ans.toBigInteger());
                ans = ans.divide(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)), new MathContext(MainActivity.contextPrecision));

                sqrtTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans));

                final String ansString = ans.toString();
                sqrtTextView.setOnLongClickListener(new View.OnLongClickListener() {
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
                    sqrtTextView.setVisibility(View.VISIBLE);

                    sqrtTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans));

                } catch (NumberFormatException nfe2) {
                    System.out.println(nfe2.getMessage());
                    if (message.equals("Infinity or NaN: Infinity"))
                        sqrtTextView.setText("Maximum limit reached");
                } catch (ArithmeticException ae) {
                    // Beyond maximum limit
                    sqrtTextView.setText("Maximum limit reached");

                } catch (Exception e) {

                    // Print exception
                    e.printStackTrace();
                    // Show error as it occured during calculation
                    sqrtTextView.setVisibility(View.VISIBLE);
                    sqrtTextView.setText("Some error occured while calculating, please try changing the values once again!");
                }
            }

        } catch (Exception e) {

            // Print exception
            e.printStackTrace();
            // Show error as it occured during calculation
            sqrtTextView.setVisibility(View.VISIBLE);
            sqrtTextView.setText("Some error occured while calculating, please try changing the values once again!");
        }

    }

    private BigInteger sqrtN(BigInteger in) {
        final BigInteger TWO = BigInteger.valueOf(2);
        int c;

        // Significantly speed-up algorithm by proper select of initial approximation
        // As square root has 2 times less digits as original value
        // we can start with 2^(length of N1 / 2)
        BigInteger n0 = TWO.pow(in.bitLength() / 2);
        // Value of approximate value on previous step
        BigInteger np = in;

        do {
            // next approximation step: n0 = (n0 + in/n0) / 2
            n0 = n0.add(in.divide(n0)).divide(TWO);

            // compare current approximation with previous step
            c = np.compareTo(n0);

            // save value as previous approximation
            np = n0;

            // finish when previous step is equal to current
        } while (c != 0);

        return n0;
    }

}