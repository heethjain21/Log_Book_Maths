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


public class FragmentCosInv extends Fragment {

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputEditText cosinvEditText;
    private TextView cosinvTextView;

    private BigDecimal lastAnsVal;
    private String lastStringVal;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInscosceState) {

        rootView = inflater.inflate(R.layout.fragment_cos_inv, container, false);
        scrollView = rootView.findViewById(R.id.fragment_cosinv_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_cosinv_progress_bar);
        loadScreen();

        return rootView;
    }

    public void mainMethod() {

        try {

            cosinvEditText = (TextInputEditText) rootView.findViewById(R.id.cosinvEditText);
            cosinvTextView = (TextView) rootView.findViewById(R.id.cosinvTextView);

            // This is useful if app has been reloaded
            calculate();

            cosinvEditText.addTextChangedListener(new TextWatcher() {
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

            String cosinvNumString = cosinvEditText.getText().toString();

            // Check if argument textfield is empty and/or base textfield is empty
            if (cosinvNumString.length() == 0) {
                cosinvTextView.setText("");
                cosinvTextView.setVisibility(View.GONE);
            }
            // If not empty, perform calculations
            else {

                cosinvTextView.setVisibility(View.VISIBLE);

                /******************* Calculate result *******************/
                // Get ans in bigdecimal
                BigDecimal n = new BigDecimal(cosinvNumString);
                BigDecimal ans = BigDecimal.ONE;
                double num = n.doubleValue();
                double cosInv;
                String stringAns;
                if (num < -1 || num > 1) {
                    cosinvTextView.setText("Error: Please enter a value between -1 and 1");
                    stringAns = "Error";
                } else {
                    // Calculate square
                    cosInv = Math.toDegrees(Math.acos(num));
                    ans = new BigDecimal(cosInv);
                    ans = ans.multiply(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                    ans = new BigDecimal(ans.toBigInteger());
                    ans = ans.divide(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)), new MathContext(MainActivity.contextPrecision));

                    cosinvTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans) + "°");
                    stringAns = ans.toString();

                }
                final String ansString = stringAns;
                cosinvTextView.setOnLongClickListener(new View.OnLongClickListener() {
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
                cosinvTextView.setVisibility(View.GONE);
            }
        } catch (Exception e) {

            // Print exception
            e.printStackTrace();
            // Show error as it occured during calculation
            cosinvTextView.setVisibility(View.VISIBLE);
            cosinvTextView.setText("Some error occured while calculating, please try changing the values once again!");
        }

    }

}