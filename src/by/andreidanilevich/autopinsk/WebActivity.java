package by.andreidanilevich.autopinsk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.webkit.WebView;

public class WebActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		WebView wv = (WebView) findViewById(R.id.wv);
		Intent intent = getIntent();
		String intent_text = intent.getStringExtra("intent_text");
		Integer intent_pix = intent.getIntExtra("intent_pix", 640);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		wv.getSettings().setUseWideViewPort(false);
		wv.setInitialScale((int) ((metrics.xdpi / intent_pix)
				* (metrics.widthPixels / metrics.xdpi) * 97));

		wv.loadDataWithBaseURL("file:///android_asset/img/",
				"<html><body style=\"width: " + intent_pix.toString()
						+ " px;\"><div style=\"text-align: left;\">"
						+ intent_text + "</body></html>", "text/html", "utf-8",
				null);

	}

	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.anim_rightback, R.anim.anim_leftback);
	}
}
