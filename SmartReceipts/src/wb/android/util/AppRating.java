package wb.android.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppRating {
    
	private static final String DONT_SHOW = "dont_show";
	private static final String LAUNCH_COUNT = "launches";
	private static final String THRESHOLD = "threshold";
	
    public static final void onLaunch(final Context context, final int launchesUntilPrompt, final String appName, final String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(appName + "rating", 0);
        if (prefs.getBoolean(DONT_SHOW, false)) 
        	return;
        final SharedPreferences.Editor editor = prefs.edit();
        final int launchCount = prefs.getInt(LAUNCH_COUNT, 0) + 1;
        final int threshold = prefs.getInt(THRESHOLD, launchesUntilPrompt);
        editor.putInt(LAUNCH_COUNT, launchCount);
        if (launchCount == threshold) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    		builder.setTitle("Like " + appName + "?")
    			   .setCancelable(true)
    			   .setPositiveButton("Rate It Now", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		        	   	context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
    		                dialog.cancel();
    		           }
    		       })
    		       .setNeutralButton("Later", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		        	   	editor.putInt(THRESHOLD, threshold+10);
    		        	   	editor.commit();
    		                dialog.cancel();
    		           }
    		       })
    			   .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		        	   	editor.putBoolean(DONT_SHOW, true);
    		        	   	editor.commit();
    		                dialog.cancel();
    		           }
    		       })
    		       .create().show();    	
        }
        editor.commit();
    }   
    
}
