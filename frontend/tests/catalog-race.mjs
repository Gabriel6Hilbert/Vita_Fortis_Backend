import assert from 'node:assert/strict'
import { chromium } from 'playwright-core'

const base = process.env.QA_URL ?? 'http://127.0.0.1:5173'
const browser = await chromium.launch({ channel: 'chrome' })
const failures = []
try {
  for (const [label, viewport, mobile] of [
    ['desktop', { width: 1440, height: 900 }, false],
    ['móvel', { width: 390, height: 844 }, true],
  ]) {
    const context = await browser.newContext({ viewport, isMobile: mobile, hasTouch: mobile })
    try {
      const page = await context.newPage()
      page.setDefaultTimeout(7000)
      const pending = new Map()
      const arrivals = new Map()
      const request = (key) => {
        if (pending.has(key)) return pending.get(key)
        return new Promise((resolve, reject) => {
          const timer = setTimeout(() => reject(new Error('Requisição não chegou: ' + key)), 7000)
          arrivals.set(key, response => { clearTimeout(timer); resolve(response) })
        })
      }
      await page.route('**/api/v1/**', async route => {
        const url = new URL(route.request().url())
        if (url.pathname !== '/api/v1/produtos' || url.searchParams.get('tamanho') !== '12') {
          await route.fulfill({ json: [] })
          return
        }
        const q = url.searchParams
        const key = [q.get('busca') ?? '', q.get('precoMax') ?? '', q.get('pagina')].join('|')
        await new Promise(done => {
          const respond = async ({ total = 1, number = 0, name = 'Whey Homologacao', status = 200 } = {}) => {
            try {
              const totalPages = Math.ceil(total / 12)
              await route.fulfill({ status, json: status === 200 ? {
                content: total && number < totalPages ? [{ id: 1, nome: name, marca: 'QA', ativo: true, precoFinal: 1, preco: 1, quantidadeEstoque: 20 }] : [],
                totalElements: total, totalPages, number, first: number === 0, last: number + 1 >= totalPages,
              } : { message: 'Erro obsoleto de QA' } })
            } finally { done() }
          }
          pending.set(key, respond)
          arrivals.get(key)?.(respond)
        })
      })
      const send = async (key, payload) => {
        const respond = await request(key)
        const settled = page.waitForEvent('requestfinished', { predicate: req => {
          const u = new URL(req.url())
          const q = u.searchParams
          return u.pathname === '/api/v1/produtos' && q.get('tamanho') === '12' &&
            [q.get('busca') ?? '', q.get('precoMax') ?? '', q.get('pagina')].join('|') === key
        } })
        pending.delete(key)
        arrivals.delete(key)
        await respond(payload)
        await settled
        await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))))
      }
      const count = page.locator('.result-count')
      const search = page.getByPlaceholder('Buscar produto, descrição ou marca')
      await page.goto(base + '/catalogo')
      await request('||0')
      await search.fill('Whey Homologacao')
      await send('Whey Homologacao||0')
      await page.getByText('1 produto encontrado', { exact: true }).waitFor()
      await page.getByRole('heading', { name: 'Whey Homologacao' }).waitFor()
      await send('||0', { total: 219, name: 'Resultado obsoleto' })
      assert.equal(await search.inputValue(), 'Whey Homologacao')
      assert.equal(await count.innerText(), '1 produto encontrado')

      await search.fill('W')
      await request('W||0')
      await search.fill('Wh')
      await request('Wh||0')
      await search.fill('Whe')
      await request('Whe||0')
      await send('Wh||0', { status: 503 })
      assert.equal(await page.locator('.catalog-results .state').innerText(), 'Carregando…')
      await send('Whe||0')
      await send('W||0', { status: 503 })
      await page.getByText('Whey Homologacao', { exact: true }).first().waitFor()

      if (mobile) await page.getByRole('button', { name: 'Filtrar', exact: true }).click()
      await page.getByPlaceholder('Máx.').fill('0.5')
      await request('Whe|0.5|0')
      await page.getByPlaceholder('Máx.').fill('2')
      await send('Whe|2|0')
      await send('Whe|0.5|0', { total: 0 })
      assert.equal(await count.innerText(), '1 produto encontrado')
      if (mobile) await page.getByRole('button', { name: 'Fechar', exact: true }).click()

      await search.fill('Whey')
      await request('Whey|2|0')
      await page.getByRole('button', { name: 'Limpar todos' }).click()
      // The original unfiltered response is now cached for this query.
      await page.getByText('219 produtos encontrados', { exact: true }).waitFor()
      await send('Whey|2|0')
      assert.equal(await search.inputValue(), '')
      assert.match(await count.innerText(), /^219 /)

      await page.getByRole('button', { name: 'Próxima' }).click()
      await request('||1')
      await search.fill('Homologacao')
      await send('Homologacao||0')
      await send('||1', { total: 1, number: 1 })
      assert.match(page.url(), /busca=Homologacao$/)
      await page.getByText('Página 1 de 1', { exact: true }).waitFor()
      await search.fill('')
      await page.getByText('219 produtos encontrados', { exact: true }).waitFor()
      await search.fill('Todos QA')
      await send('Todos QA||0', { total: 219 })
      await page.getByRole('button', { name: 'Próxima' }).click()
      await send('Todos QA||1', { total: 219, number: 1 })
      await page.getByText('Página 2 de 19', { exact: true }).waitFor()
      const rapidSearches = []
      page.on('request', req => {
        const url = new URL(req.url())
        const term = url.searchParams.get('busca')
        if (url.pathname === '/api/v1/produtos' && term?.startsWith('Rajada QA')) rapidSearches.push(term)
      })
      await search.fill('Rajada QA A')
      await search.fill('Rajada QA AB')
      await search.fill('Rajada QA ABC')
      await send('Rajada QA ABC||0')
      assert.deepEqual(rapidSearches, ['Rajada QA ABC'])
      console.log(`PASSOU Chrome ${label}: busca, carregamento, erros, filtros, limpeza e paginação`)
    } catch (error) {
      failures.push(error)
      console.error(`FALHOU Chrome ${label}: ${error.message}`)
    } finally { await context.close() }
  }
} finally { await browser.close() }
if (failures.length) process.exitCode = 1
