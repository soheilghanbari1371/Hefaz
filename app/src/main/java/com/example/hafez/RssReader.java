package com.example.hafez;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class RssReader extends AppCompatActivity {

    ListView lvRss;
    ArrayList<String> titles;
    ArrayList<String> links;
    ArrayList<NewsInformation> news =new ArrayList<>();
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_reader);
        lvRss = (ListView) findViewById(R.id.lvRss);

        titles = new ArrayList<String>();
        links = new ArrayList<String>();
        db = new DatabaseHandler(this);


        if(isNetworkAvailable(this)){
            new ProcessInBackground().execute();

        }else {
            try {
                db.open();
                ArrayList<NewsInformation> news = db.getAllNews();
                CustomAdapter adapter = new CustomAdapter(this,news);
                lvRss.setAdapter(adapter);
                db.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }


    }

    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception> {
        ProgressDialog progressDialog = new ProgressDialog(RssReader.this);

        Exception exception = null;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Fetching Latest News!");
            progressDialog.show();

        }

        @Override
        protected Exception doInBackground(Integer... integers) {
            try {
                // rss feed site here
                URL url = new URL("https://moxie.foxnews.com/feedburner/world.xml");

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                Document doc = db.parse(getInputStream(url));

                doc.getDocumentElement().normalize();
                NodeList nodeList = doc.getElementsByTagName("item");
                //// fetch every node item in RSS and create news_item list
                for (int i = 0; i < nodeList.getLength(); i++) {
                    NewsInformation news_item = new NewsInformation();
                    Node node = nodeList.item(i);
                    Element parentItem = (Element) node;

                    NodeList links = parentItem.getElementsByTagName("link");
                    Element element_link = (Element) links.item(0);
                    NodeList element_link_childNodes = element_link.getChildNodes();
                    news_item.link = element_link_childNodes.item(0).getNodeValue();


                    NodeList titles = parentItem.getElementsByTagName("title");
                    Element element_title = (Element) titles.item(0);
                    NodeList element_title_childNodes = element_title.getChildNodes();
                    news_item.title = element_title_childNodes.item(0).getNodeValue();


                    NodeList pubDates = parentItem.getElementsByTagName("pubDate");
                    Element element_pubDate = (Element) pubDates.item(0);
                    NodeList element_pubDate_childNodes = element_pubDate.getChildNodes();
                    news_item.pubdate = element_pubDate_childNodes.item(0).getNodeValue();


                    NodeList categorys = parentItem.getElementsByTagName("pubDate");
                    Element element_category = (Element) categorys.item(0);
                    NodeList element_category_childNodes = element_category.getChildNodes();
                    news_item.category = element_category_childNodes.item(0).getNodeValue();

                    news.add(news_item);

                   //////////////////////////////////////////////////////////////////////////////////////////////

                }


            } catch (MalformedURLException e) {
                exception = e;

            } catch (IOException e) {
                exception = e;
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);
            CustomAdapter adapter = new CustomAdapter(getBaseContext(),news);
            lvRss.setAdapter(adapter);

//////////// after fetching data insert to db///////////////////////////////////////////
                try {
                    db.open();
                    for (int i = 0; i < news.size(); ++i) {
                        db.insertNewsInfo(news.get(i));
                    }
                    db.close();

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
/////////////////////////////////////////////////////////////////////////////
            progressDialog.dismiss();

        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
