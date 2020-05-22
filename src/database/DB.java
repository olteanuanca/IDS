package database;

//import jnetpcap.worker.LoadNetworkAdapterWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Attack;

import java.sql.*;
import java.util.ArrayList;

public class DB implements IDB {

    public static final Logger logger = LoggerFactory.getLogger(DB.class);
    int index;
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
        index= this.getUserID();
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
            logger.debug(se.getMessage());

        } finally {
            try {
                if (stmt != null)
                    dbConn.close();
            } catch (SQLException se) {
                logger.debug(se.getMessage());
            }
        }
    }

    @Override
    public int getUserID() {
        String tmp=null;
        Statement stmt = null;
        try {

            stmt = dbConn.createStatement();

            String sql = "SELECT credentialID FROM credentials WHERE credentialID=(SELECT MAX(credentialID) FROM credentials);";
            stmt.executeQuery(sql);

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
              tmp = rs.getString("credentialID");
            }
            rs.close();
        } catch (Exception se) {
            logger.debug(se.getMessage());

        }
        return Integer.parseInt(tmp);
    }


    @Override
    public String retrieveFromCredentials(String name) {
        String pwd = null;
        Statement stmt = null;
        try {

            stmt = dbConn.createStatement();

            String sql = "SELECT password FROM credentials WHERE username=\"" + name + "\"";
            stmt.executeQuery(sql);

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                pwd = rs.getString("password");
            }
            rs.close();
        } catch (Exception se) {
            logger.debug(se.getMessage());

        } finally {
            try {
                if (stmt != null)
                    dbConn.close();
            } catch (SQLException se) {
                logger.debug(se.getMessage());
            }
        }

        return pwd;
    }

    @Override
    public void insertToAttacks(int userID, int protocol,Float flow_duration, String src_ip, String dst_ip, Float src_port,Float dst_port, String tmstamp,int result) throws SQLException {

        Statement stmt = null;
        try {


            stmt = dbConn.createStatement();

            String insert_str = Integer.toString(userID) + "," + Integer.toString(protocol) + "," + Float.toString(flow_duration) + ",\"" + src_ip + "\",\"" + dst_ip + "\"," + Float.toString(src_port)+ "," + Float.toString(dst_port)+ ",\"" + tmstamp + "\"," + Integer.toString(result) ;
            String sql = "INSERT INTO attacks(userID,protocol,flow_duration,src_ip,dst_ip,src_port,dst_port,timestamp,result) VALUES (" + insert_str + ")";
            stmt.executeUpdate(sql);
        } catch (Exception se) {
            se.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    dbConn.close();
            } catch (SQLException se) {
                logger.debug(se.getMessage());
            }
            try {
                if (dbConn != null)
                    dbConn.close();

            } catch (SQLException se) {
                logger.debug(se.getMessage());
            }
        }
    }


    @Override
    public ArrayList<Attack> retrieveFromAttacks(int userID)
    {
        ArrayList<Attack> attacks_list=new ArrayList<>();
        ResultSet rs =null;
        Statement stmt = null;
        try {
            stmt = dbConn.createStatement();

            String sql = "SELECT * FROM attacks WHERE userID=" + String.valueOf(userID) + "";
            stmt.executeQuery(sql);

            rs = stmt.executeQuery(sql);
            Attack a;
            while (rs.next()) {
                a = new Attack(rs.getString("src_ip"),rs.getString("dst_ip"),rs.getFloat("src_port"),rs.getFloat("dst_port"),rs.getInt("protocol"),rs.getFloat("flow_duration"),rs.getString("timestamp"),rs.getInt("result"));
                attacks_list.add(a);
            }

            rs.close();
        } catch (Exception se) {
            logger.debug(se.getMessage());

        } finally {
            try {
                if (stmt != null)
                    dbConn.close();
            } catch (SQLException se) {
                logger.debug(se.getMessage());
            }
        }

       return attacks_list;
    }
}
