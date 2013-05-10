/**
 * To find the overlapping categories between entities
 */
package edu.columbia.cs.watson.newsframe.cluster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * @author Tushar
 *
 */
public class CategoryMatcher {
	
	private Connection connect = null;
	private Statement selectStatement = null;
	private ResultSet selectedRows = null;
	private ArrayList<String> hardMatch = new ArrayList<String>();
	private ArrayList<String> softMatch = new ArrayList<String>();
	
	public static void main(String[] args) throws SQLException {
		CategoryMatcher cMatch = new CategoryMatcher();
		String str1 = "Barack_Obama";
		String str2 = "Bill_Clinton";
		cMatch.computeOverlap(str1, str2);
	}
	
	private void computeOverlap(String ent1, String ent2) {
		ArrayList<ArrayList<String>> categoryList = 
											new ArrayList<ArrayList<String>>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.connect = DriverManager
							.getConnection("jdbc:mysql://localhost:4000/" + 
							"newsframe?user=newsframe&password=newsframe");
			System.out.println("Connected!");
			
			ArrayList<String> entities = new ArrayList<String>();
			entities.add(ent1);
			entities.add(ent2);
			ArrayList<String> categories = new ArrayList<String>();
			
			for(String eachEntity : entities) {
				categories = queryDB(eachEntity);
				categoryList.add(categories);
			}
			
			doCategoryMatching(categoryList);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
		
		System.out.println(hardMatch);
		System.out.println(softMatch);
	}

	public ArrayList<String> queryDB(String queryEntity) {
		ArrayList<String> propertyList = new ArrayList<String>();
		try {
			this.selectStatement = this.connect.createStatement();
			String sqlQuery = "select categories from dbpedia where " +
								"entity_name='" + queryEntity + "'";
			
			this.selectedRows = this.selectStatement.executeQuery(sqlQuery);
			if (selectedRows.next()) {
				String categories = selectedRows.getString(1);
				String[] splits = categories.split(":");
				
				for(String eachCategory: splits) {
					propertyList.add(eachCategory);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return propertyList;
	}
	
	private void closeConnection() {
		if (this.connect != null) {
			try {
				this.connect.close();
			} catch (SQLException ignore) {}
		}
	}
	
	private void doCategoryMatching(ArrayList<ArrayList<String>> catList ) {
		//Go through both array lists and check matching strings.
		
		for (String a1 : catList.get(0)) {
		    for (String a2 : catList.get(1)) {
		    	if (a1.equals(a2)) {
		    		hardMatch.add(a1);
		    	}
		    	
		    	if (similarStrings(a1,a2)) {
		    		softMatch.add(a1 + ':' + a2);
		    	}
		    }
		}
	}

	private boolean similarStrings(String x, String y) {
		int z = new LevenshteinDistance().computeLevenshteinDistance(x, y);
		return z < 5 && z > 0 ? true : false ;
	}
	
	public class LevenshteinDistance {
        private int minimum(int a, int b, int c) {
            return Math.min(Math.min(a, b), c);
        }

        public int computeLevenshteinDistance(CharSequence str1,
                    CharSequence str2) {
            int[][] distance = new int[str1.length() + 1][str2.length() + 1];

            for (int i = 0; i <= str1.length(); i++)
                    distance[i][0] = i;
            for (int j = 1; j <= str2.length(); j++)
                    distance[0][j] = j;

            for (int i = 1; i <= str1.length(); i++)
                    for (int j = 1; j <= str2.length(); j++)
                            distance[i][j] = minimum(
                                            distance[i - 1][j] + 1,
                                            distance[i][j - 1] + 1,
                                            distance[i - 1][j - 1]
                                                            + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                                                                            : 1));

            return distance[str1.length()][str2.length()];
        }
	}
}
