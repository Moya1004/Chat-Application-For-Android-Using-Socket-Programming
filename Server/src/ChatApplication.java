/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author Moya
 */
public class ChatApplication {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic 
        try
        {
            //System.out.println(java.lang.System.getProperty("java.library.path"));
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
            "jdbc:jtds:sqlserver://localhost:57004;databaseName=ASOSDB;integratedSecurity=true");
            System.out.println("connected");
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select * from [User]");
            while (rs.next()) {
            String lastName = rs.getString("UserName");
            System.out.println(lastName + "\n");
}
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
}
