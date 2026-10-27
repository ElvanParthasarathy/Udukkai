// Builds the print-only bundle consumed by the KMP app.
// Output: kmp/composeApp/src/androidMain/assets/print/ (index.html + assets + fonts)
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { nodePolyfills } from 'vite-plugin-node-polyfills'
import fs from 'fs'
import path from 'path'

const outDir = path.resolve(__dirname, '../kmp/composeApp/src/androidMain/assets/print')

// Only the fonts the global stylesheet (vadivam.css) actually references.
const PRINT_FONTS = [
  'ElvanSans-Regular.woff',
  'ElvanSans-Italic.woff',
  'ElvanSans-Medium.woff',
  'ElvanSans-MediumItalic.woff',
  'ElvanSans-SemiBold.woff',
  'ElvanSans-SemiBoldItalic.woff',
  'ElvanSans-Bold.woff',
  'ElvanSans-BoldItalic.woff',
]

const copyPrintFonts = () => ({
  name: 'copy-print-fonts',
  closeBundle() {
    const dest = path.join(outDir, 'fonts')
    fs.mkdirSync(dest, { recursive: true })
    for (const f of PRINT_FONTS) {
      fs.copyFileSync(path.resolve(__dirname, 'podhu/fonts', f), path.join(dest, f))
    }
  },
})

// The print views import moolam/Avanam.ts for company profiles; it talks to
// Firebase, so the bundle swaps it for print/shims/Avanam.ts (KMP supplies data).
// Forward slashes so Rollup sees the same module id as main.tsx's own import.
const avanamShim = path.resolve(__dirname, 'print/shims/Avanam.ts').split(path.sep).join('/')
const shimAvanam = () => ({
  name: 'shim-avanam',
  enforce: 'pre',
  resolveId(source, importer) {
    if (importer && /\/Avanam$/.test(source) && /[\\/]moolam[\\/]/.test(importer)) {
      return avanamShim
    }
    return null
  },
})

// Same idea for React's custom pinch-zoom hook: the WebView zooms natively instead.
const pinchShim = path.resolve(__dirname, 'print/shims/usePinchZoom.ts').split(path.sep).join('/')
const shimPinchZoom = () => ({
  name: 'shim-pinch-zoom',
  enforce: 'pre',
  resolveId(source, importer) {
    if (importer && /hooks\/usePinchZoom$/.test(source) && /[\\/]moolam[\\/]/.test(importer)) return pinchShim
    return null
  },
})

export default defineConfig({
  root: path.resolve(__dirname, 'print'),
  // Absolute base: the KMP WebView serves this folder at the origin root, so
  // the React stylesheet's url('/fonts/...') resolves unchanged.
  base: '/',
  publicDir: false,
  plugins: [
    nodePolyfills({
      include: ['stream', 'util', 'buffer'],
      globals: { Buffer: true, global: true, process: true },
    }),
    shimAvanam(),
    shimPinchZoom(),
    react(),
    copyPrintFonts(),
  ],
  build: {
    outDir,
    emptyOutDir: true,
    assetsInlineLimit: 0,
    chunkSizeWarningLimit: 4000,
  },
})
