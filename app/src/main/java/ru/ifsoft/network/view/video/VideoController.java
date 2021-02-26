package ru.ifsoft.network.view.video;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import ru.ifsoft.network.R;
import ru.ifsoft.network.VideoViewActivity;

public class VideoController extends LinearLayout implements PlayerControlView.VisibilityListener, View.OnClickListener {
    public enum Type {LIST, FULL}
    
    public Uri s;
    private SimpleExoPlayer player;
    ImageButton btFulScreen;
    private Type type;
    PlayerView playerView;
    public VideoController(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.video_controller, this, true);
        btFulScreen = findViewById(R.id.bt_full_screen);
        btFulScreen.setVisibility(VISIBLE);
        btFulScreen.setOnClickListener(this);
        playerView= findViewById(R.id.videoView);
        player = ExoPlayerFactory.newSimpleInstance(getContext());

        playerView.setKeepScreenOn(true);


        playerView.setPlayer(player);
        try {
            java.lang.reflect.Field field = playerView.getClass().getDeclaredField("controller");
            field.setAccessible(true);
            PlayerControlView playerControlView = (PlayerControlView) field.get(playerView);
            assert playerControlView != null;
            playerControlView.addVisibilityListener(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean isPause() {
        return player.getPlaybackState() == Player.STATE_READY && !player.getPlayWhenReady();
    }

    public boolean isPlay() {
        return (player.getPlaybackState() == Player.STATE_READY || player.getPlaybackState() == Player.STATE_BUFFERING)
                && player.getPlayWhenReady();
    }


    public void setTypeAndVideoURI(Type type, Uri uri) {
        if(type == Type.FULL) {
            btFulScreen.setVisibility(GONE);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
        }else {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        }
        player.setPlayWhenReady(true);
        player.prepare(buildMediaSource(uri), false, false);
        s = uri;
        this.type = type;

        }
    
    public void pause(){
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }
    
    public  void stop(){
        player.release();
    }
    
    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
            new DefaultDataSourceFactory(getContext(), "exoplayer-codelab");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri);
        
    }
    
    @Override
    public void onClick(View v) {
        getContext().startActivity(new Intent(getContext(), VideoViewActivity.class).putExtra("videoUrl", s.toString()).putExtra("pos",player.getCurrentPosition()));
        pause();
    }
    public void pauseVideo() {
        player.setPlayWhenReady(false);
        player.getPlaybackState();
//        pause();
    }

    public boolean isPlaying() {
        return player != null
                && player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlayWhenReady();
    }

    public void setPos(long pos){
        player.seekTo(pos);
    }
    public void startFrom(long time,Uri uri){
        player.setPlayWhenReady(true);
        player.prepare(buildMediaSource(uri), false, false);
        player.seekTo(time);
        s = uri;
    }

    @Override
    public void onVisibilityChange(int visibility) {
        if (type == Type.LIST)
            btFulScreen.setVisibility(visibility);
    }
    public long getTime(){
       return player.getContentPosition();
    }
}
