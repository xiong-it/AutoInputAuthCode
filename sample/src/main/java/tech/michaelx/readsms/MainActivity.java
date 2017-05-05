package tech.michaelx.readsms;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import tech.michaelx.authcode.AuthCode;
import tech.michaelx.authcode.CodeConfig;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handlePermission();

        findViewById(R.id.get_code_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CodeConfig config = new CodeConfig.Builder().codeLength(4).smsFromStart(133).build();
                AuthCode.getInstance().config(config).into((EditText) findViewById(R.id.code_et));
            }
        });

    }

    private void handlePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_SMS}, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length != 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "您阻止了app读取您的短信，你可以自己手动输入验证码", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
