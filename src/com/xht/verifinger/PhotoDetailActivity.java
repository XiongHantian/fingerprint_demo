package com.xht.verifinger;

import java.io.InputStream;

import com.xht.util.HttpUtil;
import com.xht.util.LocalManager;
import com.xht.util.PointUtil;
import com.xht.view.dragimage.DragImageView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class PhotoDetailActivity extends Activity implements OnClickListener {
	private static final String TAG = "PhotoDetailActivity";
	private int window_width, window_height;// 控件宽度
	private DragImageView dragImageView;// 自定义控件
	private int state_height;// 状态栏的高度

	private ViewTreeObserver viewTreeObserver;

	public boolean isPoint = false; // 是否显示特征点
	private int mImageType;
	private Button mbackBtn;
	private Button msaveBtn;
	private Button moriBtn;
	private Button mpoiBtn;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_photodetail);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		mImageType = bundle.getInt("mImageType");

		/** 获取可区域高度 **/
		WindowManager manager = getWindowManager();
		window_width = manager.getDefaultDisplay().getWidth();
		window_height = manager.getDefaultDisplay().getHeight();

		dragImageView = (DragImageView) findViewById(R.id.dragimage_view);
		mbackBtn = (Button) findViewById(R.id.title_back);
		msaveBtn = (Button) findViewById(R.id.title_save);
		moriBtn = (Button) findViewById(R.id.detail_original_btn);
		mpoiBtn = (Button) findViewById(R.id.detail_point_btn);
		mbackBtn.setOnClickListener(this);
		msaveBtn.setOnClickListener(this);
		moriBtn.setOnClickListener(this);
		mpoiBtn.setOnClickListener(this);

		// 设置图片
		Bitmap ori_bitmap = BitmapFactory.decodeFile(LocalManager.LOCAL_CACHE
				+ LocalManager.LOCAL_ORI_FINGER);
		Bitmap fea_bitmap = BitmapFactory.decodeFile(LocalManager.LOCAL_CACHE
				+ LocalManager.LOCAL_FEA_FINGER);
		dragImageView.initView(ori_bitmap, fea_bitmap, mImageType);
		dragImageView.setmActivity(this);// 注入Activity.
		/** 测量状态栏高度 **/
		viewTreeObserver = dragImageView.getViewTreeObserver();
		viewTreeObserver
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						if (state_height == 0) {
							// 获取状况栏高度
							Rect frame = new Rect();
							getWindow().getDecorView()
									.getWindowVisibleDisplayFrame(frame);
							state_height = frame.top;
							dragImageView.setScreen_H(window_height
									- state_height);
							dragImageView.setScreen_W(window_width);
						}

					}
				});
	}

	void switchOriBtn() {
		if (mImageType == 1 || mImageType == 2) {
			moriBtn.setText("显示增强图");
		} else {
			moriBtn.setText("显示原始图");
		}
	}
	
	void switchPoiBtn() {
		if (mImageType == 1 || mImageType == 3) {
			mpoiBtn.setText("显示特征点");
		} else {
			mpoiBtn.setText("隐藏特征点");
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.title_back:
			this.finish();
			break;
		case R.id.title_save:
			savePointToLocal();
			UploadImgAsyncTask uploadtask = new UploadImgAsyncTask();
			uploadtask.execute(HttpUtil.SERVER_SAVEPOINT);
			break;
		case R.id.detail_original_btn:
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
			switchOriBtn();
			dragImageView.updateShowImage(mImageType);
			break;
		case R.id.detail_point_btn:
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
			switchPoiBtn();
			dragImageView.updateShowImage(mImageType);
			break;
		default:
			break;
		}
	}
	
	void savePointToLocal()
	{
		String result = PointUtil.getPointStr(dragImageView.Pointlist);
		LocalManager.saveLocalStr(result, PointUtil.CURRENT_POINT);
	}
	
	private class UploadImgAsyncTask extends AsyncTask<String, Integer, String> {
		private ProgressDialog dialog = null;

		// onPreExecute方法用于在执行后台任务前做一些UI操作
		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute() called");
			dialog = new ProgressDialog(PhotoDetailActivity.this);
			dialog.setMessage("正在保存...");
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
					+ PointUtil.CURRENT_POINT);
		}

		// onPostExecute方法用于在执行完后台任务后更新UI,显示结果
		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "onPostExecute(Result result) called");
			dialog.dismiss();
			Log.i(TAG, result);
			if (result.substring(0, 2).equals("OK")) {
				Log.i(TAG, "结束");
				MainActivity.isUpdate2 = true;
				PhotoDetailActivity.this.finish();
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"上传失败！请检查网络是否正常。", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
	}

}
