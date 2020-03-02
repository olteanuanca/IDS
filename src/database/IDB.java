package database;

import java.sql.SQLException;

public interface IDB {
    void startDBConn();

    void insertToCredentials(String uname, String pwd) throws SQLException;

    void insertToUsers(String lname, String fname, String email, String companyName, String companyRole, String deviceName, String profilePic);

    void insertToAppdata(String networkAdapter, String saveFlowPath, int delHostory, int startAuto, int rememberMe);

    int gedUserID();

    String retrieveFromCredentials(String name);
}
