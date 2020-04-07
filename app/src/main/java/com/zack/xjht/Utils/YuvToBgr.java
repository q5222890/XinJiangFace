package com.zack.xjht.Utils;

public class YuvToBgr {

	static byte[] data = new byte[1024*1024*2];
    private static int R = 0;
    private static int G = 1;
    private static int B = 2;
    private static class RGB{
        public int r, g, b;
    }
    private static RGB yuvTorgb(byte Y, byte U, byte V){
		RGB rgb = new RGB();
		rgb.r = (int)((Y&0xff) + 1.4075 * ((V&0xff)-128));
		rgb.g = (int)((Y&0xff) - 0.3455 * ((U&0xff)-128) - 0.7169*((V&0xff)-128));
		rgb.b = (int)((Y&0xff) + 1.779 * ((U&0xff)-128));
		rgb.r =(rgb.r<0? 0: rgb.r>255? 255 : rgb.r);
		rgb.g =(rgb.g<0? 0: rgb.g>255? 255 : rgb.g);
		rgb.b =(rgb.b<0? 0: rgb.b>255? 255 : rgb.b);
		return rgb;
	}
    public static byte[] YV12ToRGB(byte[] src, int width, int height){
        int numOfPixel = width * height;
        int positionOfV = numOfPixel;
        int positionOfU = numOfPixel/4 + numOfPixel;
        byte[] rgb = new byte[numOfPixel*3];

        for(int i=0; i<height; i++){
            int startY = i*width;
            int step = (i/2)*(width/2);
            int startV = positionOfV + step;
            int startU = positionOfU + step;
            for(int j = 0; j < width; j++){
                int Y = startY + j;
                int V = startV + j/2;
                int U = startU + j/2;
                int index = Y*3;
                RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
                rgb[index+R] = (byte)tmp.b;
                rgb[index+G] = (byte)tmp.g;
                rgb[index+B] = (byte)tmp.r;
            }
        }
        return rgb;
    }
    
    public static int[] NV21ToRGB(byte[] src, int width, int height){
		int numOfPixel = width * height;
		int positionOfV = numOfPixel;
		int[] rgb = new int[numOfPixel*3];
		for(int i=0; i<height; i++){
			int startY = i*width;
			int step = i/2*width;
			int startV = positionOfV + step;
			for(int j = 0; j < width; j++){
				int Y = startY + j;
				int V = startV + j/2;
				int U = V + 1;
				int index = Y*3;
				RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
				rgb[index+R] = tmp.r;
				rgb[index+G] = tmp.g;
				rgb[index+B] = tmp.b;
			}
		}
		return rgb;
	}

    public static void YUV_NV21_TO_RGB(byte[] yuv, byte[] rgb, int width, int height) {
        final int frameSize = width * height;

        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for (int i = 0, ci = ii; i < height; ++i, ci += di) {
            for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                int y = (0xff & ((int) yuv[ci * width + cj]));
                int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
                int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgb[a++] = (byte) r;
                rgb[a++] = (byte) g;
                rgb[a++] = (byte) b;
            }
        }
    }
	
}
