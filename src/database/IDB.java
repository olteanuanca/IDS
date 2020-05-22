package database;

import utils.Attack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public interface IDB {
    void startDBConn();

    void insertToCredentials(String uname, String pwd) throws SQLException;

    void insertToUsers(String lname, String fname, String email, String companyName, String companyRole, String deviceName, String profilePic);

    void insertToAppdata(String networkAdapter, String saveFlowPath, int delHostory, int startAuto, int rememberMe);

    void insertToAttacks(int userID, int protocol,Float flow_duration, String src_ip, String dst_ip, Float src_port,Float dst_port, String timestamp,int result) throws SQLException;

    int getUserID();

    String retrieveFromCredentials(String name);

    ArrayList<Attack> retrieveFromAttacks(int userID);

}
