/**
 * 
 */
package com.twopiradian.parking.task;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.twopiradian.parking.listener.TaskCompletionListener;

/**
 * @author Prodipta Golder
 *
 */
public class ReadCalendarTask extends AsyncTask<String, Void, Boolean> {
	private TaskCompletionListener<Boolean> callbackListener;
	private CalendarEventFeed resultFeed;
	
	public ReadCalendarTask(TaskCompletionListener<Boolean> callbackListener) {
		this.callbackListener = callbackListener;
	}
	
	@Override
	protected Boolean doInBackground(String... args) {
		try {
			CalendarService myService = new CalendarService("GoldyTest");
			myService.setUserToken(args[0]);
			String calUrl = "https://www.google.com/calendar/feeds/" 
					+ "2pirad.com_s2bps0vks9t9q6ki07bpgovjfs@group.calendar.google.com" 
					+ "/private/full";
			URL feedUrl = new URL(calUrl);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String currDateStr = sdf.format(new Date());
			
//				Log.i("1111111goldy", "currDateStr: " + currDateStr);
			CalendarQuery myQuery = new CalendarQuery(feedUrl);
			myQuery.setMinimumStartTime(DateTime.parseDateTime(currDateStr + "T00:00:00"));
			myQuery.setMaximumStartTime(DateTime.parseDateTime(currDateStr + "T23:59:59"));

			resultFeed = myService.getFeed(myQuery, CalendarEventFeed.class);
//				Log.i("1111111goldy", "Feed read successfully...");
		} catch (Exception e) {
			Log.i("1111111goldy", e.toString());
			return false;
		}
		return true;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		callbackListener.execute(result, resultFeed);
	}
}
