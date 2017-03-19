package com.sirap.basic.search;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sirap.basic.domain.MexedObject;
import com.sirap.basic.domain.MexedTextSearchRecord;
import com.sirap.basic.util.FileUtil;
import com.sirap.basic.util.IOUtil;
import com.sirap.basic.util.StrUtil;

public class TextSearcher {
	
	private static Map<String, TextSearcher> instances = new HashMap<>();
	private List<MexedObject> allItems;
	
	private TextSearcher(List<String> folders, String[] suffixes, String criteria, boolean printSource) {
		allItems = readAllItems(folders, suffixes, printSource);
	}
	
	private synchronized static TextSearcher getInstance(String engineName, List<String> folders, String[] suffixes, String criteria, boolean printSource) {
		TextSearcher wang = instances.get(engineName);
		if(wang == null) {
			wang = new TextSearcher(folders, suffixes, criteria, printSource);
			instances.put(engineName, wang);
		}
		
		return wang;
	}
	
	public static List<MexedObject> search(String foldersStr, String suffixesStr, String criteria, boolean printSource) {
		List<String> folders = StrUtil.splitByRegex(foldersStr);
		String[] suffixes = suffixesStr.split("[;|,]");
		
		return search(folders, suffixes, criteria, printSource);
	}
	
	public static List<MexedObject> search(List<String> folders, String[] suffixes, String criteria, boolean printSource) {
		TextSearcher wang = new TextSearcher(folders, suffixes, criteria, printSource);
		List<MexedObject> result = wang.search(criteria);
		
		return result;
	}
	
	public static List<MexedObject> searchWithCache(String engineName, String foldersStr, String suffixesStr, String criteria, boolean printSource) {
		List<String> folders = StrUtil.splitByRegex(foldersStr);
		String[] suffixes = suffixesStr.split("[;|,]");
		
		return searchWithCache(engineName, folders, suffixes, criteria, printSource);
	}
	
	public static List<MexedObject> searchWithCache(String engineName, List<String> folders, String[] suffixes, String criteria, boolean printSource) {
		TextSearcher wang = getInstance(engineName, folders, suffixes, criteria, printSource);
		List<MexedObject> result = wang.search(criteria);
		
		return result;
	}
	
	private List<MexedObject> search(String criteria) {
		MexFilter<MexedObject> filter = new MexFilter<MexedObject>(criteria, allItems);
		List<MexedObject> result = filter.process();
		
		return result;
	}
	
	private List<MexedObject> readAllItems(List<String> folders, String[] suffixes, boolean printSource) {
		List<File> files = FileUtil.scanFolder(folders, 9, suffixes, false);
		List<MexedObject> allItems = new ArrayList<>();
		for(File file : files) {
			String fileName = file.getAbsolutePath();
			List<MexedObject> items = readEachFile(fileName, printSource);
			allItems.addAll(items);
		}
		
		return allItems;
	}
	
	@SuppressWarnings("unchecked")
	private List<MexedObject> readEachFile(String filename, boolean printSource) {
		if(!FileUtil.isNormalFile(filename)) {
			return Collections.EMPTY_LIST;
		}
		
		List<String> items = IOUtil.readFileIntoList(filename);
		
		String shortFilename = FileUtil.extractFilenameWithoutExtension(filename);
		List<MexedObject> mexItems = new ArrayList<>();
		for(int i = 0; i < items.size(); i++) {
			String item = items.get(i);
			MexedTextSearchRecord mo = new MexedTextSearchRecord(item.trim());
			if(printSource) {
				mo.setLineNumber(i + 1);
				mo.setPrintSource(true);
				mo.setShortFilename(shortFilename);
				mo.setFullFilename(filename);
			}
			
			mexItems.add(mo);
		}
		
		return mexItems;
	}
}
