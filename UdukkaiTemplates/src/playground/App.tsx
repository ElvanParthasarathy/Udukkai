import { useEffect, useState } from 'react';
import { GstInvoice, CoolieInvoice, GstReceipt, CoolieReceipt, LanguageProvider } from '../index';
import gstInvoice from './samples/gst-invoice.json';
import gstReceipt from './samples/gst-receipt.json';
import coolieInvoice from './samples/coolie-invoice.json';
import coolieReceipt from './samples/coolie-receipt.json';

// Each entry: the sample data (what the template takes as props) and how the template is called.
const TEMPLATES: Record<string, { label: string; sample: any; render: (p: any) => JSX.Element }> = {
  'gst-invoice': { label: 'GST invoice', sample: gstInvoice, render: (p) => <GstInvoice {...p} /> },
  'gst-receipt': { label: 'GST receipt', sample: gstReceipt, render: (p) => <GstReceipt receipt={p.receipt} profile={p.profile} /> },
  'coolie-invoice': { label: 'Coolie invoice', sample: coolieInvoice, render: (p) => <CoolieInvoice bill={p.bill} /> },
  'coolie-receipt': { label: 'Coolie receipt', sample: coolieReceipt, render: (p) => <CoolieReceipt receipt={p.receipt} /> },
};

const pretty = (v: any) => JSON.stringify(v, null, 2);

export default function App() {
  const [key, setKey] = useState('gst-invoice');
  const [lang, setLang] = useState<'ta' | 'en'>('ta');
  const [zoom, setZoom] = useState(0.8);
  const [text, setText] = useState(pretty(TEMPLATES[key].sample.props));
  const [props, setProps] = useState<any>(TEMPLATES[key].sample.props);
  const [error, setError] = useState('');

  // LanguageProvider reads the UI language from localStorage when it mounts.
  localStorage.setItem('elvanniril_language', lang);

  const pick = (k: string) => {
    setKey(k);
    setLang(TEMPLATES[k].sample.appLanguage === 'en' ? 'en' : 'ta');
    setText(pretty(TEMPLATES[k].sample.props));
    setProps(TEMPLATES[k].sample.props);
    setError('');
  };

  // Live: a valid edit re-renders the paper, an invalid one keeps the last good paper and shows why.
  useEffect(() => {
    const id = setTimeout(() => {
      try { setProps(JSON.parse(text)); setError(''); } catch (e: any) { setError(e.message); }
    }, 250);
    return () => clearTimeout(id);
  }, [text]);

  return (
    <div className="pg">
      <div className="pg-side">
        <div className="pg-row">
          <select value={key} onChange={(e) => pick(e.target.value)}>
            {Object.entries(TEMPLATES).map(([k, t]) => <option key={k} value={k}>{t.label}</option>)}
          </select>
          <select value={lang} onChange={(e) => setLang(e.target.value as 'ta' | 'en')}>
            <option value="ta">UI language: Tamil</option>
            <option value="en">UI language: English</option>
          </select>
        </div>
        <div className="pg-row">
          <label>Zoom <input type="range" min="0.3" max="1.2" step="0.05" value={zoom} onChange={(e) => setZoom(Number(e.target.value))} /> {Math.round(zoom * 100)}%</label>
          <button onClick={() => window.print()}>Print / Save PDF</button>
          <button onClick={() => pick(key)}>Reset sample</button>
        </div>
        <textarea spellCheck={false} value={text} onChange={(e) => setText(e.target.value)} />
        <div className="pg-err">{error}</div>
      </div>
      <div className="pg-view">
        <div className="pg-sheet invoice-paper print-wrapper" style={{ zoom }}>
          <LanguageProvider key={key + lang}>{TEMPLATES[key].render(props)}</LanguageProvider>
        </div>
      </div>
    </div>
  );
}
