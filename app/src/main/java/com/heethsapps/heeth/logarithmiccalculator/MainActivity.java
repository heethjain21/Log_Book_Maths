package com.heethsapps.heeth.logarithmiccalculator;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AccentColorDialog.AccentColorDialogListener, RoundingDialog.RoundingDialogListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SwitchCompat darkModeSwitch;
    private TextView roundingTextView;
    private Menu menu;

    // Theme related variables
    private String theme;
    private Theme themeObject;
    private AccentColorDialog accentColorDialog;
    private RoundingDialog roundingDialog;

    public static boolean activityRecreated = false;
    public static boolean isTransactionSafe;
    public static boolean isTransactionPending;

    public static int loadingTime = 300;
    public static int backPressed = 0;
    public static int decimalPrecision = 4;
    public static int contextPrecision = 100;

    private Snackbar snackBar;
    private TypedArray array;

    private int checkedNavItem;
    private int accentColorId;

    private FragmentManager manager;
    private Fragment currentFragment;

    private Bundle savedInstanceState;
    private FirebaseAnalytics mFirebaseAnalytics;

    private static boolean appLaunched = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Set current theme
        themeSetter();
        // Initialize the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Load mainMethod from aSync
        new LoadingScreen().execute();
        // Set current savedInstanceState
        this.savedInstanceState = savedInstanceState;
        // Call In App Review API
        inAppReview();

    }

    public void mainMethod() {

        try {

            // Toolbar related initializations
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // Layout related initializations
            drawerLayout = findViewById(R.id.drawer_layout);
            toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            // Firebase Analytics initialization
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

            // Theme related calls
            array = getTheme().obtainStyledAttributes(new int[]{R.attr.accentTextColor});
            accentColorId = array.getResourceId(0, 0);
            array.recycle();
            toggle.getDrawerArrowDrawable().setColor(getResources().getColor(accentColorId));

            // Navigation View related calls
            navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            // Menu initialization and rounding menu item in navigation drawer related calls
            menu = navigationView.getMenu();
            MenuItem menuItem = menu.findItem(R.id.nav_round);
            roundingTextView =  menuItem.getActionView().findViewById(R.id.roundingTextView);
            roundingTextView.setText(decimalPrecision + "");

            // Dark Theme Switch Compat related function
            darkModeSwitchCompat();

            new Thread( () ->
                drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                    @Override public void onDrawerSlide(@NonNull View view, float v) { }
                    @Override public void onDrawerOpened(@NonNull View view) { }
                    @Override
                    public void onDrawerClosed(@NonNull View view) {
                        switch (checkedNavItem) {
                            // Calculators and Home Screen
                            case R.id.nav_home: homeFragment(); break;
                            case R.id.nav_log: logFragment(); break;
                            case R.id.nav_alog: alogFragment(); break;
                            case R.id.nav_sin: sinFragment(); break;
                            case R.id.nav_cos: cosFragment(); break;
                            case R.id.nav_tan: tanFragment(); break;
                            case R.id.nav_pow: powFragment(); break;
                            case R.id.nav_squares: squaresFragment(); break;
                            case R.id.nav_sqrt: sqrtFragment(); break;
                            case R.id.nav_fact: factFragment(); break;
                            case R.id.nav_reci: reciFragment(); break;
                            case R.id.nav_lsin: lsinFragment(); break;
                            case R.id.nav_lcos: lcosFragment(); break;
                            case R.id.nav_ltan: ltanFragment(); break;
                            case R.id.nav_isin: isinFragment(); break;
                            case R.id.nav_icos: icosFragment(); break;
                            case R.id.nav_itan: itanFragment(); break;
                            // Decimal Rounding Places Setting
                            case R.id.nav_round: openRoundingDialog(); break;
                            // Accent Colour Setting
                            case R.id.nav_accent: {
                                openAccentColorDialog();
                                checkedNavItem = 0;
                            } break;
                            // Light/Dark Theme Setting
                            case R.id.nav_theme: {
                                System.out.println("Restarted Fragment");
                                if (themeObject.getThemeState().equals(themeObject.getDarkThemeName())) {
                                    themeObject.setLightTheme();
                                    darkModeSwitch.setChecked(false);
                                } else {
                                    themeObject.setDarkTheme();
                                    darkModeSwitch.setChecked(true);
                                }
                                reloadApp();
                                checkedNavItem = 0;
                            } break;
                            // Open privacy policy
                            case R.id.nav_policy: {
                                Uri uri = Uri.parse("https://heethjain21.wixsite.com/logbookmaths/privacy-policy");
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    showSnackBar(drawerLayout, R.string.webBrowserNotFound);
                                }
                            } break;
                            // Open play store for app rating
                            case R.id.nav_rate: {
                                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    showSnackBar(drawerLayout, R.string.playStoreNotFound);
                                }
                            } break;
                            // Default case
                            default: { /*Do nothing*/ }break;
                        }
                    }
                    @Override public void onDrawerStateChanged(int i) { }
                })
            ).start();
            // Check current instance and set toolbar title and nav items checked accordingly
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHome()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                navigationView.setCheckedItem(R.id.nav_home);
                toolbar.setTitle(R.string.app_name);
            }
            // Get current fragment instance from fragment manager
            manager = getSupportFragmentManager();
            currentFragment = manager.findFragmentById(R.id.fragment_container);
            // Set checked value of navigation items and set title of fragment at toolbar according to above fragment
            if (currentFragment instanceof FragmentHome) {
                navigationView.getMenu().getItem(0).setChecked(true);
                toolbar.setTitle(R.string.app_name);
            } else if (currentFragment instanceof FragmentLog) {
                navigationView.getMenu().getItem(1).setChecked(true);
                toolbar.setTitle(R.string.logTitle);
            } else if (currentFragment instanceof FragmentAlog) {
                navigationView.getMenu().getItem(2).setChecked(true);
                toolbar.setTitle(R.string.alogTitle);
            } else if (currentFragment instanceof FragmentSin) {
                navigationView.getMenu().getItem(3).setChecked(true);
                toolbar.setTitle(R.string.sinTitle);
            } else if (currentFragment instanceof FragmentCos) {
                navigationView.getMenu().getItem(4).setChecked(true);
                toolbar.setTitle(R.string.cosTitle);
            } else if (currentFragment instanceof FragmentTan) {
                navigationView.getMenu().getItem(5).setChecked(true);
                toolbar.setTitle(R.string.tanTitle);
            } else if (currentFragment instanceof FragmentPow) {
                navigationView.getMenu().getItem(6).setChecked(true);
                toolbar.setTitle(R.string.powTitle);
            } else if (currentFragment instanceof FragmentSquares) {
                navigationView.getMenu().getItem(7).setChecked(true);
                toolbar.setTitle(R.string.squaresTitle);
            } else if (currentFragment instanceof FragmentSqrt) {
                navigationView.getMenu().getItem(8).setChecked(true);
                toolbar.setTitle(R.string.sqrtTitle);
            } else if (currentFragment instanceof FragmentFact) {
                navigationView.getMenu().getItem(9).setChecked(true);
                toolbar.setTitle(R.string.factTitle);
            } else if (currentFragment instanceof FragmentReci) {
                navigationView.getMenu().getItem(10).setChecked(true);
                toolbar.setTitle(R.string.reciTitle);
            } else if (currentFragment instanceof FragmentLogSin) {
                navigationView.getMenu().getItem(11).setChecked(true);
                toolbar.setTitle(R.string.lsinTitle);
            } else if (currentFragment instanceof FragmentLogCos) {
                navigationView.getMenu().getItem(12).setChecked(true);
                toolbar.setTitle(R.string.lcosTitle);
            } else if (currentFragment instanceof FragmentLogTan) {
                navigationView.getMenu().getItem(13).setChecked(true);
                toolbar.setTitle(R.string.ltanTitle);
            } else if (currentFragment instanceof FragmentSinInv) {
                navigationView.getMenu().getItem(14).setChecked(true);
                toolbar.setTitle(R.string.isinTitle);
            } else if (currentFragment instanceof FragmentCosInv) {
                navigationView.getMenu().getItem(15).setChecked(true);
                toolbar.setTitle(R.string.icosTitle);
            } else if (currentFragment instanceof FragmentTanInv) {
                navigationView.getMenu().getItem(16).setChecked(true);
                toolbar.setTitle(R.string.itanTitle);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showSnackBar(drawerLayout, R.string.error);
        }
    }

    private void inAppReview() {

        // Check if app has launched already or it is being reloaded from memory or some other state
        if (appLaunched) {
            // Since it has been launched, do the following
            appLaunched = false;
            // Save the app launches count
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            int totalCount = prefs.getInt("launches", 0);
            totalCount++;
            editor.putInt("launches", totalCount);
            editor.apply();

            /* TODO: In App Review API Implementation
            if (totalCount % 5 == 0) {
                reviewManager = ReviewManagerFactory.create(this);
                Task<ReviewInfo> request = reviewManager.requestReviewFlow();
                request.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) reviewInfo = task.getResult();
                    else reviewInfo = null;
                });
                if (reviewInfo != null) {
                    Task<Void> flow = reviewManager.launchReviewFlow(this, reviewInfo);
                    flow.addOnCompleteListener(stask -> {});
                }
            }
            */
        }
    }

    // onPostResume is called only when the activity's state is completely restored. In this we will set our boolean variable to true. Indicating that transaction is safe now
    public void onPostResume() {
        super.onPostResume();
        isTransactionSafe = true;
        /* Here after the activity is restored we check if there is any transaction pending from the last restoration*/
        if (isTransactionPending) {
            loadFragments();
        }
    }

    // onPause is called just before the activity moves to background and also before onSaveInstanceState. In this we will mark the transaction as unsafe
    public void onPause() {
        super.onPause();
        isTransactionSafe = false;
    }

    // On clicking the "hamburger" (3 horizontal lines each one below another on the top left of action bar) app_icon
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // On "back" button pressed
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);

        } else {
            if (backPressed == 0) {
                backPressed++;
                showSnackBar(drawerLayout, R.string.backPressed);
            } else {
                backPressed = 0;
                super.onBackPressed();
            }
        }
    }

    // Dark Theme Switch Compat related calls
    private void darkModeSwitchCompat() {

        MenuItem menuItem = menu.findItem(R.id.nav_theme);
        darkModeSwitch = menuItem.getActionView().findViewById(R.id.dark_mode_switch);

        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                // Check the value of dark theme switch and set theme accordingly
                if (isChecked) themeObject.setDarkTheme();
                else themeObject.setLightTheme();
                reloadApp();
            }
        });
    }

    //Restart the HomeActvity/App
    public void reloadApp() {
        activityRecreated = !(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof FragmentHome);
        this.recreate();
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    //Open up the dialog for choosing the accent color
    private void openAccentColorDialog() {
        accentColorDialog = new AccentColorDialog();
        accentColorDialog.show(getSupportFragmentManager(), "accent color dialog");
        int width = (int) (340 * Resources.getSystem().getDisplayMetrics().density);
        getSupportFragmentManager().executePendingTransactions();
        accentColorDialog.getDialog().getWindow().setLayout(width, accentColorDialog.getDialog().getWindow().getAttributes().height);

    }

    //Open up the dialog for choosing the rounding values
    private void openRoundingDialog() {
        roundingDialog = new RoundingDialog();
        roundingDialog.show(getSupportFragmentManager(), "rounding dialog");
        int width = (int) (340 * Resources.getSystem().getDisplayMetrics().density);
        getSupportFragmentManager().executePendingTransactions();
        roundingDialog.getDialog().getWindow().setLayout(width, roundingDialog.getDialog().getWindow().getAttributes().height);
    }

    //Apply Theme which is chosen
    private void themeSetter() {
        themeObject = new Theme(getApplicationContext());
        theme = themeObject.getTheme();
        System.out.println("Current Theme " + theme);
        int style = getResources().getIdentifier(theme, "style", getPackageName());
        setTheme(style);

    }

    // Apply the rounding value which was chosen
    @Override
    public void applyRoundingValue(int value) {
        decimalPrecision = value;
        roundingTextView.setText(decimalPrecision + "");
        System.out.println("Decimal Precision: " + decimalPrecision);
        roundingDialog.dismiss();
        reloadApp();
    }

    //Apply the accent color which is chosen
    @Override
    public void applyAccentColor(String accentColor) {
        themeObject.setAccentColor(accentColor);
        accentColorDialog.dismiss();
        reloadApp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        checkedNavItem = menuItem.getItemId();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    // Fragments switch initial part code
    public void homeFragment() { checkedNavItem = R.id.nav_home; loadFragments(); }
    public void logFragment() { checkedNavItem = R.id.nav_log; loadFragments(); }
    public void alogFragment() { checkedNavItem = R.id.nav_alog; loadFragments(); }
    public void sinFragment() { checkedNavItem = R.id.nav_sin; loadFragments(); }
    public void cosFragment() { checkedNavItem = R.id.nav_cos; loadFragments(); }
    public void tanFragment() { checkedNavItem = R.id.nav_tan; loadFragments(); }
    public void powFragment() { checkedNavItem = R.id.nav_pow; loadFragments(); }
    public void squaresFragment() { checkedNavItem = R.id.nav_squares; loadFragments(); }
    public void sqrtFragment() { checkedNavItem = R.id.nav_sqrt; loadFragments(); }
    public void factFragment() { checkedNavItem = R.id.nav_fact; loadFragments(); }
    public void reciFragment() { checkedNavItem = R.id.nav_reci; loadFragments(); }
    public void lsinFragment() { checkedNavItem = R.id.nav_lsin; loadFragments(); }
    public void lcosFragment() { checkedNavItem = R.id.nav_lcos; loadFragments(); }
    public void ltanFragment() { checkedNavItem = R.id.nav_ltan; loadFragments(); }
    public void isinFragment() { checkedNavItem = R.id.nav_isin; loadFragments(); }
    public void icosFragment() { checkedNavItem = R.id.nav_icos; loadFragments(); }
    public void itanFragment() { checkedNavItem = R.id.nav_itan; loadFragments(); }

    private void loadFragments() {
        // Check is it is safe to change fragments
        if (isTransactionSafe) {
            // Since it is safe, we can now switch fragments
            switch (checkedNavItem) {
                case R.id.nav_home: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentHome()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_home);
                    toolbar.setTitle(R.string.app_name);
                } break;
                case R.id.nav_log: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentLog()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_log);
                    toolbar.setTitle(R.string.logTitle);
                } break;
                case R.id.nav_alog: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentAlog()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_alog);
                    toolbar.setTitle(R.string.alogTitle);
                } break;
                case R.id.nav_sin: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentSin()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_sin);
                    toolbar.setTitle(R.string.sinTitle);
                } break;
                case R.id.nav_cos: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentCos()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_cos);
                    toolbar.setTitle(R.string.cosTitle);
                } break;
                case R.id.nav_tan: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTan()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_tan);
                    toolbar.setTitle(R.string.tanTitle);
                } break;
                case R.id.nav_pow: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentPow()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_pow);
                    toolbar.setTitle(R.string.powTitle);
                } break;
                case R.id.nav_squares: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentSquares()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_squares);
                    toolbar.setTitle(R.string.squaresTitle);
                } break;
                case R.id.nav_sqrt: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentSqrt()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_sqrt);
                    toolbar.setTitle(R.string.sqrtTitle);
                } break;
                case R.id.nav_fact: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentFact()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_fact);
                    toolbar.setTitle(R.string.factTitle);
                } break;
                case R.id.nav_reci: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentReci()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_reci);
                    toolbar.setTitle(R.string.reciTitle);
                } break;
                case R.id.nav_lsin: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentLogSin()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_lsin);
                    toolbar.setTitle(R.string.lsinTitle);
                } break;
                case R.id.nav_lcos: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentLogCos()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_lcos);
                    toolbar.setTitle(R.string.lcosTitle);
                } break;
                case R.id.nav_ltan: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentLogTan()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_ltan);
                    toolbar.setTitle(R.string.ltanTitle);
                } break;
                case R.id.nav_isin: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentSinInv()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_isin);
                    toolbar.setTitle(R.string.isinTitle);
                } break;
                case R.id.nav_icos: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentCosInv()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_icos);
                    toolbar.setTitle(R.string.icosTitle);
                } break;
                case R.id.nav_itan: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTanInv()).setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
                    navigationView.setCheckedItem(R.id.nav_itan);
                    toolbar.setTitle(R.string.itanTitle);
                } break;
            }
            checkedNavItem = 0;
            backPressed = 0;
        } else {
            isTransactionPending = true;
        }
    }

    // Display a snackbar on the bottom os the screen with current theme settings
    public void showSnackBar(View view, int id) {
        // Initialize snackbar in current view (drawerLayout or some other layout)
        snackBar = Snackbar.make(view, id, Snackbar.LENGTH_SHORT);
        // Initialize text in the snackbar
        TextView textView = snackBar.getView().findViewById(R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(accentColorId));
        // Get theme values based on current theme
        array = getTheme().obtainStyledAttributes(new int[]{R.attr.primaryColor});
        int backgroundColor = array.getResourceId(0, 0);
        // Recycle/Destroy the array
        array.recycle();
        snackBar.getView().setBackgroundColor(getResources().getColor(backgroundColor));

        snackBar.show();
    }

    // Disable keyboard by default on loading fragment
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    // Asynctask to manage loading screen for the app, and to take care that app restarts from the same position in fragment
    private class LoadingScreen extends AsyncTask<String, Void, String> {
        @Override protected String doInBackground(String... params) {
            return null;
        }
        @Override protected void onPostExecute(String result) {
            mainMethod();
        }
        @Override protected void onPreExecute() { }
        @Override protected void onProgressUpdate(Void... values) { }
    }

}
