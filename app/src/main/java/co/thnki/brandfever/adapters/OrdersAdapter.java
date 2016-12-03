package co.thnki.brandfever.adapters;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.ProductActivity;
import co.thnki.brandfever.R;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.Order;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.fragments.NotificationDialogFragment;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.OrdersUtil;
import co.thnki.brandfever.view.holders.OrderListProductViewHolder;

import static co.thnki.brandfever.Brandfever.getResString;
import static co.thnki.brandfever.Brandfever.toast;

public class OrdersAdapter extends FirebaseRecyclerAdapter<Order, OrderListProductViewHolder>
{
    private Activity mActivity;
    private String mGoogleId;
    private SharedPreferences mPreferences;

    public static OrdersAdapter getInstance(DatabaseReference reference, String googleId, Activity activity)
    {
        OrdersAdapter adapter = new OrdersAdapter(Order.class, R.layout.order_list_product_row,
                OrderListProductViewHolder.class, reference.orderByChild(Order.TIME_STAMP));
        adapter.mActivity = activity;
        adapter.mGoogleId = googleId;
        adapter.mPreferences = Brandfever.getPreferences();
        return adapter;
    }

    private OrdersAdapter(Class<Order> modelClass, int modelLayout, Class<OrderListProductViewHolder> viewHolderClass, Query ref)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(final OrderListProductViewHolder viewHolder, final Order model, final int position)
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

        updateOrderItemColor(model.getOrderStatus(), viewHolder.mStatusImageView, viewHolder.mItemViewContainer);

        String status = "";
        try
        {
            status = Brandfever.getResString(R.string.status) + " : " + Brandfever.getResString(model.getOrderStatus());
        }
        catch (Exception e)
        {
            status = Brandfever.getResString(R.string.status) + " : "+model.getOrderStatus();
        }

        viewHolder.mOrderStatus.setText(status);
        viewHolder.mImageView.setOnClickListener(new View.OnClickListener()
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
        configureOptions(viewHolder, model);
    }

    private void updateOrderItemColor(String status, ImageView itemView, View viewContainer)
    {
        switch (status)
        {
            case OrdersUtil.ORDER_ADDED_TO_CART:
            case OrdersUtil.ORDER_PLACED:
                itemView.setImageResource(R.mipmap.shopping_cart);
                viewContainer.setBackgroundResource(R.color.transparentWhite);
                break;
            case OrdersUtil.ORDER_PACKED:
                itemView.setImageResource(R.mipmap.packed);
                viewContainer.setBackgroundResource(R.color.transparentWhite);
                break;
            case OrdersUtil.ORDER_SHIPPED:
                itemView.setImageResource(R.mipmap.shipped);
                viewContainer.setBackgroundResource(R.color.transparentWhite);
                break;
            case OrdersUtil.ORDER_DELAYED:
                itemView.setImageResource(R.mipmap.delayed);
                viewContainer.setBackgroundResource(R.color.transparentWhite);
                break;
            case OrdersUtil.ORDER_DELIVERED:
                itemView.setImageResource(R.mipmap.delivered);
                viewContainer.setBackgroundResource(R.color.cancelled);
                break;
            case OrdersUtil.ORDER_CANCELLED:
                itemView.setImageResource(R.mipmap.remove_shopping_cart_black);
                viewContainer.setBackgroundResource(R.color.cancelled);
                break;
            case OrdersUtil.ORDER_REQUESTED_RETURN:
                itemView.setImageResource(R.mipmap.return_order);
                itemView.setAlpha(0.5f);
                viewContainer.setBackgroundResource(R.color.colorAccentTransparent);
                break;
            case OrdersUtil.ORDER_RETURNED:
                itemView.setImageResource(R.mipmap.return_order);
                viewContainer.setBackgroundResource(R.color.cancelled);
                break;
        }
    }

    private void configureOptions(final OrderListProductViewHolder holder, final Order order)
    {
        holder.mOrderOptionsContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(mActivity, v);
                popup.getMenuInflater()
                        .inflate(R.menu.order_options_menu, popup.getMenu());

                Menu menu = popup.getMenu();
                if (mPreferences.getBoolean(Accounts.IS_OWNER, false)
                        && !mPreferences.getString(Accounts.GOOGLE_ID, "").equals(mGoogleId))
                {
                    menu.getItem(0).setVisible(true);
                    menu.getItem(1).setVisible(true);
                    menu.getItem(2).setVisible(true);
                    menu.getItem(3).setVisible(true);
                    menu.getItem(4).setVisible(true);
                    menu.getItem(5).setVisible(false);
                    menu.getItem(6).setVisible(true);
                }
                else
                {
                    menu.getItem(0).setVisible(false);
                    menu.getItem(1).setVisible(false);
                    menu.getItem(2).setVisible(false);
                    menu.getItem(3).setVisible(false);
                    menu.getItem(4).setVisible(true);
                    menu.getItem(5).setVisible(true);
                    menu.getItem(6).setVisible(false);
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        if (ConnectivityUtil.isConnected())
                        {
                            switch (item.getItemId())
                            {
                                case R.id.packed:
                                    sendNotification(order, R.string.packedNotification, OrdersUtil.ORDER_PACKED);
                                    break;
                                case R.id.shipped:
                                    sendNotification(order, R.string.shippedNotification, OrdersUtil.ORDER_SHIPPED);
                                    break;
                                case R.id.delivered:
                                    sendNotification(order, R.string.deliveredNotification, OrdersUtil.ORDER_DELIVERED);
                                    break;
                                case R.id.delayed:
                                    sendNotification(order, R.string.delayedNotification, OrdersUtil.ORDER_DELAYED);
                                    break;
                                case R.id.cancel:
                                    sendNotification(order, 0, OrdersUtil.ORDER_CANCELLED);
                                    break;
                                case R.id.returnOrder:
                                    sendNotification(order, -1, OrdersUtil.ORDER_REQUESTED_RETURN);
                                    break;
                                case R.id.returnAccepted:
                                    sendNotification(order, R.string.returnOrderAccepted, OrdersUtil.ORDER_RETURNED);
                                    break;
                            }
                        }
                        else
                        {
                            toast(mActivity.getString(R.string.noInternet));
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void sendNotification(Order model, int msgResourceId, String status)
    {
        model.setOrderStatus(status);
        NotificationDialogFragment fragment = NotificationDialogFragment.getInstance(
                model, mGoogleId, msgResourceId);

        fragment.show(((AppCompatActivity) mActivity).getSupportFragmentManager(),
                NotificationDialogFragment.TAG);
    }
}