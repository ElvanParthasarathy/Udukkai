// @ts-nocheck
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { registerSW } from 'virtual:pwa-register'
import Seyali from './Seyali'
import './vadivam.css'

import { LanguageProvider } from './mozhi/LanguageContext'

import { BrowserRouter } from 'react-router-dom';

// Register service worker with auto-update
const updateSW = registerSW({
  onNeedRefresh() {
    // Auto-update when new content is available
    updateSW(true)
  },
  onOfflineReady() {
    console.log('App ready to work offline')
  },
})

// Global fix for stuck ripples on touch devices and desktop clicks
document.addEventListener('pointerup', (e) => {
  const target = e.target as HTMLElement;
  const button = target.closest('button, [role="button"], a, .MuiButtonBase-root');
  if (button) {
    // Delay blur slightly to allow the ripple animation to start fading out naturally
    setTimeout(() => {
      if (document.activeElement === button || button.contains(document.activeElement)) {
        (document.activeElement as HTMLElement).blur();
      }
    }, 250); 
  }
}, { passive: true });

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <LanguageProvider>
      <BrowserRouter>
        <Seyali />
      </BrowserRouter>
    </LanguageProvider>
  </StrictMode>,
)
