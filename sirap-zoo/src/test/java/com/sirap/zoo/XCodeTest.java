package com.sirap.zoo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import com.sirap.basic.component.Konstants;
import com.sirap.basic.tool.C;
import com.sirap.basic.tool.D;
import com.sirap.basic.util.LocaleUtil;
import com.sirap.basic.util.StrUtil;
import com.sirap.basic.util.XCodeUtil;
import com.sirap.geek.manager.GeekManager;

public class XCodeTest {

	@Test
	public void bug() {
		Map ma = LocaleUtil.getCurrencyCodeAndSymbol();
		D.pl(ma);
		C.pl(ma.size());
	}
	
	public void fran() {
		Currency currency = Currency.getInstance(Locale.US); 
        System.out.println("United States: " + currency.getSymbol(Locale.US));

        currency = Currency.getInstance(Locale.UK);
        System.out.println("United Kingdom: " + currency.getSymbol(Locale.UK));

        currency = Currency.getInstance(Locale.FRANCE);
        System.out.println("France: " + currency.getSymbol(Locale.FRANCE));
	}
	
	public void currency() {
		Currency ca = Currency.getInstance(Locale.UK);
		List items = new ArrayList<>();
		items.add(ca.getCurrencyCode());
		items.add(ca.getSymbol());
		items.add(ca.getSymbol(Locale.UK));
		items.add(ca.getSymbol(Locale.CHINA));
		items.add(ca.getSymbol(Locale.ENGLISH));
		items.add(ca.getDisplayName(Locale.ENGLISH));
		items.add(ca.getDisplayName());
	
		C.list(items);
		
		List list = LocaleUtil.getAllCurrencies(null);
		C.listSome(list, 3);
	}
	
	public void countrycode() {
		List list = LocaleUtil.getIso3Countries();
		C.list(list);
	}
	
	public void codeAll() {
		Charset code = Charset.defaultCharset();
//		C.pl(code.aliases());
//		C.pl(code.displayName());
//		List list = new ArrayList(Charset.availableCharsets().entrySet());
//		C.list(list);
		C.total(GeekManager.g().allCodingNames().size());
//		C.list(DevManager.g().allCodingNames());
		C.list(GeekManager.g().searchCharsetNames("big5|gbk"));
	}
	
//	@Test
	public void utf8() {
		String content = "\\uE59BBE\\uE781B5";
		String code = "utf-8";
		C.pl(XCodeUtil.replaceHexChars(content, code));
	}
	public void urlParams() {
		String url = "https://www.baidu.com/s?ninja=&ie=utf-8&f=8&rsv_bp=1&tn=baidu&wd=%E5%9B%BE%E7%81%B5openapi&oq=%25E5%259B%25BE%25E7%2581%25B5api%2520key&rsv_pq=c899596000112db3&rsv_t=f66bgZie1eOrmfaOYAkAWDNarLkHiJDV2GYpIGfiyMs9mYhO3ioNevTkLUY&rqlang=cn&rsv_enter=1&inputT=3127&rsv_sug3=86&rsv_sug1=56&rsv_sug7=100&rsv_sug2=0&rsv_sug4=4593&rsv_sug=2";
		url = "http://www.tuling123.com/openapi/api?key=e8c190a005adc401867efd1ad2602f70&info=桂林";
		String fixedUrl = XCodeUtil.urlParamsEncode(url, Konstants.CODE_UTF8);
		C.pl(fixedUrl);
	}
	
	public void is2by() throws Exception {
		String source = "\\uCDF2ACD\\uCDF2";
		String va = XCodeUtil.replaceHexChars(source, Konstants.CODE_GBK);
		C.pl(va);

		source = "\\uE4B887THINK\\uE4B887";
		va = XCodeUtil.replaceHexChars(source, Konstants.CODE_UTF8);
		C.pl(va);

		source = "\\u4E07LEILA\\u4E07";
		va = XCodeUtil.replaceHexChars(source, Konstants.CODE_UNICODE);
		C.pl(va);
	}
	
	public final byte[] input2byte(InputStream inStream) throws IOException {  
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();  
        byte[] buff = new byte[100];  
        int rc = 0;  
        while ((rc = inStream.read(buff, 0, 100)) > 0) {  
        	C.pl(rc);
            swapStream.write(buff, 0, rc);  
        }  
        byte[] in2b = swapStream.toByteArray();  
        return in2b;  
    }  
	
	public void console() throws Exception {
		String wan = "万";
		byte[] b1 = wan.getBytes(Konstants.CODE_GBK);
		C.pl(D.toList(b1));
		byte[] b2 = wan.getBytes(Konstants.CODE_UTF8);
		C.pl(D.toList(b2));
		byte[] b3 = wan.getBytes(Konstants.CODE_UNICODE);
		C.pl(D.toList(b3));
	}
	public void g2() throws Exception {
//		C.pl(DevManager.decode("BBB6", "GBK"));
//		C.pl(DevManager.decode("6B22", "Unicode"));
//		C.pl(DevManager.decode("E6ACA1", "UTF-8"));
		C.pl(XCodeUtil.decodeHexChars("A1A2", "GBK"));
		C.pl(XCodeUtil.decodeHexChars("3023", "Unicode"));
		C.pl(XCodeUtil.decodeHexChars("E38081", "UTF-8"));
	}
	public void gbk() throws Exception {
		String s1 = "欢";
		byte[] bs = s1.getBytes(Konstants.CODE_GBK);
		C.pl(D.toList(bs));
		String binStr = Integer.toBinaryString(bs[0]);
		int index = binStr.length() - 8;
		String last8 = index >= 0 ? binStr.substring(index) : binStr;
		String hex = Integer.toHexString(Integer.parseInt(last8, 2));
		if(hex.length() == 1) {
			hex = "0" + hex;
		}
		D.pl(binStr, last8, hex);
		String bin2 = Integer.toBinaryString(Integer.parseInt(hex, 16));
		int intB2 = Integer.parseInt(bin2, 2);
		D.pl(intB2, Integer.parseInt(hex, 16));
		int intBinary = 0xFFFF0000|(byte)intB2;
		D.pl(intBinary);
	}
	
	public byte decode(String hex, String charset) {
		int intValue = Integer.parseInt(hex, 16);
		int afterOr = 0xFFFF0000|intValue;
		
		return (byte)(afterOr);
	}
	
	public void bytes2Hex() {
		byte[] bytes = {-51, -14};
		String v1 = StrUtil.bytesToHexString(bytes);
		String v2 = XCodeUtil.encode2HexChars('万', Konstants.CODE_UTF8);
		String v3 = XCodeUtil.encode2HexChars('万', Konstants.CODE_GBK);
		String v4 = XCodeUtil.encode2HexChars('万', Konstants.CODE_UNICODE);
		C.pl(v1);
		C.pl(v2);
		C.pl(v3);
		C.pl(v4);
	}
	
	public void encode() {
		XCodeUtil.encode2HexChars('万', Konstants.CODE_UTF8);
		XCodeUtil.encode2HexChars('万', Konstants.CODE_GBK);
		XCodeUtil.encode2HexChars('万', Konstants.CODE_UNICODE);
		C.pl();
		XCodeUtil.encode2HexChars('T', Konstants.CODE_UTF8);
		XCodeUtil.encode2HexChars('T', Konstants.CODE_GBK);
		XCodeUtil.encode2HexChars('T', Konstants.CODE_UNICODE);
		C.pl();
		XCodeUtil.encode2HexChars('の', Konstants.CODE_UTF8);
		XCodeUtil.encode2HexChars('の', Konstants.CODE_GBK);
		XCodeUtil.encode2HexChars('の', Konstants.CODE_UNICODE);
		//C.pl(StrUtil.unicode2regular("7E"));
		//C.pl(StrUtil.unicode2regular("0054"));
	}
	
	public void base64() throws Exception {
		C.pl(Charset.defaultCharset().toString());
		String v1 = "T法の";
		String t64 = XCodeUtil.toBase64(v1);
		String f64 = XCodeUtil.fromBase64(t64);
		D.pl(v1, t64, f64);
	}
}
