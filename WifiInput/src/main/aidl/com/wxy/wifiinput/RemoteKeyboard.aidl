package com.wxy.wifiinput;

import com.wxy.wifiinput.RemoteKeyListener;
import com.wxy.wifiinput.PortUpdateListener;

interface RemoteKeyboard {
    void registerKeyListener(RemoteKeyListener listener);
    void unregisterKeyListener(RemoteKeyListener listener);
    void setPortUpdateListener(PortUpdateListener listener);
    void startTextEdit(String content);
    void stopTextEdit();
}
