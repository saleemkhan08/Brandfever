package co.thnki.brandfever.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.R;
import co.thnki.brandfever.view.holders.DrawerCategoryEditorViewHolder;
import co.thnki.brandfever.firebase.database.models.Category;
import co.thnki.brandfever.interfaces.Const;
import co.thnki.brandfever.interfaces.DrawerItemClickListener;
import co.thnki.brandfever.singletons.Otto;

import static co.thnki.brandfever.interfaces.Const.CATEGORY_ID;

/**
 * Fragment handles the drawer menu.
 */

public class CategoryEditorFragment extends Fragment
{
    @Bind(R.id.categoryRecyclerView)
    RecyclerView mCategoriesRecyclerView;

    private DatabaseReference mAvailableCategoriesRef;
    private DatabaseReference mCategoriesRef;
    private DrawerItemClickListener mItemClickListener;

    public static CategoryEditorFragment getInstance(String category, DrawerItemClickListener itemClickListener )
    {
        CategoryEditorFragment fragment = new CategoryEditorFragment();
        fragment.mCategoriesRef = FirebaseDatabase.getInstance().getReference().child(category);
        fragment.mAvailableCategoriesRef = FirebaseDatabase.getInstance().getReference()
                .child(Const.AVAILABLE_+category);
        fragment.mItemClickListener = itemClickListener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View layout = inflater.inflate(R.layout.fragment_drawer_category_editor, container, false);
        ButterKnife.bind(this, layout);
        Otto.register(this);

        mCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCategoriesRecyclerView.setAdapter(getAdapter());
        return layout;
    }

    private RecyclerView.Adapter getAdapter()
    {
        return new FirebaseRecyclerAdapter<Category, DrawerCategoryEditorViewHolder>(
                Category.class,
                R.layout.category_editor_list_item,
                DrawerCategoryEditorViewHolder.class,
                mCategoriesRef.orderByChild(CATEGORY_ID))
        {
            @Override
            protected void populateViewHolder(final DrawerCategoryEditorViewHolder viewHolder, final Category model, int position)
            {
                viewHolder.mCheckedTextView.setText(model.getCategory());
                viewHolder.mCheckedTextView.setText(model.getCategory());
                viewHolder.mCheckedTextView.setChecked(model.isCategorySelected());
                viewHolder.mCheckedTextView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        int catId = model.getCategoryId();
                        CheckedTextView category = (CheckedTextView) view;
                        if (category.isChecked())
                        {
                            mAvailableCategoriesRef.child(catId+"").removeValue();
                            category.setChecked(false);
                            mCategoriesRef.child(catId+"").child("categorySelected").setValue(false);
                        }
                        else
                        {
                            category.setChecked(true);
                            mAvailableCategoriesRef.child(catId+"").setValue(model);
                            mCategoriesRef.child(catId+"").child("categorySelected").setValue(true);
                        }
                    }
                });
                String imageUrl = model.getCategoryImage();
                if (imageUrl != null && !imageUrl.isEmpty())
                {
                    Glide.with(getActivity()).load(imageUrl)
                            .asBitmap().centerCrop().into(new BitmapImageViewTarget(viewHolder.mImageView)
                    {
                        @Override
                        protected void setResource(Bitmap resource)
                        {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            viewHolder.mImageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }else
                {
                    viewHolder.mImageView.setVisibility(View.GONE);
                }
            }
        };
    }

    @OnClick(R.id.backIcon)
    public void back()
    {
        mItemClickListener.onBackClick();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }
}
