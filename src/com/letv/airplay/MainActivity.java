package com.letv.airplay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Button button = new Button(this);
		setContentView(button);
		button.requestFocus();
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(MainActivity.this, AirplayService.class);
				i.setAction("startService");
				startService(i);
				MainActivity.this.finish();
			}
		});
	}
}
