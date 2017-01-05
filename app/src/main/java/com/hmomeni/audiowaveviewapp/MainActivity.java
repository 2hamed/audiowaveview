package com.hmomeni.audiowaveviewapp;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.hmomeni.audiowaveview.views.AudioWaveView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
	AudioWaveView audioWaveView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_main);
		copyAssets();
		audioWaveView = (AudioWaveView) findViewById(R.id.audioWaveView);
		audioWaveView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				audioWaveView.setAudioFilePath(new File(getFilesDir(), "1.mp3"));
			}
		});
	}

	private void copyAssets() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		if (files != null) {
			for (String filename : files) {
				InputStream in = null;
				OutputStream out = null;
				try {
					in = assetManager.open(filename);
					File outFile = new File(getFilesDir(), filename);
					out = new FileOutputStream(outFile);
					copyFile(in, out);
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// NOOP
						}
					}
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							// NOOP
						}
					}
				}
			}
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
}
