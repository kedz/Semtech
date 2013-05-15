package edu.columbia.cs.watson.newsframe.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class HighCategoryMap {
	private Connection connect = null;
	
	public static void main(String[] args) {
		HighCategoryMap cMap = new HighCategoryMap();
		cMap.buildCategoryToEntityTable();
	}

	private void buildCategoryToEntityTable() {
		
		try {
			ArrayList<String> highEntities = new ArrayList<String>();
			this.connect = ConnectionFactory.getConnection();
			String sqlQuery = "SELECT distinct(n) FROM " +
								"(SELECT entity1 AS n FROM high_frames UNION " +
								"SELECT entity2 AS n FROM high_frames) AS t";
			System.out.println(sqlQuery);
			PreparedStatement highEntityStatement = this.connect.
													prepareStatement(sqlQuery);
			ResultSet highEntityRows = highEntityStatement.executeQuery();
			
			while (highEntityRows.next()) {
				highEntities.add(highEntityRows.getString("n"));
			}
			
			System.out.println(highEntities.size());
			
			//for each entity in highEntities, get categories.
			//		for each category in categories, do regex query.
			//			insert each new entity into new table
			
			
			int count = 0;
			for (String eachEntity : highEntities) {
				count = count + 1;
				if (count % 5 == 0 ) {
					System.out.println("");
					System.out.print(count);
				}
				else {
					System.out.print(".");
				}

				String categoryQuery = "SELECT categories FROM dbpedia " +
										"WHERE entity_name = ?";
				
				PreparedStatement categoryStatement = this.connect.
												prepareStatement(categoryQuery);
				categoryStatement.setString(1, eachEntity);
				ResultSet categoryRows = categoryStatement.executeQuery();
				while (categoryRows.next()) {
					String[] categories = categoryRows.getString("categories")
																	.split(":");					
					for (String eachCategory : categories) {	
						String insertQuery = "INSERT INTO high_category_map " + 
											"(category, entity) VALUES (?,?)";
						PreparedStatement insertStatement = this.connect.
												prepareStatement(insertQuery);
						insertStatement.setString(1, eachCategory);
						insertStatement.setString(2, eachEntity);
						insertStatement.executeUpdate();
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
	
}
