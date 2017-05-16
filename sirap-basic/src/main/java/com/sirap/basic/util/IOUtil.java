package com.sirap.basic.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import com.sirap.basic.component.Konstants;
import com.sirap.basic.component.MexedMap;
import com.sirap.basic.domain.MexedFile;
import com.sirap.basic.domain.MexedObject;
import com.sirap.basic.output.ExcelParams;
import com.sirap.basic.output.PDFParams;
import com.sirap.basic.thirdparty.excel.ExcelHelper;
import com.sirap.basic.thirdparty.pdf.PdfHelper;
import com.sirap.basic.thread.Master;
import com.sirap.basic.thread.MasterGeneralItemOriented;
import com.sirap.basic.thread.business.InternetFileFetcher;
import com.sirap.basic.thread.business.NormalFileMover;
import com.sirap.basic.tool.C;
import com.sirap.basic.tool.CaptchaGenerator;
import com.sirap.basic.tool.D;
import com.sirap.basic.tool.MexedAudioPlayer;
import com.sirap.basic.tool.ScreenCaptor;

public class IOUtil {
	
	public static String readURL(String address) {
		return readURL(address, Konstants.CODE_UTF8);
	}
	
	public static String readURL(String address, String charset) {
		return readURL(address, charset, true);
	}

	public static String readURL(String address, String charset, boolean printException) {
		WebReader xiu = new WebReader(address, charset, true);
		return xiu.readIntoString();
	}

	public static List<String> readURLIntoList(String address, String charset, boolean printException) {
		WebReader xiu = new WebReader(address, charset, true);
		return xiu.readIntoList();
	}
	
	public static List<String> readResourceIntoList(String filePath) {
		InputStream inputStream = InputStream.class.getResourceAsStream(filePath);
		if(inputStream == null) {
			return null;
		}
		
		return readStreamIntoList(inputStream);
	}
	
	public static boolean isSourceExist(String filePath) {
		InputStream inputStream = InputStream.class.getResourceAsStream(filePath);
		if(inputStream == null) {
			return false;
		}
		
		return true;
	}
	
	public static List<String> readStreamIntoList(InputStream inputStream) {
		return readStreamIntoList(inputStream, false, "");
	}
	
	public static List<String> readStreamIntoList(InputStream inputStream, boolean printAlong) {
		return readStreamIntoList(inputStream, printAlong, "");
	}

	public static List<String> readStreamIntoList(InputStream inputStream, boolean printAlong, String prefix) {
		List<String> list = new ArrayList<String>();
		
		try {
			InputStreamReader isr = new InputStreamReader(inputStream);
			BufferedReader br = new BufferedReader(isr);
			String record;
			while ((record = br.readLine()) != null) {
				if(printAlong) {
					C.pl(record);
				}
				list.add(prefix + record);
			}

			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return list;
	}

	public static String readFileWithoutLineSeparator(String fileName) {
		return readFileWithLineSeparator(fileName, "");
	}

	public static String readFileWithRegularLineSeparator(String fileName) {
		return readFileWithLineSeparator(fileName, Konstants.NEWLINE);
	}
	
	public static String readFileWithLineSeparator(String fileName, String lineSeperator) {
		return readFileWithLineSeparator(fileName, lineSeperator, new String[0]);
	}

	/***
	 * 
	 * @param fileName
	 * @param lineSeperator
	 * @param prefixesToIgnoreOneline
	 * @return
	 */
	public static String readFileWithLineSeparator(String fileName, String lineSeperator, String... prefixesToIgnoreOneline) {
		StringBuffer sb = new StringBuffer();

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String record = br.readLine();
			boolean theFirstOne = true;
			while (record != null) {
				if(!theFirstOne) {
					sb.append(lineSeperator);
				}
				
				boolean toIgnore = StrUtil.startsWith(record.trim(), prefixesToIgnoreOneline);
				if(!toIgnore) {
					sb.append(record);
					theFirstOne = false;
				}
				
				record = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			C.pl(e);
		}

		return sb.toString();
	}
	
	public static List<String> readFileIntoList(String fileName) {
		return readFileIntoList(fileName, null);
	}
	
	public static List<String> readFileIntoList(String fileName, String charset) {
		List<String> list = new ArrayList<String>();
		try {
			InputStreamReader isr = null;
			if(charset != null) {
				isr = new InputStreamReader(new FileInputStream(fileName), charset);
			} else {
				isr = new InputStreamReader(new FileInputStream(fileName));
			}
			BufferedReader br = new BufferedReader(isr);
			String record = br.readLine();
			while (record != null) {
				list.add(record);
				record = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}
	public static int totalLines(String fileName) {
		int count = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String record = br.readLine();
			while (record != null) {
				count++;
				record = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}
	
	public static MexedMap createMexedMapByRegularFile(String filePath) {
		try {
			InputStream inputStream = new FileInputStream(filePath);
			return readKeyValuesIntoMexedMap(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static MexedMap createMexedMapByResourceFile(String filePath) {
		InputStream inputStream = InputStream.class.getResourceAsStream(filePath);
		
		return readKeyValuesIntoMexedMap(inputStream);
	}
	
	public static MexedMap createMexedMapByProperties(Properties props) {
		XXXUtil.nullCheck(props, "Properties props");
		
		MexedMap map = new MexedMap();
		Enumeration<?> en = props.propertyNames();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			String value = props.getProperty(key).trim();
			map.put(key.toLowerCase(), value);
		}

		return map;
	}
	
	public static Properties readProperties(String filePath) {
		Properties props = new Properties();
		
		try {
			InputStream inputStream = new FileInputStream(filePath);
			props = readProperties(inputStream);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return props;
	}
	
	public static Properties readProperties(InputStream inputStream) {
		Properties props = new Properties();
		
		try {
			props.load(inputStream);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return props;
	}
	
	public static MexedMap readKeyValuesIntoMexedMap(InputStream stream) {
		XXXUtil.nullCheck(stream, "InputStream stream");
		MexedMap map = new MexedMap();
		try {
			BufferedReader reader =new BufferedReader(new InputStreamReader(stream, "gbk"));
			String record;
			while ((record = reader.readLine()) != null) {
				String temp = record.trim();
				
				boolean toIgnore = StrUtil.startsWith(temp, "#");
				if(toIgnore) {
					continue;
				}
				
				String regex = "([^=]+)=(.+)";
				String[] params = StrUtil.parseParams(regex, temp);
				if(params == null) {
					continue;
				}
				
				String key = params[0].trim();
				String value = params[1].trim();
				
				if(key.isEmpty() || value.isEmpty()) {
					continue;
				}
				
				map.put(key, value);
			}
			
			return map;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
	
	public static MexedMap readPropertiesIntoMexedMap(InputStream stream) {
		XXXUtil.nullCheck(stream, "InputStream stream");
		
		try {
			BufferedReader reader =new BufferedReader(new InputStreamReader(stream, "gbk"));
			Properties props = new Properties();
			props.load(reader);
			return createMexedMapByProperties(props);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
	
	public static String takeConsecutivePhotos(String fileNamePrefix, String soundSource, String format, int delay, int count, boolean isEntireScreen) {
		countDown(delay);
		if(delay > 0) {
			C.pl();
		}
		String lastOne = null;
		String temp = fileNamePrefix + "_{1}.{2}";
		for(int i = 0; i < count; i++) {
			if(i != 0) {
				ThreadUtil.sleepInSeconds(1);
			}
			int index = i + 1;
			String indexStr = StrUtil.extendLeftward(index + "", (count + "").length(), "0");
			String imgFileName = StrUtil.occupy(temp, DateUtil.timestamp(), indexStr, format);
			ScreenCaptor fang = new ScreenCaptor(imgFileName, format, isEntireScreen);
	    	if(!EmptyUtil.isNullOrEmptyOrBlank(soundSource)) {
	        	IOUtil.playSound(soundSource);
	    	}
	    	
			String filePath = fang.capture();
			C.pl(index + "/" + count +" => " + filePath);
			if(i == count - 1) {
				lastOne = filePath;
			}
		}
		
		return lastOne;
	}

	public static String takePhoto(String fileNamePrefix, String soundSource, String format, int delay, boolean isEntireScreen) {
		countDown(delay);
		String fileName = StrUtil.occupy(fileNamePrefix, DateUtil.timestamp()) + "." + format;
		ScreenCaptor cam = new ScreenCaptor(fileName, format, isEntireScreen);
    	if(!EmptyUtil.isNullOrEmptyOrBlank(soundSource)) {
        	IOUtil.playSound(soundSource);
    	}
    	
		String filePath = cam.capture();
		
		return filePath;
	}

	public static String[] generateCaptcha(int numberOfChars, String storage) {
		String text = RandomUtil.letters(4);
    	String filePath = storage + DateUtil.timestamp() + "_captcha.jpeg";
		CaptchaGenerator james = new CaptchaGenerator(text);
		boolean flag = james.writeImageTo(filePath);
		if(flag) {
			return new String[]{text, filePath};
		} else {
			return null;
		}
	}

	public static void playSound(String soundSource) {
		MexedAudioPlayer andy = new MexedAudioPlayer(soundSource);
		
    	andy.play();
	}
	
	private static void setUserAgent(URLConnection urlConn) {
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1WOW64rv:25.0) Gecko/20100101 Firefox/25.0");
	}
	
	public static boolean downloadNormalFile(String address, String filePath) {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		
		boolean flag = true;
		try {
			URLConnection urlConn = new URL(address).openConnection();
			setUserAgent(urlConn);
			urlConn.setConnectTimeout(9000);
			urlConn.setReadTimeout(9000);
			inBuff = new BufferedInputStream(urlConn.getInputStream());
			outBuff = new BufferedOutputStream(new FileOutputStream(filePath));

			int t;
			while ((t = inBuff.read()) != -1) {
				outBuff.write(t);
			}
		} catch (Exception ex) {
			C.pl(ex + "\n\turl=>" + address + "\n\tLocation=>" + IOUtil.class.getName() + ".downloadNormalFile");
			File invalidFile = new File(filePath);
			if(invalidFile.exists()) {
				invalidFile.delete();
			}
			
			flag = false;
		} finally {
        	try {
                if (inBuff != null)
                	inBuff.close();
                if (outBuff != null)
                    outBuff.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		return flag;
	}
	
	public static boolean append(Object content, String fullFileName) {
		List<Object> objList = new ArrayList<>();
		objList.add(content);
		
		return append(objList, fullFileName);
	}
	
	public static boolean append(List<Object> objList, String fullFileName) {
		BufferedWriter thomas = null;
		try {
			thomas = new BufferedWriter(new FileWriter(fullFileName, true));
			
			for(Object member: objList) {
				thomas.write(member.toString());
				thomas.newLine();
			}
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(thomas != null) thomas.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean saveAsTxtWithHeaderAndTotal(List objList, String fullFileName) {
		return saveAsTxt(objList, fullFileName, true, true);
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean saveAsTxtWithHeaderOnly(List objList, String fullFileName) {
		return saveAsTxt(objList, fullFileName, true, false);
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean saveAsTxt(List objList, String fullFileName) {
		return saveAsTxt(objList, fullFileName, false, false);
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean saveAsTxt(List objList, String fullFileName, boolean printSimpleHeader, boolean printTotal) {
		if(objList == null) {
			objList = Collections.EMPTY_LIST;
		}
		
		List<String> header = new ArrayList<String>();
		if(printSimpleHeader) {
			header.add("#Date: " + new Date() + ", KY");
		}
		
		List<String> footer = new ArrayList<String>();
		if(printTotal) {
			footer.add("#Records:" + objList.size());
		}
		
		return saveAsTxt(objList, fullFileName, header, footer);
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean saveAsTxt(List objList, String fullFileName, List<String> header, List<String> footer) {
		XXXUtil.nullCheck(objList, "List objList");
		
		BufferedWriter thomas = null;
		try {

			thomas = new BufferedWriter(new FileWriter(fullFileName));
			if(!EmptyUtil.isNullOrEmpty(header)) {
				for(String record:header) {
					thomas.write(record);
					thomas.newLine();
				}
			}
			
			for(Object member: objList) {
				thomas.write(member + "");
				thomas.newLine();
			}
			
			if(!EmptyUtil.isNullOrEmpty(footer)) {
				for(String record:footer) {
					thomas.write(record);
					thomas.newLine();
				}
			}
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(thomas != null) thomas.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}

	public static Object readObject(String fileName) {
		Object obj = null;
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
			ois = new ObjectInputStream(fis);
			obj = ois.readObject();
		} catch (Exception e) {
			D.debug(IOUtil.class, "readObject", fileName);
			e.printStackTrace();
		} finally {
			try {
				if(fis != null) {
					fis.close();
				}
				
				if(ois != null) {
					ois.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return obj;
	}

	public static boolean saveObject(Serializable obj, String fileName) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
			oos.writeObject(obj);
			oos.flush();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * @See MexUtil.copyFiles(List<File>, String), which yields better performance 
	 * @param sourceFiles
	 * @param targetFolder
	 * @return
	 */
	public static int[] copyFiles(List<File> sourceFiles, String targetFolder)  {
		int[] results = new int[2];
		for(File file:sourceFiles) {
			boolean flag = copyFile(file, targetFolder);
			if(flag) {
				results[0]++;
			} else {
				results[1]++;
			}
		}
		
		return results;
	}
	
	public static boolean copyFile(String sourceFilePath, String targetFolder)  {
		return copyFile(new File(sourceFilePath), targetFolder);
	}
	
	public static boolean isInTheFolder(File file, String folder) {
		boolean flag = file.getParentFile().equals(new File(folder));
		
		return flag;
	}
	
	public static boolean copyFile(File sourceFile, String targetFolder)  {
    	if(sourceFile == null ||targetFolder == null) {
    		return false;
    	}
    	
    	if(isInTheFolder(sourceFile, targetFolder)) {
    		return true;
    	}

    	BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
        	String fileName = sourceFile.getName();
        	FileUtil.makeDirectoriesIfNonExist(targetFolder);
        	String targetPath = targetFolder + File.separator + fileName; 
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
            outBuff = new BufferedOutputStream(new FileOutputStream(targetPath));

            byte[] b = new byte[Konstants.FILE_SIZE_STEP * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            outBuff.flush();
            
            return true;
        } catch (Exception ex) {
        	ex.printStackTrace();        	
        } finally {
        	try {
                if (inBuff != null)
                    inBuff.close();
                if (outBuff != null)
                    outBuff.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        return false;
    }
	

	
	public static int[] copyMexedFiles(List<MexedFile> sourcefiles, String targetFolder) {
		return copyMexedFiles(sourcefiles, targetFolder, -1);
	}
	
	public static int[] copyMexedFiles(List<MexedFile> sourcefiles, String targetFolder, final int threads) {
		Master<MexedFile> george = new Master<MexedFile>(sourcefiles, new NormalFileMover(targetFolder)) {
			@Override
			protected int countOfThread() {
				int temp = threads <= 0 ? super.countOfThread() : threads;
				return temp;
			}
		};
		george.sitAndWait();
		
		return null;
	}
	
	public static List<String> downloadFiles(String storage, List<MexedObject> links, String suffixWhenObscure, final int threads) {
		FileUtil.makeDirectoriesIfNonExist(storage);
		
		MasterGeneralItemOriented<MexedObject> master = new MasterGeneralItemOriented<MexedObject>(links, new InternetFileFetcher(storage, suffixWhenObscure)){
			@Override
			protected int countOfThread() {
				int temp = threads <= 0 ? super.countOfThread() : threads;
				return temp;
			}
		};

		master.sitAndWait();
		return master.getValidStringResults();
	}
	
	public static boolean saveAsPDF(List<Object> objList, String fullFileName, PDFParams params) {
		return PdfHelper.export(objList, fullFileName, params);
	}
	
	public static boolean saveAsExcel(List<Object> objList, String fullFileName, ExcelParams params) {
		return ExcelHelper.export(objList, fullFileName, params);
	}
	
	private static void countDown(int seconds) {
		for(int i = 0; i < seconds; i++) {
			int left = seconds - i;
			String display = left + (left > 1 ? " " : "");
			C.pr(display);
			
			ThreadUtil.sleepInSeconds(1);
		}
	}
	
	public static List<String> echoPath() {
		String path = System.getProperty("java.library.path");
		char delimiter = ';';
		if(PanaceaBox.isMac()) {
			delimiter = ':';
		}
		List<String> items = StrUtil.split(path, delimiter);

		return items;
	}
}
