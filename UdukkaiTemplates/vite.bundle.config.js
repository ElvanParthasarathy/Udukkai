// The bundle an app loads in a WebView: index.html + assets + fonts, same layout as the old assets/print folder.
// Run: npm run bundle   (output: dist-bundle/)
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import fs from 'fs'
import path from 'path'

const root = path.resolve(import.meta.dirname, 'src/bridge')
const outDir = path.resolve(import.meta.dirname, 'dist-bundle')

// The app keys its saved PDFs on this, so a bundle with the same look never makes them rebuild.
const writeVersion = () => ({
  name: 'write-version',
  closeBundle() {
    const v = /'([^']+)'/.exec(fs.readFileSync(path.join(root, 'version.ts'), 'utf8'))[1]
    fs.writeFileSync(path.join(outDir, 'version.txt'), v)
  },
})

export default defineConfig({
  root,
  base: '/',
  publicDir: path.resolve(import.meta.dirname, 'public'),
  plugins: [react(), writeVersion()],
  build: { outDir, emptyOutDir: true, assetsInlineLimit: 0, chunkSizeWarningLimit: 4000 },
})
