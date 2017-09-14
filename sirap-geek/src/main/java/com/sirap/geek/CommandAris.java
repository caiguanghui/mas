package com.sirap.geek;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sirap.basic.util.IOUtil;
import com.sirap.basic.util.StrUtil;
import com.sirap.common.command.CommandBase;

public class CommandAris extends CommandBase {
	private static final String KEY_EXECUTE_JAVACODE = "ar";

	public boolean handle() {
		
		singleParam = parseParam(KEY_EXECUTE_JAVACODE + "\\s+(.+)");
		if(singleParam != null) {
			File file = parseFile(singleParam);
			List<String> javacodes = new ArrayList<>();
			if(file != null ) {
				javacodes = IOUtil.readFileIntoList(file.getAbsolutePath(), g().getCharsetInUse());
			} else {
				String output = StrUtil.findFirstMatchedItem("^=(.+)", singleParam);
				if(output != null) {
					javacodes.add("\t\t" + StrUtil.occupy("System.out.println({0});", output));
				} else {
					javacodes.add("\t\t" + singleParam + ";");
				}
			}
				
			boolean keepAlive = g().isYes("aris.keep");
			List<String> cpItems = g().getUserValuesByKeyword("aris.path.");
			String classpath = StrUtil.connect(cpItems, ";");
			
			export(ArisExecutor.g.execute(javacodes, classpath, keepAlive));
			
			return true;
		}
		
		return false;
	}
}
