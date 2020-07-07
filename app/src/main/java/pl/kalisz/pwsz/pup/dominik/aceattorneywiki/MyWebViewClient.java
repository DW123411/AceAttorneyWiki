package pl.kalisz.pwsz.pup.dominik.aceattorneywiki;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URL;

public class MyWebViewClient extends WebViewClient {
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon){
        super.onPageStarted(view,url,favicon);
        MainActivity.CURRENT_URL = url;
        MainActivity.CURRENT_TITLE = view.getTitle();
        //Log.d("Test", "onPageStarted: url: "+url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest url){
        try{
            URL urlObj = new URL(url.getUrl().toString());
            MainActivity.CURRENT_URL = url.getUrl().toString();
            if(TextUtils.equals(urlObj.getHost(),"aceattorney.fandom.com")){
                return false;
            }else{
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url.getUrl().toString()));
                view.getContext().startActivity(intent);
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url){
        super.onPageFinished(view,url);
        view.loadUrl(
                "javascript:(function() { "
                        +"var elements = document.getElementsByTagName('footer');"
                        +"var i;"
                        +"for(i=0;i<elements.length;i++){"
                            + "elements[i].style.display='none';"
                        +"}"
                        +"})()");
        view.loadUrl(
                "javascript:(function() { "
                        +"var elements = document.getElementsByClassName('global-footer-bottom__bar');"
                        +"var i;"
                        +"for(i=0;i<elements.length;i++){"
                            + "elements[i].style.display='none';"
                        +"}"
                        +"})()");
        swipeRefreshLayout = (SwipeRefreshLayout)view.getParent();
        swipeRefreshLayout.setRefreshing(false);
        MainActivity.CURRENT_URL = url;
        Log.d("Test", "onPageStarted: url: "+url);
    }
}
