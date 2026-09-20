export interface ClientState {
  id?: string;
  pin?: string;
  gstin?: string;
  [key: string]: any;
}

export interface LineItemState {
  id: string;
  isTemp?: boolean;
  productId?: string | null;
  hsn: string;
  rate: number;
  qty: number;
  unit: string;
  taxPercent: number;
  cessPercent?: number;
  discount: number;
  discountType?: 'percentage' | 'amount';
  [key: string]: any;
}


export interface InvoiceMetadataState {
  invoiceType: 'tax-invoice' | 'proforma';
  invoiceNumber: string;
  date: string;
  placeOfSupply: string;
}

export interface InvoiceTotalsState {
  subtotal: number;
  totalDiscount: number;
  cgst: number;
  sgst: number;
  igst: number;
  roundOff: number;
  tcsAmount: number;
  tdsAmount: number;
  total: number;
  netReceivable: number;
  amountPaid: number;
  globalDiscountValue?: number;
  globalDiscountType?: 'percentage' | 'amount';
  globalDiscountAmount?: number;
  itemDiscounts?: number;
}

export interface InvoiceSettingsState {
  taxInclusive: boolean;
  showDiscountColumn: boolean;
  showHSN: boolean;
  showPlaceOfSupply: boolean;
  showVehicle: boolean;
  showDueDate: boolean;
  showTerms: boolean;
  showBank: boolean;
  showNotes: boolean;
  showSignature: boolean;
  showItemizedTax?: boolean;
  showCess?: boolean;
  currency?: string;
  measureMode?: 'weight' | 'quantity';
}

export interface InvoiceDocumentState {
  client: ClientState;
  metadata: InvoiceMetadataState;
  items: LineItemState[];
  settings: InvoiceSettingsState;
  customTerms: string;
  internalNote: string;
}

export const createEmptyClient = (): ClientState => ({
  pin: '',
  gstin: '',
});

export const createEmptyLineItem = (): LineItemState => ({
  id: Date.now().toString() + Math.random().toString(36).substring(2),
  hsn: '',
  rate: 0,
  qty: 1,
  unit: 'Nos',
  taxPercent: 0,
  discount: 0,
  discountType: 'amount',
});


