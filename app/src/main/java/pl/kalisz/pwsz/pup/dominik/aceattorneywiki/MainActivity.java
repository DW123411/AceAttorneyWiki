package pl.kalisz.pwsz.pup.dominik.aceattorneywiki;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener{
    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static String CURRENT_URL = "https://aceattorney.fandom.com/wiki/Ace_Attorney_Wiki";
    public static String CURRENT_TITLE = "Ace Attorney Wiki";
    public static final String PREFERENCES = "PREFERENCES_NAME";
    public static final String WEB_LINKS = "links";
    public static final String WEB_TITLE = "title";
    private Menu mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(getIntent().getExtras()!=null){
            Log.d("Test", "onCreate: extra: "+getIntent().getStringExtra("url"));
            if(getIntent().getStringExtra("url")!=null && getIntent().getStringExtra("url")!="") {
                CURRENT_URL = getIntent().getStringExtra("url");
            }
        }

        webView = (WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title){
                CURRENT_URL = view.getUrl();
                CURRENT_TITLE = title;
                SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                String links = sharedPreferences.getString(WEB_LINKS, null);

                if(mainMenu!=null) {
                    if (links != null) {
                        Gson gson = new Gson();
                        ArrayList<String> linkList = gson.fromJson(links, new TypeToken<ArrayList<String>>() {
                        }.getType());
                        if (linkList.contains(CURRENT_URL)) {
                            mainMenu.getItem(0).setIcon(R.drawable.ic_action_favourite_true);
                        } else {
                            mainMenu.getItem(0).setIcon(R.drawable.ic_action_favourite_false);
                        }
                    } else {
                        mainMenu.getItem(0).setIcon(R.drawable.ic_action_favourite_false);
                    }
                }
                super.onReceivedTitle(view,title);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3");
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl(CURRENT_URL);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.mySwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        webView.reload();
                    }
                }
        );
    }

    @Override
    public void onBackPressed(){
        if(webView.canGoBack()){
            webView.goBack();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);

        mainMenu = menu;

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String links = sharedPreferences.getString(WEB_LINKS, null);

        if (links != null) {
            Gson gson = new Gson();
            ArrayList<String> linkList = gson.fromJson(links, new TypeToken<ArrayList<String>>() {}.getType());
            if (linkList.contains(CURRENT_URL)) {
                menu.getItem(0).setIcon(R.drawable.ic_action_favourite_true);
            } else {
                menu.getItem(0).setIcon(R.drawable.ic_action_favourite_false);
            }
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_action_favourite_false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.favourite){
            String message;
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
            String jsonLink = sharedPreferences.getString(WEB_LINKS, null);
            String jsonTitle = sharedPreferences.getString(WEB_TITLE, null);
            if (jsonLink != null && jsonTitle != null) {
                Gson gson = new Gson();
                ArrayList<String> linkList = gson.fromJson(jsonLink, new TypeToken<ArrayList<String>>() {}.getType());
                ArrayList<String> titleList = gson.fromJson(jsonTitle, new TypeToken<ArrayList<String>>() {}.getType());
                if (linkList.contains(CURRENT_URL)) {
                    linkList.remove(CURRENT_URL);
                    CURRENT_TITLE = webView.getTitle().trim();
                    titleList.remove(CURRENT_TITLE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                    editor.putString(WEB_TITLE, new Gson().toJson(titleList));
                    editor.apply();
                    message = "Favourite Removed";
                }else{
                    linkList.add(CURRENT_URL);
                    CURRENT_TITLE = webView.getTitle().trim();
                    titleList.add(CURRENT_TITLE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                    editor.putString(WEB_TITLE, new Gson().toJson(titleList));
                    editor.apply();
                    message = "Set As Favourite";
                }
            }else{
                ArrayList<String> linkList = new ArrayList<>();
                ArrayList<String> titleList = new ArrayList<>();
                linkList.add(CURRENT_URL);
                CURRENT_TITLE = webView.getTitle().trim();
                titleList.add(CURRENT_TITLE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                editor.putString(WEB_TITLE, new Gson().toJson(titleList));
                editor.apply();
                message = "Set As Favourite";
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.favourites){
            startActivity(new Intent(this, Favourites.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
