import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [
    react()
  ],
  base: './',
  build: {
    rollupOptions: {
      input: {
        pattiyal: path.resolve(__dirname, 'pattiyal.html'),
        patrucheettu: path.resolve(__dirname, 'patrucheettu.html')
      },
      output: {
        manualChunks: {
          'vendor': ['react', 'react-dom', '@mui/material', '@emotion/react', '@emotion/styled', '@phosphor-icons/react'],
          'icons': ['lucide-react']
        }
      }
    }
  }
})
