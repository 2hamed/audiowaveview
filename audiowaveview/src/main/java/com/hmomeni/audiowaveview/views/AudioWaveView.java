package com.hmomeni.audiowaveview.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hamed on 10/24/16.in AudioWaveViewApp
 */

public class AudioWaveView extends View {
	Paint barPaint, bgPaint;
	int mWidth, mHeight;
	int barWidth = 20;
	int barCount;

	public AudioWaveView(Context context) {
		super(context);
		init();
	}

	public AudioWaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AudioWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public AudioWaveView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		barPaint.setColor(Color.parseColor("#7CC965"));

		bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgPaint.setColor(Color.parseColor("#D7E9A2"));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(0, 0, mWidth, mHeight, bgPaint);
		for (int i = 0; i < barCount; i++) {
			canvas.drawRect(i * barWidth, 10, ((i + 1) * barWidth) - 5, mHeight, barPaint);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		barCount = mWidth / barWidth;
	}
}
