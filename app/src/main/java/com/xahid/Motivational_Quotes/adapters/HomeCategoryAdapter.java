package com.xahid.Motivational_Quotes.adapters;

import android.app.Activity;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xahid.Motivational_Quotes.R;
import com.xahid.Motivational_Quotes.listeners.ListItemClickListener;
import com.xahid.Motivational_Quotes.models.content.Categories;

import java.util.ArrayList;

public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder> {

    private Context mContext;
    private Activity mActivity;

    private ArrayList<Categories> mCategoryList;
    private ListItemClickListener mItemClickListener;

    public HomeCategoryAdapter(Context mContext, Activity mActivity, ArrayList<Categories> mCategoryList) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mCategoryList = mCategoryList;
    }

    public void setItemClickListener(ListItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_category_recycler, parent, false);
        return new ViewHolder(view, viewType, mItemClickListener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView categoryImg;
        private TextView categoryTitle;
        private RelativeLayout lytContainer;
        private ListItemClickListener itemClickListener;


        public ViewHolder(View itemView, int viewType, ListItemClickListener itemClickListener) {
            super(itemView);

            this.itemClickListener = itemClickListener;
            // Find all views ids
            categoryImg = (ImageView) itemView.findViewById(R.id.category_img);
            categoryTitle = (TextView) itemView.findViewById(R.id.category_name);
            lytContainer = (RelativeLayout) itemView.findViewById(R.id.lyt_container);

            lytContainer.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getLayoutPosition(), view);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != mCategoryList ? mCategoryList.size() : 0);

    }

    @Override
    public void onBindViewHolder(HomeCategoryAdapter.ViewHolder mainHolder, int position) {
        final Categories model = mCategoryList.get(position);

        // setting data over views
        String imgUrl = model.getImageUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(mContext)
                    .load(imgUrl)
                    .into(mainHolder.categoryImg);
        }
        mainHolder.categoryTitle.setText(Html.fromHtml(model.getTitle()));
    }
}