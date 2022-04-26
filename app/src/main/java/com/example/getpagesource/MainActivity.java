package com.example.getpagesource;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.getpagesource.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Get web page source code");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.protocol_list, R.layout.spinner_item);
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        binding.sourceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.urlText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "URL is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.show();
                new Thread(new Runnable() {
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                try {
                                    binding.sourceText.setText(getSourceFromURL(binding.spinner.getSelectedItem().toString()
                                            + binding.urlText.getText()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        dialog.dismiss();
                    }
                }).start();

                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }

    private String getSourceFromURL(String urlText) {
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

        try {
            System.out.println(urlText);
            URL url = new URL(urlText);
            URLConnection conn = null;
            conn = url.openConnection();
            String encoding = conn.getContentEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }
            BufferedReader buffer = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
            StringBuilder strBuilder = new StringBuilder();
            try {
                String str;
                while ((str = buffer.readLine()) != null) {
                    strBuilder.append(str);
                    strBuilder.append("\n");
                }
            } finally {
                buffer.close();
            }
            return strBuilder.toString();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            if (ex.getMessage().contains("Cleartext HTTP traffic") &&
                    ex.getMessage().contains("not permitted")) {
                return "Change to HTTPS!";
            } else if (ex.getMessage().contains("Unable to resolve host")) {
                return "Try another URL again!";
            }
            return "Some error occurred!";
        }
    }
}
