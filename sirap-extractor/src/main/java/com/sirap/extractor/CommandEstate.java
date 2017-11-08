package com.sirap.extractor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sirap.basic.domain.MexItem;
import com.sirap.basic.domain.MexObject;
import com.sirap.basic.exception.MexException;
import com.sirap.basic.thread.MasterMexItemsOriented;
import com.sirap.basic.thread.WorkerMexItemsOritented;
import com.sirap.basic.tool.C;
import com.sirap.basic.util.CollectionUtil;
import com.sirap.basic.util.EmptyUtil;
import com.sirap.basic.util.MathUtil;
import com.sirap.basic.util.StrUtil;
import com.sirap.common.command.CommandBase;
import com.sirap.common.domain.Link;
import com.sirap.common.framework.Janitor;
import com.sirap.extractor.manager.Extractors;

public class CommandEstate extends CommandBase {

	private static final String KEY_ANJUKE = "anju";
	private static final String KEY_TOWN_POLICE = "pai";
	private static final String KEY_HANGYANG = "hang";

	public boolean handle() {
		if(is(KEY_ANJUKE)) {
			export(Extractors.fetchAnjukeCities());
			return true;
		}
		
		regex = KEY_ANJUKE + "\\s([^\\.]*)\\.([^\\.\\d]*)\\.(|\\d+)";
		params = parseParams(regex);
		if(params != null) {
			String city = params[0];
			if(EmptyUtil.isNullOrEmpty(city)) {
				city = g().getUserValueOf("anju.city", "nanning");
			}
			String town = params[1];
			int maxPage = MathUtil.toInteger(params[2], 1);
			Set<MexObject> allItems = new LinkedHashSet<MexObject>();
			for(int k = 1; k <= maxPage; k++) {
				try {
					List<MexObject> items = Extractors.fetchAnjukeHouse(city, town, k);
					if(EmptyUtil.isNullOrEmpty(items)) {
						break;
					}
					
					int before = allItems.size();
					allItems.addAll(items);
					int after = allItems.size();
					if(after <= before) {
						break;
					}
				} catch (MexException ex) {
					C.pl(ex);
					break;
				}
			}
			
			export(new ArrayList<>(allItems));
			
			return true;
		}
		
		if(is(KEY_TOWN_POLICE)) {
			export(Extractors.fetchNanningPolice());
			return true;
		}
		
		if(is(KEY_HANGYANG)) {
			List<String> allKeys = Lists.newArrayList();
			allKeys.addAll(MathUtil.permutation(StrUtil.ALPHANUMERIC, 1));
			allKeys.addAll(MathUtil.permutation(StrUtil.ALPHANUMERIC, 2));
			allKeys.addAll(MathUtil.permutation(StrUtil.ALPHANUMERIC, 3));
//			allKeys.addAll(MathUtil.permutation(StrUtil.ALPHANUMERIC, 4));
			List<MexItem> keyWords = CollectionUtil.toMexItems(allKeys);
			List<Link> links = getAllFuzzyPlates(keyWords);

			exportWithDefaultOptions(new ArrayList<>(new LinkedHashSet<>(links)));
			return true;
		}
		
		regex = KEY_HANGYANG + "-([A-Z][a-z0-9]{5})";
		solo = parseSoloParam(regex);
		if(solo != null) {
			Link mo = Extractors.fetchHangyangLocation(solo);
			export(mo);
			
			if(mo != null && !EmptyUtil.isNullOrEmpty(mo.getHref())) {
				Janitor.g().process(mo.getHref().trim());
			}
			
			return true;
		}
		
		regex = KEY_HANGYANG + "\\s+([a-z0-9]{1,5})";
		solo = parseSoloParam(regex);
		if(solo != null) {
			List<Link> items = Extractors.fetchHangyangPlates(solo);
			exportWithDefaultOptions(items);
			return true;
		}
		
		if(is(KEY_HANGYANG + "." + KEY_LOAD)) {
			List<String> allKeys = Lists.newArrayList();
			allKeys.addAll(MathUtil.permutation(StrUtil.ALPHANUMERIC, 1));
			allKeys.addAll(MathUtil.permutation(StrUtil.ALPHANUMERIC, 2));
			List<MexItem> keyWords = CollectionUtil.toMexItems(allKeys);
			List<Link> links = getAllFuzzyPlates(keyWords);

			exportWithDefaultOptions(new ArrayList<>(new LinkedHashSet<>(links)));
			return true;
		}

		return false;
	}
	
	public static List<Link> getAllFuzzyPlates(List<MexItem> keyWords) {
		MasterMexItemsOriented<MexItem, Link> master = new MasterMexItemsOriented<MexItem, Link>(keyWords, new WorkerMexItemsOritented<MexItem, Link>() {
			@Override
			public List<Link> process(MexItem mo) {
				String keyword = mo.toString();
				int count = countOfTasks - tasks.size();
				status(STATUS_TEMPLATE_SIMPLE, count, countOfTasks, "Fetching...", keyword);
				List<Link> items = Extractors.fetchHangyangPlates(keyword);
				status(STATUS_TEMPLATE_SIMPLE, count, countOfTasks, "Fetched.", "");
				
				return items;
			}
		});
		
		master.sitAndWait();
		
		return master.getAllMexItems();
	}
}
