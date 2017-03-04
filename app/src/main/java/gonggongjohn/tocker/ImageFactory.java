package gonggongjohn.tocker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.Date;

public class ImageFactory {
    public Bitmap compressBitmap(Bitmap image){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 95;

        while(baos.toByteArray().length / 1024 > 75){
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 5;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public byte[] BitmapToByteArray(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , baos);
        return baos.toByteArray();
    }

    public void savePicture(Bitmap bitmap){
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), ""+(new Date()).getTime()+".jpg");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将字符串写入到文本文件中
    public void saveString(String strcontent, String filePath, String fileName) {
        makeFile(filePath, fileName);
        String strFilePath= filePath + fileName;
        String strContent= strcontent + "\r\n";
        try{
            File file = new File(strFilePath);
            if (!file.exists()){
                System.out.println("Create the file:"+strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //生成文件
    public File makeFile(String filePath, String fileName){
        File file = null;
        makeDirectory(filePath);
        try{
            file = new File(filePath + fileName);
            if (!file.exists()){
                file.createNewFile();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return file;
    }

    //生成文件夹
    public void makeDirectory(String filePath){
        File file = null;
        try{
            file = new File(filePath);
            if (!file.exists()){
                file.mkdir();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
