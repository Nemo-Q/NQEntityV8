package com.nemoq.nqentityv8.NetworkHandling;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Martin Backudd on 2015-09-04. Parses Http and puts it in a table
 */
public class HttpParser {

    int bodyLengthRead;

    Map<String,String> httpTable;

    static private String LINE_DELIMITER = "\r\n";
    static private String BODY_DELIMITER = "\r\n\r\n";

    private StringBuilder bufferString;
    private StringBuilder bodyBuffer;

    public HttpParser(){

        bodyLengthRead = 0;

        httpTable = new HashMap<>();
        bufferString = new StringBuilder();
        bodyBuffer = new StringBuilder();

    }



    public boolean parse(byte[] bytes){



        for (byte b:bytes){
            bufferString.append((char)b);
        }

        if (bufferString.toString().contains(BODY_DELIMITER)){

            String header = bufferString.substring(0, bufferString.indexOf(BODY_DELIMITER));
            parseHeader(header);
            bodyBuffer.append(bufferString.toString().replace(header,""));
            bufferString.delete(0,bufferString.length());

        }
        if (bodyBuffer.toString().contains(BODY_DELIMITER)) {
            bodyBuffer.append(bufferString);
            bufferString.delete(0,bufferString.length());
        }

        if (Integer.parseInt(httpTable.get("Content-Length")) <= (bodyBuffer.length()+4)){


            parseBody(bodyBuffer.toString().replace(BODY_DELIMITER, ""));
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

        bodyBuffer = null;
        bufferString = null;

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
        String serverHeader = "Server: Nemo-Q V8 Printer";
        String contentTypeHeader = "Content-Type: text/html";

        String lengthHeader = "Content-Length: " + htmlBody.length();

        String response = typeHeader + LINE_DELIMITER + serverHeader + LINE_DELIMITER + contentTypeHeader + LINE_DELIMITER + lengthHeader + BODY_DELIMITER + htmlBody + BODY_DELIMITER;




        return response;


    }




}
