package co.thnki.brandfever.firebase.database.models;

public class FavoriteProduct
{
    public static final String TIME_STAMP = "timeStamp";
    private String brand;
    private String photoUrl;
    private String priceAfter;
    private String productId;
    private String categoryId;
    private long timeStamp;
    private String priceBefore;

    public FavoriteProduct(Products product)
    {
        brand = product.getBrand();
        photoUrl = product.getPhotoUrlList().get(0);
        priceAfter = product.getPriceAfter();
        productId = product.getProductId();
        categoryId = product.getCategoryId();
        timeStamp = - System.currentTimeMillis();
        priceBefore = product.getPriceBefore();
    }

    public FavoriteProduct()
    {

    }

    public String getBrand()
    {
        return brand;
    }

    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    public String getPhotoUrl()
    {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl)
    {
        this.photoUrl = photoUrl;
    }

    public String getPriceAfter()
    {
        return priceAfter;
    }

    public void setPriceAfter(String priceAfter)
    {
        this.priceAfter = priceAfter;
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

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String getPriceBefore()
    {
        return priceBefore;
    }

    public void setPriceBefore(String priceBefore)
    {
        this.priceBefore = priceBefore;
    }
}
