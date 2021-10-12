package com.xahid.Motivational_Quotes.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdView;
import com.xahid.Motivational_Quotes.BuildConfig;
import com.xahid.Motivational_Quotes.R;
import com.xahid.Motivational_Quotes.adapters.PostsPagerAdapter;
import com.xahid.Motivational_Quotes.data.constant.AppConstant;
import com.xahid.Motivational_Quotes.data.sqlite.FavoriteDbController;
import com.xahid.Motivational_Quotes.listeners.ListItemClickListener;
import com.xahid.Motivational_Quotes.models.content.Posts;
import com.xahid.Motivational_Quotes.models.favorite.FavoriteModel;
import com.xahid.Motivational_Quotes.utility.ActivityUtilities;
import com.xahid.Motivational_Quotes.utility.AdsUtilities;
import com.xahid.Motivational_Quotes.utility.AppUtilities;
import com.xahid.Motivational_Quotes.utility.PermissionUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends BaseActivity {
    private Activity mActivity;
    private Context mContext;
    private ArrayList<Posts> mItemList;
    private int mCurrentIndex;
    private ViewPager mViewPager;
    private PostsPagerAdapter mPagerAdapter = null;
    private TextView mTxtCounter;
    private ImageButton mBtnFavorite, mBtnShare, mBtnSetWall, mBtnDownloadWall;
    private Bitmap mBitmap;

    // Favourites view
    private List<FavoriteModel> mFavoriteList;
    private FavoriteDbController mFavoriteDbController;
    private boolean mIsFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVar();
        initView();
        initFunctionality();
        initListener();
    }

    private void initVar() {
        mActivity = DetailsActivity.this;
        mContext = mActivity.getApplicationContext();

        mFavoriteList = new ArrayList<>();

        Intent intent = getIntent();
        if (intent != null) {
            mCurrentIndex = intent.getIntExtra(AppConstant.BUNDLE_KEY_INDEX, 0);
            mItemList = intent.getParcelableArrayListExtra(AppConstant.BUNDLE_KEY_ITEM);
        }
    }

    private void initView() {
        setContentView(R.layout.activity_details);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        mTxtCounter = (TextView) findViewById(R.id.menus_counter);
        mBtnFavorite = (ImageButton) findViewById(R.id.menus_fav);
        mBtnShare = (ImageButton) findViewById(R.id.menus_share);
        mBtnSetWall = (ImageButton) findViewById(R.id.menus_set_image);
        mBtnDownloadWall = (ImageButton) findViewById(R.id.menus_download_image);

        initLoader();
        initToolbar(false);
        setToolbarTitle(getString(R.string.details_text));
        enableUpButton();
    }

    private void initFunctionality() {
        showLoader();

        mPagerAdapter = new PostsPagerAdapter(mActivity, mItemList);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(mCurrentIndex);

        mFavoriteDbController = new FavoriteDbController(mContext);
        mFavoriteList.addAll(mFavoriteDbController.getAllData());
        isFavorite();

        getBitmap();

        updateCounter();

        hideLoader();

        // load full screen ad
        AdsUtilities.getInstance(mContext).loadFullScreenAd(mActivity);
        // show banner ads
        AdsUtilities.getInstance(mContext).showBannerAd((AdView) findViewById(R.id.adsView));
    }


    public void initListener() {
        mBtnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Posts model = mItemList.get(mViewPager.getCurrentItem());
                mIsFavorite = !mIsFavorite;
                if (mIsFavorite) {
                    mFavoriteDbController.insertData(model.getTitle(), model.getImageUrl(), model.getCategory());
                    mFavoriteList.add(new FavoriteModel(AppConstant.BUNDLE_KEY_ZERO_INDEX, model.getTitle(), model.getImageUrl(), model.getCategory()));
                    Toast.makeText(getApplicationContext(), getString(R.string.added_to_fav), Toast.LENGTH_SHORT).show();
                } else {
                    mFavoriteDbController.deleteEachFav(model.getTitle());
                    for (int i = 0; i < mFavoriteList.size(); i++) {
                        if (mFavoriteList.get(i).getTitle().equals(model.getTitle())) {
                            mFavoriteList.remove(i);
                            break;
                        }
                    }
                    Toast.makeText(getApplicationContext(), getString(R.string.removed_from_fav), Toast.LENGTH_SHORT).show();
                }
                setFavorite();
            }
        });

        mBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionUtilities.isPermissionGranted(mActivity, PermissionUtilities.SD_READ_WRITE_PERMISSIONS, PermissionUtilities.REQUEST_READ_WRITE_STORAGE_DOWNLOAD)) {
                    try {
                        File file = new File(mActivity.getExternalCacheDir(), Html.fromHtml(mItemList.get(mViewPager.getCurrentItem()).getTitle()).toString().trim() + ".png");
                        FileOutputStream fOut = new FileOutputStream(file);
                        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                        file.setReadable(true, false);

                        final String appPackageName = mActivity.getPackageName();
                        final Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        sendIntent.putExtra(Intent.EXTRA_TEXT,
                                mActivity.getResources().getString(R.string.share_text)
                                        + " https://play.google.com/store/apps/details?id=" + appPackageName);
                        Uri uri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID + ".provider", file);
                        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        sendIntent.setType("image/png");
                        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mBtnSetWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemList != null) {
                    ActivityUtilities.getInstance().invokeWallPreviewNCropSetActiviy(mActivity, WallCropNSetActivity.class, mItemList.get(mViewPager.getCurrentItem()).getImageUrl(), false);
                }
            }
        });

        mBtnDownloadWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemList != null) {
                    AppUtilities.downloadFile(mContext, mActivity, mBitmap);
                }
            }
        });

        mPagerAdapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Posts model = mItemList.get(position);
                switch (view.getId()) {
                    case R.id.card_view_top:
                        if (mItemList != null) {
                            ActivityUtilities.getInstance().invokeWallPreviewNCropSetActiviy(mActivity, WallPreviewActivity.class, model.getImageUrl(), false);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateCounter();
                mIsFavorite = false;
                isFavorite();
                getBitmap();

                // show full-screen ads
                AdsUtilities.getInstance(mContext).showFullScreenAd();

                if (position % AppConstant.ADS_INTERVAL == 0) {
                    // load full screen ad
                    AdsUtilities.getInstance(mContext).loadFullScreenAd(mActivity);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void isFavorite() {
        for (int i = 0; i < mFavoriteList.size(); i++) {
            if (mFavoriteList.get(i).getTitle().equals(mItemList.get(mViewPager.getCurrentItem()).getTitle())) {
                mIsFavorite = true;
                break;
            }
        }
        setFavorite();
    }

    public void setFavorite() {
        if (mIsFavorite) {
            mBtnFavorite.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.ic_fav_black));
        } else {
            mBtnFavorite.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.ic_un_fav_black));
        }
    }

    public void updateCounter() {
        String counter = String.format(getString(R.string.item_counter), mViewPager.getCurrentItem() + AppConstant.BUNDLE_KEY_FIRST_INDEX, mItemList.size());
        mTxtCounter.setText(counter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // show full-screen ads
                AdsUtilities.getInstance(mContext).showFullScreenAd();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // show full-screen ads
        AdsUtilities.getInstance(mContext).showFullScreenAd();
    }

    public void getBitmap() {
        Glide.with(mContext)
                .asBitmap()
                .load(mItemList.get(mViewPager.getCurrentItem()).getImageUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        mBitmap = resource;
                    }
                });
    }
}
