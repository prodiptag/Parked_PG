/**
 * 
 */
package com.twopiradian.parking.task;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.twopiradian.parking.listener.TaskCompletionListener;

/**
 * @author Prodipta Golder
 *
 */
public class AddUpdateCalendarEventTask extends AsyncTask<String, Void, Boolean> {
	private TaskCompletionListener<Boolean> callbackListener;
	private CalendarEventEntry insertedEntry;
	private String email;
	private boolean isForBooking;
	
	public AddUpdateCalendarEventTask(TaskCompletionListener<Boolean> callbackListener, 
									  CalendarEventEntry entry,
									  String email, boolean isForBooking) {
		this.callbackListener = callbackListener;
		this.insertedEntry = entry;
		this.email = email;
		this.isForBooking = isForBooking;
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
//			URL editUrl = null;
			
			boolean isAdd = false;
//			CalendarEventEntry myEntry = new CalendarEventEntry();
//			Log.i("1111111goldy", "11111111111111");			
//			myEntry.setTitle(new PlainTextConstruct("Slot-" + args[1]));
//			Log.i("1111111goldy", "222222222222222222");			
			if (insertedEntry == null) {
				isAdd = true;
				insertedEntry = new CalendarEventEntry();
				insertedEntry.setTitle(new PlainTextConstruct("Slot-" + args[1]));
			} else {
				isAdd = false;
//				myEntry = insertedEntry;
//				editUrl = new URL(calUrl + "/" + insertedEntry.getId());
			}
			if (isForBooking) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String currDateStr = sdf.format(new Date());

//				List<When> whens = insertedEntry.getTimes();
//				for (When when : whens) {
//					myEntry.addTime(when);
//				}
				
				When eventTimes = new When();
				eventTimes.setStartTime(DateTime.now());
				eventTimes.setEndTime(DateTime.parseDateTime(currDateStr + "T23:59:59"));
				insertedEntry.addTime(eventTimes);
				insertedEntry.setContent(new PlainTextConstruct("Booked parking slot-" + args[1] + " by " + email));
			} else {
//				List<When> whens = insertedEntry.getTimes();
				List<When> whens = insertedEntry.getTimes();
				When when = whens.get(whens.size() - 1);
				when.setEndTime(DateTime.now());

//				When eventTimes = new When();
//				eventTimes.setStartTime(when.getStartTime());
//				eventTimes.setEndTime(DateTime.now());
//				myEntry.addTime(eventTimes);
				insertedEntry.setContent(new PlainTextConstruct("Booked and Released parking slot-" + args[1] + " by " + email));
			}

			if (isAdd) {
//				insertedEntry.delete();
				insertedEntry = myService.insert(feedUrl, insertedEntry);
				Log.i("1111111goldy", "insert call...");
			} else {
				insertedEntry = insertedEntry.update();
				Log.i("1111111goldy", "update call...");
			}

//			Log.i("1111111goldy", "cal entry added successfully...");
		} catch (Exception e) {
			Log.i("1111111goldy", e.toString());
			return false;
		}
		return true;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		callbackListener.execute(result, insertedEntry);
	}
}
