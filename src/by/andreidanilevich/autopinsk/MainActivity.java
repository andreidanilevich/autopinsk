package by.andreidanilevich.autopinsk;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {

	ArrayList<HashMap<String, Object>> mList;
	private SQLiteDatabase DB_rasp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DB_rasp = openOrCreateDatabase("rasp.db", Context.MODE_PRIVATE, null);
		Cursor cursor;
		mList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hm;

		cursor = DB_rasp.rawQuery("SELECT * FROM table_1", null);
		final String[] intent_text = new String[cursor.getCount()];
		cursor.moveToFirst();

		do {
			intent_text[cursor.getPosition()] = cursor.getString(cursor
					.getColumnIndex("data"));
			hm = new HashMap<>();
			hm.put("num_list", cursor.getString(cursor.getColumnIndex("name")));
			hm.put("img_list", R.drawable.list_bus);
			mList.add(hm);
		} while (cursor.moveToNext());

		ListView lv = (ListView) findViewById(R.id.lv);
		cursor.close();
		DB_rasp.close();

		SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),
				mList, R.layout.css, new String[] { "num_list", "img_list" },
				new int[] { R.id.num_list, R.id.img_list });

		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View itemClicked,
					int position, long id) {

				Intent intent = new Intent(MainActivity.this, WebActivity.class);
				intent.putExtra("intent_text", intent_text[position]);
				intent.putExtra("intent_pix", 320);
				startActivity(intent);
				overridePendingTransition(R.anim.anim_left, R.anim.anim_right);
			}
		});
	}

	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.anim_rightback, R.anim.anim_leftback);
	}

}
