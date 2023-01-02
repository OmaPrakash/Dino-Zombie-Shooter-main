package com.buildbox;

import org.cocos2dx.lib.Cocos2dxActivity;
import java.lang.ref.WeakReference;

public class AdIntegrator {
    public static native boolean rewardedVideoDidEnd();

    private static WeakReference<Cocos2dxActivity> activity;

    public static void initBridge(Cocos2dxActivity act){
        activity = new WeakReference<Cocos2dxActivity>(act);
    }

    public static void initAds(){

    }

    public static void showBanner(){

    }

    public static void hideBanner(){

    }

    public static boolean isBannerVisible(){
        return true;
    }

    public static boolean isRewardedVideoAvialable(){
        return true;
    }

    public static void showInterstitial(){

    }

    public static void showRewardedVideo(){
        rewardedVideoDidEnd();
    }

    public static void buttonActivated(){

    }
    
    public static boolean buttonVisible(){
        return true;
    }
}
