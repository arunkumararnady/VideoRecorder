package com.diona.videoplugin;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import android.util.Log;
import android.provider.Settings;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class VideoRecorder extends CordovaPlugin {
	public static final String TAG = "Video Plugin";
	public CallbackContext callbackContext;
	/**
	 * Constructor.
	 */
	public VideoRecorder() {}
	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 *
	 * @param cordova The context of the main Activity.
	 * @param webView The CordovaWebView Cordova is running in.
	 */
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		Log.v(TAG,"Init Plugin");
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("videoOutputFile onActivityResult");
	    if (requestCode == 0) {
	        if(resultCode == cordova.getActivity().RESULT_OK){
	            String result=data.getStringExtra("result");
	            String result2=data.getDataString();
	            System.out.println("videoOutputFile onActivityResult"+result2);
	            this.callbackContext.success(result2);
	        }
	        if (resultCode == cordova.getActivity().RESULT_CANCELED) {
	            //Write your code if there's no result
	        	System.out.println("videoOutputFile onActivityResult failed");
	        	//this.callbackContext.error();
	        }
	    }
	}
	
	public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		final int duration = Toast.LENGTH_SHORT;
		Log.v(TAG,"execute call received :"+ action);
		Context context =  cordova.getActivity().getApplicationContext();
		Intent intent = new Intent(context,CameraActivity.class);

		Log.v(TAG,"activity start request");
		//cordova.getActivity().setActivityResultCallback(CameraActivity.this);
		this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
		Log.v(TAG,"activity start request completed");
		return true;
	}
}