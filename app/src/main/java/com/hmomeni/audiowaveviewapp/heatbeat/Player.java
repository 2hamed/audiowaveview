package com.hmomeni.audiowaveviewapp.heatbeat;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hmomeni.audiowaveviewapp.R;

import java.util.ArrayList;

public class Player extends Fragment implements OnClickListener {
	SeekBar seek_bar;
	Button play_button, pause_button;
	MediaPlayer player = new MediaPlayer();
	TextView text_shown;
	Handler seekHandler = new Handler();
	String address;
	Button browse, btnEqu;
	LinearLayout equWrapper;
	View rootView;
	private Equalizer eq;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.player_layout, container, false);

		equWrapper = (LinearLayout) rootView.findViewById(R.id.equWrapper);

		browse = (Button) rootView.findViewById(R.id.browse);
		seek_bar = (SeekBar) rootView.findViewById(R.id.seek_bar);
		play_button = (Button) rootView.findViewById(R.id.play_button);
		pause_button = (Button) rootView.findViewById(R.id.pause_button);
		btnEqu = (Button) rootView.findViewById(R.id.btnEqualizer);
		text_shown = (TextView) rootView.findViewById(R.id.text_shown);
		browse.setOnClickListener(this);
		play_button.setOnClickListener(this);
		pause_button.setOnClickListener(this);

		btnEqu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (equWrapper.getVisibility() == View.GONE) {
					equWrapper.setVisibility(View.VISIBLE);
				} else {
					equWrapper.setVisibility(View.GONE);
				}
			}
		});

		return rootView;

	}

	public void getInit(String address) {
		Uri uri = Uri.parse(address);
		player = MediaPlayer.create(getActivity(), uri);
		seek_bar.setMax(player.getDuration());
		player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				seek_bar.setProgress(0);
				player.reset();
				text_shown.setText("finished");
				clearEqulizer();
			}
		});
		initEqualizer();

	}

	Runnable run = new Runnable() {

		@Override
		public void run() {
			seekUpdation();
		}
	};

	public void seekUpdation() {

		seek_bar.setProgress(player.getCurrentPosition());
		seekHandler.postDelayed(run, 1000);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.play_button:
				if (player == null) {
					text_shown.setText("Browse for a file first...");
					break;
				}
				text_shown.setText("Playing...");
				player.start();
				break;
			case R.id.pause_button:
				if (player == null) {
					text_shown.setText("Browse for a file first...");
					break;
				}
				player.pause();
				text_shown.setText("Paused...");
				break;
			case R.id.browse:
				seek_bar.setProgress(0);
				Intent intent = new Intent(getActivity(), FilePickerActivity.class);
				intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, Environment.getExternalStorageDirectory() + "");
				ArrayList<String> extensions = new ArrayList<String>();
				extensions.add(".wav");
				intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS, extensions);
				startActivityForResult(intent, 1);
				break;
		}

	}


	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case 1:
					if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
						// Get the file path
						address = data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH);
						getInit(address);
						seekUpdation();
					}
			}
		}
	}

	private void initEqualizer() {
		// Adding the equalizer parts
		eq = new Equalizer(9999, player.getAudioSessionId());
		int bandsNum = eq.getNumberOfBands();
		final short blr[] = eq.getBandLevelRange();
		for (short i = 0; i < bandsNum; i++) {
			final int[] range = eq.getBandFreqRange(i);
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
					eq.setBandLevel(eq.getBand(range[0] + 100), (short) bandLevel);
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
}