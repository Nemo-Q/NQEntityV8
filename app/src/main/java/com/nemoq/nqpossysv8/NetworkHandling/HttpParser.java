package com.nemoq.nqpossysv8.NetworkHandling;

import android.text.Html;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Martin Backudd on 2015-09-04. Parses Http and puts it in a table
 */
public class HttpParser {

    boolean done;

    int bodyLengthRead;

    Map<String,String> httpTable;

    private String bodyCharacters;
    private String restCharacters;

    static private String LINE_DELIMITER = "\r\n";
    static private String BODY_DELIMITER = "\r\n\r\n";

    private int numberOfChunks;

    private String bufferString;
    private String bodyBuffer;



    public HttpParser(){

        bodyLengthRead = 0;

        httpTable = new HashMap<String,String>();
        restCharacters = new String();
        bodyCharacters = new String();

        numberOfChunks = 0;

    }



    public boolean parse(byte[] bytes){


        String characters = new String(bytes,0,bytes.length,Charset.forName("UTF-8"));

        bufferString += characters;
        String parseString = "";
        if (bufferString.contains(BODY_DELIMITER)){

            String header = bufferString.substring(0, bufferString.indexOf(BODY_DELIMITER));
            parseHeader(header);
            bodyBuffer = bufferString.replace(header,"");
            bufferString = "";

        }
        if (bodyBuffer.contains(BODY_DELIMITER)) {
            bodyBuffer += bufferString;
            bufferString = "";
        }

        if (Integer.parseInt(httpTable.get("Content-Length")) <= (bodyBuffer.length()+4)){


            parseBody(bodyBuffer.replace(BODY_DELIMITER,""));
            return true;

        }


        return false;

    }


    private void parseHeader(String headers){

        for (String component : headers.split("\r\n")) {

            String key = component.split(":")[0];
            String value = component.replace(key + ":", "").trim();

            httpTable.put(key, value);


        }




    }


    private void parseBody(String body){





        if (body.length() == 0)
            httpTable.put("Body","");

        else
            httpTable.put("Body", body);






    }

    public Map<String,String> getTable(){



        return httpTable;
    }


    public static String makeHttpResponse(String message){




        String htmlBody = "<html>\n" +
                "<head>\n" +
                "<title>Nemo-Q</title></head><body>\n" +
                message +
                "\n" +
                "</body>\n" +
                "</html>";

        String typeHeader = "HTTP/1.1 200 OK";
        String serverHeader = "Server: Nemo-Q Printer";
        String contentTypeHeader = "Content-Type: text/html";

        String lengthHeader = "Content-Length: " + htmlBody.length();

        String response = typeHeader + LINE_DELIMITER + serverHeader + LINE_DELIMITER + contentTypeHeader + LINE_DELIMITER + lengthHeader + BODY_DELIMITER + htmlBody + BODY_DELIMITER;




        return response;


    }




}
