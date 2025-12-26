package ma.bs.myapplication;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import ma.bs.myapplication.databinding.ActivityMainBinding;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    // Hardcoded secrets in code
    private final String awsAccessKey = "AKIAFAKEACCESSKEYXYZ12345";
    private final String googleApiKey = "AIzaFakeGoogleApiKey98765";
    private final String hardcodedPassword = "password123";
    private final String fakePrivateKey = "-----BEGIN PRIVATE KEY-----\nMIIBVgIBADANBgkqh...\n-----END PRIVATE KEY-----";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Call insecure crypto functions and log results
        testWeakCryptography();

        // Make insecure network call
        new Thread(new Runnable() {
            @Override
            public void run() {
                trustAllSslCertificates();
                makeInsecureHttpRequest();
            }
        }).start();
    }

    private void testWeakCryptography() {
        Log.d("InsecureApp", "AWS Access Key: " + awsAccessKey);
        Log.d("InsecureApp", "Google API Key: " + googleApiKey);
        Log.d("InsecureApp", "Hardcoded Password: " + hardcodedPassword);
        Log.d("InsecureApp", "Fake Private Key: " + fakePrivateKey);

        String sampleText = "testInput";

        String md5Hash = CryptoUtils.md5(sampleText);
        Log.d("InsecureApp", "MD5 Hash of '" + sampleText + "': " + md5Hash);

        String sha1Hash = CryptoUtils.sha1(sampleText);
        Log.d("InsecureApp", "SHA-1 Hash of '" + sampleText + "': " + sha1Hash);

        String aesEncrypted = CryptoUtils.aesEcbEncrypt(sampleText);
        Log.d("InsecureApp", "AES ECB Encrypted text: " + aesEncrypted);

        String desEncrypted = CryptoUtils.desEncrypt(sampleText);
        Log.d("InsecureApp", "DES Encrypted text: " + desEncrypted);
    }

    private void makeInsecureHttpRequest() {
        try {
            URL url = new URL("http://insecure.example.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            Log.d("InsecureApp", "HTTP Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            Log.d("InsecureApp", "HTTP Response: " + response.toString());
        } catch (Exception e) {
            Log.d("InsecureApp", "Error during HTTP request: " + e.getMessage());
        }
    }

    // Trust manager that does not validate certificate chains
    private void trustAllSslCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) { return true; }
            });
        } catch (Exception e) {
            Log.d("InsecureApp", "Error trusting all SSL certificates: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
