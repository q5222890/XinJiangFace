package com.zack.xjht.Utils;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileCache {

    private static final String TAG = "FileCache";
    private File cacheDir;

    public FileCache(Context context) {

        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) { //判断sd卡可读写
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),
                    "com_zack_intelligent");  // /mnt/sdcard外置sd卡根目录
            if (!cacheDir.exists())
                cacheDir.mkdirs();
        } else {
            cacheDir = context.getCacheDir();
            if (!cacheDir.exists())
                cacheDir.mkdirs();
        }
    }

    public File getFile(String url) {

        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;

    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

    public File getCacheDir() {
        return this.cacheDir;
    }

    /**
     * 获取Cache目录文件总共大小
     *
     * @param file
     * @return
     */
    public long getCacheSize(File file) {
        long size = 0;
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                size = size + getCacheSize(files[i]);
            } else {
                size = size + files[i].length();
            }
        }
        return size;
    }

    /**
     * 获取Cache目录文件总共大小(格式化)
     *
     * @param file
     * @return
     */
    public String formatCacheSize(File file) {
        if (file.exists()) {
            double Dsize = Double.valueOf(getCacheSize(file));
            double B = 1024;
            double K = 1048576;
            if (Dsize < K) { //如果数据量小于k以字节为单位显示
                return Double.parseDouble(String.format("%.2f", Dsize / B)) //保留小数点后两位
                        + "K";
            } else {  //如果数据量大于k就以兆为单位显示
                return Double.parseDouble(String.format("%.2f", Dsize / K))
                        + "M";
            }
        }
        return "";
    }

    /**
     * 删除cache目录下所有文件
     *
     * @param file
     * @return
     */
    public boolean deleteCache(File file) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                deleteCache(files[i]);
            } else {
                files[i].delete();
            }
        }
        return true;
    }

    /**
     * 写入JSON 文件
     *
     * @param fileName
     * @param content
     */
    public void writeJsonFile(String fileName, String content) {

        if (content != null) {
            try {
                File f = new File(cacheDir, fileName);
                FileWriter fw = new FileWriter(f.getAbsolutePath());
                PrintWriter out = new PrintWriter(fw);
                out.write(content);
                out.println();
                fw.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 读取json文件
     *
     * @param fileName
     * @return
     */
    public String readJsonFile(String fileName) {
        String fileContentStr = "";
        try {

            File f = new File(cacheDir, fileName);
            if (!f.exists()) {
                return fileContentStr;
            }
            FileInputStream inStream = new FileInputStream(f);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = inStream.read(buffer)) != -1) {
                stream.write(buffer, 0, length);
            }
            stream.close();
            inStream.close();
            fileContentStr = stream.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileContentStr;
    }


    public void writeFile(String fileName, String content) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readFile(String fileName){
        FileReader fileReader =null;
        try {
            fileReader =new FileReader(fileName);
            char[] chars =new char[1024];
            int len;
            try {
                if((len = fileReader.read(chars)) !=-1){
                    return new String(chars, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return "";

    }
}