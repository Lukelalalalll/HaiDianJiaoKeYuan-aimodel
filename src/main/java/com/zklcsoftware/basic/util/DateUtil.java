package com.zklcsoftware.basic.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * 日期Util类
 */
public class DateUtil {

	/**
	 * 指定的格式来格式化日期
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDateByFormat(Date date, String format) {

		String result = "";
		if (date != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				result = sdf.format(date);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 指定格式将字符串转为Date
	 * @param strDate
	 * @param format
	 * @return
	 */
	public static Date parseDate(String strDate, String format) {
		Date result = null;
		if (StringUtils.isAlpha(strDate)) {
			try {
				result = new SimpleDateFormat(format).parse(strDate);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 在日期上增加数个整月
	 * @param date
	 * @param num
	 * @return
	 */
	public static Date addMonth(Date date, int num) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, num);
		return cal.getTime();
	}

	/**
	 * 在日期上增加数一天
	 * @param date
	 * @param num
	 * @return
	 */
	public static Date addDay(Date date, int num) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, num);
		return cal.getTime();
	}
	
    /**
     * 根据日期获得星期
     * @param date
     * @return
     */
	public static String getWeekOfDate(Date date) {
		  //String[] weekDaysName = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		  String[] weekDaysCode = { "0", "1", "2", "3", "4", "5", "6" };
		  Calendar calendar = Calendar.getInstance();
		  calendar.setTime(date);
		  int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		  return weekDaysCode[intWeek];
	} 
	
	/**
	  * 获得周几的日期
	  *
	  * @param date
	  * @return
	  */
	public static String getWeekOfDay(Date date, int week) {

	  Calendar calendar = Calendar.getInstance();
	  calendar.setTime(date);
	  int value=0;
	  if(week == 0){
		  value = Calendar.SUNDAY;
	  }else if(week == 1){
		  value = Calendar.MONDAY;
	  }else if(week == 2){
		  value = Calendar.TUESDAY;
	  }else if(week == 3){
		  value = Calendar.WEDNESDAY;
	  }else if(week == 4){
		  value = Calendar.THURSDAY;
	  }else if(week == 5){
		  value = Calendar.FRIDAY;
	  }else if(week == 6){
		  value = Calendar.SATURDAY;
	  }
	  calendar.set(Calendar.DAY_OF_WEEK, value);

	  return formatDateByFormat(calendar.getTime(),"yyyy-MM-dd");

	}
    
    /**
     * 获取两个日期之间的所有日期
     * 
     * @param startTime 开始日期
     * @param endTime 结束日期
     * @return
     */
    public static List<String> getDays(String startTime, String endTime) {
        // 返回的日期集合
        List<String> days = new ArrayList<String>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;
    }
    
    /**
     * 
    * @Description: 判断两个日期大小 
    * @param @param DATE1
    * @param @param DATE2
    * @throws
     */
    public static int compareDate(String DATE1, String DATE2, int index) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() < dt2.getTime()) {
//                System.out.println("dt1 在dt2前");
                return 1;
            } else if (dt1.getTime() > dt2.getTime()) {
//                System.out.println("dt1在dt2后");
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }
}
