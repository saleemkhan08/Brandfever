package co.thnki.brandfever.adapters;

import android.app.Activity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.view.holders.AccountsViewHolder;

import static co.thnki.brandfever.Brandfever.toast;

public class UsersAdapter extends FirebaseRecyclerAdapter<Accounts, AccountsViewHolder>
{
    private Activity mActivity;

    public static UsersAdapter getInstance(DatabaseReference reference, Activity activity)
    {
        return new UsersAdapter(Accounts.class,
                R.layout.user_list_row, AccountsViewHolder.class, reference, activity);
    }

    private UsersAdapter(Class<Accounts> modelClass, int modelLayout, Class<AccountsViewHolder> viewHolderClass,
                         Query ref, Activity activity)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mActivity = activity;
    }

    @Override
    protected void populateViewHolder(final AccountsViewHolder viewHolder, final Accounts model, int position)
    {
        String imageUrl = model.photoUrl;
        Glide.with(mActivity).load(imageUrl)
                .asBitmap().placeholder(R.mipmap.user_icon_accent)
                .centerCrop().into(viewHolder.mImageView);

        viewHolder.mUsername.setText(model.name);
        viewHolder.mUserEmail.setText(model.email);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
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
}
