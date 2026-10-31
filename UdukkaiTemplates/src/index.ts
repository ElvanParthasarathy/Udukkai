// The whole public surface of the templates. Importing this also loads the two stylesheets the papers need.
import './styles/vadivam.css';
import './styles/print.css';

export { default as GstInvoice } from './templates/GstInvoice';
export { default as CoolieInvoice } from './templates/CoolieInvoice';
export { GstReceipt, CoolieReceipt } from './templates/Receipt';
export { LanguageProvider } from './lib/mozhi/LanguageContext';
