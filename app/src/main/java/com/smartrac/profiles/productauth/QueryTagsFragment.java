package com.smartrac.profiles.productauth;

import com.smartrac.profiles.productauth.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

public class QueryTagsFragment extends Fragment {

    public QueryTagsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	_view = inflater.inflate(R.layout.fragment_querytags, container, false);
        ((EditText) _view.findViewById(R.id.editTextCriteria)).setText(_sCriteria);        
        return _view;
    }
    
    public String getCriteriaType()
    {
    	Spinner spinner = ((Spinner) _view.findViewById(R.id.spinCriteria));
    	return (String)spinner.getItemAtPosition(spinner.getSelectedItemPosition());
    }
    
    public String getMaxCountValue()
    {
    	Spinner spinner = ((Spinner) _view.findViewById(R.id.spinMaxCount));
    	return (String)spinner.getItemAtPosition(spinner.getSelectedItemPosition());    	
    }
    
    public String getCriteriaValue()
    {
    	return ((EditText) _view.findViewById(R.id.editTextCriteria)).getText().toString();
    }    
    
    public void setCriteriaValue(String sCriteria)
    {
    	_sCriteria = sCriteria;
    }
    
    private View _view;
    private String _sCriteria;
}
