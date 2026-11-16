import assert from 'node:assert/strict'
import { mkdir } from 'node:fs/promises'
import { chromium } from 'playwright-core'

const base = process.env.QA_URL ?? 'http://127.0.0.1:5173'
const password = process.env.QA_PASSWORD ?? 'Teste@123'
const output = process.env.QA_OUTPUT ?? '../output/three-roles-e2e'
await mkdir(output, { recursive: true })

const browser = await chromium.launch({ channel: 'chrome', headless: true })
const login = async (email) => {
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } })
  const page = await context.newPage()
  page.setDefaultTimeout(25_000)
  await page.goto(base + '/entrar', { waitUntil: 'domcontentloaded' })
  await page.getByLabel(/e-mail/i).fill(email)
  await page.locator('input[name="senha"]').fill(password)
  await page.locator('form').getByRole('button', { name: 'Entrar', exact: true }).click()
  await page.waitForURL(url => !url.pathname.endsWith('/entrar'))
  return { context, page }
}

let client
let admin
let collaborator
try {
  client = await login('cliente@vitafortis.test')
  let orderId = process.env.QA_ORDER_ID
  if (!orderId) {
    await client.page.goto(base + '/catalogo?busca=HML-WHEY', { waitUntil: 'domcontentloaded' })
    await client.page.getByRole('heading', { name: 'Whey Homologacao', exact: true }).waitFor()
    await client.page.getByRole('button', { name: /Adicionar Whey Homologacao à sacola/i }).click()
    await client.page.getByRole('link', { name: /Sacola, \d+ itens/i }).waitFor()
    await client.page.goto(base + '/sacola', { waitUntil: 'domcontentloaded' })
    await client.page.getByRole('heading', { name: 'Sacola e checkout' }).waitFor()
    await client.page.getByPlaceholder('Digite o código').fill('COLAB10')
    await client.page.getByRole('button', { name: 'Aplicar', exact: true }).click()
    await client.page.getByText('Cupom aplicado.', { exact: true }).waitFor()
    await client.page.getByText(/cashback para Colaborador Homologacao/i).waitFor()
    await client.page.getByRole('button', { name: 'Revisar pedido' }).click()
    await client.page.getByText('Revisão final', { exact: true }).waitFor()
    await client.page.screenshot({ path: `${output}/01-cliente-revisao.png`, fullPage: true })
    await client.page.getByRole('button', { name: /Confirmar pedido/i }).click()
    await client.page.waitForURL(/\/pedidos\?novo=\d+/)
    orderId = new URL(client.page.url()).searchParams.get('novo') ?? undefined
  } else {
    await client.page.goto(base + `/pedidos?novo=${orderId}`, { waitUntil: 'domcontentloaded' })
  }
  assert.match(orderId ?? '', /^\d+$/)
  await client.page.getByText(new RegExp(`#${orderId}\\b`)).first().waitFor()
  await client.page.screenshot({ path: `${output}/02-cliente-pedido.png`, fullPage: true })

  admin = await login('admin@vitafortis.test')
  await admin.page.getByRole('button', { name: 'Pedidos', exact: true }).click()
  await admin.page.getByRole('heading', { name: 'Pedidos', exact: true }).waitFor()
  const orderTrigger = admin.page.getByRole('button', { name: `Ver detalhes do pedido número ${orderId}`, exact: true }).first()
  const row = admin.page.locator('tr').filter({ has: orderTrigger })
  await row.waitFor()
  const approve = row.getByRole('button', { name: 'Aprovar', exact: true })
  if (await approve.isVisible().catch(() => false)) {
    admin.page.once('dialog', dialog => dialog.accept())
    await approve.click()
    await admin.page.getByText('Pagamento aprovado.', { exact: true }).waitFor()
  }
  await row.getByRole('combobox', { name: `Status do pedido #${orderId}` }).waitFor()
  await admin.page.screenshot({ path: `${output}/03-admin-aprovado.png`, fullPage: true })

  collaborator = await login('colaborador@vitafortis.test')
  await collaborator.page.getByRole('heading', { name: /Olá, Colaborador/i }).waitFor()
  await collaborator.page.getByRole('heading', { name: 'Saldo disponível de cashback' }).waitFor()
  await collaborator.page.getByText(/COLAB10 · Ativo · Cashback: 5%/i).waitFor()
  await collaborator.page.getByRole('heading', { name: 'Extrato de cashback' }).waitFor()
  await collaborator.page.getByText(/Crédito/).first().waitFor()
  await collaborator.page.screenshot({ path: `${output}/04-colaborador-cashback.png`, fullPage: true })

  await client.page.reload({ waitUntil: 'domcontentloaded' })
  const clientOrder = client.page.locator('article, tr, section').filter({ hasText: `#${orderId}` }).first()
  await clientOrder.getByText(/Pagamento aprovado|Pagamento: aprovado/i).first().waitFor()
  await client.page.screenshot({ path: `${output}/05-cliente-aprovado.png`, fullPage: true })
  console.log(`PASSOU: cliente criou #${orderId}, admin aprovou e colaborador recebeu o cashback.`)
} finally {
  await Promise.allSettled([client?.context.close(), admin?.context.close(), collaborator?.context.close()])
  await browser.close()
}
