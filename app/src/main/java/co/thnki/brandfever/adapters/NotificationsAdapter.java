package co.thnki.brandfever.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.NotificationModel;
import co.thnki.brandfever.fragments.NotificationListFragment;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.UserUtil;
import co.thnki.brandfever.view.holders.NotificationViewHolder;

import static co.thnki.brandfever.Brandfever.toast;

public class NotificationsAdapter extends FirebaseRecyclerAdapter<NotificationModel, NotificationViewHolder>
{
    private Activity mActivity;
    public static NotificationsAdapter getInstance(DatabaseReference reference, Activity activity)
    {
        Log.d(NotificationListFragment.TAG, "NotificationsAdapter - getInstance");
        return new NotificationsAdapter(NotificationModel.class,
                R.layout.notification_list_row, NotificationViewHolder.class, reference, activity);
    }

    private NotificationsAdapter(Class<NotificationModel> modelClass, int modelLayout, Class<NotificationViewHolder> viewHolderClass,
                                 Query ref, Activity activity)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.d(NotificationListFragment.TAG, "NotificationsAdapter");
        mActivity = activity;
    }

    @Override
    protected void populateViewHolder(final NotificationViewHolder viewHolder, final NotificationModel model, int position)
    {
        Log.d(NotificationListFragment.TAG, "populateViewHolder");
        viewHolder.mNotificationMsg.setText(model.notification);

        boolean isNotificationSentByOwner = UserUtil.getInstance().isOwner(model.googleId);
        boolean isCurrentUserOwner = Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false);

        if(isNotificationSentByOwner)
        {
            viewHolder.mUserImageView.setImageResource(R.mipmap.app_icon);
        }
        else
        {
            Glide.with(mActivity).load(model.photoUrl)
                    .asBitmap().placeholder(R.mipmap.user_icon_accent)
                    .centerCrop().into(viewHolder.mUserImageView);
        }

        if (model.action.equals(StoreActivity.ORDER_PLACED))
        {
            viewHolder.mUsername.setText(model.username);
            viewHolder.mNotificationImageView.setImageResource(R.mipmap.shopping_cart);
            viewHolder.mItemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (ConnectivityUtil.isConnected())
                    {
                        ((StoreActivity) mActivity).showUserOrdersFragment(model.googleId);
                    }
                    else
                    {
                        toast(R.string.noInternet);
                    }
                }
            });
        }
        else if (model.action.equals(StoreActivity.ORDER_CANCELLED))
        {
            String cancellationTitle = model.username+" "+mActivity.getString(R.string.cancelledAnOrder);
            viewHolder.mUsername.setText(cancellationTitle);
            viewHolder.mNotificationImageView.setImageResource(R.mipmap.ic_remove_shopping_cart_black_48dp);
        }

    }
}
