package com.smartrac.profiles.productauth;

import com.smartrac.profiles.productauth.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	_view = inflater.inflate(R.layout.fragment_settings, container, false);
        ((EditText) _view.findViewById(R.id.editTextSettingsNetworkServer)).setText(_sServer);
        ((EditText) _view.findViewById(R.id.editTextSettingsNetworkUser)).setText(_sUser);
        ((EditText) _view.findViewById(R.id.editPasswordSettingsNetworkPassword)).setText(_sPassword);
        return _view;
    }
    
    public String getServer()
    {
    	return ((EditText) _view.findViewById(R.id.editTextSettingsNetworkServer)).getText().toString();
    }
    
    public String getUser()
    {
    	return ((EditText) _view.findViewById(R.id.editTextSettingsNetworkUser)).getText().toString();
    }
   
    public String getPassword()
    {
    	return ((EditText) _view.findViewById(R.id.editPasswordSettingsNetworkPassword)).getText().toString();
    }    
    
    public void setServer(String sServer)
    {
    	_sServer = sServer;
    }
    
    public void setUser(String sUser)
    {
    	_sUser = sUser;
    }
    
    public void setPassword(String sPassword)
    {
    	_sPassword = sPassword;
    }    
    
    private String _sServer;
    private String _sUser;
    private String _sPassword;
    private View _view;
}
