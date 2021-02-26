package ru.ifsoft.network;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.ImageLoader;

import ru.ifsoft.network.common.ActivityBase;
import ru.ifsoft.network.view.video.VideoController;
import uk.co.senab.photoview.PhotoViewAttacher;


public class VideoViewActivity extends ActivityBase {

    Toolbar toolbar;

    long pos;
    //VideoView mVideoView;

    ImageView photoView;

    LinearLayout mContentScreen;
    RelativeLayout mLoadingScreen;

    PhotoViewAttacher mAttacher;
    ImageLoader imageLoader;

    String videoUrl;
    VideoController videoController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_view);

        //mVideoView = (VideoView) findViewById(R.id.videoView);

        Intent i = getIntent();

        videoUrl = i.getStringExtra("videoUrl");
        pos = i.getLongExtra("pos",0);

        //videoUrl = videoUrl.replaceAll("https://","http://");

/*
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");
*/

        videoController = findViewById(R.id.media_controller);
        videoController.setTypeAndVideoURI(VideoController.Type.FULL,Uri.parse(videoUrl));
        videoController.setPos(pos);
//        showpDialog();
//
//        try {
//            // Start the MediaController
//            MediaController mediacontroller = new MediaController(VideoViewActivity.this);
//
//            mediacontroller.setAnchorView(mVideoView);
//            // Get the URL from String VideoURL
//
//            mVideoView.setMediaController(mediacontroller);
//
//            Uri videoUri = Uri.parse(videoUrl);
//
//            mVideoView.setVideoURI(videoUri);
//            // mVideoView.setVideoPath(videoUrl);
//
//        } catch (Exception e) {
//
//            hidepDialog();
//
//            Log.e("Error Video Play", e.getMessage());
//            e.printStackTrace();
//        }
//
//        mVideoView.requestFocus();
//
//        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            // Close the progress bar and play the video
//            public void onPrepared(MediaPlayer mp) {
//
//                hidepDialog();
//                mVideoView.start();
//            }
//        });
//
//        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//
//                Log.e("setOnCompletionListener", "setOnCompletionListener");
//            }
//        });
//
//        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//
//                hidepDialog();
//
//                Log.e("setOnErrorListener", "setOnErrorListener");
//
//                return false;
//            }
//
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {

            case android.R.id.home: {

                finish();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoController.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FeedFragment.time = videoController.getTime();
    }
}
