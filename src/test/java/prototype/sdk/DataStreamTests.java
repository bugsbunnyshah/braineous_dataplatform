package prototype.sdk;

import org.ehcache.sizeof.SizeOf;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class DataStreamTests {

    @Test
    public void testQueueBasedApproach() throws Exception{
        //add listener to queue
        // we create new `LinkedList` as a backing queue and decorate it
        ListenableQueue<String> q = new ListenableQueue<>(new LinkedList<>());

        // register a listener which polls a queue and prints an element
        q.registerListener(e -> {
            SizeOf sizeOf = SizeOf.newInstance();
            long dataStreamSize = sizeOf.deepSizeOf(q);

            //System.out.println("****DATA_STREAM_SIZE****");
            //System.out.println(q.size());
            //System.out.println(dataStreamSize+"");
            //System.out.println("************************");

            int windowSize = 400;
            if(dataStreamSize >= windowSize){
                for(int i=0; i<q.size();i++) {
                    String element = q.remove();
                    System.out.println(i);
                    System.out.println(element);
                    System.out.println("*****************");
                }
            }
        });

        // voila!
        for(int i=0; i<10; i++) {
            q.add("hello"+i);
        }
    }
}
