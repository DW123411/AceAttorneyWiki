package pl.kalisz.pwsz.pup.dominik.aceattorneywiki;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Widget extends AppWidgetProvider {
    private int amount;
    private String text;
    private String textSource;
    private int checktext;

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        checktext = 0;
        try {
            int tmp = new WidgetUpdateCount().execute("").get();
            amount = tmp;
        }catch (Exception e){
            e.printStackTrace();
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent intentSync = new Intent(context, Widget.class);
        intentSync.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intentSync.putExtra( AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {appWidgetId} );
        PendingIntent pendingSync = PendingIntent.getBroadcast(context,0, intentSync, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_refresh,pendingSync);
        if(amount!=0) {
            //Dynamiczne dodawanie TextView
            /*RemoteViews view;
            view = new RemoteViews(context.getPackageName(), R.layout.widget_title);
            views.addView(R.id.widget_layout, view);
            int counter = 0;
            do {
                view = new RemoteViews(context.getPackageName(), R.layout.empty_textview_speaker);
                try {
                    String s = new WidgetUpdate().execute(counter).get();
                    text = s;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checktext++;
                Log.d("Test", "speaker: " + text);
                view.setTextViewText(R.id.empty_textview_speaker, text);
                views.addView(R.id.widget_layout, view);
                view = new RemoteViews(context.getPackageName(), R.layout.empty_textview_quote);
                try {
                    String s = new WidgetUpdate().execute(counter).get();
                    text = s;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checktext++;
                Log.d("Test", "quote: " + text);
                view.setTextViewText(R.id.empty_textview_quote, text);
                views.addView(R.id.widget_layout, view);
                view = new RemoteViews(context.getPackageName(), R.layout.empty_textview_link);
                try {
                    String s = new WidgetUpdate().execute(counter).get();
                    text = s;
                    textSource = s;
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(textSource!=null && !textSource.equals("")) {
                    view.setTextViewText(R.id.empty_textview_link, text);
                    views.addView(R.id.widget_layout, view);
                }
                counter++;
            }  while(textSource==null || textSource.equals(""));
            Log.d("Test","source: "+text);
            Log.d("Test", "updateAppWidget: checktext="+checktext);*/
            try {
                String s = new WidgetUpdate().execute(0).get();
                text = s;
            }catch (Exception e){
                e.printStackTrace();
            }
            checktext++;
            views.setTextViewText(R.id.textview_speaker,text);
            try {
                String s = new WidgetUpdate().execute(0).get();
                text = s;
            }catch (Exception e){
                e.printStackTrace();
            }
            checktext++;
            views.setTextViewText(R.id.textview_quote,text);
            try {
                String s = new WidgetUpdate().execute(0).get();
                text = s;
            }catch (Exception e){
                e.printStackTrace();
            }
            views.setTextViewText(R.id.textview_link,text);
            Intent intent = new Intent(context,MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
            views.setOnClickPendingIntent(R.id.textview_link,pendingIntent);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
        /*Intent intent = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.widget);
        views.setOnClickPendingIntent(R.id.link,pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId,views);*/
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /*@Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }*/

    private class WidgetUpdate extends AsyncTask<Integer, Integer, String>{
        @Override
        protected String doInBackground(Integer... integers){
            Document doc = null;
            try{
                doc = Jsoup.connect("https://aceattorney.fandom.com/wiki/Ace_Attorney_Wiki").get();
            }catch (IOException e){
                e.toString();
            }
            if(doc!=null) {
                Element divContainer = doc.selectFirst("div.quote");
                Elements characters = divContainer.select("table.speaker");
                Elements quotes = divContainer.select("table.quotetext");
                Element source = doc.select("div.source").first();//quotes.get(i).text()
                Log.d("Test","async checktext="+checktext);
                if(checktext%2==0 && checktext<=characters.size()){
                    //checktext++;
                    Log.d("Test",characters.get(integers[0]).text());
                    return characters.get(integers[0]).text();
                }else if(checktext%2!=0 && checktext<=quotes.size()){
                    //checktext++;
                    Log.d("Test",quotes.get(integers[0]).text());
                    return quotes.get(integers[0]).text();
                }else{
                    //checktext = 0;
                    Log.d("Test",source.text());
                    return source.text();
                }
            }
            return "";
        }

        /*@Override
        protected void onPostExecute(String string){
            text = string;
        }*/
    }

    private class WidgetUpdateCount extends AsyncTask<String, Integer, Integer>{
        @Override
        protected Integer doInBackground(String... strings){
            Document doc = null;
            try{
                doc = Jsoup.connect("https://aceattorney.fandom.com/wiki/Ace_Attorney_Wiki").get();
            }catch (IOException e){
                e.toString();
            }
            if(doc!=null){
                int size = 0;
                Element divContainer = doc.selectFirst("div.quote");
                Elements characters = divContainer.select("table.speaker");
                Elements quotes = divContainer.select("table.quotetext");
                size +=characters.size() + quotes.size() + 1;
                Log.d("Test", "updateAppWidget: characters.size="+size);
                return new Integer(size);
            }
            return new Integer(0);
        }

        /*@Override
        protected void onPostExecute(Integer integer){
            amount = integer;
        }*/
    }
}

