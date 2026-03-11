package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Wrapper cho Spring Page response:
 * { "data": { "content": [...], "totalPages": N, "totalElements": N, ... } }
 */
public class PageResponse<T> {
    @SerializedName("content")
    private List<T> content;

    /** API endpoint /api/movies/getAll trả về key "movies" thay vì "content" */
    @SerializedName("movies")
    private List<T> movies;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("totalElements")
    private long totalElements;

    @SerializedName("number")
    private int number; // current page (0-based)

    @SerializedName("size")
    private int size;

    @SerializedName("last")
    private boolean last;

    @SerializedName("first")
    private boolean first;

    public List<T> getContent() { return content; }
    public List<T> getMovies() { return movies != null ? movies : content; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
    public int getNumber() { return number; }
    public int getSize() { return size; }
    public boolean isLast() { return last; }
    public boolean isFirst() { return first; }
}
