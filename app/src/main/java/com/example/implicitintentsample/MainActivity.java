package com.example.implicitintentsample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private double latitude = 0;
    private double longitude = 0;

    // FusedLocationProviderClientオブジェクト
    private FusedLocationProviderClient fusedLocationProviderClient;

    // LocationRequestオブジェクト（FusedLocationProviderClientが位置情報を取得するにあたっての設定情報を格納するオブジェクト）
    private LocationRequest locationRequest;

    // 位置情報が変更された時の処理を行うコールバックオブジェクト
    private OnUpdateLocation onUpdateLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FusedLocationProviderClientオブジェクト取得
        // ※FusedLocationProviderClient = 位置情報の提供元プロバイダを自動選択するライブラリ
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        // 位置情報（LocationRequest）オブジェクト取得
        locationRequest = LocationRequest.create();

        // 位置情報の更新間隔設定
        locationRequest.setInterval(5000);

        // 位置情報の最短更新間隔設定
        locationRequest.setFastestInterval(1000);

        // 位置情報の取得精度設定
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // 位置情報が更新された場合の処理を行うコールバックオブジェクト生成
        onUpdateLocation = new OnUpdateLocation();
    }

    @Override
    protected void onResume() {

        super.onResume();

        // ACCESS_FINE_LOCATIONの許可が下りていない場合
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            // ACCESS_FINE_LOCATIONの許可を求めるダイアログを表示、その際のリクエストコードを1000に設定
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

            // 　第一引数：パーミッションダイアログを表示するアクティビティ
            // 　第二引数：許可を求めるパーミッション名の文字列配列
            // 　第三引数：リクエストコード
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1000);

            // onResume終了
            return;
        }

        /*
        * 位置情報の追跡はアプリの画面が表示されているとき（アプリがフォアグラウンド）に限定
        * 画面が表示される直前にメソッド（onResume）に以下のrequestLocationUpdatesを実装
        */

        // 位置情報の追跡開始
        // 　第一引数：位置情報（LocationRequest）オブジェクト
        // 　第二引数：位置情報更新時に実行されるコールバックオブジェクト
        // 　第三引数：コールバックオブジェクトを実行させるスレッドのLooperオブジェクト（コールバック処理を確実にUIスレッドで実行するため）
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, onUpdateLocation, Looper.getMainLooper());
    }

    @Override
    protected void onPause() {

        super.onPause();

        /*
         * 位置情報の追跡はアプリの画面が表示されているとき（アプリがフォアグラウンド）に限定
         * 画面が非表示後の最初のメソッド（onPause）に以下のremoveLocationUpdatesを実装
         */

        // 位置情報の追跡を停止
        fusedLocationProviderClient.removeLocationUpdates(onUpdateLocation);
    }

    /**
     * パーミッションダイアログの処理
     * @param requestCode リクエストコード
     * @param permissions パーミッション文字列配列
     * @param grantResults 各パーミッションリクエストに対してユーザが許可したかどうか格納された配列
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // ACCESS_FINE_LOCATIONに対するパーミッションダイアログで「許可」を選択
        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 再度、ACCESS_FINE_LOCATIONの許可が下りているか確認
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            // 位置情報の追跡を開始
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, onUpdateLocation, Looper.getMainLooper());
        }
    }


    public void onMapSearchButtonClick(View view) {

        // 入力欄の文字列取得
        EditText editText = findViewById(R.id.etSearchWord);
        String searchWord = String.valueOf(editText.getText());

        try {

            // 入力文字列をURLエンコード
            String encodeSearchWord = URLEncoder.encode(searchWord, "UTF-8");

            // マップアプリと連携するURI文字列を生成
            String urlStr = String.format("geo:0,0?q=%s", encodeSearchWord);

            // URI文字列からURIオブジェクトを生成
            Uri uri = Uri.parse(urlStr);

            // （暗黙的）Intentを生成しアクティビティを起動
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void onMapShowCurrentButtonClick(View view) {

        // マップアプリと連携するURI文字列を生成
        String urlStr = "geo:" + this.latitude + "," + this.longitude;

        // URI文字列からURIオブジェクトを生成
        Uri uri = Uri.parse(urlStr);

        // （暗黙的）Intentを生成しアクティビティを起動
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * 位置情報が変更された場合に処理を行うコールバッククラス
     */
    private class OnUpdateLocation extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            if (locationResult != null) {
                // 直近の位置情報を取得
                Location location = locationResult.getLastLocation();

                if (location != null) {

                    // locationオブジェクトから緯度経度を取得
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    // 画面表示
                    TextView etLatitude = findViewById(R.id.tvLatitude);
                    TextView etLongitude = findViewById(R.id.tvLongitude);
                    etLatitude.setText(Double.toString(latitude));
                    etLongitude.setText(Double.toString(longitude));
                }
            }

        }
    }
}