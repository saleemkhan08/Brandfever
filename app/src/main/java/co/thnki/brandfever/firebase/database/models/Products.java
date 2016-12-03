package co.thnki.brandfever.firebase.database.models;

import android.os.Bundle;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;

public class Products
{
    public static final String PHOTO_URL = "photoUrlList";
    public static final String PHOTO_NAME = "photoNameList";
    public static final String ORDER_STATUS = "orderStatus";
    public static final String PRODUCT_ID = "productId";
    public static final String CATEGORY_ID = "categoryId";
    public static final String TIME_STAMP = "timeStamp";

    private String categoryId;
    private String productId;

    private String brand;
    private String priceAfter;
    private Map<String, Integer> sizesMap;
    private String material;
    private String priceBefore;
    private ArrayList<String> photoUrlList;
    private ArrayList<String> photoNameList;
    private static final String NOT_SPECIFIED = "Not Specified";
    private long timeStamp;

    public Products()
    {
    }

    public Products(String photoUrl, String photoName, String categoryId, String key)
    {
        photoUrlList = new ArrayList<>();
        photoNameList = new ArrayList<>();
        photoNameList.add(photoName);
        photoUrlList.add(photoUrl);
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
        timeStamp = - System.currentTimeMillis();
    }

    public static String generateRandomKey()
    {
        int length = 6;
        Random random = new SecureRandom();
        return String.format("%" + length + "s", new BigInteger(length * 5, random)
                .toString(32)).replace('\u0020', '0');
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

    public int getActualPriceAfter()
    {
        String price = priceAfter.replaceAll("[^\\d]", "");
        return Integer.parseInt(price);
    }

    public int getActualPriceBefore()
    {
        String price = priceBefore.replaceAll("[^\\d]", "");
        return Integer.parseInt(price);
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

    public ArrayList<String> getPhotoUrlList()
    {
        return photoUrlList;
    }

    public void setPhotoUrlList(ArrayList<String> photoUrlList)
    {
        this.photoUrlList = photoUrlList;
    }

    public ArrayList<String> getPhotoNameList()
    {
        return photoNameList;
    }

    public void setPhotoNameList(ArrayList<String> photoNameList)
    {
        this.photoNameList = photoNameList;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }
}