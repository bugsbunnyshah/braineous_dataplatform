package org.apache.flink.table.examples.java.basics;

public class Message {
    private String msg;

    public Message(String str){
        this.msg=str;
    }

    public String getMsg() {
        return msg;
    }
}
