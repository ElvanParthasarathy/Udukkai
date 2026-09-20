package com.elvan.udukkai.lite;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private View splashView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Register the custom plugins before calling super.onCreate
        registerPlugin(CustomSplashPlugin.class);
        registerPlugin(NativeDocumentPlugin.class);
        super.onCreate(savedInstanceState);

        // Determine if dark theme is active
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkTheme = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        // Create the Compose overlay using our Kotlin wrapper
        splashView = ComposeOverlay.INSTANCE.create(this, isDarkTheme);

        // Add it to the root content view so it floats over the Capacitor WebView
        addContentView(splashView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void hideSplash() {
        if (splashView != null) {
            // Fade out the splash screen smoothly
            splashView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        if (splashView.getParent() instanceof ViewGroup) {
                            ((ViewGroup) splashView.getParent()).removeView(splashView);
                        }
                        splashView = null;
                    })
                    .start();
        }
    }
}
