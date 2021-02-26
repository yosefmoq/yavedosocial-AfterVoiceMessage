package ru.ifsoft.network;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import github.ankushsachdeva.emojicon.EditTextImeBackListener;
import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import okhttp3.MultipartBody;
import ru.ifsoft.network.adapter.ChatListAdapter;
import ru.ifsoft.network.adapter.StickerListAdapter;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.model.ChatItem;
import ru.ifsoft.network.model.Sticker;
import ru.ifsoft.network.util.Api;
import ru.ifsoft.network.util.CustomRequest;
import ru.ifsoft.network.util.Helper;
import timerx.Stopwatch;
import timerx.StopwatchBuilder;
import timerx.TimeTickListener;

import static com.facebook.FacebookSdk.getApplicationContext;
import static ru.ifsoft.network.constants.AppConstants.getGifUrl;


public class ChatFragment extends Fragment implements Constants {

    private static final String STATE_LIST = "State Adapter Data";

    public final static int STATUS_START = 100;
    private static final String AUDIO_RECORDER_FILE_EXT_MP3 = ".mp3";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private String[] file_exts = {".mp3"};
    private int currentFormat = 0;
    private int duration = 0;
    public final static String PARAM_TASK = "task";
    public final static String PARAM_STATUS = "status";
    public final static String BROADCAST_ACTION = "ru.ifsoft.network.chat";
    public final static String BROADCAST_ACTION_SEEN = "ru.ifsoft.network.seen";
    public final static String BROADCAST_ACTION_TYPING_START = "ru.ifsoft.network.typing_start";
    public final static String BROADCAST_ACTION_TYPING_END = "ru.ifsoft.network.typing_end";
    private MediaRecorder.OnInfoListener infoListener = (mr, what, extra) -> {

    };
    /* class ru.ifsoft.network.ChatFragment.AnonymousClass45 */
    private MediaRecorder.OnErrorListener errorListener = (mr, what, extra) -> Log.v("ttt", "error");
    final String LOG_TAG = "myLogs";
    ImageView ivMic;
    LinearLayout llTime;
    public static final int RESULT_OK = -1;
    private MediaRecorder recorder = null;
    String soundPath = "";
    Stopwatch stopwatch;
    TextView tvTime;
    private ProgressDialog pDialog;
    private LinearLayout llCantReply,llChat;
    Menu MainMenu;
    private int audioPosition;
    View mListViewHeader;

    RelativeLayout mLoadingScreen, mErrorScreen;
    LinearLayout mContentScreen, mTypingContainer, mContainerImg, mContainerActions, mChatListViewHeaderContainer, mContainerStickers,mContainerGif;

    ImageView mSendMessage, mActionContainerImg, mEmojiBtn, mDeleteImg, mPreviewImg, playVideo, mActionImage, mActionVideo, mActionSticker,mActionGif;
    EmojiconEditText mMessageText;

    ListView listView;

    RecyclerView mRecyclerView;

    private ArrayList<Sticker> stickersList;
    private StickerListAdapter stickersAdapter;

    BroadcastReceiver br, br_seen, br_typing_start, br_typing_end;

    private ArrayList<ChatItem> chatList;

    private ChatListAdapter chatAdapter;

    String withProfile = "", messageText = "", messageImg = "", messageVideo = "", stickerImg = "";
    int chatId = 0, msgId = 0, messagesCount = 0, position = 0;
    long profileId = 0, stickerId = 0, lStickerId = 0;

    String lMessage = "", lMessageImage = "", lMessageVideo = "", lStickerImg = "";

    Boolean blocked = false;

    Boolean stickers_container_visible = false, actions_container_visible = false, img_container_visible = false;

    long fromUserId = 0, toUserId = 0;

    private String selectedChatImg = "";
    private Uri selectedImage;
    private Uri outputFileUri;

    int arrayLength = 0;
    Boolean loadingMore = false;
    Boolean viewMore = false;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;
    private Boolean visible = true;

    private Boolean inboxTyping = false, outboxTyping = false;

    private String with_user_username = "", with_user_fullname = "", with_user_photo_url = "";
    private int with_user_state = 0, with_user_verified = 0;

    EmojiconsPopup popup;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        Log.i("BRX", "ChatFragment:");

        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();
        position = i.getIntExtra("position", 0);
        chatId = i.getIntExtra("chatId", 0);
        profileId = i.getLongExtra("profileId", 0);
        withProfile = i.getStringExtra("withProfile");

        with_user_username = i.getStringExtra("with_user_username");
        with_user_fullname = i.getStringExtra("with_user_fullname");
        with_user_photo_url = i.getStringExtra("with_user_photo_url");

        with_user_state = i.getIntExtra("with_user_state", 0);
        with_user_verified = (int) i.getLongExtra("with_user_verified", 0);

        blocked = i.getBooleanExtra("blocked", false);

        fromUserId = i.getLongExtra("fromUserId", 0);
        toUserId = i.getLongExtra("toUserId", 0);

        chatList = new ArrayList<ChatItem>();
        chatAdapter = new ChatListAdapter(getActivity(), chatList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        popup = new EmojiconsPopup(rootView, getActivity());
        ivMic = (ImageView) rootView.findViewById(R.id.ivMic);
        llTime = (LinearLayout) rootView.findViewById(R.id.llTime);
        popup.setSizeForSoftKeyboard();
        tvTime =  rootView.findViewById(R.id.tvTime);
        /* class ru.ifsoft.network.$$Lambda$ChatFragment$fhqZ2YcM2NkvXqRT_c1lBCMbhVg */// timerx.TimeTickListener
        stopwatch = new StopwatchBuilder().startFormat("MM:SS").onTick(charSequence -> tvTime.setText(charSequence)).changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS").build();
        ivMic.setOnTouchListener(new View.OnTouchListener() {
            /* class ru.ifsoft.network.ChatFragment.AnonymousClass12 */

            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == 0) {
                    Log.v("ttt", "recording start");
                    ChatFragment.this.startRecording();
                    return true;
                } else if (action != 1) {
                    return false;
                } else {
                    Log.v("ttt", "stop recording");
                    ChatFragment.this.stopRecording();
                    return false;
                }
            }
        });
        //Set on emojicon click listener
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mMessageText.append(emojicon.getEmoji());
            }
        });

        //Set on backspace click listener
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mMessageText.dispatchKeyEvent(event);
            }
        });

        //If the emoji popup is dismissed, change mEmojiBtn to emoji icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                setIconEmojiKeyboard();
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {

                if (popup.isShowing())

                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mMessageText.append(emojicon.getEmoji());
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mMessageText.dispatchKeyEvent(event);
            }
        });


        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

            stickers_container_visible = savedInstanceState.getBoolean("stickers_container_visible");
            actions_container_visible = savedInstanceState.getBoolean("actions_container_visible");
            img_container_visible = savedInstanceState.getBoolean("img_container_visible");

            stickersList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            stickersAdapter = new StickerListAdapter(getActivity(), stickersList);

        } else {

            stickersList = new ArrayList<Sticker>();
            stickersAdapter = new StickerListAdapter(getActivity(), stickersList);

            App.getInstance().setCurrentChatId(chatId);

            restore = false;
            loading = false;
            preload = false;

            stickers_container_visible = false;
            actions_container_visible = false;
            img_container_visible = false;
        }

        br_typing_start = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                typing_start();
            }
        };

        IntentFilter intFilt4 = new IntentFilter(BROADCAST_ACTION_TYPING_START);
        getActivity().registerReceiver(br_typing_start, intFilt4);

        br_typing_end = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                typing_end();
            }
        };

        IntentFilter intFilt3 = new IntentFilter(BROADCAST_ACTION_TYPING_END);
        getActivity().registerReceiver(br_typing_end, intFilt3);

        br_seen = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                seen();
            }
        };

        IntentFilter intFilt2 = new IntentFilter(BROADCAST_ACTION_SEEN);
        getActivity().registerReceiver(br_seen, intFilt2);

        br = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                int msgId = intent.getIntExtra("msgId", 0);
                long msgFromUserId = intent.getLongExtra("msgFromUserId", 0);
                int msgFromUserState = intent.getIntExtra("msgFromUserState", 0);
                int msgFromUserVerify = intent.getIntExtra("msgFromUserVerify", 0);
                String msgFromUserUsername = intent.getStringExtra("msgFromUserUsername");
                String msgFromUserFullname = intent.getStringExtra("msgFromUserFullname");
                String msgFromUserPhotoUrl = intent.getStringExtra("msgFromUserPhotoUrl");
                String msgMessage = intent.getStringExtra("msgMessage");
                String msgImgUrl = intent.getStringExtra("msgImgUrl");
                String msgVideoUrl = intent.getStringExtra("msgVideoUrl");
                int msgCreateAt = intent.getIntExtra("msgCreateAt", 0);
                String msgDate = intent.getStringExtra("msgDate");
                String msgTimeAgo = intent.getStringExtra("msgTimeAgo");

                ChatItem c = new ChatItem();
                c.setId(msgId);
                c.setFromUserId(msgFromUserId);

                if (msgFromUserId == App.getInstance().getId()) {

                    c.setFromUserState(App.getInstance().getState());
                    c.setFromUserVerify(App.getInstance().getVerify());
                    c.setFromUserUsername(App.getInstance().getUsername());
                    c.setFromUserFullname(App.getInstance().getFullname());
                    c.setFromUserPhotoUrl(App.getInstance().getPhotoUrl());

                } else {

                    c.setFromUserState(with_user_state);
                    c.setFromUserVerify(with_user_verified);
                    c.setFromUserUsername(with_user_username);
                    c.setFromUserFullname(with_user_fullname);
                    c.setFromUserPhotoUrl(with_user_photo_url);
                }

                c.setMessage(msgMessage);
                c.setImgUrl(msgImgUrl);
                c.setVideoUrl(msgVideoUrl);
                c.setCreateAt(msgCreateAt);
                c.setDate(msgDate);
                c.setTimeAgo(msgTimeAgo);
                Log.e(LOG_TAG, "onReceive: msgVideoUrl" + msgVideoUrl);
                Log.e(LOG_TAG, "onReceive: task = " + task + ", status = " + status + " " + c.getMessage() + " " + Integer.toString(c.getId()));


                final ChatItem lastItem = (ChatItem) listView.getAdapter().getItem(listView.getAdapter().getCount() - 1);

                messagesCount = messagesCount + 1;

                chatList.add(c);

                if (!visible) {

                    try {

                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
                        r.play();

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                chatAdapter.notifyDataSetChanged();

                scrollListViewToBottom();

                if (inboxTyping) typing_end();

                seen();

                sendNotify(GCM_NOTIFY_SEEN);
            }
        };

        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        getActivity().registerReceiver(br, intFilt);

        if (loading) {

            showDialog();
        }

        mLoadingScreen = (RelativeLayout) rootView.findViewById(R.id.loadingScreen);
        mErrorScreen = (RelativeLayout) rootView.findViewById(R.id.errorScreen);
        llCantReply = rootView.findViewById(R.id.llCantReply);
        llChat = rootView.findViewById(R.id.llChat);
        Log.v("ttt",toUserId+"");
        Log.v("ttt",fromUserId+"");
        if(fromUserId==9959){
            llCantReply.setVisibility(View.VISIBLE);
            llChat.setVisibility(View.GONE);
        }else {
            llCantReply.setVisibility(View.GONE);
            llChat.setVisibility(View.VISIBLE);
        }

        mContentScreen = (LinearLayout) rootView.findViewById(R.id.contentScreen);

        mSendMessage = (ImageView) rootView.findViewById(R.id.sendMessage);
        mMessageText = (EmojiconEditText) rootView.findViewById(R.id.messageText);

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newMessage();
            }
        });

        listView = (ListView) rootView.findViewById(R.id.listView);

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        mListViewHeader = getActivity().getLayoutInflater().inflate(R.layout.chat_listview_header, null);
        mChatListViewHeaderContainer = (LinearLayout) mListViewHeader.findViewById(R.id.chatListViewHeaderContainer);

        listView.addHeaderView(mListViewHeader);

        mListViewHeader.setVisibility(View.GONE);

        listView.setAdapter(chatAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0 && mListViewHeader.getVisibility() == View.VISIBLE) {

                    getPreviousMessages();
                }
            }
        });

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        final LinearLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Helper.getStickersGridSpanCount(getActivity()));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(stickersAdapter);

        stickersAdapter.setOnItemClickListener(new StickerListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Sticker obj, int position) {

                stickerId = obj.getId();
                stickerImg = obj.getImgUrl();

                hideStickersContainer();

                send();
            }
        });

        mActionContainerImg = (ImageView) rootView.findViewById(R.id.actionContainerImg);

        mTypingContainer = (LinearLayout) rootView.findViewById(R.id.container_typing);

        mTypingContainer.setVisibility(View.GONE);

        mActionImage = (ImageView) rootView.findViewById(R.id.addFilesImg);
        mActionVideo = (ImageView) rootView.findViewById(R.id.addFilesVideo);
        mActionSticker = (ImageView) rootView.findViewById(R.id.addStickerImg);
        mActionGif = rootView.findViewById(R.id.addGifImg);
        mEmojiBtn = (ImageView) rootView.findViewById(R.id.emojiBtn);
        mDeleteImg = (ImageView) rootView.findViewById(R.id.deleteImg);
        mPreviewImg = (ImageView) rootView.findViewById(R.id.previewImg);
        playVideo = rootView.findViewById(R.id.playVideo);

        mContainerImg = (LinearLayout) rootView.findViewById(R.id.container_img);
        mContainerImg.setVisibility(View.GONE);

        mContainerStickers = (LinearLayout) rootView.findViewById(R.id.container_stickers);
        mContainerStickers.setVisibility(View.GONE);


        mContainerActions = (LinearLayout) rootView.findViewById(R.id.container_actions);
        mContainerActions.setVisibility(View.GONE);

        mDeleteImg.setOnClickListener(v -> {

            selectedImage = null;
            selectedChatImg = "";

            selectedChatVideoThumb = "";
            selectedChatVideo = "";

            hideMediaContainer();
        });

        mActionContainerImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (actions_container_visible) {

                    hideActionsContainer();

                    return;
                }

                if (stickers_container_visible) {

                    hideStickersContainer();

                    return;
                }

                showActionsContainer();
            }
        });

        mActionSticker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showStickersContainer();
            }
        });

        mActionImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                hideActionsContainer();

                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);

                    } else {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);
                    }

                } else {

                    choiceImage();
                }
            }
        });
        mActionGif.setOnClickListener(v -> {
            hideActionsContainer();
            GiphyDialogFragment giphyDialogFragment = new GiphyDialogFragment();
            giphyDialogFragment.show(requireFragmentManager(),"gif");
            giphyDialogFragment.setGifSelectionListener(new GiphyDialogFragment.GifSelectionListener() {
                @Override
                public void onGifSelected(@NotNull Media media, @Nullable String s, @NotNull GPHContentType gphContentType) {
                    String url =getGifUrl(media.getId());
                    messageImg = url;
                    send();
                }

                @Override
                public void onDismissed(@NotNull GPHContentType gphContentType) {

                }

                @Override
                public void didSearchTerm(@NotNull String s) {

                }
            });

        });
        mActionVideo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                hideActionsContainer();

                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(intent, getActivity().getString(R.string.label_select_video)), REQUEST_TAKE_GALLERY_VIDEO);
                }
            }
        });

        if (selectedChatImg != null && selectedChatImg.length() > 0) {

            mPreviewImg.setImageURI(FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(selectedChatImg)));


            showMediaContainer(true);
        } else if (selectedChatVideo.length() > 0) {
            mPreviewImg.setImageURI(FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(selectedChatVideoThumb)));
            showMediaContainer(false);
        }

        if (actions_container_visible) {

            showActionsContainer();
        }

        if (stickers_container_visible) {

            showStickersContainer();
        }

        if (!EMOJI_KEYBOARD) {

            mEmojiBtn.setVisibility(View.GONE);
        }

        mEmojiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                hideActionsContainer();
                hideStickersContainer();

                if (img_container_visible) {

                    mActionContainerImg.setVisibility(View.GONE);
                }

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if (!popup.isShowing()) {

                    //If keyboard is visible, simply show the emoji popup
                    if (popup.isKeyBoardOpen()) {

                        popup.showAtBottom();
                        setIconSoftKeyboard();

                    } else {

                        //else, open the text keyboard first and immediately after that show the emoji popup
                        mMessageText.setFocusableInTouchMode(true);
                        mMessageText.requestFocus();
                        popup.showAtBottomPending();

                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mMessageText, InputMethodManager.SHOW_IMPLICIT);
                        setIconSoftKeyboard();
                    }

                } else {

                    //If popup is showing, simply dismiss it to show the undelying text keyboard
                    popup.dismiss();
                }
            }
        });

        EditTextImeBackListener er = new EditTextImeBackListener() {

            @Override
            public void onImeBack(EmojiconEditText ctrl, String text) {

                hideEmojiKeyboard();
            }
        };

        mMessageText.setOnEditTextImeBackListener(er);

        mMessageText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                String txt = mMessageText.getText().toString();

                if (txt.length() == 0 && outboxTyping) {

                    outboxTyping = false;

                    sendNotify(GCM_NOTIFY_TYPING_END);

                } else {

                    if (!outboxTyping && txt.length() > 0) {

                        outboxTyping = true;

                        sendNotify(GCM_NOTIFY_TYPING_START);
                    }
                }

                Log.e("", "afterTextChanged");
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                //Log.e("", "beforeTextChanged");
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //Log.e("", "onTextChanged");
            }
        });

        if (inboxTyping) {

            mTypingContainer.setVisibility(View.VISIBLE);

        } else {

            mTypingContainer.setVisibility(View.GONE);
        }

        if (!restore) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getChat();

            } else {

                showErrorScreen();
            }

        } else {

            if (App.getInstance().isConnected()) {

                if (!preload) {

                    showContentScreen();

                } else {

                    showLoadingScreen();
                }

            } else {

                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void typing_start() {

        inboxTyping = true;

        mTypingContainer.setVisibility(View.VISIBLE);
    }

    public void typing_end() {

        mTypingContainer.setVisibility(View.GONE);

        inboxTyping = false;
    }

    public void seen() {

        if (chatAdapter.getCount() > 0) {

            for (int i = 0; i < chatAdapter.getCount(); i++) {

                ChatItem item = chatList.get(i);

                if (item.getFromUserId() == App.getInstance().getId()) {

                    chatList.get(i).setSeenAt(1);
                }
            }
        }

        chatAdapter.notifyDataSetChanged();
    }

    public void sendNotify(final int notifyId) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_NOTIFY, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.d("send fcm", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                Log.e("send fcm error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("chatId", Integer.toString(chatId));
                params.put("notifyId", Integer.toString(notifyId));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void hideEmojiKeyboard() {

        popup.dismiss();
    }

    public void setIconEmojiKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_emoji);
    }

    public void setIconSoftKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_keyboard);
    }

    public void onDestroyView() {

        super.onDestroyView();

        getActivity().unregisterReceiver(br);

        getActivity().unregisterReceiver(br_seen);

        getActivity().unregisterReceiver(br_typing_start);

        getActivity().unregisterReceiver(br_typing_end);

        hidepDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(chatAdapter.mediaPlayer!=null){
            if(chatAdapter.mediaPlayer.isPlaying()){
                chatAdapter.mediaPlayer.release();
                chatAdapter.mediaPlayer = null;
            }
        }


    }

    @Override
    public void onResume() {

        super.onResume();

        visible = true;
    }

    @Override
    public void onPause() {

        super.onPause();
        visible = false;
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showDialog() {
        Log.d(TAG, "showDialog: pDialog.isShowing() " + pDialog.isShowing());
        if (!pDialog.isShowing()) {
            Log.d(TAG, "showDialog: Show Dialog...");
            pDialog.show();
        } else
            Log.d(TAG, "showDialog: Already Dialog...");
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);

        outState.putBoolean("stickers_container_visible", stickers_container_visible);
        outState.putBoolean("actions_container_visible", actions_container_visible);
        outState.putBoolean("img_container_visible", img_container_visible);

        outState.putParcelableArrayList(STATE_LIST, stickersList);
    }

    public void openApplicationSettings() {

        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(appSettingsIntent, 10001);
    }

    public void showNoStoragePermissionSnackbar() {

        Snackbar.make(getView(), getString(R.string.label_no_storage_permission), Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(getApplicationContext(), getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
            }

        }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    choiceImage();

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        showNoStoragePermissionSnackbar();
                    }
                }

                return;
            }
        }
    }

    private Bitmap resize(String path) {

        int maxWidth = 512;
        int maxHeight = 512;

        // create the options
        BitmapFactory.Options opts = new BitmapFactory.Options();

        //just decode the file
        opts.inJustDecodeBounds = true;
        Bitmap bp = BitmapFactory.decodeFile(path, opts);

        //get the original size
        int orignalHeight = opts.outHeight;
        int orignalWidth = opts.outWidth;

        //initialization of the scale
        int resizeScale = 1;

        //get the good scale
        if (orignalWidth > maxWidth || orignalHeight > maxHeight) {

            final int heightRatio = Math.round((float) orignalHeight / (float) maxHeight);
            final int widthRatio = Math.round((float) orignalWidth / (float) maxWidth);
            resizeScale = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        //put the scale instruction (1 -> scale to (1/1); 8-> scale to 1/8)
        opts.inSampleSize = resizeScale;
        opts.inJustDecodeBounds = false;

        //get the futur size of the bitmap
        int bmSize = (orignalWidth / resizeScale) * (orignalHeight / resizeScale) * 4;

        //check if it's possible to store into the vm java the picture
        if (Runtime.getRuntime().freeMemory() > bmSize) {

            //decode the file
            bp = BitmapFactory.decodeFile(path, opts);

        } else {

            return null;
        }

        return bp;
    }

    public void save(String outFile, String inFile) {

        try {

            Bitmap bmp = resize(outFile);

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, inFile);
            FileOutputStream fOut = new FileOutputStream(file);

            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();

        } catch (Exception ex) {

            Log.e("Error", ex.getMessage());
        }
    }

    public static final int REQUEST_TAKE_GALLERY_VIDEO = 1001;
    private String selectedChatVideoThumb = "";
    private String selectedChatVideo = "";

    public String getRealPathFromURI(Uri contentUri) {

        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);

        if (cursor.moveToFirst()) {

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }

        cursor.close();

        return res;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                Log.e("BRX" + "resultUri: ", String.valueOf(resultUri));

                selectedImage = resultUri;

                selectedChatImg = getImageUrlWithAuthority(getActivity(), selectedImage, "msg.jpg");

                mPreviewImg.setImageURI(null);

                mPreviewImg.setImageURI(FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(selectedChatImg)));

                showMediaContainer(true);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("BRX" + "error: ", String.valueOf(error));
            }
        } else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
            if (resultCode == RESULT_OK) {
                try{
                    Uri selectedImageUri = data.getData();
                    String selectedVideoFileName = getRealPathFromURI(selectedImageUri);
                    File videoFile = new File(selectedVideoFileName);

                    if (videoFile.length() > VIDEO_FILE_MAX_SIZE) {
                        Toast.makeText(getActivity(), getString(R.string.msg_video_too_large), Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(selectedVideoFileName, MediaStore.Images.Thumbnails.MINI_KIND);
                            Matrix matrix = new Matrix();
                            Bitmap bmThumbnail = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(), thumb.getHeight(), matrix, true);

                            String newFileName = Helper.randomString(6) + ".jpg";
                            String selectedImageFile;

                            writeToTempImageAndGetPathUri(getActivity(), bmThumbnail, newFileName);

                            selectedImageFile = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + newFileName;
                            mPreviewImg.setImageURI(null);

                            mPreviewImg.setImageURI(FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(selectedImageFile)));

                            selectedChatVideoThumb = selectedImageFile;
                            selectedChatVideo = selectedVideoFileName;
                            showMediaContainer(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Something error, choose another video", Toast.LENGTH_SHORT).show();
                        }


                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Something Error, Please Choose Another video", Toast.LENGTH_SHORT).show();
                }


            }
        }

        /**

         if (requestCode == SELECT_CHAT_IMG && resultCode == RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && null != data) {

         selectedImage = data.getData();

         selectedChatImg = getImageUrlWithAuthority(getActivity(), selectedImage, "msg.jpg");

         try {

         save(selectedChatImg, "msg.jpg");

         selectedChatImg = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + "msg.jpg";

         mPreviewImg.setImageURI(null);
         mPreviewImg.setImageURI(FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(selectedChatImg)));

         showImageContainer();

         } catch (Exception e) {

         Log.e("OnSelectPostImage", e.getMessage());
         }

         } else if (requestCode == CREATE_CHAT_IMG && resultCode == getActivity().RESULT_OK) {

         try {

         selectedChatImg = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + "msg.jpg";

         save(selectedChatImg, "msg.jpg");

         mPreviewImg.setImageURI(null);
         mPreviewImg.setImageURI(FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(selectedChatImg)));

         showImageContainer();

         } catch (Exception ex) {

         Log.v("OnCameraCallBack", ex.getMessage());
         }

         }*/
    }

    public static String getImageUrlWithAuthority(Context context, Uri uri, String fileName) {

        InputStream is = null;

        if (uri.getAuthority() != null) {

            try {

                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);

                return writeToTempImageAndGetPathUri(context, bmp, fileName).toString();

            } catch (FileNotFoundException e) {

                e.printStackTrace();

            } finally {

                try {

                    if (is != null) {

                        is.close();
                    }

                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static String writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage, String fileName) {

        String file_path = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER;
        File dir = new File(file_path);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);

        try {

            FileOutputStream fos = new FileOutputStream(file);

            inImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();

        } catch (FileNotFoundException e) {

            Toast.makeText(inContext, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + fileName;
    }

    public void choiceImage() {


        CropImage.activity()
                .start(getContext(), this);

        /**
         AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

         final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

         arrayAdapter.add(getText(R.string.action_gallery).toString());
         arrayAdapter.add(getText(R.string.action_camera).toString());

         builderSingle.setTitle(getText(R.string.dlg_choice_image_title));


         builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

        @Override public void onClick(DialogInterface dialog, int which) {

        switch (which) {

        case 0: {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getText(R.string.label_select_img)), SELECT_CHAT_IMG);

        break;
        }

        default: {

        try {

        File root = new File(Environment.getExternalStorageDirectory(), APP_TEMP_FOLDER);

        if (!root.exists()) {

        root.mkdirs();
        }

        File sdImageMainDirectory = new File(root, "msg.jpg");
        outputFileUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", sdImageMainDirectory);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(cameraIntent, CREATE_CHAT_IMG);

        } catch (Exception e) {

        Toast.makeText(getActivity(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }

        break;
        }
        }
        }
        });

         builderSingle.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

         public void onClick(DialogInterface dialog, int which) {

         dialog.dismiss();
         }
         });

         AlertDialog d = builderSingle.create();
         d.show();
         */
    }

    private void scrollListViewToBottom() {

        listView.smoothScrollToPosition(chatAdapter.getCount());

        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(chatAdapter.getCount() - 1);
            }
        });
    }

    public void updateChat() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_UPDATE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("TAG", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                preload = false;
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("chatId", Integer.toString(chatId));

                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getChat() {

        preload = true;

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                msgId = response.getInt("msgId");
                                chatId = response.getInt("chatId");
                                messagesCount = response.getInt("messagesCount");

                                App.getInstance().setCurrentChatId(chatId);

                                fromUserId = response.getLong("chatFromUserId");
                                toUserId = response.getLong("chatToUserId");

                                if (messagesCount > 20) {

                                    mListViewHeader.setVisibility(View.VISIBLE);
                                }

                                if (response.has("newMessagesCount")) {

                                    App.getInstance().setMessagesCount(response.getInt("newMessagesCount"));
                                }

                                if (response.has("messages")) {

                                    JSONArray messagesArray = response.getJSONArray("messages");

                                    arrayLength = messagesArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = messagesArray.length() - 1; i > -1; i--) {

                                            JSONObject msgObj = (JSONObject) messagesArray.get(i);

                                            ChatItem item = new ChatItem(msgObj);
                                            Log.v("ttt",item.toString());
                                            chatList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            showContentScreen();

                            chatAdapter.notifyDataSetChanged();

                            scrollListViewToBottom();

                            updateChat();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                preload = false;
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));

                params.put("chatId", Integer.toString(chatId));
                params.put("msgId", Integer.toString(msgId));

                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getPreviousMessages() {

        loading = true;

        showDialog();
        Log.d(TAG, "getPreviousMessages: ");
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_GET_PREVIOUS, null,
                response -> {
                    if (!isAdded() || getActivity() == null) {
                        Log.e("ERROR", "ChatFragment Not Added to Activity");
                        return;
                    }

                    try {
                        Log.d(TAG, "getPreviousMessages: " + response.toString());
                        if (!response.getBoolean("error")) {

                            msgId = response.getInt("msgId");
                            chatId = response.getInt("chatId");

                            if (response.has("messages")) {
                                JSONArray messagesArray = response.getJSONArray("messages");

                                arrayLength = messagesArray.length();

                                if (arrayLength > 0) {

                                    for (int i = 0; i < messagesArray.length(); i++) {

                                        JSONObject msgObj = (JSONObject) messagesArray.get(i);

                                        ChatItem item = new ChatItem(msgObj);
                                        Log.d(TAG, "onResponse: " + item.toString());
                                        chatList.add(0, item);
                                    }
                                }
                            }
                        }

                    } catch (JSONException e) {

                        e.printStackTrace();

                    } finally {

                        loading = false;

                        hidepDialog();

                        chatAdapter.notifyDataSetChanged();

                        if (messagesCount <= listView.getAdapter().getCount() - 1) {

                            mListViewHeader.setVisibility(View.GONE);

                        } else {

                            mListViewHeader.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));

                params.put("chatId", Integer.toString(chatId));
                params.put("msgId", Integer.toString(msgId));

                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void newMessage() {
        Log.d(TAG, "newMessage: ");
        if (App.getInstance().isConnected()) {

            messageText = mMessageText.getText().toString();
            messageText = messageText.trim();

            if (selectedChatImg.length() != 0) {
                Log.d(TAG, "newMessage: Image and text");
                loading = true;
                showDialog();


                File f = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, "msg.jpg");
                uploadFile(METHOD_MSG_UPLOAD_IMG, f, true);

            } else if (selectedChatVideo != null && selectedChatVideo.length() != 0) {
                Log.d(TAG, "newMessage: Video and text");
                loading = true;
                File selectedChatVideoThumbFile = new File(selectedChatVideoThumb);
                File selectedChatVideoFile = new File(selectedChatVideo);

                Toast.makeText(getApplicationContext(), "Video Uploading ...", Toast.LENGTH_SHORT).show();
                notificationDialog();
                hideMediaContainer();

                messageText = "";
                messageImg = "";
                messageVideo = "";
                selectedChatVideo = "";
                selectedChatVideoThumb = "";

                uploadVideoFile(METHOD_GALLERY_UPLOAD_VIDEO, selectedChatVideoThumbFile, selectedChatVideoFile);

            } else {
                if (messageText.length() > 0) {
                    loading = true;
                    send();
                } else {
                    Toast.makeText(getActivity(), getText(R.string.msg_enter_msg), Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
        }
    }

    NotificationManager notificationManager;
    public static final String CHANNEL_ID = "chat_channel";
    public static final int NOTIF_ID = 10;
    private static final String TAG = "ChatFragment";

    public Boolean uploadVideoFile(String serverURL, File selectedChatVideoThumbFile, File videoFile) {
        Log.d(TAG, "uploadVideoFile: ");

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();

        try {
            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uploaded_file", selectedChatVideoThumbFile.getName(), okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/csv"), selectedChatVideoThumbFile))
                    .addFormDataPart("uploaded_video_file", videoFile.getName(), okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/csv"), videoFile))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();


            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                    String jsonData = response.body().string();
                    Log.e("response", jsonData);
                    try {
                        JSONObject result = new JSONObject(jsonData);
                        if (!result.getBoolean("error")) {
                            messageImg = result.getString("imgFileUrl");
                            messageVideo = result.getString("videoFileUrl");
                        }
                        if (notificationManager != null)
                            notificationManager.cancel(NOTIF_ID);
                        Log.d("My App", response.toString());
                    } catch (Throwable t) {
                        if (notificationManager != null)
                            notificationManager.cancel(NOTIF_ID);
                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");
                    } finally {
                        if (notificationManager != null)
                            notificationManager.cancel(NOTIF_ID);
                        getActivity().runOnUiThread(() -> send());
                    }
                }

                @Override
                public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                    loading = false;
                    Log.e("failure", request.toString());
                }
            });


            loading = false;
            return true;

        } catch (Exception ex) {
            // Handle the error

            loading = false;
        }

        return false;
    }

    private void notificationDialog() {
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notificationChannel.setDescription("Sample Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_upload)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Video Uploading...");
        notificationManager.notify(NOTIF_ID, notificationBuilder.build());
    }

    public void send() {
        Log.d(TAG, "send: New Message... ");
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_MSG_NEW, null,
                response -> {
                    if (!isAdded() || getActivity() == null) {
                        Log.e(TAG, "ChatFragment Not Added to Activity");
                        return;
                    }
                    try {
                        Log.d(TAG, "send: Response: " + response.toString());
                        if (!response.getBoolean("error")) {
                            Log.d(TAG, "send: Error False");
                            chatId = response.getInt("chatId");
                            App.getInstance().setCurrentChatId(chatId);

                            if (response.has("chatFromUserId")) {
                                fromUserId = response.getLong("chatFromUserId");
                            }

                            if (response.has("chatToUserId")) {
                                toUserId = response.getLong("chatToUserId");
                            }

                            if (response.has("message")) {
                                JSONObject msgObj = (JSONObject) response.getJSONObject("message");
                                ChatItem item = new ChatItem(msgObj);
                                item.setListId(response.getInt("listId"));
                                Log.d(TAG, "send: new ChatItem " + item.toString());
                            }

                        } else {
                            Toast.makeText(getActivity(), getString(R.string.msg_send_msg_error), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        loading = false;
                        hidepDialog();
                        messageText = "";
                        messageImg = "";
                        messageVideo = "";
                    }
                }, error -> {
            if (!isAdded() || getActivity() == null) {
                Log.e("ERROR", "ChatFragment Not Added to Activity");
                return;
            }
            messageText = "";
            messageImg = "";
            messageVideo = "";
            loading = false;
            hidepDialog();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));

                params.put("chatId", Integer.toString(chatId));
                params.put("messageText", lMessage);
                params.put("messageImg", lMessageImage);
                Log.d(TAG, "getParams: lMessageImage " + lMessageImage);
                params.put("messageVideo", lMessageVideo);
                Log.d(TAG, "getParams: lMessageVideo " + lMessageVideo);

                params.put("listId", Integer.toString(listView.getAdapter().getCount()));

                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                params.put("stickerImgUrl", lStickerImg);
                params.put("stickerId", Long.toString(lStickerId));
                params.put("duration",String.valueOf(duration));
                return params;
            }
        };

        lMessage = messageText;
        lMessageImage = messageImg;
        lMessageVideo = messageVideo;
        lStickerImg = stickerImg;
        lStickerId = stickerId;

        if (stickerId != 0) {

            messageImg = stickerImg;

            lMessage = "";
            lMessageImage = "";

            messageText = "";
        }
        ChatItem cItem = new ChatItem();

        cItem.setListId(listView.getAdapter().getCount());
        cItem.setId(0);
        cItem.setFromUserId(App.getInstance().getId());
        cItem.setFromUserState(ACCOUNT_STATE_ENABLED);
        cItem.setFromUserUsername(App.getInstance().getUsername());
        cItem.setFromUserFullname(App.getInstance().getFullname());
        cItem.setFromUserPhotoUrl(App.getInstance().getPhotoUrl());
        cItem.setMessage(messageText);
        cItem.setStickerId(stickerId);
        cItem.setStickerImgUrl(stickerImg);
        cItem.setImgUrl(messageImg);
        cItem.setVideoUrl(messageVideo);
        cItem.setTimeAgo(getActivity().getString(R.string.label_just_now));


        chatList.add(cItem);
        chatAdapter.notifyDataSetChanged();
        scrollListViewToBottom();

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);

        outboxTyping = false;

        mContainerImg.setVisibility(View.GONE);
        selectedChatImg = "";
        selectedImage = null;
        selectedChatVideo = null;
        selectedChatVideoThumb = null;
        messageImg = "";
        mMessageText.setText("");
        messagesCount++;

        stickerImg = "";
        stickerId = 0;

        hideMediaContainer();
    }

    public void deleteChat() {

        loading = true;

        showDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_REMOVE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                Intent i = new Intent();
                                i.putExtra("action", "Delete");
                                i.putExtra("position", position);
                                i.putExtra("chatId", chatId);
                                getActivity().setResult(RESULT_OK, i);

                                getActivity().finish();

//                                Toast.makeText(getActivity(), getString(R.string.msg_send_msg_error), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));
                params.put("chatId", Integer.toString(chatId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void showLoadingScreen() {

        mContentScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mLoadingScreen.setVisibility(View.VISIBLE);
    }

    public void showErrorScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);

        mErrorScreen.setVisibility(View.VISIBLE);
    }

    public void showContentScreen() {

        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mContentScreen.setVisibility(View.VISIBLE);

        preload = false;

        getActivity().invalidateOptionsMenu();
    }

    private void showMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++) {

            menu.getItem(i).setVisible(visible);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (App.getInstance().isConnected()) {

            if (!preload) {

                getActivity().setTitle(withProfile);

                showMenuItems(menu, true);

            } else {

                showMenuItems(menu, false);
            }

        } else {

            showMenuItems(menu, false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_chat, menu);

        MainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_chat_delete: {

                deleteChat();

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {

        super.onDetach();

        updateChat();

        if (outboxTyping) {

            sendNotify(GCM_NOTIFY_TYPING_END);
        }
    }


    public Boolean uploadFile(String serverURL, File file, boolean sendMessageDirect) {

        final OkHttpClient client = new OkHttpClient();

        client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        try {

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                    loading = false;
                    hidepDialog();
                    Log.e(TAG, request.toString());
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                    String jsonData = response.body().string();
                    Log.e(TAG, jsonData);
                    try {
                        JSONObject result = new JSONObject(jsonData);
                        if (!result.getBoolean("error")) {
                            messageImg = result.getString("imgUrl");
                        }
                        Log.d(TAG, response.toString());
                    } catch (Throwable t) {
                        Log.e(TAG, "Could not parse malformed JSON: \"" + t.getMessage() + "\"");
                    } finally {
                        if (sendMessageDirect) {
                            getActivity().runOnUiThread(() -> send());
                        }
                    }

                }
            });
            return true;
        } catch (Exception ex) {
            loading = false;
            hidepDialog();
        }

        return false;
    }

    public void loadStickers() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GET_STICKERS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!isAdded() || getActivity() == null) {

                                Log.e("ERROR", "ChatFragment Not Added to Activity");

                                return;
                            }

                            if (!loadingMore) {

                                stickersList.clear();
                            }

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

//                                stickerId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray stickersArray = response.getJSONArray("items");

                                    arrayLength = stickersArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < stickersArray.length(); i++) {

                                            JSONObject stickerObj = (JSONObject) stickersArray.get(i);

                                            Sticker u = new Sticker(stickerObj);

                                            stickersList.add(u);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.d("SUCCESS", "ChatFragment Success Load Stickers");

                            stickersAdapter.notifyDataSetChanged();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                Log.e("ERROR", "ChatFragment Not Load Stickers");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Integer.toString(0));

                return params;
            }
        };

        jsonReq.setRetryPolicy(new RetryPolicy() {

            @Override
            public int getCurrentTimeout() {

                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {

                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void showStickersContainer() {

        stickers_container_visible = true;

        if (stickersAdapter.getItemCount() == 0) {

            loadStickers();
        }

        mContainerStickers.setVisibility(View.VISIBLE);

        hideActionsContainer();

        mActionContainerImg.setBackgroundResource(R.drawable.ic_close_container_action);

        hideEmojiKeyboard();

        mEmojiBtn.setVisibility(View.GONE);
        mMessageText.setVisibility(View.GONE);
        mSendMessage.setVisibility(View.GONE);
    }

    public void hideStickersContainer() {

        stickers_container_visible = false;

        mContainerStickers.setVisibility(View.GONE);

        mActionContainerImg.setBackgroundResource(R.drawable.ic_open_container_action);

        mEmojiBtn.setVisibility(View.VISIBLE);
        mMessageText.setVisibility(View.VISIBLE);
        mSendMessage.setVisibility(View.VISIBLE);
    }

    public void showActionsContainer() {

        actions_container_visible = true;

        mContainerActions.setVisibility(View.VISIBLE);

        mActionContainerImg.setBackgroundResource(R.drawable.ic_close_container_action);
    }

    public void hideActionsContainer() {

        actions_container_visible = false;

        mContainerActions.setVisibility(View.GONE);

        mActionContainerImg.setBackgroundResource(R.drawable.ic_open_container_action);
    }

    public void showMediaContainer(boolean isImage) {

        img_container_visible = true;

        mContainerImg.setVisibility(View.VISIBLE);
        if (isImage) {
            playVideo.setVisibility(View.GONE);
        } else {
            playVideo.setVisibility(View.VISIBLE);
        }
        mActionContainerImg.setVisibility(View.GONE);
    }

    public void hideMediaContainer() {

        img_container_visible = false;

        mContainerImg.setVisibility(View.GONE);

        mActionContainerImg.setVisibility(View.VISIBLE);

        mActionContainerImg.setBackgroundResource(R.drawable.ic_open_container_action);
    }
    private void startRecording() {
        stopwatch.start();
        llTime.setVisibility(View.VISIBLE);
        if (ContextCompat.checkSelfPermission(requireContext(), "android.permission.RECORD_AUDIO") != 0) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{"android.permission.RECORD_AUDIO"}, 1234);
            return;
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(2);
        recorder.setAudioEncoder(1);
        String path = getFilename();
        recorder.setOutputFile(path);
        Log.v("ttt", "filename" + getFilename());
        soundPath = path;
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);
        try {
            this.recorder.prepare();
            this.recorder.start();
        } catch (IllegalStateException e) {
            Log.v("ttt", "start state::" + e.getLocalizedMessage());
        } catch (IOException e2) {
            Log.v("ttt", "start::Io::" + e2.getLocalizedMessage());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopRecording() {
        duration = (int) stopwatch.getTimeIn(TimeUnit.SECONDS);
        Log.v("ttt",duration+" duration");
        this.llTime.setVisibility(View.GONE);
        if(stopwatch.getTimeIn(TimeUnit.SECONDS)>1){
            this.stopwatch.stop();
            this.stopwatch.reset();

            MediaRecorder mediaRecorder = this.recorder;
            if (mediaRecorder != null) {
                try {

                    mediaRecorder.stop();
                } catch (Exception stopException) {
                    String localizedMessage = stopException.getLocalizedMessage();
                    Log.e("ttt", localizedMessage+"");
                }
                this.recorder.reset();
                this.recorder.release();
                this.recorder = null;
            }
            if (!this.soundPath.equalsIgnoreCase("")) {
                File file = new File(this.soundPath);
                Log.v("ttt", "size::" + file.getTotalSpace());
                Log.v("ttt", "name::" + file.getName());
                Log.v("ttt", "ext::" + file.getPath());
                uploadFile(Constants.METHOD_MSG_UPLOAD_IMG, file, true);
            }

        }else {
            Toast.makeText(requireContext(), "you can't send voice less than 1 sec", Toast.LENGTH_SHORT).show();
            this.stopwatch.stop();
            this.stopwatch.reset();

        }
        this.soundPath = "";

    }
    private String getFilename() {
        PackageInfo p = null;
        try {
            p = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        File file = new File(p.applicationInfo.dataDir, AUDIO_RECORDER_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath() + "/" + UUID.randomUUID() + file_exts[currentFormat];
    }
}