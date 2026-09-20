import React from 'react';
import { Autocomplete, TextField } from '@mui/material';

/**
 * ElvanPillAutocomplete — a self-contained pill-shaped Autocomplete.
 *
 * Uses variant="filled" but applies scoped sx overrides to fix
 * the padding conflict with the global MuiFilledInput theme,
 * ensuring the dropdown arrow and clear button are always visible.
 *
 * Drop-in replacement: pass any Autocomplete props plus
 * convenience `label` / `placeholder` / `textFieldSx`.
 */
export default function ElvanPillAutocomplete({
  label,
  placeholder,
  textFieldSx,
  textFieldProps,
  ...autocompleteProps
}: any) {
  // If the caller already provides renderInput, use it as-is.
  if (autocompleteProps.renderInput) {
    return <Autocomplete {...autocompleteProps} />;
  }

  return (
    <Autocomplete
      fullWidth
      size="small"
      {...autocompleteProps}
      renderInput={(params: any) => {
        // CRITICAL: merge slotProps so we don't clobber the Autocomplete's
        // internal input props (role="combobox", aria-*, ref, endAdornment).

        // In MUI v6, inputProps and InputLabelProps are passed via slotProps
        const mergedSlotProps = {
          ...params.slotProps,
          inputLabel: {
            ...params.slotProps?.inputLabel,
            ...params.InputLabelProps,
            ...(textFieldProps?.InputLabelProps || {}),
            ...(textFieldProps?.slotProps?.inputLabel || {}),
            shrink: true,
          },
          htmlInput: {
            ...params.slotProps?.htmlInput,
            ...params.inputProps,
            ...(textFieldProps?.inputProps || {}),
            ...(textFieldProps?.slotProps?.htmlInput || {})
          }
        };

        // Remove deprecated props to prevent React DOM errors
        const finalTextFieldProps = { ...textFieldProps };
        delete finalTextFieldProps.inputProps;
        delete finalTextFieldProps.InputLabelProps;

        return (
          <TextField
            {...params}
            variant="filled"
            fullWidth
            size="small"
            label={label}
            placeholder={placeholder}
            slotProps={mergedSlotProps}
            {...finalTextFieldProps}
            sx={{
              // Scoped overrides that fix the Autocomplete padding conflict
              '& .MuiFilledInput-root': {
                borderRadius: 50,
                bgcolor: 'action.hover',
                paddingRight: '48px !important',
                paddingTop: '0px !important',
                paddingBottom: '0px !important',
                '&.Mui-focused': {
                  bgcolor: 'action.selected',
                },
                '&::before, &::after': {
                  display: 'none',
                },
              },
              '& .MuiFilledInput-input': {
                padding: '12px 24px !important',
                paddingRight: '8px !important',
              },
              '& .MuiAutocomplete-endAdornment': {
                position: 'absolute',
                right: '8px !important',
                top: '50%',
                transform: 'translateY(-50%)',
                display: 'flex',
              },
              '& .MuiAutocomplete-clearIndicator, & .MuiAutocomplete-popupIndicator': {
                backgroundColor: 'transparent !important',
                boxShadow: 'none !important',
                border: 'none !important',
                padding: '4px !important',
                borderRadius: '50% !important',
                color: 'text.secondary',
                '@media (hover: hover)': { '&:hover': {
                  backgroundColor: 'rgba(128, 128, 128, 0.1) !important',
                } },
                '& .MuiSvgIcon-root': {
                  color: 'inherit',
                  fontSize: '1.25rem',
                }
              },
              ...textFieldSx,
            }}
          />
        );
      }}
    />
  );
}
