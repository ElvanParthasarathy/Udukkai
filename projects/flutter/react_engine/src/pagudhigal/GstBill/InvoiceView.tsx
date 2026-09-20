// @ts-nocheck
// InvoiceView — thin wrapper that uses PrintableViewWrapper for PDF/print/zoom
// and passes InvoicePreview or SjsTheme as the visual theme
import { INVOICE_TYPES } from '../../Payanpadu';
import { PrintableViewWrapper } from '../PrintableViewWrapper';
import InvoicePreview from './InvoicePreview';
import SjsTheme from './SjsTheme';

export default function InvoiceView({ bill, profile, onBack, onEdit, onDuplicate }) {
  if (!bill) return null;

  const data = bill.data || {};
  const typeConfig = INVOICE_TYPES[data.invoiceType || 'tax-invoice'] || INVOICE_TYPES['tax-invoice'];

  return (
    <PrintableViewWrapper
      data={data}
      typeConfig={typeConfig}
      onBack={onBack}
      onEdit={() => onEdit && onEdit(bill)}
    >
      {(printRef, mergedOptions) => (
        profile?.invoiceTheme === 'sjs' ? (
          <SjsTheme
            ref={printRef}
            profile={profile}
            client={data.client}
            details={data.details}
            items={data.items}
            totals={data.totals}
            invoiceType={data.invoiceType || 'tax-invoice'}
            customTerms={data.customTerms}
            options={mergedOptions}
          />
        ) : (
          <InvoicePreview
            ref={printRef}
            profile={profile}
            client={data.client}
            details={data.details}
            items={data.items}
            totals={data.totals}
            invoiceType={data.invoiceType || 'tax-invoice'}
            customTerms={data.customTerms}
            options={mergedOptions}
          />
        )
      )}
    </PrintableViewWrapper>
  );
}
