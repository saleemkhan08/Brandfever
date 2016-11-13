package co.thnki.brandfever.firebase.database.models;

import java.util.Date;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Category
{
    private String category;
    private String categoryName;
    private long timeStamp;
    private int categoryId;
    private String categoryImage;
    private boolean categorySelected;

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategorySelected(boolean categorySelected)
    {
        this.categorySelected = categorySelected;
    }

    public boolean isCategorySelected()
    {
        return categorySelected;
    }

    public void setCategoryId(int categoryId)
    {
        this.categoryId = categoryId;
    }

    public int getCategoryId()
    {
        return categoryId;
    }

    public Category()
    {

    }

    public Category(String category, int i, String childId, String categoriesImageUrl)
    {
        timeStamp = new Date().getTime();
        this.category = category;
        this.categoryId = i;
        this.categoryName = childId;
        this.categoryImage = categoriesImageUrl;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String getCategoryImage()
    {
        return categoryImage;
    }

    public void setCategoryImage(String categoryImage)
    {
        this.categoryImage = categoryImage;
    }
}
