package edu.columbia.cs.watson.newsframe.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

public class CategoryMap {
	private Connection connect = null;
	
	public static void main(String[] args) {
		CategoryMap cMap = new CategoryMap();
		cMap.buildCategoryToEntityTable();
	}

	private void buildCategoryToEntityTable() {
		
		try {
			HashSet<String> categoryCache = new HashSet<String>();
			this.connect = ConnectionFactory.getConnection();
			String sqlQuery = "SELECT categories FROM dbpedia where " +
								"categories <> ''";
			System.out.println(sqlQuery);
			PreparedStatement categoryStatement = this.connect.
													prepareStatement(sqlQuery);
			ResultSet categoryRows = categoryStatement.executeQuery();
			
			int count = 0;
			while (categoryRows.next()) {
				count = count + 1;
				if (count % 5 == 0 ) {
					System.out.println("");
					System.out.print(count);
				}
				else {
					System.out.print(".");
				}
				String[] categories = categoryRows.getString("categories")
																	.split(":");
				
				for (String eachCategory : categories) {
//					System.out.println(eachCategory);
					if (! categoryCache.contains(eachCategory.toLowerCase())) {
						categoryCache.add(eachCategory.toLowerCase());
						getOtherEntities(eachCategory);
					}
				}	
			}	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	private void closeConnection() {
		if (this.connect != null) {
			try {
				this.connect.close();
			} catch (SQLException ignore) {}
		}
	}
	
	private void getOtherEntities(String category) {
		
		try {
			ArrayList<String> entityList = new ArrayList<String>();
			String escapedCategory = escapeChars(category);
			String entityQuery = "SELECT entity_name FROM dbpedia WHERE " +
					"categories REGEXP '(^|:)" + escapedCategory + "(:|$)'";
			
			PreparedStatement entityStatement = this.connect.
											prepareStatement(entityQuery);
			
			ResultSet entityRows = entityStatement.executeQuery();

			while (entityRows.next()) {
				String entity = entityRows.getString("entity_name");
				entityList.add(entity);
			}
//			System.out.println(entityList);
//			System.out.println("");
			
			insertEntities(entityList, category);
		} catch (Exception e) {
			e.printStackTrace();
			
			//System.exit(0);
		}
	}

	private String escapeChars(String s) {
		String[] escapeItems = {"*", ".", "?", "+", "[", "]", "(", ")", 
											"{", "}", "^", "$", "|", "\\", "'"};

		
		for (int i = 0; i < escapeItems.length ; i++) {
			if (s.contains(escapeItems[i])) {
				s = s.replace(escapeItems[i], '\\' + escapeItems[i]);
			}
		}
		
		return s;
	}
	
	private void insertEntities(ArrayList<String> entities, String category) {
		try {

			for(String eachEntity : entities) {

				String insertQuery = "INSERT INTO category_map " + 
												"(category, entity) VALUES (?,?)";
				PreparedStatement insertStatement = this.connect.
												prepareStatement(insertQuery);
				insertStatement.setString(1, category);
				insertStatement.setString(2, eachEntity);
				insertStatement.executeUpdate();
			}
				
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: " + e);
			//System.exit(0);
		}
		
	}
	
	private String removeLastChar(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length() - 1);
    }
}
