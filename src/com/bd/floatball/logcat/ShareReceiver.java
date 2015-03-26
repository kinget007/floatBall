package com.bd.floatball.logcat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShareReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d("logcat", "received intent for share");

		com.bd.floatball.logcat.Intent.handleExtras(context, intent);

		Lock.acquire(context);

		Intent svcIntent = new Intent(context, ShareService.class);
		context.startService(svcIntent);
	}

}
