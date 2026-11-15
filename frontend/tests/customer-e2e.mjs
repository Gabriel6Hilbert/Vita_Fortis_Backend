import assert from 'node:assert/strict'
import { mkdir } from 'node:fs/promises'
import { chromium } from 'playwright-core'

const base = process.env.QA_URL ?? 'http://127.0.0.1:5173'
const email = process.env.QA_EMAIL ?? 'cliente@vitafortis.test'
const password = process.env.QA_PASSWORD ?? 'Teste@123'
const confirmOrder = process.env.CONFIRM_ORDER === '1'
const output = process.env.QA_OUTPUT ?? '../output/customer-e2e'

await mkdir(output, { recursive: true })
const browser = await chromium.launch({ channel: 'chrome', headless: true })
const context = await browser.newContext({ viewport: { width: 1440, height: 900 } })
const page = await context.newPage()
page.setDefaultTimeout(20_000)

try {
  await page.goto(base + '/entrar', { waitUntil: 'domcontentloaded' })
  await page.getByLabel(/e-mail/i).fill(email)
  await page.locator('input[name="senha"]').fill(password)
  await page.locator('form').getByRole('button', { name: 'Entrar', exact: true }).click()
  await page.getByRole('link', { name: /minha conta/i }).waitFor()

  await page.goto(base + '/sacola', { waitUntil: 'domcontentloaded' })
  await page.waitForLoadState('networkidle').catch(() => undefined)
  if (await page.getByText('Sua sacola está vazia', { exact: true }).isVisible().catch(() => false)) {
    await page.goto(base + '/kits', { waitUntil: 'domcontentloaded' })
    const add = page.getByRole('button', { name: /Adicionar Kit Rotina Ativa à sacola/i })
    await add.waitFor()
    await add.click()
    await page.getByText(/adicionado|sacola/i).last().waitFor().catch(() => undefined)
    await page.goto(base + '/sacola', { waitUntil: 'domcontentloaded' })
  }

  const checkout = page.getByRole('heading', { name: 'Sacola e checkout' })
  await checkout.waitFor()
  await page.screenshot({ path: `${output}/01-sacola.png`, fullPage: true })
  await page.getByRole('button', { name: 'Revisar pedido' }).click()
  await page.getByText('Revisão final', { exact: true }).waitFor()
  await page.getByRole('button', { name: /Confirmar pedido/i }).waitFor()
  await page.screenshot({ path: `${output}/02-revisao.png`, fullPage: true })

  if (!confirmOrder) {
    console.log('PASSOU: login, sacola e revisão final. Confirmação do pedido não solicitada.')
    process.exitCode = 0
  } else {
    const before = page.url()
    await page.getByRole('button', { name: /Confirmar pedido/i }).click()
    await page.waitForURL(/\/pedidos\?novo=\d+/, { timeout: 30_000 })
    assert.notEqual(page.url(), before)
    const orderId = new URL(page.url()).searchParams.get('novo')
    assert.match(orderId ?? '', /^\d+$/)
    await page.screenshot({ path: `${output}/03-pedido-criado.png`, fullPage: true })
    console.log(`PASSOU: pedido simulado #${orderId} criado e exibido em Meus pedidos.`)
  }
} finally {
  await context.close()
  await browser.close()
}
