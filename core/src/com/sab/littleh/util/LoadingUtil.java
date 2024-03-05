package com.sab.littleh.util;

import com.sab.littleh.LittleH;
import com.sab.littleh.screen.LoadingScreen;

public class LoadingUtil {
    public static void startLoading(Runnable loadedCode) {
        LittleH.program.dynamicCamera.reset();
        LittleH.program.switchScreen(new LoadingScreen());
        new Thread(loadedCode).start();
    }
}
