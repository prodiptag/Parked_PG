package com.twopiradian.parking;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gdata.data.DateTime;
import com.google.gdata.data.Person;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.extensions.When;
import com.twopiradian.parking.listener.TaskCompletionListener;
import com.twopiradian.parking.task.AddUpdateCalendarEventTask;
import com.twopiradian.parking.task.AuthTokenTask;
import com.twopiradian.parking.task.ReadCalendarTask;

/**
 * 
 * @author Prodipta Golder
 */

public class Parked_PGActivity extends Activity {
	
	public static final String PREFS_NAME = "ParkedPGPrefsFile";
	private LinearLayout linerLayout;
	private LinearLayout buttonLayout;
	private Button exitBtn;
	private TextView testResult;
	private AccountManager accMgr;
	private int currentAccountIndex = -1;
	private Map<String, Account> accountMap = new HashMap<String, Account>();
	private String authToken = "";
	private String authEmail = "";
	private Map<Integer, CalendarEventEntry> eventEntries = new HashMap<Integer, CalendarEventEntry>();
	private List<Button> parkingButtons = new ArrayList<Button>();
	private Geocoder geocoder;
	private Address twopiradAddr;
	private double currLatitude;
	private double currLongitude;
	
	public void showAlert(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setMessage(message);
		alertDialog.setTitle(title);
		alertDialog.setButton("OK", 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}
		);
		alertDialog.show();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		SubMenu smenu = menu.addSubMenu(0, 0, 0, "Choose Account");
		int i = 2;
		for (Account acc : accountMap.values()) {
			MenuItem item = smenu.add(1, i, (i - 2), acc.name);
			if (item.getTitle().equals(authEmail)) {
				currentAccountIndex = i - 2;
			}
			i++;
		}
		smenu.setGroupCheckable(1, true, true);
		if (currentAccountIndex > -1) {
			MenuItem item = smenu.getItem(currentAccountIndex);
			item.setChecked(true);
		}
		menu.add(0, 1, 1, "Notification Test");
		menu.add(0, 2, 2, "Exit");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if ("Exit".equals(item.getTitle())) {
			finish();
	    } else if ("Notification Test".equals(item.getTitle())) {
	    	String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
			Context context = getApplicationContext();

		    CharSequence contentTitle = "Notification";

		    CharSequence contentText = "Test korar jonno";

		    final Notification notifyDetails =
		        new Notification(R.drawable.ic_launcher, "amar tairi notification", System.currentTimeMillis());

		    Intent notifyIntent = new Intent(context, AccountSettingActivity.class);

		    PendingIntent intent = PendingIntent.getActivity(context, 0, 
//		    		notifyIntent, 0);
		          notifyIntent,  PendingIntent.FLAG_UPDATE_CURRENT | Notification.FLAG_AUTO_CANCEL);

		    notifyDetails.setLatestEventInfo(context, contentTitle, contentText, intent);

//		    Log.i("1111111111ParkedNotificationService", "before notification");

		    mNotificationManager.notify(1, notifyDetails);
		    finish();
	    } else {
	    	currentAccountIndex = item.getOrder();
	    	Account account = accountMap.get(item.getTitle());
	    	if (account != null) {
		    	authEmail = account.name;
		    	
		    	FileOutputStream fos = null;
				try {
					fos = openFileOutput(PREFS_NAME, Context.MODE_PRIVATE);
			    	fos.write(account.name.getBytes());
				} catch (FileNotFoundException e) {
					Log.e("Parked_PGActivity", e.getMessage());
				} catch (IOException e) {
					Log.e("Parked_PGActivity", e.getMessage());
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							Log.e("Parked_PGActivity", e.getMessage());
						}
					}
				}
//		    	
//				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//				SharedPreferences.Editor editor = settings.edit();
//				editor.putString("SelectedEmail", account.name);
//				editor.commit();

	    		LoginTaskCompletionListener listener = new LoginTaskCompletionListener();
				try {
					AuthTokenTask authTask = new AuthTokenTask(accMgr, listener, this);
					authTask.execute(new Account[] { account });
				} catch (Exception e) {
					listener.execute(false, e.toString());
				}
	    	}
	    }
		return super.onOptionsItemSelected(item);
	}
	
	private void updateScreenAfterLogin() {
		testResult.setText("Click parking slot to book...");
		new MyThread().start();
		for (Button parkingButton : parkingButtons) {
			if (parkingButton.getParent() == null) {
				buttonLayout.addView(parkingButton, parkingButton.getId());
			}
		}
	}
	
    private void bookSlot(Button b, TaskCompletionListener<Boolean> listener, boolean isForBooking) {
		try {
			AddUpdateCalendarEventTask calTask = new AddUpdateCalendarEventTask(
					listener, eventEntries.get(b.getId()), authEmail, isForBooking);
			calTask.execute(new String[] { authToken, String.valueOf(b.getId() + 1) });
		} catch (Exception e) {
			showAlert("Error...", "Calendar access korte para jachhe na...");
		}
    }
    
    private boolean canBookSlot() {
		float[] results = new float[10];
		Location.distanceBetween(currLatitude, currLongitude, twopiradAddr.getLatitude(), twopiradAddr.getLongitude(), results);
		showAlert("can book slot", twopiradAddr.getLatitude() + ":" + twopiradAddr.getLongitude() + ":" + currLatitude + ":" + currLongitude + ":" + results[0]);
//    	try {
//			List<Address> addrs = geocoder.getFromLocation(currLatitude, currLongitude, 1);
//			Address currAddr = addrs.get(0);
//		} catch (IOException e) {
//			Log.e("Parked_PGActivity", e.getMessage());
//		}
		if (results[0] <= 100.0) {
			return true;
		}
		showAlert("Booking alert", "Can not book slot from more than 100 meter distance");
    	return false;
    }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(this);
        try {
			List<Address> addrs = geocoder.getFromLocationName("TwoPiRadian Infotech Private Limited", 1);
//			List<Address> addrs1 = geocoder.getFromLocationName("SDF Building, Salt Lake City, Kolkata, West Bengal, India", 1);
			twopiradAddr = addrs.get(0);
//			float[] results = new float[10];
//			Address sdf = addrs1.get(0);
//			Location.distanceBetween(twopiradAddr.getLatitude(), twopiradAddr.getLongitude(), sdf.getLatitude(), sdf.getLongitude(), results);
//			Log.i("Parked_PGActivity", "distance between: " + results[0]);
		} catch (IOException e) {
			Log.e("Parked_PGActivity", e.getMessage());
		} catch (Exception e) {
			Log.e("Parked_PGActivity", e.getMessage());
		}
        
		accMgr = AccountManager.get(this);
		Account[] accs = accMgr.getAccountsByType("com.google");
		for (Account acc : accs) {
			accountMap.put(acc.name, acc);
		}
		
		linerLayout = new LinearLayout(this);
		linerLayout.setOrientation(LinearLayout.VERTICAL);
		linerLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		for (int i = 0; i < 3; i++) {
			Button parkingButton = new Button(this);
			parkingButton.setText("Slot-" + (i + 1));
			parkingButton.setId(i);
			parkingButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (canBookSlot()) {
						Button b = (Button) v;
						BookSlotTaskListener listener = new BookSlotTaskListener(b.getId());
						bookSlot(b, listener, true);
	//					try {
	//						AddUpdateCalendarEventTask calTask = new AddUpdateCalendarEventTask(
	//								listener, eventEntries.get(b.getId()), authEmail, true);
	//						calTask.execute(new String[] { authToken, String.valueOf(b.getId() + 1) });
	//					} catch (Exception e) {
	//						showAlert("Error...", "Calendar access korte para jachhe na...");
	//					}
					}
				}
			});
			parkingButton.setEnabled(false);
			parkingButtons.add(parkingButton);
		}

        exitBtn = new Button(this);
        exitBtn.setText("Exit");
        exitBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		testResult = new TextView(this);
		testResult.setText("Click menu...");
		testResult.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		linerLayout.addView(testResult);
		
		buttonLayout = new LinearLayout(this);
		buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
		buttonLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		buttonLayout.addView(exitBtn);
		linerLayout.addView(buttonLayout);

        setContentView(linerLayout);
        StringBuilder sb = new StringBuilder();
        
//        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        FileInputStream fis = null;
		try {
			fis = openFileInput(PREFS_NAME);
			int ch;
			while ((ch = fis.read()) != -1) {
				sb.append((char) ch);
			}
		} catch (FileNotFoundException e) {
			Log.e("Parked_PGActivity", e.getMessage());
		} catch (IOException e) {
			Log.e("Parked_PGActivity", e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					Log.e("Parked_PGActivity", e.getMessage());
				}
			}
		}
//        String email = settings.getString("SelectedEmail", null);
		String email = sb.toString();
        if ((email != null) && !email.trim().equals("")) {
        	Account account = accountMap.get(email);
	    	if (account != null) {
		    	authEmail = account.name;
	    		LoginTaskCompletionListener listener = new LoginTaskCompletionListener();
				try {
					AuthTokenTask authTask = new AuthTokenTask(accMgr, listener, this);
					authTask.execute(new Account[] { account });
				} catch (Exception e) {
					listener.execute(false, e.toString());
				}
	    	}
        }

		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener mlocListener = new MyLocationListener();

		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
//		mlocManager.requestLocationUpdates("Network", 0, 0, mlocListener);
    }

    private class LoginTaskCompletionListener implements TaskCompletionListener<Boolean> {
    	public void execute(Boolean loginSuccess, Object token) {
    		if (loginSuccess && (token != null)) {
    			authToken = (String) token;
    			updateScreenAfterLogin();
    		} else {
    			showAlert("Login failed...", "hoini. abar account select koro...");
    		}
    	}
    }

    private class BookSlotTaskListener implements TaskCompletionListener<Boolean> {
    	private int buttonId;
    	
    	public BookSlotTaskListener(int buttonId) {
    		this.buttonId = buttonId;
		}
    	
    	public void execute(Boolean success, Object entry) {
    		if (success && (entry != null)) {
    			CalendarEventEntry resultEntry = (CalendarEventEntry) entry;
    			eventEntries.put(buttonId, resultEntry);
				showAlert("Booked successfully", resultEntry.getPlainTextContent());
				for (Button b : parkingButtons) {
					if (b.getId() == buttonId) {
						testResult.setText("Click parking slot to release...");
						b.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								Button b = (Button) v;
								ReleaseSlotTaskListener listener = new ReleaseSlotTaskListener(b.getId());
								bookSlot(b, listener, false);
//								try {
//									AddUpdateCalendarEventTask calTask = new AddUpdateCalendarEventTask(
//											listener, eventEntries.get(b.getId()), authEmail, false);
//									calTask.execute(new String[] { authToken, String.valueOf(b.getId() + 1) });
//								} catch (Exception e) {
//									showAlert("Error...", "Calendar access korte para jachhe na...");
//								}
							}
						});
					} else {
						b.setEnabled(false);
					}
				}
    		} else {
    			showAlert("Error...", "Calendar access hochhe na...");
    		}
    	}
    }

	private class ReleaseSlotTaskListener implements TaskCompletionListener<Boolean> {
		private int buttonId;

		public ReleaseSlotTaskListener(int buttonId) {
			this.buttonId = buttonId;
		}

		public void execute(Boolean success, Object entry) {
			if (success && (entry != null)) {
    			CalendarEventEntry resultEntry = (CalendarEventEntry) entry;
    			eventEntries.put(buttonId, resultEntry);
				showAlert("Parking released successfully", resultEntry.getPlainTextContent());
				for (Button b : parkingButtons) {
					if (b.getId() == buttonId) {
						testResult.setText("Click parking slot to book...");
						b.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								if (canBookSlot()) {
									Button b = (Button) v;
									BookSlotTaskListener listener = new BookSlotTaskListener(b.getId());
									bookSlot(b, listener, true);
	//								try {
	//									AddUpdateCalendarEventTask calTask = new AddUpdateCalendarEventTask(
	//											listener, eventEntries.get(b.getId()), authEmail, true);
	//									calTask.execute(new String[] { authToken, String.valueOf(b.getId() + 1) });
	//								} catch (Exception e) {
	//									showAlert("Error...", "Calendar access korte para jachhe na...");
	//								}
								}
							}
						});
					} else {
						b.setEnabled(true);
					}
				}
			} else {
				showAlert("Error...", "Calendar access hochhe na...");
			}
		}
    }

    private class ReadCalendarTaskListener implements TaskCompletionListener<Boolean> {
    	public void execute(Boolean success, Object feed) {
    		if (success) {
				for (Button b : parkingButtons) {
					b.setEnabled(true);
				}
    			if (feed != null) {
        			CalendarEventFeed resultFeed = (CalendarEventFeed) feed;
        			int size = resultFeed.getEntries().size();
        			for (int i = 0; i < size; i++) {
        				CalendarEventEntry entry = resultFeed.getEntries().get(i);
        				String title = entry.getTitle().getPlainText();
        				if (title.startsWith("Slot-")) {
	        				title = title.replaceAll("Slot-", "");
	        				int buttonIndex = Integer.parseInt(title) - 1;
        					eventEntries.put(buttonIndex, entry);
	        				List<When> whens = entry.getTimes();
	        				When when = whens.get(whens.size() - 1);
	        				if (when.getEndTime().getValue() >= DateTime.now().getValue()) {
	        					List<Person> persons = entry.getAuthors();
	        					Person person = persons.get(persons.size() - 1);
	        					Button button = parkingButtons.get(buttonIndex);
	        					if (authEmail.equals(person.getEmail())) {
	        						for (Button b : parkingButtons) {
	        							b.setEnabled(false);
	        						}
	        						button.setEnabled(true);
	        						testResult.setText("Click parking slot to release...");
	        						button.setOnClickListener(new OnClickListener() {
	        							public void onClick(View v) {
	        								Button b = (Button) v;
	        								ReleaseSlotTaskListener listener = new ReleaseSlotTaskListener(b.getId());
	        								bookSlot(b, listener, false);
//	        								try {
//	        									AddUpdateCalendarEventTask calTask = new AddUpdateCalendarEventTask(
//	        											listener, eventEntries.get(b.getId()), authEmail, false);
//	        									calTask.execute(new String[] { authToken, String.valueOf(b.getId() + 1) });
//	        								} catch (Exception e) {
//	        									showAlert("Error...", "Calendar access korte para jachhe na...");
//	        								}
	        							}
	        						});
	        						return;
	        					}
	        					parkingButtons.get(buttonIndex).setEnabled(false);
	        				}
        				}
        			}
    			}
    		} else {
    			showAlert("Error...", "Calendar access hochhe na...");
    		}
    	}
    }
    
	public class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location loc) {
			currLatitude = loc.getLatitude();
			currLongitude = loc.getLongitude();
			String Text = "My current location is: " 
							+ "Latitude = " + loc.getLatitude() 
							+ "Longitude = " + loc.getLongitude(); 
			Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
			
		}
		
		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
		}
		
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

    
    private class MyThread extends Thread {
    	@Override
    	public void run() {
    		while (true) {
	    		ReadCalendarTaskListener listener = new ReadCalendarTaskListener();
	    		try {
	    			ReadCalendarTask calTask = new ReadCalendarTask(listener);
	    			calTask.execute(new String[] { authToken });
	    		} catch (Exception e) {
	    			Log.e("Error...", "Calendar access korte para jachhe na...");
	    		}
	    		
	    		try {
	    			sleep(300000);
	    		} catch (InterruptedException ie) {
	    			// do nothing
	    		}
    		}
    	}
    }
}