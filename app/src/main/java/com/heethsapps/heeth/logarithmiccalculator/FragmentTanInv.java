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


public class FragmentTanInv extends Fragment {

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;

    private TextInputEditText taninvEditText;
    private TextView taninvTextView;

    private BigDecimal lastAnsVal;
    private String lastStringVal;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_tan_inv, container, false);
        scrollView = rootView.findViewById(R.id.fragment_taninv_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_taninv_progress_bar);
        loadScreen();

        return rootView;
    }

    public void mainMethod() {

        try {

            taninvEditText = (TextInputEditText) rootView.findViewById(R.id.taninvEditText);
            taninvTextView = (TextView) rootView.findViewById(R.id.taninvTextView);

            calculate();

            taninvEditText.addTextChangedListener(new TextWatcher() {
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

            String taninvNumString = taninvEditText.getText().toString();

            // Check if argument textfield is empty and/or base textfield is empty
            if (taninvNumString.length() == 0) {
                taninvTextView.setText("");
                taninvTextView.setVisibility(View.GONE);
            }
            // If not empty, perform calculations
            else {

                taninvTextView.setVisibility(View.VISIBLE);

                /******************* Calculate result *******************/
                // Get ans in bigdecimal
                BigDecimal n = new BigDecimal(taninvNumString);
                BigDecimal ans = BigDecimal.ONE;
                double num = n.doubleValue();
                double tanInv;
                String stringAns;

                // Calculate square
                tanInv = Math.toDegrees(Math.atan(num));
                ans = new BigDecimal(tanInv);

                if (tanInv == 90) {
                    ans = new BigDecimal(89.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999);
                } else if (tanInv == -90) {
                    ans = new BigDecimal(-89.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999);
                }

                ans = ans.multiply(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)));
                ans = new BigDecimal(ans.toBigInteger());
                ans = ans.divide(new BigDecimal(Math.pow(10, MainActivity.decimalPrecision)), new MathContext(MainActivity.contextPrecision));

                taninvTextView.setText("The result is: " + String.format("%." + MainActivity.decimalPrecision + "f", ans) + "°");
                stringAns = ans.toString();

                final String ansString = stringAns;
                taninvTextView.setOnLongClickListener(new View.OnLongClickListener() {
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
                taninvTextView.setVisibility(View.GONE);
            }
        } catch (Exception e) {

            // Print exception
            e.printStackTrace();
            // Show error as it occured during calculation
            taninvTextView.setVisibility(View.VISIBLE);
            taninvTextView.setText("Some error occured while calculating, please try changing the values once again!");
        }

    }

}