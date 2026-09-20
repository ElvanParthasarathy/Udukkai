package com.elvan.udukkai.lite;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CustomSplash")
public class CustomSplashPlugin extends Plugin {
    @PluginMethod
    public void hide(PluginCall call) {
        if (getActivity() instanceof MainActivity) {
            getActivity().runOnUiThread(() -> {
                ((MainActivity) getActivity()).hideSplash();
                call.resolve();
            });
        } else {
            call.resolve();
        }
    }
}
