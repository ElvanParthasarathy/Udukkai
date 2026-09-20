import React, { useState, useEffect } from 'react';
import { AuthLayout, AuthButton } from '../auth/AuthComponents';
import { CheckCircle, GlobeHemisphereWest, FileText } from '@phosphor-icons/react';
import { List, ListItem, ListItemButton, ListItemText, ListItemIcon, Divider } from '@mui/material';
import { useLanguage } from '../../mozhi/LanguageContext';

const GREETINGS = ["வணக்கம்!", "Hello!", "നമസ്കാരം!"];

export default function NalvaravuWelcome({ onContinue }: { onContinue: () => void }) {
    const { t, language, setLanguage } = useLanguage();
    
    const [phase, setPhase] = useState<'greeting' | 'language' | 'billingLanguage'>('greeting');
    const [greetingIndex, setGreetingIndex] = useState(0);
    const [greetingOpacity, setGreetingOpacity] = useState(0);
    const [showLanguage, setShowLanguage] = useState(false);
    const [billingLanguage, setBillingLanguage] = useState<'Tamil' | 'English'>('Tamil');
    const [showBillingLanguage, setShowBillingLanguage] = useState(false);

    useEffect(() => {
        if (!localStorage.getItem('elvanniril_language')) {
            setLanguage('ta');
        }
    }, []);

    useEffect(() => {
        let loopCount = 0;
        let isMounted = true;

        const runGreetingLoop = async () => {
            while (isMounted && phase === 'greeting') {
                // Fade in
                setGreetingOpacity(1);
                await new Promise(r => setTimeout(r, 1200));
                
                // Fade out
                setGreetingOpacity(0);
                await new Promise(r => setTimeout(r, 800));
                
                if (!isMounted) break;

                // Move to next greeting or switch to language phase
                loopCount++;
                if (loopCount >= GREETINGS.length) {
                    setPhase('language');
                    break;
                } else {
                    setGreetingIndex((prev) => prev + 1);
                }
            }
        };

        if (phase === 'greeting') {
            runGreetingLoop();
        }

        return () => { isMounted = false; };
    }, [phase]);

    useEffect(() => {
        if (phase === 'language') {
            setTimeout(() => setShowLanguage(true), 100);
        } else if (phase === 'billingLanguage') {
            setTimeout(() => setShowBillingLanguage(true), 100);
        }
    }, [phase]);

    return (
        <AuthLayout hideLogo>
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                flex: 1,
                justifyContent: 'center',
                alignItems: 'center',
                maxWidth: '480px',
                margin: '0 auto',
                width: '100%',
                position: 'relative'
            }}>
                
                {/* PHASE 1: GREETING ANIMATION */}
                {phase === 'greeting' && (
                    <div style={{
                        opacity: greetingOpacity,
                        transition: 'opacity 0.8s ease-in-out',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flex: 1
                    }}>
                        <h1 style={{
                            fontSize: '48px',
                            fontWeight: '300',
                            color: 'var(--auth-text)',
                            margin: 0,
                            letterSpacing: '-1px'
                        }}>
                            {GREETINGS[greetingIndex]}
                        </h1>
                    </div>
                )}

                {/* PHASE 2: LANGUAGE SELECTION SCREEN */}
                {phase === 'language' && (
                    <div style={{
                        display: 'flex',
                        flexDirection: 'column',
                        width: '100%',
                        alignItems: 'center',
                        gap: '40px',
                        opacity: showLanguage ? 1 : 0,
                        transform: showLanguage ? 'translateY(0)' : 'translateY(20px)',
                        transition: 'all 0.8s cubic-bezier(0.34, 1.56, 0.64, 1)',
                    }}>
                        
                        {/* ICON SECTION */}
                        <div style={{
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                        }}>
                            <GlobeHemisphereWest size={80} weight="regular" color="var(--auth-text)" />
                        </div>

                        {/* TEXT SECTION */}
                        <div style={{
                            textAlign: 'center',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '12px'
                        }}>
                            <h1 style={{
                                fontSize: 'clamp(20px, 5.5vw, 28px)',
                                fontWeight: '800',
                                color: 'var(--auth-text)',
                                margin: 0,
                                letterSpacing: '-0.5px'
                            }}>
                                {t('selectLanguageTitle') !== 'selectLanguageTitle' ? t('selectLanguageTitle') : 'Select Language'}
                            </h1>
                            <p style={{
                                fontSize: '18px',
                                color: 'var(--auth-text-secondary)',
                                margin: 0,
                                fontWeight: '500'
                            }}>
                                {t('selectLanguageSubtitle') !== 'selectLanguageSubtitle' ? t('selectLanguageSubtitle') : 'Choose your preferred language'}
                            </p>
                        </div>

                        {/* LANGUAGE OPTIONS & BUTTON SECTION */}
                        <div style={{
                            width: '100%',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                        }}>
                            
                            {/* MUI LANGUAGE SELECTOR */}
                            <List sx={{
                                width: '100%',
                                maxWidth: '360px',
                                bgcolor: 'var(--auth-input-bg)',
                                borderRadius: '16px',
                                mb: 4,
                                overflow: 'hidden',
                                p: 0
                            }}>
                                <ListItem disablePadding>
                                    <ListItemButton 
                                        onClick={() => setLanguage('ta')} 
                                        sx={{ 
                                            py: 2, 
                                            px: 3,
                                            bgcolor: 'transparent',
                                            '@media (hover: hover)': { '&:hover': { bgcolor: 'transparent' } }
                                        }}
                                        disableRipple
                                    >
                                        <ListItemText 
                                            primary={
                                                <span style={{ 
                                                    fontSize: '18px', 
                                                    fontWeight: language === 'ta' ? 600 : 500, 
                                                    color: 'var(--auth-text)' 
                                                }}>
                                                    தமிழ்
                                                </span>
                                            } 
                                        />
                                        {language === 'ta' && (
                                            <ListItemIcon sx={{ minWidth: 'auto' }}>
                                                <CheckCircle size={24} weight="fill" color="var(--auth-text)" />
                                            </ListItemIcon>
                                        )}
                                    </ListItemButton>
                                </ListItem>
                                <Divider sx={{ mx: 3, borderColor: 'var(--auth-divider)' }} />
                                <ListItem disablePadding>
                                    <ListItemButton 
                                        onClick={() => setLanguage('en')} 
                                        sx={{ 
                                            py: 2, 
                                            px: 3,
                                            bgcolor: 'transparent',
                                            '@media (hover: hover)': { '&:hover': { bgcolor: 'transparent' } }
                                        }}
                                        disableRipple
                                    >
                                        <ListItemText 
                                            primary={
                                                <span style={{ 
                                                    fontSize: '18px', 
                                                    fontWeight: language === 'en' ? 600 : 500, 
                                                    color: 'var(--auth-text)' 
                                                }}>
                                                    English
                                                </span>
                                            } 
                                        />
                                        {language === 'en' && (
                                            <ListItemIcon sx={{ minWidth: 'auto' }}>
                                                <CheckCircle size={24} weight="fill" color="var(--auth-text)" />
                                            </ListItemIcon>
                                        )}
                                    </ListItemButton>
                                </ListItem>
                            </List>

                            <div style={{ width: '100%', maxWidth: '360px' }}>
                                <AuthButton onClick={() => setPhase('billingLanguage')}>
                                    {language === 'ta' ? 'தொடரவும்' : 'Continue'}
                                </AuthButton>
                            </div>
                        </div>
                    </div>
                )}

                {/* PHASE 2.5: BILLING LANGUAGE SELECTION SCREEN */}
                {phase === 'billingLanguage' && (
                    <div style={{
                        display: 'flex',
                        flexDirection: 'column',
                        width: '100%',
                        alignItems: 'center',
                        gap: '40px',
                        opacity: showBillingLanguage ? 1 : 0,
                        transform: showBillingLanguage ? 'translateY(0)' : 'translateY(20px)',
                        transition: 'all 0.8s cubic-bezier(0.34, 1.56, 0.64, 1)',
                    }}>
                        
                        {/* ICON SECTION */}
                        <div style={{
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                        }}>
                            <FileText size={80} weight="duotone" color="var(--auth-text)" />
                        </div>

                        {/* TEXT SECTION */}
                        <div style={{
                            textAlign: 'center',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '12px'
                        }}>
                            <h1 style={{
                                fontSize: 'clamp(20px, 5.5vw, 28px)',
                                fontWeight: '800',
                                color: 'var(--auth-text)',
                                margin: 0,
                                letterSpacing: '-0.5px'
                            }}>
                                {language === 'ta' ? 'பில் முதன்மை மொழி' : 'Select Primary Billing Language'}
                            </h1>
                            <p style={{
                                fontSize: '18px',
                                color: 'var(--auth-text-secondary)',
                                margin: 0,
                                fontWeight: '500'
                            }}>
                                {language === 'ta' ? 'பில்களுக்கு எந்த மொழியைப் பயன்படுத்த விரும்புகிறீர்கள்?' : 'Which language would you like to use for your bills?'}
                            </p>
                        </div>

                        {/* LANGUAGE OPTIONS & BUTTON SECTION */}
                        <div style={{
                            width: '100%',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                        }}>
                            
                            {/* MUI LANGUAGE SELECTOR */}
                            <List sx={{
                                width: '100%',
                                maxWidth: '360px',
                                bgcolor: 'var(--auth-input-bg)',
                                borderRadius: '16px',
                                mb: 4,
                                overflow: 'hidden',
                                p: 0
                            }}>
                                <ListItem disablePadding>
                                    <ListItemButton 
                                        onClick={() => setBillingLanguage('Tamil')} 
                                        sx={{ 
                                            py: 2, 
                                            px: 3,
                                            bgcolor: 'transparent',
                                            '@media (hover: hover)': { '&:hover': { bgcolor: 'transparent' } }
                                        }}
                                        disableRipple
                                    >
                                        <ListItemText 
                                            primary={
                                                <span style={{ 
                                                    fontSize: '18px', 
                                                    fontWeight: billingLanguage === 'Tamil' ? 600 : 500, 
                                                    color: 'var(--auth-text)' 
                                                }}>
                                                    தமிழ்
                                                </span>
                                            } 
                                        />
                                        {billingLanguage === 'Tamil' && (
                                            <ListItemIcon sx={{ minWidth: 'auto' }}>
                                                <CheckCircle size={24} weight="fill" color="var(--auth-text)" />
                                            </ListItemIcon>
                                        )}
                                    </ListItemButton>
                                </ListItem>
                                <Divider sx={{ mx: 3, borderColor: 'var(--auth-divider)' }} />
                                <ListItem disablePadding>
                                    <ListItemButton 
                                        onClick={() => setBillingLanguage('English')} 
                                        sx={{ 
                                            py: 2, 
                                            px: 3,
                                            bgcolor: 'transparent',
                                            '@media (hover: hover)': { '&:hover': { bgcolor: 'transparent' } }
                                        }}
                                        disableRipple
                                    >
                                        <ListItemText 
                                            primary={
                                                <span style={{ 
                                                    fontSize: '18px', 
                                                    fontWeight: billingLanguage === 'English' ? 600 : 500, 
                                                    color: 'var(--auth-text)' 
                                                }}>
                                                    English
                                                </span>
                                            } 
                                        />
                                        {billingLanguage === 'English' && (
                                            <ListItemIcon sx={{ minWidth: 'auto' }}>
                                                <CheckCircle size={24} weight="fill" color="var(--auth-text)" />
                                            </ListItemIcon>
                                        )}
                                    </ListItemButton>
                                </ListItem>
                            </List>

                            <div style={{ width: '100%', maxWidth: '360px' }}>
                                <AuthButton onClick={() => {
                                    localStorage.setItem('elvanniril_setup_billingLang', billingLanguage);
                                    onContinue();
                                }}>
                                    {billingLanguage === 'Tamil' ? 'தொடரவும்' : 'Continue'}
                                </AuthButton>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </AuthLayout>
    );
}
