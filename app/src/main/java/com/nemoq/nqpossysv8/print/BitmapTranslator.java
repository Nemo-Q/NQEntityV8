package com.nemoq.nqpossysv8.print;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Temporär on 2015-08-14.
 */
public class BitmapTranslator {

    static int DOTS_PER_SEGMENT = 8;

    Bitmap bitmap;
    private byte[] portableBitmap;



    public BitmapTranslator(Bitmap bitMap){




        bitmap = bitMap;



    }

    public byte[] getPortableBitmap(){

        portableBitmap = makePBM(makeBitArray(bitmap));

        return portableBitmap;

    }


    private byte[] makeBitArray(Bitmap bitmap){


        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];

        int xPixels = bitmap.getWidth();
        int yPixels = bitmap.getHeight();

        bitmap.getPixels(pixels, 0, xPixels, 0, 0, xPixels, yPixels);

        byte[] bitArray = new byte[pixels.length];

        for (int i = 0;i<pixels.length-1;i++){

            bitArray[i] = (pixels[i] < -1) ? (byte) 1 : (byte) 0;


        }


        return  bitArray;
    }



    private byte[] makePBM(byte[] bitArray){



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
        pbmBytes[3] = 0x0;
        pbmBytes[4] = (byte)xL;
        pbmBytes[5] = (byte)xH;
        pbmBytes[6] = (byte)yL;
        pbmBytes[7] = (byte)yH;


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
                segmentIndex = DOTS_PER_SEGMENT - 1;
            }
            else
                segmentIndex++;




        }



        return pbmBytes;



    }





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







}
