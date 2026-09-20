import { useState, useEffect, useRef } from 'react';

export function useDraftAndUnsaved<T>(
  draftKey: string,
  initialForm: T,
  currentForm: T,
  setForm: (form: T) => void,
  isEditing: boolean,
  getIsBlank: (form: T) => boolean
) {
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [isDraftLoaded, setIsDraftLoaded] = useState(false);
  const setFormRef = useRef(setForm);
  setFormRef.current = setForm;

  const actualDraftKey = isEditing && (currentForm as any)?.id ? `${draftKey}_edit_${(currentForm as any).id}` : draftKey;

  // Load draft on mount
  useEffect(() => {
    const savedDraft = localStorage.getItem(actualDraftKey);
    if (savedDraft) {
        try {
          const parsedDraft = JSON.parse(savedDraft);
          setFormRef.current((prev: any) => ({ ...prev, ...parsedDraft }));
        } catch (e) {
          console.error('Failed to parse draft', e);
        }
      }
    setIsDraftLoaded(true);
  }, [actualDraftKey]);

  // Save draft and check for unsaved changes
  useEffect(() => {
    if (!isDraftLoaded) return;

    const isBlank = getIsBlank(currentForm);
    const hasChanges = JSON.stringify(currentForm) !== JSON.stringify(initialForm);

    if (hasChanges && !isBlank) {
      setHasUnsavedChanges(true);
    } else {
      setHasUnsavedChanges(false);
    }
    
    // Always sync non-blank drafts to localStorage to prevent data loss
    if (!isBlank) {
      localStorage.setItem(actualDraftKey, JSON.stringify(currentForm));
    } else if (isBlank) {
      localStorage.removeItem(actualDraftKey);
    }
  }, [currentForm, initialForm, actualDraftKey, isDraftLoaded, getIsBlank]);

  const clearDraft = () => {
    localStorage.removeItem(actualDraftKey);
  };

  return { hasUnsavedChanges, clearDraft };
}
