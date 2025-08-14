package com.zklcsoftware.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pagination {
    Long current;
    Long pageSize;
    Long total;
    Long totalPages;
}
