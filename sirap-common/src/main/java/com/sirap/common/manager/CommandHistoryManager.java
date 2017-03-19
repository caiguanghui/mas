package com.sirap.common.manager;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sirap.basic.component.Konstants;
import com.sirap.basic.search.MexFilter;
import com.sirap.basic.tool.C;
import com.sirap.basic.util.CollectionUtil;
import com.sirap.basic.util.DateUtil;
import com.sirap.basic.util.EmptyUtil;
import com.sirap.basic.util.FileUtil;
import com.sirap.basic.util.MexUtil;
import com.sirap.basic.util.StrUtil;
import com.sirap.common.domain.InputRecord;
import com.sirap.common.framework.SimpleKonfig;

public class CommandHistoryManager {
	
	private static CommandHistoryManager instance;
	
	private List<InputRecord> HISTORY_RECORDS = new ArrayList<InputRecord>();
	private List<InputRecord> CURRENT_RECORDS = new ArrayList<InputRecord>();
	private List<File> historyFileList = new ArrayList<File>();
	private String location;
	private String filePath;
	private boolean isEnabled;
	
	public static CommandHistoryManager g() {
		if(instance == null) {
			instance = new CommandHistoryManager();
		}
		
		return instance;
	}
	
	public void start() {
		isEnabled = SimpleKonfig.g().isHistoryEnabled();
		if(!isEnabled) {
			return;
		}
		
		String loginDatetime = DateUtil.timestamp();
    	String fileName = "C_" + loginDatetime + ".txt";
    	location = SimpleKonfig.g().pathWithSeparator("storage.history", Konstants.FOLDER_HISTORY);
    	loadHistoryFiles();
		filePath = location + fileName;
	}
	
	public void loadHistoryFiles() {
		File fodler = FileUtil.getIfNormalFolder(location);
		if(fodler == null) {
			C.pl("Uncanny, invalid input history location [" + location + "].");
			return;
		}
		String regexFilename = "C_.*?\\.txt";
		fodler.listFiles(new FileFilter() {
			public boolean accept(File file) {
				String name = file.getName();
				if(file.isFile() && StrUtil.isRegexMatched(regexFilename, name)) {
					historyFileList.add(file);
				}
				
				return true;
			}
		});
	}
	
	public void collect(String input) {
		String moment = DateUtil.displayNow(DateUtil.DATETIME);
		CURRENT_RECORDS.add(new InputRecord(moment, input));
		if(isEnabled && filePath != null) {
			MexUtil.saveAsNew(CURRENT_RECORDS, filePath);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<InputRecord> getNRecords(int latestCount) {
		if(latestCount <= 0) {
			return Collections.EMPTY_LIST;
		}
		
		List<InputRecord> allRecords = getAllRecords();
		List<InputRecord> records = CollectionUtil.last(allRecords, latestCount);
		
		return records;
	}
	
	public List<InputRecord> search(String keyWord) {
		List<InputRecord> records = getAllRecords();
		MexFilter<InputRecord> filter = new MexFilter<InputRecord>(keyWord, records);
		List<InputRecord> list = filter.process();
		Collections.sort(list);
		
		return list;
	}
	
	public List<InputRecord> getAllRecords() {
		List<InputRecord> historyRecords = new ArrayList<InputRecord>();
		if(EmptyUtil.isNullOrEmpty(HISTORY_RECORDS)) {
			for(File file: historyFileList) {
				List<InputRecord> records = MexUtil.readMexItemsViaExplicitClass(file.getAbsolutePath(), InputRecord.class);
				historyRecords.addAll(records);
			}
		} else {
			historyRecords.addAll(HISTORY_RECORDS);
		}
		
		List<InputRecord> items = new ArrayList<InputRecord>();
		items.addAll(historyRecords);
		items.addAll(CURRENT_RECORDS);
		
		return items;
	}
}
