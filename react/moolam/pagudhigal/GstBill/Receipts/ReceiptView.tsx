// @ts-nocheck
import React from 'react';
import PodhuReceiptPaper from '../../PodhuReceiptPaper';

export default function ReceiptView({ receipt, profile, onBack, onEdit }) {
  return (
    <PodhuReceiptPaper 
      receipt={receipt} 
      profile={profile} 
      onBack={onBack} 
      onEdit={onEdit} 
      appMode="Udukkai Silk"
    />
  );
}
