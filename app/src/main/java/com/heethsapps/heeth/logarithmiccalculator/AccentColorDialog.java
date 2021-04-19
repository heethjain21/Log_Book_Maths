package com.heethsapps.heeth.logarithmiccalculator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class AccentColorDialog extends AppCompatDialogFragment {

    private final ImageButton[] accentButton;
    private final ArrayList<String> accentList;
    private final int accentCount;
    private int accentIndex;
    private int accentId;

    private AlertDialog.Builder builder;
    private LayoutInflater inflater;
    private View view;

    private AccentColorDialogListener accentColorDialogListener;

    public AccentColorDialog() {

        accentCount = 25;
        accentIndex = 0;

        accentButton = new ImageButton[25];
        accentList = new ArrayList<>(accentCount);

        accentList.addAll(Arrays.asList("Lemon", "Yellow", "Amber", "Orange", "DeepOrange",
                "Pink", "Red", "BloodRed", "BrightRed", "Brown",
                "Lime", "LightGreen", "LeafGreen", "Green", "DarkGreen",
                "Cyan", "LightBlue", "BrightBlue", "Blue", "Indigo",
                "DeepPurple", "BrightPurple", "Purple", "DarkPurple", "SkyPurple"));

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        builder = new AlertDialog.Builder(getActivity());
        inflater = getActivity().getLayoutInflater();

        view = inflater.inflate(R.layout.theme_selector, null);
        builder.setView(view);

        while (accentIndex < accentCount) {

            //Create new R.id (in integer form) of the theme_selector layout whose id will be "accentButton" + (accentList's string)
            accentId = getResources().getIdentifier("accentButton".concat(accentList.get(accentIndex)), "id", "com.heethsapps.heeth.logarithmiccalculator");
            accentButton[accentIndex] = view.findViewById(accentId);

            //Set tag as "accentList's string" to accentButton's respective index position
            accentButton[accentIndex].setTag(accentList.get(accentIndex));
            accentButton[accentIndex].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Call "applyAccentColor" function and pass the value of tag i.e value of accentList matching to the corresponding value of accentButton which was clicked
                    accentColorDialogListener.applyAccentColor(v.getTag().toString());
                }
            });

            accentIndex++;
        }

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        accentColorDialogListener = (AccentColorDialogListener) context;
    }

    public interface AccentColorDialogListener {
        void applyAccentColor(String accentColor);
    }

}
