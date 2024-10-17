package me.j0keer.mediaplayer.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class Downloader extends Thread implements Runnable{
    private String url;
    private String filename;
    private String destinationFolder;
    private DownloaderCallback callback;

    public Downloader(){
        this.callback = null;
    }

    public Downloader setCallback(DownloaderCallback callback){
        this.callback = callback;
        return this;
    }

    public Downloader setUrl(String url){
        this.url = url;
        return this;
    }

    public Downloader setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public Downloader setDestinationFolder(String destinationFolder){
        this.destinationFolder = destinationFolder;
        return this;
    }

    public void download(){
        super.start();
    }

    public void run(){
        try {
            URL url = new URL(this.url);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();

            long total = urlConnection.getContentLengthLong();
            int count;

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(destinationFolder + File.separator + filename);

            byte data[] = new byte[4096];
            long current = 0;

            while ((count = input.read(data)) != -1) {
                current += count;
                output.write(data, 0, count);
                if(callback!=null){
                    callback.onProgress((int) ((current*100)/total));
                }
            }

            output.flush();

            output.close();
            input.close();

            if(callback!=null){
                callback.onComplete();
            }
        } catch (Exception e) {
            if(callback!=null)
                callback.onFailed(e.getMessage());
        }
    }

    public interface DownloaderCallback{
        void onComplete();
        void onFailed(String message);
        void onProgress(int progress);
    }
}