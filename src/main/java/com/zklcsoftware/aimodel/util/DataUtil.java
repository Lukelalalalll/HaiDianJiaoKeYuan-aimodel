package com.zklcsoftware.aimodel.util;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class DataUtil {

    /**
     * 递归处理教学数据
     */
    public static void execJson(List<String> names, List<Object> child, String fullName) {
        for (Object data : child) {
            Map map = new Gson().fromJson(new Gson().toJson(data), Map.class);
            if (map.containsKey("children")) {
                execJson(names, (List<Object>) map.get("children"), fullName + map.get("name") + " | ");
            } else {
                names.add(fullName + map.get("name"));
            }
        }
    }

    /**
     * 自定义排序
     */
    public static void bubbleSort(String[] names) {
        if (names == null || names.length == 0) {
            return;
        }
        int n = names.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if ((names[j].contains("年级") && names[j + 1].contains("年级")) || (names[j].contains("第") && names[j + 1].contains("第"))) {
                    int iii = convertNum(names[j]);
                    int jjj = convertNum(names[j + 1]);
                    if (jjj > 0 && iii > jjj) {
                        String temp = names[j];
                        names[j] = names[j + 1];
                        names[j + 1] = temp;
                        swapped = true; // 表示发生了交换
                    }
                }
            }
            // 如果这一趟没有发生交换，说明数组已经有序，可以提前结束排序
            if (!swapped) {
                break;
            }
        }
    }

    private static int convertNum(String s) {
        if (s.contains("一")) {
            return 1;
        } else if (s.contains("二")) {
            return 2;
        } else if (s.contains("三")) {
            return 3;
        } else if (s.contains("四")) {
            return 4;
        } else if (s.contains("五")) {
            return 5;
        } else if (s.contains("六")) {
            return 6;
        } else if (s.contains("七")) {
            return 7;
        } else if (s.contains("八")) {
            return 8;
        } else if (s.contains("九")) {
            return 9;
        } else {
            return 0;
        }
    }

}
