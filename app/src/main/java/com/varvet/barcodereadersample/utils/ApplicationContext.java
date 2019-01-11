package com.varvet.barcodereadersample.utils;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class ApplicationContext extends Application {

    /** Instance of the current application. */
    private static ApplicationContext instance;

    /**
     * Constructor.
     */
    public ApplicationContext() {
        instance = this;
    }

    /**
     * Gets the application context.
     *
     * @return the application context
     */
    public static Context getContext() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    /**
     * display toast message
     *
     * @param data
     */
    public static void showToast(String data) {
        Toast.makeText(getContext(), data,
                Toast.LENGTH_SHORT).show();
    }

}