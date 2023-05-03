package com.github.tvbox.osc.wxwz.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * 
 * @author zhangliangming
 * 
 */
public class FileUtils {
	public static final int SIZETYPE_B = 1;//获取文件大小单位为B的double值
	public static final int SIZETYPE_KB = 2;//获取文件大小单位为KB的double值
	public static final int SIZETYPE_MB = 3;//获取文件大小单位为MB的double值
	public static final int SIZETYPE_GB = 4;//获取文件大小单位为GB的double值
	public static String getFileExt(File file) {
		return getFileExt(file.getName());
	}

	public static String removeExt(String s) {
		if (s==null||s.isEmpty()){
			return "";
		}
		int index = s.lastIndexOf(".");
		if (index == -1)
			index = s.length();
		return s.substring(0, index);
	}

	public static String getFileName(String url) {
		int pos = url.lastIndexOf("/");
		if (pos == -1)
			return "";
		return url.substring(pos + 1).toLowerCase();
	}
	public static String getFileExt(String fileName) {
		int pos = fileName.lastIndexOf(".");
		if (pos == -1)
			return "";
		return fileName.substring(pos + 1).toLowerCase();
	}

	/**
	 * 计算文件的大小，返回相关的m字符串
	 * 
	 * @param fileS
	 * @return
	 */
	public static String getFileSize(long fileS) {// 转换文件大小
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 获取指定文件大小
	 * @param
	 * @return
	 * @throws Exception
	 */
	public static long getFileSize(File file) throws Exception
	{
		long size = 0;
		if (file.exists()){
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			size = fis.available();
		}
		else{
			file.createNewFile();
			Log.e("获取文件大小","文件不存在!");
		}
		return size;
	}

	/**
	 * 获取指定文件夹
	 * @param f
	 * @return
	 * @throws Exception
	 */
	public static long getFileSizes(File f) throws Exception
	{
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++){
			if (flist[i].isDirectory()){
				size = size + getFileSizes(flist[i]);
			}
			else{
				size =size + getFileSize(flist[i]);
			}
		}
		return size;
	}

	public static boolean isFileExists(String path,String oldpath){
		String root = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file =null;
		if (oldpath.equals("")){
			file = new File(root + "/tvbox/.cache/Music");
		}else {
			file = new File(root + oldpath);
		}

		if (!file.exists())
			file.mkdirs();
		File filename = new File(file, path);
		if (filename.exists()) {
			return true;
		}else {
			return false;
		}
	}

	public static String getMusicCacheFile(String path,String rootFile){
		String root = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file;
		if (rootFile.equals("")){
			file = new File(root + "/tvbox/.cache/Music");
		}else {
			file = new File(root + rootFile);
		}

		if (!file.exists())
			file.mkdirs();
		File filename = new File(file, path);
		return filename.getAbsolutePath();
	}



	//获取指定目录的权限
	public static void requestAccessAndroidData(Activity activity){
		try {
			Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata");
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
			//flag看实际业务需要可再补充
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
					| Intent.FLAG_GRANT_WRITE_URI_PERMISSION
					| Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
			activity.startActivityForResult(intent, 6666);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isGrantAndroidData(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			for (UriPermission persistedUriPermission : context.getContentResolver().getPersistedUriPermissions()) {
				if (persistedUriPermission.getUri().toString().
						equals("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")) {
					return true;
				}
			}
		}
		return false;
	}



	/**
		 * 根据Uri获取图片的绝对路径
		 *
		 * @param context 上下文对象
		 * @param uri     图片的Uri
		 * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
		 */
		public static String getRealPathFromUri(Context context, Uri uri) {
			int sdkVersion = Build.VERSION.SDK_INT;
			if (sdkVersion >= 19) { // api >= 19
				return getRealPathFromUriAboveApi19(context, uri);
			} else { // api < 19
				return getRealPathFromUriBelowAPI19(context, uri);
			}
		}

		/**
		 * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
		 *
		 * @param context 上下文对象
		 * @param uri     图片的Uri
		 * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
		 */
		public static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
			return getDataColumn(context, uri, null, null);
		}

		/**
		 * 适配api19及以上,根据uri获取图片的绝对路径
		 *
		 * @param context 上下文对象
		 * @param uri     图片的Uri
		 * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
		 */
		@SuppressLint("NewApi")
		public static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
			String filePath = null;
			if (DocumentsContract.isDocumentUri(context, uri)) {
				// 如果是document类型的 uri, 则通过document id来进行处理
				String documentId = DocumentsContract.getDocumentId(uri);
				if (isMediaDocument(uri)) { // MediaProvider
					// 使用':'分割
					String id = documentId.split(":")[1];

					String selection = MediaStore.Images.Media._ID + "=?";
					String[] selectionArgs = {id};
					filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
				} else if (isDownloadsDocument(uri)) { // DownloadsProvider
					Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
					filePath = getDataColumn(context, contentUri, null, null);
				}
			} else if ("content".equalsIgnoreCase(uri.getScheme())) {
				// 如果是 content 类型的 Uri
				filePath = getDataColumn(context, uri, null, null);
			} else if ("file".equals(uri.getScheme())) {
				// 如果是 file 类型的 Uri,直接获取图片对应的路径
				filePath = uri.getPath();
			}
			return filePath;
		}

		/**
		 * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
		 *
		 * @return
		 */
		public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
			String path = null;

			String[] projection = new String[]{MediaStore.Images.Media.DATA};
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
				if (cursor != null && cursor.moveToFirst()) {
					int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
					path = cursor.getString(columnIndex);
				}
			} catch (Exception e) {
				if (cursor != null) {
					cursor.close();
				}
			}
			return path;
		}

		/**
		 * @param uri the Uri to check
		 * @return Whether the Uri authority is MediaProvider
		 */
		public static boolean isMediaDocument(Uri uri) {
			return "com.android.providers.media.documents".equals(uri.getAuthority());
		}

		/**
		 * @param uri the Uri to check
		 * @return Whether the Uri authority is DownloadsProvider
		 */
		public static boolean isDownloadsDocument(Uri uri) {
			return "com.android.providers.downloads.documents".equals(uri.getAuthority());
		}

	/** 删除文件，可以是文件或文件夹
	 * @param delFile 要删除的文件夹或文件名
	 * @return 删除成功返回true，否则返回false
	 */
	public static boolean delete(String delFile) {
		File file = new File(delFile);
		if (!file.exists()) {
//            Toast.makeText(HnUiUtils.getContext(), "删除文件失败:" + delFile + "不存在！", Toast.LENGTH_SHORT).show();
			Log.d("delteFile","删除文件失败:" + delFile + "不存在！");
			return false;
		} else {
			if (file.isFile())
				return deleteSingleFile(delFile);
			else
				return deleteDirectory(delFile);
		}
	}

	/**
	 * 在SD卡上创建目录
	 *
	 * @param dirName
	 */
	public File creatDir(String dirName) {
		File dir = new File(dirName);
		dir.mkdir();

		return dir;
	}

	/**
	 * 在SD卡上创建文件
	 *
	 * @throws IOException
	 */
	public File creatFile(String fileName) throws IOException {
		File file = new File(fileName);
		file.createNewFile();
		return file;
	}

	/** 删除单个文件
	 * @param filePath$Name 要删除的文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteSingleFile(String filePath$Name) {
		File file = new File(filePath$Name);
		// 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
		if (file.exists() && file.isFile()) {
			if (file.delete()) {
				Log.d("delteFile","Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
				return true;
			} else {
				Log.d("delteFile","删除单个文件" + filePath$Name + "失败！");
				return false;
			}
		} else {
			Log.d("delteFile","删除单个文件失败：" + filePath$Name + "不存在！");
			return false;
		}
	}

	/** 删除目录及目录下的文件
	 * @param filePath 要删除的目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String filePath) {
		// 如果dir不以文件分隔符结尾，自动添加文件分隔符
		if (!filePath.endsWith(File.separator))
			filePath = filePath + File.separator;
		File dirFile = new File(filePath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
			Log.d("delteFile","删除目录失败：" + filePath + "不存在！");
			return false;
		}
		boolean flag = true;
		// 删除文件夹中的所有文件包括子目录
		File[] files = dirFile.listFiles();
		for (File file : files) {
			// 删除子文件
			if (file.isFile()) {
				flag = deleteSingleFile(file.getAbsolutePath());
				if (!flag)
					break;
			}
			// 删除子目录
			else if (file.isDirectory()) {
				flag = deleteDirectory(file
						.getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag) {
			Log.d("delteFile","删除目录失败！");
			return false;
		}
		// 删除当前目录
		if (dirFile.delete()) {
			Log.d("delteFile", "Copy_Delete.deleteDirectory: 删除目录" + filePath + "成功！");
			return true;
		} else {
			Log.d("delteFile","删除目录：" + filePath + "失败！");
			return false;
		}
	}

	/**
	 * 拷贝一个文件,srcFile源文件，destFile目标文件
	 *
	 * @param //path
	 * @throws IOException
	 */
	public static boolean copyFileTo(File srcFile, File destFile) throws IOException {
		if (srcFile.isDirectory() || destFile.isDirectory())
			return false;// 判断是否是文件
		FileInputStream fis = new FileInputStream(srcFile);
		FileOutputStream fos = new FileOutputStream(destFile);
		int readLen = 0;
		byte[] buf = new byte[1024];
		while ((readLen = fis.read(buf)) != -1) {
			fos.write(buf, 0, readLen);
		}
		fos.flush();
		fos.close();
		fis.close();
		return true;
	}

	/**
	 * 拷贝目录下的所有文件到指定目录
	 *
	 * @param srcDir
	 * @param destDir
	 * @return
	 * @throws IOException
	 */
	public boolean copyFilesTo(File srcDir, File destDir) throws IOException {
		if (!srcDir.isDirectory() || !destDir.isDirectory())
			return false;// 判断是否是目录
		if (!destDir.exists())
			return false;// 判断目标目录是否存在
		File[] srcFiles = srcDir.listFiles();
		for (int i = 0; i < srcFiles.length; i++) {
			if (srcFiles[i].isFile()) {
// 获得目标文件
				File destFile = new File(destDir.getPath() + "//"
						+ srcFiles[i].getName());
				copyFileTo(srcFiles[i], destFile);
			} else if (srcFiles[i].isDirectory()) {
				File theDestDir = new File(destDir.getPath() + "//"
						+ srcFiles[i].getName());
				copyFilesTo(srcFiles[i], theDestDir);
			}
		}
		return true;
	}

	/**
	 * 移动一个文件
	 *
	 * @param srcFile
	 * @param destFile
	 * @return
	 * @throws IOException
	 */
	public static boolean moveFileTo(File srcFile, File destFile) throws IOException {
		boolean iscopy = copyFileTo(srcFile, destFile);
		if (!iscopy)
			return false;
		delFile(srcFile);
		return true;
	}

	/**
	 * 移动目录下的所有文件到指定目录
	 *
	 * @param srcDir
	 * @param destDir
	 * @return
	 * @throws IOException
	 */
	public boolean moveFilesTo(File srcDir, File destDir) throws IOException {
		if (!srcDir.isDirectory() || !destDir.isDirectory()) {
			return false;
		}
		File[] srcDirFiles = srcDir.listFiles();
		for (int i = 0; i < srcDirFiles.length; i++) {
			if (srcDirFiles[i].isFile()) {
				File oneDestFile = new File(destDir.getPath() + "//"
						+ srcDirFiles[i].getName());
				moveFileTo(srcDirFiles[i], oneDestFile);
				delFile(srcDirFiles[i]);
			} else if (srcDirFiles[i].isDirectory()) {
				File oneDestFile = new File(destDir.getPath() + "//"
						+ srcDirFiles[i].getName());
				moveFilesTo(srcDirFiles[i], oneDestFile);
				delDir(srcDirFiles[i]);
			}
		}
		return true;
	}

	/**
	 * 删除一个文件
	 *
	 * @param file
	 * @return
	 */
	public static boolean delFile(File file) {
		if (file.isDirectory())
			return false;
		return file.delete();
	}

	/**
	 * 删除一个目录（可以是非空目录）
	 *
	 * @param dir
	 */
	public static boolean delDir(File dir) {
		if (dir == null || !dir.exists() || dir.isFile()) {
			return false;
		}
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				delDir(file);// 递归
			}
		}
		dir.delete();
		return true;
	}

	/**
	 * 转换文件大小
	 * @param fileS
	 * @return
	 */
	public static String FormetFileSize(long fileS)
	{
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		String wrongSize="0B";
		if(fileS==0){
			return wrongSize;
		}
		if (fileS < 1024){
			fileSizeString = df.format((double) fileS) + "B";
		}
		else if (fileS < 1048576){
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		}
		else if (fileS < 1073741824){
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		}
		else{
			fileSizeString = df.format((double) fileS / 1073741824) + "GB";
		}
		return fileSizeString;
	}
	/**
	 * 转换文件大小,指定转换的类型
	 * @param fileS
	 * @param sizeType
	 * @return
	 */
	public static double FormetFileSize(long fileS,int sizeType)
	{
		DecimalFormat df = new DecimalFormat("#.00");
		double fileSizeLong = 0;
		switch (sizeType) {
			case SIZETYPE_B:
				fileSizeLong=Double.valueOf(df.format((double) fileS));
				break;
			case SIZETYPE_KB:
				fileSizeLong=Double.valueOf(df.format((double) fileS / 1024));
				break;
			case SIZETYPE_MB:
				fileSizeLong=Double.valueOf(df.format((double) fileS / 1048576));
				break;
			case SIZETYPE_GB:
				fileSizeLong=Double.valueOf(df.format((double) fileS / 1073741824));
				break;
			default:
				break;
		}
		return fileSizeLong;
	}


}
