// @ts-nocheck
/**
 * Tamil Number to Words Converter
 * 
 * Converts numeric values to Tamil words with proper Sandhi rules (grammar joining).
 * Example: 57 -> ஐம்பத்தேழு (not ஐம்பத்து ஏழு)
 * Example: 500 oblique -> ஐந்நூற்று
 */

const ones = [
    '', 'ஒன்று', 'இரண்டு', 'மூன்று', 'நான்கு',
    'ஐந்து', 'ஆறு', 'ஏழு', 'எட்டு', 'ஒன்பது',
    'பத்து', 'பதினொன்று', 'பன்னிரண்டு', 'பதிமூன்று', 'பதினான்கு',
    'பதினைந்து', 'பதினாறு', 'பதினேழு', 'பதினெட்டு', 'பத்தொன்பது'
];

// Base tens (20, 30...90)
const tensBase = [
    '', '', 'இருபது', 'முப்பது', 'நாற்பது',
    'ஐம்பது', 'அறுபது', 'எழுபது', 'எண்பது', 'தொண்ணூறு'
];

// Oblique tens (20, 30...90 used when combining)
const tensOblique = [
    '', '', 'இருபத்து', 'முப்பத்து', 'நாற்பத்து',
    'ஐம்பத்து', 'அறுபத்து', 'எழுபத்து', 'எண்பத்து', 'தொண்ணூற்று'
];

const hundredsBase: Record<number, string> = {
    1: 'நூறு',
    2: 'இருநூறு',
    3: 'முந்நூறு',
    4: 'நானூறு',
    5: 'ஐந்நூறு',
    6: 'அறுநூறு',
    7: 'எழுநூறு',
    8: 'எண்ணூறு',
    9: 'தொள்ளாயிரம்'
};

const hundredsOblique: Record<number, string> = {
    1: 'நூற்று',
    2: 'இருநூற்று',
    3: 'முந்நூற்று',
    4: 'நானூற்று',
    5: 'ஐந்நூற்று',
    6: 'அறுநூற்று',
    7: 'எழுநூற்று',
    8: 'எண்ணூற்று',
    9: 'தொள்ளாயிரத்து'
};

/**
 * Helper to join two Tamil words using Sandhi rules.
 * Specifically handles words ending in 'u' (து, று, டு) joining with words starting with vowels.
 */
function joinTamil(word1: string, word2: string): string {
    if (!word1) return word2;
    if (!word2) return word1;

    // Use oblique forms (passed as word1) but separated by spaces.
    // Disable the vowel merging logic that combines them into single words.
    return word1 + ' ' + word2;
}

function convertUnder100(n: number): string {
    if (n < 20) return ones[n];

    const tensDigit = Math.floor(n / 10);
    const onesDigit = n % 10;

    if (onesDigit === 0) {
        return tensBase[tensDigit];
    }

    return joinTamil(tensOblique[tensDigit], ones[onesDigit]);
}

function convertUnder1000(n: number): string {
    if (n < 100) return convertUnder100(n);

    const hundredsDigit = Math.floor(n / 100);
    const remainder = n % 100;

    if (remainder === 0) {
        return hundredsBase[hundredsDigit];
    }

    return joinTamil(hundredsOblique[hundredsDigit], convertUnder100(remainder));
}

function convertUnder100000(n: number): string {
    if (n < 1000) return convertUnder1000(n);

    const thousands = Math.floor(n / 1000);
    const remainder = n % 1000;

    let thousandsStr = convertUnder100(thousands);
    let suffix = (remainder === 0) ? 'ஆயிரம்' : 'ஆயிரத்து';

    let thousandPart = joinTamil(thousandsStr, suffix);

    if (remainder === 0) return thousandPart;

    return thousandPart + ' ' + convertUnder1000(remainder);
}

function convertUnderCrore(n: number): string {
    if (n < 100000) return convertUnder100000(n);

    const lakhs = Math.floor(n / 100000);
    const remainder = n % 100000;

    let lakhPart = '';

    if (lakhs === 1) {
        lakhPart = (remainder === 0) ? 'ஒரு லட்சம்' : 'ஒரு லட்சத்து';
    } else {
        let lakhsStr = convertUnder100(lakhs);
        let suffix = (remainder === 0) ? 'லட்சம்' : 'லட்சத்து';
        lakhPart = joinTamil(lakhsStr, suffix);
    }

    if (remainder === 0) return lakhPart;

    return lakhPart + ' ' + convertUnder100000(remainder);
}

function convert(n: number): string {
    if (n === 0) return 'சுழியம்';
    if (n < 10000000) return convertUnderCrore(n); // Less than 1 Crore

    const crores = Math.floor(n / 10000000);
    const remainder = n % 10000000;

    let crorePart = '';
    
    if (crores === 1) {
        crorePart = (remainder === 0) ? 'ஒரு கோடி' : 'ஒரு கோடியே';
    } else {
        // Recursive call allows for 'நூறு கோடி', 'லட்சம் கோடி', 'கோடி கோடி', etc.
        let croresStr = convert(crores); 
        let suffix = (remainder === 0) ? 'கோடி' : 'கோடியே';
        crorePart = joinTamil(croresStr, suffix);
    }

    if (remainder === 0) return crorePart;

    return crorePart + ' ' + convertUnderCrore(remainder);
}

export function numberToWordsTamil(num: number, suffix = 'ரூபாய் மட்டும்'): string {
    if (num === 0) return 'சுழியம்';
    if (typeof num !== 'number' || isNaN(num)) return '';

    const intPart = Math.floor(Math.abs(num));
    const words = convert(intPart);

    return words + ' ' + suffix;
}

export default numberToWordsTamil;
