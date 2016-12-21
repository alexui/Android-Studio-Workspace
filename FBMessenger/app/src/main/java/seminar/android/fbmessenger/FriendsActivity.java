package seminar.android.fbmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsActivity extends AppCompatActivity {

    private FriendsListTask mFrListTask = null;

    private ProgressDialog mProgressDialog;
    private Intent openFriendsActivity;
    private Bundle httpLoginBundle;
    private TextView text;
    private Map<String, String> cookies;
    private ArrayList<Friend> friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        openFriendsActivity = getIntent();
        httpLoginBundle = openFriendsActivity.getBundleExtra(getString(R.string.HTTP_LOGIN));
        ArrayList<String> cookiesList = httpLoginBundle
                .getStringArrayList(getString(R.string.COOKIES_LIST));
        cookies = new ConcurrentHashMap<String, String>();
        for (String cookie : cookiesList)
            cookies.put(cookie, httpLoginBundle.getString(cookie));

        text = (TextView) findViewById(R.id.textHtml);
        friendsList = new ArrayList<Friend>();
        Log.e("Cook", "Friends");
        LoginActivity.printCookies(cookies);
        mFrListTask = new FriendsListTask();
        mFrListTask.checkForDeletedCookies();
        mFrListTask.addExtraCookies();
        mFrListTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FriendsListTask extends AsyncTask<Void, Void, Boolean> {

        private String connFriends;
        private String friendsUrl = "https://m.facebook.com/friends/center/friends/?ppk=0";
        private Document httpResp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(FriendsActivity.this);
            mProgressDialog.setTitle("Android Basic JSoup Tutorial");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                LoginActivity.printCookies(cookies);
                Connection.Response response = Jsoup.connect(friendsUrl)
                        .method(Connection.Method.GET)
                        .cookies(cookies)
                        .execute();
                cookies = response.cookies();
                connFriends = response.statusCode()+" "+response.statusMessage()+"\n";
                for (String s : response.cookies().keySet())
                    connFriends += s+" = "+response.cookies().get(s)+"\n";
                connFriends += "\n";

                Document document = response.parse();
                if (response.statusCode() == 200 || response.statusCode() == 302) {
                    httpResp = document;
                    return true;
                }
                else return false;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mProgressDialog.dismiss();
            int k = 0;
            Log.e("GET Friends Cookies:", connFriends);

            if (success) {
                Log.e("Result:", "Success");
                Elements friendsContent = httpResp
                        .body()
                        .getElementById("friends_center_main")
                        .select("div > div");

                Elements friendContent = friendsContent.eq(k);
                text.setText(friendContent.html());
            } else {
                Log.e("Result","Failed");
            }
        }

        private void checkForDeletedCookies() {
            boolean found = false;
            Set<String> cookiesSet = new HashSet<String>();
            cookiesSet.addAll(cookies.keySet());
            for (String cookie : cookiesSet) {
                if (cookies.get(cookie)
                        .compareToIgnoreCase(getString(R.string.deleted_cookie)) == 0) {
                    found = true;
                    cookies.remove(cookie);
                }
            }

            if (found) {
                String cookList = "\n";
                for (String s : cookies.keySet())
                    cookList += s+" = "+cookies.get(s)+"\n";
                cookList += "\n";
                Log.e("Found deleted cookie", cookList);
            }
        }

        private void addExtraCookies() {
            cookies.put("wd", getString(R.string.wd_cookie));
            cookies.put("m_pixel_ratio", getString(R.string.pixel_ratio_cookie));
        }

    }

    public class Friend {

        private String name;
        private String fbId;
        private String imgSrc;

        public Friend (String n, String fbid, String imgsrc) {
            name = n;
            fbId = fbid;
            imgSrc = imgsrc;
        }

        @Override
        public String toString() {
            String s = "[\n";
            s += name + "\n";
            s += fbId + "\n";
            s += imgSrc + "\n";
            s += "]\n";
            return super.toString();
        }
    }
}
