// @ts-nocheck
// Print-bundle stand-in for moolam/Avanam.ts (which talks to Firebase).
// The React print views only read company profiles from it; the KMP app
// supplies those through setCoolieProfiles() before rendering.

let coolieProfiles = [];

export const setCoolieProfiles = (profiles) => {
  coolieProfiles = Array.isArray(profiles) ? profiles : [];
};

export const getAllCoolieProfiles = async () => coolieProfiles;

// Used by ReceiptView's on-screen logo position editor; nothing to save here.
export const saveProfile = async (profile) => profile;

export const getInvoiceDisplayOptions = async () => null;
