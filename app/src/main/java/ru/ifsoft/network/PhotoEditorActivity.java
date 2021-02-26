package ru.ifsoft.network;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import ja.burhanrashid52.photoeditor.SaveSettings;
import ja.burhanrashid52.photoeditor.TextStyleBuilder;
import ja.burhanrashid52.photoeditor.ViewType;

public class PhotoEditorActivity extends BaseActivity implements OnPhotoEditorListener, View.OnClickListener
        , PropertiesBSFragment.Properties
        , EmojiBSFragment.EmojiListener
        , StickerBSFragment.StickerListener
        , FilterListener {
    private PhotoEditorView mPhotoEditorView;
    private PhotoEditor mPhotoEditor;
    private ImageButton ibt_share, ibt_undo, ibt_redo, ibt_brush, ibt_text, ibt_eraser, ibt_sticker, ibt_emoji, ibt_close, ibt_save, ibt_done;
    private TextView tv_brush, tv_text, tv_erase, tv_sticker, tv_emoji, mTxtCurrentTool;
    private static final String TAG = PhotoEditorActivity.class.getSimpleName();
    public static final String FILE_PROVIDER_AUTHORITY = "com.burhanrashid52.photoeditor.fileprovider";
    private static final int PICK_REQUEST = 53;

    private PropertiesBSFragment mPropertiesBSFragment;
    private EmojiBSFragment mEmojiBSFragment;
    private StickerBSFragment mStickerBSFragment;
    private Typeface mWonderFont;
    private RecyclerView mRvTools, mRvFilters;
    //private EditingToolsAdapter mEditingToolsAdapter = new EditingToolsAdapter(this);
    private FilterViewAdapter mFilterViewAdapter = new FilterViewAdapter(this);
    private ConstraintLayout mRootView;
    private ConstraintSet mConstraintSet = new ConstraintSet();
    private boolean mIsFilterVisible;

    @Nullable
    @VisibleForTesting
    Uri mSaveImageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);

        initView();
        initItem();
        initListener();

        mWonderFont = Typeface.createFromAsset(getAssets(), "beyond_wonderland.ttf");
        mPropertiesBSFragment = new PropertiesBSFragment();
        mEmojiBSFragment = new EmojiBSFragment();
        mStickerBSFragment = new StickerBSFragment();
        mStickerBSFragment.setStickerListener(this);
        mEmojiBSFragment.setEmojiListener(this);
        mPropertiesBSFragment.setPropertiesChangeListener(this);

//        LinearLayoutManager llmTools = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        mRvTools.setLayoutManager(llmTools);
//        mRvTools.setAdapter(mEditingToolsAdapter);

//        LinearLayoutManager llmFilters = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        mRvFilters.setLayoutManager(llmFilters);
//        mRvFilters.setAdapter(mFilterViewAdapter);

    }

    private void initView() {
        mPhotoEditorView = findViewById(R.id.photoEditorView);

        ibt_share = findViewById(R.id.ibt_share);
        ibt_share.setOnClickListener(this);
        ibt_undo = findViewById(R.id.ibt_undo);
        ibt_undo.setOnClickListener(this);
        ibt_redo = findViewById(R.id.ibt_redo);
        ibt_redo.setOnClickListener(this);
        ibt_brush = findViewById(R.id.ibt_brush);
        ibt_brush.setOnClickListener(this);
        ibt_text = findViewById(R.id.ibt_text);
        ibt_text.setOnClickListener(this);
        ibt_eraser = findViewById(R.id.ibt_eraser);
        ibt_eraser.setOnClickListener(this);
        ibt_sticker = findViewById(R.id.ibt_sticker);
        ibt_sticker.setOnClickListener(this);
        ibt_emoji = findViewById(R.id.ibt_emoji);
        ibt_emoji.setOnClickListener(this);
        ibt_close = findViewById(R.id.ibt_close);
        ibt_close.setOnClickListener(this);
        ibt_save = findViewById(R.id.ibt_save);
        ibt_save.setOnClickListener(this);
        ibt_done = findViewById(R.id.ibt_done);
        ibt_done.setOnClickListener(this);
        tv_brush = findViewById(R.id.tv_brush);
        tv_brush.setOnClickListener(this);
        tv_text = findViewById(R.id.tv_text);
        tv_text.setOnClickListener(this);
        tv_erase = findViewById(R.id.tv_erase);
        tv_erase.setOnClickListener(this);
        tv_sticker = findViewById(R.id.tv_sticker);
        tv_sticker.setOnClickListener(this);
        tv_emoji = findViewById(R.id.tv_emoji);
        tv_emoji.setOnClickListener(this);
        mTxtCurrentTool = findViewById(R.id.tv_action_name);
    }

    String type = "";

    private void initItem() {

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true).build();
        type = Objects.requireNonNull(getIntent().getExtras()).getString("type");
        assert type != null;
        if (type.equals("file")) {
            Picasso.with(getBaseContext())
                    .load(new File(Objects.requireNonNull(getIntent().getExtras().getString("url"))))
                    .into(mPhotoEditorView.getSource(), callback);
        } else if (type.equals("url")) {
            Picasso.with(getBaseContext())
                    .load(Objects.requireNonNull(getIntent().getExtras().getString("url")))
                    .into(mPhotoEditorView.getSource(), callback);
        }


    }

    private void initListener() {

    }


    @Override
    public void onEditTextChangeListener(final View rootView, String text, int colorCode) {
        Log.d("text123", "started");
        TextEditorDialogFragment textEditorDialogFragment =
                TextEditorDialogFragment.show(this, text, colorCode);
        textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
            @Override
            public void onDone(String inputText, int colorCode) {
                final TextStyleBuilder styleBuilder = new TextStyleBuilder();
                styleBuilder.withTextColor(colorCode);

                mPhotoEditor.editText(rootView, inputText, styleBuilder);
                mTxtCurrentTool.setText(R.string.label_text);
            }
        });
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
        Log.d(TAG, "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {
        Log.d(TAG, "onRemoveViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_undo:
                mPhotoEditor.saveAsBitmap(new OnSaveBitmap() {
                    @Override
                    public void onBitmapReady(Bitmap saveBitmap) {
                        //myImg[0] = saveBitmap;
                        Matrix matrix = new Matrix();
                        matrix.postRotate(-90);
                        Bitmap rotated = Bitmap.createBitmap(saveBitmap, 0, 0, saveBitmap.getWidth(), saveBitmap.getHeight(),
                                matrix, true);
                        mPhotoEditorView.getSource().setImageBitmap(rotated);
                    }
                    @Override
                    public void onFailure(Exception e) {
                    }
                });
                //mPhotoEditor.undo();
                break;
            case R.id.ibt_redo:
                mPhotoEditor.saveAsBitmap(new OnSaveBitmap() {
                    @Override
                    public void onBitmapReady(Bitmap saveBitmap) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);

                        Bitmap rotated = Bitmap.createBitmap(saveBitmap, 0, 0, saveBitmap.getWidth(), saveBitmap.getHeight(),
                                matrix, true);
                        mPhotoEditorView.getSource().setImageBitmap(rotated);
                    }
                    @Override
                    public void onFailure(Exception e) {
                    }
                });
                //mPhotoEditor.redo();
                break;
            case R.id.ibt_save:
            case R.id.ibt_done:
                saveImage();
                break;
            case R.id.ibt_close:
                onBackPressed();
                break;
            case R.id.ibt_share:
                shareImage();
                break;
            case R.id.ibt_brush:
                mPhotoEditor.setBrushDrawingMode(true);
                mTxtCurrentTool.setText(R.string.label_brush);
                mPropertiesBSFragment.show(getSupportFragmentManager(), mPropertiesBSFragment.getTag());
                break;
            case R.id.ibt_text:
                Log.d("text321", "started");
                TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
                textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
                    @Override
                    public void onDone(String inputText, int colorCode) {
                        final TextStyleBuilder styleBuilder = new TextStyleBuilder();
                        styleBuilder.withTextColor(colorCode);

                        mPhotoEditor.addText(inputText, styleBuilder);
                        mTxtCurrentTool.setText(R.string.label_text);
                    }
                });
                break;
            case R.id.ibt_eraser:
                mPhotoEditor.brushEraser();
                mTxtCurrentTool.setText(R.string.label_eraser_mode);
                break;
            case R.id.ibt_sticker:
                mStickerBSFragment.show(getSupportFragmentManager(), mStickerBSFragment.getTag());
                break;
            case R.id.ibt_emoji:
                mEmojiBSFragment.show(getSupportFragmentManager(), mEmojiBSFragment.getTag());
                break;
        }
    }

    private void shareImage() {
    }

    private Uri buildFileProviderUri(@NonNull Uri uri) {
        return FileProvider.getUriForFile(this,
                FILE_PROVIDER_AUTHORITY,
                new File(uri.getPath()));
    }

    @SuppressLint("MissingPermission")
    private void saveImage() {
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showLoading("Saving...");
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + ""
                    + System.currentTimeMillis() + ".png");
            try {
                file.createNewFile();

                SaveSettings saveSettings = new SaveSettings.Builder()
                        .setClearViewsEnabled(true)
                        .setTransparencyEnabled(true)
                        .build();

                mPhotoEditor.saveAsFile(file.getAbsolutePath(), saveSettings, new PhotoEditor.OnSaveListener() {
                    @Override
                    public void onSuccess(@NonNull String imagePath) {
                        hideLoading();
                        showSnackbar("Image Saved Successfully");
                        mSaveImageUri = Uri.fromFile(new File(imagePath));
                        mPhotoEditorView.getSource().setImageURI(mSaveImageUri);
                        if (type.equals("file")) {
                            setResult(101, getIntent());
                            getIntent().putExtra("returnpath", imagePath);
                            finish();
                        } else if (type.equals("url")) {

                        }
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        hideLoading();
                        showSnackbar("Failed to save Image");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                hideLoading();
                showSnackbar(e.getMessage());
            }
        }
    }

    Callback callback = new Callback() {

        @Override
        public void onSuccess() {

        }

        @Override
        public void onError() {

        }
    };

    @Override
    public void onColorChanged(int colorCode) {
        mPhotoEditor.setBrushColor(colorCode);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onOpacityChanged(int opacity) {
        mPhotoEditor.setOpacity(opacity);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onBrushSizeChanged(int brushSize) {
        mPhotoEditor.setBrushSize(brushSize);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onEmojiClick(String emojiUnicode) {
        mPhotoEditor.addEmoji(emojiUnicode);
        mTxtCurrentTool.setText(R.string.label_emoji);
    }

    @Override
    public void onStickerClick(Bitmap bitmap) {
        mPhotoEditor.addImage(bitmap);
        mTxtCurrentTool.setText(R.string.label_sticker);
    }

    @Override
    public void isPermissionGranted(boolean isGranted, String permission) {
        if (isGranted) {
            saveImage();
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.msg_save_image));
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveImage();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create().show();

    }

    @Override
    public void onFilterSelected(PhotoFilter photoFilter) {
        mPhotoEditor.setFilterEffect(photoFilter);
    }

//    @Override
//    public void onToolSelected(ToolType toolType) {
//        switch (toolType) {
//            case BRUSH:
//                mPhotoEditor.setBrushDrawingMode(true);
//                mTxtCurrentTool.setText(R.string.label_brush);
//                mPropertiesBSFragment.show(getSupportFragmentManager(), mPropertiesBSFragment.getTag());
//                break;
//            case TEXT:
//                TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
//                textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
//                    @Override
//                    public void onDone(String inputText, int colorCode) {
//                        final TextStyleBuilder styleBuilder = new TextStyleBuilder();
//                        styleBuilder.withTextColor(colorCode);
//
//                        mPhotoEditor.addText(inputText, styleBuilder);
//                        mTxtCurrentTool.setText(R.string.label_text);
//                    }
//                });
//                break;
//            case ERASER:
//                mPhotoEditor.brushEraser();
//                mTxtCurrentTool.setText(R.string.label_eraser_mode);
//                break;
//            case FILTER:
//                mTxtCurrentTool.setText(R.string.label_filter);
//                showFilter(true);
//                break;
//            case EMOJI:
//                mEmojiBSFragment.show(getSupportFragmentManager(), mEmojiBSFragment.getTag());
//                break;
//            case STICKER:
//                mStickerBSFragment.show(getSupportFragmentManager(), mStickerBSFragment.getTag());
//                break;
//        }
//    }


    void showFilter(boolean isVisible) {
        mIsFilterVisible = isVisible;
        mConstraintSet.clone(mRootView);

        if (isVisible) {
            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.START);
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.START);
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
        } else {
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.END);
        }

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(350);
        changeBounds.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        TransitionManager.beginDelayedTransition(mRootView, changeBounds);

        mConstraintSet.applyTo(mRootView);
    }

    @Override
    public void onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false);
            mTxtCurrentTool.setText(R.string.app_name);
        } else if (!mPhotoEditor.isCacheEmpty()) {
            showSaveDialog();
        } else {
            super.onBackPressed();
        }
    }
}
