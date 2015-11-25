package com.smartrac.profiles.productauth;

import com.smartrac.profiles.productauth.R;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

public class ResultTagFragment extends Fragment {

    public ResultTagFragment() {
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_resulttag, container, false);
        ((TextView) rootView.findViewById(R.id.textViewUid)).setText("UID: " + _sUid);
        ((TextView) rootView.findViewById(R.id.textViewNtagVersion)).setText("Chip Type: " + _sNtagVersion);
        ((TextView) rootView.findViewById(R.id.textViewNxpSignature)).setText(_sNxpSignature);
        ((TextView) rootView.findViewById(R.id.textViewProfilesAuth)).setText(_sProfilesAuth);
        ((TextView) rootView.findViewById(R.id.textViewProductAuth)).setText(_sProductAuth);
        ((TextView) rootView.findViewById(R.id.textViewStatusMsg)).setText(_sStatusMsg);
        this.setImage((ImageView) rootView.findViewById(R.id.imageViewStatus), _imageStatus);
        this.setImage((ImageView) rootView.findViewById(R.id.imageViewNxpSignature), _imageNxpSignature);
        this.setImage((ImageView) rootView.findViewById(R.id.imageViewProfilesAuth), _imageProfilesAuth);
        this.setImage((ImageView) rootView.findViewById(R.id.imageViewProductAuth), _imageProductAuth);
        this.setImage((ImageView) rootView.findViewById(R.id.imageViewVerification), _imageVerification);
        return rootView;
    }

    public void setUid(String sUid) {
    	_sUid = sUid;
    }
    
    public void setNtagVersion(String sNtagVersion) {
    	_sNtagVersion = sNtagVersion;
    }
    
    public void setNxpSignatureMsg(String sNxpSignature) {
    	_sNxpSignature = sNxpSignature;
    }
    
    public void setProfilesAuthMsg(String sProfilesAuth) {
    	_sProfilesAuth = sProfilesAuth;
    }
    
    public void setProductAuthMsg(String sProductAuth) {
    	_sProductAuth = sProductAuth;
    }    
    
    public void setStatusMsg(String sStatusMsg) {
    	_sStatusMsg = sStatusMsg;
    }
    
    public void setStateImage(Drawable imageStatus)
    {
    	_imageStatus = imageStatus;
    }
    
    public void setNxpSignatureImage(Drawable imageNxpSignature)
    {
    	_imageNxpSignature = imageNxpSignature;
    }

    public void setProfilesAuthImage(Drawable imageProfilesAuth)
    {
    	_imageProfilesAuth = imageProfilesAuth;
    }
    
    public void setProductAuthImage(Drawable imageProductAuth)
    {
    	_imageProductAuth = imageProductAuth;
    }    
    
    public void setVerificationImage(Drawable imageVerification)
    {
    	_imageVerification = imageVerification;
    }
    
    private void setImage(ImageView iv, Drawable image)
    {
        if (image != null)
        {
        	iv.setVisibility(View.VISIBLE);
        	iv.setImageDrawable(image);
        }
        else
        {
        	iv.setVisibility(View.INVISIBLE);
        }    	
    }
    
    private Drawable _imageStatus;
    private Drawable _imageNxpSignature;
    private Drawable _imageProfilesAuth;
    private Drawable _imageProductAuth;
    private Drawable _imageVerification;
    private String _sUid;
    private String _sNtagVersion;
    private String _sNxpSignature;
    private String _sProfilesAuth;
    private String _sProductAuth;
    private String _sStatusMsg;
}

