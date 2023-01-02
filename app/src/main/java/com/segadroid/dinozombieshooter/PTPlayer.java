package com.segadroid.dinozombieshooter;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxReflectionHelper;

import android.os.Build;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.apponboard.aob_sessionreporting.AOBReporting;
import com.buildbox.AdIntegrator;
import com.secrethq.store.PTStoreBridge;
import com.google.android.gms.games.GamesActivityResultCodes;

import com.secrethq.ads.*;
import com.secrethq.utils.*;

import android.content.SharedPreferences;
import android.content.Context;


public class PTPlayer extends Cocos2dxActivity {

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    try {
		    Log.v("----------","onActivityResult: request: " + requestCode + " result: "+ resultCode);
		    if(PTStoreBridge.iabHelper().handleActivityResult(requestCode, resultCode, data)){
		    	Log.v("-----------", "handled by IABHelper");
		    }
		    else if(requestCode == PTServicesBridge.RC_SIGN_IN){
		    	SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();

		    	if(resultCode == RESULT_OK){
		    		PTServicesBridge.instance().onActivityResult(requestCode, resultCode, data);
		    		editor.putBoolean("GooglePlayServiceSignInError", false);
					editor.commit();
		    	}
		    	else if(resultCode == GamesActivityResultCodes.RESULT_SIGN_IN_FAILED){
		    		int duration = Toast.LENGTH_SHORT;
		    		Toast toast = Toast.makeText(this, "Google Play Services: Sign in error", duration);
		    		toast.show();
		    		editor.putBoolean("GooglePlayServiceSignInError", true);
					editor.commit();
		    	}
		    	else if(resultCode == GamesActivityResultCodes.RESULT_APP_MISCONFIGURED){
		    		int duration = Toast.LENGTH_SHORT;
		    		Toast toast = Toast.makeText(this, "Google Play Services: App misconfigured", duration);
		    		toast.show();	    		
		    	}
		    }
	    } catch (Exception e) {
		    	Log.v("-----------", "onActivityResult FAIL on iabHelper : " + e.toString());
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.hideVirtualButton();

		AOBReporting.initialize(this, "2.3.9");

		PTServicesBridge.initBridge(this, getString( R.string.app_id ));
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onNativeInit(){
			initBridges();				
	}

	private void initBridges(){
        AdIntegrator.initBridge( this );
		PTStoreBridge.initBridge( this );


		if (PTJniHelper.isAdNetworkActive("kChartboost")) {
			PTAdChartboostBridge.initBridge(this);
		}

        if (PTJniHelper.isAdNetworkActive("kInMobi")) {
            PTAdInMobiBridge.initBridge(this);
        }
		
		if (PTJniHelper.isAdNetworkActive("kAdMob") || PTJniHelper.isAdNetworkActive("kFacebook")) {
			PTAdAdMobBridge.initBridge(this);
		}

		if (PTJniHelper.isAdNetworkActive("kAppLovin")) {
			PTAdAppLovinBridge.initBridge(this);
		}

		if (PTJniHelper.isAdNetworkActive("kLeadBolt")) {
			PTAdLeadBoltBridge.initBridge(this);
		}
		
		if (PTJniHelper.isAdNetworkActive("kFacebook")) {
			PTAdFacebookBridge.initBridge(this);
		}
	}

	@Override
	public Cocos2dxGLSurfaceView onCreateView() {
		Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
		glSurfaceView.setEGLConfigChooser(8, 8, 8, 0, 0, 0);

		return glSurfaceView;
	}

	static {
		System.loadLibrary("player");
	}

	@Override
	protected void onResume() {
		this.hideVirtualButton();
		super.onResume();
		if (PTJniHelper.isAdNetworkActive("kChartboost")) {
			PTAdChartboostBridge.onResume( this );
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			this.hideVirtualButton();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		AOBReporting.startOrResumeSessionReporting();
		if (PTJniHelper.isAdNetworkActive("kChartboost")) {
			PTAdChartboostBridge.onStart( this );
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		AOBReporting.pauseSessionReporting();
		if (PTJniHelper.isAdNetworkActive("kChartboost")) {
			PTAdChartboostBridge.onStop( this );
		}
	}

	@Override
	protected void onDestroy() {
		AOBReporting.stopSessionReporting();
		super.onDestroy();
	}

	protected void hideVirtualButton() {
		if (Build.VERSION.SDK_INT >= 19) {
			// use reflection to remove dependence of API level

			Class viewClass = View.class;
			final int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = Cocos2dxReflectionHelper
					.<Integer> getConstantValue(viewClass,
							"SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION");
			final int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = Cocos2dxReflectionHelper
					.<Integer> getConstantValue(viewClass,
							"SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN");
			final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = Cocos2dxReflectionHelper
					.<Integer> getConstantValue(viewClass,
							"SYSTEM_UI_FLAG_HIDE_NAVIGATION");
			final int SYSTEM_UI_FLAG_FULLSCREEN = Cocos2dxReflectionHelper
					.<Integer> getConstantValue(viewClass,
							"SYSTEM_UI_FLAG_FULLSCREEN");
			final int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = Cocos2dxReflectionHelper
					.<Integer> getConstantValue(viewClass,
							"SYSTEM_UI_FLAG_IMMERSIVE_STICKY");
			final int SYSTEM_UI_FLAG_LAYOUT_STABLE = Cocos2dxReflectionHelper
					.<Integer> getConstantValue(viewClass,
							"SYSTEM_UI_FLAG_LAYOUT_STABLE");

			// getWindow().getDecorView().setSystemUiVisibility();
			final Object[] parameters = new Object[] { SYSTEM_UI_FLAG_LAYOUT_STABLE
					| SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
					| SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
					| SYSTEM_UI_FLAG_IMMERSIVE_STICKY };
			Cocos2dxReflectionHelper.<Void> invokeInstanceMethod(getWindow()
							.getDecorView(), "setSystemUiVisibility",
					new Class[] { Integer.TYPE }, parameters);
		}
	}
}
