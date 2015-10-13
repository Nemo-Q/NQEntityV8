package com.nemoq.nqpossysv8.NetworkHandling;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Created by Martin on 2015-10-09.
 */
public class SOAPWriter {



    private StringWriter stringWriter;

    private XmlSerializer xmlSerializer;

    public SOAPWriter(){

        stringWriter = new StringWriter();


    }




    public void setEnvelope(String prefix){


        try {
            stringWriter.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            stringWriter.write("<" + prefix + ":Envelope " + setParameters(prefix) + ">");

            setHeader(prefix);
            setBody(prefix);


            stringWriter.write("</" + prefix + ":Envelope>");

        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    private void setHeader(String namespace) throws IOException {



        stringWriter.write(startTag(namespace, "Header"));


        stringWriter.write(endTag(namespace, "Header"));




    }



    public void setBody(String namespace){


        stringWriter.write(startTag(namespace,"Body"));

        stringWriter.write(startTagForParameters("t", "Ticket"));

        stringWriter.write(setParameters(namespace, "asdf=blabal", "asdf=123"));


        stringWriter.write(encapsulatedText("Ticket1", "asdf"));




        stringWriter.write(endTag("t","Ticket"));


    }



    private String tagWithParameters(String namespace, String prefix,String name,String... params){

            return startTagForParameters(prefix,name) + setParameters(namespace,params);

    }



    private String encapsulatedText(String tag,String text){


        return (startTag("",tag)) + text + endTag("",tag);



    }



    private void setContent(String prefix, String name) throws IOException {


        String tag = "<" + prefix + ":" + name + " "+ setParameters(prefix) + ">";




        stringWriter.write(startTagForParameters(prefix,name));
        stringWriter.write(setParameters(prefix));
        stringWriter.write(endTag(prefix,name));


    }



    private String setParameters(String prefix,String... parameters){

        String paramString = "";

        for (String param:parameters) {
            paramString += prefix + ":";
            paramString += param;
            paramString += " ";
        }




        return paramString + ">";

    }




    private String startTagForParameters(String prefix, String name){

        String tag = "<" + prefix + ":" + name + " ";


        return tag;
    }


    private String startTag(String prefix,String name){


        return "<" + prefix + ":" + name + ">";

    }

    private String endTag(String prefix,String name){


        String tag = "</" + prefix + ":" + name + ">";


        return tag;


    }

}
