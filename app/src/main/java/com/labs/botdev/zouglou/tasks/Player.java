package com.labs.botdev.zouglou.tasks;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

public class Player extends AsyncTask<String, Void, Boolean> {
     MediaPlayer mediaPlayer= new MediaPlayer();
     boolean initialStage = true;
     boolean playPause;
    @Override
    protected Boolean doInBackground(String... strings) {
        Boolean prepared = false;

        try {
            mediaPlayer.setDataSource(strings[0]);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    initialStage = true;
                    playPause = false;
                    //btn.setText("Launch Streaming");
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
            });

            mediaPlayer.prepare();
            prepared = true;

        } catch (Exception e) {
            Log.e("MyAudioStreamingApp", e.getMessage());
            prepared = false;
        }

        return prepared;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        mediaPlayer.start();
        initialStage = false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}