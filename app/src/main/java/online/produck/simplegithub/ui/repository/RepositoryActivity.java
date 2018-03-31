package online.produck.simplegithub.ui.repository;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import online.produck.simplegithub.R;
import online.produck.simplegithub.api.GithubApi;
import online.produck.simplegithub.api.GithubApiProvider;
import online.produck.simplegithub.api.model.GithubRepo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RepositoryActivity extends AppCompatActivity {

    public static final String KEY_USER_LOGIN = "user_login";

    public static final String KEY_REPO_NAME = "repo_name";

    LinearLayout llConetent;

    ImageView ivProfile;

    TextView tvName;

    TextView tvStars;

    TextView tvDescription;

    TextView tvLanguage;

    TextView tvLastUpdates;

    ProgressBar pbProgress;

    TextView tvMessage;

    GithubApi api;

    Call<GithubRepo> repoCall;

    SimpleDateFormat dateFormatIntResponse = new SimpleDateFormat(
            "yyyy-MM-dd 'T' HH:mm:ssX", Locale.getDefault()
    );

    SimpleDateFormat dateFormatToShow = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repository);

        llConetent = findViewById(R.id.llActivityRepositoryContent);
        ivProfile = findViewById(R.id.ivActivityRepositoryProfile);
        tvName = findViewById(R.id.tvActivityRepositoryName);
        tvStars = findViewById(R.id.tvActivityRepositoryStars);
        tvDescription = findViewById(R.id.tvActivityRepositoryDescription);
        tvLanguage = findViewById(R.id.tvActivityRepositoryLanguage);
        tvLastUpdates = findViewById(R.id.tvActivityRepositoryLastUpdate);
        pbProgress = findViewById(R.id.pbActivityRepository);
        tvMessage = findViewById(R.id.tvActivityRepositoryMessage);

        api = GithubApiProvider.INSTANCE.provideGithubApi(this);

        String login = getIntent().getStringExtra(KEY_USER_LOGIN);
        if (null == login) {
            throw new IllegalArgumentException("No login info exists in extras");
        }
        String repo = getIntent().getStringExtra(KEY_REPO_NAME);
        if (null == repo) {
            throw new IllegalArgumentException("No repo info exists in extras");
        }

        showRepositoryInfo(login, repo);
    }

    private void showRepositoryInfo(String login, String repoName) {
        showProgress();

        repoCall = api.getRepository(login, repoName);
        repoCall.enqueue(new Callback<GithubRepo>() {

            @Override
            public void onResponse(@NonNull Call<GithubRepo> call, @NonNull Response<GithubRepo> response) {
                hideProgress(true);

                GithubRepo repo = response.body();
                if (response.isSuccessful() && null != repo) {
                    Glide.with(RepositoryActivity.this)
                            .load(repo.getOwner().getAvatarUrl())
                            .into(ivProfile);

                    tvName.setText(repo.getFullName());
                    tvStars.setText(getResources()
                        .getQuantityString(R.plurals.star, repo.getStars(), repo.getStars()));

                    if (null == repo.getDescription()) {
                        tvDescription.setText(R.string.no_description_provided);
                    } else {
                        tvDescription.setText(repo.getDescription());
                    }
                    if (null == repo.getLanguage()) {
                        tvLanguage.setText(R.string.no_language_specified);
                    } else {
                        tvLanguage.setText(repo.getLanguage());
                    }

                    try {
                        Date lastUpdate = dateFormatIntResponse.parse(repo.getUpdatedAt());
                        tvLastUpdates.setText(dateFormatToShow.format(lastUpdate));
                    } catch (ParseException e) {
                        tvLastUpdates.setText(getString(R.string.unknown));
                    }
                } else {
                    showError("Not successful " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GithubRepo> call, @NonNull Throwable t) {
                hideProgress(false);
                showError(t.getMessage());
            }
        } );
    }

    private void showProgress() {
        pbProgress.setVisibility(View.VISIBLE);
        llConetent.setVisibility(View.GONE);
    }

    private void hideProgress(boolean isSucceed) {
        pbProgress.setVisibility(View.GONE);
        llConetent.setVisibility(isSucceed ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        tvMessage.setText(message);
        tvMessage.setVisibility(View.VISIBLE);
    }
}
