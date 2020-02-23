package shuhei.emostack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class InitialActivity extends AppCompatActivity {

    private Button start;
    private Context initial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        start = (Button)findViewById(R.id.start);
        initial = this;

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent showIntent = new Intent(initial,LoginActivity.class);
                    startActivity(showIntent);
            }
        });
    }
}
