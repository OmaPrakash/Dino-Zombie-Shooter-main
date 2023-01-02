#include "PTPAppDelegate.h"
#include "cocos2d.h"
#include "PTPConfig.h"
#include "models/PTModelController.h"
#include "PTPSettingsController.h"
#include "platform/android/jni/JniHelper.h"
#include <jni.h>
#include <android/log.h>
#include "models/screens/PTModelScreen.h"
#include "models/PTModelAtlas.h"
#include "models/PTModelGeneralSettings.h"
#include "models/objects/PTModelAssetCharacter.h"
#include "models/objects/PTModelObjectButtonPurchase.h"

#define  LOG_TAG    "main"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define ARCHIVE_PASSWORD PTJniHelper_passwordJNI()

using namespace cocos2d;

extern "C"
{

jint JNI_OnLoad(JavaVM *vm, void *reserved){
    JniHelper::setJavaVM(vm);
    return JNI_VERSION_1_4;
}

std::string PTJniHelper_passwordJNI(){
    JniMethodInfo methodInfo;
    if (! JniHelper::getStaticMethodInfo(methodInfo, "com/secrethq/utils/PTJniHelper", "password", "()Ljava/lang/String;")){
        return "";
    }
    jstring returnString = (jstring) methodInfo.env->CallStaticObjectMethod(methodInfo.classID, methodInfo.methodID);
    methodInfo.env->DeleteLocalRef(methodInfo.classID);

    const char *out = methodInfo.env->GetStringUTFChars(returnString, NULL);
    methodInfo.env->DeleteLocalRef(returnString);

    std::string outString(out);
    return  outString;

}
void Java_com_secrethq_utils_PTJniHelper_updateScreenGeometry(JNIEnv*  env, jobject thiz, jstring adNetworkNameString ){
    PTPAppDelegate::updateScreenGeometry();
}

jboolean Java_com_secrethq_utils_PTJniHelper_isAdNetworkActive(JNIEnv*  env, jobject thiz, jstring adNetworkNameString ){
    const char* adNetworkName = env->GetStringUTFChars(adNetworkNameString, 0);
    std::string platformName = "Google Play Store";

    PTModelController *mc = PTModelController::shared();
    PTPSettingsController* sc = PTPSettingsController::shared();

    std::vector<PTModelScreenPtr> screensArray  = mc->getModels<PTModelScreen>();
    if(screensArray.empty() || sc->removeAds()){
        return false;
    }
    for(int i=0; i < screensArray.size(); i++){
        PTModelScreenPtr model = screensArray.at( i );
        if(model->adNetworkFullscreen() ==  adNetworkName){
            return true;
        }
        if(model->adNetworkBanner() == adNetworkName){
            return true;    
        }
    }

    // check if this network in backup list
    // banner
    std::list<std::string> bannerList = PTModelGeneralSettings::shared()->adBannersList( platformName );
    std::list<std::string>::const_iterator bannerIterator;
    for (bannerIterator = bannerList.begin(); bannerIterator != bannerList.end(); ++bannerIterator) {
        if(strcmp((*bannerIterator).c_str(), adNetworkName) == 0){
            PTLog("ad Network (%s) in backup list (banner)", adNetworkName);
            return true;
        }
    }

    // interstitials
    std::list<std::string> interstitialList = PTModelGeneralSettings::shared()->adInterstitialsList( platformName );
    std::list<std::string>::const_iterator interstittialsIterator;
    for (interstittialsIterator = interstitialList.begin(); interstittialsIterator != interstitialList.end(); ++interstittialsIterator) {
        if(strcmp((*interstittialsIterator).c_str(), adNetworkName) == 0){
            PTLog("ad Network (%s) in backup list (interstitials)", adNetworkName);
            return true;
        }
    }

    // check if network is used as reward video
    std::vector<PTModelObjectButtonPurchasePtr> buttonsArray  = mc->getModels<PTModelObjectButtonPurchase>();
    if ( !buttonsArray.empty() ){
        for(int i=0; i < buttonsArray.size(); i++){
            PTModelObjectButtonPurchasePtr model = buttonsArray.at( i );
            if ( model->purchaseMethod() == "kRewardedVideos"
                 && model->rewardedVideoAdNetwork() == adNetworkName) {
                return true;
            }
        }
    }

    // check if network is used as reward video
    std::vector<PTModelAssetCharacterPtr> charsArray  = mc->getModels<PTModelAssetCharacter>();
    if ( !charsArray.empty() ){
        for(int i=0; i < charsArray.size(); i++){
            PTModelAssetCharacterPtr model = charsArray.at( i );
            if ( model->purchaseMethod() == "kRewardedVideos"
                 && model->rewardedVideoAdNetwork() == adNetworkName) {
                return true;
            }
        }
    }

    return false;
}

void Java_org_cocos2dx_lib_Cocos2dxRenderer_nativeInit(JNIEnv*  env, jobject thiz, jint w, jint h){
    PTModelController *mc = PTModelController::shared();
    if (!CCDirector::sharedDirector()->getOpenGLView()){
        CCEGLView *view = CCEGLView::sharedOpenGLView();
        view->setFrameSize(w, h);

        /////////////////////////////////////
        bool bRet = false;
        unsigned long size = 0;
        char* pBuffer = (char*)CCFileUtils::sharedFileUtils()->getFileData("data/data.pkg", "rb", &size);
        if (pBuffer != NULL && size > 0){
            PTLog("data.pkg size: (%lu)", size);
            mc->setUsingDataEncryption( true );
        }

        std::string path = CCFileUtils::sharedFileUtils()->getWritablePath();
        std::string filePath = path + std::string("data.pkg");
        FILE *fp = fopen(filePath.c_str(), "wb");
        if (! fp){
            PTLog("can not create file %s", path.c_str());
            return;
        }

        fwrite(pBuffer, 1, size, fp);
        fclose(fp);

        if ( CCFileUtils::sharedFileUtils()->isFileExist(filePath) ) {
            PTLog("pkg file is good");
        }

        //loading general info 
        mc->clean();
        mc->loadDataForSplashScreen(filePath, ARCHIVE_PASSWORD.c_str());

        PTPAppDelegate *pAppDelegate = new PTPAppDelegate();
		pAppDelegate->setDataArchiveProcessor(ARCHIVE_PASSWORD);
		
        CCApplication::sharedApplication()->run();

        //clean up main model controller before starting loading Objects form XML files
        mc->clean();


    }
    else {
        PTLog("NATIVE RE INIT");
        ccGLInvalidateStateCache();        
        CCShaderCache::sharedShaderCache()->reloadDefaultShaders();
        ccDrawInit();
        CCTextureCache::reloadAllTextures();
        CCNotificationCenter::sharedNotificationCenter()->postNotification(EVNET_COME_TO_FOREGROUND, NULL);
        CCDirector::sharedDirector()->setGLDefaultValues(); 

    }
}
void Java_org_cocos2dx_lib_Cocos2dxRenderer_nativeOnSurfaceChanged(JNIEnv*  env, jobject thiz, jint w, jint h){
    auto models = PTModelController::shared()->getModels(PTModelGeneralSettings::staticType());
    if(models.size() != 0){ //Checking if there is no PTModelGeneralSettings then we are not ready for reshape (meains we are in the middle of loading process)
        CCEGLView *glView = CCEGLView::sharedOpenGLView();
        CCDirector::sharedDirector()->reshapeProjection(CCSizeMake(w,h));
        glView->setFrameSize(w, h);
        PTPAppDelegate::updateScreenGeometry();
    }

}

}
