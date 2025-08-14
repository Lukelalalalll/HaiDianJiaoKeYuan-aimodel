package com.zklcsoftware.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableResult<T> {
    List<T> list;
    Pagination pagination;

    public static TableResult putResult(List list){
        TableResult result= new TableResult();
        result.setList(list);
        return result;
    }

    public static TableResult putResult(Page page){
        TableResult result= new TableResult();
        if (page.hasContent()){
            result.setList(page.getContent());
        }
        result.setPagination(Pagination.builder().current(Long.valueOf(page.getNumber())).pageSize(Long.valueOf(page.getSize())).total(Long.valueOf(page.getTotalElements())).totalPages(Long.valueOf(page.getTotalPages())).build());
        return result;
    }
}