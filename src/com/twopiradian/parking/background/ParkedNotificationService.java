/**
 * 
 */
package com.twopiradian.parking.background;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEventFeed;

/**
 * @author Prodipta Golder
 *
 */
public class ParkedNotificationService extends Service {

	private static final long POLL_INTERVAL = 60000;

	private Timer mTimer;
	
	private TimerTask mTimerTask;
	
	private String authToken;
	  
	@Override
	public void onCreate() {
		if (this.mTimer != null) {
			this.mTimer.cancel();
		}
		this.mTimer = new Timer();
//		AccountManager accMgr = AccountManager.get(getApplicationContext());
//		mTimerTask = new ParkedServiceTimeTask(accMgr);
		mTimerTask = new ParkedServiceTimeTask();
		Log.i("11111111111NotificationService", "on create..................");
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		authToken = intent.getStringExtra("CalAuthToken");
		Log.i("11111111111NotificationService", "on start..................");
		this.mTimer.schedule(mTimerTask, 120000, POLL_INTERVAL);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class ParkedServiceTimeTask extends TimerTask {
//		private AccountManager accMgr;
		
//		public ParkedServiceTimeTask(AccountManager accMgr) {
//			this.accMgr = accMgr;
//		}
		
		@Override
		public void run() {
//			if (authToken == null) {
//				String ns = Context.NOTIFICATION_SERVICE;
//				NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
//				Context context = getApplicationContext();
//
//			    CharSequence contentTitle = "Notification";
//
//			    CharSequence contentText = "New Notification";
//
//			    final Notification notifyDetails =
//			        new Notification(R.drawable.ic_launcher, "amar tairi notification", System.currentTimeMillis());
//
//			    Intent notifyIntent = new Intent(context, AccountSettingActivity.class);
//
//			    PendingIntent intent = PendingIntent.getActivity(context, 0, 
//			    		notifyIntent, 0);
////			          notifyIntent,  PendingIntent.FLAG_UPDATE_CURRENT | Notification.FLAG_NO_CLEAR);
//
//			    notifyDetails.setLatestEventInfo(context, contentTitle, contentText, intent);
//
//			    mNotificationManager.notify(1, notifyDetails);
			    
//			    mNotificationManager.cancel(1);
			    
//			    ArrayList<Account> accounts = notifyIntent.getParcelableArrayListExtra("SelectedAccount");
//			    Account account = accounts.get(0);
//			    
//			    Log.i("1111111111ParkedNotificationService", "111111111111111111111111111111");
//			    Log.i("1111111111ParkedNotificationService", account.name);
//			    
//			    LoginTaskCompletionListener listener = new LoginTaskCompletionListener();
//				try {
//					AuthTokenTask authTask = new AuthTokenTask(accMgr, listener);
//					authTask.execute(new Account[] { account });
//				} catch (Exception e) {
//					// do nothing
//				}
//			} else {
				try {
					CalendarService myService = new CalendarService("GoldyTest");
					myService.setUserToken(authToken);
					String calUrl = "https://www.google.com/calendar/feeds/" 
							+ "2pirad.com_s2bps0vks9t9q6ki07bpgovjfs@group.calendar.google.com" 
							+ "/private/full";
					URL feedUrl = new URL(calUrl);
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String currDateStr = sdf.format(new Date());
					
						Log.i("1111111goldy", "currDateStr: " + currDateStr);
					CalendarQuery myQuery = new CalendarQuery(feedUrl);
					myQuery.setMinimumStartTime(DateTime.parseDateTime(currDateStr + "T00:00:00"));
					myQuery.setMaximumStartTime(DateTime.parseDateTime(currDateStr + "T23:59:59"));
	
					myService.getFeed(myQuery, CalendarEventFeed.class);
						Log.i("1111111goldy", "Feed read successfully...");
				} catch (Exception e) {
					Log.i("1111111goldy", e.toString());
				}
//			}
		}
	}

//    private class LoginTaskCompletionListener implements TaskCompletionListener<Boolean> {
//    	public void execute(Boolean loginSuccess, Object token) {
//    		if (loginSuccess && (token != null)) {
//    			authToken = (String) token;
//    		}
//    	}
//    }
}
