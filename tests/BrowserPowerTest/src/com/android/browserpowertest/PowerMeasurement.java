package com.android.browserpowertest;

import android.content.Intent;
import android.app.Instrumentation;
import android.os.Handler;
import android.os.Message;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import junit.framework.*;

public class PowerMeasurement extends ActivityInstrumentationTestCase2<PowerTestActivity> {

    private static final String LOGTAG = "PowerMeasurement";
    private static final String PKG_NAME = "com.android.browserpowertest";
    private static final String TESTING_URL =
        "http://75.17.48.204:10088/nyt/index.html";
    private static final int TIME_OUT = 2 * 60 * 1000;
    private static final int DELAY = 0;

    public PowerMeasurement() {
        super(PKG_NAME, PowerTestActivity.class);
    }

    public void testPageLoad() throws Throwable {
        Instrumentation mInst = getInstrumentation();
        PowerTestActivity act = getActivity();

        Intent intent = new Intent(mInst.getContext(), PowerTestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        long start = System.currentTimeMillis();
        PowerTestActivity activity = (PowerTestActivity)mInst.startActivitySync(
                intent);
        activity.reset();
        //send a message with the new URL
        Handler handler = activity.getHandler();
        Message msg = handler.obtainMessage(
                PowerTestActivity.MSG_NAVIGATE, TIME_OUT, DELAY);
        msg.getData().putString(PowerTestActivity.MSG_NAV_URL, TESTING_URL);
        msg.getData().putBoolean(PowerTestActivity.MSG_NAV_LOGTIME, true);

        handler.sendMessage(msg);
        boolean timeoutFlag = activity.waitUntilDone();
        long end = System.currentTimeMillis();
        assertFalse(TESTING_URL + " failed to load", timeoutFlag);
        boolean pageErrorFlag = activity.getPageError();
        assertFalse(TESTING_URL + " is not available, either network is down or the server is down",
                pageErrorFlag);
        Log.v(LOGTAG, "Page is loaded in " + activity.getPageLoadTime() + " ms.");

        // Force to clean up the cache dir so that it get back to the clean
        // state
        Runtime fileRemoval = Runtime.getRuntime();
        String cmdBecomeSu = "su";
        boolean clearCacheSuccess = false;
        try{
            Process runsum = fileRemoval.exec(cmdBecomeSu);
            int exitVal = runsum.waitFor();
            String rmfile = "rm -r /data/data/com.android.browserpowertest/cache";
            Process removal = fileRemoval.exec(rmfile);
            exitVal = removal.waitFor();
            if (exitVal == 0) {
                clearCacheSuccess = true;
            }
        } catch ( Exception e){
            assertTrue("Fails to clear the cahche", false);   
        }
        assertTrue("Fails to clear the cahche", clearCacheSuccess);
        activity.finish();
    }
}
