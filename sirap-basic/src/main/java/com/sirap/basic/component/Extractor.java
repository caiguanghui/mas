package com.sirap.basic.component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.sirap.basic.exception.MexException;
import com.sirap.basic.tool.C;
import com.sirap.basic.util.EmptyUtil;
import com.sirap.basic.util.HtmlUtil;
import com.sirap.basic.util.IOUtil;
import com.sirap.basic.util.PanaceaBox;
import com.sirap.basic.util.StrUtil;
import com.sirap.basic.util.WebReader;
import com.sirap.basic.util.XCodeUtil;
import com.sirap.basic.util.XXXUtil;

public abstract class Extractor<T extends Object> {

	private String url;
	private String charset = Konstants.CODE_UTF8;
	
	protected boolean printFetching;
	private boolean printExceptionIfNeeded = true;
	private boolean allBeingWell = true;
	private boolean intoList;
	private boolean methodPost;
	private String requestParams;

	protected String source;
	protected List<String> sourceList;
	protected T item;
	protected List<T> mexItems = new ArrayList<>();
	
	public static String encodeURLParam(String param) {
		return XCodeUtil.urlEncodeUTF8(param);
	}
	
	public static String decodeURLParam(String param) {
		return XCodeUtil.urlDecodeUTF8(param);
	}
	
	protected final Matcher createMatcher(String regex) {
		return createMatcher(regex, source);
	}
	
	protected final Matcher createMatcher(String regex, String content) {
		return StrUtil.createMatcher(regex, content);
	}
	
	public Extractor<T> process() {
		fetch();
		
		if(source != null || sourceList != null) {
			parse();
			if(!allBeingWell) {
				C.pl2("Not cool, wrong web content");
			}
		} else {
			allBeingWell = false;
		}
		
		return this;
	}
	
	protected void fetch() {
		String target = getUrl();
		
		XXXUtil.nullCheck(target, "url");
		if(!StrUtil.isHttp(target)) {
			if(printFetching) {
				C.pl("Reading... " + target);
			}
			boolean asResourceStream = PanaceaBox.isWindows() && target.startsWith("/");
			if(intoList) {
				if(asResourceStream) {
					sourceList = IOUtil.readResourceIntoList(target);
				} else {
					sourceList = IOUtil.readFileIntoList(target, charset);
				}
			} else {
				source = IOUtil.readFileWithLineSeparator(target, "", charset);
			}
			
			return;
		}
		
		if(printFetching) {
			String temp = target;
			if(requestParams != null) {
				if(StrUtil.contains(temp, "?")) {
					temp += "&" + requestParams;
				} else {
					temp += "?" + requestParams;
				}
			}
			
			if(methodPost) {
				temp += " $+post"; 
			}
			
			C.pl("Fetching... " + temp);
		}
		
		WebReader xiu = new WebReader(target, charset);
		xiu.setMethodPost(methodPost);
		xiu.setRequestParams(requestParams);

		try {
			if(intoList) {
				sourceList = xiu.readIntoList();
			} else {
				source = xiu.readIntoString();
			}
		} catch (MexException ex) {
			if(printExceptionIfNeeded) {
				C.pl(ex);
			} else {
				throw ex;
			}
		}
	}
	
	public Extractor<T> setUrl(String url) {
		this.url = url;
		return this;
	}
	
	public String getUrl() {
		return url;
	}
	
	public Extractor<T> setCharset(String charset) {
		this.charset = charset;
		return this;
	}
	
	public Extractor<T> useUTF8() {
		this.charset = Konstants.CODE_UTF8;
		return this;
	}
	
	public Extractor<T> useList() {
		this.intoList = true;
		return this;
	}
	
	public Extractor<T> useGBK() {
		this.charset = Konstants.CODE_GBK;
		return this;
	}

	public List<T> getItems() {
		return mexItems;
	}

	public T getItem() {
		return item;
	}

	protected abstract void parse();
		
	public boolean isAllBeingWell() {
		return allBeingWell;
	}
	
	public static String getPrettyText(String source) {
		if(EmptyUtil.isNullOrEmpty(source)) {
			return source;
		}
		
		String temp = source;
		temp = HtmlUtil.removeComment(temp);
		temp = HtmlUtil.removeHttpTag(temp);
		temp = HtmlUtil.replaceRawUnicode(temp);
		temp = HtmlUtil.replaceHtmlEntities(temp);
		temp = StrUtil.reduceMultipleSpacesToOne(temp);
		temp = temp.trim();
		
		return temp;
	}

	public boolean isMethodPost() {
		return methodPost;
	}

	public Extractor<T> usePost() {
		this.methodPost = true;
		return this;
	}

	public String getRequestParams() {
		return requestParams;
	}

	public Extractor<T> setRequestParams(String requestParams) {
		this.requestParams = requestParams;
		return this;
	}

	public Extractor<T> setPrintExceptionIfNeeded(boolean flag) {
		this.printExceptionIfNeeded = flag;
		return this;
	}

	public Extractor<T> setAllBeingWell(boolean flag) {
		this.allBeingWell = flag;
		return this;
	}
}