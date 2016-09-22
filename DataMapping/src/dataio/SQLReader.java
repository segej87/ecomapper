package dataio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SQLReader {
	
	public String jsonString;
	private String uname;
	private String pword;
	
	public SQLReader(String un, String pw){
		uname = un;
		pword = pw;
		try {
			setJsonString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setJsonString() throws Exception{
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		
		Connection m_Connection = DriverManager.getConnection(
				"jdbc:sqlserver://map-it.database.windows.net:1433;database=geojson;user=ecoCollector@map-it;password={173394aBzZqR!};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
		
		Statement m_Statement = m_Connection.createStatement();
		
		String query = "SELECT UID FROM login WHERE username = '" + uname +"' AND password = '" + pword + "'";
		
		ResultSet m_ResultSet = m_Statement.executeQuery(query);
		
		String uid = "";
		while (m_ResultSet.next()){
			uid = m_ResultSet.getString(1);	
		}
		
		query = "SELECT geojsonText FROM personal WHERE UID = '" + uid + "'";
		
		m_ResultSet = m_Statement.executeQuery(query);
		while (m_ResultSet.next()){
			jsonString = m_ResultSet.getString(1);
		}
		
		m_Connection.close();
	}
}
