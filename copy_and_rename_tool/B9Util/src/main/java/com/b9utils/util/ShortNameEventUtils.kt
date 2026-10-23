package com.b9utils.util;

object ShortNameEventUtils {
    fun makeActivityShortName(activityName: String): String {
        var screen = activityName
        screen = screen.replace("Activity", "")
        screen = screen.replace("Dialog", "Dl")
        screen = screen.replace("BottomSheet", "Bsh")
        screen = screen.replace("Language", "Lang")
        screen = screen.replace("WelcomeBack", "Wcb")
        screen = screen.replace("AppUpdate", "upt")
        screen = screen.replace("Custom", "Ct")
        screen = screen.replace("Permission", "Per")
        screen = screen.replace("Update", "Upd")
        return screen
    }

    fun makeKeyShortName(longKey: String): String {
        var key = longKey
        key = key.replace("_", "")
        key = key.replace("fullscreen", "Fs")
        key = key.replace("screen", "Scr")
        key = key.replace("banner", "Bn")
        key = key.replace("collapsible", "Col")
        key = key.replace("adaptive", "Adapt")
        key = key.replace("bottom", "Bot")
        key = key.replace("splash", "Spl")
        key = key.replace("inter", "In")
        key = key.replace("native", "Nt")
        key = key.replace("language", "Lang")
        key = key.replace("welcomeback", "Wcb")
        key = key.replace("config", "Cf")
        key = key.replace("resume", "Rs")
        key = key.replace("update", "Upd")
        key = key.replace("exit", "Ex")
        key = key.replace("feedback", "fb")
        key = key.replace("permission", "per")
        key = key.replace("intro", "Int")
        key = key.replace("resume", "Rs")

        return key
    }
}