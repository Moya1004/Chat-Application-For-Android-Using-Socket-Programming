package com.github.jmsoft.socketclient;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Moya
 */public class DB {
    private Connection conn;
    private Statement statement;




    public DB() {
        //System.out.println(java.lang.System.getProperty("java.library.path"));

        final String LOG = "DEBUG";
        String ip = "192.168.43.180";
        String port = "49170";
        String classs = "net.sourceforge.jtds.jdbc.Driver";
        String db = "Chat";
        String un = "sa";
        String password = "Lovely6508";
        conn = null;
        String ConnURL = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip +":"+port+";"
                    + "databaseName=" + db + ";user=" + un + ";password="
                    + password + ";";
            conn = DriverManager.getConnection(ConnURL);
            this.statement = conn.createStatement();
        } catch (Exception e) {
            Log.d(LOG, e.getMessage());
        }
    }

    public boolean insertUser(String Username, String Pass, byte[] image)
    {
        String sql = String.format("Insert Into Users(Username,[Password]) Values('%s','%s')", Username , Pass);
        try {
            boolean result = statement.execute(sql);
            return result;
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public boolean loginUser(String Username, String Pass)
    {
        String sql = String.format("Select * from [Users] where [Username] = '%s' and [Password] = '%s'", Username , Pass);
        try {
            ResultSet result = statement.executeQuery(sql);
            if (result.next())
                return true;
            return false;
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean insertMessage(String message , int senderID , int recieverID, int messageType)
    {
        String sql = String.format("Insert Into Messages(MessageText,Sender,Reciever,MessageType) Values('{0}','{1}','{2}','{3}')",message , senderID, recieverID, messageType);
        try {
            ResultSet result = statement.executeQuery(sql);
            if (result.next())
                return true;
            return false;
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public ArrayList<Message> getMessages(int userID)
    {
        ArrayList<Message> messages = new ArrayList();
        String sql = String.format("Select * from Messages where Reciever = {0} or Receiver = 0 or Reciever in (select GROUPID from GroupUsers where UserID = {0} )", userID);
        try {
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                int sender = rs.getInt("Sender");
                int messageType = rs.getInt("MessageType");
                String message = rs.getString("MessageText");
                Date date = rs.getDate("Date");
                messages.add(new Message(sender,messageType,message,date));
            }
            return messages;
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String insertLog(String ip , String date)
    {
        String sql = String.format("Insert Into dbo.[Logs](ip_address,date) Values('%s','%s')",ip , date);
        try {
            boolean result = statement.execute(sql);
            return "TRUE";
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            return "False " + ex.getMessage();
        }
    }

    public String getUserNameByUserID(int id){
        String sql = String.format("Select * from Users where UserID = " + id);
        try {
            ResultSet rs = this.statement.executeQuery(sql);
            if (rs.next())
                return rs.getString("Username");
            return null;
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            return null;
        }
    }

    public int getUserIDByUsername(String username) throws SQLException {
        String sql = String.format("Select * from Users where [Username] = '%s'",username);
            ResultSet rs = this.statement.executeQuery(sql);
            if (rs.next())
                return rs.getInt("UserID");
            return 0;
    }

    public ResultSet getMessage(int id)
    {
        String sql = String.format("Select * from Messages ms inner join Users us on us.UserID = ms.Sender where Reciever =  "+id  + " or (MessageType = 0 and Reciever = 0)");
        try {
            ResultSet rs = this.statement.executeQuery(sql);
            return rs;
        } catch (SQLException ex) {
            //System.out.println(ex.getMessage());
            return null;
        }
    }

}
