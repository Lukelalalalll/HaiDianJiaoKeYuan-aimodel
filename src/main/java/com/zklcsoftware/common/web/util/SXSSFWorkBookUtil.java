package com.zklcsoftware.common.web.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 导入导出工具类
 */
@Component
public class SXSSFWorkBookUtil {

    private static SXSSFWorkBookUtil util;

    public static void downloadExcel(String heads,String data,String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (data == null) {
            return;
        }

        JSONArray headsArray = JSONObject.parseArray(heads);
        JSONArray dataArray = JSONObject.parseArray(data);

        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet();

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("序号");// 第一行列名
        int l = 1;
        for (int i = 0; i < headsArray.size(); i++) {
            sheet.setColumnWidth(l, 256 * 24);
            JSONArray items = (JSONArray) headsArray.get(i);
            row.createCell(l).setCellValue(items.get(1).toString());// 第一行列名
            l++;
        }
        //组装数据
        for (int j = 0; j < dataArray.size(); j++) {
            JSONObject item = (JSONObject) dataArray.get(j);
            Row nextRow = sheet.createRow(j + 1);
            int k = 0;
            nextRow.createCell(k++).setCellValue(j+1);
            for (Object head : headsArray) {
                JSONArray items = (JSONArray) head;
                nextRow.createCell(k++).setCellValue(item.get(items.get(0).toString()) == null ? "" : (String) item.get(items.get(0).toString()));
            }
        }

        setResponse(request, response, fileName+".xls");
        OutputStream out = response.getOutputStream();
        workbook.write(out);
        out.close();
    }

    public static String removeHtml(String text) {
        Pattern pattern = Pattern.compile("<.+?>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll("");
    }

    public static void setResponse(HttpServletRequest request, HttpServletResponse response, String name) throws IOException {
        String disposition = "attachment;filename=" + new String((name).getBytes(), StandardCharsets.ISO_8859_1);
        String header = request.getHeader("USER-AGENT").toLowerCase();
        if (header.contains("edge") || (!header.contains("firefox") && !header.contains("safari"))) {
            disposition = "attachment;filename=" + URLEncoder.encode(name, "UTF-8");
        }
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", disposition);
        response.flushBuffer();
    }

    /**
     * 根据扩展名判断版本对应对象。
     */
    public static Workbook createBook(String fileName, InputStream is) throws IOException {
        Workbook workbook = null;
        if (fileName.endsWith("xls")) {
            //2003
            workbook = new HSSFWorkbook(is);
        } else if (fileName.endsWith("xlsx")) {
            //2007
            workbook = new XSSFWorkbook(is);
        }
        return workbook;
    }

    /**
     * 获取两个日期之间的所有月份 (年月)
     *
     * @param startDate
     * @param endDate
     * @return：list
     */
    public static List<String> getMonthBetweenDate(Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        // 声明保存日期集合
        List<String> list = new ArrayList<>();
        try {
            /*// 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);*/

            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime() <= endDate.getTime()) {

                // 把日期添加到集合
                list.add(sdf.format(startDate));

                // 设置日期
                calendar.setTime(startDate);

                //把月数增加 1
                calendar.add(Calendar.MONTH, 1);

                // 获取增加后的日期
                startDate = calendar.getTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    @PostConstruct
    public void init() {
        util = this;
    }

    /**
     * 查询字符串中某个子字符串出现的次数
     * @param text
     * @param pattern
     * @return
     */
    public static int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}