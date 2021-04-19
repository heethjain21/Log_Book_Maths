package com.heethsapps.heeth.logarithmiccalculator;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.xw.repo.BubbleSeekBar;

public class RoundingDialog extends AppCompatDialogFragment {

    private AlertDialog.Builder builder;
    private LayoutInflater inflater;
    private View view;

    private int progress = 0;

    private BubbleSeekBar roundingSelector;

    private RoundingDialogListener roundingDialogListener;

    public RoundingDialog() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        builder = new AlertDialog.Builder(getActivity());
        inflater = getActivity().getLayoutInflater();

        view = inflater.inflate(R.layout.rounding_selector, null);
        builder.setView(view);

        roundingSelector = view.findViewById(R.id.roundingSelector);
        roundingSelector.setProgress(MainActivity.decimalPrecision);
        System.out.println("Rounding Value: " + roundingSelector.getProgress());

        roundingSelector.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    // Do nothing
                    case MotionEvent.ACTION_DOWN: break;
                    // Case when the finger pressed is released, change the rounding value
                    case MotionEvent.ACTION_UP:
                        progress = roundingSelector.getProgress();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        roundingDialogListener.applyRoundingValue(progress);
                        break;
                }
                return false;
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        roundingDialogListener = (RoundingDialogListener) context;
    }

    public interface RoundingDialogListener {
        void applyRoundingValue(int value);
    }

}
