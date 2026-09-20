# AI & Developer Architecture Guide

**ATTENTION ALL AI AGENTS AND DEVELOPERS:** 
This React/Vite project uses a custom **Tanglish** (Tamil + English phonetics) file and folder architecture. 

If you are tasked with modifying this codebase, do **NOT** attempt to recreate standard `src/` or `components/` folders. You must use the existing Tanglish structure outlined below.

## Root Directory Mapping
| Tanglish (Current) | English Equivalent (Standard) | Description |
|--------------------|-------------------------------|-------------|
| `moolam/`          | `src/`                        | Main source code directory. |
| `podhu/`           | `public/`                     | Public static assets (favicon, etc). |

## Source Subdirectories (`/moolam/`)
| Tanglish (Current) | English Equivalent (Standard) | Description |
|--------------------|-------------------------------|-------------|
| `pagudhigal/`      | `components/`                 | React UI components. |
| `mozhi/`           | `i18n/`                       | Internationalization / language files (`ta.ts`, `en.ts`). |
| `sevaigal/`        | `services/`                   | Backend/API services or utility workers. |

## Core Files (`/moolam/`)
| Tanglish (Current) | English Equivalent (Standard) | Description |
|--------------------|-------------------------------|-------------|
| `Seyali.tsx`       | `App.tsx`                     | Main application routing and shell. |
| `Thodakkam.tsx`    | `main.tsx`                    | Vite entry point (`createRoot`). |
| `Avanam.ts`        | `store.ts`                    | Global state, database, or store logic. |
| `Payanpadu.ts`     | `utils.ts`                    | Shared utility functions. |
| `vadivam.css`      | `index.css`                   | Global CSS styles. |
| `sutruzhal.d.ts`   | `vite-env.d.ts`               | TypeScript environment definitions. |
| `KaiyeduUlladakkam.ts` | `userGuideContent.ts` | Hardcoded content for the welcome/onboarding guide. |

## Component Files (`/moolam/pagudhigal/`)
All React components use Tanglish names. Here is a partial mapping of the core screens:
- `Mugappu.tsx` = Dashboard
- `PattiyalUruvakkam.tsx` = Invoice Generator
- `Amaippugal.tsx` = Settings View
- `Vanigargal.tsx` = Clients View
- `Porul.tsx` = Inventory View (formerly Sarakku/Products)
- `Arikkaigal.tsx` = Reports View
- `Pakkapatti.tsx` = Sidebar Navigation
- `Thagaval.tsx` = Toast Notifications
- `Nalvaravu.tsx` = Welcome Guide

## Variable Guidelines
When editing code, you will also notice that internal variables and component state use Tanglish (e.g., `niruvanathinPeyar` instead of `businessName`, `mugavari` instead of `address`). 

**Rule for AI Agents:** Do not arbitrarily rename these variables back to English. Always match the surrounding Tanglish variable naming conventions to avoid breaking data persistence loops.
