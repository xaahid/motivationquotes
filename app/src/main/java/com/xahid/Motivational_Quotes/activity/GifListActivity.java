package com.xahid.Motivational_Quotes.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.xahid.Motivational_Quotes.R;
import com.xahid.Motivational_Quotes.adapters.HomeRecentPostAdapter;
import com.xahid.Motivational_Quotes.data.constant.AppConstant;
import com.xahid.Motivational_Quotes.data.sqlite.FavoriteDbController;
import com.xahid.Motivational_Quotes.listeners.ListItemClickListener;
import com.xahid.Motivational_Quotes.models.content.Posts;
import com.xahid.Motivational_Quotes.models.favorite.FavoriteModel;
import com.xahid.Motivational_Quotes.utility.ActivityUtilities;
import com.xahid.Motivational_Quotes.utility.AdsUtilities;

import java.util.ArrayList;
import java.util.List;

public class GifListActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;

    private ArrayList<Posts> mContentList;
    private HomeRecentPostAdapter mAdapter = null;
    private RecyclerView mRecycler;

    // Favourites view
    private List<FavoriteModel> mFavoriteList;
    private FavoriteDbController mFavoriteDbController;

    // Firebase Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFirebase();

        initVar();
        initView();
        initFunctionality();
        initListener();

    }

    private void initVar() {
        mActivity = GifListActivity.this;
        mContext = mActivity.getApplicationContext();

        mContentList = new ArrayList<>();
        mFavoriteList = new ArrayList<>();
    }

    private void initView() {
        setContentView(R.layout.activity_common_recycler);

        mRecycler = (RecyclerView) findViewById(R.id.rvContent);
        mRecycler.setLayoutManager(new GridLayoutManager(mActivity, 2, GridLayoutManager.VERTICAL, false));
        mAdapter = new HomeRecentPostAdapter(mContext, mActivity, mContentList);
        mRecycler.setAdapter(mAdapter);

        initLoader();
        initToolbar(true);
        enableUpButton();
        setToolbarTitle(getString(R.string.site_menu_gif));
    }

    private void initFunctionality() {
        loadData();

        // show full-screen ads
        AdsUtilities.getInstance(mContext).showFullScreenAd();
        // show banner ads
        AdsUtilities.getInstance(mContext).showBannerAd((AdView) findViewById(R.id.adsView));
    }

    public void initListener() {
        // recycler list item click listener
        mAdapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Posts model = mContentList.get(position);

                switch (view.getId()) {
                    case R.id.btn_fav:
                        if (model.isFavorite()) {
                            mFavoriteDbController.deleteEachFav(mContentList.get(position).getTitle());
                            mContentList.get(position).setFavorite(false);
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), getString(R.string.removed_from_fav), Toast.LENGTH_SHORT).show();

                        } else {
                            mFavoriteDbController.insertData(model.getTitle(), model.getImageUrl(), model.getCategory());
                            mContentList.get(position).setFavorite(true);
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), getString(R.string.added_to_fav), Toast.LENGTH_SHORT).show();

                        }
                        break;
                    case R.id.card_view_top:
                        ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mContentList, position, false);
                        break;
                    default:
                        break;
                }
            }

        });
    }


    private void loadData() {
        showLoader();
        loadPostsFromFirebase();
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
    }

    private void loadPostsFromFirebase() {
        mDatabaseReference.child(AppConstant.JSON_KEY_GIFS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot contentSnapShot : dataSnapshot.getChildren()) {
                    Posts post = contentSnapShot.getValue(Posts.class);
                    mContentList.add(post);
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showEmptyView();
            }
        });
    }

    private void updateUI() {
        if (mFavoriteDbController == null) {
            mFavoriteDbController = new FavoriteDbController(mContext);
        }
        mFavoriteList.clear();
        mFavoriteList.addAll(mFavoriteDbController.getAllData());

        // Check for favorite
        for (int i = 0; i < mContentList.size(); i++) {
            boolean isFavorite = false;
            for (int j = 0; j < mFavoriteList.size(); j++) {
                if (mFavoriteList.get(j).getTitle().equals(mContentList.get(i).getTitle())) {
                    isFavorite = true;
                    break;
                }
            }
            mContentList.get(i).setFavorite(isFavorite);
        }
        mAdapter.notifyDataSetChanged();
        if (!mContentList.isEmpty() && mContentList.size() > 0) {
            hideLoader();
        } else {
            showEmptyView();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mContentList.size() != 0) {
            updateUI();
        }
    }
}
