package com.myntai.slightech.myntairobotromupdateservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import timber.log.Timber;

public class FileUtil {
    private static final int BUFFER = 2048;
    /**
     * 读取asset目录下文件。
     *
     * @return content
     */
    public static String readFile(Context mContext, String file, String code) {
        int len;
        byte[] buf;
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(file);
            len = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static boolean isMusicFile(File file) {
        return file.getName().toLowerCase().endsWith(".mp3");
    }

    public static boolean isVideoFile(File file) {
        return file.getName().toLowerCase().endsWith(".mp4") || file.getName().toLowerCase().endsWith(".3gp");
    }

    public static void writeReaderToFile(Reader reader, File file) {
        Writer fw = null;
        try {
            fw = new FileWriter(file);
            char[] buff = new char[1024 * 10];
            int sum = reader.read(buff);
            while (sum != -1) {
                fw.write(buff, 0, sum);
                sum = reader.read(buff);
            }
            fw.flush();
        } catch (IOException e) {
            Timber.e(e, "文件IO过程出错 %s", e.toString());
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * check a file if exist,
     * if not then create it,
     * if a directory with same name exist already, delete the directory and create.
     * <p>
     * warning: this method will delete directory in some case
     *
     * @param path the path of the file
     * @return true if file exist or create success or delete&create success,
     * false otherwise
     */
    public static boolean checkFileExistOrCreate(File path) {
        File folder = path.getParentFile();
        if (!checkDirectoryExistOrCreate(folder)) {
            return false;
        }
        try {
            if (path.exists()) {
                return path.isFile() || path.delete() && path.createNewFile();
            } else {
                return path.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * check a Directory if exist,
     * if not then create it,
     * if a file with same name exist already, delete the file and create a directory.
     * <p>
     * * warning: this method will delete files in some case
     *
     * @param path the path of the Directory
     * @return true if Directory exist or create success or delete&create success,
     * false otherwise
     */
    public static boolean checkDirectoryExistOrCreate(File path) {
        if (path.exists()) {
            return path.isDirectory() || path.delete() && path.mkdirs();
        } else {
            return path.mkdirs();
        }
    }

    public static void copyFileFromRawToOthers(final Context context, int id, final String targetPath) {
        InputStream in = context.getResources().openRawResource(id);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(targetPath);
            byte[] buff = new byte[1024];
            int read;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteAppUserData(Context context) {
        //删除内部文件
        String cacheFile = context.getCacheDir().getAbsolutePath();
        deleteFolder(new File(cacheFile));//删除缓存
        String internalPath = context.getFilesDir().getParent();
        deleteSharePreference(new File(internalPath + "/shared_prefs"));//删除share_prefs
        File file_files = context.getFilesDir();
        deleteFolder(file_files);//删除files
        File databasesFile = new File(internalPath + "/databases");
        deleteFolder(databasesFile); //删除databases

        //删除外部文件
        String externalPath = context.getExternalFilesDir(null).getParent();
        String externalCacheFile = externalPath + "/cache";
        String externalFileFiles = externalPath + "/files";
        String externalFlytekFile = externalPath + "/filesiflytek";
        deleteFolder(new File(externalCacheFile));
        deleteFolder(new File(externalFileFiles));
        deleteFolder(new File(externalFlytekFile));
    }

    /**
     * 递归删除
     */
    private static void deleteFolder(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (File f : files) {
                deleteFolder(f);
            }
        }
        file.delete();
    }

    //不删除motion_settings_name.xml
    private static void deleteSharePreference(File file) {
        boolean canDelete = true;
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (File f : files) {
                if (!TextUtils.equals(f.getName(), "motion_settings_name.xml")) {
                    deleteFolder(f);
                } else {
                    canDelete = false;
                }
            }
        }
        if (canDelete) {
            file.delete();
        }
    }



    /**
     * 删除单个文件
     *
     * @param file 需要删除的文件，保证不能是文件夹
     * @return 是否删除成功
     */
    public static boolean deleteFile(File file) {
        return file.exists() && !file.isDirectory() && file.delete();
    }

    /**
     * 获取文件的扩展名
     */
    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    /**
     * 获取文件的名称
     */
    private static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return "";
    }


    public static Bitmap getBitmapFromPath(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        //bitmap = retriever.getFrameAtTime(-1);
        Bitmap bitmap = retriever.getFrameAtTime();
        // Assume this is a corrupt video file.
        retriever.release();
        return bitmap;
    }


    /**
     * 获得圆角图片,可以设置圆角大小
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        if (bitmap == null) {
            return null;
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 扫描 path 路径下的 所有视频文件，并添加在list中
     *
     * @param path   需要扫描的路径
     * @param videos 所有的视频集合
     */
    public static void scanVideo(final String path, List<String> videos) {
        File dirFile = new File(path);
        if (dirFile.isDirectory()) {
            File[] files = dirFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        return true;
                    }

                    String name = pathname.getName();
                    int i = name.indexOf('.');
                    if (i != -1) {
                        name = name.substring(i);//获取文件后缀名
                        return name.equalsIgnoreCase(".mp4") || name.equalsIgnoreCase(".3gp");
                    }
                    return false;
                }
            });
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (file.isFile()) {
                    videos.add(file.getAbsolutePath());
                } else {
                    scanVideo(file.getAbsolutePath(), videos);
                }
            }
        }
    }


    /**
     * 扫描 path 路径下的 所有music文件，并添加在list中
     *
     * @param path  需要扫描的路径
     * @param songs 所有的视频集合
     */
    public static void scanMusic(final String path, List<String> songs) {
        File dirFile = new File(path);
        if (dirFile.isDirectory()) {
            File[] files = dirFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        return true;
                    }

                    String name = pathname.getName();
                    int i = name.indexOf('.');
                    if (i != -1) {
                        name = name.substring(i);//获取文件后缀名
                        return name.equalsIgnoreCase(".mp3");
                    }
                    return false;
                }
            });
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (file.isFile()) {
                    songs.add(file.getAbsolutePath());
                } else {
                    scanMusic(file.getAbsolutePath(), songs);
                }
            }
        }
    }


    /**
     * 把文件复制到目标路径下，并以name命名
     *
     * @param file 被复制的文件
     * @param dest 目标路径
     * @param name 文件名字
     */
    public static boolean copyVideo(File file, File dest, String name) {
        //保证视频存在
        if (!file.exists() || file.isDirectory()) {
            return false;
        }
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        byte[] data = new byte[BUFFER];
        int read;
        try {
            //保证目标路径OK
            if (!dest.exists()) {
                if (!dest.mkdirs()) {
                    return false;
                }
            }

            File destFile = new File(dest, !TextUtils.isEmpty(name)
                    ? name + "." + getExtFromFilename(file.getName())
                    : file.getName());

            int n = 0;
            //重复的文件，最多复制32个
            while (destFile.exists() && n++ < 32) {
                String destName =
                        (!TextUtils.isEmpty(name)
                                ? name : getNameFromFilename(file.getName())) + "(" + n + ")" + "."
                                + getExtFromFilename(file.getName());
                destFile = new File(dest, destName);
            }

            if (!destFile.createNewFile()) {
                return false;
            }
            bos = new BufferedOutputStream(new FileOutputStream(destFile));
            bis = new BufferedInputStream(new FileInputStream(file));
            long hasReadLength = 0;
            while ((read = bis.read(data, 0, BUFFER)) != -1) {
                bos.write(data, 0, read);
                hasReadLength += read;
            }
            if (hasReadLength != file.length()) {
                deleteFile(file);
                return false;
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bis.close();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void saveToFile(@NonNull byte[] data, @NonNull File file) {
        FileOutputStream outputStream = null;
        try {
            if (!checkFileExistOrCreate(file)) {
                return;
            }

            outputStream = new FileOutputStream(file);
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveBitmapToJpegFile(Bitmap bitmap, File file) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Example path: /storage/emulated/0/.packageName
     */
    public static File getExternalPackageDir(Context context) {
        return new File(Environment.getExternalStorageDirectory(), '.' + context.getPackageName());
    }



}

