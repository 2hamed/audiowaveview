package com.hmomeni.audiowaveviewapp.heatbeat;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.Equalizer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hmomeni.audiowaveviewapp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main extends Fragment implements View.OnClickListener {

	private static final int REQUEST_PICK_FILE = 1;
	private static final int RECORDER_BPP = 16;
	//file
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "HeartBeatAnalyser";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	public boolean FROM_FILE_FLAG = false;
	//for recording audio
	ProcessSound ps = new ProcessSound();
	int minBufferSize = AudioRecord.getMinBufferSize(16000
			, AudioFormat.CHANNEL_IN_MONO
			, AudioFormat.ENCODING_PCM_16BIT);
	AudioRecord audioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT
			, 16000, AudioFormat.CHANNEL_IN_MONO
			, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
	int minOutBufferSize = AudioTrack.getMinBufferSize(16000,
			AudioFormat.CHANNEL_OUT_MONO,
			AudioFormat.ENCODING_PCM_16BIT);
	AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
			AudioFormat.CHANNEL_OUT_MONO,
			AudioFormat.ENCODING_PCM_16BIT, minOutBufferSize,
			AudioTrack.MODE_STREAM);
	////////////////////////////
	short[][] samples = new short[800][400]; //for reading buffer and storing data
	byte[] recorded = new byte[44];
	int currentWriteIndex, currentReadIndex, tempWr;
	boolean writeCycle, readCycle;
	String filename;
	////////////////////////////
	FileOutputStream os;
	boolean started = false;
	boolean recording = false;
	//views and stuff
	ImageView imageView;
	ImageView imageView2;
	ImageView imageView3;
	ImageView imageView4;
	ImageView imageView5;
	ImageView imageView6;
	ImageView imageView7;
	ImageView imageView8;
	ImageView imageView9;

	//////////
	Bitmap bmp;
	int gain = 1;
	Equalizer mEqualizer;
	int heartbeats;
	int sampleRate = 16000;
	int hBCycle = 0;
	//
	Spinner spinner;
	//////////////
	File selectedFile = null;
	FileInputStream fileInputStream;
	// animations
	long duration = 2000;
	ObjectAnimator objectAnimatorM1;
	ObjectAnimator objectAnimatorM2;
	LinearLayout equWrapper;
	ImageView mask1 = null;
	ImageView mask2 = null;
	//
	View rootView;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {


		rootView = inflater.inflate(R.layout.activity_main, container, false);
		equWrapper = (LinearLayout) rootView.findViewById(R.id.equalizer);
		if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.layout, new PlaceholderFragment())
//                    .commit();
		}

		//start of activity here


		Display display = getActivity().getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);

		float density = getResources().getDisplayMetrics().density;
		float dpWidth = outMetrics.widthPixels / density;


		SeekBar sb = (SeekBar) rootView.findViewById(R.id.Gain);

		FrameLayout f = (FrameLayout) rootView.findViewById(R.id.layout);
		f.setOnClickListener(this);

		//visualizer container

		float x[] = {0, 100, 100, 100,
				100, 100, 100, 95,
				100, 95, 105, 100,
				105, 100, 110, 103,
				110, 103, 120, 0,
				120, 0, 140, 200,
				140, 200, 145, 100,
				145, 100, 160, 100,
				160, 100, 163, 97,
				163, 97, 165, 100,
				165, 100, 170, 50,
				170, 50, 175, 100,
				175, 100, 177, 97,
				177, 97, 180, 100,
				180, 100, 270, 100};

		final ImageView a = (ImageView) rootView.findViewById(R.id.pulse);

		Bitmap bmp = Bitmap.createBitmap(270, 200, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		Paint p = new Paint();

		p.setColor(Color.YELLOW);
		p.setStrokeWidth(3);
		p.setAntiAlias(true);
		p.setDither(true);
		p.setStyle(Paint.Style.STROKE);
		c.drawLines(x, p);

		a.setImageBitmap(bmp);

		a.invalidate();


		mask1 = (ImageView) rootView.findViewById(R.id.mask1);
		mask2 = (ImageView) rootView.findViewById(R.id.mask2);

		Bitmap maskBmp = Bitmap.createBitmap(800, 200, Bitmap.Config.ARGB_8888);
		mask1.setImageBitmap(maskBmp);
		mask2.setImageBitmap(maskBmp);

		mask1.invalidate();
		mask2.invalidate();

		mask1.setX(0);
		mask2.setX(-800 - 50);

		Log.e("width", dpWidth + "");
		objectAnimatorM1 = ObjectAnimator.ofFloat(mask1, "x", 800);
		objectAnimatorM2 = ObjectAnimator.ofFloat(mask2, "x", -50);

		objectAnimatorM1.setDuration(duration);
		objectAnimatorM2.setDuration(duration);

		objectAnimatorM1.setRepeatCount(ObjectAnimator.INFINITE);
		objectAnimatorM2.setRepeatCount(ObjectAnimator.INFINITE);

		objectAnimatorM1.start();
		objectAnimatorM2.start();


		//end of visualizer container
		//CHECK BOX CLICK HANDLER
		CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.fromfile);
		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
				FROM_FILE_FLAG = checked;
				if (checked) {
//                    makeToast("???? ???? ??? ?? ?????? ????");
					TextView tv = (TextView) rootView.findViewById(R.id.gainNum);
					tv.setVisibility(View.INVISIBLE);
					tv = (TextView) rootView.findViewById(R.id.gain);
					tv.setVisibility(View.INVISIBLE);
					SeekBar sb = (SeekBar) rootView.findViewById(R.id.Gain);
					sb.setVisibility(View.INVISIBLE);
					Button eq = (Button) rootView.findViewById(R.id.eq);
					eq.setVisibility(View.INVISIBLE);

					Button btn = (Button) rootView.findViewById(R.id.browse);
					btn.setVisibility(View.VISIBLE);
					TextView textView = (TextView) rootView.findViewById(R.id.fileLabel);
					textView.setVisibility(View.VISIBLE);
					Spinner sp = (Spinner) rootView.findViewById(R.id.sample);
					sp.setVisibility(View.VISIBLE);
				} else {
//                    makeToast("???? ?????? ?? ????");
					TextView tv = (TextView) rootView.findViewById(R.id.gainNum);
					tv.setVisibility(View.VISIBLE);
					tv = (TextView) rootView.findViewById(R.id.gain);
					tv.setVisibility(View.VISIBLE);
					SeekBar sb = (SeekBar) rootView.findViewById(R.id.Gain);
					sb.setVisibility(View.VISIBLE);
					Button eq = (Button) rootView.findViewById(R.id.eq);
					eq.setVisibility(View.VISIBLE);

					Button btn = (Button) rootView.findViewById(R.id.browse);
					btn.setVisibility(View.INVISIBLE);
					TextView textView = (TextView) rootView.findViewById(R.id.fileLabel);
					textView.setVisibility(View.INVISIBLE);
					Spinner sp = (Spinner) rootView.findViewById(R.id.sample);
					sp.setVisibility(View.INVISIBLE);
				}
			}
		});
		//***************************

		sb.setProgress(0);
		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
				gain = (progress) + 1;
				TextView tv = (TextView) rootView.findViewById(R.id.gainNum);
				tv.setText((progress) + " dB");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
		return rootView;

		///////////////////////
	}

	private void initEqualizer() {
		// Adding the equalizer parts
		//mEqualizer = new Equalizer(9999, audioTrack.getAudioSessionId());
		int bandsNum = mEqualizer.getNumberOfBands();
		final short blr[] = mEqualizer.getBandLevelRange();
		for (short i = 0; i < bandsNum; i++) {
			final int[] range = mEqualizer.getBandFreqRange(i);
			final TextView tv = new TextView(getActivity());
			tv.setText("from:" + range[0] + " to: " + range[1]);
			equWrapper.addView(tv);
			SeekBar sb = new SeekBar(getActivity());
			sb.setMax(blr[1] / 5);
			sb.setProgress(blr[1] / 10);
			equWrapper.addView(sb);
			sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
					int bandLevel = ((i - (blr[1] / 10)) * 10);
					tv.setText("from:" + range[0] + " to: " + range[1] + " --> " + bandLevel);
					mEqualizer.setBandLevel(mEqualizer.getBand(range[0] + 100), (short) bandLevel);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {

				}
			});
		}
		// equalizer
	}

	private void clearEqulizer() {
		equWrapper.removeAllViewsInLayout();
	}

	//process sound class

	public void recordAudio() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int currentRecIndex = 0;
				while (recording) {
					while (currentRecIndex >= currentWriteIndex) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					byte[] buffer = ShortToByte(samples[currentRecIndex]);
					try {
						os.write(buffer);
					} catch (IOException e) {
						e.printStackTrace();
					}
					currentRecIndex++;

				}
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				copyWaveFile(getTempFilename(true), getFilename());
				deleteTempFile();
			}
		}).start();


	}

	////////////////////

	//rest of functions here

	public void makeToast(String msg) {
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}

	public void getByteArray() {
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(selectedFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		fileInputStream = fin;
	}

	public void setWavHeader(FileOutputStream out,
	                         long totalAudioLen,
	                         long totalDataLen,
	                         long longSampleRate,
	                         int channels,
	                         long byteRate) {
		recorded[0] = 'R';  // RIFF/WAVE header
		recorded[1] = 'I';
		recorded[2] = 'F';
		recorded[3] = 'F';
		recorded[4] = (byte) (totalDataLen & 0xff);
		recorded[5] = (byte) ((totalDataLen >> 8) & 0xff);
		recorded[6] = (byte) ((totalDataLen >> 16) & 0xff);
		recorded[7] = (byte) ((totalDataLen >> 24) & 0xff);
		recorded[8] = 'W';
		recorded[9] = 'A';
		recorded[10] = 'V';
		recorded[11] = 'E';
		recorded[12] = 'f';  // 'fmt ' chunk
		recorded[13] = 'm';
		recorded[14] = 't';
		recorded[15] = ' ';
		recorded[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		recorded[17] = 0;
		recorded[18] = 0;
		recorded[19] = 0;
		recorded[20] = 1;  // format = 1
		recorded[21] = 0;
		recorded[22] = (byte) channels;
		recorded[23] = 0;
		recorded[24] = (byte) (longSampleRate & 0xff);
		recorded[25] = (byte) ((longSampleRate >> 8) & 0xff);
		recorded[26] = (byte) ((longSampleRate >> 16) & 0xff);
		recorded[27] = (byte) ((longSampleRate >> 24) & 0xff);
		recorded[28] = (byte) (byteRate & 0xff);
		recorded[29] = (byte) ((byteRate >> 8) & 0xff);
		recorded[30] = (byte) ((byteRate >> 16) & 0xff);
		recorded[31] = (byte) ((byteRate >> 24) & 0xff);
		recorded[32] = (byte) (channels * 16 / 8);  // block align
		recorded[33] = 0;
		recorded[34] = RECORDER_BPP;  // bits per sample
		recorded[35] = 0;
		recorded[36] = 'd';
		recorded[37] = 'a';
		recorded[38] = 't';
		recorded[39] = 'a';
		recorded[40] = (byte) (totalAudioLen & 0xff);
		recorded[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		recorded[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		recorded[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		try {
			out.write(recorded, 0, 44);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	byte[] ShortToByte(short[] input) {
		int short_index, byte_index;
		int iterations = input.length; //input.length;
		byte[] buffer = new byte[iterations * 2];

		short_index = byte_index = 0;

		for (/*NOP*/; short_index != iterations; /*NOP*/) {
			buffer[byte_index] = (byte) (input[short_index] & 0x00FF);
			buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);

			++short_index;
			byte_index += 2;
		}

		return buffer;
	}

	private String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private String getTempFilename(boolean x) {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

		if (tempFile.exists() && !x)
			tempFile.delete();

		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}

	private void deleteTempFile() {
		File file = new File(getTempFilename(true));

		file.delete();
	}

	private void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = 16000;
		int channels = 1;
		long byteRate = RECORDER_BPP * 8000 * channels / 8;

		byte[] data = new byte[400];

		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;


			setWavHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);

			while (in.read(data) != -1) {
				out.write(data);
			}

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onStart() {
		super.onStart();

		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

		mEqualizer = new Equalizer(9999, audioTrack.getAudioSessionId());
		mEqualizer.setEnabled(true);
		initEqualizer();
		for (int i = 0; i < mEqualizer.getNumberOfBands(); i++) {
			int[] b = mEqualizer.getBandFreqRange((short) i);
			Log.d("hba", "number of band " + i + ": " + b[0] / 1000 + " " + b[1] / 1000);
		}

//        int[] x = mEqualizer.getBandFreqRange((short) (mEqualizer.getBand(200000)+1));
//        int[] y = mEqualizer.getBandFreqRange((short) (mEqualizer.getBand(40000)+1));
//
//        Log.e("200", x[0] + "-" + x[1]);
//        Log.e("40", y[0] + "-" + y[1]);

		Button b = (Button) rootView.findViewById(R.id.button);
		Button eq = (Button) rootView.findViewById(R.id.eq);

		b.setOnClickListener(this);
		eq.setOnClickListener(this);


		//Button normal = (Button) rootView.findViewById(R.id.normal);

		//normal.setOnClickListener(this);
		//imageviews
		imageView = (ImageView) rootView.findViewById(R.id.imageView);
		imageView2 = (ImageView) rootView.findViewById(R.id.imageView2);
		imageView3 = (ImageView) rootView.findViewById(R.id.imageView3);
		imageView4 = (ImageView) rootView.findViewById(R.id.imageView4);
		imageView5 = (ImageView) rootView.findViewById(R.id.imageView5);
		imageView6 = (ImageView) rootView.findViewById(R.id.imageView6);
		imageView7 = (ImageView) rootView.findViewById(R.id.imageView7);
		imageView8 = (ImageView) rootView.findViewById(R.id.imageView8);
		imageView9 = (ImageView) rootView.findViewById(R.id.imageView9);

		bmp = Bitmap.createBitmap(40, 200, Bitmap.Config.ARGB_8888);

		imageView.setImageBitmap(bmp);
		imageView2.setImageBitmap(bmp);
		imageView3.setImageBitmap(bmp);
		imageView4.setImageBitmap(bmp);
		imageView5.setImageBitmap(bmp);
		imageView6.setImageBitmap(bmp);
		imageView7.setImageBitmap(bmp);
		imageView8.setImageBitmap(bmp);
		imageView9.setImageBitmap(bmp);
		///////////////////////////

		//file output set
		filename = getTempFilename(false);
		/////////////////////
//        //eq cancel
//        Button eqc = (Button) rootView.findViewById(R.id.eqCancel);
//        eqc.setOnClickListener(this);

		///////////


		//spinner listener

		final Spinner spin = (Spinner) rootView.findViewById(R.id.sample);

		spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				sampleRate = Integer.parseInt(spin.getSelectedItem().toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});

		//////////////////////

		Button btn = (Button) rootView.findViewById(R.id.browse);
		btn.setOnClickListener(this);

		btn = (Button) rootView.findViewById(R.id.Rec);
		btn.setOnClickListener(this);

		//initialize saved states for equalizer and gain...


		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

		int gainPref = preferences.getInt("gain", 0);
		this.gain = (gainPref) + 1;
		TextView t1 = (TextView) rootView.findViewById(R.id.gainNum);
		t1.setText((gainPref) + " dB");


		//end of setting preferences for equalizer and gain


	}


	///////////////////////

	//on start

	@Override
	public void onClick(View view) {


		LinearLayout fl = (LinearLayout) rootView.findViewById(R.id.equalizer);
		if (fl.getVisibility() == View.VISIBLE)
			fl.setVisibility(View.GONE);

		if (view.getId() == R.id.button) {
			Button b = (Button) rootView.findViewById(R.id.button);
			if (started) {
				ps.cancel(true);
				started = false;
				audioRecorder.stop();
				audioRecorder.release();
				audioRecorder = null;
				audioTrack.stop();
				audioTrack.release();
				audioTrack.flush();
				ps = null;
				b.setText("start");

				ImageView iv = (ImageView) rootView.findViewById(R.id.imageView14);
				Bitmap bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
				iv.setImageBitmap(bmp);
				iv.invalidate();

				if (!FROM_FILE_FLAG) {
					Button btn = (Button) rootView.findViewById(R.id.Rec);
					btn.setVisibility(View.GONE);
					if (recording) {
						makeToast("صدا در پوشه HeartBeatAnalyser ذخیره شد.");
					}
					recording = false;

				}

				samples = new short[800][400];
				currentWriteIndex = 0;
				currentReadIndex = 0;


			} else {
				audioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT
						, 16000, AudioFormat.CHANNEL_IN_MONO
						, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
				audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
						AudioFormat.CHANNEL_OUT_MONO,
						AudioFormat.ENCODING_PCM_16BIT, minOutBufferSize,
						AudioTrack.MODE_STREAM);

				audioTrack.setStereoVolume(100, 100);
				audioTrack.play();

				started = true;
				ps = new ProcessSound();
				ps.execute();
				b.setText("stop");

				mEqualizer = new Equalizer(9999, audioTrack.getAudioSessionId());
				mEqualizer.setEnabled(true);

				if (!FROM_FILE_FLAG) {
					Button btn = (Button) rootView.findViewById(R.id.Rec);
					btn.setVisibility(View.VISIBLE);

				}

//                makeToast("???? ??? ? ??????");
			}
		}


		if (view.getId() == R.id.Rec) {
			if (!recording) {

				recording = true;
				Button btn = (Button) rootView.findViewById(R.id.Rec);
				btn.setText("Stop Rec");

				recordAudio();
			} else {
				recording = false;
				Button btn = (Button) rootView.findViewById(R.id.Rec);
				btn.setText("recorder");

				makeToast("صدا در پوشه HeartBeatAnalyser ذخیره شد");
			}

		}
		if (view.getId() ==
				R.id.eq) {
//            makeToast("???????? ???");
			if (fl.getVisibility() == View.VISIBLE)
				fl.setVisibility(View.GONE);
			else
				fl.setVisibility(View.VISIBLE);
		}
//        SeekBar sb1 = (SeekBar) rootView.findViewById(R.id.sB);
//        SeekBar sb2 = (SeekBar) rootView.findViewById(R.id.sB2);
//        SeekBar sb3 = (SeekBar) rootView.findViewById(R.id.sB3);
//        SeekBar sb4 = (SeekBar) rootView.findViewById(R.id.sB4);
		if (view.getId() == R.id.normal) {
//            for(int i = 0 ; i < mEqualizer.getNumberOfBands() ; i++){
//                mEqualizer.setBandLevel((short) (i), (short) 1);
//            }
			mEqualizer.release();
			mEqualizer = new Equalizer(9999, audioTrack.getAudioSessionId());
//            sb1.setProgress(10);
//            sb2.setProgress(10);
//            sb3.setProgress(10);
//            sb4.setProgress(10);
			fl.setVisibility(View.GONE);
		}


		if (view.getId() == R.id.eqCancel) {
			fl.setVisibility(View.GONE);
		}
		if (view.getId() == R.id.browse) {
			Intent intent = new Intent(getActivity(), FilePickerActivity.class);

			// Set the initial directory to be the sdcard
			intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, Environment.getExternalStorageDirectory() + "");

			// Show hidden files
			//intent.putExtra(FilePickerActivity.EXTRA_SHOW_HIDDEN_FILES, true);

			// Only make .png files visible
			//ArrayList<String> extensions = new ArrayList<String>();
			//extensions.add(".png");
			//intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS, extensions);

			// Start the activity
			startActivityForResult(intent, REQUEST_PICK_FILE);
		}


	}


	//////////



    /*public void onBackPressed() {
        getActivity().onBackPressed();

        try {
            audioTrack.release();
            audioRecorder.release();
            ps.cancel(true);
        } catch (Exception e) {

        }


        SeekBar sb1 = (SeekBar) rootView.findViewById(R.id.sB);
        SeekBar sb2 = (SeekBar) rootView.findViewById(R.id.sB2);
        SeekBar sb3 = (SeekBar) rootView.findViewById(R.id.sB3);
        SeekBar sb4 = (SeekBar) rootView.findViewById(R.id.sB4);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

        editor.putInt("Gain", this.gain);
        editor.putInt("sb1", sb1.getProgress());
        editor.putInt("sb2", sb2.getProgress());
        editor.putInt("sb3", sb3.getProgress());
        editor.putInt("sb4", sb4.getProgress());

        editor.commit();
    }*/

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REQUEST_PICK_FILE:
					if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
						// Get the file path
						selectedFile = new File(data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
						// Set the file path text view
						getByteArray();
						makeToast(selectedFile.getName() + " Selected");
						//Now you have your selected file, You can do your additional requirement with file.
					}
			}
		}
	}

	///////////////////////////////////

	@Override
	public void onDestroy() {
		super.onDestroy();

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

		editor.putInt("Gain", this.gain);

		editor.commit();
	}

	public void stopOperation() {

		try {
			audioTrack.release();
			audioRecorder.release();
			ps.cancel(true);
		} catch (Exception e) {

		}

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

		editor.putInt("Gain", this.gain);


		editor.apply();
	}

	public class ProcessSound extends AsyncTask<Void, short[], Void> {


		@Override
		protected Void doInBackground(Void... voids) {
			double[] castToDouble = new double[400];

			if (!FROM_FILE_FLAG) {
				//start recording
				try {
					audioRecorder.startRecording();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
				///////////////////////////////////////


				currentReadIndex = 0;

				try {
					os = new FileOutputStream(filename);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}


				if (started) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							currentWriteIndex = 0;
							writeCycle = false;
							short[] temp;


							while (started) {
								if (isCancelled())
									break;

								final int bufferReadResult = audioRecorder.read(samples[currentWriteIndex], 0, 400);

								temp = samples[currentWriteIndex].clone();
								temp = setGainOfOutput(temp);

								try {
									audioTrack.write(temp, 0, 400);
								} catch (Exception e) {
									e.printStackTrace();
								}

								if (currentWriteIndex % 165 == 0) {
									final int currwr = currentWriteIndex;
									new Thread(new Runnable() {
										@Override
										public void run() {
											int x[] = countBeats(samples, currwr, 22050);

											//Log.e("heartb" , x[1]+"");

											heartbeats = x[0];
											float sec = (x[1] / sampleRate);
											if (sec == 0)
												sec++;

											final int hb = heartbeats;

											heartbeats = (int) (heartbeats * (60.0 / sec));

											final float finalSec = sec;
											getActivity().runOnUiThread(new Runnable() {
												@Override
												public void run() {
													ImageView iv = (ImageView) rootView.findViewById(R.id.imageView14);
													Bitmap bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
													Canvas cv = new Canvas(bmp);
													Paint p = new Paint();
													p.setColor(Color.RED);
													p.setTextSize(50);
													p.setAntiAlias(true);
													p.setDither(true);
													cv.drawText(heartbeats + "", 50, 90, p);
													iv.setImageBitmap(bmp);
													iv.invalidate();

													if (hBCycle == 0) {
														objectAnimatorM1.setRepeatCount(0);
														objectAnimatorM2.setRepeatCount(0);

														objectAnimatorM1.end();
														objectAnimatorM2.end();

														objectAnimatorM1 = ObjectAnimator.ofFloat(mask1, "x", 800);
														objectAnimatorM2 = ObjectAnimator.ofFloat(mask2, "x", -50);

														mask1.setX(0);
														//noinspection ResourceType
														mask2.setX(-850);

														objectAnimatorM1.setDuration((long) ((finalSec / hb) * 1500));
														objectAnimatorM2.setDuration((long) ((finalSec / hb) * 1500));

														objectAnimatorM1.setRepeatCount(ObjectAnimator.INFINITE);
														objectAnimatorM2.setRepeatCount(ObjectAnimator.INFINITE);

														objectAnimatorM1.start();
														objectAnimatorM2.start();
													}

													if (hBCycle == 3)
														hBCycle = -1;
													hBCycle++;


												}
											});
										}
									}).start();


								}


								if (currentWriteIndex == 799) {
									currentWriteIndex = -1;
									writeCycle = !writeCycle;
								}
								currentWriteIndex++;


							}


						}
					}).start();
				}
			} else {
				//if reading from file
				new Thread(new Runnable() {
					@Override
					public void run() {
						boolean flag = true;
						currentWriteIndex = 0;

						ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						byte[] temp = new byte[0];

						try {
							fileInputStream.skip(44);
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							int length = fileInputStream.available();
							temp = new byte[length];
							int read = fileInputStream.read(temp, 0, length);
							buffer.write(temp, 0, length);
							temp = buffer.toByteArray();
							short[] samps = new short[length / 2 + 1];
							for (int i = 0, j = 0; i < samps.length && j < temp.length; i++, j += 2) { //j<2406 && i < 400
								samps[i] = temp[j];
								short x = temp[j + 1];
								samps[i] += x << 8;
							}
							int[] heart = countFileBeats(samps, sampleRate);
							heartbeats = heart[0];
							Log.e("count ", heartbeats + "");
							float sec = (length / sampleRate);
							sec /= 2;
							Log.e("sec ", sec + "");
							heartbeats /= 2;

							heartbeats = (int) (60 * heartbeats / sec);

						} catch (IOException e) {
							e.printStackTrace();
						}
// commit
//
//                        while (true) {
//
//
//
//                            if(false)
//                                break;
//                            if ((currentWriteIndex == - 1 || currentWriteIndex == 799)) {
//                                final int currwr = currentWriteIndex;
//
//                                int[] heart = countBeats(samples, currwr + 1, sampleRate);
//                                sec = (int) (heart[1] / sampleRate);
//                                Log.e("count ", heartbeats+"");
////                                            heartbeats *= 60;
////                                            if(sec == 0)
////                                                sec = 1;
////                                            heartbeats = (int) (heartbeats / sec);
//
//
//                            }
//
//
//
//
//
////                                try{
////                                    audioTrack.write(samples[currentWriteIndex], 0, 400);
////                                }catch (Exception e){
////                                    e.printStackTrace();
////                                }
//
//                            if (currentWriteIndex == 799) {
//                                currentWriteIndex = -1;
//                                flag = false;
//                            }
//                            currentWriteIndex++;
//                        }
//                        if(sec == 0)
//                            sec = 1;
//                        heartbeats = (int) (heartbeats / sec);

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								ImageView iv = (ImageView) rootView.findViewById(R.id.imageView14);
								Bitmap bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
								Canvas cv = new Canvas(bmp);
								Paint p = new Paint();
								p.setColor(Color.RED);
								p.setTextSize(50);
								cv.drawText(heartbeats + "", 50, 90, p);
								iv.setImageBitmap(bmp);
								iv.invalidate();
							}
						});

					}
				}).start();

			}


			if (!FROM_FILE_FLAG) {
				readCycle = false;
				int delay = 50;
				while (started) {
					if (isCancelled())
						break;


					//                for (short x:samples[currentReadIndex])
					//                    Log.e("samples["+currentReadIndex+"] = ",x+"");


					while (readCycle == writeCycle && currentReadIndex + 1 > currentWriteIndex) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					short max = 0;
					int index = 0;
					short[] progress = new short[1];
					for (int i = 0; i < samples[currentReadIndex].length; i++) {
						if (samples[currentReadIndex][i] > 100 && samples[currentReadIndex][i] > max) {
							max = samples[currentReadIndex][i];
							index = i;
						}
						if (i == samples[currentReadIndex].length - 1) {
							if (index != i) {
								if (max > 500) {
									progress[0] = 1;
								}
							}
						}

					}

					publishProgress(progress);

					try {
						if (readCycle == writeCycle && currentReadIndex + 1 < currentWriteIndex && delay != 0)
							delay--;
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					animateBitmaps();
					if (isCancelled())
						break;


					if (currentReadIndex == 799) {
						currentReadIndex = -1;
						readCycle = !readCycle;
					}
					currentReadIndex++;


				}
			}

			audioRecorder.stop();
			audioTrack.stop();

			return null;
		}


		@Override
		protected void onProgressUpdate(short[]... values) {
			super.onProgressUpdate(values);

			ImageView iv = (ImageView) rootView.findViewById(R.id.imageView9);

			Bitmap bmp = Bitmap.createBitmap(40, 200, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);
			Paint paint = new Paint();

			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(3);
			paint.setAntiAlias(true);
			paint.setDither(true);
//            for (int i = 0; i < 400; i++) {
//                canvas.drawLine(i, 100 - values[0][i] / 10, i + 1
//                        , 100 - values[0][i + 1] / 10, paint);
//                Log.e("val[0]["+i+"]",values[0][i]+"");
//            }

			if (values[0][0] == 1) {
				canvas.drawLine(0, 100, 10, 100, paint);
				canvas.drawLine(10, 100, 17, 0, paint);
				canvas.drawLine(17, 0, 22, 200, paint);
				canvas.drawLine(22, 200, 30, 100, paint);
				canvas.drawLine(30, 100, 40, 100, paint);
			} else {
				canvas.drawLine(0, 100, 40, 100, paint);
			}

			iv.setImageBitmap(bmp);
			iv.invalidate();

		}

		public void animateBitmaps() {
			final Bitmap bmp2 = ((BitmapDrawable) imageView2.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
			final Bitmap bmp3 = ((BitmapDrawable) imageView3.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
			final Bitmap bmp4 = ((BitmapDrawable) imageView4.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
			final Bitmap bmp5 = ((BitmapDrawable) imageView5.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
			final Bitmap bmp6 = ((BitmapDrawable) imageView6.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
			final Bitmap bmp7 = ((BitmapDrawable) imageView7.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
			final Bitmap bmp8 = ((BitmapDrawable) imageView8.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
			final Bitmap bmp9 = ((BitmapDrawable) imageView9.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);


			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					imageView.setImageBitmap(bmp2);
					imageView2.setImageBitmap(bmp3);
					imageView3.setImageBitmap(bmp4);
					imageView4.setImageBitmap(bmp5);
					imageView5.setImageBitmap(bmp6);
					imageView6.setImageBitmap(bmp7);
					imageView7.setImageBitmap(bmp8);
					imageView8.setImageBitmap(bmp9);

				}
			});
		}


		short[] setGainOfOutput(short[] output) {
			for (int i = 0; i < output.length; i++) {

				if (output[i] < 32767 / gain && output[i] > -32768) {
					output[i] *= gain;
					continue;
				} else if (output[i] > 32767 / gain) {
					output[i] = 32767;
					continue;
				} else if (output[i] < -32768 / gain) {
					output[i] = -32767;
					continue;
				}
			}

			return output;
		}


		public int[] countBeats(short[][] in, int length, int sampleRate) {
			int[] heartBeats = {0, 0};
			int max = 0, index = 0;
			int limit = (int) (sampleRate / 3.3) + 200; //maximum of 200 bps can be calculated
			for (int i = 0; i < length; i++) {
				for (int j = 0; j < in[0].length; j++) {
					if (in[i][j] < 110)
						in[i][j] = 0;

					if (in[i][j] > max) {
						max = in[i][j];
						index = i * 400 + j;

					}

					if (max > 110) {
						if (i * 400 + j > index + limit) {
							Log.e("max " + max + " index ", index + "");
							heartBeats[0]++;
							heartBeats[1] = index;
							max = 110;
							continue;
						}
					}
				}
			}
			Log.e("in f count ", heartBeats[0] + "");
			return heartBeats;
		}

		public int[] countFileBeats(short[] in, int sampleRate) {
			int[] heartBeats = {0, 0};
			int max = 0, index = 0;
			int limit = (int) (sampleRate / 3.3) + 200; //maximum of 200 bps can be calculated
			for (int i = 0; i < in.length; i++) {

				if (in[i] < 110)
					in[i] = 0;

				if (in[i] > max) {
					max = in[i];
					index = i;

				}

				if (max > 110) {
					if (i > index + limit) {
						Log.e("max " + max + " index ", index + "");
						heartBeats[0]++;
						heartBeats[1] = index;
						max = 110;
						continue;
					}
				}
			}
			return heartBeats;
		}
	}

}


