package ru.ifsoft.network;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.balysv.materialripple.MaterialRippleLayout;
import com.facebook.common.statfs.StatFsHelper;
import com.facebook.common.util.UriUtil;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import github.ankushsachdeva.emojicon.EditTextImeBackListener;
import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import okhttp3.MultipartBody;
import ru.ifsoft.network.adapter.FeelingsListAdapter;
import ru.ifsoft.network.adapter.MediaListAdapter;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.db.Session;
import ru.ifsoft.network.model.Feeling;
import ru.ifsoft.network.model.Item;
import ru.ifsoft.network.model.MediaItem;
import ru.ifsoft.network.model.PostInShared;
import ru.ifsoft.network.util.Api;
import ru.ifsoft.network.util.CountingRequestBody;
import ru.ifsoft.network.util.CustomRequest;
import ru.ifsoft.network.util.Helper;

public class NewItemFragment extends Fragment implements Constants, GiphyDialogFragment.GifSelectionListener {

    private static final int VIDEO_FILES_LIMIT = 1;
    private static final int IMAGE_FILES_LIMIT = 7;

    public static final int REQUEST_TAKE_GALLERY_VIDEO = 1001;

    private static final String STATE_LIST = "State Adapter Data";

    public static final int RESULT_OK = -1;

    private static final int ITEM_FEELINGS = 1;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    private MaterialRippleLayout mOpenBottomSheet;
    boolean isSelectGif = false;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View mBottomSheet;
    GiphyDialogFragment giphyDialogFragment;

    private RecyclerView mRecyclerView;
    private LinearLayout mImagesLayout;

    private LinearLayout mRepostLayout, mDeleteRepost;
    private TextView mRepostAuthorTitle, mRepostContent;
    private CircularImageView mRepostAuthorPhoto;
    private ImageView mRepostImage;
    private Button mViewRepostButton;
    private Translate translate;
    public ArrayList<MediaItem> itemsList;
    private MediaListAdapter itemsAdapter;

    private CircularImageView mPhoto;
    private TextView mFullname;

    private ImageView mAccessModeIcon;
    private TextView mAccessModeTitle;
    private LinearLayout mAccessModeLayout;

    private ImageView mLocationIcon;
    private TextView mLocationTitle;
    private LinearLayout mLocationLayout;

    private ImageView mFeelingIcon;
    private TextView mFeelingTitle;
    private LinearLayout mFeelingLayout;

    private ProgressDialog pDialog;
    private boolean isClickPost = false;
    EmojiconEditText mPostEdit;
    ImageView mEmojiBtn;

    private long group_id = 0;
    private int position = 0;

    private Item post_item;

    private Boolean loading = false;

    EmojiconsPopup popup;

    private SharedPreferences sharedPref;

    //private static final int CROP_REQUEST = 200; // VIDEO CROP

    public NewItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        sharedPref = getActivity().getSharedPreferences("posting", Context.MODE_PRIVATE);
        getTranslateService();
        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        group_id = i.getLongExtra("groupId", 0);

        position = i.getIntExtra("position", 0);

        Item repost = new Item();

        if (i.getExtras() != null) {

            post_item = (Item) i.getExtras().getParcelable("item");

            if (post_item == null) {

                post_item = new Item();
            }

            if (post_item.getGroupId() != 0) group_id = post_item.getGroupId();

            repost = (Item) i.getExtras().getParcelable("repost");

            if (repost == null) {

                repost = new Item();
            }

        } else {

            post_item = new Item();
        }

        if (repost.getId() != 0) {

            if (repost.getRePostId() == 0) {

                post_item.setRePostId(repost.getId());
                post_item.setRePostPost(repost.getPost());
                post_item.setRePostFromUserFullname(repost.getFromUserFullname());
                post_item.setRePostFromUserPhotoUrl(repost.getFromUserPhotoUrl());
                post_item.setRePostImgUrl(repost.getImgUrl());

            } else {

                post_item.setRePostId(repost.getRePostId());
                post_item.setRePostPost(repost.getRePostPost());
                post_item.setRePostFromUserFullname(repost.getRePostFromUserFullname());
                post_item.setRePostFromUserPhotoUrl(repost.getRePostFromUserPhotoUrl());
                post_item.setRePostImgUrl(repost.getRePostImgUrl());
            }


        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PostInShared postInShared = Session.getInstance(getContext()).getLocalSave().getPost();
        if (postInShared != null) {
            if (postInShared.getItemsList().size() != 0 || !postInShared.getPost().equalsIgnoreCase("")) {
                mPostEdit.setText(postInShared.getPost());
                if (postInShared.getItemsList().size() != 0) {
                    itemsList.addAll(postInShared.getItemsList());
                    updateMediaLayout();
                }
            }
        }
        giphyDialogFragment = new GiphyDialogFragment();
        giphyDialogFragment.setGifSelectionListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_item, container, false);

        if (savedInstanceState != null) {

            post_item = savedInstanceState.getParcelable("item");

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new MediaListAdapter(getActivity(), itemsList);

        } else {

            itemsList = new ArrayList<>();
            itemsAdapter = new MediaListAdapter(getActivity(), itemsList);
        }

        popup = new EmojiconsPopup(rootView, getActivity());

        popup.setSizeForSoftKeyboard();

        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                mPostEdit.setEmojiconSize(3000);
                mPostEdit.append(emojicon.getEmoji());
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mPostEdit.dispatchKeyEvent(event);
            }
        });

        popup.setOnDismissListener(() -> setIconEmojiKeyboard());

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

        popup.setOnEmojiconClickedListener(emojicon -> mPostEdit.append(emojicon.getEmoji()));

        popup.setOnEmojiconBackspaceClickedListener(v -> {

            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            mPostEdit.dispatchKeyEvent(event);
        });

        if (loading) {
            Log.d("dialogshow", "onCreateView");
            showpDialog();
        }

        //

        mOpenBottomSheet = (MaterialRippleLayout) rootView.findViewById(R.id.open_bottom_sheet_button);

        mOpenBottomSheet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showBottomSheet();
            }
        });

        // Prepare bottom sheet

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);

        //

        mPhoto = (CircularImageView) rootView.findViewById(R.id.photo_image);
        mFullname = (TextView) rootView.findViewById(R.id.fullname_label);

        //

        mRepostLayout = (LinearLayout) rootView.findViewById(R.id.repost_layout);
        mDeleteRepost = (LinearLayout) rootView.findViewById(R.id.repost_delete);

        mDeleteRepost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                post_item.setRePostId(0);

                updateRepostLayout();
            }
        });

        mRepostAuthorPhoto = (CircularImageView) rootView.findViewById(R.id.repost_author_photo_image);
        mRepostImage = (ImageView) rootView.findViewById(R.id.repost_image);

        mRepostAuthorTitle = (TextView) rootView.findViewById(R.id.repost_author_fullname_label);
        mRepostContent = (TextView) rootView.findViewById(R.id.repost_text);

        mViewRepostButton = (Button) rootView.findViewById(R.id.repost_view);

        mViewRepostButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ViewItemActivity.class);
                intent.putExtra("itemId", post_item.getRePostId());
                getActivity().startActivity(intent);
            }
        });

        //

        mImagesLayout = (LinearLayout) rootView.findViewById(R.id.images_layout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(itemsAdapter);

        itemsAdapter.setOnItemClickListener(new MediaListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, MediaItem obj, int position, int action) {

                if (action != 0) {
                    NewItemFragment.this.itemsList.remove(position);
                    NewItemFragment.this.itemsAdapter.notifyDataSetChanged();
                    NewItemFragment.this.updateMediaLayout();
                    boolean z = false;
                    if (NewItemFragment.this.itemsList.size() == 0) {
                        NewItemFragment.this.isSelectGif = false;
                        return;
                    }
                    int imageCount = 0;
                    int videoCount = 0;
                    int gifCount = 0;
                    Iterator<MediaItem> it = NewItemFragment.this.itemsList.iterator();
                    while (it.hasNext()) {
                        MediaItem mediaItem = it.next();
                        if (mediaItem.getType() == 0) {
                            imageCount++;
                        } else if (mediaItem.getType() == 1) {
                            videoCount++;
                        } else if (mediaItem.getType() == 2) {
                            gifCount++;
                        }
                    }
                    NewItemFragment newItemFragment = NewItemFragment.this;
                    if (gifCount > 0) {
                        z = true;
                    }
                    newItemFragment.isSelectGif = z;
                }
            }
        });

        //

        mAccessModeIcon = (ImageView) rootView.findViewById(R.id.access_mode_image);
        mAccessModeTitle = (TextView) rootView.findViewById(R.id.access_mode_label);
        mAccessModeLayout = (LinearLayout) rootView.findViewById(R.id.access_mode_layout);

        mAccessModeLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

                arrayAdapter.add(getText(R.string.label_post_to_public).toString());
                arrayAdapter.add(getText(R.string.label_post_to_friends).toString());

                builderSingle.setTitle(getText(R.string.label_post_to_dialog_title));


                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        post_item.setAccessMode(which);
                        ;

                        updateAccessMode();
                    }
                });

                builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                AlertDialog d = builderSingle.create();
                d.show();
            }
        });

        mLocationIcon = (ImageView) rootView.findViewById(R.id.location_image);
        mLocationTitle = (TextView) rootView.findViewById(R.id.location_label);
        mLocationLayout = (LinearLayout) rootView.findViewById(R.id.location_layout);

        mLocationLayout.setOnClickListener(v -> deleteLocation());

        mFeelingIcon = (ImageView) rootView.findViewById(R.id.feeling_image);
        mFeelingTitle = (TextView) rootView.findViewById(R.id.feeling_label);
        mFeelingLayout = (LinearLayout) rootView.findViewById(R.id.feeling_layout);

        mFeelingLayout.setOnClickListener(v -> deleteFeeling());

        mEmojiBtn = (ImageView) rootView.findViewById(R.id.emojiBtn);
        mEmojiBtn.setVisibility(View.GONE);

        mPostEdit = (EmojiconEditText) rootView.findViewById(R.id.postEdit);
        // TODO: 08/12/2019  ORIG BRX  mPostEdit.setText(item.getPost());

        // String mPost = Html.fromHtml(item.getPost(), Html.FROM_HTML_MODE_LEGACY).toString();
        String mPost;

       /* if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

             mPost = Html.fromHtml(item.getPost(), Html.FROM_HTML_MODE_LEGACY).toString();

        } else {

             mPost = htmlifyPlain(item.getPost());//Html.fromHtml(item.getPost()).toString();
        }
        */

        mPost = htmlifyPlain(post_item.getPost());

        Log.e("BRX", "mPost: " + mPost);

        mPostEdit.setText(mPost);

        //mPostEdit.setText(item.getPost().replace("<br><br>", "\n\n")); // TODO: 08/12/2019 FIXED SPACE ISSUE ON THE APP
        mPostEdit.setText(post_item.getPost().replace("<br>", "\n"));

        Log.e("BRX", "item.getPost(): " + Html.fromHtml(post_item.getPost()));

        mPostEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (isAdded()) {

                    if (hasFocus) {

                        //got focus

                        if (EMOJI_KEYBOARD) {

                            mEmojiBtn.setVisibility(View.VISIBLE);
                        }

                    } else {

                        mEmojiBtn.setVisibility(View.GONE);
                    }
                }
            }
        });

        setEditTextMaxLength(POST_CHARACTERS_LIMIT);

        mPostEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                // Log.e("BRX", "afterTextChanged: " + s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                int cnt = s.length();

                //   Log.e("BRX", "onTextChanged: " + s);

                if (cnt == 0) {

                    updateTitle();

                } else {

                    getActivity().setTitle(Integer.toString(POST_CHARACTERS_LIMIT - cnt));
                }
            }
        });

        mEmojiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!popup.isShowing()) {

                    if (popup.isKeyBoardOpen()) {

                        popup.showAtBottom();
                        setIconSoftKeyboard();

                    } else {

                        mPostEdit.setFocusableInTouchMode(true);
                        mPostEdit.requestFocus();
                        popup.showAtBottomPending();

                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mPostEdit, InputMethodManager.SHOW_IMPLICIT);
                        setIconSoftKeyboard();
                    }

                } else {

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

        mPostEdit.setOnEditTextImeBackListener(er);

        updateTitle();
        updateProfileInfo();
        updateAccessMode();
        updateLocation();
        updateFeeling();
        updateMediaLayout();
        updateRepostLayout();
        Intent intent = ((NewItemActivity) getContext()).getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            } else if (type.startsWith("video/")) {
                handleSendVideo(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {

        }
        // Inflate the layout for this fragment
        return rootView;
    }

    private static String htmlifyPlain(String textIn) {
        SpannableString spannable = SpannableString.valueOf(textIn);
        Linkify.addLinks(spannable, Linkify.WEB_URLS);
        return Html.toHtml(spannable);
    }

    private void updateTitle() {

        if (isAdded()) {

            if (post_item.getId() != 0) {

                getActivity().setTitle(getText(R.string.title_edit_item));

            } else {

                getActivity().setTitle(getText(R.string.title_new_item));
            }
        }
    }

    private void updateProfileInfo() {

        if (isAdded()) {

            if (App.getInstance().getPhotoUrl() != null && App.getInstance().getPhotoUrl().length() > 0) {

                App.getInstance().getImageLoader().get(App.getInstance().getPhotoUrl(), ImageLoader.getImageListener(mPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            } else {

                mPhoto.setImageResource(R.drawable.profile_default_photo);
            }

            mFullname.setText(App.getInstance().getFullname());
        }
    }

    private void updateAccessMode() {

        if (group_id != 0) {

            mAccessModeLayout.setVisibility(View.GONE);

        } else {

            mAccessModeLayout.setVisibility(View.VISIBLE);

            if (post_item.getAccessMode() == 0) {

                mAccessModeTitle.setText(getString(R.string.label_post_to_public));
                mAccessModeIcon.setImageResource(R.drawable.ic_unlock);

            } else {

                mAccessModeTitle.setText(getString(R.string.label_post_to_friends));
                mAccessModeIcon.setImageResource(R.drawable.ic_lock);
            }
        }
    }

    private void updateLocation() {

        String location = "";

        mLocationLayout.setVisibility(View.GONE);

        if (post_item.getCountry().length() > 0 || post_item.getCity().length() > 0) {

            if (post_item.getCountry().length() > 0) {

                location = post_item.getCountry();
            }

            if (post_item.getCity().length() > 0) {

                if (post_item.getCountry().length() > 0) {

                    location = location + ", " + post_item.getCity();

                } else {

                    location = post_item.getCity();
                }
            }

            if (location.length() > 0) {

                mLocationLayout.setVisibility(View.VISIBLE);
                mLocationTitle.setText(location);
            }
        }
    }

    public void setLocation() {

        post_item.setCountry(App.getInstance().getCountry());
        post_item.setCity(App.getInstance().getCity());

        post_item.setLat(App.getInstance().getLat());
        post_item.setLng(App.getInstance().getLng());

        updateLocation();
    }

    public void deleteLocation() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getText(R.string.dlg_delete_location_title));

        alertDialog.setMessage(getText(R.string.dlg_delete_location_subtitle));
        alertDialog.setCancelable(true);

        alertDialog.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                post_item.setCountry("");
                post_item.setCity("");

                post_item.setLat(0.000000);
                post_item.setLng(0.000000);

                updateLocation();
            }
        });

        alertDialog.show();
    }

    private void updateFeeling() {

        mFeelingLayout.setVisibility(View.GONE);

        if (post_item.getFeeling() != 0) {

            ImageLoader imageLoader = App.getInstance().getImageLoader();

            imageLoader.get(Constants.WEB_SITE + "feelings/" + Integer.toString(post_item.getFeeling()) + ".png", ImageLoader.getImageListener(mFeelingIcon, R.drawable.mood, R.drawable.mood));

            Log.e("BRX", "" + Constants.WEB_SITE + "feelings/" + post_item.getFeeling() + ".png");


            mFeelingLayout.setVisibility(View.VISIBLE);
        }
    }

    public void deleteFeeling() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

        arrayAdapter.add(getText(R.string.action_remove).toString());
        arrayAdapter.add(getText(R.string.action_edit).toString());

        builderSingle.setTitle(getText(R.string.dlg_delete_feeling_title));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case 0: {

                        post_item.setFeeling(0);

                        updateFeeling();

                        break;
                    }

                    default: {

                        choiceFeeling();

                        break;
                    }
                }
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void choiceFeeling() {

        choiceFeelingDialog();

//        Intent intent = new Intent(getActivity(), SelectFeelingActivity.class);
//        intent.putExtra("profileId", 0);
//        startActivityForResult(intent, ITEM_FEELINGS);
    }

    private void updateMediaLayout() {


        mImagesLayout.setVisibility(View.GONE);

        if (post_item.getId() != 0 && itemsAdapter.getItemCount() == 0) {

            if (post_item.getVideoUrl().length() > 0) {

                itemsList.add(new MediaItem("", "", post_item.getPreviewVideoImgUrl(), post_item.getVideoUrl(), 1));
            }

            if (post_item.getImgUrl().length() > 0) {

                itemsList.add(new MediaItem("", "", post_item.getImgUrl(), "", 0));
            }

            if (post_item.getImagesCount() != 0) {

                post_item.setImagesCount(0);

                getMediaItems();
            }

            post_item.setImgUrl("");
            post_item.setVideoUrl("");
            post_item.setPreviewVideoImgUrl("");
            post_item.getMediaList().clear();
        }

        if (itemsAdapter.getItemCount() > 0) {

            mImagesLayout.setVisibility(View.VISIBLE);

            itemsAdapter.notifyDataSetChanged();
        }
    }

    private void updateRepostLayout() {

        mRepostLayout.setVisibility(View.GONE);

        if (post_item.getRePostId() != 0) {

            mRepostLayout.setVisibility(View.VISIBLE);

            if (post_item.getRePostFromUserPhotoUrl().length() != 0) {

                App.getInstance().getImageLoader().get(post_item.getRePostFromUserPhotoUrl(), ImageLoader.getImageListener(mRepostAuthorPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            } else {

                mRepostAuthorPhoto.setImageResource(R.drawable.img_loading);
            }

            mRepostAuthorTitle.setText(post_item.getRePostFromUserFullname());

            if (post_item.getRePostImgUrl().length() != 0) {

                App.getInstance().getImageLoader().get(post_item.getRePostImgUrl(), ImageLoader.getImageListener(mRepostImage, R.drawable.img_loading, R.drawable.img_loading));

                mRepostImage.setVisibility(View.VISIBLE);

            } else {

                mRepostImage.setVisibility(View.GONE);
            }

            if (post_item.getRePostPost().length() != 0) {

                mRepostContent.setText(post_item.getRePostPost());

                mRepostContent.setVisibility(View.VISIBLE);

            } else {

                mRepostContent.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Check GPS is enabled
                    LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

                        mFusedLocationClient.getLastLocation().addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {

                                if (task.isSuccessful() && task.getResult() != null) {

                                    mLastLocation = task.getResult();

                                    // Set geo data to App class

                                    App.getInstance().setLat(mLastLocation.getLatitude());
                                    App.getInstance().setLng(mLastLocation.getLongitude());

                                    // Save data

                                    App.getInstance().saveData();

                                    // Get address

                                    App.getInstance().getAddress(App.getInstance().getLat(), App.getInstance().getLng());

                                    setLocation();

                                } else {

                                    Log.d("GPS", "New Item getLastLocation:exception", task.getException());
                                }
                            }
                        });
                    }

                    setLocation();

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) || !ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                        showNoLocationPermissionSnackbar();
                    }
                }

                return;
            }

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    choiceImageAction();

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        showNoStoragePermissionSnackbar();
                    }
                }

                return;
            }

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_VIDEO_IMAGE: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    choiceVideo();

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        showNoStoragePermissionSnackbar();
                    }
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void showNoStoragePermissionSnackbar() {

        Snackbar.make(getView(), getString(R.string.label_no_storage_permission), Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(getActivity(), getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
            }

        }).show();
    }

    public void showNoLocationPermissionSnackbar() {

        Snackbar.make(getView(), getString(R.string.label_no_location_permission), Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(getActivity(), getString(R.string.label_grant_location_permission), Toast.LENGTH_SHORT).show();
            }

        }).show();
    }

    public void openApplicationSettings() {

        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(appSettingsIntent, 10001);
    }

    public void setEditTextMaxLength(int length) {

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(length);
        mPostEdit.setFilters(FilterArray);
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

        hidepDialog();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(STATE_LIST, itemsList);
        outState.putParcelable("item", post_item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_post: {
                if (!isClickPost) {
                    isClickPost = true;


                    hideEmojiKeyboard();
                    String lang = translate.detect(mPostEdit.getText().toString()).getLanguage();
                    post_item.setPostLang(lang);

                    String mPost = txtToHtml(mPostEdit.getText().toString().trim());
                    //String mPost = mPostEdit.getText().toString().trim();

                    Log.e("BRX", "NewItemTextHtml:" + mPost);
                    this.post_item.setPost(mPost);

                    //this.item.setPost(mPostEdit.getText().toString());

                    // this.item.setPost(mPostEdit.getText().toString().trim().replace("<br>", "<br><br>"));

                    if (itemsList.size() == 0 && this.post_item.getRePostId() == 0 && this.post_item.getPost().length() == 0) {

                        Toast toast = Toast.makeText(getActivity(), getText(R.string.msg_enter_item), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                    } else {

                        this.loading = true;
                        Log.d("dialogshow", "onOptionsItemSelected");
                        Toast.makeText(getContext(), "Posting... please wait", 1).show();
                        if (this.isSelectGif || this.itemsList.size() == 0) {
                            sendPost();
                        } else {
                            uploadImages(0);
                            getActivity().finish();
                            return true;

                        }

/**
 AsyncTask.execute(new Runnable() {
@Override public void run() {

if (itemsList.size() != 0) {

uploadImages(0);

// return true;
} else {

sendPost();


}
//
}
});
 */
                        // Toast.makeText(getActivity(), "Posting... refresh feeds to show post", Toast.LENGTH_LONG).show();
                        // sharedPref.edit().putBoolean("posting", true).apply();
                        //getActivity().finish();

                        /**new Thread( new Runnable() { @Override public void run() {
                         // Run whatever background code you want here.

                         } } ).start();*/


                    }
                }
                return true;
            }

            default: {

                break;
            }
        }

        return false;
    }


    private void uploadImages(int index) {


        Log.e("uploadImages", "uploadImages:" + Integer.toString(index));

        if (itemsList.size() > 0) {

            if (index < itemsList.size()) {

                boolean need_upload = false;

                for (int i = 0; i < itemsList.size(); i++) {

                    if (itemsList.get(i).getImageUrl().length() == 0) {

                        need_upload = true;
                    }
                }

                if (need_upload) {


                    for (int i = index; i < itemsList.size(); i++) {

                        if (itemsList.get(i).getImageUrl().length() == 0) {

                            if (itemsList.get(i).getType() == 0) {

                                File f = new File(itemsList.get(i).getSelectedImageFileName());

                                uploadFile(METHOD_ITEMS_UPLOAD_IMG, f, i);
                            } else {

                                File f = new File(itemsList.get(i).getSelectedImageFileName());

                                File f2 = new File(itemsList.get(i).getSelectedVideoFileName());

                                uploadVideoFile(METHOD_VIDEO_UPLOAD, f, f2, i);
                            }

                            break;
                        }
                    }

                } else {

                    sendPost();
                }

            } else {

                sendPost();
            }

        } else {

            sendPost();
        }


    }


    public Bitmap resizeBitmap(String photoPath) {

        Log.e("Image", "resizeBitmap()");

        int targetW = 512;
        int targetH = 512;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;

        scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true; //Deprecated from  API 21

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

    public void save(String outFile, String inFile) {

        Log.e("Image", "save()");

        try {

            Bitmap bmp = resizeBitmap(outFile);

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, inFile);
            FileOutputStream fOut = new FileOutputStream(file);

            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();

        } catch (Exception ex) {

            Log.e("Image save() Exception", ex.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 203) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == -1) {
                Uri selectedImage = result.getUri();
                Log.e("BRXresultUri: ", String.valueOf(selectedImage));
                String newFileName = Helper.randomString(6) + ".jpg";
                save(getImageUrlWithAuthority(getActivity(), selectedImage, newFileName), newFileName);
                this.itemsList.add(new MediaItem(Environment.getExternalStorageDirectory() + File.separator + Constants.APP_TEMP_FOLDER + File.separator + newFileName, "", "", "", 0));
                this.itemsAdapter.notifyDataSetChanged();
                updateMediaLayout();
            } else if (resultCode == 204) {
                Log.e("BRXerror: ", String.valueOf(result.getError()));
            }
        }
        if (requestCode == 3 && resultCode == -1 && data != null) {
            String newFileName2 = Helper.randomString(6) + ".jpg";
            try {
                save(getImageUrlWithAuthority(getActivity(), data.getData(), newFileName2), newFileName2);
                Intent i = new Intent(getActivity(), PhotoEditorActivity.class);
                i.putExtra("type", UriUtil.LOCAL_FILE_SCHEME);
                i.putExtra("url", Environment.getExternalStorageDirectory() + File.separator + Constants.APP_TEMP_FOLDER + File.separator + newFileName2);
                startActivityForResult(i, 101);
            } catch (Exception e) {
                Log.e("OnSelectPostImage", e.getMessage());
            }
        } else if (requestCode != 101 || data == null || !data.hasExtra("returnpath")) {
            if (requestCode == 5) {
                getActivity();
                if (resultCode == -1) {

                    try {
                        String newFileName3 = Helper.randomString(6) + ".jpg";
                        save(Environment.getExternalStorageDirectory() + File.separator + Constants.APP_TEMP_FOLDER + File.separator + "camera.jpg", newFileName3);
                        this.itemsList.add(new MediaItem(Environment.getExternalStorageDirectory() + File.separator + Constants.APP_TEMP_FOLDER + File.separator + newFileName3, "", "", "", 0));
                        this.itemsAdapter.notifyDataSetChanged();
                        updateMediaLayout();
                        return;
                    } catch (Exception ex) {
                        Log.v("OnCameraCallBack", ex.getMessage());
                        return;
                    }
                }
            }
            if (requestCode == 77) {
                getActivity();
                if (resultCode == -1) {
                    setLocation();
                    return;
                }
            }
            if (requestCode == 1001) {
                getActivity();
                if (resultCode == -1) {
                    String selectedVideoFileName = getRealPathFromURI(data.getData());
                    if (new File(selectedVideoFileName).length() > StatFsHelper.DEFAULT_DISK_RED_LEVEL_IN_BYTES) {
                        Toast.makeText(getActivity(), getString(R.string.msg_video_too_large), 0).show();
                        return;
                    } else if (selectedVideoFileName != null) {
                        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(selectedVideoFileName, 1);
                        String newFileName4 = Helper.randomString(6) + ".jpg";
                        writeToTempImageAndGetPathUri(getActivity(), Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(), thumb.getHeight(), new Matrix(), true), newFileName4);
                        this.itemsList.add(0, new MediaItem(Environment.getExternalStorageDirectory() + File.separator + Constants.APP_TEMP_FOLDER + File.separator + newFileName4, selectedVideoFileName, "", "", 1));
                        this.itemsAdapter.notifyDataSetChanged();
                        updateMediaLayout();
                        return;
                    } else {
                        return;
                    }
                }
            }
            if (requestCode == 1 && resultCode == -1) {
                this.post_item.setFeeling(data.getIntExtra("feeling", 0));
                updateFeeling();
            }
        } else {
            this.itemsList.add(new MediaItem(data.getStringExtra("returnpath"), "", "", "", 0));
            this.itemsAdapter.notifyDataSetChanged();
            updateMediaLayout();
        }
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

    public static String writeToTempImageAndGetPathUri(Context inContext, Bitmap
            inImage, String fileName) {

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

    public void choiceVideo() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getActivity().getString(R.string.label_select_video)), REQUEST_TAKE_GALLERY_VIDEO);


    }

    public void choiceImageAction() {

//        CropImage.activity()
//                .start(getContext(), this);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getText(R.string.label_select_img)), SELECT_POST_IMG);
        Log.d("selectimg123", "opened");
/*
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

        arrayAdapter.add(getText(R.string.action_gallery).toString());
        //arrayAdapter.add(getText(R.string.action_camera).toString());

        builderSingle.setTitle(getText(R.string.dlg_choice_image_title));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(intent, getText(R.string.label_select_img)), SELECT_POST_IMG);
                    Log.d("selectimg123", "opened");

                    default: {

                        try {

                            File root = new File(Environment.getExternalStorageDirectory(), APP_TEMP_FOLDER);

                            if (!root.exists()) {

                                root.mkdirs();
                            }

                            File sdImageMainDirectory = new File(root, "camera.jpg");

                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", sdImageMainDirectory));
                            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivityForResult(cameraIntent, CREATE_POST_IMG);

                        } catch (Exception e) {

                            Log.e("Camera", "Error occured. Please try again later.");
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

    public void sendPost() {

        post_item.getMediaList().clear();
        post_item.setImgUrl("");
        post_item.setVideoUrl("");
        post_item.setPreviewVideoImgUrl("");
        post_item.setImagesCount(0);
        if (this.isSelectGif) {
            Log.v("ttt", this.itemsList.get(0).getImageUrl());
            this.post_item.setImgUrl(this.itemsList.get(0).getImageUrl());
            this.post_item.setImagesCount(0);
        } else if (itemsList.size() != 0) {

            for (int i = 0; i < itemsList.size(); i++) {

                if (itemsList.get(i).getType() == 0) {

                    if (post_item.getImgUrl().length() == 0) {

                        post_item.setImgUrl(itemsList.get(i).getImageUrl());

                    } else {

                        post_item.getMediaList().add(itemsList.get(i));
                        post_item.setImagesCount(post_item.getImagesCount() + 1);
                    }

                } else {

                    post_item.setVideoUrl(itemsList.get(i).getVideoUrl());
                    post_item.setPreviewVideoImgUrl(itemsList.get(i).getImageUrl());
                }
            }
        }


        if (this.post_item.getId() != 0) {

            savePost();

            Log.v("BRX", "savePost: ");

        } else {

            newPost();

            Log.v("BRX", "newPost: ");

        }

    }

    private void savePost() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_EDIT, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            savePostSuccess();

                            //Toast.makeText(getActivity(), "Posting... refresh feeds to show post", Toast.LENGTH_LONG).show();


                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                savePostSuccess();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("groupId", Long.toString(group_id));
                params.put("postId", Long.toString(post_item.getId()));
                params.put("rePostId", Long.toString(post_item.getRePostId()));
                params.put("postMode", Integer.toString(post_item.getAccessMode()));
                params.put("postText", post_item.getPost());
                Log.v("ttt", post_item.getPost());
                params.put("postImg", post_item.getImgUrl());
                params.put("postArea", post_item.getArea());
                params.put("postCountry", post_item.getCountry());
                params.put("postCity", post_item.getCity());
                params.put("postLat", Double.toString(post_item.getLat()));
                params.put("postLng", Double.toString(post_item.getLng()));
                params.put("postLang", post_item.getPostLang());

                params.put("feeling", Integer.toString(post_item.getFeeling()));
                if (!NewItemFragment.this.isSelectGif && NewItemFragment.this.post_item.getMediaList().size() != 0) {
                    Collections.reverse(NewItemFragment.this.post_item.getMediaList());
                    for (int i = 0; i < NewItemFragment.this.post_item.getMediaList().size(); i++) {
                        if (NewItemFragment.this.post_item.getMediaList().get(i).getType() == 0) {
                            params.put("images[" + i + "]", NewItemFragment.this.post_item.getMediaList().get(i).getImageUrl());
                        }
                    }
                }

/*
                if (post_item.getMediaList().size() != 0) {

                    Collections.reverse(post_item.getMediaList());

                    for (int i = 0; i < post_item.getMediaList().size(); i++) {

                        if (post_item.getMediaList().get(i).getType() == 0) {

                            params.put("images[" + i + "]", post_item.getMediaList().get(i).getImageUrl());
                        }
                    }
                }
*/

                params.put("videoImgUrl", post_item.getPreviewVideoImgUrl());
                params.put("videoUrl", post_item.getVideoUrl());

                Log.e("BRX", "editPost: " + post_item.getPost());

                return params;
            }
        };

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void savePostSuccess() {

        loading = false;

        hidepDialog();

        if (isAdded()) {

            Intent intent = new Intent();
            intent.setAction("ru.ifsoft.refreshfeed");
            //intent.putExtra("action","refresh");
            App.getInstance().getApplicationContext().sendBroadcast(intent);

            Intent i = new Intent();
            i.putExtra("item", post_item);
            i.putExtra("position", position);

            getActivity().setResult(RESULT_OK, i);


            Log.e("BRX", "Posting... refresh feeds to show post: " + post_item.getPost());

            //Toast.makeText(getActivity(), getText(R.string.msg_item_saved), Toast.LENGTH_SHORT).show();
            //Toast.makeText(getActivity(), "Posting... refresh feeds to show post", Toast.LENGTH_LONG).show();

            getActivity().finish();
        } else {
            Toast.makeText(getContext(), "there problem, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void newPost() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_NEW, null,
                response -> {
                    try {
                        if (!response.getBoolean("error")) {
                            Log.e("BRX", "response.getBoolean: " + !response.getBoolean("error"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        sendPostSuccess();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                sendPostSuccess();


//                     Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("groupId", Long.toString(group_id));
                params.put("rePostId", Long.toString(post_item.getRePostId()));
                params.put("postMode", Integer.toString(post_item.getAccessMode()));
                params.put("postText", post_item.getPost());
                params.put("postImg", post_item.getImgUrl());
                params.put("postArea", post_item.getArea());
                params.put("postCountry", post_item.getCountry());
                params.put("postCity", post_item.getCity());
                params.put("postLat", Double.toString(post_item.getLat()));
                params.put("postLng", Double.toString(post_item.getLng()));
                params.put("postLang", post_item.getPostLang());
                params.put("postLang", post_item.getPostLang());

                params.put("feeling", Integer.toString(post_item.getFeeling()));
                if (!NewItemFragment.this.isSelectGif && NewItemFragment.this.post_item.getMediaList().size() != 0) {
                    Collections.reverse(NewItemFragment.this.post_item.getMediaList());
                    for (int i = 0; i < NewItemFragment.this.post_item.getMediaList().size(); i++) {
                        if (NewItemFragment.this.post_item.getMediaList().get(i).getType() == 0) {
                            params.put("images[" + i + "]", NewItemFragment.this.post_item.getMediaList().get(i).getImageUrl());
                        }
                    }
                }

/*
                if (post_item.getMediaList().size() != 0) {

                    Collections.reverse(post_item.getMediaList());

                    for (int i = 0; i < post_item.getMediaList().size(); i++) {

                        if (post_item.getMediaList().get(i).getType() == 0) {

                            params.put("images[" + i + "]", post_item.getMediaList().get(i).getImageUrl());
                        }
                    }
                }
*/

                params.put("videoImgUrl", post_item.getPreviewVideoImgUrl());

                params.put("videoUrl", post_item.getVideoUrl());

                Log.e("BRX", "newPost: " + post_item.getPost());

                return params;
            }
        };

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }
    //sharedPref.edit().putBoolean("posting", true).apply();

    // TODO: 25/12/2019 NEW POST
    public void sendPostSuccess() {

        Intent intent = new Intent();
        intent.setAction("ru.ifsoft.refreshfeed");
        //intent.putExtra("action","refresh");
        App.getInstance().getApplicationContext().sendBroadcast(intent);

        Log.d("action123", "refresh");
        loading = false;

        hidepDialog();

        if (isAdded()) {

            Log.e("BRX", "newPost: ");

            Intent i = new Intent();

            getActivity().setResult(RESULT_OK, i);

            // Toast.makeText(getActivity(), getText(R.string.msg_item_posted), Toast.LENGTH_SHORT).show();
            // Toast.makeText(getActivity(),"Posting... refresh feeds to show post", Toast.LENGTH_LONG).show();

            getActivity().finish();

        }
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    @Override
    public void onDetach() {

        super.onDetach();
    }


    private void showBottomSheet() {

        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        if (App.getInstance().getCountry().length() == 0 && App.getInstance().getCity().length() == 0) {

            if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

                App.getInstance().getAddress(App.getInstance().getLat(), App.getInstance().getLng());
            }
        }

        final View view = getLayoutInflater().inflate(R.layout.item_editor_sheet_list, null);

        MaterialRippleLayout mAddImageButton = (MaterialRippleLayout) view.findViewById(R.id.add_image_button);
        MaterialRippleLayout mAddVideoButton = (MaterialRippleLayout) view.findViewById(R.id.add_video_button);
        MaterialRippleLayout mAddLocationButton = (MaterialRippleLayout) view.findViewById(R.id.add_location_button);
        MaterialRippleLayout mAddFeelingButton = (MaterialRippleLayout) view.findViewById(R.id.add_feeling_button);
        MaterialRippleLayout mAddGif = (MaterialRippleLayout) view.findViewById(R.id.addGif);


        mAddGif.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
            if (bottomSheetDialog != null) {
                bottomSheetDialog.dismiss();
            }

            for (MediaItem mediaItem : itemsList) {
                if(mediaItem.getType()==0||mediaItem.getType()==1){
                    Toast.makeText(requireContext(), "You can't post image & video with gif", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            giphyDialogFragment.show(requireActivity().getSupportFragmentManager(), "giphy");
        });
        mAddImageButton.setOnClickListener(view1 -> {
            if (isSelectGif) {
                Toast.makeText(requireContext(), "You can't choose image and gif in one post", Toast.LENGTH_LONG).show();
                return;
            }

            if (mBottomSheetDialog != null) {

                mBottomSheetDialog.dismiss();
            }

            if (getMediaCount(0) < IMAGE_FILES_LIMIT) {

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);

                    } else {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);
                    }

                } else {

                    choiceImageAction();
                }

            } else {

                Toast.makeText(getActivity(), String.format(Locale.getDefault(), getString(R.string.images_limit_of), IMAGE_FILES_LIMIT), Toast.LENGTH_SHORT).show();
            }
        });

        mAddVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSelectGif) {
                    Toast.makeText(requireContext(), "You can't choose video and gif in one post", Toast.LENGTH_LONG).show();
                    return;
                }

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                if (getMediaCount(1) < VIDEO_FILES_LIMIT) {

                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_VIDEO_IMAGE);

                        } else {

                            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_VIDEO_IMAGE);
                        }

                    } else {

                        choiceVideo();
                    }

                } else {

                    Toast.makeText(getActivity(), String.format(Locale.getDefault(), getString(R.string.video_limit_of), VIDEO_FILES_LIMIT), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAddLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                if (App.getInstance().getCountry().length() != 0 || App.getInstance().getCity().length() != 0) {

                    setLocation();

                } else {

                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                        } else {

                            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
                        }

                    } else {

                        Intent i = new Intent(getActivity(), LocationActivity.class);
                        startActivityForResult(i, 77);
                    }
                }
            }
        });

        mAddFeelingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                choiceFeeling();
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(getActivity());

        mBottomSheetDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();

        doKeepDialog(mBottomSheetDialog);

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                mBottomSheetDialog = null;
            }
        });
    }

    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog) {

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }


    private int getMediaCount(int type) {

        int count = 0;

        if (itemsList.size() > 0) {

            for (int i = 0; i < itemsList.size(); i++) {

                if (itemsList.get(i).getType() == type) {

                    count++;
                }
            }
        }

        return count;
    }


    public Boolean uploadFile(String serverURL, File file, final int index) {

        Log.e("uploadFile", "index:" + Integer.toString(index));

        final OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(30, TimeUnit.SECONDS);

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

                    Log.e("failure", request.toString());
                    Toast.makeText(getContext(), "there problem, please try again", Toast.LENGTH_SHORT).show();

                    Log.v("ttt", e.getLocalizedMessage() + "");
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {

                    String jsonData = response.body().string();

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            itemsList.get(index).setImageUrl(result.getString("imgUrl"));
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        Log.e("response", jsonData);

                        uploadImages(index);
                    }
                }
            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            loading = false;

            hidepDialog();
        }

        return false;
    }

    private final String CHANNEL_ID = "personal_notification";


    public Boolean uploadVideoFile(String serverURL, File file, File videoFile, final int index) {


        final int random = new Random().nextInt(999999) + 1;
        createNotificationChannel();
        String appName = Objects.requireNonNull(getActivity()).getResources().getString(R.string.app_name);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(Objects.requireNonNull(getContext()), CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_upload);
        builder.setContentTitle(appName + " upload (0%)");
        builder.setContentText("Posting... notify you when finish");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        final int max_progress = 100;
        int current_progress = 0;
        builder.setProgress(max_progress, current_progress, false);
        final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getContext());
        notificationManagerCompat.notify(random, builder.build());


        final okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();

        //client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        try {

//            RequestBody requestBody = new MultipartBuilder()
//                    .type(MultipartBuilder.FORM)
//                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
//                    .addFormDataPart("uploaded_video_file", videoFile.getName(), RequestBody.create(MediaType.parse("text/csv"), videoFile))
//                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
//                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
//                    .build();

            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uploaded_file", videoFile.getName(), okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/csv"), file))
                    .addFormDataPart("uploaded_video_file", videoFile.getName(), okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/csv"), videoFile))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            CountingRequestBody countingBody = new CountingRequestBody(requestBody, (bytesWritten, totalSize) -> {
                float percentage = 100f * bytesWritten / videoFile.length();
                if (percentage > 100)
                    percentage = 99;
                Log.d("uploadingprogress", percentage + "");
                builder.setContentTitle(appName + " upload (" + (int) percentage + "%)");
                builder.setProgress(max_progress, (int) percentage, false);
                notificationManagerCompat.notify(random, builder.build());

            }, videoFile.length(), () -> false

            );


            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(countingBody)
                    .build();

//            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
//                    .url(serverURL)
//                    .addHeader("Accept", "application/json;")
//                    .post(requestBody)
//                    .build()

            client.newCall(request).enqueue(new okhttp3.Callback() {


                @Override
                public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                    String jsonData = response.body().string();

                    Log.e("response", jsonData);

                    try {


                        builder.setContentText("Post uploaded successfully");
                        builder.setProgress(0, 0, false);
                        notificationManagerCompat.notify(random, builder.build());

                        JSONObject result = new JSONObject(jsonData);
                        Log.d("uploaded", "done");

                        if (!result.getBoolean("error")) {

                            itemsList.get(index).setImageUrl(result.getString("imgFileUrl"));
                            itemsList.get(index).setVideoUrl(result.getString("videoFileUrl"));
                        }

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {
                        uploadImages(index);
                    }

                }

                @Override
                public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                    loading = false;

                    hidepDialog();

                    builder.setContentText("Post failed to upload");
                    builder.setProgress(0, 0, false);
                    notificationManagerCompat.notify(random, builder.build());
                    Log.e("failure", e.toString());
                }


            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            loading = false;

            hidepDialog();
        }

        return false;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Personal Notification";
            String description = "Include all the personal notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);

            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

        }
    }

    public void getMediaItems() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEM_GET_IMAGES, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "NewItemFragment Not Added to Activity");

                            return;
                        }

                        try {

                            int arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            MediaItem item = new MediaItem();

                                            item.setImageUrl(itemObj.getString("imgUrl"));
                                            item.setType(0);

                                            itemsList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            updateMediaLayout();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "NewItemFragment Not Added to Activity");

                    return;
                }

                updateMediaLayout();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(post_item.getId()));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    private void choiceFeelingDialog() {

        final FeelingsListAdapter feelingsAdapter;

        feelingsAdapter = new FeelingsListAdapter(getActivity(), App.getInstance().getFeelingsList());

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_feelings);
        dialog.setCancelable(true);

        final ProgressBar mProgressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        TextView mDlgTitle = (TextView) dialog.findViewById(R.id.title_label);
        mDlgTitle.setText(R.string.dlg_choice_feeling_title);

        AppCompatButton mDlgCancelButton = (AppCompatButton) dialog.findViewById(R.id.cancel_button);
        mDlgCancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        NestedScrollView mDlgNestedView = (NestedScrollView) dialog.findViewById(R.id.nested_view);
        final RecyclerView mDlgRecyclerView = (RecyclerView) dialog.findViewById(R.id.recycler_view);

        final LinearLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Helper.getStickersGridSpanCount(getActivity()));
        mDlgRecyclerView.setLayoutManager(mLayoutManager);
        mDlgRecyclerView.setHasFixedSize(true);
        mDlgRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mDlgRecyclerView.setAdapter(feelingsAdapter);

        mDlgRecyclerView.setNestedScrollingEnabled(true);

        feelingsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {

                super.onChanged();

                if (App.getInstance().getFeelingsList().size() != 0) {

                    mDlgRecyclerView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        feelingsAdapter.setOnItemClickListener(new FeelingsListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Feeling obj, int position) {

                post_item.setFeeling(position);

                updateFeeling();

                dialog.dismiss();
            }
        });

        if (App.getInstance().getFeelingsList().size() == 0) {

            mDlgRecyclerView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);

            Api api = new Api(getActivity());
            api.getFeelings(feelingsAdapter);
        }

        dialog.show();

        doKeepDialog(dialog);
    }

    // TODO: 11/12/2019 BRX TXT TO HTML
    private static String txtToHtml(String s) {
        StringBuilder builder = new StringBuilder();
        boolean previousWasASpace = false;
        for (char c : s.toCharArray()) {
            if (c == ' ') {
                if (previousWasASpace) {
                    builder.append("&nbsp;");
                    previousWasASpace = false;
                    continue;
                }
                previousWasASpace = true;
            } else {
                previousWasASpace = false;
            }
            switch (c) {
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '&':
                    builder.append("&amp;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\n':
                    builder.append("<br>");
                    break;
                // We need Tab support here, because we print StackTraces as HTML
                case '\t':
                    builder.append("&nbsp; &nbsp; &nbsp;");
                    break;
                default:
                    builder.append(c);

            }
        }
        String converted = builder.toString();
        String str = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>?«»“”‘’]))";
        Pattern patt = Pattern.compile(str);
        Matcher matcher = patt.matcher(converted);
        converted = matcher.replaceAll("<a href=\"$1\">$1</a>");
        return converted;
    }

    public String parseXml(String s) {
        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(s)); // pass input whatever xml you have
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d(TAG, "Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    Log.d(TAG, "Start tag " + xpp.getName());
                } else if (eventType == XmlPullParser.END_TAG) {
                    Log.d(TAG, "End tag " + xpp.getName());
                } else if (eventType == XmlPullParser.TEXT) {
                    Log.d(TAG, "Text " + xpp.getText()); // here you get the text from xml
                    return xpp.getText();
                }
                eventType = xpp.next();
            }
            Log.d(TAG, "End document");

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void getTranslateService() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = getResources().openRawResource(R.raw.credintel)) {

            //Get credentials:
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            Log.v("ttt", ioe.getLocalizedMessage());
        }
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {


            String newFileName = Helper.randomString(6) + ".jpg";

            String selectedImageFile = getImageUrlWithAuthority(getActivity(), imageUri, newFileName);

            try {

                save(selectedImageFile, newFileName);
                selectedImageFile = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + newFileName;
                Intent i = new Intent(getActivity(), PhotoEditorActivity.class);
                //i.putExtra("type", PhotoEditorActivity.ImageType.LOCAL);
                i.putExtra("type", "file");
                i.putExtra("url", selectedImageFile);
                startActivityForResult(i, 101);
            } catch (Exception e) {

                Log.e("OnSelectPostImage", e.getMessage());

            }
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            for (Uri uri : imageUris) {

                String newFileName = Helper.randomString(6) + ".jpg";

                String selectedImageFile = getImageUrlWithAuthority(getActivity(), uri, newFileName);

                try {

                    save(selectedImageFile, newFileName);
                    selectedImageFile = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + newFileName;
                    Intent i = new Intent(getActivity(), PhotoEditorActivity.class);
                    //i.putExtra("type", PhotoEditorActivity.ImageType.LOCAL);
                    i.putExtra("type", "file");
                    i.putExtra("url", selectedImageFile);
                    startActivityForResult(i, 101);
                    itemsList.add(new MediaItem(selectedImageFile, "", "", "", 0));
                    itemsAdapter.notifyDataSetChanged();

                    updateMediaLayout();


                } catch (Exception e) {

                    Log.e("OnSelectPostImage", e.getMessage());

                }

            }
        }
    }

    private void handleSendVideo(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String selectedVideoFileName = getRealPathFromURI(imageUri);
        Log.v("ttt", selectedVideoFileName);

        File videoFile = new File(selectedVideoFileName);


        if (videoFile.length() > VIDEO_FILE_MAX_SIZE) {

            Toast.makeText(getActivity(), getString(R.string.msg_video_too_large), Toast.LENGTH_SHORT).show();

        } else {

            if (selectedVideoFileName != null) {

                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(selectedVideoFileName, MediaStore.Images.Thumbnails.MINI_KIND);
                Matrix matrix = new Matrix();
                Bitmap bmThumbnail = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(), thumb.getHeight(), matrix, true);

                String newFileName = Helper.randomString(6) + ".jpg";
                String selectedImageFile;

                writeToTempImageAndGetPathUri(getActivity(), bmThumbnail, newFileName);

                selectedImageFile = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + newFileName;
                if (itemsList != null) {
                    itemsList.add(new MediaItem(selectedImageFile, selectedVideoFileName, "", "", 1));
                    itemsAdapter.notifyDataSetChanged();
                } else {
                    itemsList.add(new MediaItem(selectedImageFile, selectedVideoFileName, "", "", 1));
                    itemsAdapter.notifyDataSetChanged();

                }
                updateMediaLayout();
            }
        }
    }

    @Override // com.giphy.sdk.ui.views.GiphyDialogFragment.GifSelectionListener
    public void didSearchTerm(String s) {
    }

    @Override // com.giphy.sdk.ui.views.GiphyDialogFragment.GifSelectionListener
    public void onDismissed(GPHContentType gphContentType) {
    }

    @Override // com.giphy.sdk.ui.views.GiphyDialogFragment.GifSelectionListener
    public void onGifSelected(Media media, String s, GPHContentType gphContentType) {
        this.itemsList.add(new MediaItem("", "", getGifUrl(media.getId()), "", 2));
        this.itemsAdapter.notifyDataSetChanged();
        updateMediaLayout();
        isSelectGif = true;
    }

    public String getGifUrl(String id) {
        return "https://i.giphy.com/media/" + id + "/giphy.gif";
    }
}