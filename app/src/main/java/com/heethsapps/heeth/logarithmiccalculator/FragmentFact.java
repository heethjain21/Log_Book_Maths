package com.heethsapps.heeth.logarithmiccalculator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
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

public class FragmentFact extends Fragment {

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;
    private ProgressBar calculatingProgressBar;

    private TextInputEditText factEditText;
    private TextView factTextView;

    private BigInteger lastAnsVal;
    private String lastStringVal;

    private Calculation calculation;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_fact, container, false);
        scrollView = rootView.findViewById(R.id.fragment_fact_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_fact_progress_bar);
        calculatingProgressBar = rootView.findViewById(R.id.calculating_progress_bar);
        loadScreen();

        return rootView;
    }

    public void mainMethod() {

        try {

            factEditText = (TextInputEditText) rootView.findViewById(R.id.factEditText);
            factTextView = (TextView) rootView.findViewById(R.id.factTextView);

            calculate();

            factEditText.addTextChangedListener(new TextWatcher() {
                @Override public void afterTextChanged(Editable s) { }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculatingProgressBar.setVisibility(View.GONE);
                    if (calculation != null)
                        calculation.cancel(true);
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
            String factNumString = factEditText.getText().toString();
            // Check if argument textfield is empty and/or base textfield is empty
            if (factNumString.length() == 0) {
                factTextView.setText("");
                factTextView.setVisibility(View.GONE);
            }
            // If not empty, perform calculations
            else {
                factTextView.setVisibility(View.VISIBLE);
                /******************* Calculate result *******************/
                BigInteger n = new BigInteger(factNumString);
                BigInteger ans = BigInteger.ONE;

                System.out.println("Input value: " + n.longValue());

                if (n.equals(BigInteger.ZERO)) {
                    factTextView.setText("The result is: 1");
                } else if (n.compareTo(new BigInteger("9223372036854775806")) > 1 || n.longValue() < 0 || n.toString().length() > 19) {
                    factTextView.setText("Maximum limit reached");
                } else {
                    calculation = new Calculation(n);
                    calculation.execute();
                    //for (int i = 1; i <= n.intValue(); i++) ans = ans.multiply(BigInteger.valueOf(i));

                    //BigDecimal ansf = new BigDecimal(ans);
                    //ansf = ansf.round(new MathContext(10));
                    //factTextView.setText("The result is: " + ansf);
                }

            }
        } catch (Exception e) {
            // Print exception
            e.printStackTrace();
            // Show error as it occured during calculation
            factTextView.setVisibility(View.VISIBLE);
            factTextView.setText("Some error occured while calculating, please try changing the values once again!");
        }

    }

    private class Calculation extends AsyncTask<String, Void, String> {

        BigInteger ans = BigInteger.ONE;
        BigInteger n;

        long tStart, tEnd, tDelta;
        double elapsedSeconds;

        Calculation(BigInteger n) {
            this.n = n;
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                for (long i = 1; i <= n.longValue(); i++) {
                    ans = ans.multiply(BigInteger.valueOf(i));
                    if (isCancelled()) break;
                }
            } catch (OutOfMemoryError oome) {
                oome.printStackTrace();
                ((MainActivity) getActivity()).showSnackBar(((MainActivity) getActivity()).getDrawerLayout(), R.string.memory);
            } catch (Exception e) {
                e.printStackTrace();
                ((MainActivity) getActivity()).showSnackBar(((MainActivity) getActivity()).getDrawerLayout(), R.string.error);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                calculatingProgressBar.setVisibility(View.GONE);
                factTextView.setVisibility(View.VISIBLE);

                BigDecimal finalAns = finalAns = new BigDecimal(ans);
                //f (n.longValue() > 10000) finalAns = finalAns.round(new MathContext(35660));
                finalAns = finalAns.round(new MathContext(10001));

                tEnd = System.currentTimeMillis();
                tDelta = tEnd - tStart;
                elapsedSeconds = (double) tDelta / (double) 1000;

                factTextView.setText("Time taken: " + elapsedSeconds + " seconds\n" + "The result is: " + finalAns);

                final String ansString = finalAns.toString();
                factTextView.setOnLongClickListener(new View.OnLongClickListener() {
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
            } catch (Exception e) {
                e.printStackTrace();
                ((MainActivity) getActivity()).showSnackBar(((MainActivity) getActivity()).getDrawerLayout(), R.string.memory);
            }
        }

        @Override
        protected void onPreExecute() {

            tStart = System.currentTimeMillis();

            calculatingProgressBar.setVisibility(View.VISIBLE);
            factTextView.setVisibility(View.GONE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }

}
