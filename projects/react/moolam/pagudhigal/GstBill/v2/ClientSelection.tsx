import React, { useState, useEffect, useRef } from 'react';
import { Box, Typography, Paper, Grid, TextField, Button, List, ListItem, ListItemButton, ListItemText, Divider, IconButton, InputAdornment } from '@mui/material';
import { Plus, X } from '@phosphor-icons/react';
import { useLanguage } from '../../../mozhi/LanguageContext';
import { ClientState } from './InvoiceTypes';
import { getBilingualStateName, getBilingualCountryName } from '../../../Payanpadu';
import { getAllClients, saveClient } from '../../../Avanam';
import { thagaval } from '../../Thagaval';
import { getDynamicField } from '../../../Payanpadu';
import ElvanCard from '../../ElvanCard';

interface ClientSelectionProps {
  client: ClientState;
  setClient: (client: ClientState | ((prev: ClientState) => ClientState)) => void;
  isBilingual: boolean;
  primaryLang: string;
  secondaryLang: string;
  profile: any;
  invoiceOptions: any;
  onRequestAddClient?: () => void;
  dataVersion?: number;
}

export default function ClientSelection({
  client,
  setClient,
  isBilingual,
  primaryLang,
  secondaryLang,
  profile,
  invoiceOptions,
  onRequestAddClient,
  dataVersion,
}: ClientSelectionProps) {
  const { t } = useLanguage();
  
  const [savedClients, setSavedClients] = useState<any[]>([]);
  const [showClientSuggestions, setShowClientSuggestions] = useState(false);
  const [selectedClientId, setSelectedClientId] = useState<string | null>(null);
  
  const clientNameRef = useRef<HTMLInputElement>(null);
  const clientSuggestionsRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    getAllClients(setSavedClients).then(setSavedClients);
  }, []);

  useEffect(() => {
    if (dataVersion && dataVersion > 0) {
      getAllClients(setSavedClients).then(setSavedClients);
    }
  }, [dataVersion]);

  useEffect(() => {
    if (client?.id && client.id !== selectedClientId) {
      setSelectedClientId(client.id);
    }
  }, [client?.id]);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (clientSuggestionsRef.current && !clientSuggestionsRef.current.contains(e.target as Node) &&
          clientNameRef.current && !clientNameRef.current.contains(e.target as Node)) {
        setShowClientSuggestions(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const getClientField = (f: keyof ClientState, lang: string) => {
    const exact = client[`${f}_${lang}`];
    if (exact) return exact as string;
    if (lang === 'English' || lang === 'Tamil') return (client[f] || '') as string;
    return '';
  };

  const setClientField = (f: keyof ClientState, lang: string, v: string) => {
    setClient(prev => ({
      ...prev,
      [`${f}_${lang}`]: v
    }));
  };


  const selectSavedClient = (cli: any) => {
    setClient(cli);
    setSelectedClientId(cli.id);
    setShowClientSuggestions(false);
    thagaval(`Loaded client: ${cli.name}`, 'info');
  };

  const filteredClients = getClientField('name', primaryLang).trim()
    ? savedClients.filter(cli => {
        const cliName = getDynamicField(cli, 'name', profile, true);
        const cliNameSec = getDynamicField(cli, 'name', profile, false);
        const q = getClientField('name', primaryLang).trim().toLowerCase();
        return (cliName || '').toLowerCase().includes(q) || (cliNameSec || '').toLowerCase().includes(q);
      })
    : savedClients;

  return (
    <>
      <Box sx={{ py: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, ml: 1.5 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px' }}>1</Box>
            <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>{t('billedTo')}</Typography>
          </Box>
        </Box>

        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 5, lg: 4 }}>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', ml: 1.5, mb: 0.5 }}>
              {t('clientName')}{profile?.enableBilingual !== false ? ` (${profile?.primaryDataLanguage || 'Tamil'} & ${profile?.secondaryDataLanguage || 'English'})` : ''}
            </Typography>
            <Box sx={{ display: 'flex', gap: 1, flexDirection: 'column', mt: 0 }}>
              <Box sx={{ position: 'relative' }}>
                <TextField fullWidth size="small" label="" margin="none" sx={{ m: 0, '& .MuiInputBase-root': { mt: 0 } }} value={getClientField('name', primaryLang)} inputRef={clientNameRef}
                  onChange={(e) => {
                    const newVal = e.target.value;
                    if (selectedClientId) {
                      // Clear all other fields if modifying a previously selected client
                      setClient({ [`name_${primaryLang}`]: newVal } as any);
                    } else {
                      setClientField('name', primaryLang, newVal);
                    }
                    setSelectedClientId(null);
                    setShowClientSuggestions(true);
                  }}
                  onFocus={() => { if (savedClients.length > 0) setShowClientSuggestions(true); }}
                  placeholder={`${t("typeClientName")}${profile?.enableBilingual !== false ? ` (${profile?.primaryDataLanguage || 'Tamil'})` : ''}`} autoComplete="off" />
                
                {getClientField('name', primaryLang) && (
                  <IconButton 
                    size="small" 
                    onClick={() => {
                      setClient({} as any);
                      setSelectedClientId(null);
                      setShowClientSuggestions(false);
                      if (clientNameRef.current) clientNameRef.current.focus();
                    }}
                    sx={{ position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)', zIndex: 2 }}
                  >
                    <X size={18} weight="bold" />
                  </IconButton>
                )}

                {showClientSuggestions && (
                  <Paper elevation={4} sx={{ position: 'absolute', top: '100%', left: 0, right: 0, zIndex: 10, mt: 0.5, maxHeight: 300, overflow: 'auto' }} ref={clientSuggestionsRef}>
                    <List disablePadding>
                      {filteredClients.length > 0 ? filteredClients.map(cli => {
                        const cliName = getDynamicField(cli, 'name', profile, true);
                        const cliNameSec = getDynamicField(cli, 'name', profile, false);
                        
                        return (
                          <ListItem key={cli.id} disablePadding>
                            <ListItemButton onClick={() => selectSavedClient(cli)}>
                              <ListItemText 
                                primary={cliName} 
                                secondary={
                                  profile?.enableBilingual !== false && cliNameSec
                                    ? `${cliNameSec}${cli.oorEn ? ` · ${cli.oorEn}` : ''}`
                                    : [cli.oor || cli.mugavari?.substring(0, 30), typeof cli.maanilam === 'object' && cli.maanilam !== null ? cli.maanilam.primary : cli.maanilam].filter(Boolean).join(' · ')
                                }
                              />
                            </ListItemButton>
                          </ListItem>
                        );
                      }) : (
                        <ListItem>
                          <ListItemText primary={getClientField('name', primaryLang).trim() ? "No saved clients found." : "Type to search clients"} />
                        </ListItem>
                      )}
                      <Divider />
                      <ListItem disablePadding>
                        <ListItemButton 
                          onClick={() => {
                             setShowClientSuggestions(false);
                             if (onRequestAddClient) { onRequestAddClient(); } else { window.location.href = window.location.pathname + '?view=client-editor'; }
                          }} 
                          sx={{ color: 'primary.main' }}
                        >
                          <Plus size={18} weight="bold" style={{ marginRight: 8 }} />
                          <ListItemText primary={<Typography sx={{ fontWeight: 600 }}>{t('hc_addNewClient') || 'Add New Customer'}</Typography>} />
                        </ListItemButton>
                      </ListItem>
                    </List>
                  </Paper>
                )}
              </Box>
              {selectedClientId && (getClientField('mugavari', primaryLang) || getClientField('oor', primaryLang) || client.maanilam || client.gstin || getClientField('name', secondaryLang)) && (
                <ElvanCard sx={{ mt: 2 }} boxSx={{ p: 2 }}>
                  <Typography variant="overline" color="text.secondary" sx={{ fontWeight: 600, display: 'block', mb: 1 }}>{t('hc_savedClientDetails')}</Typography>

                  {/* 1. Business Name English */}
                  {getClientField('name', secondaryLang) && profile?.enableBilingual !== false && (
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                      {getClientField('name', secondaryLang)}
                    </Typography>
                  )}

                  {/* 2. Primary Language Address */}
                  <Box sx={{ mb: profile?.enableBilingual !== false ? 1 : 0 }}>
                    {getClientField('mugavari', primaryLang) && <Typography variant="body2">{getClientField('mugavari', primaryLang)}</Typography>}
                    {(getClientField('oor', primaryLang) || getClientField('maavattam', primaryLang) || client.pin) && (
                      <Typography variant="body2">
                        {[getClientField('oor', primaryLang), getClientField('maavattam', primaryLang), client.pin].filter(Boolean).join(' - ')}
                      </Typography>
                    )}
                    {client.maanilam && <Typography variant="body2">{getBilingualStateName(client.maanilam, { ...profile, returnOnlyPrimary: true })}</Typography>}
                    {client.country && <Typography variant="body2">{getBilingualCountryName(client.country, { ...profile, returnOnlyPrimary: true })}</Typography>}
                  </Box>

                  {/* 3. Secondary Language Address (Subtle) */}
                  {profile?.enableBilingual !== false && (getClientField('mugavari', secondaryLang) || getClientField('oor', secondaryLang) || getClientField('maavattam', secondaryLang)) && (
                    <Box sx={{ mb: 1, color: 'text.secondary' }}>
                      {getClientField('mugavari', secondaryLang) && <Typography variant="body2">{getClientField('mugavari', secondaryLang)}</Typography>}
                      {(getClientField('oor', secondaryLang) || getClientField('maavattam', secondaryLang) || client.pin) && (
                        <Typography variant="body2">
                          {[getClientField('oor', secondaryLang), getClientField('maavattam', secondaryLang), client.pin].filter(Boolean).join(' - ')}
                        </Typography>
                      )}
                      {client.maanilam && <Typography variant="body2">{getBilingualStateName(client.maanilam, { ...profile, returnOnlySecondary: true })}</Typography>}
                      {client.country && <Typography variant="body2">{getBilingualCountryName(client.country, { ...profile, returnOnlySecondary: true })}</Typography>}
                    </Box>
                  )}

                  {/* 4. GSTIN */}
                  {client.gstin && (
                    <Typography variant="body2" sx={{ mt: 0.5 }}>
                      GSTIN: <strong>{client.gstin}</strong>
                    </Typography>
                  )}
                </ElvanCard>
              )}
            </Box>
          </Grid>
        </Grid>
      </Box>
    </>
  );
}
