package com.smartrac.profiles.productauth;

import java.io.IOException;
import java.util.HashMap;

import com.smartrac.profiles.productauth.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;

import com.smartrac.nfc.NfcNtag;
import com.smartrac.nfc.NfcNtagVersion;
import net.smartcosmos.android.utility.AsciiHexConverter;
import net.smartcosmos.android.utility.IntentLauncher;
import net.smartcosmos.android.ProfilesRestClient;
import net.smartcosmos.android.ProfilesRestClient.ProfilesRestResult;
import net.smartcosmos.android.ProfilesRestClient.ProfilesAuthOtpState;
import net.smartcosmos.android.ProfilesQueryTagProperty;

public class MainActivity extends Activity {
	
	// debug stuff
	static final boolean DEBUG = true;
	static final String TAG = MainActivity.class.getSimpleName();
	static final String PREFS_NAME = "Settings";
	
	public static final int IMAGE_NONE = 0;
    public static final int IMAGE_OK = 1;
    public static final int IMAGE_WARNING = 2;
    public static final int IMAGE_ERROR = 3;
    public static final int IMAGE_CERTIFIED = 4;
    public static final int IMAGE_INVALID = 5;
    
	// state machine
	final int FWSTATE_SCANTAG = 1;
	final int FWSTATE_RESULTTAG = 2;
	final int FWSTATE_QUERYTAGS = 3;
	final int FWSTATE_FOUNDTAGS = 4;
	final int FWSTATE_WELCOME = 250;
	final int FWSTATE_SETTINGS = 255;
	
	int iState = FWSTATE_WELCOME;
	int iImageStatus = IMAGE_NONE;
	int iImageNxpSignature = IMAGE_NONE;
	int iImageProfilesAuth = IMAGE_NONE;
	int iImageProductAuth = IMAGE_NONE;
	int iImageVerification = IMAGE_NONE;
	
	// global vars
	private Drawable[] aImagesState;
	
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private IntentFilter filterNdef;
	private IntentFilter filterTag;
	private IntentFilter filterTech;
	private IntentFilter[] aFilters;
	private String[][] aTechList;
	
	private String currentUid;
	private String currentNtagVersion;
	private String currentNxpSignatureMsg;
	private String currentProfilesAuthMsg;
	private String currentProductAuthMsg;
	private String currentStatusMsg;
	
	private String server;
	private String user;
	private String password;
	
	private ProfilesQueryTagProperty criteriaType;
	private int queryMaxCount = 100;
	private String criteriaValue = "";
	private String[] foundTagIds;
	
	private NfcNtag ntag;
	private ProgressDialog progressDlg;
	private byte[] uid;
	private boolean settingsValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        progressDlg = new ProgressDialog(this);
        progressDlg.setMessage("Please wait");
        
        // load images
        aImagesState = new Drawable[6];
        aImagesState[IMAGE_NONE] = null;
        aImagesState[IMAGE_OK] = MainActivity.this.getResources().getDrawable(R.drawable.ok);
        aImagesState[IMAGE_WARNING] = MainActivity.this.getResources().getDrawable(R.drawable.warning);
        aImagesState[IMAGE_ERROR] = MainActivity.this.getResources().getDrawable(R.drawable.error);
        aImagesState[IMAGE_CERTIFIED] = MainActivity.this.getResources().getDrawable(R.drawable.logo_authentic);
        aImagesState[IMAGE_INVALID] = MainActivity.this.getResources().getDrawable(R.drawable.logo_authfailed);
		
		// find default adapter 
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter == null)
			Toast.makeText(this, "No NFC adapter available!", Toast.LENGTH_LONG).show();
		else
			if (nfcAdapter.isEnabled() == false)
				Toast.makeText(this, "NFC adapter not enabled!", Toast.LENGTH_LONG).show();
		
		// construct PendingIntent Object which handles NFC events
		pendingIntent = PendingIntent.getActivity(this, 0,
		            new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				
		filterNdef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		filterTag =  new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		filterTech =  new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		aFilters = new IntentFilter[] {filterNdef, filterTag, filterTech,};
		aTechList = new String[][] 	{ 
				new String[] { NfcA.class.getName() },
				new String[] { NfcB.class.getName() },
				new String[] { NfcF.class.getName() },
				new String[] { NfcV.class.getName() },				
									};          
        
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
        	getFragmentManager().beginTransaction()
            	.add(R.id.container, new WelcomeFragment())
            	.commit();
        }
        settingsValid = LoadSettings();
    }
    
	@Override
	protected void onPause() {
	    super.onPause();
	    nfcAdapter.disableForegroundDispatch(this);
	}	
	
	@Override
	public void onResume() {
	    super.onResume();
	    nfcAdapter.enableForegroundDispatch(this, pendingIntent, aFilters, aTechList);
	}
	
	public void onNewIntent(Intent intent) {
		String sTagProtocol;
		String[] sTech;
		int i;
		boolean bTech;
		boolean bIso14443A;
		
		sTagProtocol = "";
		bIso14443A = false;
		
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
	        NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
	    	NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
			// Retrieve Tag ID
	    	uid = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
	    	// Retrieve Tag technology
	    	Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    	if (tag != null) {
	    		sTech = tag.getTechList();
	    		i = 0;
	    		bTech = false;
	    		while (i<sTech.length && ! bTech)
	    		{
	    			if (sTech[i].equals("android.nfc.tech.NfcA"))
	    			{
	    				sTagProtocol = "ISO/IEC 14443-A";
	    				bTech = true;
	    				bIso14443A = true;
	    			}
	    			if (sTech[i].equals("android.nfc.tech.NfcB"))
	    			{
	    				sTagProtocol = "ISO/IEC 14443-B";
	    				bTech = true;
	    			}
	    			if (sTech[i].equals("android.nfc.tech.NfcF"))
	    			{
	    				sTagProtocol = "JIS X 6319-4";
	    				bTech = true;
	    			}
	    			if (sTech[i].equals("android.nfc.tech.NfcV"))
	    			{
	    				sTagProtocol = "ISO/IEC 15693";
	    				bTech = true;
	    			}	    			
	    			i++;
	    		}
	    		if (!bTech)
	    			sTagProtocol = "<unknown>";
	    	}
			if (bIso14443A)
			{
				// Construct NTAG object by using the tag
				ntag = NfcNtag.get(tag);
				if (ntag != null)
				{
					progressDlg.setTitle("Verifying tag...");
					new VerifyNtagTask().execute(this);
				}
			}
			else
			{
				iState = FWSTATE_RESULTTAG;
				if (uid != null)
					currentUid = AsciiHexConverter.bytesToHexReverse(uid);
				else
					currentUid = "<no UID>";
				currentNtagVersion = "Tag is no NXP NTAG";
				currentNxpSignatureMsg = "";
				currentProfilesAuthMsg = "";
				currentProductAuthMsg = "";				
				iImageStatus = IMAGE_WARNING;
				iImageNxpSignature = IMAGE_ERROR;
				iImageProfilesAuth = IMAGE_ERROR;
				iImageProductAuth = IMAGE_ERROR;				
				iImageVerification = IMAGE_NONE;
				currentStatusMsg = "Tags with " + sTagProtocol + " protocol are not supported by this app.";
				updateState();			
			}
	    }
	}
	
	public void onClickOK(View view) {
		
		switch (iState) {
			case FWSTATE_WELCOME:
		        if (settingsValid)
		        {
					iState = FWSTATE_SCANTAG;
					updateState();
		        }
		        else
		        {
		        	Toast.makeText(this, "Settings needed.", Toast.LENGTH_LONG).show();
		        	iState = FWSTATE_SETTINGS;
		        	updateState();		        	
		        }
			break;		
			case FWSTATE_SCANTAG:
				// this should never happen (no OK button in ScanTag)
			break;
			case FWSTATE_RESULTTAG:
				iState = FWSTATE_SCANTAG;
				updateState();
			break;
			case FWSTATE_QUERYTAGS:
				QueryTagsFragment qtf = (QueryTagsFragment)getFragmentManager().findFragmentById(R.id.container);
				criteriaValue = qtf.getCriteriaValue();
				try {
					criteriaType = ProfilesQueryTagProperty.parseProfilesQueryTagProperty(qtf.getCriteriaType());
					try {
						queryMaxCount = Integer.parseInt(qtf.getMaxCountValue());
					}
					catch (NumberFormatException e) {
						queryMaxCount = 0;
					}
					progressDlg.setTitle("Looking up tags...");
					new QueryTagsTask().execute(this);
					
				}
				catch (IllegalArgumentException e) {
					Toast.makeText(this, "Invalid tag property.", Toast.LENGTH_LONG).show();
				}				
				catch (NullPointerException e) {
					Toast.makeText(this, "Undefined tag property.", Toast.LENGTH_LONG).show();
				}
			break;
			case FWSTATE_FOUNDTAGS:
				iState = FWSTATE_SCANTAG;
				updateState();
			break;				
			case FWSTATE_SETTINGS:
				SettingsFragment sf = (SettingsFragment)getFragmentManager().findFragmentById(R.id.container);
				server = sf.getServer();
				user = sf.getUser();
				password = sf.getPassword();
				SaveSettings();
				Toast.makeText(this, "Settings saved.", Toast.LENGTH_LONG).show();
				iState = FWSTATE_SCANTAG;
				updateState();
			break;
		}
	}
	
	public void updateState() {
		switch (iState) {
			case FWSTATE_WELCOME:
		    	getFragmentManager().beginTransaction()
				.replace(R.id.container, new WelcomeFragment())
				.commit();				
			case FWSTATE_SCANTAG:
		    	getFragmentManager().beginTransaction()
				.replace(R.id.container, new ScanTagFragment())
				.commit();					
			break;
			case FWSTATE_RESULTTAG:
				ResultTagFragment rtf = new ResultTagFragment();
				rtf.setUid(currentUid);
				rtf.setNtagVersion(currentNtagVersion);
				rtf.setNxpSignatureMsg(getString(R.string.txtNxpSignature) + " " + currentNxpSignatureMsg);
				rtf.setProfilesAuthMsg(getString(R.string.txtProfilesAuth) + " " + currentProfilesAuthMsg);
				rtf.setProductAuthMsg(getString(R.string.txtProductAuth) + " " + currentProductAuthMsg);
				rtf.setStatusMsg(currentStatusMsg);
				rtf.setNxpSignatureImage(aImagesState[iImageNxpSignature]);
				rtf.setProfilesAuthImage(aImagesState[iImageProfilesAuth]);
				rtf.setProductAuthImage(aImagesState[iImageProductAuth]);
				rtf.setStateImage(aImagesState[iImageStatus]);				
				rtf.setVerificationImage(aImagesState[iImageVerification]);
		    	getFragmentManager().beginTransaction()
				.replace(R.id.container, rtf)
				.commit();
			break;
			case FWSTATE_QUERYTAGS:
				QueryTagsFragment qtf = new QueryTagsFragment();
				qtf.setCriteriaValue(criteriaValue);
		    	getFragmentManager().beginTransaction()
				.replace(R.id.container, qtf)
				.commit();					
			break;
			case FWSTATE_FOUNDTAGS:
				FoundTagsFragment ftf = new FoundTagsFragment();
				Drawable[] icons = new Drawable[foundTagIds.length];
				for (int i = 0; i < foundTagIds.length; i++) {
					icons[i] = aImagesState[IMAGE_OK];
				}
				ftf.setStatusImages(icons);
				ftf.setUids(foundTagIds);
		    	getFragmentManager().beginTransaction()
				.replace(R.id.container, ftf)
				.commit();					
			break;			
			case FWSTATE_SETTINGS:
				SettingsFragment sf = new SettingsFragment();
				sf.setServer(server);
				sf.setUser(user);
				sf.setPassword(password);
		    	getFragmentManager().beginTransaction()
				.replace(R.id.container, sf)
				.commit();
			break;
		}
	}
	
	public MainActivity getActivity() {
		return this;
	}
	
	@Override
	public void onBackPressed() {
	if ((iState == FWSTATE_SETTINGS) || 
		(iState == FWSTATE_QUERYTAGS) ||
		(iState == FWSTATE_FOUNDTAGS))
	{
		iState = FWSTATE_SCANTAG;
		updateState();
	}
	else
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	  	builder.setTitle(R.string.app_name)
	  	   .setIcon(R.drawable.ic_launcher)
	  	   .setMessage(R.string.app_onClose)
	       .setPositiveButton(R.string.txtYes, new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int id) {
	            	 MainActivity mainact = getActivity();
	            	 mainact.finish();
	             }
	         })
	        .setNegativeButton(R.string.txtNo, null);
	  	builder.show();
		}
	}
	
	// settings management
	private boolean LoadSettings()
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		server = settings.getString("network.server", "");
		user = settings.getString("network.user", "");
		password = settings.getString("network.password", "");
		Log.d(TAG, "User: " + user);
		Log.d(TAG, "Server: " + server);
		Log.d(TAG, "Password: " + password);

		return ((server.length() > 0) && (user.length() > 0) && (password.length() > 0));
	}
	
	private void SaveSettings()
	{
		SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
		Log.d(TAG, "User: " + user);
		Log.d(TAG, "Server: " + server);
		Log.d(TAG, "Password: " + password);
		editor.putString("network.server", server);
		editor.putString("network.user", user);
		editor.putString("network.password", password);
		editor.commit();
	}

	// action bar menu stuff
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		 // Inflate the menu items for use in the action bar
		 MenuInflater inflater = getMenuInflater();
		 inflater.inflate(R.menu.main_activity_actions, menu);
		 return super.onCreateOptionsMenu(menu);
	}
	
	 @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	      case R.id.searchTags:
	    	  iState = FWSTATE_QUERYTAGS;
	    	  updateState();
	    	  return true;
	      case R.id.cfgSettings:
	    	  iState = FWSTATE_SETTINGS;
	    	  updateState();
	    	  return true;
	      case R.id.cfgApidoc:
	    	  new IntentLauncher(this).launchUrl(getString(R.string.urlApidoc));
	    	  return true;
	      case R.id.cfgFeedback:
	    	  new IntentLauncher(this).launchUrl(getString(R.string.urlFeedback));
	    	  return true;	    	  
	      case R.id.cfgPartner:
	    	  new IntentLauncher(this).launchUrl(getString(R.string.urlPartner));
	    	  return true;
	      case R.id.cfgAccount:
	    	  new IntentLauncher(this).launchEmail(
	    			  getString(R.string.accEmailAddr),
	    			  getString(R.string.accEmailSubj),
	    			  getString(R.string.accEmailBody)
	    			  );
	    	  return true;
	      case R.id.cfgAbout:
	    	  //Toast.makeText(this, "About... is not implemented yet.", Toast.LENGTH_LONG).show();
	    	  AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	  builder.setTitle(R.string.app_name)
	    	   .setIcon(R.drawable.ic_launcher)
	    	   .setMessage(R.string.app_about)
	           .setCancelable(false)
	           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   //Do something here
	               }
	           });
	    	  builder.show();
	    	  
	        return true;	        
	      default:
	        return super.onOptionsItemSelected(item);
	    } 
	}
	
	private class VerifyNtagTask extends AsyncTask<MainActivity, Integer, String[]>
	{
		static final int MSG_NTAGVERSION = 0;
		static final int MSG_NXPSIGNATURE = 1;
		static final int MSG_PROFILESAUTH = 2;
		static final int MSG_PRODUCTAUTH = 3;
		static final int MSG_STATUS = 4;
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			progressDlg.show();
		}

		@Override
		protected String[] doInBackground(MainActivity... params)
		{
			/* OTP Authentication
			 	1   NFC:    Read UID
				2   NFC:    Read Signature
				3   REST:   Validate Signature on NFC tag
				4   REST:   Get Tag Key
				5   NFC:    Auth NTAG
				6   NFC:    Read HMAC
				7   REST:   Request OTP
				8   (local) Calculate OTP
				9   REST:   Validate OTP
			 */
			
			boolean bOk = false;
			byte[] version = null;
			byte[] signature = null;
			byte[] hmacKey = null;
			byte[] hmacData = null;
			byte[] pack;
			
			String[] sResult = new String[5];
			
			sResult[MSG_NTAGVERSION] = "Tag is no NXP NTAG";
			sResult[MSG_NXPSIGNATURE] = "";
			sResult[MSG_PROFILESAUTH] = "";
			sResult[MSG_PRODUCTAUTH] = "";
			sResult[MSG_STATUS] = "";
			
			MainActivity activity = params[0];
			activity.iImageStatus = IMAGE_NONE;
			activity.iImageNxpSignature = IMAGE_ERROR;
			activity.iImageProfilesAuth = IMAGE_ERROR;
			activity.iImageProductAuth = IMAGE_ERROR;			
			activity.iImageVerification = IMAGE_NONE;
			NfcNtag ntag = activity.ntag;
			
			try
			{
				ProfilesAuthOtpState otpState;
				ProfilesRestResult result;
				ProfilesRestClient client= new ProfilesRestClient(
						activity.server,
						activity.user,
						activity.password
					);
				
				ntag.connect();
				
				/* Read NTAG Version */
				version = ntag.getVersion();
				if (version != null)
				{
					if (DEBUG)
					{
						Log.d(TAG, "NTAG: VERSION = " + AsciiHexConverter.bytesToHex(version));
					}						
					if (version.length == 8)
					{
						sResult[MSG_NTAGVERSION] = NfcNtagVersion.fromGetVersion(version).toString();
						bOk = true;
					}
					else
					{
						activity.iImageStatus = IMAGE_WARNING;
						sResult[MSG_STATUS] ="NTAG GetVersion: incorrect length.";
					}
				}
				else
				{
					activity.iImageStatus = IMAGE_WARNING;
					sResult[MSG_STATUS] ="NTAG GetVersion: read failed.";
				}
				//*/
				
				/* Read NTAG Signature */
				if (bOk)
				{
					bOk = false;
					signature = ntag.readSig();
					if (signature != null)
					{
						if (DEBUG)
						{
							Log.d(TAG, "NTAG: SIGNATURE = " + AsciiHexConverter.bytesToHex(signature));
						}						
						if (signature.length == 32)
						{
							bOk = true;
						}
					}
				}
				//*/
				
				/* Validate Signature in Profiles */
				if (bOk)
				{
					bOk = false;
					result = client.verifyNxpTag(uid, version, signature);
					switch (result.iCode)
					{
						case 0:
							activity.iImageNxpSignature = IMAGE_OK;
							sResult[MSG_NXPSIGNATURE] = "Valid";
							bOk = true;
						break;
						case 1:
							activity.iImageNxpSignature = IMAGE_ERROR;
							activity.iImageVerification = IMAGE_INVALID;
							sResult[MSG_NXPSIGNATURE] = "Invalid";
						break;
						default:
							activity.iImageStatus = IMAGE_ERROR;
							sResult[MSG_STATUS] = "Network error - check connection settings:\n"					
												+ result.sMessage;							
					}
				}
				//*/
				
				/* Get Tag key from Profiles */
				if (bOk)
				{
					bOk = false;
					result = client.getSingleTagKey(uid, "hmac");
					switch (result.iCode)
					{
						case 0:
							hmacKey = AsciiHexConverter.hexToBytes(result.sMessage);
							sResult[MSG_PROFILESAUTH] = "Valid";
							activity.iImageProfilesAuth = IMAGE_OK;
							bOk = true;
						break;
						case 1:
							activity.iImageProfilesAuth = IMAGE_ERROR;
							activity.iImageVerification = IMAGE_INVALID;
							sResult[MSG_PROFILESAUTH] = "Invalid";
						break;
						default:
							activity.iImageStatus = IMAGE_ERROR;
							sResult[MSG_STATUS] = "Network error - check connection settings:\n"					
												+ result.sMessage;							
					}					
				}
				//*/
				
				/* Log in in NTAG read HMAC from protected memory */
				if (bOk)
				{
					bOk = false;
					pack = ntag.pwdAuth(hmacKey);
					if (pack != null)
					{
						if (pack.length == 2)
						{
							if (DEBUG)
							{
								Log.d(TAG, "NTAG: PACK = " + AsciiHexConverter.bytesToHex(pack));
							}
							bOk = true;
						}
						else
						{
							activity.iImageProductAuth = IMAGE_ERROR;
							activity.iImageVerification = IMAGE_INVALID;
							sResult[MSG_PRODUCTAUTH] = "Invalid";
						}
					}
					else
					{
						activity.iImageProductAuth = IMAGE_ERROR;
						activity.iImageVerification = IMAGE_INVALID;
						sResult[MSG_PRODUCTAUTH] = "Invalid";
					}							
				}
				//*/
				
				/* Read HMAC from protected memory */
				if (bOk)
				{
					bOk = false;				
					hmacData = ntag.fastRead(0x16, 0x27);
					if (hmacData != null)
					{
						bOk = (hmacData.length == 72);
						if (bOk)
						{
							if (DEBUG)
							{
								Log.d(TAG, "NTAG: HMAC = " + AsciiHexConverter.bytesToHex(hmacData));
							}
							bOk = true;
						}
						else
						{
							activity.iImageStatus = IMAGE_WARNING;
							sResult[MSG_STATUS] ="NTAG FastRead: incorrect length.";
						}
					}
					else
					{
						activity.iImageStatus = IMAGE_WARNING;
						sResult[MSG_STATUS] ="NTAG FastRead: read failed.";
					}
				}

				/* Run OTP authentication */
				if (bOk)
				{
					bOk = false;
					otpState = client.requestOtpAuthentication(uid, "hmac");
					if (otpState.iCode == 0)
					{
						otpState = client.calculateOtpAuthResult(otpState, hmacData);
						result = client.validateOtpAuthentication(otpState);
						switch (result.iCode)
						{
							case 0:
								bOk = true;
							break;
							case 1:
								activity.iImageVerification = IMAGE_INVALID;
								sResult[MSG_PRODUCTAUTH] = "Invalid";
							break;
							default:
								activity.iImageStatus = IMAGE_ERROR;
								sResult[MSG_STATUS] = "Network error - check connection settings:\n"					
													+ result.sMessage;	
						}	
					}
					else
					{
						if (otpState.iCode > 0)
						{
								activity.iImageVerification = IMAGE_INVALID;
								activity.iImageStatus = IMAGE_WARNING;
								sResult[MSG_PRODUCTAUTH] = "Invalid";
								sResult[MSG_STATUS] = otpState.sMessage;
						}
						else
						{							
							activity.iImageStatus = IMAGE_ERROR;
							sResult[MSG_STATUS] = "Network error - check connection settings:\n"					
												+ otpState.sMessage;	
						}				
					}
				}
				//*/
				ntag.close();
				
				if (bOk)
				{
					activity.iImageProductAuth = IMAGE_OK;					
					activity.iImageVerification = IMAGE_CERTIFIED;
					sResult[MSG_PRODUCTAUTH] = "Valid";
				}
			}
			catch (IOException e)
			{
				activity.iImageStatus = IMAGE_ERROR;
				sResult[MSG_STATUS] = "IO Exception caught\n:" + e.getMessage();
			}
			catch (Exception e)
			{
				activity.iImageStatus = IMAGE_ERROR;
				sResult[MSG_STATUS] = "Exception caught\n: " + e.getMessage();
			}			
			return sResult;
		}
		
		@Override
		protected void onPostExecute(String[] response)
		{
			progressDlg.dismiss();
			iState = FWSTATE_RESULTTAG;
			currentUid = AsciiHexConverter.bytesToHex(uid);
			currentNtagVersion = response[MSG_NTAGVERSION];
			currentNxpSignatureMsg = response[MSG_NXPSIGNATURE];
			currentProfilesAuthMsg = response[MSG_PROFILESAUTH];
			currentProductAuthMsg = response[MSG_PRODUCTAUTH];
			currentStatusMsg = response[MSG_STATUS];
			updateState();			
		}
	}
	
	public void onClickTestConnection(View view) {
		progressDlg.setTitle("Testing Profiles connection...");		
		new TestConnectionTask().execute(this);
	}
	
	public void displayTestConnectionResults(String[] sResult) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(sResult[0])
			.setIcon(R.drawable.ic_launcher)
			.setMessage(sResult[1])
			.setCancelable(false)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		builder.show();	
	}
	
	private class QueryTagsTask extends AsyncTask<MainActivity, Integer, String[]>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			progressDlg.show();
		}

		@Override
		protected String[] doInBackground(MainActivity... params)
		{
			String[] tagIds;
			String[] sResult;
			MainActivity activity = params[0];
			
			try {
				HashMap<ProfilesQueryTagProperty, Object> queryMap = new HashMap<ProfilesQueryTagProperty, Object>();
				queryMap.put(activity.criteriaType, activity.criteriaValue);
				if (activity.queryMaxCount > 0) {
					queryMap.put(ProfilesQueryTagProperty.count, activity.queryMaxCount);
				}
				ProfilesRestClient client= new ProfilesRestClient(
						activity.server,
						activity.user,
						activity.password
					);
				
				tagIds = client.getTagsByProperties(queryMap);
				if (tagIds.length > 0)
				{
					sResult = new String[tagIds.length + 1];
					sResult[0] = String.format("%d", tagIds.length);
					for (int i = 0; i < tagIds.length; i++)
					{
						sResult[i + 1] = tagIds[i];
					}
				}
				else
				{
					sResult = new String[2];
					sResult[0] = "0";
					sResult[1] = "No matching tags found";
				}
			}
			catch (Exception e) {
				sResult = new String[2];
				sResult[0] = "-1";
				sResult[1] = "Error: " + e.getMessage();
			}			
			return sResult;			
		}
		
		@Override
		protected void onPostExecute(String[] response)
		{
			progressDlg.dismiss();
			int numTags = Integer.parseInt(response[0]);
			switch (numTags)
			{
				case -1:
				case 0:
					displayTestConnectionResults(response);
					break;
				default:
					foundTagIds = new String[numTags];
					for (int i = 0; i < numTags; i++) {
						foundTagIds[i] = response[i+1];
					}
					iState = FWSTATE_FOUNDTAGS;
					updateState();					
			}

		}
	}	
	
	private class TestConnectionTask extends AsyncTask<MainActivity, Integer, String[]>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			progressDlg.show();
		}

		@Override
		protected String[] doInBackground(MainActivity... params)
		{
			String sResult[] = new String[2];
			
			try {
				SettingsFragment sf = (SettingsFragment)getFragmentManager().findFragmentById(R.id.container);
				ProfilesRestClient client= new ProfilesRestClient(sf.getServer(), sf.getUser(), sf.getPassword());
				ProfilesRestResult result = client.getVerificationMessage("RR",  0);
				if ((result.httpStatus == 200) && (result.iCode == 0))
				{
					sResult[0] = "Test Connection OK";
				}
				else
				{
					sResult[0] = "Test Connection FAILED";
				}
				StringBuilder sb = new StringBuilder();
				sb.append("HTTP " + result.httpStatus);
				sb.append("\nReturn code: " + result.iCode);
				sb.append("\nMessage: " + result.sMessage);
				sResult[1] = sb.toString();
			}
			catch (Exception e) {
				sResult[1] = "Error: " + e.getMessage();
			}			
			return sResult;			
		}
		
		@Override
		protected void onPostExecute(String[] response)
		{
			progressDlg.dismiss();
			displayTestConnectionResults(response);
		}
	}
}
