package com.xht.verifinger;

import java.io.FileNotFoundException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.xht.util.HttpUtil;
import com.xht.util.LocalManager;
import com.xht.util.PointUtil;

public class MainActivity extends Activity implements OnTouchListener,
		OnClickListener {

	// data
	private static final String TAG = "MainActivity";
	private static final int TAKE_BIG_PICTURE = 1;
	private static final int CROP_BIG_PICTURE = 2;
	private static final int CHOOSE_BIG_PICTURE = 3;
	private static final String IMAGE_FILE_LOCATION = "file:///sdcard/temp.jpg";
	private Uri imageUri;// to store the big bitmap
	private Bitmap bitmap;
	private Bitmap featureBitmap;
	private Bitmap PointBitmap;
	// variables
	private String featureImgUrl;
	private List<String> Pointlist;
	private boolean isUpdate1 = false;
	public static boolean isUpdate2 = false;
	private boolean isCacheO = false;
	private boolean isCache1 = false;
	private int mImageType = 0; // 0为什么都不显示，1为显示无点原图,2为显示有点原图,3为显示无点增强图，4为显示有点增强图
	// views
	private ImageView imageView;
	private Button switchOriginalBtn;
	private Button switchPointBtn;

	private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, requestCode);
	}

	private Bitmap decodeUriAsBitmap(Uri uri) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(getContentResolver()
					.openInputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}

	/** Handle touch events */
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.imageView:
			if (imageView.getDrawable() == null)
				return false;
			Intent inten1 = new Intent(MainActivity.this,
					PhotoDetailActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt("mImageType", mImageType);
			inten1.putExtras(bundle);
			startActivity(inten1);
			break;
		default:
			break;
		}

		return false;
	}

	/** Handle click events */
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.buttonTakeBigPicture:
			if (imageUri == null)
				Log.e(TAG, "image uri can't be null");
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(intent, TAKE_BIG_PICTURE);

			break;
		case R.id.buttonChooseBigPicture:
			intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 500);
			intent.putExtra("outputY", 500);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", false);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			intent.putExtra("outputFormat",
					Bitmap.CompressFormat.JPEG.toString());
			intent.putExtra("noFaceDetection", false); // no face detection
			startActivityForResult(intent, CHOOSE_BIG_PICTURE);
			break;
		case R.id.buttonFeature:
			Log.i(TAG, "get feature");
			if (isNetworkAvailable()) {
				if (isUpdate1) {
					UploadImgAsyncTask uploadtask = new UploadImgAsyncTask();
					uploadtask.execute(HttpUtil.SERVER_UPLOAD);
				} else {
					Toast toast;
					toast = Toast.makeText(getApplicationContext(),
							"请选择新指纹获取特征。", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			} else {
				setNetwork();
			}
			break;

		case R.id.buttonVerify:
			Log.i(TAG, "FingerVerify");
			if (isNetworkAvailable()) {
				if (isUpdate2) {
					MatchResults.type = 2;
					isUpdate2 = false;
				} else {
					if (imageView.getDrawable() == null) {
						Toast toast;
						toast = Toast.makeText(getApplicationContext(),
								"请选择指纹图片后进行识别。", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
						return;
					} else
						MatchResults.type = 1;
				}
				Intent inten = new Intent(MainActivity.this, MatchResults.class);
				startActivity(inten);
				overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
			} else {
				setNetwork();
			}
			break;

		case R.id.switch_original_btn:
			switch (mImageType) {
			case 1:
				mImageType = 3;
				break;
			case 2:
				mImageType = 4;
				break;
			case 3:
				mImageType = 1;
				break;
			case 4:
				mImageType = 2;
				break;
			default:
				break;
			}
			updateShowImage(mImageType);
			break;
		case R.id.switch_point_btn:
			switch (mImageType) {
			case 1:
				mImageType = 2;
				break;
			case 2:
				mImageType = 1;
				break;
			case 3:
				mImageType = 4;
				break;
			case 4:
				mImageType = 3;
				break;
			default:
				break;
			}
			updateShowImage(mImageType);
			break;
		default:
			break;
		}
	}

	private void updateShowImage(int mtype) {
		if (mtype == 0 || (mtype == 1 && isUpdate1)) {
			switchOriginalBtn.setEnabled(false);
			switchPointBtn.setEnabled(false);
		} else {
			switchOriginalBtn.setEnabled(true);
			switchPointBtn.setEnabled(true);
		}
		switch (mtype) {
		case 0:
			switchOriginalBtn.setText("显示增强图");
			switchPointBtn.setText("显示特征点");
			break;
		case 1:
			imageView.setImageBitmap(bitmap);
			switchOriginalBtn.setText("显示增强图");
			switchPointBtn.setText("显示特征点");
			break;
		case 2:
			imageView.setImageBitmap(bitmap);
			PointBitmap = PointUtil.AddPointToBitmap(Pointlist, bitmap);
			imageView.setImageBitmap(PointBitmap);
			switchOriginalBtn.setText("显示增强图");
			switchPointBtn.setText("隐藏特征点");
			break;
		case 3:
			imageView.setImageBitmap(featureBitmap);
			switchOriginalBtn.setText("显示原始图");
			switchPointBtn.setText("显示特征点");
			break;
		case 4:
			imageView.setImageBitmap(featureBitmap);
			PointBitmap = PointUtil.AddPointToBitmap(Pointlist, featureBitmap);
			imageView.setImageBitmap(PointBitmap);
			switchOriginalBtn.setText("显示原始图");
			switchPointBtn.setText("隐藏特征点");
			break;
		default:
			break;
		}
	}

	/** Activity life cycle */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// views
		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setOnTouchListener(this);
		switchOriginalBtn = (Button) findViewById(R.id.switch_original_btn);
		switchPointBtn = (Button) findViewById(R.id.switch_point_btn);
		switchOriginalBtn.setOnClickListener(this);
		switchPointBtn.setOnClickListener(this);
		LocalManager.CreateLocalDir();
		mImageType = 0;
		updateShowImage(mImageType);
		// instantiate
		imageUri = Uri.parse(IMAGE_FILE_LOCATION);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (isCacheO) {
			bitmap = BitmapFactory.decodeFile(LocalManager.LOCAL_CACHE
					+ LocalManager.LOCAL_ORI_FINGER);
			imageView.setImageBitmap(bitmap);
		}
		if (isCache1) {
			featureBitmap = BitmapFactory.decodeFile(LocalManager.LOCAL_CACHE
					+ LocalManager.LOCAL_FEA_FINGER);
			imageView.setImageBitmap(featureBitmap);
			String pointStr = LocalManager
					.getLocalStr(PointUtil.CURRENT_POINT);
			Pointlist = PointUtil.getPointArray(pointStr);
			updateShowImage(mImageType);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {// result is not correct
			Log.e(TAG, "requestCode = " + requestCode);
			Log.e(TAG, "resultCode = " + resultCode);
			Log.e(TAG, "data = " + data);
			return;
		} else {
			switch (requestCode) {
			case TAKE_BIG_PICTURE:
				Log.d(TAG, "TAKE_BIG_PICTURE: data = " + data);// it seems to be
																// null
				cropImageUri(imageUri, 780, 600, CROP_BIG_PICTURE);
				break;
			case CROP_BIG_PICTURE:// from crop_big_picture
				Log.d(TAG, "CROP_BIG_PICTURE: data = " + data);// it seems to be
																// null
				if (imageUri != null) {
					bitmap = decodeUriAsBitmap(imageUri);
					imageView.setImageBitmap(bitmap);
					LocalManager.saveBitmap(bitmap,
							LocalManager.LOCAL_ORI_FINGER);
					isUpdate1 = true;
					isCacheO = true;
					mImageType = 1;
				}
				break;
			case CHOOSE_BIG_PICTURE:
				Log.d(TAG, "CHOOSE_BIG_PICTURE: data = " + data);// it seems to
																	// be null
				if (imageUri != null) {
					bitmap = decodeUriAsBitmap(imageUri);
					imageView.setImageBitmap(bitmap);
					LocalManager.saveBitmap(bitmap,
							LocalManager.LOCAL_ORI_FINGER);
					isUpdate1 = true;
					isCacheO = true;
					mImageType = 1;
				}
				break;
			default:
				break;
			}
		}
	}

	private class DownImgAsyncTask extends AsyncTask<String, Void, Bitmap> {

		private ProgressDialog dialog = null;

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute() called");
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("正在提取图片特征...");
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setProgress(0);
			dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
			super.onPreExecute();

		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Log.i(TAG, "doInBackground(Params... params) called" + params[0]);
			Bitmap b = HttpUtil.getImageBitmap(params[0]);
			String pointStr = HttpUtil.getURLResponse(HttpUtil.SERVER_URL
					+ "getPoints.php");
			Pointlist = PointUtil.getPointArray(pointStr);
			LocalManager.saveLocalStr(pointStr, PointUtil.ORIGINAL_POINT);
			LocalManager.saveLocalStr(pointStr, PointUtil.CURRENT_POINT);
			return b;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			Log.i(TAG, "onPostExecute(Result result) called");
			if (result != null) {
				dialog.dismiss();
				featureBitmap = Bitmap.createBitmap(result);
				LocalManager.saveBitmap(featureBitmap,
						LocalManager.LOCAL_FEA_FINGER);
				mImageType = 4;
				updateShowImage(mImageType);
				isCache1 = true;
			}
		}

	}

	private class UploadImgAsyncTask extends AsyncTask<String, Integer, String> {
		private ProgressDialog dialog = null;

		// onPreExecute方法用于在执行后台任务前做一些UI操作
		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute() called");
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("正在上传...");
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setProgress(0);
			dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		// doInBackground方法内部执行后台任务,不可在此方法内修改UI
		@Override
		protected String doInBackground(String... params) {
			Log.i(TAG, "doInBackground(Params... params) called" + params[0]);
			return HttpUtil.uploadFile(params[0], LocalManager.LOCAL_CACHE
					+ LocalManager.LOCAL_ORI_FINGER);
		}

		// onPostExecute方法用于在执行完后台任务后更新UI,显示结果
		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "onPostExecute(Result result) called");
			dialog.dismiss();
			if (result.substring(0, 2).equals("OK")) {
				featureImgUrl = HttpUtil.SERVER_CACHE_URL + result.substring(3);
				isUpdate1 = false;
				isUpdate2 = true;
				DownImgAsyncTask downtask = new DownImgAsyncTask();
				downtask.execute(featureImgUrl);
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"上传失败！请检查网络是否正常。", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
	}

	// 设置网络
	public void setNetwork() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle("网络状态");
		builder.setMessage("当前网络不可用，是否设置网络?");
		builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent;
				if (android.os.Build.VERSION.SDK_INT > 10) {
					intent = new Intent(
							android.provider.Settings.ACTION_WIRELESS_SETTINGS);
				} else {
					intent = new Intent();
					ComponentName component = new ComponentName(
							"com.android.settings",
							"com.android.settings.WirelessSettings");
					intent.setComponent(component);
					intent.setAction("android.intent.action.VIEW");
				}
				startActivity(intent);
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create();
		builder.show();
	}

	// 判断网络状态
	public boolean isNetworkAvailable() {
		Context context = getApplicationContext();
		ConnectivityManager connect = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connect == null) {
			return false;
		} else// get all network info
		{
			NetworkInfo[] info = connect.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
