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
    List<String> chatView;


    public ChatClient(String host, int port, String id) {
        this.host=host;
        this.port=port;
        this.id=id;
        serverResponseByteBuffer = ByteBuffer.allocate(2048);
        chatView= new ArrayList<>();

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
                        chatView.add(serverResponseCharBuffer.toString());
                    }
                }
            }catch (IOException e ){
                return;
            }

        }).start();

    }

    public void login(){

        System.out.println("Client:  "+id+ " login");
        String loginMess= "login "+ id+ "\n";
        try {
            channel.write(charset.encode(loginMess));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout(){
        System.out.println("Client:  "+id+ " bye" );
        String logoutMess= "bye"+ "\n";
        try {
            channel.write(charset.encode(logoutMess));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void send(String req){

        System.out.println("Client:  "+id+ " " +req);
        CharBuffer clientMess = CharBuffer.wrap(req+ "\n");

        try {
            channel.write(charset.encode(clientMess));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getChatView(){

        String chatview="=== "+id+" chat view\n";

        for(String s: chatView){
            chatview+=s;
        }
        return chatview;
    }



}
