package com.czj.student.util;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class PageRequest {
    @Min(value = 1, message = "页码必须大于0")
    private int pageNum = 1;

    @Min(value = 1, message = "每页条数必须大于0")
    private int pageSize = 10;

    private String orderBy;
} 