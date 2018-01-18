package edu.usc.cs_server.stockinfo.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Anuj Doiphode on 19-11-2017.
 */

public class UtilMethods {
    public static String readInputStream(InputStream inputStream){
        String text = "";
        try {
            int size = inputStream.available();
            byte buffer[] = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            text = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }
}
