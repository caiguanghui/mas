package com.sirap.basic.thirdparty.media;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.datatype.NumberFixedLength;
import org.jaudiotagger.tag.id3.ID3v23Frame;
import org.jaudiotagger.tag.id3.ID3v23Tag;

import com.sirap.basic.exception.MexException;
import com.sirap.basic.util.PanaceaBox;
import com.sirap.basic.util.StrUtil;

public class MediaFileUtil {
	
	public static int readMp3DurationInSeconds(String filepath) throws MexException {
		NumberFixedLength.logger.setLevel(Level.SEVERE);
		AudioFileIO.logger.setLevel(Level.SEVERE); 
		ID3v23Frame.logger.setLevel(Level.SEVERE);  
		ID3v23Tag.logger.setLevel(Level.SEVERE);
		
		try {
			File file = new File(filepath);
	        MP3File f = (MP3File)AudioFileIO.read(file);  
	        MP3AudioHeader audioHeader = (MP3AudioHeader)f.getAudioHeader();  
	        int len = audioHeader.getTrackLength();
	        
	        return len;
	    } catch(Exception ex) {  
	        String msg = "filepath: " + filepath + ", original message" + ex.getMessage();
	        throw new MexException(msg);
	    }
	}
	
	/***
	 * Duration: 00:04:03.07, start: 0.025057, bitrate: 192 kb/s
	 * FF means FFMpeg.exe
	 * @param filepath
	 * @param ffLocation
	 * @return
	 */
	public static String readMediaDurationInSecondsWithFF(String filepath) {
		String cmd = "ffmpeg -i \"" + filepath + "\"";
		String newCommand = "cmd /c " + cmd;

		List<String> result = PanaceaBox.executeAndRead(newCommand);
		for(String record : result) {
			String regex = "Duration: ([\\d:\\.]+),";
			String param = StrUtil.findFirstMatchedItem(regex, record);
			if(param != null) {
				return param;
			}
		}
		
		return null;
	}
}
