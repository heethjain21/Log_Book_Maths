package com.heethsapps.heeth.logarithmiccalculator;

import android.content.Context;
import android.content.SharedPreferences;

public class Theme {

    private String themeState;
    private String accentColor;
    private String theme;
    private final String darkThemeName;
    private final String lightThemeName;

    private final SharedPreferences accentColorSharedPreferences;
    private final SharedPreferences.Editor accentColorSharedPreferencesEditor;

    private final SharedPreferences themeStateSharedPreferences;
    private final SharedPreferences.Editor themeStateSharedPreferencesEditor;

    Theme(Context context) {

        darkThemeName = "DarkTheme";
        lightThemeName = "LightTheme";

        accentColorSharedPreferences = context.getSharedPreferences("accentColor", Context.MODE_PRIVATE);
        accentColorSharedPreferencesEditor = accentColorSharedPreferences.edit();

        themeStateSharedPreferences = context.getSharedPreferences("themeState", Context.MODE_PRIVATE);
        themeStateSharedPreferencesEditor = themeStateSharedPreferences.edit();
    }

    //Name of the light theme
    public String getLightThemeName() {
        return lightThemeName;
    }

    //Name of the dark theme
    public String getDarkThemeName() {
        return darkThemeName;
    }

    //Get saved accent color from Shared Preferences
    public String getAccentColor() {
        this.accentColor = accentColorSharedPreferences.getString("accentColor", "BrightBlue");
        return this.accentColor;
    }

    //Get saved theme state from Shared Preferences
    public String getThemeState() {
        this.themeState = themeStateSharedPreferences.getString("themeState", lightThemeName);
        return this.themeState;
    }

    //Merge "theme state" and "accent color" in one single string named "theme"
    public String getTheme() {
        this.accentColor = getAccentColor();
        this.themeState = getThemeState();
        this.theme = accentColor.concat(themeState);
        return this.theme;
    }

    //Save accent color in Shared Preferences
    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
        accentColorSharedPreferencesEditor.putString("accentColor", accentColor);
        accentColorSharedPreferencesEditor.commit();
    }

    //Save theme state i.e light or dark theme in Shared Preferences
    public void setThemeState(String themeState) {
        this.themeState = themeState;
        System.out.println("In Theme " + themeState);
        themeStateSharedPreferencesEditor.putString("themeState", themeState);
        themeStateSharedPreferencesEditor.commit();
    }

    //Call setThemeState() and pass the name of light theme
    public void setLightTheme() {
        setThemeState(getLightThemeName());
    }

    //Call setThemeState() and pass the name of dark theme
    public void setDarkTheme() {
        setThemeState(getDarkThemeName());
    }
}
