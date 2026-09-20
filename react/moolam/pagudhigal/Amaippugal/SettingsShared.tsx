import React from "react";
import { CaretLeft, PencilSimple } from "@phosphor-icons/react";

// ═══════════════════════════════════════════════════════════════════════════════
// SHARED COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

export const SubHeader = ({ title, onBack }: { title: string, onBack: () => void }) => (
    <div className="s2-sub-header">
        <button className="s2-back-btn" onClick={onBack}>
            <CaretLeft weight="bold" size={22} />
        </button>
        <span className="s2-sub-title">{title}</span>
    </div>
);

export const SettingsGroup = ({ children }: { children: React.ReactNode }) => (
    <div className="s2-group">{children}</div>
);

export const SettingsDivider = () => <div className="s2-divider" />;

export const SettingsItem = ({ 
    icon, 
    iconColor = "", 
    title, 
    desc, 
    onClick, 
    danger 
}: { 
    icon?: React.ReactNode; 
    iconColor?: string; 
    title: string; 
    desc?: string; 
    onClick?: () => void; 
    danger?: boolean 
}) => (
    <button className="s2-item" onClick={onClick} style={!onClick ? { cursor: 'default' } : undefined}>
        {icon && <span className={`s2-icon-circle ${iconColor}`}>{icon}</span>}
        <div className="s2-item-text">
            <div className={`s2-item-title ${danger ? "danger" : ""}`}>
                {title}
            </div>
            {desc && <div className="s2-item-desc">{desc}</div>}
        </div>
    </button>
);

export const NotifItem = ({ 
    icon, 
    iconColor = "", 
    title, 
    desc, 
    checked, 
    onChange,
    control
}: { 
    icon?: React.ReactNode; 
    iconColor?: string; 
    title: string; 
    desc?: string; 
    checked?: boolean; 
    onChange?: (c: boolean) => void;
    control?: React.ReactNode;
}) => (
    <div className="s2-notif-item">
        {icon && <span className={`s2-icon-circle ${iconColor}`}>{icon}</span>}
        <div className="s2-item-text">
            <div className="s2-item-title">{title}</div>
            {desc && <div className="s2-item-desc">{desc}</div>}
        </div>
        {control ? control : <ToggleSwitch checked={!!checked} onChange={onChange!} />}
    </div>
);

export const ToggleSwitch = ({ checked, onChange }: { checked: boolean; onChange: (checked: boolean) => void }) => (
    <button
        className={`s2-toggle ${checked ? "on" : ""}`}
        onClick={() => (typeof onChange === "function" ? onChange(!checked) : null)}
    >
        <span className="s2-toggle-knob" />
    </button>
);

// ═══════════════════════════════════════════════════════════════════════════════
// PROFILE COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

export const ProfileSection = ({ title, icon, children }: { title: string, icon?: React.ReactNode, children: React.ReactNode }) => (
    <div className="s2-prof-section">
        <div className="s2-prof-section-header">
            {icon && <div className="s2-prof-section-icon">{icon}</div>}
            <span className="s2-prof-section-title">{title}</span>
        </div>
        {children}
    </div>
);

export const ProfileField = ({ 
    label, 
    value, 
    isEditing, 
    onEdit, 
    onCancel, 
    onSave, 
    children 
}: {
    label: string;
    value?: string;
    isEditing?: boolean;
    onEdit?: () => void;
    onCancel?: () => void;
    onSave?: () => void;
    children?: React.ReactNode;
}) => (
    <div className="s2-prof-field">
        {!isEditing ? (
            <div className="s2-prof-field-display">
                <div>
                    <div className="s2-prof-field-label">{label}</div>
                    <div className={`s2-prof-field-value ${!value ? 'empty' : ''}`}>{value || "Not set"}</div>
                </div>
                {onEdit && (
                    <button className="s2-prof-edit-btn" onClick={onEdit}>
                        <PencilSimple size={18} weight="bold" />
                    </button>
                )}
            </div>
        ) : (
            <div className="s2-prof-field-editing">
                {children}
                <div className="s2-prof-field-actions">
                    <button className="s2-prof-cancel-btn" onClick={onCancel}>Cancel</button>
                    <button className="s2-prof-save-btn" onClick={onSave}>Save</button>
                </div>
            </div>
        )}
    </div>
);
