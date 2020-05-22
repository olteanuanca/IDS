package connection;

import database.DB;
import database.IDB;
import jnetpcap.manager.FlowMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.frames.HomeFrame;

import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client implements Runnable{
    protected static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static float count=0;
    private static float countAttacks=0;
    private static float protectionStatus=100;
    IDB db = new DB();
    PrintWriter out;
    DataInputStream in;
    private List<String> rows;
    private String flowID;
    public Client(List<String> rows) throws IOException {
        this.rows = rows;

    }

    public static int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public float getProtectionStatus() {
        return protectionStatus;
    }

    @Override
    public void run()
    {

        try{
            Socket socket = new Socket("127.0.0.1",3500);
            in =new DataInputStream(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(),true);

            out.println(rows.get(0));


            byte[] message=new byte[15];
            in.read(message);

            int[] result =new int[message.length];
            for(int i=0;i<result.length;i++)
                result[i]=message[i];
                    //message[0];
            count++;

            if( result[0]!=0 )
            {
                countAttacks++;
                float tmp=countAttacks/count*100f;
                protectionStatus=100f-tmp;

                List<String> items = Arrays.asList(rows.get(0).split("\\s*,\\s*"));
                db.startDBConn();
                db.insertToAttacks(db.getUserID(), Integer.parseInt(items.get(5)),Float.parseFloat(items.get(7)),items.get(1),items.get(3),Float.parseFloat(items.get(2)),Float.parseFloat(items.get(4)),items.get(6),result[0]);
                logger.info("Attack detected and inserted to database.");
            }
        }
        catch (Exception e)
        {
            logger.debug(e.getMessage());
        }
    }


}
