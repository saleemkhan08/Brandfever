package co.thnki.brandfever.firebase.database.models;

import java.util.Date;

@SuppressWarnings({"WeakerAccess", "unused"})
public class SubCategory
{
    private String subCategory;
    private int subCategoryCode;
    private long timeStamp;

    public SubCategory()
    {

    }

    public SubCategory(String subCategory)
    {
        timeStamp = -1 * new Date().getTime();
        this.subCategory = subCategory;
    }

    public String getSubCategory()
    {
        return subCategory;
    }

    public void setSubCategory(String subCategory)
    {
        this.subCategory = subCategory;
    }

    public int getSubCategoryCode()
    {
        return subCategoryCode;
    }

    public void setSubCategoryCode(int subCategoryCode)
    {
        this.subCategoryCode = subCategoryCode;
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
