package com.loui.sra2022bpm_2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.SeekBar;

public class NotificationReceiver extends Activity {

    SeekBar m_Brightness = null;
    SeekBar m_Volume     = null;

    SeekBar.OnSeekBarChangeListener m_BrightnessChanged = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TryChangeBrightness(progress);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}

        private Boolean CanChangeBrightness(Context _context) {
            return Settings.System.canWrite(_context);
        }

        private void TryChangeBrightness(int _brightness) {

            try {

                Context context = getApplicationContext();

                // Check whether app has the write settings permission or not.
                if (CanChangeBrightness(context) == false) {

                    // If not then open the "can modify system settings" panel.
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else {

                    //Settings.System.putInt(getContentResolver(), // OVERRIDE AUTO BRIGHTNESS TO MANUAL
                    //        Settings.System.SCREEN_BRIGHTNESS_MODE,
                    //        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                    // Set the value of the system's brightness.
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, _brightness);

                    WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                    layoutParams.screenBrightness = _brightness / (float)255;

                    getWindow().setAttributes(layoutParams);
                }
            }
            catch(Exception e) {
                Log.println(Log.ERROR, "Serial", e.toString());
            }
        }

    };

    SeekBar.OnSeekBarChangeListener m_VolumeChanged = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            float normalised = (float)progress / 255.0f;
            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                (int)(progress * (float)audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),
                AudioManager.FLAG_PLAY_SOUND
            );
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.highbpm_options);

        m_Brightness = findViewById(R.id.brightness);
        m_Volume     = findViewById(R.id.volume);

        m_Brightness.setOnSeekBarChangeListener(m_BrightnessChanged);
            m_Volume.setOnSeekBarChangeListener(m_VolumeChanged);
    }

}
