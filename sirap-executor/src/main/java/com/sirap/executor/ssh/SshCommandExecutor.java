package com.sirap.executor.ssh;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.sirap.basic.component.Konstants;
import com.sirap.basic.exception.MexException;
import com.sirap.basic.tool.C;
import com.sirap.basic.util.EmptyUtil;
import com.sirap.basic.util.PanaceaBox;
import com.sirap.basic.util.StrUtil;
import com.sirap.basic.util.XCodeUtil;
import com.sirap.basic.util.XXXUtil;
import com.sirap.common.framework.SimpleKonfig;  
  
public class SshCommandExecutor {
	
	private SshConfigItem config;
	private Session session;
	private static SshCommandExecutor instance;
	
	public SshConfigItem getConfig() {
		return config;
	}

	public void closeSession() {
		closeSessionWithMsg(false);
	}
	
	public void closeSessionWithMsg(boolean showMsg) {
		if(session != null) {
			if(session.isConnected()) {
				session.disconnect();
				if(showMsg) {
					C.pl2("SSH session closed.");
				}
			} else {
				if(showMsg) {
					C.pl2("SSH session not yet connected.");
				}
			}
		} else {
			if(showMsg) {
				C.pl2("SSH session not yet created.");
			}
		}
	}

	public static SshCommandExecutor g() {
		if(instance == null) {
			String configInfo = SimpleKonfig.g().getUserValueOf("ssh.info");
			if(EmptyUtil.isNullOrEmpty(configInfo)) {
	    		XXXUtil.alert("No ssh config info by key [{0}]", "ssh.info");
	    	}
			SshConfigItem config = SshConfigItem.of(configInfo);
			XXXUtil.nullCheck(config, ":Not valid ssh config info: " + configInfo);
			SshCommandExecutor.create(config);
		}
		
		return instance;
	}
	
	public static SshCommandExecutor create(SshConfigItem config) {
		if(instance != null) {
			instance.closeSession();
		}
		
		instance = new SshCommandExecutor(config);
		return instance;
	}
	
	public SshCommandExecutor(SshConfigItem config) {
		this.config = config;
	}
  
    public List<String> execute(final String command) {
    	return execute(command, true);
    }
    
    public List<String> execute(final String command, boolean pretty) {
    	List<String> result = remoteExecute(command);
    	
    	List<String> items = new ArrayList<>();
        for (String item : result) { 
        	if(pretty) {
            	String temp = XCodeUtil.ofOctalUtf8Chars(item);
            	temp = temp.replace("\\ ", " ");
                items.add(temp);
        	} else {
            	items.add(item);
        	}
        }
        
        return items;
    }
    
    public String getMilliSecondsFrom1970(boolean pretty) {
    	String command = "date +%s%N";
    	List<String> items = execute(command, pretty);
    	if(items.size() != 1) {
    		XXXUtil.alert("Request [{0}] generates {1}", command, items);
    	}
    	
    	String result = items.get(0).replaceAll("\\d{6}$", "");
        return result; 
    }
    
    public Session getSession() {
        JSch jsch = new JSch();  
        MyUserInfo userInfo = new MyUserInfo();  
  
        try {
        	if(session == null || !session.isConnected()) {
                session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());  
                session.setPassword(config.getPassword());  
                session.setUserInfo(userInfo);
                long start = System.currentTimeMillis();
                C.pl("SSH session creating... to " + config.getHost());
                session.connect();
                PanaceaBox.addShutdownHook(SshCommandExecutor.g(), "closeSession");
                long end = System.currentTimeMillis();
                String timespent = "timespent: " + (end - start)/1000.0 + " seconds.";
                C.pl2("SSH session created, " + timespent);
        	}
        } catch(Exception ex) {
            throw new MexException(ex);  
        }
        
        return session;
    }
  
    private List<String> remoteExecute(String command) throws MexException {  
        List<String> lines = Lists.newArrayList();
        try {
        	Session temp = getSession();
        	C.pl(StrUtil.occupy("Fetching... [{0}] from {1} by {2}", command, config.getHost(), config.getUsername()));
            Channel channel = temp.openChannel("exec");  
            ((ChannelExec)channel).setCommand(command);  
  
            channel.setInputStream(null);
            InputStream standard = channel.getInputStream();
            InputStream error = channel.getExtInputStream();
            channel.connect();
  
            String line;

            BufferedReader input = new BufferedReader(new InputStreamReader(standard, Konstants.CODE_UTF8));
            while ((line = input.readLine()) != null) {  
            	lines.add(line);  
            }
            input.close();
            
            input = new BufferedReader(new InputStreamReader(error, Konstants.CODE_UTF8));
            while ((line = input.readLine()) != null) {  
            	lines.add("ERROR: " + line);  
            }
            input.close();
  
            channel.disconnect();  
//            session.disconnect();  
        } catch (Exception ex) {
        	String msg = ex.getMessage() + ", info: " + this;
            throw new MexException(msg);  
        }  
        
        return lines;  
    }
}