package com.smartrac.profiles.productauth;

import com.smartrac.profiles.productauth.R;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        ((TextView)rootView.findViewById(R.id.textViewWelcome)).setMovementMethod(LinkMovementMethod.getInstance());
        return rootView;
    }
}
