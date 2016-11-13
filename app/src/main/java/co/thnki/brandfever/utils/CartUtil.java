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
 *      a. getInstance() $ updateCartList() - > onActivityResult()
 *      b. clearInstance() -> setUpLogin()
 *
 * 2. Product Activity
 *      a. getInstance() -> onCreate()
 *      b. isAddedToCart() -> onCreate() used to update UI
 *      c. placeAnOrder() -> placeAnOrder()
 *      d. removeFromCart() -> placeAnOrder()
 *
 * 2. Product Activity
 *      a. placeAnOrder() ->
 *      b. removeFromCart()
 *      c. isAddedToCart()
 *
 * 3. Products Fragment
 *      a. placeAnOrder()
 *      b. removeFromCart()
 *      c. isAddedToCart()
 */
public class CartUtil
{
    public static final String CART_LIST = "CartList";
    private DatabaseReference mCartRef;
    private static CartUtil sInstance;
    private SharedPreferences mPreferences;

    private CartUtil()
    {
        mPreferences = Brandfever.getPreferences();
        String googleId = mPreferences.getString(Accounts.GOOGLE_ID, "dummyId");
        Log.d(CartUtil.CART_LIST, "googleId : " + googleId);
        mCartRef = FirebaseDatabase.getInstance().getReference()
                .child(googleId).child(CART_LIST);
    }

    public static CartUtil getsInstance()
    {
        if (sInstance == null)
        {
            sInstance = new CartUtil();
        }
        return sInstance;
    }

    public static void clearInstance()
    {
        sInstance = null;
    }

    public String addToCart(ProductBundle productBundle)
    {
        String key = getKey(productBundle);
        DatabaseReference reference = mCartRef.child(key);

        mPreferences.edit().putBoolean(key, true).apply();
        reference.setValue(new Products(productBundle));
        return key;
    }

    public void removeFromCart(Products products)
    {
        String key = getKey(products);
        mPreferences.edit().remove(key).apply();
        mCartRef.child(key).removeValue();
    }

    public String addToCart(Products products)
    {
        String key = getKey(products);
        DatabaseReference reference = mCartRef.child(key);

        mPreferences.edit().putBoolean(key, true).apply();
        reference.setValue(products);
        return key;
    }

    public void removeFromCart(ProductBundle productBundle)
    {
        String key = getKey(productBundle);

        mPreferences.edit().remove(key).apply();
        mCartRef.child(key).removeValue();
    }

    public void updateCartList()
    {
        mCartRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Products product = snapshot.getValue(Products.class);
                    String key = getKey(product);
                    Log.d(CartUtil.CART_LIST, "key : " + key);
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

    public boolean isAddedToCart(ProductBundle productBundle)
    {
        String key = getKey(productBundle);
        boolean isFav = mPreferences.getBoolean(key, false);
        Log.d(CartUtil.CART_LIST, "isAddedToCart : " + isFav);
        return isFav;
    }

    public boolean isAddedToCart(Products product)
    {
        String key = getKey(product);
        boolean isFav = mPreferences.getBoolean(key, false);
        Log.d(CartUtil.CART_LIST, "isAddedToCart : " + isFav);
        return isFav;
    }

    public String getKey(ProductBundle product)
    {
        return product.getCategoryId() + "_" + product.getProductId();
    }

    public static String getKey(Products product)
    {
        return product.getCategoryId() + "_" + product.getProductId();
    }

    public boolean toggleCart(Products model)
    {
        boolean isAddedToCart = isAddedToCart(model);
        if(isAddedToCart)
        {
            removeFromCart(model);
        }
        else
        {
            addToCart(model);
        }

        return !isAddedToCart;
    }
}
