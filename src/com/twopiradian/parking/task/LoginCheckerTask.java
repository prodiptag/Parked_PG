/**
 * 
 */
package com.twopiradian.parking.task;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import android.os.AsyncTask;

import com.twopiradian.parking.Params;
import com.twopiradian.parking.listener.TaskCompletionListener;

/**
 * @author Prodipta Golder
 *
 */
public class LoginCheckerTask extends AsyncTask<Properties, Void, Boolean> {
	private String message = "";

	private TaskCompletionListener<Boolean> callbackListener;
	
	public LoginCheckerTask(TaskCompletionListener<Boolean> callbackListener) {
		this.callbackListener = callbackListener;
	}
	
	@Override
	protected Boolean doInBackground(Properties... props) {
		boolean loginSuccess = false;
		try {
			if ((props == null) || (props.length == 0)) {
				return false;
			}

			Properties map = props[0];
			String loginUrl = map.getProperty(Params.URL);
			String userId = map.getProperty(Params.USER_ID);
			String passwd = map.getProperty(Params.PASSWORD);
			String service = map.getProperty(Params.SERVICE);
			URL url = new URL(loginUrl);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setRequestMethod("POST");
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			StringBuilder content = new StringBuilder();
			content.append("Email=").append(userId);
			content.append("&Passwd=").append(passwd);
			content.append("&service=").append(service);
			
//			message += "*****open connection";
			OutputStream outputStream = urlConn.getOutputStream();
//			message += "******get stream";
			outputStream.write(content.toString().getBytes("UTF-8"));
			outputStream.close();
//			message += "****close stream";
			
			if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				message = userId + "! login hoyechhe hu hu ha ha ha...";
				loginSuccess = true;
			} else {
				message = "holo na abar korte hobe...";
				loginSuccess = false;
			}
		} catch (Exception e) {
			message = e.toString();
			loginSuccess = false;
		}
		return loginSuccess;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		callbackListener.execute(result, message);
	}
}
