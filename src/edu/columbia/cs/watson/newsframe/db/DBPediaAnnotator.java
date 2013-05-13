package edu.columbia.cs.watson.newsframe.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
//import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import edu.columbia.cs.watson.newsframe.schema.DBPediaCategory;

public class DBPediaAnnotator {
    private ArrayList<DBPediaCategory> propertyList = new ArrayList<DBPediaCategory>();
    private static Connection connect = null;
    private PreparedStatement selectStatement = null;
    private PreparedStatement insertStatement = null;
    private ResultSet selectedRows = null;
    private String resource = null;

    private static final String USERNAME = "newsframe";
    private static final String PASSWORD = "newsframe";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        DBPediaAnnotator dbpedia = new DBPediaAnnotator();


        if (args.length > 0){
            dbpedia.annotate(args[0]);
            System.out.println(dbpedia.propertyList);
        }
    }

    public static void closeConnection() {
        if (connect!=null)
            try {
                connect.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
    }

    public List<DBPediaCategory> annotate(String str) throws Exception {
        try {
            this.resource = str;
            Class.forName("com.mysql.jdbc.Driver");

            if (connect == null || connect.isClosed()) {
                connect = ConnectionFactory.getConnection();
            }


            if(this.newQuery(resource)) {
                System.out.println("Retrieving resource: <"+resource.replaceAll("<","\\<").replaceAll(">","\\>")+"> from the DBPedia endpoint.");

                String service = "http://dbpedia.org/sparql";

                String query = "PREFIX dcterms: <http://purl.org/dc/terms/> " +
                        "SELECT ?category WHERE { " +
                        "<http://dbpedia.org/resource/" +
                        this.resource.replaceAll("<","\\<").replaceAll(">","\\>") +
                        "> dcterms:subject ?category .}";
                 /*
                String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX dcterms: <http://purl.org/dc/terms/> " +
                        "SELECT ?category WHERE { " +
                        "<http://dbpedia.org/resource/" +
                        this.resource +
                        "> rdf:type ?class . " +
                        "<http://dbpedia.org/resource/" +
                        this.resource +
                        "> dcterms:subject ?category . " +
                        "FILTER( REGEX(" +
                        "?class, \"http://dbpedia.org/ontology/Person\") " +
                        "|| REGEX(?class, \"http://dbpedia.org/ontology/Place\") )" +
                        "}";

                  */


                QueryExecution qe = QueryExecutionFactory
                        .sparqlService(service, query);

                com.hp.hpl.jena.query.ResultSet results = qe.execSelect();

                //if (results.hasNext()) {
                this.parseResults(results);
                this.createCategoryString();
                //}
                qe.close();

            }
            else {
                System.out.println("Retrieving resource: <"+resource+"> from the database");
                this.getFromDatabase();
            }


        } catch(Exception e) {
            e.printStackTrace();

        }

        return propertyList;
    }

    private void getFromDatabase() {
        try {
            this.selectStatement = this.connect.prepareStatement("select categories from dbpedia where entity_name = ?");
            selectStatement.setString(1,resource);

            //this.selectStatement = this.connect.createStatement();

            //String sqlQuery = "select categories from dbpedia where entity_name='" +
            //resource + "'";

            this.selectedRows = this.selectStatement.executeQuery();
            if (selectedRows.next()) {
                String categories = selectedRows.getString(1);
                String[] splits = categories.split(":");

                for(String eachCategory: splits){
                    this.propertyList.add( DBPediaCategory.getCategory(eachCategory));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean newQuery(String resource) {
        try {
            this.selectStatement = this.connect.prepareStatement("select entity_name from dbpedia where entity_name = ?");
            selectStatement.setString(1,resource);



            //String sqlQuery = "select entity_name from dbpedia where entity_name = '" +
            //					resource + "'";

            this.selectedRows = this.selectStatement.executeQuery();
            if (selectedRows.next()) {
                //String a = selectedRows.getString(1);
                return false;
            }
            else {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void parseResults(com.hp.hpl.jena.query.ResultSet dbpediaResults) {

        try {
            while(dbpediaResults.hasNext()) {
                QuerySolution sol = (QuerySolution) dbpediaResults.next();
                String category = sol.get("?category").toString();
                //byte[] b = category.getBytes("UTF-8");
                //String newCategory = new String(b, "UTF-8");
                int x = category.indexOf("Category:");
                this.propertyList.add(DBPediaCategory.getCategory(category.substring(x + 9)));
            }
            this.propertyList.remove(this.resource);

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void createCategoryString() {
        StringBuffer categoryString = new StringBuffer();
        for(DBPediaCategory eachProperty : propertyList) {
            categoryString.append(eachProperty.toString() + ":");
        }
        String str = removeLastChar(categoryString.toString());
        try {
            this.insertStatement = this.connect
                    .prepareStatement("insert into dbpedia (entity_name, categories) values (?, ?)");
            this.insertStatement.setString(1, this.resource);
            this.insertStatement.setString(2, str);
            this.insertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String removeLastChar(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length() - 1);
    }
}
