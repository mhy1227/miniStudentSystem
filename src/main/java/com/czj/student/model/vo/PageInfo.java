package com.czj.student.model.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 分页信息封装类
 * @param <T> 分页数据类型
 */
public class PageInfo<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private int pages;

    /**
     * 分页数据
     */
    private List<T> rows;

    /**
     * 缓存标识，用于标识分页缓存结果
     */
    private String uuid;

    /**
     * 无参构造函数
     */
    public PageInfo() {
        this(1, 10);
    }

    /**
     * 构造函数
     * @param page 当前页码
     * @param size 每页大小
     */
    public PageInfo(int page, int size) {
        this.page = page > 0 ? page : 1;
        this.size = size > 0 ? size : 10;
    }

    /**
     * 填充分页结果
     * @param list 数据列表
     * @param total 总记录数
     * @return 当前对象
     */
    public PageInfo<T> of(List<T> list, long total) {
        this.rows = list;
        this.total = total;
        this.pages = this.size > 0 ? (int) Math.ceil((double) total / this.size) : 0;
        return this;
    }

    // Getters and Setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page > 0 ? page : 1;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size > 0 ? size : 10;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * 获取当前页第一条记录在总结果中的位置
     * @return 偏移量
     */
    public int getOffset() {
        return (page - 1) * size;
    }

    /**
     * 是否有下一页
     * @return 是否有下一页
     */
    public boolean hasNextPage() {
        return page < pages;
    }

    /**
     * 是否有上一页
     * @return 是否有上一页
     */
    public boolean hasPreviousPage() {
        return page > 1;
    }

    /**
     * 是否为第一页
     * @return 是否为第一页
     */
    public boolean isFirstPage() {
        return page == 1;
    }

    /**
     * 是否为最后一页
     * @return 是否为最后一页
     */
    public boolean isLastPage() {
        return page >= pages;
    }

    @Override
    public String toString() {
        return "PageInfo{" +
                "page=" + page +
                ", size=" + size +
                ", total=" + total +
                ", pages=" + pages +
                ", rowsSize=" + (rows == null ? 0 : rows.size()) +
                ", uuid='" + uuid + '\'' +
                '}';
    }
} 