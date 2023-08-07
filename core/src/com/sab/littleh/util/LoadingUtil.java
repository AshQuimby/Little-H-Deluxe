package com.sab.littleh.util;

import com.sab.littleh.LittleH;
import com.sab.littleh.mainmenu.LoadingMenu;

public class LoadingUtil {
    public static void startLoading(Runnable loadedCode) {
        LittleH.program.dynamicCamera.reset();
        LittleH.program.switchMenu(new LoadingMenu());
        new Thread(loadedCode).start();
    }
}
