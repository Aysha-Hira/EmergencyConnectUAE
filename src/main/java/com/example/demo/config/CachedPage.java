//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.config;

import java.io.Serializable;
import java.util.List;

public class CachedPage<T> implements Serializable {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    public CachedPage() {}

    public CachedPage(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<T> getContent() { return content; }
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setContent(List<T> content) { this.content = content; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}