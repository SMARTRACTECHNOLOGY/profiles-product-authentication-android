package com.smartrac.profiles.productauth;

import com.smartrac.profiles.productauth.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScanTagFragment extends Fragment {

    public ScanTagFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scantag, container, false);
        return rootView;
    }
}
