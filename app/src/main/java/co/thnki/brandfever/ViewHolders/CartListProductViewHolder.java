package co.thnki.brandfever.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.R;

public class CartListProductViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.productImage)
    public ImageView mImageView;

    @Bind(R.id.productBrand)
    public TextView mBrand;

    @Bind(R.id.productPriceAfter)
    public TextView mPriceAfter;

    @Bind(R.id.productPriceBefore)
    public TextView mPriceBefore;

    @Bind(R.id.productSize)
    public TextView mProductSize;

    @Bind(R.id.noOfPieces)
    public TextView mNoOfPieces;

    @Bind(R.id.toggleCart)
    public ImageView mToggleCart;

    public View mItemView;

    public CartListProductViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}
