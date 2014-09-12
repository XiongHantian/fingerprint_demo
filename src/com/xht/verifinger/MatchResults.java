package com.xht.verifinger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xht.util.HttpUtil;
import com.xht.util.LocalManager;
import com.xht.verifinger.R;
import com.xht.view.waterfall.WaterFall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class MatchResults extends Activity {
	// Data
	private static final String TAG = "MatchResults";
	public static final int RESULTNUM = 10;
	private static final String SERVER_ENROLL_URL = "http://finger.nat123.net/finger/enroll/";
	private static final String SERVER_URL = "http://finger.nat123.net/finger/";
	private static final String SERVER_AVATAR_URL = "http://finger.nat123.net/finger/avatar/";
	// variables
	private List<String> scorelist;
	private List<String> imgidlist;
	public static int type = 1; // 0:什么都不做 1：从本地加载匹配结果 2：从服务器上获取匹配结果
	// View
	private WaterFall waterFall;
	private ProgressDialog dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.matchresults);

		if (type == 0) {
		} else {
			dialog = new ProgressDialog(MatchResults.this);
			dialog.setMessage("指纹匹配中...");
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setProgress(0);
			dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();

			DownImgAsyncTask downImgTask = new DownImgAsyncTask();
			downImgTask.execute();
		}
		waterFall = (WaterFall) findViewById(R.id.waterfall);
	}

	/** Handle click events */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backbtn:
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;

		default:
			break;
		}
	}

	private class DownImgAsyncTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute() called");
			super.onPreExecute();

		}

		@Override
		protected String doInBackground(Void... params) {
			Log.i(TAG, "doInBackground(Params... params) called");
			String result = null;
			if (type == 2) {
				result = HttpUtil.getURLResponse(SERVER_URL + "verify.php");
				File file = new File(LocalManager.LOCAL_CACHE + "result.txt");
				FileOutputStream outStream;
				try {
					outStream = new FileOutputStream(file);
					try {
						outStream.write(result.getBytes());
						outStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				File file = new File(LocalManager.LOCAL_CACHE + "result.txt");
				try {
					InputStreamReader isr = null;
					isr = new InputStreamReader(new FileInputStream(file),
							"UTF-8");
					BufferedReader br = new BufferedReader(isr);
					String str = "";
					String mimeTypeLine = null;
					while ((mimeTypeLine = br.readLine()) != null) {
						str = str + mimeTypeLine;
					}
					result = str;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Pattern pattern = Pattern.compile("\\d+\\D\\d+");
			Matcher matcher = pattern.matcher(result);
			scorelist = new ArrayList<String>();
			imgidlist = new ArrayList<String>();
			while (matcher.find()) {
				String str = matcher.group();
				int gap = str.indexOf(":");
				scorelist.add(str.substring(gap + 1, str.length()));
				imgidlist.add(str.substring(0, gap));
			}
			// dialog.setMessage("获取资料中...");
			for (int i = 1; i <= RESULTNUM; i++) {
				File tempfile = new File(LocalManager.LOCAL_AVATAR_CACHE
								+ imgidlist.get(i - 1) + ".jpg");
				if (!tempfile.exists()) {
					Bitmap bm = HttpUtil.getImageBitmap(SERVER_AVATAR_URL
							+ imgidlist.get(i - 1) + ".jpg");
					LocalManager.saveAvatarBitmap(bm, imgidlist.get(i - 1)+ ".jpg");
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i(TAG, "onPostExecute(Result result) called");
			waterFall.setup(scorelist, imgidlist);
			dialog.dismiss();
		}

	}

	
}
