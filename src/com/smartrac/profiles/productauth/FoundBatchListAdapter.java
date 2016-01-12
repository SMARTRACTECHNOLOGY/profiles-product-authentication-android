package com.smartrac.profiles.productauth;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class FoundBatchListAdapter extends ArrayAdapter<String> {
 
	private final Activity context;
	private final Drawable[] imageStatus;
	private final String[] sBatch;
	
	public FoundBatchListAdapter(Activity context, Drawable[] imageStatus, String[] sBatch) {
		super(context, R.layout.fragment_taglistitem, sBatch);
		// TODO Auto-generated constructor stub
		
		this.context=context;
		this.imageStatus = imageStatus;
		this.sBatch=sBatch;
	}
	
	public View getView(int position,View view,ViewGroup parent) {
		LayoutInflater inflater=context.getLayoutInflater();
		View rowView=inflater.inflate(R.layout.fragment_batchlistitem, null,true);
		
		TextView txtBatch = (TextView) rowView.findViewById(R.id.textViewBatch);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.imageViewStatus);

		imageView.setImageDrawable(imageStatus[position]);
		txtBatch.setText("Batch: " + sBatch[position]);
		return rowView;
		
	};
}