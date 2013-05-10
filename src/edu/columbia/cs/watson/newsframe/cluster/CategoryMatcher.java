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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	private ArrayList<String> computeOverlap(ArrayList<String> entities) {
		HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();
		ArrayList<String> overlap = new ArrayList<String>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.connect = DriverManager
							.getConnection("jdbc:mysql://localhost:4000/" + 
							"newsframe?user=newsframe&password=newsframe");
			//System.out.println("Connected!");
			
			ArrayList<String> categories = new ArrayList<String>();
			Integer categoryCount;
			for(String eachEntity : entities){
				categories = queryDB(eachEntity);
				//System.out.println(categories);
				for (String eachCategory : categories) {
					categoryCount = categoryMap.get(eachCategory);
					if (categoryCount == null) {
						categoryCount = 0;
					}
					categoryMap.put(eachCategory, ++categoryCount);
				}
			}
			
			Integer len = entities.size();
			for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
				String key = entry.getKey();
			    Integer value = entry.getValue();
			    if (value == len) {
			    	//System.out.println(key);
			    	//Add to an arraylist here
			    	overlap.add(key);
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println("Error:" + e.getMessage());
		} finally {
			closeConnection();
		}
		return overlap;
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

	private void checkSimilarity(HashMap<String, Integer> categoryMap) {
		Set<String> common = null;
		int maxLen;
		for (String key1 : categoryMap.keySet()) {
			String[] key1Split = key1.split("_");
			for (String key2 : categoryMap.keySet()) {
				String[] key2Split = key2.split("_");
				common = arrayIntersect(key1Split, key2Split);
				maxLen = Math.max(key1Split.length, key2Split.length);
				if (common.size() >= maxLen - 1 ) {
					System.out.println(key1 + "\t" + key2 + "\t" + common);
				}
			}
		}
		
	}

	private Set<String> arrayIntersect(String[] a, String[] b) {
		Set<String> ai = new HashSet<String>();  

		HashSet<String> ah = new HashSet<String>();  
		for (int i = 0; i < a.length; i++) {  
			ah.add(a[i]);  
		}  

		for (int i = 0; i < b.length; i++) {  
			if (ah.contains(b[i])) {  
				ai.add(b[i]);  
			}  
		}
		return ai;	
	}
}
