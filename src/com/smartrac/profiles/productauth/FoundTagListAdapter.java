package com.smartrac.profiles.productauth;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class FoundTagListAdapter extends ArrayAdapter<String> {
 
	private final Activity context;
	private final Drawable[] imageStatus;
	private final String[] sUid;
	
	public FoundTagListAdapter(Activity context, Drawable[] imageStatus, String[] sUid) {
		super(context, R.layout.fragment_taglistitem, sUid);
		// TODO Auto-generated constructor stub
		
		this.context=context;
		this.imageStatus = imageStatus;
		this.sUid=sUid;
	}
	
	public View getView(int position,View view,ViewGroup parent) {
		LayoutInflater inflater=context.getLayoutInflater();
		View rowView=inflater.inflate(R.layout.fragment_taglistitem, null,true);
		
		TextView txtUid = (TextView) rowView.findViewById(R.id.textViewUid);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.imageViewStatus);

		imageView.setImageDrawable(imageStatus[position]);
		txtUid.setText("UID: " + sUid[position]);
		return rowView;
		
	};
}