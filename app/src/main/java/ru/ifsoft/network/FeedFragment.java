package ru.ifsoft.network;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.irozon.sneaker.Sneaker;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.ifsoft.network.adapter.AdvancedItemListAdapter;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;
import ru.ifsoft.network.db.Session;
import ru.ifsoft.network.model.Item;
import ru.ifsoft.network.util.Api;
import ru.ifsoft.network.util.CustomRequest;
import ru.ifsoft.network.view.video.VideoController;

import static com.facebook.FacebookSdk.getApplicationContext;
import static ru.ifsoft.network.GroupSettingsFragment.RESULT_OK;

public class FeedFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {
    private static final String STATE_LIST = "State Adapter Data";
    Receiver receiver;
    private int mToolbarOffset = 0;
    private int mToolbarHeight;

    public static long time = 0;
    private static final int PROFILE_NEW_POST = 4;
    boolean mIsLoading;
    private CardView mNewItemBox;
    private MaterialRippleLayout mNewItemButton;
    private CircularImageView mNewItemImage;
    private TextView mNewItemTitle,tvStream,tvInviteFriends;

    private RecyclerView mRecyclerView;
    private NestedScrollView mNestedView;

    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View mBottomSheet;

    private TextView mDesc;
    private TextView mMessage;
    private ImageView mSplash;

    private SwipeRefreshLayout mItemsContainer;

    private ArrayList<Item> itemsList;
    private AdvancedItemListAdapter itemsAdapter;

    private int itemId = 0;
    private int arrayLength = 0;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;
    private Boolean restore = false;
    private Boolean loaded = false;

    ProgressBar pbLoad;

    private SharedPreferences sharedPref;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        sharedPref = getActivity().getSharedPreferences("posting", Context.MODE_PRIVATE);

        /*if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new AdvancedItemListAdapter(getActivity(), itemsList,0);
            restore = savedInstanceState.getBoolean("restore");
            loaded = savedInstanceState.getBoolean("loaded");
            itemId = savedInstanceState.getInt("itemId");

        } else {*/

            itemsList = new ArrayList<>();
            itemsAdapter = new AdvancedItemListAdapter(getActivity(), itemsList,0);

            restore = false;
            loaded = false;
            itemId = 0;
//        }

        IntentFilter filter = new IntentFilter("ru.ifsoft.refreshfeed");
        receiver = new Receiver();
        getActivity().registerReceiver(receiver, filter);
    }


    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Session.getInstance(getContext()).getLocalSave().clearPost();
            itemId = 0;

            getItems();
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_feed, container,
                false);

        mItemsContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.container_items);
        mItemsContainer.setOnRefreshListener(this);
        mItemsContainer.setColorSchemeResources(
                R.color.green,
                R.color.green,
                R.color.green);
        //

        mDesc = rootView.findViewById(R.id.desc);
        mMessage = rootView.findViewById(R.id.message);
        mSplash = rootView.findViewById(R.id.splash);
        pbLoad = rootView.findViewById(R.id.pbLoad);
        pbLoad.getLayoutParams().height = 0;
        pbLoad.getLayoutParams().width = 0;
        pbLoad.setVisibility(View.INVISIBLE);
        mDesc.setVisibility(View.GONE);

        // Prepare bottom sheet

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);

        // New item spotlight

        mNewItemBox = (CardView) rootView.findViewById(R.id.newItemBox);
        mToolbarHeight = mNewItemBox.getHeight();

        mNewItemButton = (MaterialRippleLayout) rootView.findViewById(R.id.newItemButton);

        if (!loaded) mNewItemBox.setVisibility(View.GONE);

        mNewItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                showPostDialog();

                Intent intent = new Intent(getActivity(), NewItemActivity.class);
                startActivityForResult(intent, FEED_NEW_POST);
            }
        });

        mNewItemImage = (CircularImageView) rootView.findViewById(R.id.newItemImage);
        mNewItemTitle = (TextView) rootView.findViewById(R.id.newItemTitle);
        tvStream = rootView.findViewById(R.id.tvChackPost);
        tvInviteFriends = rootView.findViewById(R.id.tvInviteFriends);

        tvStream.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(),StreamActivity.class));
        });
        tvInviteFriends.setOnClickListener(v -> {
            getSharableLink();
        });

        updateProfileInfo();

        //

        mNestedView = (NestedScrollView) rootView.findViewById(R.id.nested_view);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        itemsAdapter.setOnMoreButtonClickListener((v, obj, actionId, position) -> {

            switch (actionId) {

                case ITEM_ACTION_REPOST: {

                    if (obj.getFromUserId() != App.getInstance().getId()) {

                        if (obj.getRePostFromUserId() != App.getInstance().getId()) {

                            repost(position);

                        } else {

                            Toast.makeText(getActivity(), getActivity().getString(R.string.msg_not_make_repost), Toast.LENGTH_SHORT).show();
                        }

                    } else {

                        Toast.makeText(getActivity(), getActivity().getString(R.string.msg_not_make_repost), Toast.LENGTH_SHORT).show();
                    }

                    break;
                }

                case ITEM_ACTIONS_MENU: {

                    showItemActionDialog(position);

                    break;
                }
            }
        });

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(itemsAdapter);

        mRecyclerView.setNestedScrollingEnabled(true);

//        mRecyclerView.setOnScrollListener(mScrollListener);

        mNestedView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

            if (scrollY < oldScrollY) { // up


            }


            if (itemsAdapter.videoIsClicked != -1) {

                if (mLayoutManager.getChildAt(itemsAdapter.videoIsClicked).getY() + 2000 < scrollY || mLayoutManager.getChildAt(itemsAdapter.videoIsClicked).getY() - 2000 > scrollY) {
                    stopVideoPlayback();
                }
                Log.v("tttt", scrollY + "");

            }
            if (scrollY > oldScrollY) { // down


            }

            if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {

                if (!loadingMore && (viewMore) && !(mItemsContainer.isRefreshing())) {

                    loadingMore = true;

                    getPaginationItems();
                }
            }
        });


        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        return rootView;
    }

    LinearLayoutManager mLayoutManager = null;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {

            if (loaded) updateProfileInfo();

            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (isAdded()) {

                        if (!loaded) {

                            showMessage(getText(R.string.msg_loading_2).toString());

                            getItems();
                        }
                    }
                }
            }, 50);
        }
    }

    private void updateProfileInfo() {

        if (isAdded()) {

            if (App.getInstance().getPhotoUrl() != null && App.getInstance().getPhotoUrl().length() > 0) {

                App.getInstance().getImageLoader().get(App.getInstance().getPhotoUrl(), ImageLoader.getImageListener(mNewItemImage, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            } else {

                mNewItemImage.setImageResource(R.drawable.profile_default_photo);
            }

            if (App.getInstance().getFullname().length() != 0) {

                SpannableStringBuilder txt = new SpannableStringBuilder(String.format(getString(R.string.msg_new_item_promo), App.getInstance().getFullname()));
                txt.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, App.getInstance().getFullname().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                mNewItemTitle.setText(txt);

            } else {

                SpannableStringBuilder txt = new SpannableStringBuilder(String.format(getString(R.string.msg_new_item_promo), "Hi"));

                mNewItemTitle.setText(txt);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putBoolean("loaded", loaded);
        outState.putInt("itemId", itemId);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {


            itemId = 0;
            getItems();
        } else {

            mItemsContainer.setRefreshing(false);
        }
    }

    public void refresh() {
        if (App.getInstance().isConnected()) {
            Log.d("refresh123", "called");
            itemId = 0;
            itemsList.clear();
            itemsAdapter.notifyDataSetChanged();
            getItems();
            mRecyclerView.scrollToPosition(0);

        } else {

            mItemsContainer.setRefreshing(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FEED_NEW_POST && resultCode == RESULT_OK && null != data) {
            itemId = 0;
            getItems();
//            showPostSuccessMessage("Posting... please check back");

        } else if (requestCode == ITEM_EDIT && resultCode == RESULT_OK) {

            int position = data.getIntExtra("position", 0);

            if (data.getExtras() != null) {


                Item item = (Item) data.getExtras().getParcelable("item");

                itemsList.set(position, item);


            }

            itemsAdapter.notifyDataSetChanged();

        } else if (requestCode == ITEM_REPOST && resultCode == getActivity().RESULT_OK) {

            int position = data.getIntExtra("position", 0);

            Item item = itemsList.get(position);

            item.setMyRePost(true);
            item.setRePostsCount(item.getRePostsCount() + 1);

            itemsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void showAlertPost() {


    }


    void showPostSuccessMessage(String message) {

        Sneaker.with(getActivity()).setTitle("Success!!", R.color.white) // Title and title color
                .setMessage(message, R.color.white) // Message and message color
                .setDuration(3000) // Time duration to show
                .autoHide(true) // Auto hide Sneaker view
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT) // Height of the Sneaker layout
                // .setIcon(R.drawable.ic_lau, R.color.white, false) // Icon, icon tint color and circular icon view
                //.setTypeface(Typeface.createFromAsset(this.getAssets(), "font/" + fontName)); // Custom font for title and message
                // .setOnSneakerClickListener(getActivity()) // Click listener for Sneaker
                //    .setOnSneakerDismissListener(this) // Dismiss listener for Sneaker. - Version 1.0.2
                //.setCornerRadius(radius, margin) // Radius and margin for round corner Sneaker. - Version 1.0.2
                .sneakSuccess();
        // .sneak(R.color.colorAccent); // Sneak with background color

        //getItems();

        //itemsAdapter.notifyDataSetChanged();

        //sharedPref.edit().putBoolean("posting", false).apply();

    }

    public void getPaginationItems() {
        pbLoad.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        pbLoad.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        pbLoad.setVisibility(View.VISIBLE);
        mIsLoading = true;

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_FEEDS_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        if (response.has("viewsCount")) {
//                            try {
//                                Log.d("viewsCount123", response.getString("viewsCount"));
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "FeedFragment Not Added to Activity");

                            return;
                        }

                        if (!loadingMore) {

                            itemsList.clear();
                        }

                        try {

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                itemId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");
                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);
//                                            Log.d("itemslog",itemObj.toString());

                                            Item item = new Item(itemObj);
//                                            if(itemObj.has("VideoViewsCount")){
//                                                Log.d("itemdata", item.getId() + "--" + itemObj.get("VideoViewsCount"));
//                                            }
                                            item.setAd(0);

                                            itemsList.add(item);


                                            // Ad after first item
                                            if (i == MY_AD_AFTER_ITEM_NUMBER && App.getInstance().getAdmob() == ENABLED) {

                                                Item ad = new Item(itemObj);

                                                ad.setAd(1);

                                                itemsList.add(ad);
                                            }
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            Log.e("ttt", e.getLocalizedMessage());
                        } finally {

                            loadingComplete();

                            pbLoad.getLayoutParams().height = 0;
                            pbLoad.getLayoutParams().width = 0;
                            pbLoad.setVisibility(View.INVISIBLE);

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "FeedFragment Not Added to Activity");

                    return;
                }

//                Log.e("ttt",error.getLocalizedMessage());
                loadingComplete();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Integer.toString(itemId));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getItems() {
        mIsLoading = true;
        mItemsContainer.setRefreshing(true);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_FEEDS_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        if (response.has("viewsCount")) {
//                            try {
//                                Log.d("viewsCount123", response.getString("viewsCount"));
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "FeedFragment Not Added to Activity");

                            return;
                        }

                        if (!loadingMore) {

                            itemsList.clear();
                        }

                        try {

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                itemId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");
                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);
//                                            Log.d("itemslog",itemObj.toString());

                                            Item item = new Item(itemObj);
//                                            if(itemObj.has("VideoViewsCount")){
//                                                Log.d("itemdata", item.getId() + "--" + itemObj.get("VideoViewsCount"));
//                                            }
                                            item.setAd(0);

                                            itemsList.add(item);


                                            // Ad after first item
                                            if (i == MY_AD_AFTER_ITEM_NUMBER && App.getInstance().getAdmob() == ENABLED) {

                                                Item ad = new Item(itemObj);

                                                ad.setAd(1);

                                                itemsList.add(ad);
                                            }
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            Log.e("ttt", e.getLocalizedMessage());
                        } finally {

                            loadingComplete();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "FeedFragment Not Added to Activity");

                    return;
                }

//                Log.e("ttt",error.getLocalizedMessage());
                loadingComplete();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Integer.toString(itemId));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void loadingComplete() {
        this.loaded = true;
        this.mIsLoading = false;
        this.mNewItemBox.setVisibility(View.VISIBLE);
        if (this.arrayLength == 20) {
            this.viewMore = true;
        } else {
            this.viewMore = false;
        }
        this.itemsAdapter.notifyDataSetChanged();
        if (itemsAdapter.getItemCount() != 0) {
            this.tvInviteFriends.setVisibility(View.GONE);
            this.tvStream.setVisibility(View.GONE);
            hideMessage();
        } else if (isVisible()) {
            showMessage(getText(R.string.label_empty_list).toString());
            this.tvInviteFriends.setVisibility(View.VISIBLE);
            this.tvStream.setVisibility(View.VISIBLE);
        } else {
            this.tvInviteFriends.setVisibility(View.VISIBLE);
            this.tvStream.setVisibility(View.VISIBLE);
        }
        this.loadingMore = false;
        this.mItemsContainer.setRefreshing(false);

/*
        loaded = true;

        mIsLoading = false;
        mNewItemBox.setVisibility(View.VISIBLE);

        if (arrayLength == LIST_ITEMS) {

            viewMore = true;

        } else {

            viewMore = false;
        }

        itemsAdapter.notifyDataSetChanged();

        if (itemsAdapter.getItemCount() == 0) {

            if (FeedFragment.this.isVisible()) {
                showMessage(getText(R.string.label_empty_list).toString());
                tvInviteFriends.setVisibility(View.VISIBLE);
                tvStream.setVisibility(View.VISIBLE);
            }else {
                tvInviteFriends.setVisibility(View.GONE);
                tvStream.setVisibility(View.GONE);
            }

        } else {
            tvInviteFriends.setVisibility(View.GONE);
            tvStream.setVisibility(View.GONE);

            hideMessage();
        }

        loadingMore = false;
        mItemsContainer.setRefreshing(false);
*/
    }

    // Item action


    private void showItemActionDialog(final int position) {

        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.item_action_sheet_list, null);

        MaterialRippleLayout mEditButton = (MaterialRippleLayout) view.findViewById(R.id.edit_button);
        MaterialRippleLayout mDeleteButton = (MaterialRippleLayout) view.findViewById(R.id.delete_button);
        MaterialRippleLayout mShareButton = (MaterialRippleLayout) view.findViewById(R.id.share_button);
        MaterialRippleLayout mRepostButton = (MaterialRippleLayout) view.findViewById(R.id.repost_button);
        MaterialRippleLayout mReportButton = (MaterialRippleLayout) view.findViewById(R.id.report_button);
        MaterialRippleLayout mOpenUrlButton = (MaterialRippleLayout) view.findViewById(R.id.open_url_button);
        MaterialRippleLayout mCopyUrlButton = (MaterialRippleLayout) view.findViewById(R.id.copy_url_button);

        if (!WEB_SITE_AVAILABLE) {

            mOpenUrlButton.setVisibility(View.GONE);
            mCopyUrlButton.setVisibility(View.GONE);
        }

        final Item item = itemsList.get(position);

        if (item.getFromUserId() == App.getInstance().getId()) {

            mEditButton.setVisibility(View.GONE);

            if (item.getPostType() == POST_TYPE_DEFAULT) {

                mEditButton.setVisibility(View.VISIBLE);
            }

            mDeleteButton.setVisibility(View.VISIBLE);

            mRepostButton.setVisibility(View.GONE);
            mReportButton.setVisibility(View.GONE);

        } else {

            mEditButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.GONE);

            mRepostButton.setVisibility(View.VISIBLE);
            mReportButton.setVisibility(View.VISIBLE);
        }

        mEditButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                // TODO: 08/12/2019 EDITING POST FROM MAIN FEED

                Intent i = new Intent(getActivity(), NewItemActivity.class);
                i.putExtra("item", item);
                i.putExtra("position", position);
                startActivityForResult(i, ITEM_EDIT);
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                delete(position);
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                share(position);
            }
        });

        mRepostButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                repost(position);
            }
        });

        mReportButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                report(position);
            }
        });

        mCopyUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("post url", item.getLink());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getActivity(), getText(R.string.msg_post_link_copied), Toast.LENGTH_SHORT).show();
            }
        });

        mOpenUrlButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(item.getLink()));
                startActivity(i);
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

    public void delete(final int position) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getText(R.string.label_delete));

        alertDialog.setMessage(getText(R.string.label_delete_msg));
        alertDialog.setCancelable(true);

        alertDialog.setNegativeButton(getText(R.string.action_no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                final Item item = itemsList.get(position);

                itemsList.remove(position);
                itemsAdapter.notifyDataSetChanged();

                if (itemsAdapter.getItemCount() == 0) {

                    showMessage(getText(R.string.label_empty_list).toString());
                }

                if (App.getInstance().isConnected()) {

                    Api api = new Api(getActivity());

                    api.postDelete(item.getId());

                } else {

                    Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.show();
    }

    public void share(final int position) {

        final Item item = itemsList.get(position);

        Api api = new Api(getActivity());
        api.postShare(item);
    }

    public void repost(final int position) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getText(R.string.label_post_share));

        alertDialog.setMessage(getText(R.string.label_post_share_desc));
        alertDialog.setCancelable(true);

        alertDialog.setNegativeButton(getText(R.string.action_no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                Item item = itemsList.get(position);

                Intent i = new Intent(getActivity(), NewItemActivity.class);
                i.putExtra("position", position);
                i.putExtra("repost", item);
                startActivityForResult(i, ITEM_REPOST);
            }
        });

        alertDialog.show();
    }

    public void report(final int position) {

        String[] profile_report_categories = new String[]{

                getText(R.string.label_profile_report_0).toString(),
                getText(R.string.label_profile_report_1).toString(),
                getText(R.string.label_profile_report_2).toString(),
                getText(R.string.label_profile_report_3).toString(),

        };

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getText(R.string.label_post_report_title));

        alertDialog.setSingleChoiceItems(profile_report_categories, 0, null);
        alertDialog.setCancelable(true);

        alertDialog.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                AlertDialog alert = (AlertDialog) dialog;
                int reason = alert.getListView().getCheckedItemPosition();

                final Item item = itemsList.get(position);

                Api api = new Api(getActivity());

                api.newReport(item.getId(), REPORT_TYPE_ITEM, reason);

                Toast.makeText(getActivity(), getActivity().getString(R.string.label_post_reported), Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }


    //

    public void showMessage(String message) {

        if (loaded) {

            mDesc.setVisibility(View.VISIBLE);

        } else {

            mDesc.setVisibility(View.GONE);
        }

        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);

        mSplash.setVisibility(View.VISIBLE);
    }

    public void hideMessage() {

        mDesc.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
        tvInviteFriends.setVisibility(View.GONE);
        tvStream.setVisibility(View.GONE);
        mSplash.setVisibility(View.GONE);
    }

    private void showPostDialog() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_post);
        dialog.setCancelable(true);

        NestedScrollView mDlgNestedView = (NestedScrollView) dialog.findViewById(R.id.nested_view);
        RecyclerView mDlgRecyclerView = (RecyclerView) dialog.findViewById(R.id.recycler_view);

        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        mDlgRecyclerView.setLayoutManager(mLayoutManager);

        mDlgRecyclerView.setAdapter(itemsAdapter);

        mDlgRecyclerView.setNestedScrollingEnabled(true);

        final AppCompatButton bt_submit = (AppCompatButton) dialog.findViewById(R.id.bt_submit);
        ((EditText) dialog.findViewById(R.id.et_post)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bt_submit.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        bt_submit.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(getActivity(), "Post Submitted", Toast.LENGTH_SHORT).show();
        });

        ((ImageButton) dialog.findViewById(R.id.bt_photo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Post Photo Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ((ImageButton) dialog.findViewById(R.id.bt_link)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Post Link Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ((ImageButton) dialog.findViewById(R.id.bt_file)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Post File Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ((ImageButton) dialog.findViewById(R.id.bt_setting)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Post Setting Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();

        doKeepDialog(dialog);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVideoPlayback();
        requireActivity().unregisterReceiver(receiver);


    }


    @Override
    public void onStop() {
        super.onStop();
        stopVideoPlayback();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (itemsAdapter.videoIsClicked != -1) {
            VideoController videoController = (VideoController) ((LinearLayoutManager) mRecyclerView.getLayoutManager()).getChildAt(itemsAdapter.videoIsClicked).findViewById(R.id.media_controller);
            if (videoController != null) {
                videoController.startFrom(time, videoController.s);
            }
        }
    }

    private void stopVideoPlayback() {
        if (Objects.requireNonNull(mRecyclerView.getLayoutManager()).getChildAt(itemsAdapter.videoIsClicked) != null) {
            VideoController videoController = (VideoController) ((LinearLayoutManager) mRecyclerView.getLayoutManager()).getChildAt(itemsAdapter.videoIsClicked).findViewById(R.id.media_controller);
            if (videoController != null) {
                videoController.pauseVideo();
            }
        }
    }

    RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            clipToolbarOffset();
            mNewItemBox.setTranslationY(-mToolbarOffset);
            //The most important
            if ((mToolbarOffset < mToolbarHeight && dy > 0) || (mToolbarOffset > 0 && dy < 0)) {
                mToolbarOffset += dy;
            }

            if (mIsLoading)
                return;
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
            if (pastVisibleItems + visibleItemCount >= totalItemCount) {

                //End of list

                mIsLoading = true;

                loadingMore = true;

                getPaginationItems();
            }
        }
    };

    private void clipToolbarOffset() {
        if (mToolbarOffset > mToolbarHeight) {
            mToolbarOffset = mToolbarHeight;
        } else if (mToolbarOffset < 0) {
            mToolbarOffset = 0;
        }
    }
    public void getSharableLink(){
        if(ru.ifsoft.network.repository.local.Session.getInstance(getApplicationContext()).getLocalSave().getShareUrl().equalsIgnoreCase("")){
            getShareRequest();
        }else {
            shareLinkToApps(ru.ifsoft.network.repository.local.Session.getInstance(getApplicationContext()).getLocalSave().getShareUrl());
            Log.v("ttt", ru.ifsoft.network.repository.local.Session.getInstance(getApplicationContext()).getLocalSave().getShareUrl());
        }
    }
    public void getShareRequest(){
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(requireActivity(),SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.create();
        sweetAlertDialog.show();
        CustomRequest customRequest = new CustomRequest(Request.Method.POST,METHOD_SHARE_LINK,null,response -> {
            sweetAlertDialog.dismiss();
            try {
                if(!response.getBoolean("error")){
                    String deepLinkUrl = response.getString("deeplink");
                    ru.ifsoft.network.repository.local.Session.getInstance(getApplicationContext()).getLocalSave().setShareUrl(response.getString("deeplink"));
                    shareLinkToApps(deepLinkUrl);
                    Log.v("ttt","link url ::"+deepLinkUrl);
                    Log.v("ttt",response.toString());
                    Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(requireContext(), getString(R.string.error_internet_connection), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.v("ttt",e.getMessage()+"1");
            }
        },error -> {
            sweetAlertDialog.dismiss();
            Log.v("ttt",error.getMessage()+"0");
            getString(R.string.error_internet_connection);
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("accountId", String.valueOf(App.getInstance().getId()));
                hashMap.put("accessToken",App.getInstance().getAccessToken());
                return hashMap;
            }
        };
        App.getInstance().addToRequestQueue(customRequest);
    }
    public void shareLinkToApps(String link){
/*
        Uri imageUri = Uri.parse("android.resource://" + requireActivity().getPackageName()
                + "/drawable/" + "appicon");
*/
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Yavedo");

        shareIntent.putExtra(Intent.EXTRA_TEXT,"Hello!\n" +
                "Please click this link to download Yavedo, a new and simplified social media from Play Store to connect with me\n"+ link+"\nI am using it and loved it.\n" +
                "Thanks.");
//        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "choose one"));
    }
}