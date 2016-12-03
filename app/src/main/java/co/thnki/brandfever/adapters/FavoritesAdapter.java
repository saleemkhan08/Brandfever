package co.thnki.brandfever.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import co.thnki.brandfever.ProductActivity;
import co.thnki.brandfever.R;
import co.thnki.brandfever.firebase.database.models.FavoriteProduct;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.FavoritesUtil;
import co.thnki.brandfever.view.holders.WishListProductViewHolder;

import static co.thnki.brandfever.Brandfever.getResString;
import static co.thnki.brandfever.Brandfever.toast;

public class FavoritesAdapter extends FirebaseRecyclerAdapter<FavoriteProduct, WishListProductViewHolder>
{
    private Activity mActivity;

    public static FavoritesAdapter getInstance(DatabaseReference reference, Activity activity)
    {
        FavoritesAdapter adapter = new FavoritesAdapter(FavoriteProduct.class,
                R.layout.wish_list_product_row, WishListProductViewHolder.class,
                reference.orderByChild(FavoriteProduct.TIME_STAMP));
        adapter.mActivity = activity;
        return adapter;
    }

    private FavoritesAdapter(Class<FavoriteProduct> modelClass, int modelLayout, Class<WishListProductViewHolder> viewHolderClass, Query ref)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }


    @Override
    protected void populateViewHolder(final WishListProductViewHolder viewHolder, final FavoriteProduct model, int position)
    {
        /**
         * get the zeroth item to display in the list.
         */

        String imageUrl = model.getPhotoUrl();
        Glide.with(mActivity).load(imageUrl)
                .asBitmap().placeholder(R.mipmap.price_tag)
                .centerCrop().into(viewHolder.mImageView);

        viewHolder.mBrand.setText(model.getBrand());
        viewHolder.mPriceAfter.setText(model.getPriceAfter());

        String discountText = model.getPriceBefore();
        if (discountText != null && !discountText.isEmpty())
        {
            viewHolder.mPriceBefore.setText(discountText);
            viewHolder.mPriceBefore.setPaintFlags(viewHolder.mPriceBefore.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (ConnectivityUtil.isConnected())
                {
                    Intent intent = new Intent(mActivity, ProductActivity.class);
                    intent.putExtra(Products.PRODUCT_ID, model.getProductId());
                    intent.putExtra(Products.CATEGORY_ID, model.getCategoryId());
                    if(Build.VERSION.SDK_INT >= 21)
                    {
                        String transitionName = getResString(R.string.productTransitionImage);
                        viewHolder.mImageView.setTransitionName(transitionName);

                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                                .makeSceneTransitionAnimation(mActivity, viewHolder.mImageView,
                                        transitionName);
                        mActivity.startActivity(intent, optionsCompat.toBundle());
                    }
                    else
                    {
                        mActivity.startActivity(intent);
                    }
                }
                else
                {
                    toast(R.string.noInternet);
                }
            }
        });

        viewHolder.mDeleteFav.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (ConnectivityUtil.isConnected())
                {
                    FavoritesUtil.getsInstance().removeFromFavorites(model);
                }
                else
                {
                    toast(R.string.noInternet);
                }
            }
        });
    }
}
