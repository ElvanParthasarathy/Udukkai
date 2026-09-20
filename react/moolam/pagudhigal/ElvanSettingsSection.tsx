import React from 'react';
import { Box, Typography, Button, Divider, Paper, ButtonBase, Collapse, Fade } from '@mui/material';
import { PencilSimple } from '@phosphor-icons/react';
import { useLanguage } from '../mozhi/LanguageContext';

export interface SettingsSectionProps {
  title?: string;
  description?: React.ReactNode;
  children: React.ReactNode;
  sx?: any;
  paperSx?: any;
}

export function SettingsSection({ title, description, children, sx, paperSx }: SettingsSectionProps) {
  return (
    <Box sx={{ mb: 3, ...sx }}>
      {title && (
        <Typography variant="subtitle2" sx={{ ml: 2, mb: 1, textTransform: 'uppercase', letterSpacing: 0.5, fontWeight: 600, color: 'text.secondary', fontSize: '0.75rem' }}>
          {title}
        </Typography>
      )}
      <Paper 
        elevation={0} 
        sx={{ 
          borderRadius: '24px', 
          overflow: 'hidden', 
          bgcolor: 'var(--mac-card-bg, #1c1c1e)',
          border: 'none',
          ...paperSx
        }}
      >
        {React.Children.toArray(children).filter(Boolean).map((child, index, array) => (
          <React.Fragment key={index}>
            {child}
            {index < array.length - 1 && <Divider sx={(theme) => ({ ml: '72px', mr: '20px', borderColor: theme.palette.mode === 'dark' ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.06)' })} />}
          </React.Fragment>
        ))}
      </Paper>
      {description && (
        <Typography variant="caption" sx={{ ml: 2, mt: 1, display: 'block', color: 'text.secondary' }}>
          {description}
        </Typography>
      )}
    </Box>
  );
}

export interface SettingsRowProps {
  icon?: React.ReactNode;
  iconColor?: 'blue' | 'purple' | 'orange' | 'green' | 'red' | string;
  title: string;
  description?: React.ReactNode;
  control?: React.ReactNode;
  onClick?: () => void;
  sx?: any;
}

export function SettingsRow({ icon, iconColor, title, description, control, onClick, sx }: SettingsRowProps) {
  const getIconBg = (color?: string) => {
    switch(color) {
      case 'blue': return '#2196F3';
      case 'purple': return '#9C27B0';
      case 'orange': return '#FF9800';
      case 'green': return '#4CAF50';
      case 'red': return '#F44336';
      case 'monochrome': return 'var(--mac-selection-hover)'; // Adapts to light/dark
      default: return 'var(--mac-selection-hover, rgba(255, 255, 255, 0.1))';
    }
  };

  const getIconColor = (color?: string) => {
    return color === 'monochrome' ? 'var(--mac-text)' : '#ffffff'; // Adapts to light/dark
  };

  const ContentBox = onClick ? ButtonBase : Box;

  return (
    <ContentBox 
      onClick={onClick}
      sx={{ 
        position: 'relative',
        display: 'flex', 
        alignItems: 'center', 
        width: '100%',
        justifyContent: 'flex-start',
        textAlign: 'left',
        p: '16px 20px',
        cursor: onClick ? 'pointer' : 'default',
        '@media (hover: hover)': {
          '&:hover': onClick ? { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'var(--mac-selection-hover, rgba(255, 255, 255, 0.05))' : 'action.hover' } : {},
        },
        transition: 'background-color 0.2s',
        ...sx
      }}
    >
      {icon && (
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: 36, height: 36, borderRadius: '50%', mr: 2, color: getIconColor(iconColor), bgcolor: getIconBg(iconColor), flexShrink: 0 }}>
          {icon}
        </Box>
      )}
      <Box sx={{ flexGrow: 1, minWidth: 0, pr: 2 }}>
        <Typography sx={{ fontWeight: 500, fontSize: '15px', color: 'var(--mac-text, #ffffff)', lineHeight: 1.3 }}>
          {title}
        </Typography>
        {description && (
          <Typography sx={{ mt: '2px', display: 'block', fontSize: '12px', color: 'var(--mac-text-secondary, #aaaaaa)' }}>
            {description}
          </Typography>
        )}
      </Box>
      {control && (
        <Box sx={{ flexShrink: 0, display: 'flex', alignItems: 'center' }}>
          {control}
        </Box>
      )}
    </ContentBox>
  );
}

// Keep the old component for backward compatibility if it's used elsewhere
export interface ElvanSettingsSectionProps {
  title?: string;
  action?: React.ReactNode;
  children: React.ReactNode;
  sx?: any;
}

export default function ElvanSettingsSection({ title, action, children, sx }: ElvanSettingsSectionProps) {
  return (
    <SettingsSection title={title} sx={sx}>
      <Box sx={{ p: { xs: 2, md: 3 } }}>
        {action && (
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
            {action}
          </Box>
        )}
        {children}
      </Box>
    </SettingsSection>
  );
}

export interface EditableSettingsPillProps {
  title: string;
  icon?: React.ReactNode;
  iconColor?: string;
  isEditing: boolean;
  onEdit: () => void;
  onCancel: () => void;
  onSave: () => void;
  renderDisplay: () => React.ReactNode;
  children: React.ReactNode;
  sx?: any;
  saveLabel?: string;
  cancelLabel?: string;
}

export function EditableSettingsPill({
  title, icon, iconColor, isEditing, onEdit, onCancel, onSave, renderDisplay, children, sx, saveLabel = "Save", cancelLabel = "Cancel"
}: EditableSettingsPillProps) {
  const getIconBg = (color?: string) => {
    switch(color) {
      case 'blue': return '#2196F3';
      case 'purple': return '#9C27B0';
      case 'orange': return '#FF9800';
      case 'green': return '#4CAF50';
      case 'red': return '#F44336';
      case 'monochrome': return 'var(--mac-selection-hover)';
      default: return 'var(--mac-selection-hover, rgba(255, 255, 255, 0.1))';
    }
  };

  const getIconColor = (color?: string) => {
    return color === 'monochrome' ? 'var(--mac-text)' : '#ffffff';
  };

  return (
    <Box sx={{ mb: 4, ...sx }}>
      <Paper 
        elevation={0} 
        sx={{ 
          position: 'relative',
          borderRadius: '24px', 
          overflow: 'hidden', 
          bgcolor: 'var(--mac-card-bg, #1c1c1e)',
          border: 'none',
          width: '100%'
        }}
      >
        {!isEditing && (
          <Box sx={{ position: 'absolute', top: 16, right: 20, zIndex: 10 }}>
            <ButtonBase 
              onClick={onEdit} 
              sx={{ 
                width: 42, height: 42, borderRadius: '50%', 
                color: 'text.secondary', flexShrink: 0,
                bgcolor: 'var(--mac-selection-hover, rgba(255,255,255,0.05))',
                '@media (hover: hover)': {
                  '&:hover': { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.08)', color: 'var(--mac-text)' }
                }
              }}
            >
              <PencilSimple size={18} weight="bold" />
            </ButtonBase>
          </Box>
        )}

        <Collapse in={!isEditing} timeout={300}>
          <Fade in={!isEditing} timeout={300}>
            <Box sx={{ p: '20px 80px 20px 20px' }}>
              {renderDisplay()}
            </Box>
          </Fade>
        </Collapse>
        <Collapse in={isEditing} unmountOnExit timeout={300}>
          <Fade in={isEditing} timeout={300}>
            <Box sx={{ p: '20px', bgcolor: 'transparent' }}>
              {children}
              <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
                <ButtonBase 
                  onClick={onCancel} 
                  sx={{ 
                    px: 2, py: 1, borderRadius: '500px', fontWeight: 600, fontSize: '14px',
                    color: 'text.secondary',
                    '@media (hover: hover)': {
                      '&:hover': { bgcolor: 'rgba(255,255,255,0.05)' }
                    }
                  }}
                >
                  {cancelLabel}
                </ButtonBase>
                <ButtonBase 
                  onClick={onSave} 
                  sx={{ 
                    px: 3, py: 1, borderRadius: '500px', fontWeight: 600, fontSize: '14px',
                    bgcolor: 'primary.main', color: 'primary.contrastText',
                    '@media (hover: hover)': {
                      '&:hover': { bgcolor: 'primary.dark' }
                    }
                  }}
                >
                  {saveLabel}
                </ButtonBase>
              </Box>
            </Box>
          </Fade>
        </Collapse>
      </Paper>
    </Box>
  );
}

export interface SettingsPillContainerProps {
  title?: string;
  icon?: React.ReactNode;
  iconColor?: string;
  children: React.ReactNode;
  sx?: any;
}

export function SettingsPillContainer({ title, icon, iconColor, children, sx }: SettingsPillContainerProps) {
  const getIconBg = (color?: string) => {
    switch(color) {
      case 'blue': return '#2196F3';
      case 'purple': return '#9C27B0';
      case 'orange': return '#FF9800';
      case 'green': return '#4CAF50';
      case 'red': return '#F44336';
      case 'teal': return '#009688';
      case 'monochrome': return 'var(--mac-selection-hover)';
      default: return 'var(--mac-selection-hover, rgba(255, 255, 255, 0.1))';
    }
  };

  const getIconColor = (color?: string) => {
    return color === 'monochrome' ? 'var(--mac-text)' : '#ffffff';
  };

  return (
    <Box sx={{ mb: 4, ...sx }}>
      <Paper 
        elevation={0} 
        sx={{ 
          borderRadius: '24px', 
          overflow: 'hidden', 
          bgcolor: 'var(--mac-card-bg, #1c1c1e)',
          border: 'none'
        }}
      >

        <Box sx={{ display: 'flex', flexDirection: 'column' }}>
          {React.Children.toArray(children).filter(Boolean).map((child, index, array) => (
            <React.Fragment key={index}>
              {child}
              {index < array.length - 1 && <Divider sx={(theme) => ({ ml: '20px', mr: '20px', borderColor: theme.palette.mode === 'dark' ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.06)' })} />}
            </React.Fragment>
          ))}
        </Box>
      </Paper>
    </Box>
  );
}

export interface SettingsPillRowProps {
  label: string;
  value: React.ReactNode;
  isEditing: boolean;
  onEdit: () => void;
  onCancel: () => void;
  onSave: () => void;
  children: React.ReactNode;
  saveLabel?: string;
  cancelLabel?: string;
  disableEdit?: boolean;
  extraAction?: React.ReactNode;
}

export function SettingsPillRow({ 
  label, value, isEditing, onEdit, onCancel, onSave, children, saveLabel, cancelLabel, disableEdit = false, extraAction
}: SettingsPillRowProps) {
  const { t } = useLanguage();
  
  const finalCancelLabel = cancelLabel || t('cancel');
  const finalSaveLabel = saveLabel || t('save');
  const editPrefix = t('editPrefix');

  return (
    <Box sx={{ position: 'relative' }}>
      <Collapse in={!isEditing} timeout={300}>
        <Fade in={!isEditing} timeout={300}>
          <Box sx={{ p: '16px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', pr: 2, flex: 1, minWidth: 0 }}>
               <Typography noWrap sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mb: '2px', fontWeight: 500 }}>
                 {label}
               </Typography>
               <Box sx={{ 
                 fontSize: '15px', color: 'var(--mac-text, #ffffff)',
                 overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                 '& *': { overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }
               }}>
                 {value || <span style={{ fontStyle: 'italic', opacity: 0.5 }}>Not set</span>}
               </Box>
            </Box>
            {!disableEdit && (
              <ButtonBase 
                onClick={onEdit} 
                sx={{ 
                  width: 42, height: 42, borderRadius: '50%', 
                  color: 'text.secondary', flexShrink: 0,
                  bgcolor: 'var(--mac-selection-hover, rgba(255,255,255,0.05))',
                  '@media (hover: hover)': {
                    '&:hover': { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.08)', color: 'var(--mac-text)' }
                  }
                }}
              >
                <PencilSimple size={18} weight="bold" />
              </ButtonBase>
            )}
          </Box>
        </Fade>
      </Collapse>
      
      <Collapse in={isEditing} unmountOnExit timeout={300}>
        <Fade in={isEditing} timeout={300}>
          <Box sx={{ p: '20px', bgcolor: 'transparent' }}>
            <Typography sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mb: 2, fontWeight: 500 }}>
               {editPrefix}{label}
            </Typography>
            <Box sx={{ mb: 2 }}>
              {children}
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
              {extraAction && (
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  {extraAction}
                </Box>
              )}
              <ButtonBase 
                onClick={onCancel} 
                sx={{ 
                  px: 2, py: 1, borderRadius: '500px', fontWeight: 600, fontSize: '14px', fontFamily: '"Elvan Sans", sans-serif',
                  color: 'text.secondary',
                  '@media (hover: hover)': {
                    '&:hover': { bgcolor: 'rgba(255,255,255,0.05)' }
                  }
                }}
              >
                {finalCancelLabel}
              </ButtonBase>
              <ButtonBase 
                onClick={onSave} 
                sx={{ 
                  px: 3, py: 1, borderRadius: '500px', fontWeight: 600, fontSize: '14px', fontFamily: '"Elvan Sans", sans-serif',
                  bgcolor: 'primary.main', color: 'primary.contrastText',
                  '@media (hover: hover)': {
                    '&:hover': { bgcolor: 'primary.dark' }
                  }
                }}
              >
                {finalSaveLabel}
              </ButtonBase>
            </Box>
          </Box>
        </Fade>
      </Collapse>
    </Box>
  );
}
