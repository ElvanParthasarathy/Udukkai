import React from 'react';
import { TextField, TextFieldProps, Autocomplete, AutocompleteProps, Grid } from '@mui/material';
import { useLanguage } from '../mozhi/LanguageContext';

export interface ElvanBilingualFieldProps {
  label: string;
  primaryLang?: string;
  secondaryLang?: string;
  isBilingual?: boolean;

  primaryValue: any;
  onPrimaryChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  
  secondaryValue?: any;
  onSecondaryChange?: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;

  disabled?: boolean;
  required?: boolean;
  multiline?: boolean;
  rows?: number;
  placeholder?: string;
  type?: string;

  // Render overrides
  renderPrimary?: (lang: string, suffix: string) => React.ReactNode;
  renderSecondary?: (lang: string, suffix: string) => React.ReactNode;
}

export default function ElvanBilingualField({
  label,
  primaryLang = 'Tamil',
  secondaryLang = 'English',
  isBilingual = true,
  primaryValue,
  onPrimaryChange,
  secondaryValue,
  onSecondaryChange,
  disabled,
  required,
  multiline,
  rows,
  placeholder,
  type,
  renderPrimary,
  renderSecondary
}: ElvanBilingualFieldProps) {
  const { t } = useLanguage();
  
  const getTranslatedLang = (lang: string) => {
    const key = lang.toLowerCase();
    const translated = t(key as any);
    return translated !== key ? translated : lang;
  };

  const primaryLangSuffix = isBilingual ? ` (${getTranslatedLang(primaryLang)})` : '';
  const secondaryLangSuffix = isBilingual ? ` (${getTranslatedLang(secondaryLang)})` : '';

  return (
    <>
      <Grid size={{ xs: 12, sm: 6 }}>
        {renderPrimary ? (
          renderPrimary(primaryLang, primaryLangSuffix)
        ) : (
          <TextField
            disabled={disabled}
            required={required}
            fullWidth
            size="small"
            type={type}
            multiline={multiline}
            rows={rows}
            label={`${label}${primaryLangSuffix}`}
            value={primaryValue}
            onChange={onPrimaryChange}
            placeholder={placeholder}
          />
        )}
      </Grid>
      
      {isBilingual && (
        <Grid size={{ xs: 12, sm: 6 }}>
          {renderSecondary ? (
            renderSecondary(secondaryLang, secondaryLangSuffix)
          ) : (
            <TextField
              disabled={disabled}
              fullWidth
              size="small"
              type={type}
              multiline={multiline}
              rows={rows}
              label={`${label}${secondaryLangSuffix}`}
              value={secondaryValue}
              onChange={onSecondaryChange}
              placeholder={placeholder}
              sx={{ '& .MuiInputBase-root': disabled ? { bgcolor: 'action.hover' } : {} }}
            />
          )}
        </Grid>
      )}
    </>
  );
}
