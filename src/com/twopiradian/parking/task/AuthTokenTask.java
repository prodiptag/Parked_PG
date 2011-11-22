/**
 * 
 */
package com.twopiradian.parking.task;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.twopiradian.parking.listener.TaskCompletionListener;

/**
 * @author Prodipta Golder
 *
 */
public class AuthTokenTask extends AsyncTask<Account, Void, Boolean> {
	private TaskCompletionListener<Boolean> callbackListener;
	private AccountManager accMgr;
	private String authToken;
	private Activity activity;
	
	public AuthTokenTask(AccountManager accMgr, TaskCompletionListener<Boolean> callbackListener) {
		this.callbackListener = callbackListener;
		this.accMgr = accMgr;
	}
	
	public AuthTokenTask(AccountManager accMgr, TaskCompletionListener<Boolean> callbackListener, Activity activity) {
		this.callbackListener = callbackListener;
		this.accMgr = accMgr;
		this.activity = activity;
	}
	
	@Override
	protected Boolean doInBackground(Account... accounts) {
		try {
			AccountManagerFuture<Bundle> result = activity != null ? 
					accMgr.getAuthToken(accounts[0], "cl", null, activity, null, null) : 
						accMgr.getAuthToken(accounts[0], "cl", false, null, null);
			Bundle bundle = (Bundle) result.getResult();
			authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
			if (authToken == null) {
				return false;
			}
		} catch (Exception e) {
			Log.i("Auth token", e.toString());
			return false;
		}
		return true;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		callbackListener.execute(result, authToken);
	}
}
