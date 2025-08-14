package com.zklcsoftware.basic.util;

import java.math.BigDecimal;

public class ObjectUtils{
  public static boolean nullSafeEquals(Object o1, Object o2) {
    return ((o1 == o2) || ((o1 != null) && (o1.equals(o2))));
  }
  
	public static Object convert(Object object, Class<?> type) {
	    if (object instanceof BigDecimal) {
	        Number number = (Number) object;
	        if (type.equals(byte.class) || type.equals(Byte.class)) {
	            return number.byteValue();
	        }
	        if (type.equals(short.class) || type.equals(Short.class)) {
	            return number.shortValue();
	        }
	        if (type.equals(int.class) || type.equals(Integer.class)) {
	            return number.intValue();
	        }
	        if (type.equals(long.class) || type.equals(Long.class)) {
	            return number.longValue();
	        }
	        if (type.equals(float.class) || type.equals(Float.class)) {
	            return number.floatValue();
	        }
	        if (type.equals(double.class) || type.equals(Double.class)) {
	            return number.doubleValue();
	        }
	    }		
	    return object;		
	}
}
