package com.bd.floatball.applist;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bd.floatball.R;
import com.bd.floatball.mode.RunningAppInfo;

//listView
public class BrowseRunningAppAdapter extends BaseAdapter {
	
	private List<RunningAppInfo> mlistAppInfo = null;
	private Activity activityContext;
	@SuppressWarnings("unused")
	private floatApplication myapplication;
	private View lastview;
	LayoutInflater infater = null;
    
	public BrowseRunningAppAdapter(Context context,  List<RunningAppInfo> apps) {
		infater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mlistAppInfo = apps ;
	}
	public BrowseRunningAppAdapter(Context context, int resourceID, int view_id, List<String> objects)
	{
		super();
		activityContext = (Activity)context;
		if(activityContext != null)
			myapplication = (floatApplication)(activityContext).getApplication();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mlistAppInfo.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mlistAppInfo.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public View getView(int position, View convertview, ViewGroup arg2) {
		View view = null;
		ViewHolder holder = null;
		if (convertview == null || convertview.getTag() == null) {
			view = infater.inflate(R.layout.browse_app_item, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} 
		else{
			view = convertview ;
			holder = (ViewHolder) convertview.getTag() ;
		}
		RunningAppInfo appInfo = (RunningAppInfo) getItem(position);
		holder.appIcon.setImageDrawable(appInfo.getAppIcon());
		holder.tvAppLabel.setText(appInfo.getAppLabel());
		holder.tvProcessId.setText(appInfo.getPid()+"") ;
			
        if(appInfo.getPkgName().length() > 24 ){
        	holder.tvPkgName.setText(appInfo.getPkgName().subSequence(0, 24)+"...");
        }
        else{
        	holder.tvPkgName.setText(appInfo.getPkgName());
        }
        if(appInfo.getProcessName().length() > 24){
        	holder.tvProcessName.setText(appInfo.getProcessName().subSequence(0, 24)+"...") ;
        }
        else{
        	holder.tvProcessName.setText(appInfo.getProcessName()) ;
        }
		return view;
	}

	
	public void setBackgroundColorByName(String name , int color)
	{
	}
	
	public void setUpdateIconFlag(boolean iscomplete){
	}
	
	public View getBackgoundColorView(){		
		return lastview;
	}
	
	class ViewHolder {
		ImageView appIcon;
		TextView tvAppLabel;
		TextView tvPkgName;
        TextView tvProcessId ;
		TextView tvProcessName ;
        
        
		public ViewHolder(View view) {
			this.appIcon = (ImageView) view.findViewById(R.id.imgApp);
			this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
			this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
			this.tvProcessId = (TextView) view.findViewById(R.id.tvProcessId);
			this.tvProcessName = (TextView) view.findViewById(R.id.tvProcessName);
		}
	}
}