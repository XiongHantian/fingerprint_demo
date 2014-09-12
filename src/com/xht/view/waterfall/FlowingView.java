package com.xht.view.waterfall;

import java.io.IOException;
import java.io.InputStream;

import com.xht.verifinger.ResultDetailActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * �ٲ����������ĵ�Ԫ
 * 
 * @author carrey
 * 
 */
public class FlowingView extends View implements View.OnClickListener,
		View.OnLongClickListener {

	/** ��Ԫ�ı��,�������ٲ�������Ψһ��,����������ʶ��� */
	private int index;
	/** �����ƥ����� */
	private String score;
	/** �����ȫ��id */
	private String uid;

	/** ��Ԫ��Ҫ��ʾ��ͼƬBitmap */
	private Bitmap imageBmp;
	/** ͼ���ļ���·�� */
	private String imageFilePath;
	/** ��Ԫ�Ŀ��,Ҳ��ͼ��Ŀ�� */
	private int width;
	/** ��Ԫ�ĸ߶�,Ҳ��ͼ��ĸ߶� */
	private int height;

	/** ���� */
	private Paint paint;
	/** ͼ��������� */
	private Rect rect;

	/** �����Ԫ�ĵײ����������еĶ���֮��ľ��� */
	private int footHeight;

	public FlowingView(Context context, int index, int width, int height) {
		super(context);
		this.index = index;
		this.width = width;
		this.height = height;
		init();
	}

	/**
	 * ������ʼ������
	 */
	private void init() {
		setOnClickListener(this);
		setOnLongClickListener(this);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(40);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// ����ͼ��
		canvas.drawColor(Color.WHITE);
		if (imageBmp != null && rect != null) {
			canvas.drawBitmap(imageBmp, null, rect, paint);
			canvas.drawText("������" + index, 10, 40, paint);
			canvas.drawText("�÷֣�" + score, 10, 80, paint);
		}
		super.onDraw(canvas);
	}

	/**
	 * ��WaterFall�����첽����ͼƬ����
	 */
	public void loadImage() {
		imageBmp = BitmapFactory.decodeFile(imageFilePath);
		if (imageBmp != null) {
			int bmpWidth = imageBmp.getWidth();
			int bmpHeight = imageBmp.getHeight();
			// height = (int) (bmpHeight * width / bmpWidth);
			if (bmpWidth * height > bmpHeight * width) {
				int scaleh = (int) (bmpHeight * width / bmpWidth);
				int starty = (height - scaleh) / 2;
				rect = new Rect(0, starty, width, scaleh);
			} else {
				int scalew = (int) (bmpWidth * height / bmpHeight);
				int startx = (width - scalew) / 2;
				rect = new Rect(startx, 0, scalew, height);
			}
		}
	}

	/**
	 * ���¼��ػ����˵�Bitmap
	 */
	public void reload() {
		if (imageBmp == null) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					InputStream inStream = null;
					try {
						inStream = getContext().getAssets().open(imageFilePath);
						imageBmp = BitmapFactory.decodeStream(inStream);
						inStream.close();
						inStream = null;
						postInvalidate();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	/**
	 * ��ֹOOM���л���
	 */
	public void recycle() {
		if (imageBmp == null || imageBmp.isRecycled())
			return;
		new Thread(new Runnable() {

			@Override
			public void run() {
				imageBmp.recycle();
				imageBmp = null;
				postInvalidate();
			}
		}).start();
	}

	@Override
	public boolean onLongClick(View v) {
		Toast.makeText(getContext(), "long click : " + index,
				Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(getContext(), ResultDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putCharSequence("uid", uid);
		bundle.putCharSequence("score", score);
		intent.putExtras(bundle);
		getContext().startActivity(intent);
	}

	/**
	 * ��ȡ��Ԫ�ĸ߶�
	 * 
	 * @return
	 */
	public int getViewHeight() {
		return height;
	}

	/**
	 * ����ͼƬ·��
	 * 
	 * @param imageFilePath
	 */
	public void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}

	public Bitmap getImageBmp() {
		return imageBmp;
	}

	public void setImageBmp(Bitmap imageBmp) {
		this.imageBmp = imageBmp;
	}

	public int getFootHeight() {
		return footHeight;
	}

	public void setFootHeight(int footHeight) {
		this.footHeight = footHeight;
	}

	public void setScoreAndUID(String score, String uid) {
		this.score = score;
		this.uid = uid;
	}
}
