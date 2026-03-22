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
    private Long selectedMovieId = null;
    
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
                .inflate(R.layout.item_movie_booking, parent, false);
        return new MovieViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        boolean isSelected = movie.getId() != null && movie.getId().equals(selectedMovieId);
        holder.bind(movie, listener, isSelected);
    }
    
    @Override
    public int getItemCount() {
        return movies.size();
    }
    
    public void updateData(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    public void setSelectedMovieId(Long movieId) {
        this.selectedMovieId = movieId;
        notifyDataSetChanged();
    }
    
    static class MovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivMoviePoster;
        private TextView tvMovieTitle, tvMovieGenre, tvMovieDuration;
        private android.widget.Button btnSelectMovie;
        
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMoviePoster = itemView.findViewById(R.id.iv_movie_poster);
            tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
            tvMovieGenre = itemView.findViewById(R.id.tv_movie_genre);
            tvMovieDuration = itemView.findViewById(R.id.tv_movie_duration);
            btnSelectMovie = itemView.findViewById(R.id.btn_select_movie);
        }
        
        public void bind(Movie movie, OnMovieClickListener listener, boolean isSelected) {
            tvMovieTitle.setText(movie.getTitle());
            tvMovieGenre.setText(movie.getGenre());
            tvMovieDuration.setText(String.format("%d phút", movie.getDuration()));

            ImageLoader.loadImageWithPlaceholder(ivMoviePoster, movie.getPosterUrl(),
                    R.drawable.ic_launcher_foreground);

            btnSelectMovie.setSelected(isSelected);
            btnSelectMovie.setText(isSelected ? "Đã chọn" : "Chọn phim");

            View.OnClickListener clickListener = v -> {
                if (listener != null) {
                    listener.onMovieClick(movie);
                }
            };

            itemView.setOnClickListener(clickListener);
            btnSelectMovie.setOnClickListener(clickListener);
        }
    }
}
