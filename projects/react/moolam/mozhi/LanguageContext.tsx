// @ts-nocheck
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { ta, TranslationKey } from './ta';
import { en } from './en';

type Language = 'ta' | 'en';

interface LanguageContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: TranslationKey) => string;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export const LanguageProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [language, setLanguageState] = useState<Language>(() => {
    const saved = localStorage.getItem('elvanniril_language') as Language;
    return (saved === 'en' || saved === 'ta') ? saved : 'en';
  });

  useEffect(() => {
    // Keep in sync if changed from other tabs
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'elvanniril_language' && (e.newValue === 'en' || e.newValue === 'ta')) {
        setLanguageState(e.newValue);
      }
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  const setLanguage = (lang: Language) => {
    setLanguageState(lang);
    localStorage.setItem('elvanniril_language', lang);
  };

  const t = (key: TranslationKey): string => {
    const dictionary = language === 'en' ? en : ta;
    return dictionary[key] || en[key] || key;
  };

  return (
    <LanguageContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
};

export const useLanguage = () => {
  const context = useContext(LanguageContext);
  if (context === undefined) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
};
