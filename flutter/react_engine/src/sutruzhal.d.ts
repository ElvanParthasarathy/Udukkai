// @ts-nocheck
/// <reference types="vite/client" />

// vite-plugin-pwa virtual module
declare module 'virtual:pwa-register' {
  export function registerSW(options?: {
    immediate?: boolean;
    onNeedRefresh?: () => void;
    onOfflineReady?: () => void;
    onRegistered?: (registration: ServiceWorkerRegistration | undefined) => void;
    onRegisterError?: (error: any) => void;
  }): (reloadPage?: boolean) => Promise<void>;
}

// html2canvas – ships its own types but they can be incomplete
declare module 'html2canvas' {
  const html2canvas: (element: HTMLElement, options?: any) => Promise<HTMLCanvasElement>;
  export default html2canvas;
}

// qrcode
declare module 'qrcode' {
  export function toDataURL(text: string, options?: any): Promise<string>;
  export function toCanvas(canvas: HTMLCanvasElement, text: string, options?: any): Promise<void>;
  export function toString(text: string, options?: any): Promise<string>;
}

// dompurify
declare module 'dompurify' {
  const DOMPurify: {
    sanitize(source: string, config?: any): string;
    setConfig(config: any): void;
    clearConfig(): void;
    addHook(entryPoint: string, hookFunction: (...args: any[]) => any): void;
    removeHook(entryPoint: string): void;
    removeHooks(entryPoint: string): void;
    removeAllHooks(): void;
    isSupported: boolean;
  };
  export default DOMPurify;
}
