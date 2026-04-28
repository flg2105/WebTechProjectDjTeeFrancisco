import { createReadStream, existsSync } from 'node:fs'
import { stat } from 'node:fs/promises'
import http from 'node:http'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const distDir = path.join(__dirname, 'dist')
const indexFile = path.join(distDir, 'index.html')
const port = Number(process.env.PORT || 8080)

const contentTypes = new Map([
  ['.css', 'text/css; charset=utf-8'],
  ['.html', 'text/html; charset=utf-8'],
  ['.ico', 'image/x-icon'],
  ['.js', 'text/javascript; charset=utf-8'],
  ['.json', 'application/json; charset=utf-8'],
  ['.map', 'application/json; charset=utf-8'],
  ['.png', 'image/png'],
  ['.svg', 'image/svg+xml'],
  ['.txt', 'text/plain; charset=utf-8'],
  ['.woff', 'font/woff'],
  ['.woff2', 'font/woff2']
])

function sendFile(res, filePath) {
  const ext = path.extname(filePath)
  const contentType = contentTypes.get(ext) || 'application/octet-stream'

  res.writeHead(200, { 'Content-Type': contentType })
  createReadStream(filePath).pipe(res)
}

const server = http.createServer(async (req, res) => {
  const requestPath = decodeURIComponent((req.url || '/').split('?')[0])
  const normalizedPath = requestPath === '/' ? '/index.html' : requestPath
  const assetPath = path.normalize(path.join(distDir, normalizedPath))

  // Prevent access outside the built frontend bundle.
  if (!assetPath.startsWith(distDir)) {
    res.writeHead(403)
    res.end('Forbidden')
    return
  }

  try {
    const fileStat = await stat(assetPath)
    if (fileStat.isFile()) {
      sendFile(res, assetPath)
      return
    }
  } catch {
    // Fall back to the SPA entry point below.
  }

  if (!existsSync(indexFile)) {
    res.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' })
    res.end('Frontend build output not found.')
    return
  }

  sendFile(res, indexFile)
})

server.listen(port, () => {
  console.log(`Frontend server listening on port ${port}`)
})
