package com.czj.student.util;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    // 当前页码
    private int pageNum;
    
    // 每页数量
    private int pageSize;
    
    // 总记录数
    private long total;
    
    // 总页数
    private int pages;
    
    // 数据列表
    private List<T> list;
    
    // 是否为第一页
    private boolean isFirstPage;
    
    // 是否为最后一页
    private boolean isLastPage;
    
    // 是否有前一页
    private boolean hasPreviousPage;
    
    // 是否有下一页
    private boolean hasNextPage;

    public PageResult(int pageNum, int pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
        
        // 计算总页数
        this.pages = pageSize == 0 ? 1 : (int) Math.ceil((double) total / pageSize);
        
        // 计算页面状态
        this.isFirstPage = pageNum == 1;
        this.isLastPage = pageNum >= pages;
        this.hasPreviousPage = pageNum > 1;
        this.hasNextPage = pageNum < pages;
    }
} 