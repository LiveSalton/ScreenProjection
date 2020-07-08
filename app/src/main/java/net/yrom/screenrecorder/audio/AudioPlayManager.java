package net.yrom.screenrecorder.audio;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

public class AudioPlayManager implements SensorEventListener {
    private static final String TAG = "LQR_AudioPlayManager";
    private MediaPlayer mediaPlayer;
    private IAudioPlayListener playListener;
    private Uri playingUri;
    private Sensor sensor;
    private SensorManager sensorManager;
    private AudioManager audioManager;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private Context context;

    public AudioPlayManager() {
    }

    public static AudioPlayManager getInstance() {
        return SingletonHolder.sInstance;
    }

    @Override
    @TargetApi(11)
    public void onSensorChanged(SensorEvent event) {
        float range = event.values[0];
        if (this.sensor != null && this.mediaPlayer != null) {
            if (this.mediaPlayer.isPlaying()) {
                if ((double) range > 0.0D) {
                    if (this.audioManager.getMode() == 0) {
                        return;
                    }

                    this.audioManager.setMode(0);
                    this.audioManager.setSpeakerphoneOn(true);
                    final int positions = this.mediaPlayer.getCurrentPosition();

                    try {
                        this.mediaPlayer.reset();
                        this.mediaPlayer.setAudioStreamType(3);
                        this.mediaPlayer.setVolume(1.0F, 1.0F);
                        this.mediaPlayer.setDataSource(this.context, this.playingUri);
                        this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            public void onPrepared(MediaPlayer mp) {
                                mp.seekTo(positions);
                            }
                        });
                        this.mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                            public void onSeekComplete(MediaPlayer mp) {
                                mp.start();
                            }
                        });
                        this.mediaPlayer.prepareAsync();
                    } catch (IOException var5) {
                        var5.printStackTrace();
                    }

                    this.setScreenOn();
                } else {
                    this.setScreenOff();
                    if (Build.VERSION.SDK_INT >= 11) {
                        if (this.audioManager.getMode() == 3) {
                            return;
                        }

                        this.audioManager.setMode(3);
                    } else {
                        if (this.audioManager.getMode() == 2) {
                            return;
                        }

                        this.audioManager.setMode(2);
                    }

                    this.audioManager.setSpeakerphoneOn(false);
                    this.replay();
                }
            } else if ((double) range > 0.0D) {
                if (this.audioManager.getMode() == 0) {
                    return;
                }

                this.audioManager.setMode(0);
                this.audioManager.setSpeakerphoneOn(true);
                this.setScreenOn();
            }

        }
    }

    @TargetApi(21)
    private void setScreenOff() {
        if (this.wakeLock == null) {
            if (Build.VERSION.SDK_INT >= 21) {
                this.wakeLock = this.powerManager.newWakeLock(32, "AudioPlayManager");
            } else {
                Log.e(TAG, "Does not support on level " + Build.VERSION.SDK_INT);
            }
        }

        if (this.wakeLock != null) {
            this.wakeLock.acquire();
        }

    }

    private void setScreenOn() {
        if (this.wakeLock != null) {
            this.wakeLock.setReferenceCounted(false);
            this.wakeLock.release();
            this.wakeLock = null;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void replay() {
        try {
            this.mediaPlayer.reset();
            this.mediaPlayer.setAudioStreamType(0);
            this.mediaPlayer.setVolume(1.0F, 1.0F);
            this.mediaPlayer.setDataSource(this.context, this.playingUri);
            this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            this.mediaPlayer.prepareAsync();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public void startPlay(Context context, Uri audioUri, IAudioPlayListener playListener) {
        if (context != null && audioUri != null) {
            this.context = context;
            if (this.playListener != null && this.playingUri != null) {
                this.playListener.onStop(this.playingUri);
            }

            this.resetMediaPlayer();
            this.afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    Log.d(TAG, "OnAudioFocusChangeListener " + focusChange);
                    if (AudioPlayManager.this.audioManager != null && focusChange == -1) {
                        AudioPlayManager.this.audioManager.abandonAudioFocus(AudioPlayManager.this.afChangeListener);
                        AudioPlayManager.this.afChangeListener = null;
                        AudioPlayManager.this.resetMediaPlayer();
                    }

                }
            };

            try {
                this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (!this.audioManager.isWiredHeadsetOn()) {
                    this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                    this.sensor = this.sensorManager.getDefaultSensor(8);
                    this.sensorManager.registerListener(this, this.sensor, 3);
                }

                this.muteAudioFocus(this.audioManager, true);
                this.playListener = playListener;
                this.playingUri = audioUri;
                this.mediaPlayer = new MediaPlayer();
                this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        if (AudioPlayManager.this.playListener != null) {
                            AudioPlayManager.this.playListener.onComplete(AudioPlayManager.this.playingUri);
                            AudioPlayManager.this.playListener = null;
                            AudioPlayManager.this.context = null;
                        }

                        AudioPlayManager.this.reset();
                    }
                });
                this.mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        AudioPlayManager.this.reset();
                        return true;
                    }
                });
                this.mediaPlayer.setDataSource(context, audioUri);
                this.mediaPlayer.setAudioStreamType(3);
                this.mediaPlayer.prepare();
                this.mediaPlayer.start();
                if (this.playListener != null) {
                    this.playListener.onStart(this.playingUri);
                }
            } catch (Exception var5) {
                var5.printStackTrace();
                if (this.playListener != null) {
                    this.playListener.onStop(audioUri);
                    this.playListener = null;
                }

                this.reset();
            }

        } else {
            Log.e(TAG, "startPlay context or audioUri is null.");
        }
    }

    public void setPlayListener(IAudioPlayListener listener) {
        this.playListener = listener;
    }

    public void stopPlay() {
        if (this.playListener != null && this.playingUri != null) {
            this.playListener.onStop(this.playingUri);
        }

        this.reset();
    }

    private void reset() {
        this.resetMediaPlayer();
        this.resetAudioPlayManager();
    }

    private void resetAudioPlayManager() {
        if (this.audioManager != null) {
            this.muteAudioFocus(this.audioManager, false);
        }

        if (this.sensorManager != null) {
            this.sensorManager.unregisterListener(this);
        }

        this.sensorManager = null;
        this.sensor = null;
        this.powerManager = null;
        this.audioManager = null;
        this.wakeLock = null;
        this.playListener = null;
        this.playingUri = null;
    }

    private void resetMediaPlayer() {
        if (this.mediaPlayer != null) {
            try {
                this.mediaPlayer.stop();
                this.mediaPlayer.reset();
                this.mediaPlayer.release();
                this.mediaPlayer = null;
            } catch (IllegalStateException var2) {
                var2.printStackTrace();
            }
        }

    }

    public Uri getPlayingUri() {
        return this.playingUri;
    }

    @TargetApi(8)
    private void muteAudioFocus(AudioManager audioManager, boolean bMute) {
        if (Build.VERSION.SDK_INT < 8) {
            Log.d(TAG, "muteAudioFocus Android 2.1 and below can not stop music");
        } else {
            if (bMute) {
                audioManager.requestAudioFocus(this.afChangeListener, 3, 2);
            } else {
                audioManager.abandonAudioFocus(this.afChangeListener);
                this.afChangeListener = null;
            }

        }
    }

    static class SingletonHolder {
        static AudioPlayManager sInstance = new AudioPlayManager();

        SingletonHolder() {
        }
    }
}