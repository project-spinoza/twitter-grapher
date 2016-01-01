package org.projectspinoza.twittergrapher.factory.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
	
	
	public static Map<String, Object> JsonToMap(JSONObject object) throws JSONException {
	    Map<String, Object> map = new HashMap<String, Object>();

	    Iterator<String> keysItr = object.keys();
	    while(keysItr.hasNext()) {
	        String key = keysItr.next();
	        Object value = object.get(key);

	        if(value instanceof JSONArray) {
	            value = JsonToList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = JsonToMap((JSONObject) value);
	        }
	        map.put(key, value);
	    }
	    return map;
	}

	public static List<Object> JsonToList(JSONArray array) throws JSONException {
	    List<Object> list = new ArrayList<Object>();
	    for(int i = 0; i < array.length(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JSONArray) {
	            value = JsonToList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = JsonToMap((JSONObject) value);
	        }
	        list.add(value);
	    }
	    return list;
	}
}
