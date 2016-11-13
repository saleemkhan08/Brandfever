package co.thnki.brandfever.view.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.R;

public class AccountsViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.userImage)
    public ImageView mImageView;

    @Bind(R.id.username)
    public TextView mUsername;

    @Bind(R.id.userEmail)
    public TextView mUserEmail;

    /*@Bind(R.id.userPhoneNo)
    public ImageView mUserPhoneNo;*/

    public View mItemView;

    public AccountsViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}
