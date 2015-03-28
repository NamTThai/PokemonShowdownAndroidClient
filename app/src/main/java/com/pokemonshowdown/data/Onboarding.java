package com.pokemonshowdown.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Onboarding {
    public final static String OTAG = Onboarding.class.getName();

    private static final String COOKIES_HEADER = "cookie";
    private static final String SET_COOKIES_HEADER = "Set-Cookie";
    private static final String AUTH_COOKIE = "sid=";
    private static final String COOKIE_FILE = "auth.cookie";


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
    private boolean accountRegistered;

    private Onboarding(Context appContext) {
        mAppContext = appContext;
        setSignedIn(false);
        setUsername(null);
    }

    public static Onboarding get(Context c) {
        if (sOnboarding == null) {
            sOnboarding = new Onboarding(c.getApplicationContext());
        }
        return sOnboarding;
    }

    public String attemptSignIn() {
        SignIn signIn = new SignIn();
        String cookie = null;
        try {
            FileInputStream fis = mAppContext.openFileInput(COOKIE_FILE);
            StringBuffer fileContent = new StringBuffer("");
            byte[] buffer = new byte[1024];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
            fis.close();
            cookie = fileContent.toString();
        } catch (FileNotFoundException e) {
            cookie = null;
        } catch (IOException e) {
            cookie = null;
        }

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


    public boolean isAccountRegistered() {
        return accountRegistered;
    }

    public void setAccountRegistered(boolean accountRegistered) {
        this.accountRegistered = accountRegistered;
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

                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        if (cookie.startsWith(AUTH_COOKIE)) {
                            // saving auth cookie
                            FileOutputStream fos = mAppContext.openFileOutput(COOKIE_FILE, Context.MODE_PRIVATE);
                            fos.write(cookie.substring(0, cookie.indexOf(";") + 1).getBytes());
                            fos.close();
                        }
                    }
                }

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

                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        if (cookie.startsWith(AUTH_COOKIE)) {
                            // saving auth cookie
                            FileOutputStream fos = mAppContext.openFileOutput(COOKIE_FILE, Context.MODE_PRIVATE);
                            fos.write(cookie.substring(0, cookie.indexOf(";") + 1).getBytes());
                            fos.close();
                        }
                    }
                }


                //TODO: verify that output from server actually start with ']'
                return output.substring(1);
            } catch (IOException e) {
                Log.e(OTAG, "from signingIn", e);
                return null;
            }
        }

        private String signingOut(String username) {
            mAppContext.deleteFile(COOKIE_FILE);
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
