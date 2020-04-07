package com.zack.xjht.Utils;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtil {

    private CloseUtil(){}


    public static void closeQuietly(Closeable closeable){
        if(closeable !=null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
