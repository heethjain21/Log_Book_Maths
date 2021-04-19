package com.heethsapps.heeth.logarithmiccalculator;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;


public class FragmentHome extends Fragment {

    private CardView[] cardViewButtons;
    private int[] cardViewId;

    private View rootView;

    private ScrollView scrollView;
    private ProgressBar progressBar;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        scrollView = rootView.findViewById(R.id.fragment_home_scrollview);
        progressBar = rootView.findViewById(R.id.fragment_home_progress_bar);

        loadScreen();
        return rootView;
    }

    public void mainMethod() {

        try {

            if (MainActivity.activityRecreated)
                MainActivity.activityRecreated = false;

            cardViewId = new int[]{R.id.logCard, R.id.alogCard, R.id.sinCard, R.id.cosCard, R.id.tanCard, R.id.powCard, R.id.squaresCard, R.id.sqrtCard, R.id.factCard,
                    R.id.reciCard, R.id.lsinCard, R.id.lcosCard, R.id.ltanCard, R.id.isinCard, R.id.icosCard, R.id.itanCard};
            cardViewButtons = new CardView[16];

            for (int i = 0; i < cardViewButtons.length; i++) {

                cardViewButtons[i] = rootView.findViewById(cardViewId[i]);
                cardViewButtons[i].setTag(i + 1);

                cardViewButtons[i].setOnClickListener(v -> {
                    int tag = Integer.parseInt(v.getTag().toString());
                    System.out.println(cardViewId[tag - 1] + " clicked!");
                    fragmentStart(tag);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            ((MainActivity) getActivity()).showSnackBar(rootView, R.string.error);
        }

    }

    public void fragmentStart(int tag) {

        switch (tag) {
            case 1:
                ((MainActivity) getActivity()).logFragment();
                break;
            case 2:
                ((MainActivity) getActivity()).alogFragment();
                break;
            case 3:
                ((MainActivity) getActivity()).sinFragment();
                break;
            case 4:
                ((MainActivity) getActivity()).cosFragment();
                break;
            case 5:
                ((MainActivity) getActivity()).tanFragment();
                break;
            case 6:
                ((MainActivity) getActivity()).powFragment();
                break;
            case 7:
                ((MainActivity) getActivity()).squaresFragment();
                break;
            case 8:
                ((MainActivity) getActivity()).sqrtFragment();
                break;
            case 9:
                ((MainActivity) getActivity()).factFragment();
                break;
            case 10:
                ((MainActivity) getActivity()).reciFragment();
                break;
            case 11:
                ((MainActivity) getActivity()).lsinFragment();
                break;
            case 12:
                ((MainActivity) getActivity()).lcosFragment();
                break;
            case 13:
                ((MainActivity) getActivity()).ltanFragment();
                break;
            case 14:
                ((MainActivity) getActivity()).isinFragment();
                break;
            case 15:
                ((MainActivity) getActivity()).icosFragment();
                break;
            case 16:
                ((MainActivity) getActivity()).itanFragment();
                break;
            /*case 2:
                ((MainActivity) getActivity()).step2Fragment();
                break;
            case 3:
                ((MainActivity) getActivity()).step3Fragment();
                break;
            case 4:
                ((MainActivity) getActivity()).step4Fragment();
                break;
            case 5:
                ((MainActivity) getActivity()).step5Fragment();
                break;
            case 6:
                ((MainActivity) getActivity()).step6Fragment();
                break;
            case 7:
                ((MainActivity) getActivity()).step7Fragment();
                break;
            case 8:
                ((MainActivity) getActivity()).step8Fragment();
                break;
            case 9:
                ((MainActivity) getActivity()).step9Fragment();
                break;
            case 10:
                ((MainActivity) getActivity()).commandsAdbFragment();
                break;
            case 15:
                ((MainActivity) getActivity()).showSnackBar(rootView, R.string.comingSoon);
                break;*/
        }
    }

    private void loadScreen() {

        scrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        new CountDownTimer(MainActivity.loadingTime, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                scrollView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                mainMethod();
            }
        }.start();
    }

}