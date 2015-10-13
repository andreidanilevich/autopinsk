package by.andreidanilevich.autopinsk;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class MenuActivity extends Activity {

	Handler handler;
	Animation anim_btn;
	ImageView menu_1, menu_2, menu_3, menu_4;
	Integer NofC = 0;
	Document doc;
	LinearLayout load_RL;
	private SQLiteDatabase DB_temp, DB_rasp;
	TextView load_text, text_data;
	SimpleDateFormat clock = new SimpleDateFormat("HH:mm / dd.MM.yyyyг.");
	Cursor cursor;
	Boolean isUpdate = false, nowUpdate = false, isCanceled = false;
	Update_temp ut;
	Button menu_1_1, menu_1_2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		anim_btn = AnimationUtils.loadAnimation(this, R.anim.anim_btn);
		menu_1 = (ImageView) findViewById(R.id.imageView1);
		menu_2 = (ImageView) findViewById(R.id.imageView2);
		menu_3 = (ImageView) findViewById(R.id.imageView3);
		menu_4 = (ImageView) findViewById(R.id.imageView4);

		menu_1_1 = (Button) findViewById(R.id.menu_1_1);
		menu_1_2 = (Button) findViewById(R.id.menu_1_2);

		load_text = (TextView) findViewById(R.id.load_text);
		text_data = (TextView) findViewById(R.id.text_data);
		load_text.setText("Потребуется несколько минут Вашего времени.");
		text_data.setText("Последнее обновление: --:-- / --.--.----г.");

		DB_rasp = openOrCreateDatabase("rasp.db", Context.MODE_PRIVATE, null);
		DB_rasp.execSQL("CREATE TABLE IF NOT EXISTS table_1 (_id integer primary key autoincrement, name, data)");
		DB_rasp.execSQL("CREATE TABLE IF NOT EXISTS table_2 (_id integer primary key autoincrement, time, data)");

		load_RL = (LinearLayout) findViewById(R.id.load_RL);

		// выведем дату если есть
		cursor = DB_rasp.rawQuery("SELECT * FROM table_2", null);
		if (cursor.moveToFirst()) {
			isUpdate = true;
			text_data.setText("Последнее обновление: "
					+ cursor.getString(cursor.getColumnIndex("time")));
		}
		cursor.close();
	}

	class Connecting extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			// подготовим следующий класс
			ut = new Update_temp();

			try {
				doc = Jsoup
						.connect(
								"http://pinskap.by/content/traffic/urban_transport/")
						.timeout(15 * 1000).get();
			} catch (IOException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (doc == null) {
				load_text
						.setText(load_text.getText()
								+ "\n- соединение не удалось, проверьте Ваше подключение к интернету");
				nowUpdate = false;
				menu_1_1.setEnabled(true);
				menu_1_2.setEnabled(true);
			} else {
				ut.execute();
			}
		}
	}

	class Update_temp extends AsyncTask<Void, Integer, Void> {

		ContentValues values = new ContentValues();
		Document doc2;
		Boolean error_load = false;

		@Override
		protected Void doInBackground(Void... params) {

			DB_temp = openOrCreateDatabase("temp.db", Context.MODE_PRIVATE,
					null);
			DB_temp.execSQL("CREATE TABLE IF NOT EXISTS table_1 (_id integer primary key autoincrement, name, data)");
			DB_temp.execSQL("CREATE TABLE IF NOT EXISTS table_2 (_id integer primary key autoincrement, time, data)");
			DB_temp.execSQL("drop table table_1");
			DB_temp.execSQL("drop table table_2");
			DB_temp.execSQL("CREATE TABLE IF NOT EXISTS table_1 (_id integer primary key autoincrement, name, data)");
			DB_temp.execSQL("CREATE TABLE IF NOT EXISTS table_2 (_id integer primary key autoincrement, time, data)");

			Elements links = doc
					.select("a[href*=/content/traffic/urban_transport/rasp/]");

			Integer pos = 0;
			for (Element link : links) {

				if (isCancelled())
					return null;

				try { // городские маршруты
					doc2 = Jsoup
							.connect("http://pinskap.by" + link.attr("href"))
							.timeout(15 * 1000).get();
				} catch (Exception e) {
				}
				if (doc2 == null
						|| doc2.select("td[class] > h1[id] ~ *").toString()
								.equals("")) {
					error_load = true;
					ut.cancel(true);
				} else {
					publishProgress(pos + 1, links.size());
					values.put("name", link.text()); // вставляем_NAME
					values.put("data", doc2.select("td[class] > h1[id] ~ *")
							.toString()); // вставляем_DATA
					DB_temp.insert("table_1", null, values);
					values.clear();
					pos++;
					doc2.empty();
				}
			}
			try { // автовокзал Пинск
				pos++;
				publishProgress(pos + 1, links.size());
				doc2 = Jsoup
						.connect("http://pinskap.by/content/traffic/pinsk.php")
						.timeout(15 * 1000).get();
			} catch (Exception e) {
			}
			if (doc2 == null
					|| doc2.select("td[class] > h1[id] ~ *").toString()
							.equals("")) {
				error_load = true;
			} else {
				values.put("data", doc2.select("td[class] > h1[id] ~ *")
						.toString()); // вставляем_DATA
				DB_temp.insert("table_2", null, values);
				values.clear();
			}
			return null;
		}

		protected void onCancelled() {
			super.onCancelled();
			DB_temp.execSQL("drop table table_1");
			DB_temp.execSQL("drop table table_2");
			values.clear();
			DB_temp.close();

			handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					load_text.setText("Загрузка прервана!");
					nowUpdate = false;
					menu_1_1.setEnabled(true);
					menu_1_2.setEnabled(true);
					isCanceled = false;
				}
			}, 1000);

		}

		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values[0] > values[1]) {
				load_text.setText(load_text.getText()
						+ "\n- автовокзал Пинск...");
			} else {
				load_text
						.setText("- проверка соединения... ожидайте\n- начата загрузка... ожидайте\n- городские маршруты: "
								+ values[0] + " из " + values[1] + "...");
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// перенесем данные из temp в rasp
			cursor.close();
			cursor = DB_temp.rawQuery("SELECT * FROM table_1", null);
			if (cursor.moveToFirst() && !error_load) {
				try { // удалим старую информацию из базы и запишем новую
					DB_rasp.execSQL("drop table table_1");
					DB_rasp.execSQL("drop table table_2");
					DB_rasp.execSQL("CREATE TABLE IF NOT EXISTS table_1 (_id integer primary key autoincrement, name, data)");
					DB_rasp.execSQL("CREATE TABLE IF NOT EXISTS table_2 (_id integer primary key autoincrement, time, data)");

					do {
						values.clear();
						values.put("name",
								cursor.getString(cursor.getColumnIndex("name")));
						values.put("data",
								cursor.getString(cursor.getColumnIndex("data")));
						DB_rasp.insert("table_1", null, values);
					} while (cursor.moveToNext());
					cursor = DB_temp.rawQuery("SELECT * FROM table_2", null);
					if (cursor.moveToFirst()) {
						values.clear();
						values.put("time", clock.format(new Date()).toString()); // вставим_Дату
						values.put("data",
								cursor.getString(cursor.getColumnIndex("data")));
						DB_rasp.insert("table_2", null, values);

						text_data.setText("Последнее обновление: "
								+ clock.format(new Date()).toString() + ".");
						load_text.setText(load_text.getText()
								+ "\n- загрузка завершена успешно.");

					}
				} catch (Exception e) {
					load_text
							.setText("Ошибка переноса данных.\nВозможно нехватает места или база повреждена.\nПереустановите приложение.");
				}
				DB_temp.execSQL("drop table table_1");
				DB_temp.execSQL("drop table table_2");

				values.clear();
				DB_temp.close();
				load_RL.setVisibility(View.INVISIBLE);

			} else { // если ошибка
				load_text.setText("Загрузка прервана!");
			}

			menu_1_1.setEnabled(true);
			menu_1_2.setEnabled(true);
			nowUpdate = false;
		}
	}

	// --------------------------------------------------------------ниже_обработка_кнопок
	public void click_RL(View v) {
	}

	public void menu_1_1(View v) {
		if (!nowUpdate) {
			if (isUpdate) {
				load_RL.setVisibility(View.INVISIBLE);
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"Расписания не найдено.\nНеобходимо обновить его!",
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.show();
			}
		}
	}

	public void menu_1_2(View v) {

		if (!nowUpdate) {
			menu_1_1.setEnabled(false);
			menu_1_2.setEnabled(false);

			load_text.setText("- проверка соединения... ожидайте");
			Connecting cn = new Connecting();
			cn.execute();
			nowUpdate = true;
		}
	}

	public void menu_1(View v) {
		menu_1.startAnimation(anim_btn);
		handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				Intent intent = new Intent(MenuActivity.this,
						MainActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.anim_left, R.anim.anim_right);
			}
		}, 200);
	}

	public void menu_2(View v) {
		menu_2.startAnimation(anim_btn);
		handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {

				cursor = DB_rasp.rawQuery("SELECT * FROM table_2", null);
				if (cursor.moveToFirst()) {

					Intent intent = new Intent(MenuActivity.this,
							WebActivity.class);
					intent.putExtra("intent_text",
							cursor.getString(cursor.getColumnIndex("data")));
					intent.putExtra("intent_pix", 640);
					startActivity(intent);
					overridePendingTransition(R.anim.anim_left,
							R.anim.anim_right);

				} else {
				}

			}
		}, 200);
	}

	public void menu_3(View v) {
		menu_3.startAnimation(anim_btn);
		handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				Intent intent = new Intent(MenuActivity.this, MapActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.anim_left, R.anim.anim_right);
			}
		}, 200);
	}

	public void menu_4(View v) {
		menu_4.startAnimation(anim_btn);
		handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				Intent intent = new Intent(MenuActivity.this,
						TaxiActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.anim_left, R.anim.anim_right);
			}
		}, 200);
	}

	public void onResume() {
		super.onResume();
		NofC = 0;
	}

	public void onBackPressed() {

		if (nowUpdate) { // если_щбновление_идет

			if (ut.getStatus() == Status.RUNNING && !isCanceled) {
				NofC = 0;
				AlertDialog.Builder quitDialog = new AlertDialog.Builder(
						MenuActivity.this);
				quitDialog
						.setTitle("Отменить загрузку?")
						.setMessage(
								"Вы хотите прервать загрузку обновления?\nЭто нежелательно, но возможно.\nВ этом случае произойдет откат изменений, а Вы сможете загрузить новое расписание позже.")
						.setIcon(R.drawable.autopinsk_icon).setCancelable(true);
				quitDialog.setPositiveButton("ОК", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						load_text.setText(load_text.getText()
								+ "\n- откат изменений\n- завершение загрузки... ожидайте");

						ut.cancel(true);
						isCanceled = true;
					}
				});
				quitDialog.show();
			}
		} else { // если обновление не идет

			NofC = NofC + 1;
			if (NofC > 1) {
				finish();
			} else {
				Toast toast = Toast.makeText(this,
						"Для выхода - нажмите еще раз!", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.show();
			}
		}
	}
}