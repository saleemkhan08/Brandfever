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
import co.thnki.brandfever.firebase.database.models.Order;
import co.thnki.brandfever.firebase.database.models.Products;

/**
 * 1. Login Activity
 * a. getInstance() $ updateCartList() - > onActivityResult()
 * b. clearInstance() -> setUpLogin()
 * <p>
 * 2. Product Activity
 * a. getInstance() -> onCreate()
 * b. isAddedToCart() -> onCreate() used to update UI
 * c. placeAnOrder() -> placeAnOrder()
 * d. removeFromCart() -> placeAnOrder()
 * <p>
 * 2. Product Activity
 * a. placeAnOrder() ->
 * b. removeFromCart()
 * c. isAddedToCart()
 * <p>
 * 3. Products Fragment
 * a. placeAnOrder()
 * b. removeFromCart()
 * c. isAddedToCart()
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

    public String addToCart(Products product, String selectedSize)
    {
        Order order = new Order(product, OrdersUtil.ORDER_ADDED_TO_CART, selectedSize);
        String key = getKey(product, selectedSize);
        DatabaseReference reference = mCartRef.child(key);
        reference.setValue(order);
        return key;
    }

    public String getKey(Order order)
    {
        return order.getCategoryId() + "_" + order.getProductId() + "_" + order.getSelectedSize();
    }

    private String getKey(Products product, String selectedSize)
    {
        return product.getCategoryId() + "_" + product.getProductId() + "_" + selectedSize;
    }

    public void removeFromCart(Order order)
    {
        String key = getKey(order);
        mPreferences.edit().remove(key).apply();
        mCartRef.child(key).removeValue();
    }

    public String addToCart(Order products)
    {
        String key = getKey(products);
        DatabaseReference reference = mCartRef.child(key);

        mPreferences.edit().putBoolean(key, true).apply();
        reference.setValue(products);
        return key;
    }

    public void updateCartList()
    {
        mCartRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        Order order = snapshot.getValue(Order.class);
                        String key = getKey(order);
                        Log.d(CartUtil.CART_LIST, "key : " + key);
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

    public boolean isAddedToCart(Order productBundle)
    {
        String key = getKey(productBundle);
        boolean isFav = mPreferences.getBoolean(key, false);
        Log.d(CartUtil.CART_LIST, "isAddedToCart : " + isFav);
        return isFav;
    }

    public boolean toggleCart(Order model)
    {
        boolean isAddedToCart = isAddedToCart(model);
        if (isAddedToCart)
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
