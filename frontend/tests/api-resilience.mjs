import assert from 'node:assert/strict'
import { chromium } from 'playwright-core'

const base = process.env.QA_URL ?? 'http://127.0.0.1:5173'
const browser = await chromium.launch({ channel: 'chrome', headless: true })
const context = await browser.newContext({ viewport: { width: 1280, height: 800 } })
const page = await context.newPage()
page.setDefaultTimeout(15_000)
let attempts = 0

try {
  await page.route('**/api/v1/**', async route => {
    const url = new URL(route.request().url())
    if (url.pathname === '/api/v1/produtos' && url.searchParams.get('tamanho') === '12') {
      attempts += 1
      if (attempts === 1) {
        await route.fulfill({ status: 503, json: { mensagem: 'Render acordando' } })
        return
      }
      await route.fulfill({ json: {
        content: [{ id: 999, nome: 'Produto Recuperado', marca: 'QA', ativo: true, precoFinal: 10, preco: 10, quantidadeEstoque: 5 }],
        totalElements: 1, totalPages: 1, number: 0, first: true, last: true,
      } })
      return
    }
    await route.fulfill({ json: [] })
  })
  await page.goto(base + '/catalogo', { waitUntil: 'domcontentloaded' })
  await page.getByRole('heading', { name: 'Produto Recuperado', exact: true }).waitFor()
  assert.equal(attempts, 2)
  console.log('PASSOU: leitura 503 foi repetida e a vitrine se recuperou automaticamente.')
} finally {
  await context.close()
  await browser.close()
}
