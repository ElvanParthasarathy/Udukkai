import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import fs from 'fs'
import path from 'path'

// Dev only: serves the frozen copy of the old shipped bundle at /reference/ so verify.html can compare against it.
const reference = () => ({
  name: 'serve-reference',
  configureServer(server) {
    const dir = path.resolve(import.meta.dirname, 'verify/reference')
    server.middlewares.use('/reference', (req, res, next) => {
      const file = path.join(dir, decodeURIComponent((req.url || '/').split('?')[0]).replace(/^\/$/, '/index.html'))
      if (!file.startsWith(dir) || !fs.existsSync(file)) return next()
      const types = { '.html': 'text/html', '.js': 'text/javascript', '.css': 'text/css' }
      res.setHeader('Content-Type', types[path.extname(file)] || 'application/octet-stream')
      res.end(fs.readFileSync(file))
    })
  },
})

export default defineConfig({
  plugins: [react(), reference()],
  server: { port: 5180 },
})
