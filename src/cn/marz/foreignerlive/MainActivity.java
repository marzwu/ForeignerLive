package cn.marz.foreignerlive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getList();

//		LinearLayout rlMain = null;
//		rlMain = (LinearLayout) MainActivity.this.findViewById(R.id.main);
//		LinearLayout rl = null;
//		rl = (LinearLayout) MainActivity.this.findViewById(R.id.main1);
//		rl.setLayoutParams(new LinearLayout.LayoutParams(240, 2000));
//		rl = (LinearLayout) MainActivity.this.findViewById(R.id.main2);
//		rl.setLayoutParams(new LinearLayout.LayoutParams(240, 2000));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private ArrayList<String> urls = new ArrayList<String>();
	private int cursor = Integer.MAX_VALUE;
	private int _y = 0;

	private void getList() {
		final String urlStr = "http://appservice.sinaapp.com/foreignerlive/down.php?id="
				+ cursor;

		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					URL url = new URL(urlStr);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream inputStream = conn.getInputStream();

					BufferedReader r = new BufferedReader(
							new InputStreamReader(inputStream));
					StringBuilder total = new StringBuilder();
					String line;
					while ((line = r.readLine()) != null) {
						total.append(line);
					}

					Log.i("getlist", total.toString());

					String ja = total.toString();

					try {
						JSONArray json = new JSONArray(ja);
						Log.i("getlist", json.toString());
						if (json.length() > 0) {
							cursor = json.getInt(0);
							urls.clear();
							for (int i = 1; i < json.length(); i++){
								String path = json.getString(i);
								String subfix = path.substring(path.lastIndexOf('.'));
								subfix = "g"+subfix;
								path = path.substring(0, path.length() - subfix.length()) + subfix;
								urls.add(path);
							}

							Log.i("getlist", cursor + "");
							Log.i("getlist", urls.get(0));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}

					Message msg = new Message();
					msg.what = 2;
					handler.sendMessage(msg);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void addImage(String url) {
		// final String imgPath =
		// "http://media-cache4.snatchly.com/29433dbd94ad461c9470aa16a2361e71_d.jpg";
		Log.i("url", url);
		final String imgPath = url;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URL url = new URL(imgPath);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream inputStream = conn.getInputStream();
					Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
					Message msg = new Message();
					msg.obj = bitmap;
					msg.what = 1;
					handler.sendMessage(msg);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private long time;
	private boolean bInit = false;
	private int totals = 3;

	private void addTotals() {
		++totals;
	}

	private void addImages() {
		while (totals > 0 && urls.size() > 0) {
			--totals;
			Log.i("totals", totals + "");
			addImage(urls.remove(0));
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				addTotals();
				if (urls.size() > 0) {
					addImages();
				} else {
					Log.i("complete", (System.currentTimeMillis() - time) + "");
					Log.i("complete", totals + "");
				}

				Bitmap data = (Bitmap) msg.obj;
				resIds.add(data);
				Log.i("width", data.getWidth()+"");
				Log.i("height", data.getHeight()+"");

//				ImageView view = new ImageView(getBaseContext());

				LinearLayout rl = null;
				if (resIds.size() % 2 == 1)
					rl = (LinearLayout) MainActivity.this
							.findViewById(R.id.main1);
				else
					rl = (LinearLayout) MainActivity.this
							.findViewById(R.id.main2);
				
				LayoutInflater inflater = getLayoutInflater();  
				ImageView view = (ImageView) inflater.inflate(R.layout.imageitem, null);  
				
//				ImageView view = new ImageView(rl.getContext());
				view.setScaleType(ScaleType.CENTER_CROP);
				view.setImageBitmap(data);
				Log.i("width", view.getWidth()+"");
				Log.i("height", view.getHeight()+"");

				rl.addView(view);

				// if (false == bInit) {
				// bInit = true;
				// Gallery gallery = (Gallery) findViewById(R.id.main);
				// ImageAdapter imageAdapter = new ImageAdapter(
				// MainActivity.this);
				// gallery.setAdapter(imageAdapter);
				// }
				break;
			case 2:
				time = System.currentTimeMillis();
				addImages();
			}
		}
	};

	private ArrayList<Bitmap> resIds = new ArrayList<Bitmap>();
}
