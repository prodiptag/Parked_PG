/**
 * 
 */
package com.twopiradian.parking.background;

import com.twopiradian.parking.AccountSettingActivity;
import com.twopiradian.parking.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Prodipta Golder
 * 
 */
public class ParkedNotificationReceiver extends BroadcastReceiver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(final Context context, final Intent bootintent) {
		Log.i("11111111ParkedNotificationReceiver", Intent.ACTION_BOOT_COMPLETED);
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

	    CharSequence contentTitle = "Notification";

	    CharSequence contentText = "parked receiver notification";

	    final Notification notifyDetails =
	        new Notification(R.drawable.ic_launcher, "amar tairi notification", System.currentTimeMillis());

	    Intent notifyIntent = new Intent(context, AccountSettingActivity.class);

	    PendingIntent intent = PendingIntent.getActivity(context, 0, 
//	    		notifyIntent, 0);
	          notifyIntent,  PendingIntent.FLAG_UPDATE_CURRENT | Notification.FLAG_AUTO_CANCEL);

	    notifyDetails.setLatestEventInfo(context, contentTitle, contentText, intent);

	    mNotificationManager.notify(1, notifyDetails);
//		if (Intent.ACTION_BOOT_COMPLETED.equals(bootintent.getAction())) {
//			Intent serviceIntent = new Intent();
//			serviceIntent.setClass(context, ParkedNotificationService.class);
//			context.sendBroadcast(serviceIntent);
//		}
	}
}
