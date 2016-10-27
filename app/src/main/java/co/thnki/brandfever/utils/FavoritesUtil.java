package co.thnki.brandfever.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.firebase.database.models.ProductBundle;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.firebase.database.models.Accounts;
/**
 * 1. Login Activity
 *      a. getInstance() $ updateFavoriteList() - > onActivityResult()
 *      b. clearInstance() -> setUpLogin()
 *
 * 2. Product Activity
 *      a. getInstance() -> onCreate()
 *      b. isFavorite() -> onCreate() used to update UI
 *      c. addToFavorite() -> addToFavorite()
 *      d. removeFromFavorite() -> addToFavorite()
 *
 * 3. Products Fragment
 *      a. addToCart()
 *      b. removeFromCart()
 *      c. isAddedToCart()
 */
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

    public String addToFavorites(ProductBundle productBundle)
    {
        String key = getKey(productBundle);
        DatabaseReference reference = mFavRef.child(key);

        mPreferences.edit().putBoolean(key, true).apply();
        reference.setValue(new Products(productBundle));
        return key;
    }

    public void removeFromFavorites(Products products)
    {
        String key = getKey(products);
        mPreferences.edit().remove(key).apply();
        mFavRef.child(key).removeValue();
    }

    public String addToFavorites(Products products)
    {
        String key = getKey(products);
        DatabaseReference reference = mFavRef.child(key);

        mPreferences.edit().putBoolean(key, true).apply();
        reference.setValue(products);
        return key;
    }

    public void removeFromFavorites(ProductBundle productBundle)
    {
        String key = getKey(productBundle);

        mPreferences.edit().remove(key).apply();
        mFavRef.child(key).removeValue();
    }

    public void updateFavoriteList()
    {
        mFavRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Products product = snapshot.getValue(Products.class);
                    String key = getKey(product);
                    Log.d(FavoritesUtil.FAV_LIST, "key : " + key);
                    mPreferences.edit()
                            .putBoolean(key, true)
                            .apply();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public boolean isFavorite(ProductBundle productBundle)
    {
        String key = getKey(productBundle);
        boolean isFav = mPreferences.getBoolean(key, false);
        Log.d(FavoritesUtil.FAV_LIST, "isAddedToCart : " + isFav);
        return isFav;
    }

    public boolean isFavorite(Products product)
    {
        String key = getKey(product);
        boolean isFav = mPreferences.getBoolean(key, false);
        Log.d(FavoritesUtil.FAV_LIST, "isAddedToCart : " + isFav);
        return isFav;
    }

    private String getKey(ProductBundle product)
    {
        return product.getCategoryId() + "_" + product.getProductId();
    }

    private String getKey(Products product)
    {
        return product.getCategoryId() + "_" + product.getProductId();
    }

    public boolean toggleFavorite(Products model)
    {
        boolean isFavorite = isFavorite(model);
        if(isFavorite)
        {
            removeFromFavorites(model);
        }
        else
        {
            addToFavorites(model);
        }
        return !isFavorite;
    }
}
