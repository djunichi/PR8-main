package com.example.medialab;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // ----------  Слайд-шоу  ----------
    private ImageView imageView;
    private int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3};
    private int currentIndex = 0;
    private Timer slideshowTimer;
    private boolean isSlideshowRunning = false;

    // ----------  Фоновое аудио  ----------
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private boolean isAudioPausedForVideo = false;  // признак, что пауза вызвана переходом к видео

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Привязка элементов слайд-шоу
        imageView = findViewById(R.id.imageView);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);
        Button btnSlideshow = findViewById(R.id.btnSlideshow);
        Button btnOpenVideo = findViewById(R.id.btnOpenVideo);

        // Слушатели кнопок слайд-шоу
        btnPrev.setOnClickListener(v -> showPreviousImage());
        btnNext.setOnClickListener(v -> showNextImage());
        btnSlideshow.setOnClickListener(v -> toggleSlideshow());

        // ----------  Инициализация фонового аудио  ----------
        mediaPlayer = MediaPlayer.create(this, R.raw.audio_sample);
        mediaPlayer.setLooping(true);   // зацикливание
        mediaPlayer.start();            // запуск сразу при старте приложения

        // ----------  Переход к видео с паузой аудио  ----------
        btnOpenVideo.setOnClickListener(v -> {
            // Если аудио играет, ставим на паузу и запоминаем
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isAudioPausedForVideo = true;
            }
            // Открываем VideoActivity
            startActivity(new Intent(MainActivity.this, VideoActivity.class));
        });
    }

    // ----------  Возобновление аудио при возврате из видео  ----------
    @Override
    protected void onResume() {
        super.onResume();
        // Если пауза была вызвана уходом на видео, через 1.5 сек включаем снова
        if (isAudioPausedForVideo && mediaPlayer != null && !mediaPlayer.isPlaying()) {
            handler.postDelayed(() -> {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    isAudioPausedForVideo = false;
                }
            }, 1500);
        }
    }

    // ----------  Методы слайд-шоу (без изменений)  ----------
    private void showImage(int index) {
        if (index >= 0 && index < images.length) {
            imageView.setImageResource(images[index]);
            currentIndex = index;
        }
    }

    private void showNextImage() {
        currentIndex = (currentIndex + 1) % images.length;
        showImage(currentIndex);
    }

    private void showPreviousImage() {
        currentIndex = (currentIndex - 1 + images.length) % images.length;
        showImage(currentIndex);
    }

    private void toggleSlideshow() {
        if (isSlideshowRunning) {
            if (slideshowTimer != null) {
                slideshowTimer.cancel();
            }
            isSlideshowRunning = false;
        } else {
            slideshowTimer = new Timer();
            slideshowTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> showNextImage());
                }
            }, 0, 2000);
            isSlideshowRunning = true;
        }
    }

    // ----------  Освобождение ресурсов  ----------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (slideshowTimer != null) {
            slideshowTimer.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}