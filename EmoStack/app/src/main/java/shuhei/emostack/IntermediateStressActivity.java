package shuhei.emostack;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class IntermediateStressActivity extends AppCompatActivity {

    private TextView scoreText;
    private TextView recommendation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate_stress);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        scoreText = (TextView)findViewById(R.id.score_intermediate_stress);
        recommendation = (TextView)findViewById(R.id.recommendation_intermediate);

        Intent intent = this.getIntent();
        String score = intent.getStringExtra("score");

        scoreText.setText("Your stress score: " + String.format("%.0f",Double.parseDouble(score)) + "%");
        recommendation.setText("Recommendations:\n・Change the environment\n・Meditate\n・Do relaxation exercises\n・Express your feelings");
    }
}
