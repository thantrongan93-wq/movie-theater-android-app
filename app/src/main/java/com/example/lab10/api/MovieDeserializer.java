package com.example.lab10.api;

import com.example.lab10.models.Movie;
import com.example.lab10.models.Showtime;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MovieDeserializer implements JsonDeserializer<Movie> {

    @Override
    public Movie deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || json.isJsonNull() || !json.isJsonObject()) {
            return null;
        }

        JsonObject o = json.getAsJsonObject();
        Movie m = new Movie();

        // 1. IDs
        Long id = optLong(o, "movieId", "id");
        if (id != null) m.setMovieId(id);

        // 2. Tên phim - Cực kỳ quan trọng để hiển thị ở Home
        String nameEn = optString(o, "movieNameEn");
        String nameVi = optString(o, "movieNameVi");
        String title = optString(o, "title");

        m.setMovieNameEn(nameEn);
        m.setMovieNameVi(nameVi);
        m.setTitle(title);

        // 3. Poster & Description
        m.setPosterUrl(optString(o, "posterUrl"));
        m.setDescription(optString(o, "description"));
        
        // 4. Các thông tin khác
        m.setDirector(optString(o, "director"));
        m.setCountry(optString(o, "country"));
        m.setLanguage(optString(o, "language"));
        m.setProductionCompany(optString(o, "productionCompany"));
        m.setTrailerUrl(optString(o, "trailerUrl"));
        m.setVersion(optString(o, "version"));
        m.setAgeRestriction(optString(o, "ageRestriction", "ageRating", "age_rating"));
        m.setFromDate(optString(o, "fromDate", "releaseDate", "purchaseDate"));
        m.setPurchaseDate(optString(o, "purchaseDate"));
        m.setMovieStatus(optString(o, "movieStatus", "status"));

        Double purchasePrice = optDouble(o, "purchasePrice");
        if (purchasePrice != null) m.setPurchasePrice(purchasePrice);
        
        Double rating = optDouble(o, "rating");
        if (rating != null) m.setRating(rating);

        // 5. Actors
        JsonElement actorsElem = o.get("actors");
        if (actorsElem != null && !actorsElem.isJsonNull()) {
            if (actorsElem.isJsonArray()) {
                List<String> actorsList = new ArrayList<>();
                for (JsonElement e : actorsElem.getAsJsonArray()) {
                    actorsList.add(e.getAsString());
                }
                m.setActors(actorsList);
            } else {
                m.setActors(actorsElem.getAsString());
            }
        }

        // 6. Genres
        JsonElement genresElem = o.get("genres");
        if (genresElem != null && !genresElem.isJsonNull()) {
            List<String> genresList = new ArrayList<>();
            if (genresElem.isJsonArray()) {
                for (JsonElement e : genresElem.getAsJsonArray()) {
                    genresList.add(e.getAsString());
                }
            } else if (genresElem.isJsonPrimitive()) {
                String g = genresElem.getAsString();
                if (g != null) {
                    for (String part : g.split(",")) {
                        genresList.add(part.trim());
                    }
                }
            }
            m.setGenres(genresList);
        }

        // 7. Duration
        m.setDuration(optInt(o, "duration"));
        m.setRunningTime(optString(o, "runningTime"));

        // 8. Showtimes
        JsonElement showtimesElem = o.get("showtimes");
        if (showtimesElem != null && showtimesElem.isJsonArray()) {
            List<Showtime> showtimeList = new ArrayList<>();
            for (JsonElement e : showtimesElem.getAsJsonArray()) {
                try {
                    Showtime st = context.deserialize(e, Showtime.class);
                    if (st != null) showtimeList.add(st);
                } catch (Exception ignored) {}
            }
            m.setShowtimes(showtimeList);
        }

        return m;
    }

    private static String optString(JsonObject o, String... keys) {
        for (String k : keys) {
            JsonElement e = o.get(k);
            if (e != null && !e.isJsonNull()) {
                return e.getAsString();
            }
        }
        return null;
    }

    private static Long optLong(JsonObject o, String... keys) {
        for (String k : keys) {
            JsonElement e = o.get(k);
            if (e != null && !e.isJsonNull()) {
                try {
                    return e.getAsLong();
                } catch (Exception ignored) {
                    try {
                        return Long.parseLong(e.getAsString());
                    } catch (Exception ignored2) {}
                }
            }
        }
        return null;
    }

    private static Integer optInt(JsonObject o, String... keys) {
        for (String k : keys) {
            JsonElement e = o.get(k);
            if (e != null && !e.isJsonNull()) {
                try {
                    return e.getAsInt();
                } catch (Exception ignored) {
                    try {
                        return Integer.parseInt(e.getAsString());
                    } catch (Exception ignored2) {}
                }
            }
        }
        return null;
    }

    private static Double optDouble(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e != null && !e.isJsonNull()) {
            try {
                return e.getAsDouble();
            } catch (Exception ignored) {
                try {
                    return Double.parseDouble(e.getAsString());
                } catch (Exception ignored2) {}
            }
        }
        return null;
    }
}
