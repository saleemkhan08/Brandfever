package co.thnki.brandfever.interfaces;

public interface DrawerItemClickListener
{
    boolean ENTER = true;
    boolean EXIT = false;
    void onFirstLevelItemClick(String category, boolean isEntering);
    void onSecondLevelItemClick(String category);
    void onEditClick(String category);
    void onBackClick();
}
