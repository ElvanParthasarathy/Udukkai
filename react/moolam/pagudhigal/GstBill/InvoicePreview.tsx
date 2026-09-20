import React from 'react';
import SjsTheme from './SjsTheme';

const PattiyalMunnotam = React.forwardRef(({ profile = {}, client = {}, details = {}, items = [], totals = {}, invoiceType = 'tax-invoice', customTerms, options = {} }, ref) => {
  return <SjsTheme ref={ref} profile={profile} client={client} details={details} items={items} totals={totals} invoiceType={invoiceType} customTerms={customTerms} options={options} />;
});

export default PattiyalMunnotam;
