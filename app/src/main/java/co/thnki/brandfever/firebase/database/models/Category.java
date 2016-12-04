package co.thnki.brandfever.firebase.database.models;

import java.util.Date;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Category
{
    private String categoryName;
    private String category;
    private long timeStamp;
    private int categoryId;
    private String parentCategory;
    private String categoryImage;
    private boolean categorySelected;

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getParentCategory()
    {
        return parentCategory;
    }

    public void setParentCategory(String parentCategory)
    {
        this.parentCategory = parentCategory;
    }

    public String getCategory()
    {
        return category;
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

    public Category(String categoryName, int i, String parentCategory, String childCategory, String categoriesImageUrl)
    {
        timeStamp = new Date().getTime();
        this.categoryName = categoryName;
        this.categoryId = i;
        this.category = childCategory;
        this.categoryImage = categoriesImageUrl;
        this.parentCategory = parentCategory;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
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
