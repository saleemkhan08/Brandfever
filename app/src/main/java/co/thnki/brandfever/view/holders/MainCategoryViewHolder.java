package co.thnki.brandfever.view.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.R;

public class MainCategoryViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.title)
    TextView mTitleTextView;

    @Bind(R.id.uploadCategoryImageButton)
    public LinearLayout mUploadCategoryImageButton;

    @Bind(R.id.uploadCategoryImageIcon)
    public ImageView mUploadCategoryImageIcon;

    @Bind(R.id.backgroundImage)
    public ImageView mBackgroundImageView;


    public MainCategoryViewHolder(View itemView)
    {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setTitleTextView(String categoryName)
    {
        this.mTitleTextView.setText(categoryName);
    }

    public void setBackgroundImageView(int resId)
    {
        this.mBackgroundImageView.setImageResource(resId);
    }
}
