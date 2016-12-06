package co.thnki.brandfever.firebase.database.models;

public class Order
{
    public static final String ORDER_STATUS = "orderStatus";
    public static final String TIME_STAMP = "timeStamp";
    private String brand;
    private String photoUrl;
    private String priceAfter;
    private String selectedSize;
    private int noOfProducts;
    private int availableNoOfProducts;
    private String productId;
    private String categoryId;
    private String orderStatus;
    private long timeStamp;
    private String priceBefore;

    public Order()
    {

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

    public Order(Products product, String orderStatus, String selectedSize)
    {
        brand = product.getBrand();
        photoUrl = product.getPhotoUrlList().get(0);
        priceAfter = product.getPriceAfter();
        priceBefore = product.getPriceBefore();
        this.selectedSize = selectedSize;
        noOfProducts = 1;
        availableNoOfProducts = product.getAvailableNoOfProducts(selectedSize);
        productId = product.getProductId();
        categoryId = product.getCategoryId();
        timeStamp = -System.currentTimeMillis();
        setOrderStatus(orderStatus);
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

    public String getSelectedSize()
    {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize)
    {
        this.selectedSize = selectedSize;
    }

    public int getNoOfProducts()
    {
        return noOfProducts;
    }

    public void setNoOfProducts(int noOfProducts)
    {
        this.noOfProducts = noOfProducts;
    }

    public String getProductId()
    {
        return productId;
    }

    public void setProductId(String productId)
    {
        this.productId = productId;
    }

    public String getOrderStatus()
    {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus)
    {
        this.orderStatus = orderStatus;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(String categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getPriceBefore()
    {
        return priceBefore;
    }

    public void setPriceBefore(String priceBefore)
    {
        this.priceBefore = priceBefore;
    }

    public int getAvailableNoOfProducts()
    {
        return availableNoOfProducts;
    }

    public void setAvailableNoOfProducts(int availableNoOfProducts)
    {
        this.availableNoOfProducts = availableNoOfProducts;
    }
}
