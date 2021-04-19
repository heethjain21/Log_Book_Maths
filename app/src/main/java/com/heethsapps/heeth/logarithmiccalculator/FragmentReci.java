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


public class FragmentReci extends Fragment {

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputEditText reciEditText;
    private TextView reciTextView;

    private BigDecimal lastAnsVal;
    private String lastStringVal;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_reci, container, false);
        scrollView = rootView.findViewById(R.id.fragment_reci_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_reci_progress_bar);
        loadScreen();

        return rootView;
    }

    public void mainMethod() {

        try {

            reciEditText = (TextInputEditText) rootView.findViewById(R.id.reciEditText);
            reciTextView = (TextView) rootView.findViewById(R.id.reciTextView);

            // This is useful when the app reloads
            calculate();

            reciEditText.addTextChangedListener(new TextWatcher() {
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

            String reciNumString = reciEditText.getText().toString();

            // Check if argument textfield is empty and/or base textfield is empty
            if (reciNumString.length() == 0) {
                reciTextView.setText("");
                reciTextView.setVisibility(View.GONE);
            }
            // If not empty, perform calculations
            else {

                reciTextView.setVisibility(View.VISIBLE);

                /******************* Calculate result *******************/
                // Get ans in bigdecimal
                BigDecimal n = new BigDecimal(reciNumString);
                String stringAns;
                if (n.equals(BigDecimal.ZERO)) {
                    reciTextView.setText("The result is: Infinity");
                    stringAns = "Infinity";
                } else {
                    // Calculate square
                    BigDecimal ans = BigDecimal.ONE.divide(n, new MathContext(MainActivity.contextPrecision));

                    ans = ans.multiply(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                    ans = new BigDecimal(ans.toBigInteger());
                    ans = ans.divide(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)), new MathContext(MainActivity.contextPrecision));

                    reciTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans));
                    stringAns = ans.toString();

                }
                final String ansString = stringAns;
                reciTextView.setOnLongClickListener(new View.OnLongClickListener() {
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
            if (nfe.getMessage().equals("For input string: \"\"")) {
                reciTextView.setVisibility(View.GONE);
            }
        } catch (Exception e) {

            // Print exception
            e.printStackTrace();
            // Show error as it occured during calculation
            reciTextView.setVisibility(View.VISIBLE);
            reciTextView.setText("Some error occured while calculating, please try changing the values once again!");
        }

    }

}