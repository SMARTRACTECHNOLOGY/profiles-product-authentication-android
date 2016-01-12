package com.smartrac.profiles.productauth;

import com.smartrac.profiles.productauth.R;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FoundBatchesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_foundbatches, container, false);
        _listViewResults = (ListView)rootView.findViewById(R.id.listviewResults);
        FoundBatchListAdapter adapter = new FoundBatchListAdapter(this.getActivity(), _aStatusImage, _asUid);
        _listViewResults.setAdapter(adapter);
        MainActivity activity = (MainActivity)this.getActivity();
        _listViewResults.setOnItemClickListener(activity);
        return rootView;
    }
    
    public void setStatusImages(Drawable[] aStatusImage) {
    	_aStatusImage = aStatusImage;
    }
    
    public void setUids(String[] asUid) {
    	_asUid = asUid;
    }

    private Drawable[] _aStatusImage;
    private String[] _asUid;
    private ListView _listViewResults;
}
