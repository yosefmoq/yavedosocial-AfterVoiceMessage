package ru.ifsoft.network.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import github.ankushsachdeva.emojicon.EmojiconTextView;
import ru.ifsoft.network.PhotoViewActivity;
import ru.ifsoft.network.ProfileActivity;
import ru.ifsoft.network.R;
import ru.ifsoft.network.VideoViewActivity;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.model.ChatItem;
import ru.ifsoft.network.view.ResizableImageView;

public class ChatListAdapter extends BaseAdapter implements Constants {
    ImageLoader imageLoader = App.getInstance().getImageLoader();
    public MediaPlayer mediaPlayer;
    private Activity activity;
    private LayoutInflater inflater;
    private List<ChatItem> dialogList;

    private int globalposition ;


    public ChatListAdapter(Activity activity, List<ChatItem> dialogList) {

        this.activity = activity;
        this.dialogList = dialogList;
    }

    private static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
//        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d",  m, s);
    }

    @Override
    public int getCount() {

        return dialogList.size();
    }

    @Override
    public Object getItem(int location) {

        return dialogList.get(location);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        final ChatItem chatItem = dialogList.get(position);
        if (inflater == null) {

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if(convertView == null || ((ViewHolder)convertView.getTag()).id != this.dialogList.get(position).getId())
        {


            convertView = inflater.inflate(R.layout.chat__item_list_row, null);

            viewHolder = new ViewHolder();

            viewHolder.mLeft_Img = convertView.findViewById(R.id.left_img);
            viewHolder.mRight_Img = convertView.findViewById(R.id.right_img);

          /*
            viewHolder.ivPlayLeft = convertView.findViewById(R.id.ivPlayLeft);
            viewHolder.ivPlayRight = convertView.findViewById(R.id.ivPlayRight);

            viewHolder.pbLeft = convertView.findViewById(R.id.pbLift);
            viewHolder.pbRight = convertView.findViewById(R.id.pbRight);
           */

            viewHolder.clSoundLeft = convertView.findViewById(R.id.clSoundLeft);
            viewHolder.clSoundRight = convertView.findViewById(R.id.clSoundRight);
            viewHolder.mLeftPlayVideo = convertView.findViewById(R.id.left_play_video);
            viewHolder.mRightPlayVideo = convertView.findViewById(R.id.right_play_video);

            viewHolder.mLeftItem = convertView.findViewById(R.id.leftItem);
            viewHolder.mRightItem = convertView.findViewById(R.id.rightItem);

            viewHolder.mLeft_FromUser = convertView.findViewById(R.id.left_fromUser);
            viewHolder.mLeft_Message = convertView.findViewById(R.id.left_message);
            viewHolder.mLeft_TimeAgo = convertView.findViewById(R.id.left_timeAgo);

            viewHolder.mRight_FromUser = convertView.findViewById(R.id.right_fromUser);
            viewHolder.mRight_Message = convertView.findViewById(R.id.right_message);
            viewHolder.mRight_TimeAgo = convertView.findViewById(R.id.right_timeAgo);

            viewHolder.mSeenIcon = convertView.findViewById(R.id.seenIcon);

            viewHolder.id = this.dialogList.get(position).getId();

            convertView.setTag(viewHolder);


            viewHolder.mLeft_FromUser.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int getPosition = (Integer) v.getTag();

                    ChatItem chatItem = dialogList.get(getPosition);

                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra("profileId", chatItem.getFromUserId());
                    activity.startActivity(intent);
                }
            });

        } else {

            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }

        viewHolder.mRight_Img.setTag(position);
        viewHolder.mLeft_Img.setTag(position);

        viewHolder.mLeftPlayVideo.setTag(position);
        viewHolder.mRightPlayVideo.setTag(position);

        viewHolder.mLeft_Img.setTag(position);

        viewHolder.mLeft_TimeAgo.setTag(position);
        viewHolder.mLeft_Message.setTag(position);
        viewHolder.mLeft_FromUser.setTag(position);

        viewHolder.mRight_TimeAgo.setTag(position);
        viewHolder.mRight_Message.setTag(position);
        viewHolder.mRight_FromUser.setTag(position);

        viewHolder.mLeftItem.setTag(position);
        viewHolder.mRightItem.setTag(position);

        viewHolder.mSeenIcon.setTag(position);

        /*viewHolder.pbRight.setTag(position);
        viewHolder.pbLeft.setTag(position);
        viewHolder.ivPlayRight.setTag(position);
        viewHolder.ivPlayLeft.setTag(position);*/

        viewHolder.clSoundRight.setTag(position);
        viewHolder.clSoundLeft.setTag(position);

        viewHolder.mLeftPlayVideo.setVisibility(View.GONE);
        viewHolder.mRightPlayVideo.setVisibility(View.GONE);
        viewHolder.clSoundRight.setVisibility(View.GONE);
        viewHolder.clSoundLeft.setVisibility(View.GONE);
        if (App.getInstance().getId() == chatItem.getFromUserId()) {

            viewHolder.mLeftItem.setVisibility(View.GONE);

            viewHolder.mRightItem.setVisibility(View.VISIBLE);

            if (chatItem.getFromUserPhotoUrl().length() > 0) {

                imageLoader.get(chatItem.getFromUserPhotoUrl(), ImageLoader.getImageListener(viewHolder.mRight_FromUser, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            } else {

                viewHolder.mRight_FromUser.setImageResource(R.drawable.profile_default_photo);
            }

            if (chatItem.getStickerId() != 0) {

                viewHolder.mRight_Img.getLayoutParams().width = 256;
                viewHolder.mRight_Img.requestLayout();

                viewHolder.mRight_Img.setOnClickListener(null);

                imageLoader.get(chatItem.getStickerImgUrl(), ImageLoader.getImageListener(viewHolder.mRight_Img, R.drawable.img_loading, R.drawable.img_loading));
                viewHolder.mRight_Img.setVisibility(View.VISIBLE);
                viewHolder.clSoundRight.setVisibility(View.GONE);

            } else if (chatItem.getVideoUrl() != null && chatItem.getVideoUrl().length() != 0) {
                Log.d(TAG, "getView: Video Item");
                viewHolder.mRight_Img.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                viewHolder.mRight_Img.requestLayout();

                viewHolder.mRight_Img.setOnClickListener(v -> {
                    Log.d(TAG, "getView: " + chatItem.getVideoUrl());
                    Intent i = new Intent(activity, VideoViewActivity.class);
                    i.putExtra("videoUrl", chatItem.getVideoUrl());
                    activity.startActivity(i);
                });

                viewHolder.mRightPlayVideo.setOnClickListener(v -> {
                    Log.d(TAG, "getView: " + chatItem.getVideoUrl());
                    Intent i = new Intent(activity, VideoViewActivity.class);
                    i.putExtra("videoUrl", chatItem.getVideoUrl());
                    activity.startActivity(i);
                });

                viewHolder.clSoundRight.setVisibility(View.GONE);

                imageLoader.get(chatItem.getImgUrl(), ImageLoader.getImageListener(viewHolder.mRight_Img, R.drawable.img_loading, R.drawable.img_loading));
                viewHolder.mRightPlayVideo.setVisibility(View.VISIBLE);
                viewHolder.mRight_Img.setVisibility(View.VISIBLE);

            } else {

                if (chatItem.getImgUrl().length() != 0) {

                    viewHolder.mRight_Img.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    viewHolder.mRight_Img.requestLayout();

                    viewHolder.mRight_Img.setOnClickListener(v -> {
                        Intent i = new Intent(activity, PhotoViewActivity.class);
                        i.putExtra("imgUrl", chatItem.getImgUrl());
                        activity.startActivity(i);
                    });

                    if (chatItem.getImgUrl().contains("gif")) {
                        Glide.with(activity).asGif().load(chatItem.getImgUrl()).listener(new RequestListener<GifDrawable>() {
                            /* class ru.ifsoft.network.adapter.AdvancedItemListAdapter.AnonymousClass1 */

                            @Override // com.bumptech.glide.request.RequestListener
                            public boolean onLoadFailed(GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        }).into(viewHolder.mRight_Img);
                        viewHolder.clSoundRight.setVisibility(View.GONE);

                    } else if (chatItem.getImgUrl().contains("mp3")) {

                        viewHolder.mRight_Img.setVisibility(View.GONE);
                        viewHolder.clSoundRight.setVisibility(View.VISIBLE);
                        ViewHolder finalViewHolder = viewHolder;



                        final String voicemessage = chatItem.getImgUrl();
                        ExecutorService executors = Executors.newSingleThreadExecutor();
                        executors.execute(new Runnable() {
                            @Override
                            public void run() {
                                //// do background heavy task here
                                if (!voicemessage.equals("")) {
                                    handleVoiceMessage(finalViewHolder.clSoundRight, voicemessage,position);
                                }
                            }
                        });



                    } else {
                        imageLoader.get(chatItem.getImgUrl(), ImageLoader.getImageListener(viewHolder.mRight_Img, R.drawable.img_loading, R.drawable.img_loading));
                        viewHolder.mRight_Img.setVisibility(View.VISIBLE);
                    }

                } else {
                    viewHolder.mRight_Img.setVisibility(View.GONE);
                    viewHolder.clSoundRight.setVisibility(View.GONE);
                }
            }

            viewHolder.mRight_FromUser.setVisibility(View.GONE);

            if (chatItem.getMessage().length() > 0) {

                viewHolder.mRight_Message.setVisibility(View.VISIBLE);

            } else {

                viewHolder.mRight_Message.setVisibility(View.GONE);
            }

            viewHolder.mRight_Message.setText(chatItem.getMessage().replaceAll("<br>", "\n"));
            viewHolder.mRight_Message.setMovementMethod(LinkMovementMethod.getInstance());
            Linkify.addLinks(viewHolder.mRight_Message,Linkify.ALL);


            if (chatItem.getSeenAt() > 0) {

                viewHolder.mSeenIcon.setVisibility(View.VISIBLE);

            } else {

                viewHolder.mSeenIcon.setVisibility(View.GONE);
            }

            viewHolder.mRight_TimeAgo.setText(chatItem.getTimeAgo());

            viewHolder.mRight_Message.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {

                    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("msg", chatItem.getMessage().replaceAll("<br>", "\n"));
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(activity, activity.getString(R.string.msg_copied_to_clipboard), Toast.LENGTH_SHORT).show();

                    return false;
                }
            });

        } else {
            Log.d(TAG, "getView: Chat Left Item " + chatItem.toString());
            viewHolder.mRightItem.setVisibility(View.GONE);

            viewHolder.mLeftItem.setVisibility(View.VISIBLE);

            if (chatItem.getFromUserPhotoUrl().length() > 0) {

                imageLoader.get(chatItem.getFromUserPhotoUrl(), ImageLoader.getImageListener(viewHolder.mLeft_FromUser, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            } else {

                viewHolder.mLeft_FromUser.setImageResource(R.drawable.profile_default_photo);
            }

            if (chatItem.getImgUrl().length() != 0) {
                Log.e(TAG, "getView: This is left video " + chatItem.getVideoUrl() + "");
                if (chatItem.getStickerId() != 0) {
                    viewHolder.mLeft_Img.getLayoutParams().width = 256;
                    viewHolder.mLeft_Img.requestLayout();

                    viewHolder.mLeft_Img.setOnClickListener(null);

                    imageLoader.get(chatItem.getStickerImgUrl(), ImageLoader.getImageListener(viewHolder.mLeft_Img, R.drawable.img_loading, R.drawable.img_loading));
                    viewHolder.mLeft_Img.setVisibility(View.VISIBLE);
                    viewHolder.clSoundLeft.setVisibility(View.GONE);

                } else if (chatItem.getVideoUrl() != null && chatItem.getVideoUrl().length() != 0) {
                    Log.d(TAG, "getView: Video Item");
                    viewHolder.mLeftPlayVideo.setVisibility(View.VISIBLE);
                    viewHolder.mLeft_Img.setVisibility(View.VISIBLE);

                    viewHolder.mLeft_Img.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    viewHolder.mLeft_Img.requestLayout();

                    viewHolder.mLeft_Img.setOnClickListener(v -> {
                        Log.d(TAG, "getView: " + chatItem.getVideoUrl());
                        Intent i = new Intent(activity, VideoViewActivity.class);
                        i.putExtra("videoUrl", chatItem.getVideoUrl());
                        activity.startActivity(i);
                    });
                    viewHolder.mLeftPlayVideo.setOnClickListener(v -> {
                        Log.d(TAG, "getView: " + chatItem.getVideoUrl());
                        Intent i = new Intent(activity, VideoViewActivity.class);
                        i.putExtra("videoUrl", chatItem.getVideoUrl());
                        activity.startActivity(i);
                    });
                    viewHolder.clSoundLeft.setVisibility(View.GONE);


                    imageLoader.get(chatItem.getImgUrl(), ImageLoader.getImageListener(viewHolder.mLeft_Img, R.drawable.img_loading, R.drawable.img_loading));

                } else {
                    viewHolder.mLeft_Img.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    viewHolder.mLeft_Img.requestLayout();

                    viewHolder.mLeft_Img.setOnClickListener(v -> {
                        Intent i = new Intent(activity, PhotoViewActivity.class);
                        i.putExtra("imgUrl", chatItem.getImgUrl());
                        activity.startActivity(i);
                    });
                }
                if (chatItem.getImgUrl().contains("gif")) {
                    Glide.with(activity).asGif().load(chatItem.getImgUrl()).listener(new RequestListener<GifDrawable>() {

                        @Override // com.bumptech.glide.request.RequestListener
                        public boolean onLoadFailed(GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    }).into(viewHolder.mLeft_Img);
                    viewHolder.clSoundLeft.setVisibility(View.GONE);

                } else if (chatItem.getImgUrl().contains("mp3")) {
                    viewHolder.mLeft_Img.setVisibility(View.GONE);
                    viewHolder.clSoundLeft.setVisibility(View.VISIBLE);

                    ViewHolder finalViewHolder = viewHolder;
                    final String voicemessage = chatItem.getImgUrl();
                    ExecutorService executors = Executors.newSingleThreadExecutor();
                    executors.execute(new Runnable() {
                        @Override
                        public void run() {
                            //// do background heavy task here
                            if (!voicemessage.equals("")) {
                                handleVoiceMessage(finalViewHolder.clSoundLeft, voicemessage,position);
                            }
                        }
                    });


                } else {

                    imageLoader.get(chatItem.getImgUrl(), ImageLoader.getImageListener(viewHolder.mLeft_Img, R.drawable.img_loading, R.drawable.img_loading));
                    viewHolder.mLeft_Img.setVisibility(View.VISIBLE);
                    viewHolder.clSoundLeft.setVisibility(View.GONE);
                }

            } else {
                viewHolder.clSoundLeft.setVisibility(View.GONE);
                viewHolder.mLeft_Img.setVisibility(View.GONE);
            }

            if (chatItem.getMessage().length() > 0) {

                viewHolder.mLeft_Message.setVisibility(View.VISIBLE);

            } else {

                viewHolder.mLeft_Message.setVisibility(View.GONE);
            }

            viewHolder.mLeft_Message.setText(chatItem.getMessage().replaceAll("<br>", "\n"));
            viewHolder.mLeft_Message.setMovementMethod(LinkMovementMethod.getInstance());
            Linkify.addLinks(viewHolder.mLeft_Message,Linkify.ALL);
            viewHolder.mLeft_TimeAgo.setText(chatItem.getTimeAgo());

            viewHolder.mLeft_Message.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {

                    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("msg", chatItem.getMessage().replaceAll("<br>", "\n"));
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(activity, activity.getString(R.string.msg_copied_to_clipboard), Toast.LENGTH_SHORT).show();

                    return false;
                }
            });
        }

        return convertView;
    }

    private void handleVoiceMessage(  ConstraintLayout clSound,   String imgUrl, int p) {
          MediaPlayer mplayer = new MediaPlayer();
        SeekBar seekBar = clSound.findViewById(R.id.seekBar);
        TextView txtProcess = clSound.findViewById(R.id.txtTime);
        ImageView imgPlay = clSound.findViewById(R.id.imgPlay);
        ImageView imgPause = clSound.findViewById(R.id.imgPause);


        View.OnClickListener imgPauseClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgPause.setVisibility(View.GONE);
                imgPlay.setVisibility(View.VISIBLE);
                mplayer.pause();
            }
        };
        View.OnClickListener imgPlayClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = mplayer;

                imgPause.setVisibility(View.VISIBLE);
                imgPlay.setVisibility(View.GONE);
                // start from seekbar ...


                mplayer.start();
                try {
                    update(mplayer, txtProcess, seekBar,p);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mplayer.seekTo(progress);
                    update(mplayer, txtProcess, seekBar,p);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                imgPause.setVisibility(View.GONE);
                imgPlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                imgPlay.setVisibility(View.GONE);
                imgPause.setVisibility(View.VISIBLE);
                mplayer.start();

            }
        };

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }


        if (imgUrl != null) {
            try {
                mplayer.setDataSource(imgUrl);
                mplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mplayer.prepare();
                mplayer.setVolume(10, 10);
                //mplayer and PAUSE are in other listeners
                mplayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        seekBar.setMax(mp.getDuration());

                        txtProcess.setText("00:00/" + convertSecondsToHMmSs(mp.getDuration() / 1000));
                    }
                });

                mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        imgPause.setVisibility(View.GONE);
                        imgPlay.setVisibility(View.VISIBLE);

                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ChatListAdapter", " mediaplayer catch ::::::::: " + e.getMessage());
            }
        }

        seekBar.setOnSeekBarChangeListener(seekBarListener);
        imgPlay.setOnClickListener(imgPlayClickListener);
        imgPause.setOnClickListener(imgPauseClickListener);
    }

    //Updating seekBar in realtime
    private void update(final MediaPlayer mediaPlayer, final TextView time, final  SeekBar seekBar,int position) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());

                   int duration = mediaPlayer.getDuration();

                    if (duration - mediaPlayer.getCurrentPosition() > 100) {
                        time.setText(convertSecondsToHMmSs(mediaPlayer.getCurrentPosition() / 1000) + " / " + convertSecondsToHMmSs(duration / 1000) +" p" +position);
                    } else {
                        time.setText(convertSecondsToHMmSs(duration/ 1000));
                        seekBar.setProgress(0);
                        return;
                    }


                Handler handler = new Handler();
                try {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mediaPlayer.getCurrentPosition() > -1) {
                                    try {
                                        update(mediaPlayer, time, seekBar,position);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    handler.postDelayed(runnable, 2);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    static class ViewHolder {
        private int id;
        private  ConstraintLayout clSoundLeft;
        private  ConstraintLayout clSoundRight;


        private  ResizableImageView mLeft_Img;
        private  ResizableImageView mRight_Img;

        private  TextView mLeft_TimeAgo;
        private  EmojiconTextView mLeft_Message;
        private  CircularImageView mLeft_FromUser;

        private  TextView mRight_TimeAgo;
        private  EmojiconTextView mRight_Message;
        private  CircularImageView mRight_FromUser;

        private  LinearLayout mLeftItem;
        private  LinearLayout mRightItem;

        private  ImageView mSeenIcon;
        private  ImageView mLeftPlayVideo;
        private  ImageView mRightPlayVideo;


    }


}