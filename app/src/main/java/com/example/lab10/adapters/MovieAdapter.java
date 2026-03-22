package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.Movie;
import com.example.lab10.utils.ImageLoader;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    
    private List<Movie> movies;
    private OnMovieClickListener listener;
    private Long selectedMovieId = null;
    private final int itemLayoutRes;
    private final boolean compactMode;
    
    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }
    
    public MovieAdapter(List<Movie> movies, OnMovieClickListener listener) {
        this(movies, listener, R.layout.item_movie_admin_simple, true);
    }

    public MovieAdapter(List<Movie> movies, OnMovieClickListener listener, int itemLayoutRes, boolean compactMode) {
        this.movies = movies;
        this.listener = listener;
        this.itemLayoutRes = itemLayoutRes;
        this.compactMode = compactMode;
    }
    
    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(itemLayoutRes, parent, false);
        return new MovieViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        boolean isSelected = movie.getId() != null && movie.getId().equals(selectedMovieId);
        holder.bind(movie, listener, isSelected, compactMode);
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
        private CardView cardMovie;
        
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMoviePoster = itemView.findViewById(R.id.iv_movie_poster);
            tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
            tvMovieGenre = itemView.findViewById(R.id.tv_movie_genre);
            tvMovieDuration = itemView.findViewById(R.id.tv_movie_duration);
            btnSelectMovie = itemView.findViewById(R.id.btn_select_movie);
            if (itemView instanceof CardView) {
                cardMovie = (CardView) itemView;
            }
        }
        
        public void bind(Movie movie, OnMovieClickListener listener, boolean isSelected, boolean compactMode) {
            tvMovieTitle.setText(movie.getTitle());

            if (tvMovieGenre != null) {
                if (compactMode) {
                    tvMovieGenre.setVisibility(View.GONE);
                } else {
                    tvMovieGenre.setVisibility(View.VISIBLE);
                    tvMovieGenre.setText(movie.getGenre());
                }
            }

            if (tvMovieDuration != null) {
                if (compactMode) {
                    tvMovieDuration.setVisibility(View.GONE);
                } else {
                    tvMovieDuration.setVisibility(View.VISIBLE);
                    Integer duration = movie.getDurationValue();
                    tvMovieDuration.setText(duration != null ? String.format("%d phút", duration) : "Chưa cập nhật");
                }
            }

            ImageLoader.loadImageWithPlaceholder(ivMoviePoster, movie.getPosterUrl(),
                    R.drawable.ic_launcher_foreground);

            if (cardMovie != null) {
                if (isSelected) {
                    cardMovie.setCardBackgroundColor(Color.parseColor("#E6F0FF"));
                    cardMovie.setCardElevation(6f);
                } else {
                    cardMovie.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                    cardMovie.setCardElevation(2f);
                }
            }

            if (btnSelectMovie != null) {
                if (compactMode) {
                    btnSelectMovie.setVisibility(View.GONE);
                } else {
                    btnSelectMovie.setVisibility(View.VISIBLE);
                    btnSelectMovie.setSelected(isSelected);
                    btnSelectMovie.setText(isSelected ? "Đã chọn" : "Chọn phim");
                }
            }

            View.OnClickListener clickListener = v -> {
                if (listener != null) {
                    listener.onMovieClick(movie);
                }
            };

            itemView.setOnClickListener(clickListener);
            if (btnSelectMovie != null && btnSelectMovie.getVisibility() == View.VISIBLE) {
                btnSelectMovie.setOnClickListener(clickListener);
            }
        }
    }
}
