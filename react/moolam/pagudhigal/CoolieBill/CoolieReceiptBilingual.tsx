// @ts-nocheck
import React from 'react';
import PodhuReceiptPaper from '../PodhuReceiptPaper';

export default function CoolieReceiptBilingual({ receipt }) {
  const profile = receipt?._companyProfile || receipt?.companyProfile || receipt?.profile || {};
  return (
    <PodhuReceiptPaper 
      receipt={receipt} 
      profile={profile} 
      appMode="Udukkai Coolie"
    />
  );
}
