package com.zklcsoftware.basic.repository.search;

/**
 * 排序
 */
public class SortItem {

    private String field; // 排序属性

    private String order = "desc"; // 排序规则，默认倒序

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
