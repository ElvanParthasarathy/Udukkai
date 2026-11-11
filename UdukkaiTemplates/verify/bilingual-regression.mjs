import { createServer } from 'vite';
import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { readFileSync } from 'node:fs';
import assert from 'node:assert/strict';

let uiLanguage = 'en';
globalThis.localStorage = { getItem: () => uiLanguage };
const server = await createServer({ server: { middlewareMode: true }, appType: 'custom' });
try {
  const templates = await server.ssrLoadModule('/src/index.ts');
  const adapter = await server.ssrLoadModule('/src/bridge/adapter.ts');
  const language = await server.ssrLoadModule('/src/lib/dataLanguages.ts');
  const { invoiceAddressLines } = await server.ssrLoadModule('/src/lib/invoiceAddress.ts');
  assert.deepEqual(invoiceAddressLines({ street: 'Mill Village', town: 'Mill Village, Arani' }), ['Mill Village, Arani']);
  const cases = [
    ['gst-invoice', 'GstInvoice', 'toGstInvoiceProps'],
    ['coolie-invoice', 'CoolieInvoice', 'toCoolieBillProps'],
    ['gst-receipt', 'GstReceipt', 'toGstReceiptProps'],
    ['coolie-receipt', 'CoolieReceipt', 'toCoolieReceiptProps'],
  ];
  let checked = 0;
  for (const [doc, component, convert] of cases) {
    for (const primary of ['ta', 'en']) for (const bilingual of [false, true]) for (const ui of ['ta', 'en']) {
      uiLanguage = ui;
      const payload = JSON.parse(readFileSync(new URL(`./payload-${doc}.json`, import.meta.url), 'utf8'));
      Object.assign(payload.profile, { primaryLanguage: primary, secondaryLanguage: primary === 'ta' ? 'en' : 'ta', isBilingual: bilingual });
      payload.profile.printSettings = { ...payload.profile.printSettings, receiptLanguage: primary === 'ta' ? 'en' : 'ta' };
      const names = { ta: 'CUSTOMER_TAMIL_MARKER', en: 'CUSTOMER_ENGLISH_MARKER' };
      payload.profile.businessName = { ta: 'BUSINESS_TAMIL_MARKER', en: 'BUSINESS_ENGLISH_MARKER' };
      if (payload.customer) payload.customer.name = names;
      if (payload.invoice) payload.invoice.customerName = names;
      if (payload.receipt) payload.receipt.customerName = names;
      const markup = renderToStaticMarkup(React.createElement(templates.LanguageProvider, null,
        React.createElement(templates[component], adapter[convert](payload))));
      assert.ok(markup.includes(names[primary]) || bilingual, `${doc}/${primary}/${bilingual}: missing primary customer`);
      if (!bilingual) {
        const other = payload.profile.secondaryLanguage;
        assert.ok(!markup.includes(names[other]), `${doc}/${primary}: secondary customer leaked`);
        assert.ok(markup.includes(payload.profile.businessName[primary]), `${doc}/${primary}: wrong business language`);
        assert.ok(!markup.includes(payload.profile.businessName[other]), `${doc}/${primary}: secondary business leaked`);
      }
      checked++;
    }
  }
  for (const [doc, component, convert] of cases.slice(0, 2)) {
    const payload = JSON.parse(readFileSync(new URL(`./payload-${doc}.json`, import.meta.url), 'utf8'));
    Object.assign(payload.profile, { primaryLanguage: 'en', secondaryLanguage: 'ta', isBilingual: false });
    Object.assign(payload.profile, {
      address: { en: '25 Factory Road' }, city: { en: 'Arani' }, district: { en: 'Tiruvannamalai' },
      state: { en: 'Tamil Nadu' }, country: { en: 'India' }, pinCode: '632301',
    });
    Object.assign(payload.customer, {
      address: { en: '10 Mill Street' },
      city: { en: 'Coimbatore' },
      district: { en: 'Coimbatore' },
      state: { en: 'Tamil Nadu' },
      country: { en: 'India' },
      pinCode: '641001',
      fullAddress: { en: '10 Mill Street, Coimbatore, Tamil Nadu, 641001, India' },
    });
    const markup = renderToStaticMarkup(React.createElement(templates.LanguageProvider, null,
      React.createElement(templates[component], adapter[convert](payload))));
    assert.match(markup, /inv-party-details[^>]*>[\s\S]*?10 Mill Street[\s\S]*?Coimbatore[\s\S]*?Tamil Nadu[\s\S]*?India/,
      `${doc}: missing full customer address`);
    assert.match(markup, /<p>10 Mill Street, Coimbatore - 641001<\/p><p>Coimbatore District, Tamil Nadu, India<\/p>/);
    if (doc === 'coolie-invoice') {
      assert.match(markup, />25 Factory Road, Arani - 632301<\/div><div[^>]*>Tiruvannamalai District, Tamil Nadu, India<\/div>/);
      assert.match(markup, /class="inv-section-label" style="margin-bottom:3px">Bill To<\/h4>/);
      assert.match(markup, /class="inv-section-label" style="margin-bottom:3px">Financial Year<\/h4>/);
    }
  }
  assert.deepEqual(language.visibleDataValues({ fr: 'French', hi: 'Hindi' }, { primaryLanguage: 'hi', secondaryLanguage: 'fr', isBilingual: true }), ['Hindi', 'French']);
  assert.deepEqual(language.visibleDataValues({ fr: 'French', hi: 'Hindi' }, { primaryLanguage: 'fr', secondaryLanguage: 'hi', isBilingual: false }), ['French']);
  console.log(`PASS: ${checked} print-template configurations, both customer addresses and arbitrary language-code selection`);
} finally { await server.close(); }
