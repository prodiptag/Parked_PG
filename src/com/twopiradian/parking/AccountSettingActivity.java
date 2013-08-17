package com.twopiradian.parking;

import java.util.HashMap;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twopiradian.itracker.connector.listener.TaskCompletionListener;
import com.twopiradian.itracker.connector.task.AuthTokenTask;

/**
 * 
 * @author Prodipta Golder
 */

public class AccountSettingActivity extends Activity {
	
	private LinearLayout linerLayout;
	private LinearLayout buttonLayout;
	private Button exitBtn;
	private TextView testResult;
	private AccountManager accMgr;
	private int currentAccountIndex = -1;
	private Map<String, Account> accountMap = new HashMap<String, Account>();
	private String authToken;
	
	public void showAlert(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setMessage(message);
		alertDialog.setTitle(title);
		alertDialog.setButton(1, "OK", 
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
		Account[] accs = accMgr.getAccountsByType("com.google");
		int i = 2;
		for (Account acc : accs) {
			accountMap.put(acc.name, acc);
			smenu.add(1, i, (i - 2), acc.name);
			i++;
		}
		smenu.setGroupCheckable(1, true, true);
		if (currentAccountIndex > -1) {
			MenuItem item = smenu.getItem(currentAccountIndex);
			item.setChecked(true);
		}
		menu.add(0, 1, 0, "Exit");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if ("Exit".equals(item.getTitle())) {
			finish();
	    } else {
	    	currentAccountIndex = item.getOrder();
	    	Account account = accountMap.get(item.getTitle());
	    	if (account != null) {
//		    	authEmail = account.name;
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
	
	private void afterLoging() {
		Intent serviceIntent = new Intent();
		serviceIntent.putExtra("CalAuthToken", authToken);
		startService(serviceIntent);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		linerLayout = new LinearLayout(this);
		linerLayout.setOrientation(LinearLayout.VERTICAL);
		linerLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		accMgr = AccountManager.get(this);
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
    }

    private class LoginTaskCompletionListener implements TaskCompletionListener<Boolean> {
    	public void execute(Boolean loginSuccess, Object token) {
    		if (loginSuccess && (token != null)) {
    			authToken = (String) token;
    			afterLoging();
    		} else {
    			showAlert("Login failed...", "hoini. abar account select koro...");
    		}
    	}
    }
}