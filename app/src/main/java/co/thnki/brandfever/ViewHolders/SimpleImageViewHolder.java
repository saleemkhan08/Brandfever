package co.thnki.brandfever.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import co.thnki.brandfever.R;

public class SimpleImageViewHolder extends RecyclerView.ViewHolder
{
    public ImageView mImageView;
    public ImageView mSelectedImageView;

    public SimpleImageViewHolder(View itemView)
    {
        super(itemView);
        mImageView = (ImageView) itemView.findViewById(R.id.productImage);
        mSelectedImageView = (ImageView) itemView.findViewById(R.id.productImageSelected);
    }
}
