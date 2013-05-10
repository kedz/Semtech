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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Tushar
 *
 */
public class CategoryMatcher {
	
	/**
	 * @param args
	 */
	
	private Connection connect = null;
	private Statement selectStatement = null;
	private ResultSet selectedRows = null;
	private ArrayList<String> hardMatch = new ArrayList<String>();
	private ArrayList<ArrayList<String>> softMatch = 
											new ArrayList<ArrayList<String>>();
	
	public static void main(String[] args) throws SQLException {
		CategoryMatcher ec = new CategoryMatcher();
		ArrayList<String> entities = new ArrayList<String>();
		//entities.add("Barack_Obama");
		//entities.add("Tony_Blair");
		//entities.add("George_W._Bush");
		//entities.add("Bill_Clinton");
		Collections.addAll(entities, "Barack_Obama",  
								"Bill_Clinton");
		ec.computeOverlap(entities);
	}
	
	/**
	 * @param Takes in an ArrayList of any #of entity strings and returns common
	 * categories between them 
	 */
	
	private void computeOverlap(ArrayList<String> entities) {
		HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();
		ArrayList<ArrayList<String>> categoryList = 
											new ArrayList<ArrayList<String>>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.connect = DriverManager
							.getConnection("jdbc:mysql://localhost:4000/" + 
							"newsframe?user=newsframe&password=newsframe");
			System.out.println("Connected!");
			
			ArrayList<String> categories = new ArrayList<String>();
			Integer categoryCount;
			for(String eachEntity : entities){
				categories = queryDB(eachEntity);
				System.out.println(categories);
				categoryList.add(categories);
				System.out.println(categoryList);
				for (String eachCategory : categories) {
					categoryCount = categoryMap.get(eachCategory);
					if (categoryCount == null) {
						categoryCount = 0;
					}
					categoryMap.put(eachCategory, ++categoryCount);
				}
			}
			System.out.println(categoryMap);
			
			Integer len = entities.size();
			for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
				String key = entry.getKey();
			    Integer value = entry.getValue();
			    if (value == len) {
			    	//System.out.println(key);
			    	//Add to an arraylist here
			    	hardMatch.add(key);
			    }
			}
			
			softMatch = checkSimilarity(categoryList);
			
		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println("Error:" + e.getMessage());
		} finally {
			closeConnection();
		}
		System.out.println(softMatch);
		System.out.println(hardMatch);
	}

	private ArrayList<String> queryDB(String queryEntity) {
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

	private ArrayList<ArrayList<String>> checkSimilarity(
										ArrayList<ArrayList<String>> catList ) {
		//Go through both array lists and check matching strings.
		System.out.println(catList.get(0));
		System.out.println(catList.get(1));
		
		ArrayList<ArrayList<String>> matchingStrings = 
											new ArrayList<ArrayList<String>>();
		ArrayList<String> tempList = new ArrayList<String>();
		//int counter = 0;
		for (String a1 : catList.get(0)) {
		    for (String a2 : catList.get(1)) {
		    	//Check a1 and a2 for similarity and add [a1, a2] to 
		    	//a master list that will be returned
		    	if (similarStrings(a1,a2)) {
		    		System.out.println("Match:" + a1 + ' ' + a2);
		    		System.out.println(tempList);
		    		tempList.add(a1);
		    		tempList.add(a2);
		    		matchingStrings.add(tempList);
		    		System.out.println(matchingStrings);
		    		tempList.removeAll(tempList);
		    		//matchingStrings.add(++counter, );
		    	}
		    }
		}
		
		System.out.println(matchingStrings);
		return matchingStrings;
	}

	private boolean similarStrings(String x, String y) {
		int z = new LevenshteinDistance().computeLevenshteinDistance(x, y);
//		if (z < 5) {
//			return true;
//		} else {
//			return false;
//		}
		
		return z < 5? true : false ;
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
//	private Set<String> arrayIntersect(String[] a, String[] b) {
//		Set<String> ai = new HashSet<String>();  
//
//		HashSet<String> ah = new HashSet<String>();  
//		for (int i = 0; i < a.length; i++) {  
//			ah.add(a[i]);  
//		}  
//
//		for (int i = 0; i < b.length; i++) {  
//			if (ah.contains(b[i])) {  
//				ai.add(b[i]);  
//			}  
//		}
//		return ai;	
//	}
}
