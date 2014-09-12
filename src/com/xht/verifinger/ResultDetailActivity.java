package com.xht.verifinger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.xht.util.HttpUtil;
import com.xht.util.LocalManager;
import com.xht.util.PointUtil;

public class ResultDetailActivity extends Activity {
	// Data
	private static final String TAG = "ResultDetailActivity";
	private static final String SERVER_URL = "http://finger.nat123.net/finger/";

	// variable
	private String score;
	private String uid;
	private String sex;
	private String name;
	private String pointstr;
	private Bitmap curbmp;

	// View
	private ImageView AvatarView;
	private ImageView OriginalView;
	private ImageView ResultView;
	private TextView NameTextView;
	private TextView SexTextView;
	private TextView ScoreTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resultdetail);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		score = bundle.getString("score");
		uid = bundle.getString("uid");

		AvatarView = (ImageView) findViewById(R.id.avatar_imageView);
		NameTextView = (TextView) findViewById(R.id.name_textView);
		SexTextView = (TextView) findViewById(R.id.sex_textView);
		ScoreTextView = (TextView) findViewById(R.id.score_textView);
		OriginalView = (ImageView) findViewById(R.id.original_imageView);
		ResultView = (ImageView) findViewById(R.id.result_imageView);

		HttpPostTask task = new HttpPostTask();
		task.execute(uid);
		DownImgAsyncTask down = new DownImgAsyncTask();
		down.execute();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 加载头像和指纹
		Bitmap bm = BitmapFactory.decodeFile(LocalManager.LOCAL_AVATAR_CACHE
				+ uid + ".jpg");
		AvatarView.setImageBitmap(bm);
		curbmp = BitmapFactory.decodeFile(LocalManager.LOCAL_CACHE + "feature.jpg");
		OriginalView.setImageBitmap(curbmp);
		Log.i(TAG, "OnStart");
	};

	/** Handle click events */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backbtn:
			finish();
			break;

		default:
			break;
		}
	}

	public String HttpPost(String str) {
		String result = null;
		HttpPost httpRequest = null;
		List<NameValuePair> params = null;
		HttpResponse httpResponse;
		String action = SERVER_URL + "info.php";
		httpRequest = new HttpPost(action);
		/* Post运作传送变数必须用NameValuePair[]阵列储存 */
		params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", str));
		try {
			// 发出HTTP request
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			// 取得HTTP response
			httpResponse = new DefaultHttpClient().execute(httpRequest);
			// 若状态码为200
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 取出回应字串
				result = EntityUtils.toString(httpResponse.getEntity());
			} else {
				result = "Error Response"
						+ httpResponse.getStatusLine().toString();

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = e.getMessage().toString();
		}
		return result;
	}

	private class HttpPostTask extends AsyncTask<String, Integer, String> {
		// onPreExecute方法用于在执行后台任务前做一些UI操作
		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute() called");
		}

		// doInBackground方法内部执行后台任务,不可在此方法内修改UI
		@Override
		protected String doInBackground(String... params) {
			Log.i(TAG, "doInBackground(Params... params) called" + params[0]);
			return HttpPost(params[0]);
		}

		// onProgressUpdate方法用于更新进度信息
		@Override
		protected void onProgressUpdate(Integer... progresses) {
			Log.i(TAG, "onProgressUpdate(Progress... progresses) called");
		}

		// onPostExecute方法用于在执行完后台任务后更新UI,显示结果
		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "onPostExecute(Result result) called");
			try {

				JSONObject json = new JSONObject(result);
				name = json.getString("name");
				sex = json.getString("sex");
				pointstr = json.getString("point");
				NameTextView.setText("姓名：" + name);
				SexTextView.setText("性别：" + sex);
				ScoreTextView.setText("得分：" + score);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		// onCancelled方法用于在取消执行中的任务时更改UI
		@Override
		protected void onCancelled() {
			Log.i(TAG, "onCancelled() called");
		}
	}

	private class DownImgAsyncTask extends AsyncTask<String, Void, Bitmap> {

		private String matchStr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Bitmap doInBackground(String... params) {
			matchStr = HttpUtil.getURLResponse(HttpUtil.SERVER_URL
					+ "match.php");
			Bitmap b = HttpUtil.getImageBitmap(SERVER_URL + "normalized/" + "t"
					+ uid + ".bmp");
			return b;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (result != null) {
				List<String> matchlist0 = PointUtil
						.matchPointArray(matchStr, 0);
				List<String> matchlist1 = PointUtil
						.matchPointArray(matchStr, 1);
				String oripointStr = LocalManager
						.getLocalStr(PointUtil.CURRENT_POINT);
				List<String> curlist = PointUtil.getPointArray(oripointStr);
				List<String> matlist = PointUtil.getPointArray(pointstr);
				Bitmap bmp0 = PointUtil.AddPointToMatchBitmap(curlist, matchlist0, curbmp);
				Bitmap bmp1 = PointUtil.AddPointToMatchBitmap(matlist, matchlist1, result);
				OriginalView.setImageBitmap(bmp0);
				ResultView.setImageBitmap(bmp1);
			}
		}

	}
}
