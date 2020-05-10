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
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {

    String host;
    int port;
    String id;
    SocketChannel channel;
    Charset charset = StandardCharsets.UTF_8;
    ByteBuffer serverResponseByteBuffer;
    CharBuffer serverResponseCharBuffer;
    StringBuffer serverResponseStringBuffer;
    List<String> chatView;
    boolean lastMessage=false;


    public ChatClient(String host, int port, String id) {
        this.host=host;
        this.port=port;
        this.id=id;
        serverResponseByteBuffer = ByteBuffer.allocate(2048);
        chatView= new ArrayList<>();
        serverResponseStringBuffer=new StringBuffer();
        serverResponseStringBuffer.append("=== "+id+" chat view\n");

        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            while (!channel.finishConnect()) {
                try {
                    Thread.sleep(10);
                } catch (Exception exc) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(()->{

            try {
                while (true) {

                    serverResponseByteBuffer.clear();
                    int readBytes = channel.read(serverResponseByteBuffer);

                    if (readBytes == 0) {
                        continue;
                    } else if (readBytes == -1) {
                        break;
                    } else {
                        serverResponseByteBuffer.flip();
                        serverResponseCharBuffer = charset.decode(serverResponseByteBuffer);

                        while (serverResponseCharBuffer.hasRemaining()) {
                            char c = serverResponseCharBuffer.get();
                            if (c == '$') {
                                lastMessage = true;
                                return;
                            }
                            serverResponseStringBuffer.append(c);
                        }

                        chatView.add(serverResponseCharBuffer.toString());
                    }
                }
            }catch (IOException e ){
                return;
            }

        }).start();

    }

    public void login(){

        send("login "+ id);
    }

    public void logout(){

        send("bye");
    }

    public void send(String req){

        CharBuffer clientMess = CharBuffer.wrap(req+ "\n");

        try {
            channel.write(charset.encode(clientMess));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getChatView(){

        while (true) {
            if(lastMessage) {
                return serverResponseStringBuffer.toString();
            }else{
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



}
