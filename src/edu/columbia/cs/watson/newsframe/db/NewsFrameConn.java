package edu.columbia.cs.watson.newsframe.db;



import java.sql.*;

public class NewsFrameConn {
	
    public Connection conn;
    public Statement stmt;
    private String JDBC_DRIVER;
    private String username;
	private String password;
    private String dbname;
    private PreparedStatement preparedStatement;
	
	/*
	 * 	Connector to Newsframe database
	 * 	
	 * 	Usage:
     *  	NewsFrameConn conn = new NewsFrameConn();
     *		conn.connect();
	 *		conn.updateRaw("a", "b", "c", 1.0, 0.5);
	 *		conn.updateRaw("a", "b", "c", 1.0, 0.2);
	 *		conn.updateRaw("a", "b", "d", 1.0, 0.6);
	 *		conn.deleteDep("a", "b", "c");
	 */

    public NewsFrameConn () {
	    JDBC_DRIVER = "com.mysql.jdbc.Driver";
	    username = "newsframe";
		password = "newsframe";
	    dbname = "jdbc:mysql://localhost/newsframe";
		conn = null;
		stmt = null;
    }

	/*
	 * 	Connect to MySQL Newsframe Database
	 */

    public void connect() {	
	    try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(dbname, username, password);
            System.out.println("Database connection established");
			conn.createStatement().execute("use newsframe;");
			System.out.println("Using newsframe database");
			stmt = conn.createStatement();
	    } catch (Exception e) { e.printStackTrace(); }
    }
	
	/*
	 * 	Insert or update entry in dep_frame table:
	 * 	
	 *	Arguments:	entity1
	 *				entity2
	 *				path
	 *				score
	 *				weighted score
	 */

	public void updateDep (String ent1, String ent2, String path, long score, double wscore) {
		update("dep_frame", "path", ent1, ent2, path, score, wscore);
	}
	
	/*
	 * 	Insert or update entry in raw_frame table:
	 * 	
	 *	Arguments:	entity1
	 *				entity2
	 *				word
	 *				score
	 *				weighted score
	 */
	
	public void updateRaw (String ent1, String ent2, String word, long score, double wscore) {
		update("raw_frame", "word", ent1, ent2, word, score, wscore);
	}
	
	private void update (String table, String col, String ent1, String ent2, String word, long score, double wscore) {
		try {

            preparedStatement = conn.prepareStatement("select ID, score from "+table+" where entity1 = ? AND entity2 = ? AND "+col+" = ?");
            //preparedStatement.setString(1,table);
            preparedStatement.setString(1,ent1);
            preparedStatement.setString(2,ent2);
            //preparedStatement.setString(3,col);
            preparedStatement.setString(3,word);
            //System.out.println(preparedStatement);

            ResultSet resultSet = preparedStatement.executeQuery();


            if (resultSet.next()) {

                int id = resultSet.getInt(1);
                long oldScore = resultSet.getLong(2);

                preparedStatement = conn.prepareStatement("UPDATE "+table+" SET score = ? WHERE ID = ?");
                //preparedStatement.setString(1,table);
                preparedStatement.setLong(1,score+oldScore);
                preparedStatement.setInt(2,id);
                preparedStatement.executeUpdate();

            } else {

                preparedStatement = conn.prepareStatement("INSERT INTO "+table+" (entity1, entity2, "+col+", score) VALUES (?, ?, ?, ?)");
                //preparedStatement.setString(1, table);
                //preparedStatement.setString(2, col);
                preparedStatement.setString(1,ent1);
                preparedStatement.setString(2,ent2);
                preparedStatement.setString(3,word);
                preparedStatement.setLong(4,score);

                preparedStatement.executeUpdate();



            /*
            String cols = "(entity1, entity2, "+col+", score)";// , weighted_score)";
			String vals = "( ?, ?, ?, ?)";
            //String vals = "('"+ent1+"','"+ent2+"','"+word+"','"+score+"','"+wscore+"')";
			String dups = "score=score+"+score;//+", weighted_score=weighted_score+"+wscore;
			
			String sql = "INSERT INTO "+table+" "+cols+
						" VALUES "+vals+
						" ON DUPLICATE entity1 UPDATE "+dups;

            preparedStatement = conn.prepareStatement(sql);
            */

            }
		} catch (SQLException e) {
            //System.out.println(preparedStatement);
            e.printStackTrace(); }
	}
	
	public void deleteDep (String ent1, String ent2, String path) {
		delete("dep_frame", "path", ent1, ent2, path);
	}
	
	public void deleteRaw (String ent1, String ent2, String word) {
		delete("raw_frame", "word", ent1, ent2, word);
	}
	
	private void delete (String table, String col, String ent1, String ent2, String word) {
		try {
			String cols = "(entity1, entity2, "+col+")";
	 		String vals = "(('"+ent1+"', '"+ent2+"', '"+word+"'))";
		
			String sql = "DELETE FROM "+table+
						" WHERE "+cols+
						" IN "+vals;
		
			stmt.executeUpdate(sql);
		
		} catch (SQLException e) { e.printStackTrace(); }
	}


    public void closeConnection() {
        try {
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}