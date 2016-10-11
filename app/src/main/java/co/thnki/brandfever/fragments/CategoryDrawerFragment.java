package co.thnki.brandfever.fragments;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.ViewHolders.SimpleCategoryViewHolder;
import co.thnki.brandfever.firebase.database.models.Category;
import co.thnki.brandfever.interfaces.Const;
import co.thnki.brandfever.interfaces.DrawerItemClickListener;
import co.thnki.brandfever.pojos.Accounts;

import static co.thnki.brandfever.interfaces.Const.AVAILABLE_;
import static co.thnki.brandfever.interfaces.Const.CATEGORY_ID;
import static co.thnki.brandfever.interfaces.DrawerItemClickListener.ENTER;

/**
 * Fragment handles the drawer menu.
 */

public class CategoryDrawerFragment extends Fragment
{
    private static final String LOGIN = "Login";
    @Bind(R.id.categoryRecyclerView)
    RecyclerView mCategoriesRecyclerView;

    @Bind(R.id.backIcon)
    View mHeaderBack;

    @Bind(R.id.profile)
    View mHeaderProfile;

    @Bind(R.id.editCategories)
    View mEditButton;

    @Bind(R.id.profileName)
    TextView mProfileName;

    @Bind(R.id.profilePic)
    ImageView mProfilePic;

    @Bind(R.id.categoryName)
    TextView mCategory;

    private List<String> mFirstLevelArray;
    public String mCurrentCategory;
    private DatabaseReference mAvailableCategoriesRef;
    private DrawerItemClickListener mItemClickListener;

    private Resources mResources;

    public static CategoryDrawerFragment getInstance(String categoryChild, DrawerItemClickListener listener)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        CategoryDrawerFragment fragment = new CategoryDrawerFragment();
        fragment.mItemClickListener = listener;
        fragment.mAvailableCategoriesRef = rootRef.child(categoryChild);
        fragment.mCurrentCategory = categoryChild;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View layout = inflater.inflate(R.layout.fragment_drawer_category, container, false);
        ButterKnife.bind(this, layout);
        mResources = getResources();
        String parentCategory = getParentCategory(mCurrentCategory.replace(AVAILABLE_, ""));
        mCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCategoriesRecyclerView.setAdapter(getAdapter());
        mFirstLevelArray = getFirstLevelArray();
        if (parentCategory != null)
        {
            mHeaderBack.setVisibility(View.VISIBLE);
            mHeaderProfile.setVisibility(View.GONE);
            mCategory.setText(parentCategory);
        }
        else
        {
            mHeaderProfile.setVisibility(View.VISIBLE);
            mHeaderBack.setVisibility(View.GONE);
            SharedPreferences preferences = Brandfever.getPreferences();
            String imageUrl = preferences.getString(Accounts.PHOTO_URL, "");
            if (!imageUrl.isEmpty())
            {
                Glide.with(getActivity()).load(imageUrl)
                        .asBitmap()
                        .placeholder(R.mipmap.user_icon_accent)
                        .centerCrop().into(new BitmapImageViewTarget(mProfilePic)
                {
                    @Override
                    protected void setResource(Bitmap resource)
                    {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(mResources, resource);
                        circularBitmapDrawable.setCircular(true);
                        mProfilePic.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }
            mProfileName.setText(preferences.getString(Accounts.NAME, LOGIN));
        }
        return layout;
    }

    private String getParentCategory(String currentCategory)
    {
        Log.d("getParentCategory", currentCategory);
        List<String> firstLevelCategory = Arrays.asList(mResources.getStringArray(R.array.firstLevelId));
        Log.d("getParentCategory", "" + firstLevelCategory);
        int index = firstLevelCategory.indexOf(currentCategory);
        String[] headerTitle = mResources.getStringArray(R.array.firstLevel);
        if (index >= 0)
        {
            return headerTitle[index];
        }
        return null;
    }

    private List<String> getFirstLevelArray()
    {
        String[] array = getResources().getStringArray(R.array.firstLevelId);
        for (int i = 0; i < array.length; i++)
        {
            array[i] = Const.AVAILABLE_ + array[i];
        }
        return Arrays.asList(array);
    }

    private RecyclerView.Adapter getAdapter()
    {
        return new FirebaseRecyclerAdapter<Category, SimpleCategoryViewHolder>(
                Category.class,
                R.layout.simple_list_item,
                SimpleCategoryViewHolder.class,
                mAvailableCategoriesRef.orderByChild(CATEGORY_ID))
        {
            @Override
            protected void populateViewHolder(final SimpleCategoryViewHolder viewHolder, final Category model, int position)
            {
                viewHolder.mCategory.setText(model.getCategory());
                String imageUrl = model.getCategorySquareImage();
                if (imageUrl != null && !imageUrl.isEmpty())
                {
                    Glide.with(getActivity()).load(imageUrl)
                            .asBitmap().centerCrop().into(new BitmapImageViewTarget(viewHolder.mImageView)
                    {
                        @Override
                        protected void setResource(Bitmap resource)
                        {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mResources, resource);
                            circularBitmapDrawable.setCircular(true);
                            viewHolder.mImageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }
                else
                {
                    viewHolder.mImageView.setVisibility(View.GONE);
                }
                viewHolder.mItemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        String categoryName = getAvailableCategoryName(model);
                        if (mFirstLevelArray.contains(categoryName))
                        {
                            //Otto.post(categoryName);
                            mItemClickListener.onFirstLevelItemClick(categoryName, ENTER);
                        }
                        else
                        {
                            mItemClickListener.onSecondLevelItemClick(categoryName);
                        }
                    }
                });
            }
        };
    }

    private String getAvailableCategoryName(Category model)
    {
        return Const.AVAILABLE_ + model.getCategoryName();
    }

    private String getCategoryName()
    {
        return mCurrentCategory.replace(Const.AVAILABLE_, "");
    }

    @OnClick(R.id.editCategories)
    public void enableEditing()
    {
        //Otto.post(getCategoryName());
        mItemClickListener.onEditClick(getCategoryName());
    }

    @OnClick(R.id.backIcon)
    public void back()
    {
        mItemClickListener.onBackClick();
    }
}