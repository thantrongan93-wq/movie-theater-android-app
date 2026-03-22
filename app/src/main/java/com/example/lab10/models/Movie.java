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
    
    @SerializedName("title")
    private String title;

    @SerializedName("slugEn")
    private String slugEn;

    @SerializedName("slugVi")
    private String slugVi;

    @SerializedName("actors")
    private Object actors;

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
    
    @SerializedName("runningTime")
    private String runningTime;

    @SerializedName("version")
    private String version;

    @SerializedName("purchasePrice")
    private Double purchasePrice;

    @SerializedName("purchaseDate")
    private String purchaseDate;

    @SerializedName("fromDate")
    private String fromDate;
    
    @SerializedName("releaseDate")
    private String releaseDate;

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
    
    @SerializedName("ageRating")
    private String ageRating;

    @SerializedName("language")
    private String language;

    @SerializedName("rating")
    private Double rating;

    // Thêm field showtimes vì một số API detail trả về kèm showtimes
    @SerializedName("showtimes")
    private List<Showtime> showtimes;

    public Movie() {}

    // === Setters ===
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public void setMovieNameEn(String movieNameEn) { this.movieNameEn = movieNameEn; }
    public void setMovieNameVi(String movieNameVi) { this.movieNameVi = movieNameVi; }
    public void setTitle(String title) { this.title = title; }
    public void setSlugEn(String slugEn) { this.slugEn = slugEn; }
    public void setSlugVi(String slugVi) { this.slugVi = slugVi; }
    public void setActors(Object actors) { this.actors = actors; }
    public void setGenres(List<String> genres) { this.genres = genres; }
    public void setProductionCompany(String productionCompany) { this.productionCompany = productionCompany; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public void setRunningTime(String runningTime) { this.runningTime = runningTime; }
    public void setVersion(String version) { this.version = version; }
    public void setPurchasePrice(Double purchasePrice) { this.purchasePrice = purchasePrice; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }
    public void setFromDate(String fromDate) { this.fromDate = fromDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setToDate(String toDate) { this.toDate = toDate; }
    public void setMovieStatus(String movieStatus) { this.movieStatus = movieStatus; }
    public void setDirector(String director) { this.director = director; }
    public void setCountry(String country) { this.country = country; }
    public void setAgeRestriction(String ageRestriction) { this.ageRestriction = ageRestriction; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }
    public void setLanguage(String language) { this.language = language; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setShowtimes(List<Showtime> showtimes) { this.showtimes = showtimes; }

    // === Getters ===
    public Long getMovieId() { return movieId; }
    public Long getId() { return movieId; }
    public String getMovieNameEn() { return movieNameEn; }
    public String getMovieNameVi() { return movieNameVi; }
    public List<Showtime> getShowtimes() { return showtimes; }
    
    public String getActors() { 
        if (actors instanceof List) {
            return String.join(", ", (List<String>) actors);
        }
        return actors != null ? actors.toString() : ""; 
    }

    public List<String> getGenres() { return genres; }
    public String getProductionCompany() { return productionCompany; }
    public String getTrailerUrl() { return trailerUrl; }
    public String getDescription() { return description; }
    public String getPosterUrl() { return posterUrl; }
    public String getDirector() { return director; }
    public String getCountry() { return country; }
    public String getLanguage() { return language; }
    public Double getRating() { return rating; }
    public String getVersion() { return version; }
    public Double getPurchasePrice() { return purchasePrice; }
    public String getPurchaseDate() { return purchaseDate; }

    public Integer getDurationValue() { 
        if (duration != null) return duration;
        try {
            return runningTime != null ? Integer.parseInt(runningTime) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getTitle() {
        if (title != null && !title.isEmpty()) return title;
        if (movieNameEn != null && !movieNameEn.isEmpty()) return movieNameEn;
        return movieNameVi;
    }

    public String getGenre() {
        if (genres != null && !genres.isEmpty()) {
            return String.join(", ", genres);
        }
        return "";
    }

    public String getAgeRating() { 
        if (ageRating != null && !ageRating.isEmpty()) return ageRating;
        return ageRestriction;
    }

    public String getReleaseDate() { 
        if (releaseDate != null && !releaseDate.isEmpty()) return releaseDate;
        return fromDate;
    }
    
    public String getDuration() {
        Integer d = getDurationValue();
        return d != null ? d.toString() : "Chưa cập nhật";
    }
}
