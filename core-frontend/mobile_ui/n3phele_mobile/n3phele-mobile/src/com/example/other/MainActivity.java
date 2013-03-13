package com.example.other;
import org.apache.cordova.*;


import android.os.Bundle;
import android.app.Activity;
import com.google.ads.*;
import android.view.Menu;
import android.widget.LinearLayout;

public class MainActivity extends DroidGap {
	private static final String AdMob_Ad_Unit = "a1513f7609696dd";
	private AdView adView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.loadUrl("file:///android_asset/www/index.html");

		/** ADmob**/
		adView = new AdView(this, AdSize.BANNER, AdMob_Ad_Unit); 
		LinearLayout layout = super.root;  
		layout.addView(adView); 
		adView.loadAd(new AdRequest());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
