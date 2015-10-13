package by.andreidanilevich.autopinsk;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class TaxiActivity extends Activity {

	private static final int IDM_KONT = 101;
	private static final int IDM_FREE = 102;

	private String[] nazvTax = { "АЛЬФА 696-90-50 (Velcom)",
			"АЭЛИТА 680-90-88 (Velcom)", "ВОЯЖ 907-22-22 (Velcom)",
			"МОТОР 35-43-64 (Городской)", "МОТОР 696-43-64 (Velcom)",
			"ПРЕСТИЖ 951-22-22 (Velcom)", "ПРЕСТИЖ 851-22-22 (MTC)",
			"ПРЕСТИЖ 951-22-22 (Life)" };

	private String[] nomTax = { "80296969050", "80296809088", "80299072222",
			"80165354364", "80296964364", "80299512222", "80298512222",
			"80259512222" };

	private String[] nameTax = { "Такси АЛЬФА", "Такси АЭЛИТА", "Такси ВОЯЖ",
			"Такси МОТОР", "Такси МОТОР", "Такси ПРЕСТИЖ", "Такси ПРЕСТИЖ",
			"Такси ПРЕСТИЖ" };

	Integer nomer;
	ArrayList<HashMap<String, Object>> mList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ListView lv = (ListView) findViewById(R.id.lv);

		mList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hm;

		Integer i = 0;
		do {
			hm = new HashMap<>();
			hm.put("num_list", nazvTax[i]);
			hm.put("img_list", R.drawable.list_tax);
			mList.add(hm);
			i = i + 1;
		} while (i != nazvTax.length);

		SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),
				mList, R.layout.css, new String[] { "num_list", "img_list" },
				new int[] { R.id.num_list, R.id.img_list });

		lv.setAdapter(adapter);
		registerForContextMenu(lv);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View itemClicked,
					int position, long id) {

				Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri
						.parse("tel:" + nomTax[position]));
				startActivity(dialIntent);
				overridePendingTransition(R.anim.anim_left, R.anim.anim_right);
			}
		});

		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				nomer = position;
				return false;
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(nameTax[nomer] + " - номер: " + nomTax[nomer]);
		menu.add(Menu.NONE, IDM_KONT, Menu.NONE, "Добавить в контакты");
		menu.add(Menu.NONE, IDM_FREE, Menu.NONE, "Добавить в SMS ...");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case IDM_KONT:
			Intent i = new Intent(
					ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
					Uri.parse(String.format("tel: %s", nomTax[nomer])));
			i.addCategory(Intent.CATEGORY_DEFAULT);
			i.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
			i.putExtra(ContactsContract.Intents.Insert.NAME, nameTax[nomer]);
			i.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
					ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
			startActivity(i);
			break;
		case IDM_FREE:
			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			smsIntent.setType("vnd.android-dir/mms-sms");
			smsIntent.putExtra("sms_body", nameTax[nomer] + " - номер: "
					+ nomTax[nomer]);
			startActivity(smsIntent);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.anim_rightback, R.anim.anim_leftback);
	}
}
