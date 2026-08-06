package com.ecommerce.dto;

import java.util.List;

public class PagedResult<T> {
    private List<T> items;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalCount;
    private Integer totalPages;

    public PagedResult() {}

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }
    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
}
