package co.thnki.brandfever.interfaces;

public interface Const
{
    String AVAILABLE_ = "available_";

    String ALL_CATEGORIES = "allCategories";
    String AVAILABLE_CATEGORIES = "availableCategories";

    String CATEGORIES = "categories";
    String ARE_CATEGORIES_ADDED = "areCategoriesAdded";

    String FIRST_LEVEL_CATEGORIES = "firstLevelCategories";
    String MENS_WEAR = "mensWear";
    String WOMENS_WEAR = "womensWear";
    String KIDS_WEAR = "kidsWear";
    String FASHION_ACCESSORIES = "fashionAccessories";
    String HOME_FURNISHING = "homeFurnishing";

    String AVAILABLE_FIRST_LEVEL_CATEGORIES = AVAILABLE_+FIRST_LEVEL_CATEGORIES;
    String AVAILABLE_MENS_WEAR = AVAILABLE_FIRST_LEVEL_CATEGORIES+"_"+MENS_WEAR;
    String AVAILABLE_WOMENS_WEAR = AVAILABLE_FIRST_LEVEL_CATEGORIES+"_"+WOMENS_WEAR;
    String AVAILABLE_KIDS_WEAR = AVAILABLE_FIRST_LEVEL_CATEGORIES+"_"+KIDS_WEAR;
    String AVAILABLE_FASHION_ACCESSORIES = AVAILABLE_FIRST_LEVEL_CATEGORIES+"_"+FASHION_ACCESSORIES;
    String AVAILABLE_HOME_FURNISHING = AVAILABLE_FIRST_LEVEL_CATEGORIES+"_"+HOME_FURNISHING;

    String NO_OF_TABS = "noOfTabs";

    String CATEGORY_ID = "categoryId";
}
