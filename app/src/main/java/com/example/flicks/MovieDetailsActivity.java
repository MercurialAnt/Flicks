package com.example.flicks;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.flicks.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.example.flicks.MainActivity.API_BASE_URL;
import static com.example.flicks.MainActivity.API_KEY_PARAM;

public class MovieDetailsActivity extends AppCompatActivity {

    // teh movie to display
    Movie movie;
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    ImageView ivTrailer;
    ImageView ivPlay;

    int movieId;
    public static final String TAG = "MovieDetailsActivity";
    AsyncHttpClient client;
    String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        tvTitle = findViewById(R.id.tvTitle);
        tvOverview = findViewById(R.id.tvOverview);
        rbVoteAverage = findViewById(R.id.rbVoteAverage);
        ivTrailer = findViewById(R.id.ivTrailer);
        ivPlay = findViewById(R.id.ivPlay);

        client = new AsyncHttpClient();

        // unwrap the movie passed in via intent, using its simple name as a key
        movie = Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'",movie.getTitle()));

        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        // determine the currunt orientation
        boolean isPotrait = MovieDetailsActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;



        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);
        String imageUrl = "https://image.tmdb.org/t/p/w780" + movie.getBackdropPath();

        Glide.with(getApplicationContext())
                .load(imageUrl)
                .bitmapTransform(new RoundedCornersTransformation(MovieDetailsActivity.this, 25, 0))
                .placeholder(R.drawable.flicks_backdrop_placeholder)
                .dontAnimate()
                .into(ivTrailer);
        movieId = movie.getId();
        getYoutubeVid();

        setUpImageViewListener();


    }

    private void setUpImageViewListener() {
        ivTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!videoId.isEmpty()) {
                    Intent i = new Intent( MovieDetailsActivity.this, MovieTrailerActivity.class);
                    i.putExtra("videoId", videoId);
                    Log.d("EH","I clicked it");
                    MovieDetailsActivity.this.startActivity(i);

                }

            }
        });
    }

    private void getYoutubeVid() {
        String url = API_BASE_URL + "/movie/" + movieId + "/videos";

        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));
        // execute a GET request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load the results into movies list
                try {
                    JSONArray results = response.getJSONArray("results");
                    videoId = results.getJSONObject(1).getString("key");
                    ivPlay.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    logError("Failed to retrieve trailer", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from now playing endpoint", throwable, true);
            }
        });
    }
    private void logError(String msg, Throwable error, boolean alertUser) {
        // always log error
        Log.e(TAG, msg, error);
        // alert the user to avoid silent errors
        if (alertUser) {
            // show a long toast with error msg
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    }
}
