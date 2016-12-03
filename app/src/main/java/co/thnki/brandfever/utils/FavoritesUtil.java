package co.thnki.brandfever.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.FavoriteProduct;
import co.thnki.brandfever.firebase.database.models.Products;

public class FavoritesUtil
{
    public static final String FAV_LIST = "favList";
    private DatabaseReference mFavRef;
    private static FavoritesUtil sInstance;
    private SharedPreferences mPreferences;

    private FavoritesUtil()
    {
        mPreferences = Brandfever.getPreferences();
        String googleId = mPreferences.getString(Accounts.GOOGLE_ID, "dummyId");
        Log.d(FavoritesUtil.FAV_LIST, "googleId : " + googleId);
        mFavRef = FirebaseDatabase.getInstance().getReference()
                .child(googleId).child(FAV_LIST);
    }

    public static FavoritesUtil getsInstance()
    {
        if (sInstance == null)
        {
            sInstance = new FavoritesUtil();
        }
        return sInstance;
    }

    public static void clearInstance()
    {
        sInstance = null;
    }

    public void removeFromFavorites(FavoriteProduct product)
    {
        String key = getKey(product);
        mPreferences.edit().remove(key).apply();
        mFavRef.child(key).removeValue();
    }

    public void removeFromFavorites(Products product)
    {
        FavoriteProduct favoriteProduct = new FavoriteProduct(product);
        String key = getKey(favoriteProduct);
        mPreferences.edit().remove(key).apply();
        mFavRef.child(key).removeValue();
    }

    public String addToFavorites(Products products)
    {
        FavoriteProduct favoriteProduct = new FavoriteProduct(products);
        String key = getKey(favoriteProduct);
        DatabaseReference reference = mFavRef.child(key);

        mPreferences.edit().putBoolean(key, true).apply();
        reference.setValue(favoriteProduct);
        return key;
    }

    public void updateFavoriteList()
    {
        mFavRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        FavoriteProduct product = snapshot.getValue(FavoriteProduct.class);
                        String key = getKey(product);
                        Log.d(FavoritesUtil.FAV_LIST, "key : " + key);
                        mPreferences.edit()
                                .putBoolean(key, true)
                                .apply();
                    }
                }
                catch (Exception e)
                {
                    Log.d("Exception", e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public boolean isFavorite(Products product)
    {
        FavoriteProduct favoriteProduct = new FavoriteProduct(product);
        String key = getKey(favoriteProduct);
        boolean isFav = mPreferences.getBoolean(key, false);
        Log.d(FavoritesUtil.FAV_LIST, "isAddedToCart : " + isFav);
        return isFav;
    }

    private String getKey(FavoriteProduct product)
    {
        return product.getCategoryId() + "_" + product.getProductId();
    }
}
