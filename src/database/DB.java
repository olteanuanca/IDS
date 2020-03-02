package database;

//import jnetpcap.worker.LoadNetworkAdapterWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DB implements IDB {

    public static final Logger logger = LoggerFactory.getLogger(DB.class);
    int index;
    // static final String DB_URL = "jdbc:mysql://127.0.0.1/secitdb";
    // static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private String host = "jdbc:mysql://127.0.0.1/secitsolutions";
    private String username = "root";
    private String password = "SecITSolutionsDB2020";
    private Connection dbConn;
    private Driver driver;

    public DB() {
        index = 0;
        dbConn = null;
        try {
            driver = new com.mysql.cj.jdbc.Driver();
            DriverManager.registerDriver(driver);
        } catch (SQLException ex) {
            System.out.println("Error: unable to load driver class!");
            System.exit(1);
        }
    }

    @Override
    public void startDBConn() {
        try {
            dbConn = DriverManager.getConnection(this.host, this.username, this.password);
        } catch (SQLException err) {
            System.out.println(err.getMessage());
            return;
        }

    }


    @Override
    public void insertToCredentials(String uname, String pwd) throws SQLException {
        Statement stmt = null;
        try {

            System.out.println("Inserting records into the table...");
            stmt = dbConn.createStatement();

            String sql = "INSERT INTO credentials(username,password) VALUES (\"" + uname + "\",\"" + pwd + "\")";
            stmt.executeUpdate(sql);
        } catch (Exception se) {
            se.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    dbConn.close();
            } catch (SQLException se) {
            }
            try {
                if (dbConn != null)
                    dbConn.close();
                index++;
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }


    @Override
    public void insertToUsers(String lname, String fname, String email, String companyName, String companyRole, String deviceName, String profilePic) {
        Statement stmt = null;
        index= this.gedUserID();
        try {

            System.out.println("Inserting records into users...");
            stmt = dbConn.createStatement();

            String sql = "INSERT INTO users(lname,fname,email,companyName,companyRole,deviceName,profilePic,credentialID,appID) VALUES (\"" + lname + "\",\"" + fname + "\",\"" + email + "\",\"" + companyName + "\",\"" + companyRole + "\",\"" + deviceName + "\",\"" + profilePic + "\"," + index +","+ index +");";
            stmt.executeUpdate(sql);
        } catch (Exception se) {
            se.printStackTrace();
            //TODO logger
        } finally {
            try {
                if (dbConn != null)
                    dbConn.close();
                    index++;
            } catch (SQLException se) {
                se.printStackTrace();
                //TODO logger
            }
        }
    }

    @Override
    public void insertToAppdata(String networkAdapter,String saveFlowPath, int delHostory, int startAuto, int rememberMe) {
        Statement stmt = null;
        try {

            System.out.println("Inserting records into appdata...");
            stmt = dbConn.createStatement();

            String sql = "INSERT INTO appdata(networkAdapter,saveFlow,delHistory,startAuto,rememberMe) VALUES (\"" + networkAdapter + "\",\"" + saveFlowPath + "\"," + delHostory + "," + startAuto + "," + rememberMe + ")";
            stmt.executeUpdate(sql);
        } catch (Exception se) {
            //TODO logger
            se.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    dbConn.close();
            } catch (SQLException se) {
                //TODO logger
            }
        }
    }

    @Override
    public int gedUserID() {
        String tmp=null;
        Statement stmt = null;
        try {

            System.out.println("Retrieving records from credentials...");
            stmt = dbConn.createStatement();

            String sql = "SELECT credentialID FROM credentials WHERE credentialID=(SELECT MAX(credentialID) FROM credentials);";
            stmt.executeQuery(sql);

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
              tmp = rs.getString("credentialID");
            }
            rs.close();
        } catch (Exception se) {
            //TODO logger
            se.printStackTrace();
        } finally {
            if (stmt != null)
                System.out.println("Retrieving records from credentials...");
            //TODO logger
        }
        return Integer.parseInt(tmp);
    }


    @Override
    public String retrieveFromCredentials(String name) {
        String pwd = null;
        Statement stmt = null;
        try {

            System.out.println("Retrieving records from credentials...");
            stmt = dbConn.createStatement();

            String sql = "SELECT password FROM credentials WHERE username=\"" + name + "\"";
            stmt.executeQuery(sql);

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                pwd = rs.getString("password");
            }
            rs.close();
        } catch (Exception se) {
            //TODO logger
            se.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    dbConn.close();
            } catch (SQLException se) {
                //TODO logger
            }
        }

        return pwd;
    }
}
