/**
 *
 *  @author Smardz Weronika S18991
 *
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class ChatServer extends Thread{

    ServerSocketChannel serverSocketChannel=null;
    Selector selector=null;
    InetSocketAddress inetSocketAddress;
    boolean isStopped=false;

    Charset charset = StandardCharsets.UTF_8;
    List<String> serwerLog;
    HashMap<String,String> clients;
    HashMap<String,SocketChannel> clientsSockets;

    StringBuffer messageFromClient;
    StringBuffer messageToClient;
    ByteBuffer byteBuffer;


    ChatServer(String host, int port){

        clients= new HashMap<>();
        clientsSockets = new HashMap<>();
        serwerLog= new ArrayList<>();

        messageFromClient= new StringBuffer();
        messageToClient= new StringBuffer();
        byteBuffer= ByteBuffer.allocate(1024);


        inetSocketAddress= new InetSocketAddress(host,port);
        try {
            serverSocketChannel= ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(inetSocketAddress);
            selector= Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void run() {
        while (!isStopped) {
            try {
                selector.select();
                Set keys = selector.selectedKeys();

                Iterator iter = keys.iterator();
                while(iter.hasNext()) {
                    SelectionKey key = (SelectionKey) iter.next();

                    iter.remove();

                    if (key.isAcceptable()) {
                        SocketChannel cc = serverSocketChannel.accept();

                        cc.configureBlocking(false);

                        cc.register(selector, SelectionKey.OP_READ);
                        continue;
                    }
                    if (key.isReadable()) {
                        SocketChannel cc = (SocketChannel) key.channel();
                        serviceRequest(cc);

                        continue;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startServer() {
        System.out.println("Server started\n");
        start();
    }

    public void stopServer() {
        isStopped=true;
        interrupt();
        System.out.println("Server stopped");
    }

    private void serviceRequest(SocketChannel clientSocketChanel) {

        if (!clientSocketChanel.isOpen())
            return;
        //Odczytanie zlecenia
        messageFromClient.setLength(0);
        byteBuffer.clear();
        try {
            readLoop:
            while (true) {

                int n = clientSocketChanel.read(byteBuffer);
                if (n > 0) {
                    byteBuffer.flip();
                    CharBuffer cbuf = charset.decode(byteBuffer);

                    while(cbuf.hasRemaining()) {
                        char c = cbuf.get();
                        if (c == '\r' || c == '\n')
                            break readLoop;
                        messageFromClient.append(c);
                    }
                }
            }

            String req = messageFromClient.toString();
            String [] request = req.split(" ");

            if (req.equals("bye")) {
                serwerLog.add(getDate()+ " "+ clients.get(clientSocketChanel.socket().getInetAddress().toString().substring(1)+ ":"+clientSocketChanel.socket().getPort())+ " logged out");

                for(SocketChannel socketChannel: clientsSockets.values()) {
                    writeResp(socketChannel, clients.get(clientSocketChanel.socket().getInetAddress().toString().substring(1)+ ":"+clientSocketChanel.socket().getPort())+ " logged out");
                }

                clientsSockets.remove(clientSocketChanel.socket().getInetAddress().toString().substring(1)+ ":"+clientSocketChanel.socket().getPort());

                clientSocketChanel.close();
                clientSocketChanel.socket().close();
            }
            else if (request[0].equals("login"))  {

                String clientId = clientSocketChanel.socket().getInetAddress().toString().substring(1)+ ":"+clientSocketChanel.socket().getPort();
                clients.put(clientId,request[1]);
                clientsSockets.put(clientId,clientSocketChanel);

                writeResp(clientSocketChanel,"logged in");
                serwerLog.add(getDate()+ " "+ request[1]+ " logged in");

                for(SocketChannel socketChannel: clientsSockets.values()) {
                    writeResp(socketChannel, clients.get(clientSocketChanel.socket().getInetAddress().toString().substring(1)+ ":"+clientSocketChanel.socket().getPort())+ " logged in");
                }

            }

            else {
                serwerLog.add(getDate()+ " " +clients.get(clientSocketChanel.socket().getInetAddress().toString().substring(1)+ ":"+clientSocketChanel.socket().getPort()) + ": "+ req);

                for(SocketChannel socketChannel: clientsSockets.values()) {
                    writeResp(socketChannel, clients.get(clientSocketChanel.socket().getInetAddress().toString().substring(1)+ ":"+clientSocketChanel.socket().getPort())+ ": "+ req);
                }
            }

        } catch (Exception exc) {
            exc.printStackTrace();
            try { clientSocketChanel.close();
                clientSocketChanel.socket().close();
            } catch (Exception e) {}
        }
    }


    private void writeResp(SocketChannel clientSocketChanel, String addMsg) throws IOException {
        messageToClient.setLength(0);

        if (addMsg != null) {
            messageToClient.append(addMsg+"\n");
        }
        ByteBuffer buf = charset.encode(CharBuffer.wrap(messageToClient));
        clientSocketChanel.write(buf);
    }

    public String getDate(){
        DateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        Calendar cal = Calendar.getInstance();
        String stringDate = sdf.format(cal.getTime());

        return stringDate;

    }

    public String getServerLog() {
        StringBuilder sb = new StringBuilder();
        for(String s: serwerLog){
            sb.append(s+"\n");
        }
        return sb.toString();
    }
}
