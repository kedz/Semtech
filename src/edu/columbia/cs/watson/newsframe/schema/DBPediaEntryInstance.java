package edu.columbia.cs.watson.newsframe.schema;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 4/24/13
 * Time: 6:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBPediaEntryInstance {

    private String name;
    private List<DBPediaCategory> categories;

    public DBPediaEntryInstance(String name, List<DBPediaCategory> categories) {
        setName(name);
        setCategories(categories);
    }

    public int numCategories() {return (categories!=null) ? categories.size() : 0;}

    public DBPediaCategory getCategory(int index) {
        return categories.get(index);
    }

    public void setName(String name) {this.name=name;}
    public String getName() {return name;}

    public void setCategories(List<DBPediaCategory> categories) {this.categories=categories;}
    public List<DBPediaCategory> getCategories() {return categories;}

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("{ "+getName() +": ");

        for(DBPediaCategory cat : getCategories())
            buffer.append(cat.getCategory()+",");


        return buffer.substring(0,buffer.length()-1)+"}";


    }

}
