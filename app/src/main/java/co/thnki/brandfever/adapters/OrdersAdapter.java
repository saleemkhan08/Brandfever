package co.thnki.brandfever.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.ProductActivity;
import co.thnki.brandfever.R;
import co.thnki.brandfever.firebase.database.models.ProductBundle;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.fragments.OrderCancellationDialogFragment;
import co.thnki.brandfever.utils.CartUtil;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.view.holders.OrderListProductViewHolder;

import static co.thnki.brandfever.Brandfever.getResString;
import static co.thnki.brandfever.Brandfever.toast;

public class OrdersAdapter extends FirebaseRecyclerAdapter<Products, OrderListProductViewHolder>
{
    private Activity mActivity;
    private String mGoogleId;
    public String mPhoneNumber;

    public static OrdersAdapter getInstance(DatabaseReference reference, String googleId, Activity activity)
    {
        OrdersAdapter adapter = new OrdersAdapter(Products.class, R.layout.order_list_product_row,
                OrderListProductViewHolder.class, reference);
        adapter.mActivity = activity;
        adapter.mGoogleId = googleId;
        return adapter;
    }

    private OrdersAdapter(Class<Products> modelClass, int modelLayout, Class<OrderListProductViewHolder> viewHolderClass, Query ref)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(final OrderListProductViewHolder viewHolder, final Products model, int position)
    {
        /**
         * get the zeroth item to display in the list.
         */
        String imageUrl = model.getPhotoUrlList().get(0);
        Glide.with(mActivity).load(imageUrl)
                .asBitmap().placeholder(R.mipmap.price_tag)
                .centerCrop().into(viewHolder.mImageView);

        viewHolder.mBrand.setText(model.getBrand());
        viewHolder.mPriceAfter.setText(model.getPriceAfter());
        viewHolder.mProductSize.setText(Brandfever.getResString(R.string.size)+" "+model.getSelectedSize());
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
                    ProductBundle bundle = new ProductBundle(model);
                    intent.putExtra(Products.PRODUCT_MODEL, bundle);

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
                    /*AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setView(R.layout.order_cancellation_dialog);
                    builder.setMessage(R.string.areYouSureYouWantToCancelThisOrder);
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {

                        }
                    }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            OrdersUtil.getsInstance().cancelOrder(model);
                        }
                    }).show();*/

                    OrderCancellationDialogFragment fragment = OrderCancellationDialogFragment.getInstance(CartUtil.getKey(model),mGoogleId);
                    fragment.show(((AppCompatActivity)mActivity).getSupportFragmentManager(), OrderCancellationDialogFragment.TAG);
                }
                else
                {
                    toast(R.string.noInternet);
                }
            }
        });
    }
}