package com.xht.verifinger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class WelcomeActivity extends Activity {

	private final int SPLASH_DISPLAY_LENGHT = 3000; // —”≥Ÿ»˝√Î

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent mainIntent = new Intent(WelcomeActivity.this,
						MainActivity.class);
				WelcomeActivity.this.startActivity(mainIntent);
				overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
				WelcomeActivity.this.finish();	

			}

		}, SPLASH_DISPLAY_LENGHT);
	}
}