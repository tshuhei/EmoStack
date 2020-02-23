package shuhei.emostack;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HighStressActivity extends AppCompatActivity {

    private TextView scoreText;
    private TextView recommendation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_stress);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        scoreText = (TextView)findViewById(R.id.score_high_stress);
        recommendation = (TextView)findViewById(R.id.recommendation_high);

        Intent intent = this.getIntent();
        String score = intent.getStringExtra("score");

        scoreText.setText("Your stress score: " + String.format("%.0f",Double.parseDouble(score)) + "%");
        recommendation.setText("Recommendations:\n・Change the environment\n・Meditate\n・Do relaxation exercises\n・Express your feelings");
    }
}
