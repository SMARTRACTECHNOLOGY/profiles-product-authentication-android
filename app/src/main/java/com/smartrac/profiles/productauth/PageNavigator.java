package com.smartrac.profiles.productauth;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartrac.profiles.productauth.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageNavigator extends Fragment {


    public PageNavigator() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_page_navigator, container, false);
    }

}
