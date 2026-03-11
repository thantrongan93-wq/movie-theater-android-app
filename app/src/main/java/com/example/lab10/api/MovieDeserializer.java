package com.example.lab10.api;

import com.example.lab10.models.Movie;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Backend trả về Movie không đồng nhất giữa các endpoint:
 * - actors: có thể là String hoặc Array<String>
 * - runningTime: có thể là String, trong khi model cần Integer duration
 * - ageRating/ageRestriction, releaseDate/fromDate...
 *
 * Deserializer này giúp app parse ổn định nhưng vẫn giữ Movie là Serializable
 * để truyền qua Intent.
 */
public class MovieDeserializer implements JsonDeserializer<Movie> {

    @Override
    public Movie deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || json.isJsonNull() || !json.isJsonObject()) {
            return null;
        }

        JsonObject o = json.getAsJsonObject();
        Movie m = new Movie();

        // ids
        Long id = optLong(o, "movieId", "id");
        if (id != null) m.setMovieId(id);

        // names
        m.setTitleRaw(optString(o, "title"));
        m.setMovieNameEn(optString(o, "movieNameEn"));
        m.setMovieNameVi(optString(o, "movieNameVi"));

        // simple fields
        m.setPosterUrl(optString(o, "posterUrl"));
        m.setDescription(optString(o, "description"));
        m.setDirector(optString(o, "director"));
        m.setCountry(optString(o, "country"));
        m.setLanguage(optString(o, "language"));
        m.setProductionCompany(optString(o, "productionCompany"));
        m.setTrailerUrl(optString(o, "trailerUrl"));

        m.setAgeRestriction(optString(o, "ageRestriction", "ageRating"));
        m.setFromDate(optString(o, "fromDate", "releaseDate"));
        m.setToDate(optString(o, "toDate"));
        m.setMovieStatus(optString(o, "movieStatus", "status"));
        m.setRating(optDouble(o, "rating"));

        // actors
        m.setActors(parseStringOrArray(o.get("actors")));

        // genres
        m.setGenres(parseStringList(o.get("genres")));

        // duration
        Integer duration = optInt(o, "duration", "runningTime");
        if (duration != null) m.setDuration(duration);

        return m;
    }

    private static String optString(JsonObject o, String... keys) {
        for (String k : keys) {
            JsonElement e = o.get(k);
            if (e == null || e.isJsonNull()) continue;
            if (e.isJsonPrimitive()) {
                String s = e.getAsString();
                if (s != null) return s;
            } else {
                return e.toString();
            }
        }
        return null;
    }

    private static Long optLong(JsonObject o, String... keys) {
        for (String k : keys) {
            JsonElement e = o.get(k);
            if (e == null || e.isJsonNull()) continue;
            try {
                if (e.isJsonPrimitive()) return e.getAsLong();
            } catch (Exception ignored) {}
            try {
                String s = e.getAsString();
                if (s != null && !s.trim().isEmpty()) return Long.parseLong(s.trim());
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Integer optInt(JsonObject o, String... keys) {
        for (String k : keys) {
            JsonElement e = o.get(k);
            if (e == null || e.isJsonNull()) continue;
            try {
                if (e.isJsonPrimitive()) {
                    String s = e.getAsString();
                    if (s == null) continue;
                    s = s.trim();
                    if (s.isEmpty()) continue;
                    return Integer.parseInt(s);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Double optDouble(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null || e.isJsonNull()) return null;
        try {
            if (e.isJsonPrimitive()) return Double.parseDouble(e.getAsString());
        } catch (Exception ignored) {}
        return null;
    }

    private static String parseStringOrArray(JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        if (e.isJsonPrimitive()) return e.getAsString();
        if (e.isJsonArray()) {
            JsonArray arr = e.getAsJsonArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.size(); i++) {
                JsonElement it = arr.get(i);
                if (it == null || it.isJsonNull()) continue;
                String s = it.isJsonPrimitive() ? it.getAsString() : it.toString();
                if (s == null) continue;
                s = s.trim();
                if (s.isEmpty()) continue;
                if (sb.length() > 0) sb.append(", ");
                sb.append(s);
            }
            return sb.toString();
        }
        return e.toString();
    }

    private static List<String> parseStringList(JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        List<String> out = new ArrayList<>();
        if (e.isJsonArray()) {
            for (JsonElement it : e.getAsJsonArray()) {
                if (it == null || it.isJsonNull()) continue;
                String s = it.isJsonPrimitive() ? it.getAsString() : it.toString();
                if (s == null) continue;
                s = s.trim();
                if (!s.isEmpty()) out.add(s);
            }
            return out;
        }
        if (e.isJsonPrimitive()) {
            String s = e.getAsString();
            if (s == null) return null;
            for (String part : s.split(",")) {
                String p = part.trim();
                if (!p.isEmpty()) out.add(p);
            }
            return out.isEmpty() ? null : out;
        }
        return null;
    }
}

