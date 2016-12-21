package seminar.android.fbmessenger;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ProgressDialog mProgressDialog;
    private WebView mWebView;
    private Map<String, String> cookies;
    private String lsd;
    private Intent openFriendsActivity;

    public static void printCookies(Map<String, String> cookies) {
        String conn = "Cookies:\n";
        conn += "-----------------------------------------------------------------\n";
        for (String s : cookies.keySet())
            conn += s+" = "+cookies.get(s)+"\n";
        conn += "-----------------------------------------------------------------\n";
        Log.e("Print Cookie:", conn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.customImeActionId || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        cookies = new ConcurrentHashMap<String, String>();
/*
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new MyBrowser());
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
*/
        Log.e("Error Log:", "Error log is OK\n");

        new TestConn().execute();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password, lsd);
            mAuthTask.checkForDeletedCookies();
            mAuthTask.addExtraCookies();
            mAuthTask.execute();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mLsd;
        private final String mEmail;
        private final String mPassword;
        private String conn = "POST cokies alala";
        private String myUrl = "https://m.facebook.com/login.php?refsrc=https%3A%2F%2Fm.facebook.com%2F&lwv=100";

        UserLoginTask(String email, String password, String lsd) {
            mLsd = lsd;
            mEmail = email;
            mPassword = password;

            mEmail.replace("@","%40");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                printCookies(cookies);
                Connection.Response response = Jsoup.connect(myUrl)
                        .data("lsd",mLsd)
                        .data("email",mEmail)
                        .data("pass",mPassword)
                        .method(Connection.Method.POST)
                        .cookies(cookies)
                        .execute();
                cookies = response.cookies();
                conn = response.statusCode()+" "+response.statusMessage()+"\n";
                for (String s : response.cookies().keySet())
                    conn += s+" = "+response.cookies().get(s)+"\n";
                conn += "\n";

                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //mAuthTask = null;
            showProgress(false);
            Log.e("POST Cookies:", conn);

            if (success) {
                Log.e("Result:", "Success");
                openFriendsActivity = new Intent(LoginActivity.this, FriendsActivity.class);
                Bundle httpLoginBundle = new Bundle();
                ArrayList<String> cookiesList = new ArrayList<String>();
                cookiesList.addAll(cookies.keySet());
                httpLoginBundle.putStringArrayList(getString(R.string.COOKIES_LIST), cookiesList);
                for (String cookie : cookies.keySet())
                    httpLoginBundle.putString(cookie, cookies.get(cookie));
                openFriendsActivity.putExtra(getString(R.string.HTTP_LOGIN), httpLoginBundle);
                startActivity(openFriendsActivity);
            } else {
                Log.e("Result","Failed");
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
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

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    // TestConn AsyncTask
    private class TestConn extends AsyncTask<Void, Void, Void> {
        private String myUrl = "https://m.facebook.com/login.php?refsrc=https%3A%2F%2Fm.facebook.com%2F&lwv=100";
        private String conn;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(LoginActivity.this);
            mProgressDialog.setTitle("Android Basic JSoup Tutorial");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Connect to the web site
                Connection.Response response = Jsoup.connect(myUrl).execute();
                Document document = response.parse();
                conn = ""+response.statusCode()+" "+response.statusMessage();
                Elements lsdValue = document.body()
                        .getElementById("root")
                        .getElementsByTag("form")
                        .select("input[name=\"lsd\"]");
                lsd = lsdValue.attr("value");
                conn += "\nvalue of lsd is:"+lsd+"\n\nGET Cookies:\n";
                cookies = response.cookies();
                for (String s : cookies.keySet())
                    conn += s+" = "+cookies.get(s)+"\n";
                conn += "\n";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.e("LSD and Cookies:", conn);
            mProgressDialog.dismiss();
        }
    }

    private class MyBrowser extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}

