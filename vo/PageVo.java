package com.cb.vo;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PageVo<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7019148910685021669L;

    private Integer pageNo;

    private Integer pageSize;

    private List<T> list = new ArrayList<T>();

    private long totalCounts;

    private int totalPages;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalCounts() {
        return totalCounts;
    }

    public void setTotalCounts(long totalCounts) {
        this.totalCounts = totalCounts;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

}
