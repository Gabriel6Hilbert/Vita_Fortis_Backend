import { execFileSync } from 'node:child_process'
import { readFileSync } from 'node:fs'
import { createHash } from 'node:crypto'
import assert from 'node:assert/strict'

// Verificação somente de leitura: não cria contas, pedidos ou imagens.
const base = process.env.VF_BASE_URL || 'https://vita-fortis-backend.onrender.com'
const commit = process.argv[2]
const get = path => {
  const data = execFileSync('curl', ['--silent', '--show-error', '--fail', '--max-time', '45', base + path], { maxBuffer: 5 * 1024 * 1024 })
  return data
}
const html = get('/').toString('utf8')
const local = readFileSync('src/main/resources/static/index.html', 'utf8')
const assets = [...local.matchAll(/(?:src|href)="(\/assets\/index-[^"]+\.(?:js|css))"/g)].map(match => match[1])
assert.equal(assets.length, 2)
for (const path of assets) {
  assert.ok(html.includes(path), 'Bundle ainda não publicado: ' + path)
  const hash = value => createHash('sha256').update(value).digest('hex')
  assert.equal(hash(get(path)), hash(readFileSync('src/main/resources/static' + path)), 'Conteúdo divergente: ' + path)
}
const version = JSON.parse(get('/api/v1/loja/versao').toString('utf8'))
if (commit) assert.equal(version.commit, commit)
const catalog = JSON.parse(get('/api/v1/produtos?tamanho=1').toString('utf8'))
assert.ok(catalog.totalElements > 0, 'Catálogo vazio')
console.log(JSON.stringify({ commit: version.commit, pagamento: version.pagamento, assets, produtosAtivos: catalog.totalElements, resultado: 'HTTP 200 e hashes confirmados' }, null, 2))
