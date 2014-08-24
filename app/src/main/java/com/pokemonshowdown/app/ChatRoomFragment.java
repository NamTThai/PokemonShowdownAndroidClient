package com.pokemonshowdown.app;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pokemonshowdown.data.NodeConnection;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;

public class ChatRoomFragment extends android.support.v4.app.Fragment {
    private final static String CTAG = "ChatRoomFragment";
    private static final String ROOM_NAME = "Room Name";

    private String mRoomName;
    private String mUserList;
    private String mChatLog;

    public static ChatRoomFragment newInstance(String roomName) {
        ChatRoomFragment fragment = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putString(ROOM_NAME, roomName);
        fragment.setArguments(args);
        return fragment;
    }
    public ChatRoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomName = getArguments().getString(ROOM_NAME);
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetRoomFromServer().execute();
        } else {
            Log.d(CTAG, "Check network connection");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class GetRoomFromServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                joinRoom(mRoomName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String joinRoom(final String roomName) throws IOException {

            try {
                Log.d(CTAG, "Initiating connection");

                URI uri = new URI("ws://nthai.cs.trincoll.edu:8000/showdown/websocket");

                WebSocketClient webSocketClient = new WebSocketClient(uri) {
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        Log.d(CTAG, "Opened");
                        send("|/join "+roomName);
                    }

                    @Override
                    public void onMessage(String s) {
                        Log.d(CTAG, s);
                        processServerOutput(s);
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        Log.d(CTAG, "Closed: code " + code + " reason " + reason + " remote " + remote);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(CTAG, "Error: " + e.toString());
                    }
                };
                webSocketClient.connect();
            } catch (Exception e) {
                Log.d(CTAG, e.toString());
            }
            return null;
        }

        private void processServerOutput(String output) {

        }
    }

    private class FormatChatRoomInput {

    }

}
