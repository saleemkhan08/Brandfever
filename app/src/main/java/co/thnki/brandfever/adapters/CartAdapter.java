package co.thnki.brandfever.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.ProductActivity;
import co.thnki.brandfever.R;
import co.thnki.brandfever.firebase.database.models.Order;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.utils.CartUtil;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.view.holders.CartListProductViewHolder;

import static co.thnki.brandfever.Brandfever.getResString;
import static co.thnki.brandfever.Brandfever.toast;

public class CartAdapter extends FirebaseRecyclerAdapter<Order, CartListProductViewHolder>
{
    private Activity mActivity;

    public static CartAdapter getInstance(DatabaseReference reference, Activity activity)
    {
        CartAdapter adapter = new CartAdapter(Order.class, R.layout.cart_list_product_row, CartListProductViewHolder.class,
                reference.orderByChild(Order.TIME_STAMP));
        adapter.mActivity = activity;
        return adapter;
    }

    private CartAdapter(Class<Order> modelClass, int modelLayout, Class<CartListProductViewHolder> viewHolderClass, Query ref)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(final CartListProductViewHolder viewHolder, final Order model, int position)
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
        viewHolder.mProductSize.setText(Brandfever.getResString(R.string.size) + " " + model.getSelectedSize());
        String discountText = model.getPriceBefore();
        if (discountText != null && !discountText.isEmpty())
        {
            viewHolder.mPriceBefore.setText(discountText);
            viewHolder.mPriceBefore.setPaintFlags(viewHolder.mPriceBefore.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        Integer[] items = new Integer[model.getAvailableNoOfProducts()];
        for (int i = 0; i < model.getAvailableNoOfProducts(); i++)
        {
            items[i] = i + 1;
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(mActivity, R.layout.simple_spinner_item, R.id.text1, items);
        viewHolder.mNoOfProductsSpinner.setAdapter(adapter);
        viewHolder.mNoOfProductsTextView.setText(model.getNoOfProducts() + "");
        viewHolder.mNoOfProductsSpinner.setSelection(model.getNoOfProducts() - 1);
        viewHolder.mNoOfProductsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                Log.d("onItemSelected", "onItemSelected : " + i);
                viewHolder.mNoOfProductsTextView.setText("" + (i + 1));
                model.setNoOfProducts(i + 1);
                CartUtil.getsInstance().addToCart(model);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                Log.d("onItemSelected", "onNothingSelected");
                viewHolder.mNoOfProductsTextView.setText("" + 1);
                model.setNoOfProducts(1);
                CartUtil.getsInstance().addToCart(model);
            }
        });

        //
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

                    if (Build.VERSION.SDK_INT >= 21)
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
        viewHolder.mDeleteFromCart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (ConnectivityUtil.isConnected())
                {
                    CartUtil.getsInstance().removeFromCart(model);
                }
                else
                {
                    toast(R.string.noInternet);
                }
            }
        });
    }
}