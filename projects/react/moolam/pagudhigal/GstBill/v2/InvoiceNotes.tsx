import React, { useRef, useEffect, useCallback } from 'react';
import { Box, Typography, Paper, Grid } from '@mui/material';
import { useLanguage } from '../../../mozhi/LanguageContext';
import DOMPurify from 'dompurify';

function RichEditor({ value, onChange, placeholder, toolbar = false }: any) {
  const { t } = useLanguage();
  const ref = useRef<HTMLDivElement>(null);
  const isInitialized = useRef(false);

  useEffect(() => {
    if (ref.current && !isInitialized.current) {
      ref.current.innerHTML = DOMPurify.sanitize(value || '');
      isInitialized.current = true;
    }
  }, []);

  useEffect(() => {
    if (ref.current && isInitialized.current && ref.current.innerHTML !== value) {
      ref.current.innerHTML = DOMPurify.sanitize(value || '');
    }
  }, [value]);

  const handleInput = useCallback(() => {
    if (ref.current) {
      onChange(ref.current.innerHTML);
    }
  }, [onChange]);

  const applyFormat = (cmd: string, val?: string) => {
    if (ref.current) ref.current.focus();
    document.execCommand(cmd, false, val);
    if (ref.current) onChange(ref.current.innerHTML);
  };
  
  const btnStyle = { 
    padding: '0.35rem 0.6rem', 
    fontSize: '0.85rem', 
    borderRadius: '6px', 
    border: 'none', 
    backgroundColor: 'rgba(128, 128, 128, 0.15)', 
    color: 'inherit',
    cursor: 'pointer', 
    minWidth: '32px',
    transition: 'background-color 0.2s'
  };

  return (
    <Box sx={{ width: '100%', position: 'relative' }}>
      {toolbar === 'basic' ? (
        <Box sx={{ display: 'flex', gap: '0.25rem', flexWrap: 'wrap', mb: 1, ml: '1.25rem' }}>
          <button type="button" onClick={() => applyFormat('bold')} title="Bold (Ctrl+B)" style={{ ...btnStyle, fontWeight: 700 }}>B</button>
          <button type="button" onClick={() => applyFormat('italic')} title="Italic (Ctrl+I)" style={{ ...btnStyle, fontStyle: 'italic' }}>I</button>
          <span style={{ width: 1, background: 'rgba(128, 128, 128, 0.3)', margin: '0 0.2rem' }} />
          <button type="button" onClick={() => applyFormat('insertUnorderedList')} title={t('hc_bulletList')} style={btnStyle}>•&nbsp;List</button>
        </Box>
      ) : toolbar ? (
        <Box sx={{ display: 'flex', gap: '0.25rem', flexWrap: 'wrap', mb: 1, ml: '1.25rem' }}>
          <button type="button" onClick={() => applyFormat('bold')} title="Bold (Ctrl+B)" style={{ ...btnStyle, fontWeight: 700 }}>B</button>
          <button type="button" onClick={() => applyFormat('italic')} title="Italic (Ctrl+I)" style={{ ...btnStyle, fontStyle: 'italic' }}>I</button>
          <button type="button" onClick={() => applyFormat('underline')} title="Underline (Ctrl+U)" style={{ ...btnStyle, textDecoration: 'underline' }}>U</button>
          <span style={{ width: 1, background: 'rgba(128, 128, 128, 0.3)', margin: '0 0.2rem' }} />
          <button type="button" onClick={() => applyFormat('insertUnorderedList')} title={t('hc_bulletList')} style={btnStyle}>•&nbsp;List</button>
          <button type="button" onClick={() => applyFormat('insertOrderedList')} title={t('hc_numberedList')} style={btnStyle}>1.&nbsp;List</button>
          <span style={{ width: 1, background: 'rgba(128, 128, 128, 0.3)', margin: '0 0.2rem' }} />
          <button type="button" onClick={() => applyFormat('formatBlock', '<h4>')} title={t('hc_heading')} style={{ ...btnStyle, fontWeight: 700, fontSize: '0.85rem' }}>H</button>
          <button type="button" onClick={() => applyFormat('formatBlock', '<p>')} title={t('hc_paragraph')} style={btnStyle}>¶</button>
          <button type="button" onClick={() => { const url = window.prompt('Link URL:'); if (url) applyFormat('createLink', url); }} title={t('hc_insertLink')} style={btnStyle}>🔗</button>
          <span style={{ width: 1, background: 'rgba(128, 128, 128, 0.3)', margin: '0 0.2rem' }} />
          <button type="button" onClick={() => applyFormat('removeFormat')} title={t('hc_clearFormatting')} style={btnStyle}>✕</button>
        </Box>
      ) : null}
      <Box component="div" ref={ref} contentEditable suppressContentEditableWarning
        onInput={handleInput}
        sx={{
          minHeight: '100px', whiteSpace: 'pre-wrap', width: '100%', padding: '0.8rem 1.25rem',
          border: 'none', borderRadius: 2, 
          bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.04)',
          fontFamily: 'inherit', fontSize: '0.9rem', color: 'text.primary', transition: 'all 0.2s',
          cursor: 'text', outline: 'none', lineHeight: 1.6,
          '&:empty::before': { content: 'attr(data-placeholder)', color: 'text.disabled', pointerEvents: 'none' },
          '&:focus': { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.09)' : 'rgba(0,0,0,0.06)' },
          '& ul, & ol': { marginLeft: '1.5rem', pl: 0, mt: 0.5, mb: 0.5 },
          '& table': { borderCollapse: 'collapse', width: '100%' },
          '& table th, & table td': { border: '1px solid', borderColor: 'divider', padding: '0.35rem 0.5rem' }
        }}
        data-placeholder={placeholder} />
    </Box>
  );
}

interface InvoiceNotesProps {
  customTerms: string;
  setCustomTerms: (terms: string) => void;
  internalNote: string;
  setInternalNote: (note: string) => void;
  showTerms: boolean;
  showNotes: boolean;
}

export default function InvoiceNotes({
  customTerms,
  setCustomTerms,
  internalNote,
  setInternalNote,
  showTerms,
  showNotes,
}: InvoiceNotesProps) {
  const { t } = useLanguage();

  if (!showTerms && !showNotes) return null;

  return (
    <Box sx={{ mb: 2 }}>
      <Grid container spacing={3}>
        {(showTerms || showNotes) && (
          <Grid size={{ xs: 12 }} sx={{ maxWidth: { xs: '100%', sm: 400 } }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, ml: '1.25rem' }}>
              <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px', mr: 1.5 }}>5</Box>
              <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>
                {showTerms && showNotes 
                  ? (t('notesAndTerms') || 'Notes / Terms') 
                  : showTerms 
                    ? (t('terms') || 'Terms & Conditions') 
                    : (t('notes') || 'Notes')}
              </Typography>
            </Box>
            <RichEditor 
              toolbar="basic" 
              value={customTerms} 
              onChange={setCustomTerms} 
              placeholder="E.g., Payment is due within 30 days. / Additional notes." 
            />
          </Grid>
        )}
      </Grid>
    </Box>
  );
}
