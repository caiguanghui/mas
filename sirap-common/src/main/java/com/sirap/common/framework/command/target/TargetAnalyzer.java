package com.sirap.common.framework.command.target;

import java.io.File;
import java.util.List;

import com.sirap.basic.component.Konstants;
import com.sirap.basic.email.EmailCenter;
import com.sirap.basic.tool.C;
import com.sirap.basic.util.EmptyUtil;
import com.sirap.basic.util.FileUtil;
import com.sirap.basic.util.StrUtil;

public abstract class TargetAnalyzer {

	public static final String KEY_CREATE_FOLDER = "=";
	public static final String KEY_TO_EXPORT_FOLDER = ".";
	public static final String KEY_EXPORT_INFO_TO_TEXTFILE = "(\\*|)(.*?)";
	public static final String KEY_EXPORT_INFO_TO_PDF = ".*\\.pdf$";
	public static final String KEY_EXPORT_INFO_TO_HTML = ".*\\.htm$";
	public static final String KEY_EXPORT_INFO_TO_EXCEL = ".*\\.xls$";
	public static final String KEY_EXPORT_FILE_TO_FOLDER = "\\$(.*?)";
	
	public Target parse(String command, String target) {
		return parse(command, target, false);
	}
	
	public Target parse(String command, String targetStr, boolean isEmailEnabled) {
		
		if(EmptyUtil.isNullOrEmpty(targetStr)) {
			return new TargetConsole();
		}
		
		Target target = null;
		
		boolean isEmailCase = targetStr.indexOf('@') >= 0;
		if(isEmailCase) {
			if(!isEmailEnabled) {
				C.pl2("Email currently disabled.");
				return null;
			}
			
			return createTargetEmail(targetStr, command);
		}
		
		String singleParam = StrUtil.parseParam(KEY_EXPORT_FILE_TO_FOLDER, targetStr);
		if(singleParam != null) {
			target = createTargetFolder(singleParam, command);
			target.setFileRelated(true);
			
			return target;
		}
		
		if(StrUtil.isRegexMatched(KEY_EXPORT_INFO_TO_PDF, targetStr)) {
			String destInfo = targetStr.trim();
			target = createTargetPDF(destInfo, command);

			return target;
		}
		
		if(StrUtil.isRegexMatched(KEY_EXPORT_INFO_TO_HTML, targetStr)) {
			String destInfo = targetStr.trim();
			target = createTargetHtml(destInfo, command);

			return target;
		}
		
		if(StrUtil.isRegexMatched(KEY_EXPORT_INFO_TO_EXCEL, targetStr)) {
			String destInfo = targetStr.trim();
			target = createTargetExcel(destInfo, command);

			return target;
		}
		
		String[] params = StrUtil.parseParams(KEY_EXPORT_INFO_TO_TEXTFILE, targetStr);
		if(params != null) {
			boolean isSirap = !params[0].isEmpty();
			String destInfo = params[1];
			TargetTxtFile txtFile = createTargetTxtFile(destInfo, command);
			if(isSirap) {
				String fileName = txtFile.getFileName().replaceAll(Konstants.SUFFIX_TXT + "$", Konstants.SUFFIX_SIRAP);
				txtFile.setFileName(fileName);
			}
			
			return txtFile;
		}
		
		return target; 
	}
	
	private Target createTargetFolder(String destInfo, String command) {
		String[] params = StrUtil.parseParams("(.*?)(=|)", destInfo);
		String dest = params[0];
		boolean toCreateFolder = !params[1].isEmpty();
		String newFolderName = "";
		if(toCreateFolder) {
			newFolderName = generateFileOrFolderName(command) + File.separator;
		}
		
		if(dest.isEmpty()) {
			return new TargetFolder(getDefaultExportFolder() + newFolderName);			
		}
		
		String folderPath = parseRealFolderPath(dest);
		if(folderPath != null) {
			return new TargetFolder(folderPath + newFolderName);	
		} else {
			newFolderName = generateFileOrFolderName(dest) + File.separator;
			return new TargetFolder(getDefaultExportFolder() + newFolderName);
		}
	}
	
	private TargetTxtFile createTargetTxtFile(String destInfo, String command) {
		String commandConvertedFileName = FileUtil.generateLegalFileName(command) + Konstants.SUFFIX_TXT;
		if(destInfo.isEmpty() || destInfo.equalsIgnoreCase(KEY_TO_EXPORT_FOLDER)) {
			return new TargetTxtFile(getDefaultExportFolder(), commandConvertedFileName);			
		}
		
		String folderPath = parseRealFolderPath(destInfo);
		if(folderPath != null) {
			return new TargetTxtFile(folderPath, commandConvertedFileName);
		}
		
		String[] folderAndFile = FileUtil.splitFolderAndFile(destInfo);
		if(!EmptyUtil.isNullOrEmptyOrBlank(folderAndFile[0])) {
			folderPath = parseRealFolderPath(folderAndFile[0]);
			if(folderPath != null) {
				String newFileName = generateFileOrFolderName(folderAndFile[1]);
				if(!newFileName.endsWith(Konstants.SUFFIX_TXT)) {
					newFileName += Konstants.SUFFIX_TXT;
				}
				
				return new TargetTxtFile(folderPath, newFileName);
			}
		}

		String newFileName = FileUtil.generateLegalFileName(destInfo);
		if(!newFileName.endsWith(Konstants.SUFFIX_TXT)) {
			newFileName += Konstants.SUFFIX_TXT;
		}
		
		return new TargetTxtFile(getDefaultExportFolder(), newFileName);
	}
	
	private TargetPDF createTargetPDF(String destInfo, String command) {
		String commandConvertedFileName = FileUtil.generateLegalFileName(command) + Konstants.SUFFIX_PDF;
		if(destInfo.equalsIgnoreCase(Konstants.SUFFIX_PDF)) {
			return new TargetPDF(getDefaultExportFolder(), commandConvertedFileName);			
		}
		
		String[] folderAndFile = FileUtil.splitFolderAndFile(destInfo);
		if(!EmptyUtil.isNullOrEmptyOrBlank(folderAndFile[0])) {
			String folderPath = parseRealFolderPath(folderAndFile[0]);
			if(folderPath != null) {
				String newFileName = folderAndFile[1];
				if(newFileName.equalsIgnoreCase(Konstants.SUFFIX_PDF)) {
					newFileName = commandConvertedFileName;
				}
			
				return new TargetPDF(folderPath, newFileName);
			}
		}

		String newFileName = FileUtil.generateLegalFileName(destInfo);
		
		return new TargetPDF(getDefaultExportFolder(), newFileName);
	}
	
	private TargetHtml createTargetHtml(String destInfo, String command) {
		String commandConvertedFileName = FileUtil.generateLegalFileName(command) + Konstants.SUFFIX_HTML;
		if(destInfo.equalsIgnoreCase(Konstants.SUFFIX_HTML)) {
			return new TargetHtml(getDefaultExportFolder(), commandConvertedFileName);			
		}
		
		String[] folderAndFile = FileUtil.splitFolderAndFile(destInfo);
		if(!EmptyUtil.isNullOrEmptyOrBlank(folderAndFile[0])) {
			String folderPath = parseRealFolderPath(folderAndFile[0]);
			if(folderPath != null) {
				String newFileName = folderAndFile[1];
				if(newFileName.equalsIgnoreCase(Konstants.SUFFIX_HTML)) {
					newFileName = commandConvertedFileName;
				}
			
				return new TargetHtml(folderPath, newFileName);
			}
		}

		String newFileName = FileUtil.generateLegalFileName(destInfo);
		
		return new TargetHtml(getDefaultExportFolder(), newFileName);
	}
	
	private TargetExcel createTargetExcel(String destInfo, String command) {
		String commandConvertedFileName = FileUtil.generateLegalFileName(command) + Konstants.SUFFIX_EXCEL;
		if(destInfo.equalsIgnoreCase(Konstants.SUFFIX_EXCEL)) {
			return new TargetExcel(getDefaultExportFolder(), commandConvertedFileName);			
		}
		
		String[] folderAndFile = FileUtil.splitFolderAndFile(destInfo);
		if(!EmptyUtil.isNullOrEmptyOrBlank(folderAndFile[0])) {
			String folderPath = parseRealFolderPath(folderAndFile[0]);
			if(folderPath != null) {
				String newFileName = folderAndFile[1];
				if(newFileName.equalsIgnoreCase(Konstants.SUFFIX_EXCEL)) {
					newFileName = commandConvertedFileName;
				}
			
				return new TargetExcel(folderPath, newFileName);
			}
		}

		String newFileName = FileUtil.generateLegalFileName(destInfo);
		
		return new TargetExcel(getDefaultExportFolder(), newFileName);
	}
	
	public static Target createTargetEmail(String targetStr, String command) {
		String[] params = StrUtil.parseParams("(\\$|)(.*?)", targetStr);
		boolean fileRelated = !params[0].isEmpty();
		String temp = params[1];
		String mixedAddresses = temp;
		String subject = command;
		int idxComma = temp.indexOf(",");
		if(idxComma > 0) {
			mixedAddresses = temp.substring(0, idxComma).trim();
			subject = temp.substring(idxComma + 1).trim();
		}
		
		List<String> toList = EmailCenter.parseLegalAddresses(mixedAddresses);
		if(!EmptyUtil.isNullOrEmpty(toList)) {
			Target email = new TargetEmail(subject, toList);
			email.setFileRelated(fileRelated);
			return email;
		}
		
		return null;
	}
	
	public static String generateFileOrFolderName(String source) {
		if(FileUtil.isMaliciousPath(source)) {
			return "SHIT";
		}
		
		return FileUtil.generateLegalFileName(source);
	}
	
	public abstract String getDefaultExportFolder();
	public abstract String parseRealFolderPath(String pseudoFolderpath);
}