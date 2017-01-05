package com.hmomeni.audiowaveview.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.hmomeni.audiowaveview.utils.soundfile.CheapSoundFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by hamed on 10/24/16.in AudioWaveViewApp
 */

public class AudioWaveView extends View {
	private static final String TAG = "AudioWaveView";
	private Paint barPaint, bgPaint;
	private float mWidth, mHeight;
	private float barWidth = 20;
	private int barCount;
	private float min = Integer.MAX_VALUE, max = 0;

	private File audioFilePath;
	private int[] downSampledFrameGains;

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
		if (downSampledFrameGains != null) {
			for (int i = 0; i < barCount; i++) {
				float frame = downSampledFrameGains[i] / max;
				canvas.drawRect(i * barWidth, mHeight - (frame * mHeight/2), ((i + 1) * barWidth) - 5, mHeight, barPaint);
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
//		barCount = mWidth / barWidth;
	}


	public void setAudioFilePath(File audioFilePath) {
		this.audioFilePath = audioFilePath;
		processFile();
	}

	private void processFile() {
		try {
			CheapSoundFile cheapSoundFile = CheapSoundFile.create(audioFilePath.getAbsolutePath(), new CheapSoundFile.ProgressListener() {
				@Override
				public boolean reportProgress(double fractionComplete) {
					return true;
				}
			});
			int[] frameGains = cheapSoundFile.getFrameGains();
			int sourceRate = frameGains.length;
			int targetRate = 50;
			int index = 0;
			downSampledFrameGains = new int[targetRate];
			for (int i = 0; i < targetRate; i++) {
				int A = 0;
				int windowOffset = sourceRate / targetRate;
				int blockSize = 0;
				while (windowOffset > 0) {
					A += frameGains[index];
					windowOffset--;
					index++;
					blockSize++;
				}
				downSampledFrameGains[i] = A / blockSize;
			}
			barCount = targetRate;
			barWidth = mWidth / barCount;
			calculateMinAndMax();
			invalidate();

		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void calculateMinAndMax() {
		for (int i = 0; i < barCount; i++) {
			if (downSampledFrameGains[i] > max) {
				max = downSampledFrameGains[i];
			}
			if (downSampledFrameGains[i] < min) {
				min = downSampledFrameGains[i];
			}
		}
	}
}
