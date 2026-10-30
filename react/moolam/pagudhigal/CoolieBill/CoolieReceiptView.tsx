// @ts-nocheck
import React, { useState, useEffect } from 'react';
import { getAllCoolieProfiles } from '../../Avanam';
import PodhuReceiptPaper from '../PodhuReceiptPaper';

export default function CoolieReceiptView({ receipt: receiptProp, onBack, onEdit }) {
  const receipt = receiptProp || {};
  const [coolieProfile, setCoolieProfile] = useState<any>(receipt._companyProfile || null);

  useEffect(() => {
    if (receipt._companyProfile) return;
    getAllCoolieProfiles().then(profiles => {
      if (profiles && profiles.length > 0) {
        const p = profiles.find(pr => receipt.company_id && pr.id === receipt.company_id) 
               || profiles.find(pr => pr.shortBusinessName && receipt.receiptNo?.includes(pr.shortBusinessName)) 
               || profiles[0];
        setCoolieProfile(p);
      }
    });
  }, [receipt.company_id, receipt.receiptNo, receipt._companyProfile]);

  const profile = coolieProfile || receipt._companyProfile || {};

  return (
    <PodhuReceiptPaper 
      receipt={receipt} 
      profile={profile} 
      onBack={onBack} 
      onEdit={onEdit} 
      appMode="Udukkai Coolie"
    />
  );
}
