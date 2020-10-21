package no.hiof.museum_finder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import no.hiof.museum_finder.model.MuseumDetailsApi;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toMuseumDetail(View view) {
        startActivity(new Intent(MainActivity.this, MuseumDetailsApi.class));
    }
}