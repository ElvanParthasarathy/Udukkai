import React, { useState, useRef, ReactNode } from 'react';
import { Box, Typography, Button, Paper, IconButton, Toolbar, Stack, Tooltip, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Pagination, LinearProgress, Skeleton, Collapse, Fade } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { X, PencilSimple, Plus, CheckSquare, Square, Trash, Copy } from '@phosphor-icons/react';
import { useLanguage } from '../mozhi/LanguageContext';
import { getSearchPaperSx, searchInputStyle, getEditPaperSx, getEditIconButtonSx, getAddButtonSx } from './commonStyles';
import ElvanCard from './ElvanCard';

export interface ElvanListViewProps<T> {
  title: string;
  searchPlaceholder?: string;
  addButtonText?: string;
  onAdd?: () => void;
  
  items: T[];
  isLoading?: boolean;
  filterFn: (item: T, search: string) => boolean;
  renderCard: (
    item: T, 
    globalIndex: number, 
    isSelectionMode: boolean, 
    isSelected: boolean, 
    toggleSelection: (id: string) => void
  ) => ReactNode;
  
  emptyIcon: ReactNode;
  emptyText: string;
  
  onDeleteSelected?: (ids: string[], onProgress?: (completed: number, total: number) => void) => Promise<void>;
  onDuplicateSelected?: (ids: string[], onProgress?: (completed: number, total: number) => void) => Promise<void>;
  
  deleteConfirmTitle?: string;
  deleteConfirmMessage?: (count: number) => string;
  duplicateConfirmTitle?: string;
  duplicateConfirmMessage?: (count: number) => string;

  itemsPerPage?: number;
  renderBelowSearch?: ReactNode;
}

export default function ElvanListView<T extends { id: string }>(props: ElvanListViewProps<T>) {
  const { t } = useLanguage();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  
  const {
    title, searchPlaceholder = t('search') || 'Search...', addButtonText = t('add') || 'Add', onAdd,
    items, isLoading = false, filterFn, renderCard, emptyIcon, emptyText,
    onDeleteSelected, onDuplicateSelected,
    deleteConfirmTitle = t('delete') || 'Delete?',
    deleteConfirmMessage = (count) => t('deleteConfirmMessage') || `Are you sure you want to delete ${count} item(s)?`,
    duplicateConfirmTitle = t('duplicate') || 'Duplicate?',
    duplicateConfirmMessage = (count) => t('duplicateConfirmMessage') || `Are you sure you want to duplicate ${count} item(s)?`,
    itemsPerPage = 6,
    renderBelowSearch
  } = props;

  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [isSelectionMode, setIsSelectionMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  
  const [confirmDialog, setConfirmDialog] = useState<{ open: boolean; title: string; message: string; action: (() => Promise<void>) | null }>({ open: false, title: '', message: '', action: null });
  const [copyConfirmOpen, setCopyConfirmOpen] = useState(false);
  const [progress, setProgress] = useState<{ current: number, total: number } | null>(null);
  
  const topRef = useRef<HTMLDivElement>(null);

  const filteredItems = items.filter(item => filterFn(item, search));
  
  const totalPages = Math.ceil(filteredItems.length / itemsPerPage);
  const safePage = Math.max(1, Math.min(page, totalPages === 0 ? 1 : totalPages));
  const paginatedItems = filteredItems.slice((safePage - 1) * itemsPerPage, safePage * itemsPerPage);

  const handleSelectAll = () => {
    if (selectedIds.length === filteredItems.length && filteredItems.length > 0) {
      setSelectedIds([]);
    } else {
      setSelectedIds(filteredItems.map(item => item.id));
    }
  };

  const toggleSelection = (id: string) => {
    setSelectedIds(prev =>
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    );
  };

  const handleCopySelected = () => {
    if (selectedIds.length === 0) return;
    setCopyConfirmOpen(true);
  };

  const executeCopy = async () => {
    if (onDuplicateSelected) {
      setProgress({ current: 0, total: selectedIds.length });
      await onDuplicateSelected(selectedIds, (completed, total) => setProgress({ current: completed, total }));
      setProgress(null);
      setSelectedIds([]);
      setCopyConfirmOpen(false);
      setIsSelectionMode(false);
    }
  };

  const handleDeleteSelected = () => {
    if (selectedIds.length === 0) return;
    setConfirmDialog({
      open: true,
      title: deleteConfirmTitle,
      message: deleteConfirmMessage(selectedIds.length),
      action: async () => {
        if (onDeleteSelected) {
          setProgress({ current: 0, total: selectedIds.length });
          await onDeleteSelected(selectedIds, (completed, total) => setProgress({ current: completed, total }));
          setProgress(null);
          setSelectedIds([]);
          setIsSelectionMode(false);
        }
      }
    });
  };

  return (
    <Box ref={topRef} sx={{ py: { xs: 1.5, md: 4 }, px: { xs: 0, md: 4 }, maxWidth: 1200, mx: 'auto' }}>
      <Box sx={{ display: { xs: 'none', md: 'flex' }, justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 800, letterSpacing: '-0.5px', color: 'text.primary', ml: 2 }}>
          {title}
        </Typography>
      </Box>

      {/* Search and Selection */}
      <Box sx={{ mb: 4 }}>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Paper elevation={1} className="vanigargal-search" sx={getSearchPaperSx(isDark)}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ flexShrink: 0, opacity: 0.5 }}>
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <input
              type="text"
              placeholder={searchPlaceholder}
              value={search}
              onChange={e => { setSearch(e.target.value); setPage(1); }}
              style={searchInputStyle}
            />
            {search && (
              <IconButton size="small" onClick={() => { setSearch(''); setPage(1); }} sx={{ flexShrink: 0 }}>
                <X size={14} weight="regular" />
              </IconButton>
            )}
          </Paper>

          {(onDeleteSelected || onDuplicateSelected) && (
            <Paper elevation={1} sx={getEditPaperSx(isDark, isSelectionMode)}>
              <IconButton 
                onClick={() => { setIsSelectionMode(!isSelectionMode); setSelectedIds([]); }}
                sx={getEditIconButtonSx(isDark)}
              >
                <PencilSimple size={18} weight={isSelectionMode ? 'fill' : 'regular'} color={isDark ? '#fff' : '#000'} />
              </IconButton>
            </Paper>
          )}

          {onAdd && (
            <Button variant="contained" sx={getAddButtonSx(isDark)} onClick={onAdd} startIcon={<Plus size={18} weight="bold" />}>
              {addButtonText}
            </Button>
          )}
        </Box>
        {renderBelowSearch && (
          <Box sx={{ mt: 2 }}>
            {renderBelowSearch}
          </Box>
        )}
      </Box>

      <Collapse in={isSelectionMode} unmountOnExit timeout={300}>
        <Fade in={isSelectionMode} timeout={300}>
        <Toolbar
          component={Paper}
          elevation={1}
          variant="dense"
          sx={{
            boxShadow: 'none',
            pl: { sm: 2 },
            pr: { xs: 1, sm: 1 },
            mt: 0,
            mb: 4,
            minHeight: '48px !important',
            borderRadius: '24px',
            bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF',
          }}
        >
          <IconButton onClick={handleSelectAll} color="primary" sx={{ mr: 1 }}>
            {selectedIds.length === filteredItems.length && filteredItems.length > 0 ? <CheckSquare size={24} weight="fill" /> : <Square size={24} weight="regular" />}
          </IconButton>
          
          <Typography sx={{ flex: '1 1 100%', fontWeight: 600, display: 'flex', alignItems: 'center', lineHeight: 1, mt: 0.3 }} color="primary" variant="subtitle1" component="div">
            {selectedIds.length} {t('selected') || 'Selected'}
          </Typography>

          <Stack direction="row" spacing={1}>
            {onDuplicateSelected && (
              <Tooltip title={t('copyDuplicate') || 'Copy / Duplicate'}>
                <IconButton onClick={handleCopySelected} color="primary">
                  <Copy size={20} />
                </IconButton>
              </Tooltip>
            )}
            {onDeleteSelected && (
              <Tooltip title={t('delete') || 'Delete'}>
                <IconButton onClick={handleDeleteSelected} color="error">
                  <Trash size={20} />
                </IconButton>
              </Tooltip>
            )}
          </Stack>
        </Toolbar>
        </Fade>
      </Collapse>

      {isLoading ? (
        <Box sx={{ 
          columnCount: { xs: 1, md: 2 }, 
          columnGap: '16px', 
          '& > *': { 
            breakInside: 'avoid', 
            mb: 2, 
            display: 'block', 
            width: '100%' 
          } 
        }}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <ElvanCard key={i} sx={{ height: '100%', p: 2 }}>
              <Stack direction="row" spacing={2} sx={{ alignItems: 'center', height: '100%' }}>
                <Skeleton variant="circular" width={28} height={28} />
                <Box sx={{ flex: 1 }}>
                  <Skeleton variant="text" width="60%" height={24} />
                  <Skeleton variant="text" width="40%" height={16} sx={{ mt: 0.5 }} />
                </Box>
                <Skeleton variant="rectangular" width={60} height={24} sx={{ borderRadius: 1 }} />
              </Stack>
            </ElvanCard>
          ))}
        </Box>
      ) : filteredItems.length === 0 ? (
        <ElvanCard boxSx={{ p: 6, textAlign: 'center' }}>
          <Box color="text.secondary" mb={2}>
            {emptyIcon}
          </Box>
          <Typography color="text.secondary">{emptyText}</Typography>
        </ElvanCard>
      ) : (
        <Box sx={{ 
          columnCount: { xs: 1, md: 2 }, 
          columnGap: '16px', 
          '& > *': { 
            breakInside: 'avoid', 
            mb: 2, 
            display: 'block', 
            width: '100%' 
          } 
        }}>
          {paginatedItems.map((item, index) => {
            const globalIndex = (safePage - 1) * itemsPerPage + index;
            const isSelected = selectedIds.includes(item.id);
            return renderCard(item, globalIndex, isSelectionMode, isSelected, toggleSelection);
          })}
        </Box>
      )}

      {totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <Pagination 
            count={totalPages} 
            page={safePage} 
            onChange={(_e, val) => {
              setPage(val);
              if (topRef.current) {
                topRef.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
              }
            }}
            color="primary" 
            size="large"
            sx={{
              '& .MuiPaginationItem-root': {
                fontWeight: 600,
              }
            }}
          />
        </Box>
      )}

      <Dialog open={copyConfirmOpen} onClose={() => setCopyConfirmOpen(false)} slotProps={{ paper: { elevation: 8, sx: { borderRadius: '24px', p: 1 } } }}>
        <DialogTitle sx={{ fontWeight: 800 }}>{duplicateConfirmTitle}</DialogTitle>
        <DialogContent>
          <DialogContentText>{duplicateConfirmMessage(selectedIds.length)}</DialogContentText>
          {progress && (
            <Box sx={{ width: '100%', mt: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2" color="text.secondary">{t('processing') || 'Processing...'}</Typography>
                <Typography variant="body2" color="text.secondary">{progress.current} / {progress.total}</Typography>
              </Box>
              <LinearProgress variant="determinate" value={(progress.current / progress.total) * 100} sx={{ borderRadius: 1, height: 6 }} />
            </Box>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button disabled={!!progress} onClick={() => setCopyConfirmOpen(false)} color="inherit" sx={{ borderRadius: '50px', textTransform: 'none', px: 3, whiteSpace: 'nowrap' }}>
            {t('cancel') || 'Cancel'}
          </Button>
          <Button disabled={!!progress} onClick={executeCopy} variant="contained" color="primary" sx={{ borderRadius: '50px', textTransform: 'none', px: 3, boxShadow: 'none', whiteSpace: 'nowrap' }}>
            {t('copy') || 'Copy'}
          </Button>
        </DialogActions>
      </Dialog>
      
      <Dialog open={confirmDialog.open} onClose={() => setConfirmDialog(prev => ({ ...prev, open: false }))} slotProps={{ paper: { elevation: 8, sx: { borderRadius: '24px', p: 1 } } }}>
        <DialogTitle sx={{ fontWeight: 800 }}>{confirmDialog.title}</DialogTitle>
        <DialogContent>
          <DialogContentText>{confirmDialog.message}</DialogContentText>
          {progress && (
            <Box sx={{ width: '100%', mt: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2" color="text.secondary">{t('processing') || 'Processing...'}</Typography>
                <Typography variant="body2" color="text.secondary">{progress.current} / {progress.total}</Typography>
              </Box>
              <LinearProgress variant="determinate" value={(progress.current / progress.total) * 100} sx={{ borderRadius: 1, height: 6 }} color="error" />
            </Box>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button disabled={!!progress} onClick={() => setConfirmDialog(prev => ({ ...prev, open: false }))} color="inherit" sx={{ borderRadius: '50px', textTransform: 'none', px: 3 }}>
            {t('cancel') || 'Cancel'}
          </Button>
          <Button 
            disabled={!!progress}
            onClick={async () => { 
              if (confirmDialog.action) {
                try { await confirmDialog.action(); } catch(e){}
              }
              setConfirmDialog(prev => ({ ...prev, open: false })); 
            }} 
            variant="contained" 
            color="primary" 
            sx={{ borderRadius: '50px', textTransform: 'none', px: 3, boxShadow: 'none' }}
          >
            {t('delete') || 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

    </Box>
  );
}
