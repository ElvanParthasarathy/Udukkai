import { registerPlugin } from '@capacitor/core';

export interface NativeDocumentPlugin {
  sharePdf(options: { base64Data: string; filename: string }): Promise<void>;
  downloadPdf(options: { base64Data: string; filename: string; appMode: string; category: string }): Promise<void>;
  printPdf(options: { base64Data: string; filename: string }): Promise<void>;
  printHtml(options: { html: string, baseUrl?: string, filename?: string }): Promise<void>;
}

const NativeDocument = registerPlugin<NativeDocumentPlugin>('NativeDocument');

export default NativeDocument;
