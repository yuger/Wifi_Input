/**
 * WiFi Keyboard - Remote Keyboard for Android.
 * Copyright (C) 2011 Ivan Volosyuk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.wxy.wifiinput;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class HttpService extends Service {
    RemoteKeyListener listener;
    PortUpdateListener portUpdateListener;
    String htmlpage;
    int port;
    static KeyboardHttpServer server;
    private static boolean isRunning = false;
    private IntentFilter mWifiStateFilter;
    private PhoneStateListener dataListener = new PhoneStateListener() {
        @Override
        public void onDataConnectionStateChanged(int state) {
            super.onDataConnectionStateChanged(state);
            updateNotification(false);
        }
    };

    public HttpService() {
        mWifiStateFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWifiStateFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiStateFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
    }

    final IBinder mBinder = new RemoteKeyboard.Stub() {
        @Override
        public void registerKeyListener(final RemoteKeyListener listener) throws RemoteException {
            HttpService.this.listener = listener;
        }

        @Override
        public void unregisterKeyListener(final RemoteKeyListener listener) throws RemoteException {
            if (HttpService.this.listener == listener) {
                HttpService.this.listener = null;
            }
        }

        @Override
        public void startTextEdit(String content) throws RemoteException {
            if (server != null) server.notifyClient(content);
        }

        @Override
        public void stopTextEdit() throws RemoteException {
            if (server != null) server.notifyClient(null);
        }

        @Override
        public void setPortUpdateListener(PortUpdateListener listener) throws RemoteException {
            HttpService.this.portUpdateListener = listener;
            if (port != 0) {
                portUpdateListener.portUpdated(port);
            }
        }
    };

    private void updateNotification(boolean ticker) {
        ArrayList<String> addrs = WiFiInput.getNetworkAddresses();
        String addr = null;
        for (String newAddr : addrs) {
            if (addr == null || addr.contains("::")) {
                addr = "http://" + newAddr + ":" + port;
            }
        }
        if (addr == null) {
            addr = "Port: " + port;
        }
        String tickerText = addr + " - WiFiInput";
        Intent notificationIntent = new Intent(this, WiFiInput.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this).setSmallIcon(R.drawable.icon).setTicker(ticker ? tickerText : null).setShowWhen(true).setContentTitle(getString(R.string.app_name)).setContentText(addr).setContentIntent(contentIntent).build();
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mgr.notify(0, notification);
    }

    private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotification(false);
        }
    };

    private static ServerSocketChannel makeSocket(Context context) {
        ServerSocketChannel ch;
        SharedPreferences prefs = context.getSharedPreferences("port", MODE_PRIVATE);
        int savedPort = prefs.getInt("port", 7777);
        try {
            ch = ServerSocketChannel.open();
            ch.socket().setReuseAddress(true);
            ch.socket().bind(new java.net.InetSocketAddress(savedPort));
            return ch;
        } catch (IOException ignore) {
        }
        if (savedPort != 7777) {
            try {
                ch = ServerSocketChannel.open();
                ch.socket().setReuseAddress(true);
                ch.socket().bind(new java.net.InetSocketAddress(7777));
                return ch;
            } catch (IOException ignore) {
            }
        }
        for (int i = 1; i < 9; i++) {
            try {
                ch = ServerSocketChannel.open();
                ch.socket().setReuseAddress(true);
                ch.socket().bind(new java.net.InetSocketAddress(i * 1111));
                return ch;
            } catch (IOException ignore) {
            }
        }
        for (int i = 2; i < 64; i++) {
            try {
                ch = ServerSocketChannel.open();
                ch.socket().setReuseAddress(true);
                ch.socket().bind(new java.net.InetSocketAddress(i * 1000));
                return ch;
            } catch (IOException ignore) {
            }
        }
        try {
            ch = ServerSocketChannel.open();
            ch.socket().setReuseAddress(true);
            ch.socket().bind(new java.net.InetSocketAddress(7777));
            return ch;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (isRunning) return;
        registerReceiver(mWifiStateReceiver, mWifiStateFilter);
        TelephonyManager t = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        t.listen(dataListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        InputStream is = getResources().openRawResource(R.raw.key);
        int pagesize = 32768;
        byte[] data = new byte[pagesize];
        StringBuilder page;
        try {
            int offset = 0;
            while (true) {
                int r = is.read(data, offset, pagesize - offset);
                if (r < 0) break;
                offset += r;
                if (offset >= pagesize) throw new IOException("page is too large to load");
            }
            page = new StringBuilder();
            page.append(new String(data, 0, offset));
        } catch (IOException e) {
            throw new RuntimeException("failed to load html page", e);
        }
        while (true) {
            int pos = page.indexOf("$");
            if (pos == -1) break;
            int res = Integer.parseInt(page.substring(pos + 1, pos + 9), 16);
            page.replace(pos, pos + 9, getString(res));
        }
        htmlpage = page.toString();
        startServer(this);
    }

    private static void removeNotification(Context context) {
        NotificationManager mgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mgr.cancelAll();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        onServerFinish = null;
        server.finish();
        unregisterReceiver(mWifiStateReceiver);
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tm.listen(dataListener, PhoneStateListener.LISTEN_NONE);
        removeNotification(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private static Runnable onServerFinish = null;

    public static void doStartServer(HttpService context) {
        ServerSocketChannel socket = makeSocket(context);
        context.port = socket.socket().getLocalPort();
        server = new KeyboardHttpServer(context, socket);
        Editor editor = context.getSharedPreferences("port", MODE_PRIVATE).edit();
        editor.putInt("port", context.port);
        editor.apply();
        try {
            if (context.portUpdateListener != null) {
                context.portUpdateListener.portUpdated(context.port);
            }
        } catch (RemoteException ignore) {
        }
        context.updateNotification(true);
        server.start();
    }

    public static void startServer(final HttpService context) {
        if (server == null) {
            doStartServer(context);
        } else {
            onServerFinish = new Runnable() {
                @Override
                public void run() {
                    doStartServer(context);
                }
            };
        }
    }

    public void networkServerFinished() {
        server = null;
        if (onServerFinish != null) {
            onServerFinish.run();
        }
    }
}
