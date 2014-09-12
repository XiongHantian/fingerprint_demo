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
 * 瀑布流中流动的单元
 * 
 * @author carrey
 * 
 */
public class FlowingView extends View implements View.OnClickListener,
		View.OnLongClickListener {

	/** 单元的编号,在整个瀑布流中是唯一的,可以用来标识身份 */
	private int index;
	/** 该身份匹配分数 */
	private String score;
	/** 该身份全局id */
	private String uid;

	/** 单元中要显示的图片Bitmap */
	private Bitmap imageBmp;
	/** 图像文件的路径 */
	private String imageFilePath;
	/** 单元的宽度,也是图像的宽度 */
	private int width;
	/** 单元的高度,也是图像的高度 */
	private int height;

	/** 画笔 */
	private Paint paint;
	/** 图像绘制区域 */
	private Rect rect;

	/** 这个单元的底部到它所在列的顶部之间的距离 */
	private int footHeight;

	public FlowingView(Context context, int index, int width, int height) {
		super(context);
		this.index = index;
		this.width = width;
		this.height = height;
		init();
	}

	/**
	 * 基本初始化工作
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
		// 绘制图像
		canvas.drawColor(Color.WHITE);
		if (imageBmp != null && rect != null) {
			canvas.drawBitmap(imageBmp, null, rect, paint);
			canvas.drawText("排名：" + index, 10, 40, paint);
			canvas.drawText("得分：" + score, 10, 80, paint);
		}
		super.onDraw(canvas);
	}

	/**
	 * 被WaterFall调用异步加载图片数据
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
	 * 重新加载回收了的Bitmap
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
	 * 防止OOM进行回收
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
	 * 获取单元的高度
	 * 
	 * @return
	 */
	public int getViewHeight() {
		return height;
	}

	/**
	 * 设置图片路径
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
