package com.example.lifefit.utils;

import android.graphics.Color;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

public class ViewHandler {
    public static void lockInputLayout(TextInputLayout view) {
        view.setEnabled(false);
        view.setFocusable(false);
//        view.setBackgroundColor(Color.TRANSPARENT);
    }

    public static void lockInputLayout(DatePicker view) {
        view.setEnabled(false);
        view.setFocusable(false);
    }

    public static void lockInputLayout(TextView view) {
        view.setEnabled(false);
        view.setFocusable(false);
        view.setClickable(false);
    }

    public static void unlockInputLayout(TextInputLayout view) {
        int systemBackgroundColor = ContextCompat.getColor(view.getContext(), android.R.color.background_light);

        view.setEnabled(true);
        view.setFocusable(true);
        view.setClickable(true);
//        view.setBackgroundColor(systemBackgroundColor);
    }

    public static void unlockInputLayout(DatePicker view) {
        view.setEnabled(true);
        view.setFocusable(true);
    }

    public static void unlockInputLayout(TextView view) {
        view.setEnabled(true);
        view.setFocusable(true);
    }

    public static void lockButton(Button button) {
        button.setClickable(false);
    }

    public static void unlockButton(Button button) {
        button.setClickable(true);
    }
}
