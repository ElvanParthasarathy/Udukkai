import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.elvan.udukkai.lite',
  appName: 'Udukkai',
  webDir: 'dist',
  plugins: {
    SplashScreen: {
      androidScaleType: 'FIT_CENTER',
      backgroundColor: '#ffffff'
    }
  }
};

export default config;
