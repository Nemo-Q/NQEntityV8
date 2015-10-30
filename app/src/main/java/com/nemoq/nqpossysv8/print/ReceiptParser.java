package com.nemoq.nqpossysv8.print;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.JsonReader;
import android.util.Log;
import android.util.Xml;
import com.nemoq.nqpossysv8.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Martin Backudd on 2015-08-21. Takes a Xml string and parses it to Epson-Printer commands.
 */
public class ReceiptParser {

    //Remember if we already have the text properties set to not rewrite them to the printer
    private int lastAlignment = -1;
    private int lastBold = -1;
    private int lastLineSpace = -1;
    private int lastCharacterSpace = -1;
    private int lastUnderLine = -1;
    private int[] lastFontSize = {-1,-1};

    private boolean ticketFlipped = false;
    private int ticketMargin = -1;
    private int ticketWidth = -1;
    private int codePage = -1;

    int default_linespace = 30;

    private Charset charset;
    private boolean charReversed;

    private static ReceiptParser receiptParser;


    /*int CP_PC437_STANDARD_EUROPE = 0;
    int CP_KATAKANA = 1;
    int CP_PC850_MULTILINGUAL = 2;
    int CP_PC860_PORTUGUESE = 3;
    int CP_PC864_CANADIAN_FRENCH = 4;
    int CP_PC865_NORDIC = 5;
    int CP_WEST_EUROPE = 6;
    int CP_GREEK = 7;
    int CP_HEBREW = 8;
    int CP_PC755_EAST_EUROPE = 9;
    int CP_IRAN = 10;
    int CP_PC857_TURKISH = 13;
    int CP_PC737_GREEK = 14;
    int CP_PC928_GREEK = 15;
    int CP_WPC1252_LATIN = 16;
    int CP_PC866_CYRILLIC_2 = 17;
    int CP_PC852_LATIN2 = 18;
    int CP_PC858_WEST_EUROPE = 19;
    int CP_PC874_THAI = 21;
    int CP_WPC775_BALTIC = 33;
    int CP_PC855_CYRILLIC = 34;
    int CP_PC862_HEBREW = 36;
    int CP_PC864_ARABIC = 37;
    int CP_WPC1251_CYRILLIC = 46;
    int CP_WPC1253_GREEK = 47;
    int CP_WPC1254_TURKISH = 48;
    int CP_WPC1255_HEBREW = 49;
    int CP_WPC1256_ARABIC = 50;
    int CP_WPC1257_BALTIC = 51;*/


    private Context context;

    public List<Byte> byteList;

     private ReceiptParser(Context ctx){

                context = ctx;

     }

    public static synchronized ReceiptParser getInstance(Context ctx){


            if (receiptParser == null){

                receiptParser = new ReceiptParser(ctx);


            }

        return receiptParser;

    }


    public byte[] testXml(InputStream inputStream) throws IOException, XmlPullParserException {

        byte[] bytes = xmlToPrinterBytes(inputStream);

        return bytes;

    }
    public byte[] testJson(InputStream inputStream) {


        try {
            return jsonToPrinterBytes(inputStream);
        } catch (IOException e) {
           e.printStackTrace();
            return null;
        }
        finally {
            lastAlignment = -1;
            lastBold = -1;
            lastLineSpace = -1;
            lastCharacterSpace = -1;
            lastUnderLine = -1;
            lastFontSize = new int[]{-1,-1};
        }
    }

    public byte[] JSONToPrinterCommand(String jsonString){


        try {
            return jsonToPrinterBytes(new ByteArrayInputStream(jsonString.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            lastAlignment = -1;
            lastBold = -1;
            lastLineSpace = -1;
            lastCharacterSpace = -1;
            lastUnderLine = -1;
            lastFontSize = new int[]{-1,-1};
        }

        return null;
    }

    public byte[] xmlStringToPrinterCommand(String xmlString) {

        try {

            return xmlToPrinterBytes(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (XmlPullParserException e) {
            Log.e("Xml parse error: ", e.toString());
            e.printStackTrace();
            return null;
        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
        finally {
            lastAlignment = -1;
            lastBold = -1;
            lastLineSpace = -1;
            lastCharacterSpace = -1;
            lastUnderLine = -1;
            lastFontSize = new int[]{-1,-1};
        }
    }

    /**
     Makes a 1-bit, bitmap from an image. Size will be Width/8*Height bytes
     @param
     @return
     @throws
     */
    private static byte[] makeRaster(Bitmap bitmap,boolean scaled,boolean flipped){

        //BitmapTranslator bitmapTranslator = new BitmapTranslator();

        byte[] bytes = BitmapTranslator.getInstance().getPortableBitmap(bitmap, scaled, flipped);

        return bytes;

    }
    /**
     Creates a 1-bit, bitmap from a text
     @param font type,font size, bold, underline, and the text to be made to raster
     @return
     @throws
     */
    private static byte[] makeBigTextRaster(String font,int size,boolean bold,boolean underline,String characters,boolean scaled,boolean flipped){

       // BitmapTranslator bitmapTranslator = new BitmapTranslator();
        byte[] queueRaster = BitmapTranslator.getInstance().textToPBM(font, size, bold, underline, characters, scaled, flipped);

        return queueRaster;



    }

    /**
     Creates a 1-bit, bitmap from a text
     @return
     @throws
      * @param base64Image encoded image to 1-bit bitmap.
     * @param
     */
    private static byte[] makeRasterFromBase64(String base64Image, boolean flipped){

        //BitmapTranslator bitmapTranslator = new BitmapTranslator();
        byte[] decodedPBM = new byte[0];
        try {
            decodedPBM = BitmapTranslator.getInstance().base64ToPBM(base64Image, true, flipped);
        } catch (BitmapTranslator.BitmapException e) {
            e.printStackTrace();
        }

        return decodedPBM;


    }


    private static byte[] setIndent(int numchar){



        int nL=(byte)(numchar%256);
        int nH=(byte)(numchar/256);

        byte[] bytes = {0x1B,0x24,(byte)nL,(byte)nH};


        return bytes;

    }

    private static byte[] setRelativePosition(int numdot){



        int nL=(byte)((numdot)%256);
        int nH=(byte)((numdot)/256);

        byte[] bytes = {0x1B,0x5C,(byte)nL,(byte)nH};


        return bytes;

    }

    private static byte[] selectCharacterFont(int param){

        byte[] bytes = {0x1B,0x4D,(byte)param};

        return bytes;

    }


    public static byte[] setPrintWidth(int width){

        int nL=(byte)(width%256);
        int nH=(byte)(width/256);

        return new byte[]{0x1D,0x57,(byte)nL,(byte)nH};

    }

    public static byte[] setLeftMargin(int length){


        int nL=(byte)(length%256);
        int nH=(byte)(length/256);



        return new byte[] {0x1D,0x4C,(byte)nL,(byte)nH};

    }
    /**
     Set bold or/and underline
     @param bold 0,1 underline 0,1. Multiply by 2: width 0,1 height 0,1
     @return
     @throws
     */
    private static byte[] setPrintMode(boolean underline,boolean bold,boolean width,boolean height){

            byte modeByte = (byte)((underline ? 1:0) + (bold ? 8:0) + (height ? 16:0) + (width?32:0));

            return new byte[]{0x1B,0x21,modeByte};

    }

    /**
     Set bold or/and underline
     @param flipped 0 =< rotation =< 3: 0,90,180,270 degrees.
     @return
     @throws
     */
    public static byte[] setRotation(boolean flipped){

        int rotation = (!flipped) ? 2 : 0;

        return  new byte[]{0x1B,0x56,(byte)rotation};


    }


    private static byte[] setInverted(int flipped){

        return new byte[]{0x1B, 0x7B, (byte)flipped};

    }

    private byte[] setAlignment(int position){


        //if (lastAlignment != position) {


            byte[] bytes = {0x1B, 0x61, (byte)position};

            lastAlignment = position;

            return bytes;
//        }
        /*else
            return new byte[]{};*/


    }

    private  byte[] stringToBytes(String string){

        String zeroTerminatedText = "";

        for (String letter:string.split(""))
            zeroTerminatedText += letter.concat("\0");


        zeroTerminatedText += "\n";

        return zeroTerminatedText.getBytes(charset);
    }

    private Charset charsetForCodePage(int codePage){


        String[] codePages = context.getResources().getString(R.string.codepages_to_android).split(" ");
        String charsetName;
        for (String cp:codePages) {
            int listedCodePage= Integer.parseInt(cp.split(":")[1]);


            if (codePage == listedCodePage){
                charsetName =   cp.split(":")[0];
                charset = Charset.forName(charsetName);

                return charset;

            }
        }
        charset = Charset.forName("IBM437");

        return charset;






    }

    private byte[] setCodePage(int table){


        charsetForCodePage(table);
        byte[] bytes = {0x1B, 0x74,(byte)table};

        return bytes;
    }

    private static byte[] printAndFeed(int linestofeed){


            byte[] command = {0x1B,0x64,(byte)linestofeed};

            return command;


    }

    private byte[] rowSpace(int space){

        int rows = space / 256;
        int restSpace = space - (rows*255);

        byte[] breakChars = new byte[rows];

        byte[] lineSpaceBytes = setLineSpace(255);


        byte[] charNewLineBytes = "\n".getBytes();

        for (int i = 0;i<breakChars.length;i++){

            breakChars[i] = charNewLineBytes[0];

        }

        byte[] rowBytes = appendByteArray(lineSpaceBytes,breakChars);

        lineSpaceBytes = setLineSpace(restSpace);

        byte[] restSpaceBytes = appendByteArray(lineSpaceBytes,charNewLineBytes);

        return appendByteArray(rowBytes,restSpaceBytes);

    }

    /**
     Set bold or/and underline
     @param cutmode 0 fullcut, 1 half cut, if  255 > numfeedlines > 0: Feeds for number of lines and half cuts.
     */
    private static byte[] cutPaper(int cutmode,int numfeedlines){



            return new byte[]{0x1D, 0x56, (byte)66,(byte)numfeedlines};

    }


    public static byte[] simpleCut(){


        byte[] bytes = new byte[]{0x1D, 0x56, 0};

        return bytes;

    }


    private byte[] setFontSize (int y, int x){

        if ((lastFontSize[0] != x) && (lastFontSize[1] != x)) {

            lastFontSize[0] = x;
            lastFontSize[1] = y;
            byte sizeByte = (byte) (x + (y << 4)); // 0x0X + 0xY0 first 8 bits x, last y

            byte[] bytes = new byte[]{0x1D, 0x21, sizeByte};

            return bytes;
        }
        else return
                new byte[]{};




    }
    /**
     Set space to the next vertical line .
     @param height default:30 min:0 max:255
     */
    private byte[] setLineSpace(int height){

        if (lastLineSpace != height) {
            byte[] bytes = {0x1B, 0x33, (byte) height};
            lastLineSpace = height;

            return bytes;
        }
        else
            return new byte[]{};


    }

    /**
    Set space to the next horizontal character.
     @param space:0 max: 255
     */
    private byte[] setCharacterSpace(int space){

        if (lastCharacterSpace != space) {
            byte[] bytes = {0x1B, 0x20, (byte) space};
            lastCharacterSpace = space;

            return bytes;
        }
        else
            return new byte[]{};

    }


    private static String nameSpace = null;

    /**
     Parses xml data.
     @param inputstream the stream with data to be parsed
     @return byte[] an array with the printer commands, should not exceed 4096 bytes to ensure performance.
     */
    private byte[] xmlToPrinterBytes(InputStream inputstream) throws XmlPullParserException, IOException {


        ArrayList<byte[]> commandList = new ArrayList<byte[]>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {


                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(inputstream,null);

                parser.nextTag();

                parser.require(XmlPullParser.START_TAG, nameSpace, "qticket");

                while (parser.next() != XmlPullParser.END_TAG){

                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    readTicket(parser, commandList);

                }



        }
            finally {

                inputstream.close();

        }


        if (!ticketFlipped){

            Collections.reverse(commandList);

        }

        commandList.add(0,setRotation(ticketFlipped));
        commandList.add(0, setLeftMargin(ticketMargin));

        commandList.add(0, setCodePage(codePage));

        commandList.add(0, setPrintWidth(ticketWidth));

        for (byte[] command:commandList) {
            outputStream.write(command);
        }

        outputStream.write(cutPaper(1, 1));

        return outputStream.toByteArray();
    }

    private void readTicket(XmlPullParser parser,List<byte[]> commandList) throws IOException, XmlPullParserException {

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        String tagname = parser.getName();

        switch (tagname) {
            case "ticketsettings":

                while (parser.nextTag() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }


                    String name = parser.getName();
                    String xmlVal;
                    switch (name) {

                        case "width":

                            xmlVal = parser.nextText().trim();
                            ticketWidth = (xmlVal.length()==0) ? 0:Integer.parseInt(xmlVal.trim());


                            break;

                        case "margin":
                            xmlVal = parser.nextText().trim();
                            ticketMargin =  (xmlVal.length()==0) ? 0:Integer.parseInt(xmlVal.trim());


                            break;
                        case "flipped":

                            xmlVal = parser.nextText().trim();
                            ticketFlipped =  (xmlVal.equals("0") ? false:true);


                            break;
                        case "codepage":

                            xmlVal = parser.nextText().trim();
                            codePage = (xmlVal.length()==0) ? 0:Integer.parseInt(xmlVal);
                            charsetForCodePage(codePage);

                            break;
                    }
                }

                break;

            case "rowspace":


                int space  = Integer.parseInt(parser.nextText().trim());
                commandList.add(rowSpace(space));

                break;
            case "queuenumber":

                commandList.add(readQueueNumber(parser));

                break;
            case "image":



                commandList.add(readImage(parser));

                break;
            default:

                byteBuffer = new ByteArrayOutputStream();
                byteBuffer.write(readText(parser));
                commandList.add(byteBuffer.toByteArray());

                break;

        }
        byteBuffer.close();


    }

    private byte[] readImage(XmlPullParser parser) throws IOException, XmlPullParserException {

        int alignment = 0;
        int lineSpace = default_linespace;
        String bitmapString64 = "";
        int event = parser.getEventType();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            event = parser.getEventType();


            String xmlVal;
            String tagName = parser.getName();
            switch (tagName) {
                case "alignment":

                    xmlVal = parser.nextText().trim();
                    alignment = (xmlVal.length()==0) ? 0:Integer.parseInt(xmlVal);
                    alignment = (!ticketFlipped) ? 2-alignment:alignment;

                    break;

                case "linespace":

                    xmlVal = parser.nextText().trim();
                    lineSpace = (xmlVal.length()==0) ? 0:Integer.parseInt(xmlVal);


                    break;
                case "base64":

                    bitmapString64 = parser.nextText();
                    break;
            }

        }

        byte[] alignmentBytes = setAlignment(alignment);
        byte[] lineSpaceBytes = setLineSpace(lineSpace);
        byte[] raster = makeRasterFromBase64(bitmapString64, ticketFlipped);


        return appendByteArray(alignmentBytes, lineSpaceBytes, raster);
    }

    private byte[] readText(XmlPullParser parser) throws IOException, XmlPullParserException {

        boolean width = false;
        boolean height = false;
        boolean bold = false;
        boolean underline = false;
        int alignment = 0;
        int offset = 0;
        int lineSpace = 0;
        int characterSpace = 0;
        int numCharacters = 0;
        String characters = "";

        while (parser.nextTag() != XmlPullParser.END_TAG) {

            String name = parser.getName();
            String xmlVal;
            switch (name){
                case "width":


                    xmlVal = parser.nextText().trim();
                    width = (!xmlVal.equals("0"));

                    break;


                case "height":


                    xmlVal = parser.nextText().trim();
                    height = (!xmlVal.equals("0"));

                    break;


                case "bold":

                    xmlVal = parser.nextText().trim();
                    bold = (!xmlVal.equals("0"));

                    break;

                case "underline":

                    xmlVal = parser.nextText().trim();
                    underline = (!xmlVal.equals("0"));


                    break;

                case "characters":
                characters = parser.nextText().trim();
                numCharacters = characters.length();



                    int numRTLCharacters = 0;

                    for (char character:characters.toCharArray()) {

                        if (((int)character > 1424) && ((int)character < 1792))
                            numRTLCharacters++;

                    }

                    if (!ticketFlipped && (numRTLCharacters < numCharacters/2))
                        characters = new StringBuilder(characters).reverse().toString();
                    else if (ticketFlipped && (numRTLCharacters > numCharacters/2))
                        characters = new StringBuilder(characters).reverse().toString();

                    break;
                case "alignment":


                    xmlVal = parser.nextText().trim();
                    alignment = (xmlVal.length()==0) ? 0:Integer.parseInt(xmlVal);
                    alignment = (!ticketFlipped) ? 2-alignment:alignment;
                    break;

                case "offsetx":


                    xmlVal = parser.nextText().trim();
                    offset = (xmlVal.length()==0) ? 0:Integer.parseInt(xmlVal);

                    break;

                case "linespace":

                    xmlVal = parser.nextText().trim();
                    lineSpace = (xmlVal.length()==0) ? 0:Integer.parseInt(xmlVal);

                    break;

                case "characterspace":

                    xmlVal = parser.nextText().trim();
                    characterSpace = (xmlVal.length()==0) ? 0:Integer.parseInt(xmlVal);


                    break;
            }


        }



        byte[] appearance = setPrintMode(underline, bold, width, height);

        byte[] lineSpaceBytes = setLineSpace(lineSpace);


        //When characters are flipped, the correct offset is going to be printwidth - desired offset - charactersize * number of characters.
        if ((offset > 0) && (!ticketFlipped)) {
            offset = ticketWidth - offset - numCharacters * (width ? 2:1) * 12;
            alignment = 0;

        }

        byte[] alignmentBytes = setAlignment(alignment);
        byte[] offsetBytes = setIndent(offset);
        byte[] characterSpaceBytes = setCharacterSpace(characterSpace);
        byte[] characterBytes = stringToBytes(characters);
        return appendByteArray(appearance,alignmentBytes, lineSpaceBytes, offsetBytes, characterSpaceBytes,characterBytes);


    }

    private byte[] readQueueNumber(XmlPullParser parser) throws IOException, XmlPullParserException {

        String font = "";
        String characters = "";
        boolean scaled = true;
        int size = 70;
        boolean bold = false;
        boolean underline = false;
        int alignment = 0;

        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();
            String xmlVal;

            switch (tagName) {
                case "font":


                    font = parser.nextText().trim();


                    break;

                case "alignment":

                    xmlVal = parser.nextText().trim();
                    alignment = (xmlVal.length() == 0) ? 0 : Integer.parseInt(xmlVal);
                    alignment = (!ticketFlipped) ? 2 - alignment : alignment;


                    break;
                case "size":

                    xmlVal = parser.nextText().trim();
                    size = (xmlVal.length() == 0) ? 0 : Integer.parseInt(xmlVal);


                    break;


                case "bold":

                    xmlVal = parser.nextText().trim();
                    bold = (!xmlVal.equals("0"));

                    break;

                case "underline":

                    xmlVal = parser.nextText().trim();
                    underline = (!xmlVal.equals("0"));

                    break;
                case "characters":

                    characters = parser.nextText().trim();


                    break;
            }


        }

        byte[] alignmentBytes = setAlignment(alignment);
        byte[] queueRaster = makeBigTextRaster(font, size, bold, underline, characters, scaled, ticketFlipped);

        return appendByteArray(alignmentBytes, queueRaster);



    }






    private static byte[] appendByteArray(byte[]... parameters){

        int size = 0;

        for (byte[] commandByte:parameters)
            size+=commandByte.length;

        byte[] bytes = new byte[size];

        int bytesLength = 0;

        for (byte[] commandByte:parameters) {

            System.arraycopy(commandByte, 0, bytes,bytesLength,commandByte.length);
            bytesLength += commandByte.length;
        }
        return bytes;

    }



    private byte[] jsonToPrinterBytes(InputStream inputStream) throws IOException {


        ArrayList<byte[]> commandList = new ArrayList<>();

       // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

        try {

            readJson(reader, commandList);


            if (!ticketFlipped){

                Collections.reverse(commandList);

            }

            commandList.add(0,setRotation(ticketFlipped));
            commandList.add(0, setLeftMargin(ticketMargin));

            commandList.add(0, setCodePage(codePage));

            commandList.add(0, setPrintWidth(ticketWidth));

            int commandLength = 0;

            for (byte[] command:commandList) {
                commandLength+=command.length;

            }
            byte[] cutBytes = cutPaper(1, 1);
            ByteBuffer byteBuffer = ByteBuffer.allocate(commandLength + cutBytes.length);

            for (byte[] command:commandList) {
                byteBuffer.put(command);

            }
            byteBuffer.put(cutBytes);

            return byteBuffer.array();



        }
        finally{
                reader.close();
                //outputStream.close();
                commandList.clear();


            }


        }

    private void readJson(JsonReader reader,ArrayList<byte[]> commandList) throws IOException {


        reader.beginObject();

        if(reader.nextName().equals("qticket")) {
            reader.hasNext();
            readLineObject(reader, commandList);

        }

        reader.endObject();



    }


    private void readLineObject(JsonReader reader,ArrayList<byte[]> commandList) throws IOException {


        reader.beginArray();

        while(reader.hasNext()){
            reader.beginObject();
            switch (reader.nextName()){

                case "ticketsettings":
                    readSettings(reader);
                    break;
                case "rowspace":
                    commandList.add(rowSpace(reader.nextInt()));
                    break;
                case "bigtext":
                    commandList.add(readBigText(reader));
                    break;
                case "image":
                    commandList.add(readImageJson(reader));
                    break;
                case "text":
                    commandList.add(readTextJson(reader));
                    break;
            }

            reader.endObject();

        }

        reader.endArray();




    }

    private void readSettings(JsonReader reader) throws IOException {


        reader.beginObject();
        while (reader.hasNext()){
            switch (reader.nextName()){
                case "margin":
                    ticketMargin = reader.nextInt();
                    break;
                case "width":
                    ticketWidth = reader.nextInt();
                    break;
                case "flipped":
                    ticketFlipped = reader.nextBoolean();
                    break;
                case "codepage":
                    codePage = reader.nextInt();
                    charsetForCodePage(codePage);
                    break;
            }



        }
        reader.endObject();






    }

    private byte[] readBigText(JsonReader reader) throws IOException {

        String font = null;
        int alignment = 0;
        int size = 0;
        boolean bold = false;
        boolean underline = false;
        String characters = null;

        reader.beginObject();
        while (reader.hasNext()){
            switch (reader.nextName()){
                case "font":
                    font = reader.nextString();
                    break;
                case "alignment":
                    alignment = reader.nextInt();
                    alignment = (!ticketFlipped) ? 2-alignment:alignment;
                    break;
                case "size":
                    size = reader.nextInt();
                    break;
                case "bold":
                    bold = reader.nextBoolean();
                    break;
                case "underline":
                    underline = reader.nextBoolean();
                    break;
                case "characters":
                    characters = reader.nextString();
                    break;
            }



        }
        reader.endObject();

        byte[] alignmentBytes = setAlignment(alignment);
        byte[] queueRaster = makeBigTextRaster(font, size, bold, underline, characters, true, ticketFlipped);

        return  appendByteArray(alignmentBytes, queueRaster);




    }

    private byte[] readImageJson(JsonReader reader) throws IOException {

        int alignment = 0;
        String base64 = null;

        reader.beginObject();
        while (reader.hasNext()){
            switch (reader.nextName()){
               case "alignment":
                    alignment = reader.nextInt();
                   alignment = (!ticketFlipped) ? 2-alignment:alignment;
                    break;
                case "base64":
                    base64 = reader.nextString();
                    break;
            }



        }
        reader.endObject();


        byte[] alignmentBytes = setAlignment(alignment);
        byte[] raster = makeRasterFromBase64(base64, ticketFlipped);


        return appendByteArray(alignmentBytes, raster);


    }

    private byte[] readTextJson(JsonReader reader) throws IOException {

        boolean width = false;
        boolean height = false;
        boolean bold = false;
        boolean underline = false;
        int alignment = 0;
        int offsetX = 0;
        int characterSpace = 0;
        String characters = null;

        int numCharacters = 0;


        reader.beginObject();
        while (reader.hasNext()){
            switch (reader.nextName()){
                case "width":
                    width = reader.nextBoolean();
                    break;
                case "height":
                    height = reader.nextBoolean();
                    break;
                case "bold":
                    bold = reader.nextBoolean();
                    break;
                case "underline":
                    underline = reader.nextBoolean();
                    break;
                case "alignment":
                    alignment = reader.nextInt();
                    alignment = (!ticketFlipped) ? 2-alignment:alignment;
                    break;
                case "offsetx":
                    offsetX = reader.nextInt();
                    break;
                case "characterspace":
                    characterSpace = reader.nextInt();
                    break;
                case "characters":
                    characters = reader.nextString();
                    numCharacters = characters.length();
                    int numRTLCharacters = 0;

                    for (char character:characters.toCharArray()) {

                        if (((int)character > 1424) && ((int)character < 1792))
                            numRTLCharacters++;

                    }

                    if (!ticketFlipped && (numRTLCharacters < numCharacters/2))
                        characters = new StringBuilder(characters).reverse().toString();
                    else if (ticketFlipped && (numRTLCharacters > numCharacters/2))
                        characters = new StringBuilder(characters).reverse().toString();

                    break;
            }



        }
        reader.endObject();

        byte[] appearance = setPrintMode(underline, bold, width, height);

        //When characters are flipped, the correct offset is going to be printwidth - desired offset - charactersize * number of characters.
        if ((offsetX > 0) && (!ticketFlipped)) {
            offsetX = ticketWidth - offsetX - numCharacters * (width ? 2 : 1) * 12;
            alignment = 0;

        }
        else if(offsetX < 0)
            offsetX = 0;
        byte[] alignmentBytes = setAlignment(alignment);
        byte[] offsetBytes = setIndent(offsetX);
        byte[] characterSpaceBytes = setCharacterSpace(characterSpace);
        byte[] characterBytes = stringToBytes(characters);
        return appendByteArray(appearance,alignmentBytes,offsetBytes, characterSpaceBytes,characterBytes);

    }

}
