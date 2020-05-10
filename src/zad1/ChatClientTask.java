/**
 *
 *  @author Smardz Weronika S18991
 *
 */

package zad1;


import java.util.List;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<String> {

    ChatClient client;
    List<String> msgs;
    int wait;

    ChatClientTask(ChatClient client,List<String> msgs, int wait){
        super(()->{
            if(wait != 0)
                Thread.sleep(wait);

            client.login();

            if(wait != 0)
                Thread.sleep(wait);

            for(String msg: msgs){
                client.send(msg);
                if(wait != 0)
                    Thread.sleep(wait);
            }

            if(wait != 0)
                Thread.sleep(wait);

            client.logout();

            if(wait != 0)
                Thread.sleep(wait);
            return client.getChatView();
        });

        this.client=client;
        this.msgs=msgs;
        this.wait=wait;
    }


    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        return new ChatClientTask(c,msgs,wait);
    }

    public ChatClient getClient() {
        return client;
    }
}
