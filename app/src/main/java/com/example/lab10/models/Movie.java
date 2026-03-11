package com.example.lab10.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Movie implements Serializable {

    @SerializedName("movieId")
    private Long movieId;

    @SerializedName("movieNameEn")
    private String movieNameEn;

    @SerializedName("movieNameVi")
    private String movieNameVi;

    @SerializedName("slugEn")
    private String slugEn;

    @SerializedName("slugVi")
    private String slugVi;

    @SerializedName("actors")
    private String actors;

    @SerializedName("genres")
    private List<String> genres;

    @SerializedName("productionCompany")
    private String productionCompany;

    @SerializedName("trailerUrl")
    private String trailerUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("posterUrl")
    private String posterUrl;

    @SerializedName("duration")
    private Integer duration;

    @SerializedName("fromDate")
    private String fromDate;

    @SerializedName("toDate")
    private String toDate;

    @SerializedName("movieStatus")
    private String movieStatus;

    @SerializedName("director")
    private String director;

    @SerializedName("country")
    private String country;

    @SerializedName("ageRestriction")
    private String ageRestriction;

    @SerializedName("language")
    private String language;

    @SerializedName("rating")
    private Double rating;

    /** Field "title" trả về trực tiếp từ một số endpoint (thay vì movieNameEn) */
    private String titleRaw;

    public Movie() {}

    // === Getters / Setters gốc ===
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public String getMovieNameEn() { return movieNameEn; }
    public void setMovieNameEn(String movieNameEn) { this.movieNameEn = movieNameEn; }

    public String getMovieNameVi() { return movieNameVi; }
    public void setMovieNameVi(String movieNameVi) { this.movieNameVi = movieNameVi; }

    public String getSlugEn() { return slugEn; }
    public void setSlugEn(String slugEn) { this.slugEn = slugEn; }

    public String getSlugVi() { return slugVi; }
    public void setSlugVi(String slugVi) { this.slugVi = slugVi; }

    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public String getProductionCompany() { return productionCompany; }
    public void setProductionCompany(String productionCompany) { this.productionCompany = productionCompany; }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getFromDate() { return fromDate; }
    public void setFromDate(String fromDate) { this.fromDate = fromDate; }

    public String getToDate() { return toDate; }
    public void setToDate(String toDate) { this.toDate = toDate; }

    public String getMovieStatus() { return movieStatus; }
    public void setMovieStatus(String movieStatus) { this.movieStatus = movieStatus; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(String ageRestriction) { this.ageRestriction = ageRestriction; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Double getRatingValue() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getTitleRaw() { return titleRaw; }
    public void setTitleRaw(String titleRaw) { this.titleRaw = titleRaw; }

    // === Helper getters tương thích với code cũ ===
    /** movieId */
    public Long getId() { return movieId; }

    /** Tên tiếng Anh, fallback tiếng Việt, fallback "title" từ API */
    public String getTitle() {
        if (movieNameEn != null) return movieNameEn;
        if (movieNameVi != null) return movieNameVi;
        return titleRaw;
    }

    /** Genres list -> chuỗi phân cách bằng ", " */
    public String getGenre() {
        if (genres != null && !genres.isEmpty()) {
            return String.join(", ", genres);
        }
        return "";
    }

    /** Slug tiếng Anh, fallback tiếng Việt */
    public String getSlug() {
        return slugEn != null ? slugEn : slugVi;
    }

    /** movieStatus */
    public String getStatus() { return movieStatus; }

    /** ageRestriction */
    public String getAgeRating() { return ageRestriction; }

    /** fromDate */
    public String getReleaseDate() { return fromDate; }

    /** Rating từ field "rating" trong API */
    public Double getRating() { return rating; }
}
