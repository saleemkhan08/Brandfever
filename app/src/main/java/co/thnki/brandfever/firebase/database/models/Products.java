package co.thnki.brandfever.firebase.database.models;

import android.os.Bundle;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;

public class Products
{
    private static final Random random = new SecureRandom();
    public static final String PHOTO_URL = "photoUrlMap";
    public static final String PRODUCT_MODEL = "productModel";
    private static final String PRODUCT_IMAGE_KEY = "productImageKey";

    private String categoryId;
    private String productId;

    private String brand;
    private String priceAfter;
    private Map<String, Integer> sizesMap;
    private String material;
    private String priceBefore;
    private Map<String, String> photoUrlMap;
    private static final String NOT_SPECIFIED = "Not Specified";

    public Products()
    {
    }

    public Products(String photoUrl, String categoryId, String key)
    {
        photoUrlMap = new LinkedHashMap<>();
        photoUrlMap.put("00_photo", photoUrl);
        this.categoryId = categoryId;
        brand = Brandfever.APP_NAME;
        priceAfter = "1000";
        priceBefore = "1500";

        sizesMap = new LinkedHashMap<>();
        sizesMap.put(Brandfever.getResString(R.string.xs), 0);
        sizesMap.put(Brandfever.getResString(R.string.s), 0);
        sizesMap.put(Brandfever.getResString(R.string.m), 0);
        sizesMap.put(Brandfever.getResString(R.string.l), 0);
        sizesMap.put(Brandfever.getResString(R.string.xl), 0);
        sizesMap.put(Brandfever.getResString(R.string.xxl), 0);
        material = NOT_SPECIFIED;
        productId = key;
    }

    public Products(ProductBundle bundle)
    {
        photoUrlMap = convertBundleToStringMap(bundle.getPhotoUrlMap());
        this.categoryId = bundle.getCategoryId();
        brand = bundle.getBrand();
        priceAfter = bundle.getPriceAfter();
        sizesMap = convertBundleToIntMap(bundle.getSizesMap());
        material = bundle.getMaterial();
        priceBefore = bundle.getPriceBefore();
        productId = bundle.getProductId();
    }
    private Map<String, Integer> convertBundleToIntMap(Bundle bundle)
    {
        Map<String, Integer> map = new HashMap<>();
        for (String key : bundle.keySet())
        {
            map.put(key, bundle.getInt(key));
        }
        return map;
    }

    public static Map<String, String> convertBundleToStringMap(Bundle bundle)
    {
        Map<String, String> map = new HashMap<>();
        for (String key : bundle.keySet())
        {
            map.put(key, bundle.getString(key));
        }
        return map;
    }

    public String getCategoryId()
    {
        return categoryId;
    }

    public String getProductId()
    {
        return productId;
    }

    public void setProductId(String productId)
    {
        this.productId = productId;
    }

    public void setCategoryId(String categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getBrand()
    {
        return brand;
    }

    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    public String getPriceAfter()
    {
        String priceTemp = priceAfter.replace('\u20B9' + "", "");
        return '\u20B9' + priceTemp;
    }

    public void setPriceAfter(String priceAfter)
    {
        this.priceAfter = priceAfter;
    }

    public Map<String, Integer> getSizesMap()
    {
        return sizesMap;
    }

    public void setSizesMap(Map<String, Integer> sizesMap)
    {
        this.sizesMap = sizesMap;
    }

    public String getMaterial()
    {
        return material;
    }

    public void setMaterial(String material)
    {
        this.material = material;
    }

    public String getPriceBefore()
    {
        String priceTemp = priceBefore.replace('\u20B9' + "", "");
        return '\u20B9' + priceTemp;
    }

    public void setPriceBefore(String priceBefore)
    {
        this.priceBefore = priceBefore;
    }

    public Map<String, String> getPhotoUrlMap()
    {
        return photoUrlMap;
    }

    public void setPhotoUrlMap(Map<String, String> photoUrlMap)
    {
        this.photoUrlMap = photoUrlMap;
    }
}