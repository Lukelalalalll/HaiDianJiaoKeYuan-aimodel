package com.zklcsoftware.basic.model;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

public class PageData extends HashMap implements Map {

    private static final long serialVersionUID = 1L;

    Map map = null;
    HttpServletRequest request;

    public PageData(HttpServletRequest request) {
        this.request = request;
        Map properties = request.getParameterMap();
        Map returnMap = new HashMap();
        Iterator entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        while(entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if(null == valueObj) {
                value = "";
            } else if(valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for(String value2 : values) {
                    value = value2 + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            returnMap.put(name, value);
        }
        map = returnMap;
    }

    public PageData() {
        map = new HashMap();
    }

    @Override
    public Object get(Object key) {
        Object obj = null;
        if(map.get(key) instanceof Object[]) {
            Object[] arr = (Object[]) map.get(key);
            obj = request == null ? arr : (request.getParameter((String) key) == null ? arr : arr[0]);
        } else {
            obj = map.get(key);
        }
        return obj;
    }

    public String getString(Object key) {
        return (String) get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return map.containsValue(value);
    }

    @Override
    public Set entrySet() {
        // TODO Auto-generated method stub
        return map.entrySet();
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return map.isEmpty();
    }

    @Override
    public Set keySet() {
        // TODO Auto-generated method stub
        return map.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void putAll(Map t) {
        // TODO Auto-generated method stub
        map.putAll(t);
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return map.size();
    }

    @Override
    public Collection values() {
        // TODO Auto-generated method stub
        return map.values();
    }
    @SuppressWarnings("unchecked")
    public static PageData objectToKeyValue(Object obj) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        PageData pd = new PageData();
        for(Field field : fields) {
            field.setAccessible(true);
            /*switch(field.getType().getName()) {
            case "java.lang.String":
                pd.map.put(field.getName(), field.get(obj));
                break;
            case "java.lang.Integer":
                pd.map.put(field.getName(), field.get(obj));
                break;
            case "java.lang.Double":
                pd.map.put(field.getName(), field.get(obj));
                break;
            case "java.lang.Float":
                pd.map.put(field.getName(), field.get(obj));
                break;
            case "java.lang.Char":
                pd.map.put(field.getName(), field.get(obj));
                break;
            case "java.util.Date":
                pd.map.put(field.getName(), field.get(obj));
                break;
            case "float":
                pd.map.put(field.getName(), field.get(obj));
                break;
            case "int":
                pd.map.put(field.getName(), field.get(obj));
                break;
            case "double":
                pd.map.put(field.getName(), field.get(obj));
                break;
            case "char":
                pd.map.put(field.getName(), field.get(obj));
                break;
            default:
                pd.map.put(field.getName(), field.get(obj));
                break;
            }*/
            if("java.lang.String".equals(field.getType().getName())){
            	 pd.map.put(field.getName(), field.get(obj));
            }else if("java.lang.Integer".equals(field.getType().getName())){
            	 pd.map.put(field.getName(), field.get(obj));
            }else if("java.lang.Double".equals(field.getType().getName())){
            	 pd.map.put(field.getName(), field.get(obj));
            }else if("java.lang.Float".equals(field.getType().getName())){
            	 pd.map.put(field.getName(), field.get(obj));
            }else if("java.lang.Char".equals(field.getType().getName())){
            	 pd.map.put(field.getName(), field.get(obj));
            }else if("java.util.Date".equals(field.getType().getName())){
            	 pd.map.put(field.getName(), field.get(obj));
            }else if("float".equals(field.getType().getName())){
            	 pd.map.put(field.getName(), field.get(obj));
            }else if("int".equals(field.getType().getName())){
            	 pd.map.put(field.getName(), field.get(obj));
            }else if("double".equals(field.getType().getName())){
            	 pd.map.put(field.getName(), field.get(obj));
            }else if("char".equals(field.getType().getName())){
            	 pd.map.put(field.getName(), field.get(obj));
            }else {
            	 pd.map.put(field.getName(), field.get(obj));
            }
        }
        return pd;
    }

}
