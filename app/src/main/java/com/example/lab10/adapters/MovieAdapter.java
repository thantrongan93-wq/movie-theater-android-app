package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.Movie;
import com.example.lab10.utils.ImageLoader;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    
    private List<Movie> movies;
    private OnMovieClickListener listener;
    
    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }
    
    public MovieAdapter(List<Movie> movies, OnMovieClickListener listener) {
        this.movies = movies;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie, listener);
    }
    
    @Override
    public int getItemCount() {
        return movies.size();
    }
    
    public void updateData(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }
    
    static class MovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPoster;
        private TextView tvTitle, tvGenre, tvRating;
        
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_poster);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvGenre = itemView.findViewById(R.id.tv_genre);
            tvRating = itemView.findViewById(R.id.tv_rating);
        }
        
        public void bind(Movie movie, OnMovieClickListener listener) {
            ImageLoader.loadImageWithPlaceholder(ivPoster, movie.getPosterUrl(),
                    R.drawable.ic_launcher_foreground);
            tvTitle.setText(movie.getTitle() != null ? movie.getTitle() : "N/A");
            String genre = movie.getGenre();
            tvGenre.setText(genre != null && !genre.isEmpty() ? genre : "");
            // API không có field rating -> ẩn hoặc hiện cố định
            tvRating.setVisibility(android.view.View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMovieClick(movie);
                }
            });
        }
    }
}
