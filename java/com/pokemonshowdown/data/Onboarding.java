package com.pokemonshowdown.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.pokemonshowdown.application.MyApplication;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Onboarding {
    public final static String OTAG = Onboarding.class.getName();
    public static final String ANIM_HEADER = "animation";
    public static final String COOKIES_HEADER = "cookie";
    public static final String ADV_HEADER = "advertising";
    public static final String WARNING_HEADER = "warning";
    private static final String PROPERTIES_FILE = "showdown.properties";
    private static final String BUG_HEADER = "bugreport";

    private static final String SET_COOKIES_HEADER = "Set-Cookie";
    private static final String AUTH_COOKIE = "sid=";

    private final static String GET_LOGGED_IN = "Get logged in";
    private final static String VERIFY_USERNAME_REGISTERED = "Verify username registered";
    private final static String SIGNING_IN = "Signing in";
    private final static String SIGNING_OUT = "Signing out";
    private final static String REGISTER_USER = "Register user";

    private final static int TIME_OUT = 20;
    private static Onboarding sOnboarding;
    private Context mAppContext;
    private String mKeyId;
    private String mChallenge;
    private boolean isSignedIn;
    private String mUsername;
    private String mNamed;
    private String mAvatar;
    private boolean mAccountRegistered;

    //data to save : contains cookie + animation on/off
    private Properties mAppProperties;

    private Onboarding(Context appContext) {
        mAppContext = appContext;
        setSignedIn(false);
        setUsername(null);

        mAppProperties = null;
    }

    public static Onboarding get(Context c) {
        if (sOnboarding == null) {
            sOnboarding = new Onboarding(c.getApplicationContext());
            sOnboarding.loadProperties();
        }
        return sOnboarding;
    }


    private void loadProperties() {
        if (mAppProperties == null) {
            mAppProperties = new Properties();
        }
        try {
            FileInputStream fis = mAppContext.openFileInput(PROPERTIES_FILE);
            mAppProperties.load(fis);
        } catch (IOException e) {
            // no prop file, no problems
        }
    }

    private void saveProperties() {
        try {
            FileOutputStream fos = mAppContext.openFileOutput(PROPERTIES_FILE, Context.MODE_PRIVATE);
            mAppProperties.store(fos, "comment");
        } catch (IOException e) {
            // no prop file, no problems
        }
    }

    public String getAuthCookie() {
        return mAppProperties.getProperty(COOKIES_HEADER);
    }

    public void setAuthCookie(List<String> cookiesHeader) {
        if (mAppProperties == null) {
            mAppProperties = new Properties();
        }
        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                if (cookie.startsWith(AUTH_COOKIE)) {
                    mAppProperties.setProperty(COOKIES_HEADER, cookie.substring(0, cookie.indexOf(";") + 1));
                    saveProperties();
                    break;
                }
            }
        }
    }

    public String attemptSignIn() {
        SignIn signIn = new SignIn();

        String cookie = getAuthCookie();
        if (cookie != null) {
            signIn.execute(GET_LOGGED_IN, mKeyId, mChallenge, cookie);
            try {
                String result = signIn.get(TIME_OUT, TimeUnit.SECONDS);
                JSONObject resultJson = new JSONObject(result);
                if (!resultJson.getBoolean("loggedin")) {
                    return null;
                } else {
                    setAccountRegistered(true);
                    return resultJson.getString("username") + ",0," + resultJson.getString("assertion");
                }
            } catch (Exception e) {
                Log.e(OTAG, "attemptSignIn", e);
                return null;
            }
        } else {
            //no point in attempting to sign without cookies / username
            return null;
        }
    }

    public String verifyUsernameRegistered(String username) {
        SignIn signIn = new SignIn();
        signIn.execute(VERIFY_USERNAME_REGISTERED, username, mKeyId, mChallenge);
        try {
            return signIn.get(TIME_OUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(OTAG, "verifyUsernameRegistered", e);
            return null;
        }
    }

    public String signingIn(String username, String password) {
        SignIn signIn = new SignIn();
        signIn.execute(SIGNING_IN, username, password, mKeyId, mChallenge);
        try {
            String result = signIn.get(TIME_OUT, TimeUnit.SECONDS);
            JSONObject resultJson = new JSONObject(result);
            return resultJson.getString("assertion");
        } catch (Exception e) {
            Log.e(OTAG, "from signingIn", e);
        }
        return null;
    }

    public void signingOut() {
        SignIn signIn = new SignIn();
        signIn.execute(SIGNING_OUT, mUsername);

        setSignedIn(false);
        setUsername(null);
        setAvatar(null);
    }

    public String registerUser(String password) {
        SignIn signIn = new SignIn();
        // cmon zarel, work out a better captcha system
        signIn.execute(REGISTER_USER, mUsername, password, password, "pikachu", mKeyId, mChallenge);

        //TODO
        try {
            String result = signIn.get(TIME_OUT, TimeUnit.SECONDS);
            JSONObject resultJson = new JSONObject(result);
            return resultJson.toString();
        } catch (Exception e) {
            Log.e(OTAG, "from signingIn", e);
        }
        return null;
    }

    public boolean isAnimation() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        return sharedPref.getBoolean("pref_key_moves", true);
    }

    public boolean propertyExists(String propName) {
        return mAppProperties.getProperty(propName) != null;
    }

    public boolean isBugReporting() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        return sharedPref.getBoolean("pref_key_bugs", true);
    }

    public boolean isAdvertising() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        return sharedPref.getBoolean("pref_key_ad", true);
    }

    public void setAdvertising(boolean is) {
        mAppProperties.setProperty(ADV_HEADER, Boolean.toString(is));
        SharedPreferences.Editor sharedPref = PreferenceManager.getDefaultSharedPreferences(mAppContext).edit();
        sharedPref.putBoolean("pref_key_ad", is).apply();
        saveProperties();
    }

    public void setWarned() {
        mAppProperties.setProperty(WARNING_HEADER, Boolean.toString(true));
        saveProperties();
    }

    public boolean isAccountRegistered() {
        return mAccountRegistered;
    }

    public void setAccountRegistered(boolean accountRegistered) {
        this.mAccountRegistered = accountRegistered;
    }

    public boolean isSignedIn() {
        return isSignedIn;
    }

    public void setSignedIn(boolean isSignedIn) {
        this.isSignedIn = isSignedIn;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getKeyId() {
        return mKeyId;
    }

    public void setKeyId(String keyId) {
        mKeyId = keyId;
    }

    public String getChallenge() {
        return mChallenge;
    }

    public void setChallenge(String challenge) {
        mChallenge = challenge;
    }

    public String getNamed() {
        return mNamed;
    }

    public void setNamed(String named) {
        mNamed = named;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    private class SignIn extends AsyncTask<String, Void, String> {
        private String[] getUrlComponent = {"http://play.pokemonshowdown.com/~~showdown/action.php?act=upkeep&challengekeyid=", "&challenge="};
        private String[] postDataComponent = {"act=login&name=", "&pass=", "&challengekeyid=", "&challenge="};
        private String postUrl = "http://play.pokemonshowdown.com/~~showdown/action.php";
        private String[] verifyUsernameComponent = {"act=getassertion&userid=", "&challengekeyid=", "&challenge="};
        private String signOut = "act=logout&userid=";
        private String[] register = {"act=register&username=", "&password=", "&cpassword=", "&captcha=", "&challengekeyid=", "&challenge="};

        @Override
        protected String doInBackground(String... params) {
            String task = params[0];
            params[1] = MyApplication.toId(params[1]);
            switch (task) {
                case REGISTER_USER:
                    return registerUser(params[1], params[2], params[3], params[4], params[5], params[6]);
                case GET_LOGGED_IN:
                    return getLoggedIn(params[1], params[2], params[3]);
                case VERIFY_USERNAME_REGISTERED:
                    return verifyUsernameSignedIn(params[1], params[2], params[3]);
                case SIGNING_IN:
                    return signingIn(params[1], params[2], params[3], params[4]);
                case SIGNING_OUT:
                    return signingOut(params[0]);
            }
            return null;
        }

        private String registerUser(String username, String password, String cPassword, String captcha, String keyId, String challenge) {
            try {
                String postData = register[0] + username + register[1] + password + register[2] + cPassword + register[3] + captcha
                        + register[4] + keyId + register[5] + challenge;
                URL url = new URL(postUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());

                // Send request
                outStream.writeBytes(postData);
                outStream.flush();
                outStream.close();

                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));

                String output = bufferedReader.readLine();

                inputStream.close();

                Map<String, List<String>> headerFields = conn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(SET_COOKIES_HEADER);
                setAuthCookie(cookiesHeader);


                //TODO: verify that output from server actually start with ']'
                return output.substring(1);
            } catch (IOException e) {
                Log.e(OTAG, "from getLoggedIn", e);
                return null;
            }
        }

        private String getLoggedIn(String keyId, String challenge, String cookie) {
            try {
                String getUrl = getUrlComponent[0] + keyId + getUrlComponent[1] + challenge;
                URL url = new URL(getUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                if (cookie != null) {
                    conn.setRequestProperty(COOKIES_HEADER, cookie);
                }

                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));

                String output = bufferedReader.readLine();

                inputStream.close();

                //TODO: verify that output from server actually start with ']'
                return output.substring(1);
            } catch (IOException e) {
                Log.e(OTAG, "from getLoggedIn", e);
                return null;
            }
        }

        private String verifyUsernameSignedIn(String username, String keyId, String challenge) {
            try {
                String verifyData = verifyUsernameComponent[0] + username + verifyUsernameComponent[1] + keyId + verifyUsernameComponent[2] + challenge;
                String getUrlVerify = postUrl + "?" + verifyData;
                URL url = new URL(getUrlVerify);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));

                String output = bufferedReader.readLine();

                inputStream.close();

                return output;
            } catch (IOException e) {
                Log.e(OTAG, "from verifyUsernameSignedIn", e);
                return null;
            }
        }

        private String signingIn(String username, String password, String keyId, String challenge) {
            try {
                String postData = postDataComponent[0] + username + postDataComponent[1] + password + postDataComponent[2] + keyId + postDataComponent[3] + challenge;
                URL url = new URL(postUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());

                // Send request
                outStream.writeBytes(postData);
                outStream.flush();
                outStream.close();

                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));

                String output = bufferedReader.readLine();

                inputStream.close();

                Map<String, List<String>> headerFields = conn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(SET_COOKIES_HEADER);

                setAuthCookie(cookiesHeader);

                //TODO: verify that output from server actually start with ']'
                return output.substring(1);
            } catch (IOException e) {
                Log.e(OTAG, "from signingIn", e);
                return null;
            }
        }

        private String signingOut(String username) {
            mAppProperties.remove(COOKIES_HEADER);
            saveProperties();
            try {
                String postData = signOut + username;
                URL url = new URL(postUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());

                // Send request
                outStream.writeBytes(postData);
                outStream.flush();
                outStream.close();

                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));

                String output = bufferedReader.readLine();

                inputStream.close();

                return output;
            } catch (IOException e) {
                Log.e(OTAG, "from signingOut", e);
                return null;
            }
        }
    }
}
