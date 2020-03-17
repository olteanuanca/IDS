package connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SendToServer implements Runnable {

    private static int count=0;
    private final int CHUNKSIZE = 1024;
    private final Socket sock=new Socket("localhost",3500);
    private List<String> rows;


    public SendToServer(List<String> rows) throws IOException {
        this.rows = rows;

    }
    @Override
    public void run() {
        byte[] in=null;
        byte[] rem=null;
        byte[] chunk=new byte[1024];
        byte[] tmp=null;
        int chunk_sz=0;

        for (int i = 0; i < rows.size(); i++)
        {
            in = rows.get(i).getBytes(StandardCharsets.UTF_8);
            System.out.println(rows.get(i));

            if(rem!=null) {
                if (rem.length == 1024) {
                    System.arraycopy(rem,0,chunk,0,rem.length);
                    rem = null;
                    System.arraycopy(in,0,rem,0,in.length);
                    in=null;
                } else if (rem.length < 1024) {
                    System.arraycopy(rem,0,chunk,0,rem.length);
                    chunk_sz=rem.length;
                    if(in.length<CHUNKSIZE-rem.length+1)
                    {
                        System.arraycopy(in,0,chunk,rem.length+1,in.length);
                        chunk_sz+=in.length;
                        rem=null;
                        i++;
                        if(rows.size()>=i)
                        {
                            break;
                        }
                    }
                    System.arraycopy(in,0,chunk,rem.length+1,CHUNKSIZE-rem.length+1);
                    chunk_sz+=CHUNKSIZE-rem.length+1;
                    rem = null;
                    System.arraycopy(in,CHUNKSIZE-rem.length+1,rem,0,in.length - (CHUNKSIZE - rem.length+1)+1);
                    in = null;
                } else if (rem.length > 1024){
                    System.arraycopy(rem, 0, chunk,0,CHUNKSIZE);
                    System.arraycopy(rem,CHUNKSIZE+1,tmp,0,rem.length-CHUNKSIZE+1);
                    rem=null;
                    System.arraycopy(tmp,0,rem,0,tmp.length);
                    System.arraycopy(in,0, rem,tmp.length+1,in.length);
                    in=null;
                    tmp=null;
                }
            }
            else {
                if(in.length==1024) {
                    System.arraycopy(in, 0, chunk, 0, CHUNKSIZE);
                    in=null;

                }
                else if(in.length<1024){
                    System.arraycopy(in, 0, chunk, 0, in.length);
                    chunk_sz=in.length;
                    in=null;
                    i++;
                    if(rows.size()<i)
                    {
                    in=rows.get(i).getBytes(StandardCharsets.UTF_8);
                    System.arraycopy(in, 0, chunk, CHUNKSIZE+1, CHUNKSIZE-in.length+1);
                    chunk_sz+=CHUNKSIZE-in.length+1;
                    rem=new byte[in.length-(CHUNKSIZE-in.length+1)+1];
                    System.arraycopy(in,CHUNKSIZE-in.length,rem,0,in.length-(CHUNKSIZE-in.length+1)+1);
                    in=null;
                    }
                }
                else if(in.length>1024){
                    System.arraycopy(in, 0, chunk, 0, CHUNKSIZE);
                    System.arraycopy(in, CHUNKSIZE+1, rem, 0, in.length-CHUNKSIZE+1);
                    rem=new byte[in.length-CHUNKSIZE+1];
                    in=null;
                }
            }
        }
        try {
            System.out.println(count);
             OutputStream output = sock.getOutputStream();
            output.write(chunk);
            count++;
            output.flush();
            DataInputStream din=new DataInputStream(sock.getInputStream());
            int result = din.readInt();
            System.out.println("Result:" + result);

           // din.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void close()
    {
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
