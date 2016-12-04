package co.thnki.brandfever.interfaces;

public interface Const
{
    String AVAILABLE_ = "available_";

    String ALL_CATEGORIES = "allCategories";
    String AVAILABLE_CATEGORIES = "availableCategories";

    String CATEGORIES = "categories";
    String ARE_CATEGORIES_ADDED = "areCategoriesAdded";

    String FIRST_LEVEL_CATEGORIES = "firstLevelCategories";
    String MENS_WEAR = FIRST_LEVEL_CATEGORIES + "_mensWear";
    String WOMENS_WEAR = FIRST_LEVEL_CATEGORIES + "_womensWear";
    String KIDS_WEAR = FIRST_LEVEL_CATEGORIES + "_kidsWear";
    String FASHION_ACCESSORIES = FIRST_LEVEL_CATEGORIES + "_fashionAccessories";
    String HOME_FURNISHING = FIRST_LEVEL_CATEGORIES + "_homeFurnishing";

    String AVAILABLE_FIRST_LEVEL_CATEGORIES = AVAILABLE_ + FIRST_LEVEL_CATEGORIES;
    String AVAILABLE_MENS_WEAR = AVAILABLE_FIRST_LEVEL_CATEGORIES + "_mensWear";
    String AVAILABLE_WOMENS_WEAR = AVAILABLE_FIRST_LEVEL_CATEGORIES + "_womensWear";
    String AVAILABLE_KIDS_WEAR = AVAILABLE_FIRST_LEVEL_CATEGORIES + "_kidsWear";
    String AVAILABLE_FASHION_ACCESSORIES = AVAILABLE_FIRST_LEVEL_CATEGORIES + "_fashionAccessories";
    String AVAILABLE_HOME_FURNISHING = AVAILABLE_FIRST_LEVEL_CATEGORIES + "_homeFurnishing";

    String NO_OF_TABS = "noOfTabs";

    String CATEGORY_ID = "categoryId";
}
