package by.andreidanilevich.autopinsk;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class MapActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);

		WebView wv = (WebView) findViewById(R.id.wv);
		wv.getSettings().setUseWideViewPort(true);
		wv.getSettings().setBuiltInZoomControls(true);
		wv.setInitialScale(1);
		wv.loadDataWithBaseURL("file:///android_asset/",
				"<img src=\"busstops.gif\">", "text/html", "utf-8", null);
	}

	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.anim_rightback, R.anim.anim_leftback);
	}
}
