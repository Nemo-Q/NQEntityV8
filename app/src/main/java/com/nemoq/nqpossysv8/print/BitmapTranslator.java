package com.nemoq.nqpossysv8.print;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.Log;

import java.lang.reflect.Type;

/**
 * Created by Temporär on 2015-08-14.
 */
public class BitmapTranslator {

    static int DOTS_PER_SEGMENT = 8;

    private byte[] portableBitmap;

    public boolean scaled;

    /**
     Makes a PBM out of a bitmap.
     @return Returns the portable bitmap byte array.
     @throws
     */
    public BitmapTranslator(){




    }


    //Make a bitmap out of the text that will represent the queuenumber.
    public byte[] textToPBM(String font,int size,int bold,int underline,String characters,boolean scaling,int flipped){




        Typeface typeFace = Typeface.create(Typeface.DEFAULT,Typeface.BOLD);
        Paint textPaint = new Paint();

        textPaint.setColor(Color.BLACK);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(size);
        textPaint.setFakeBoldText(false);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(typeFace);



        Rect bounds = new Rect();
        textPaint.getTextBounds(characters, 0, characters.length(), bounds);

        Bitmap textBitmap = Bitmap.createBitmap(bounds.width(),bounds.height(), Bitmap.Config.ARGB_8888);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.FILL);



        Canvas canvas = new Canvas(textBitmap);
        canvas.drawRect(0, 0, bounds.width(), bounds.height(), backgroundPaint);

        canvas.drawPaint(backgroundPaint);


        canvas.drawText(characters, bounds.left * -1, bounds.top * -1, textPaint);

        canvas.drawBitmap(textBitmap, 0, 0, textPaint);









        return getPortableBitmap(textBitmap,scaling,flipped);



    }

    //Make a bitmap out of the base64 string
    public byte[] base64ToPBM(String base64Image,boolean scale,int flipped){

        byte[] base64Bytes = Base64.decode(base64Image, Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(base64Bytes, 0, base64Bytes.length);
        if (bitmap == null) {

            Log.e("BitmapTranslator:", "Couldn't make bitmap out of base64");

            return null;
        }

        return getPortableBitmap(bitmap,scale,flipped);


    }




    public byte[] getPortableBitmap(Bitmap bitmap,boolean scale,int flipped){


        byte[] PBM = makePBM(makeBitArray(bitmap,flipped),bitmap,scale);

        return PBM;

    }

    //Read all the pixels, and make 1 bit depth bitmap. -1 is white lower is black.
    private byte[] makeBitArray(Bitmap bitmap,int flipped){


        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];

        int xPixels = bitmap.getWidth();
        int yPixels = bitmap.getHeight();

        bitmap.getPixels(pixels, 0, xPixels, 0, 0, xPixels, yPixels);

        byte[] bitArray = new byte[pixels.length];

        if (flipped == 0) {
            for (int i = pixels.length-1; i > -1; i--) {

                bitArray[pixels.length-1-i] = (pixels[i] < -1000000) ? (byte) 1 : (byte) 0;


            }
        }
        else {
            for (int i = 0; i < pixels.length - 1; i++) {

                bitArray[i] = (pixels[i] < -1) ? (byte) 1 : (byte) 0;


            }
        }

        return  bitArray;
    }


    //read out the 1-bit bitmap and translate to portable-bitmap and the corresponding combination for the printer.
    private byte[] makePBM(byte[] bitArray,Bitmap bitmap,boolean scale){



        int xPixels = bitmap.getWidth();

        int xSegments = (xPixels % 8 == 0) ? xPixels/8 : xPixels / DOTS_PER_SEGMENT +1;
        int xL = (xSegments % 256);
        int xH = xSegments / (256);


        int ySegments = bitmap.getHeight();

        int yL = ySegments % 256;
        int yH = ySegments / 256;


        byte[] pbmBytes = new byte[8+xSegments*ySegments];


        //Settings message
        pbmBytes[0] = 0x1D;
        pbmBytes[1] = 0x76;
        pbmBytes[2] = 0x30;
        pbmBytes[3] = (scale) ? (byte)3 : (byte)0;//scale 3 double
        pbmBytes[4] = (byte)xL; //X segments
        pbmBytes[5] = (byte)xH;// X segments/256
        pbmBytes[6] = (byte)yL; //Y segments
        pbmBytes[7] = (byte)yH; // Y segments/256


        int segmentIndex = 0;
        int pbmIndex = 0;
        byte segmentByte = 0x0;


        for (int i = 0;i<bitArray.length-1;i++){


            if ((bitArray[i] != 0)){


                segmentByte  += pixelToDotByte(segmentIndex);
            }



            if (segmentIndex == (DOTS_PER_SEGMENT-1)){

                pbmBytes[8+pbmIndex] = segmentByte;
                segmentByte = 0x0;
                segmentIndex = 0;
                pbmIndex++;


            }
            else if((i+1) % xPixels == 0){
                pbmBytes[8+pbmIndex] = segmentByte;
                segmentByte = 0x0;
                segmentIndex = 0;
                pbmIndex++;
            }
            else
                segmentIndex++;




        }



        return pbmBytes;



    }



    //Every 8 bit segment is described by 2 byte hex. (10001000) -> (*----*----)

    private byte pixelToDotByte(int segmentIndex){


        byte segmentByte = 0x0;



        switch (segmentIndex) {
            case 0:
                segmentByte += 0x80;
                break;
            case 1:
                segmentByte += 0x40;
                break;
            case 2:
                segmentByte += 0x20;
                break;
            case 3:
                segmentByte += 0x10;
                break;
            case 4:
                segmentByte += 0x8;
                break;
            case 5:
                segmentByte += 0x4;
                break;
            case 6:
                segmentByte += 0x2;
                break;
            case 7:
                segmentByte +=0x1;
                break;
        }

        return segmentByte;
    }



    private byte[] reverseArray(byte[] bytes){


        byte[] reverseBytes = new byte[bytes.length];

        for (int i = bytes.length-1;i > -1;i--) {

            reverseBytes[bytes.length-1-i] = bytes[i];

        }

        return  reverseBytes;



    }



}
