package edu.columbia.cs.watson.newsframe.schema;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 4/28/13
 * Time: 9:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBPediaCategory {

    private static HashMap<String, DBPediaCategory> existingCategories = new HashMap<String,DBPediaCategory>();

    private final String category;

    public static DBPediaCategory getCategory(String category) {
        if (existingCategories.containsKey(category))
            return existingCategories.get(category);

        DBPediaCategory cat = new DBPediaCategory(category);
        existingCategories.put(cat.toString(),cat);
        return cat;

    }

    public static DBPediaCategory getSingletonCategory(String category) {
        return new DBPediaCategory(category);
    }

    private DBPediaCategory(String category) {
        this.category = category;
    }

    public String getCategory() {return category;};


    @Override
    public int hashCode() { return category.hashCode();}

    @Override
    public String toString() {return category;}

    @Override
    public boolean equals(Object aThing) {
        if (this == aThing)
            return true;
        if (!(aThing instanceof DBPediaCategory))
            return false;
        DBPediaCategory aCat = (DBPediaCategory) aThing;

        return category.equals(aCat.getCategory());
    }

}
