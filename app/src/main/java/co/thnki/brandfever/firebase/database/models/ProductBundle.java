package co.thnki.brandfever.firebase.database.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class ProductBundle implements Parcelable
{
    private String categoryId;
    private String productId;
    private String brand;
    private String priceAfter;
    private Bundle sizesMap;
    private String material;
    private String priceBefore;
    private Bundle photoUrlMap;

    public ProductBundle()
    {

    }

    public ProductBundle(Products products)
    {
        photoUrlMap = convertStringMapToBundle(products.getPhotoUrlMap());
        this.categoryId = products.getCategoryId();
        brand = products.getBrand();
        priceAfter = products.getPriceAfter();
        sizesMap = convertIntMapToBundle(products.getSizesMap());
        material = products.getMaterial();
        priceBefore = products.getPriceBefore();
        productId = products.getProductId();
    }

    private Bundle convertIntMapToBundle(Map<String, Integer> map)
    {
        Bundle bundle = new Bundle();
        for(String key : map.keySet())
        {
            bundle.putInt(key, map.get(key));
        }
        return bundle;
    }

    private Bundle convertStringMapToBundle(Map<String, String> map)
    {
        Bundle bundle = new Bundle();
        for(String key : map.keySet())
        {
            bundle.putString(key, map.get(key));
        }
        return bundle;
    }

    public String getProductId()
    {
        return productId;
    }

    public void setProductId(String productId)
    {
        this.productId = productId;
    }

    public String getCategoryId()
    {
        return categoryId;
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
        return priceAfter;
    }

    public void setPriceAfter(String priceAfter)
    {
        this.priceAfter = priceAfter;
    }

    public Bundle getSizesMap()
    {
        return sizesMap;
    }

    public void setSizesMap(Bundle sizesMap)
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
        return priceBefore;
    }

    public void setPriceBefore(String priceBefore)
    {
        this.priceBefore = priceBefore;
    }

    public Bundle getPhotoUrlMap()
    {
        return photoUrlMap;
    }

    public void setPhotoUrlMap(Bundle  photoUrlMap)
    {
        this.photoUrlMap = photoUrlMap;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.categoryId);
        dest.writeString(this.brand);
        dest.writeString(this.priceAfter);
        dest.writeBundle(sizesMap);
        dest.writeString(this.material);
        dest.writeString(this.priceBefore);
        dest.writeString(this.productId);
        dest.writeBundle(this.photoUrlMap);
    }

    protected ProductBundle(Parcel in)
    {
        this.categoryId = in.readString();
        this.brand = in.readString();
        this.priceAfter = in.readString();
        sizesMap = in.readBundle();
        this.material = in.readString();
        this.priceBefore = in.readString();
        this.productId = in.readString();
        this.photoUrlMap = in.readBundle();
    }

    public static final Parcelable.Creator<ProductBundle> CREATOR = new Parcelable.Creator<ProductBundle>()
    {
        @Override
        public ProductBundle createFromParcel(Parcel source)
        {
            return new ProductBundle(source);
        }

        @Override
        public ProductBundle[] newArray(int size)
        {
            return new ProductBundle[size];
        }
    };
}