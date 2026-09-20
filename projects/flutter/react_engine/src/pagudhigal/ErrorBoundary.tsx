import React from 'react';

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends React.Component<{ children: React.ReactNode }, ErrorBoundaryState> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('React ErrorBoundary caught:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          padding: '24px',
          fontFamily: "'Elvan Sans', system-ui, sans-serif",
          background: '#111',
          color: '#fff'
        }}>
          <div style={{ fontSize: '48px', marginBottom: '16px' }}>⚠️</div>
          <h2 style={{ margin: '0 0 8px', fontSize: '18px', fontWeight: 600 }}>Something went wrong</h2>
          <p style={{ margin: '0 0 16px', fontSize: '13px', color: '#aaa', textAlign: 'center', maxWidth: '300px' }}>
            {this.state.error?.message || 'An unexpected error occurred while rendering the view.'}
          </p>
          <button
            onClick={() => {
              if ((window as any).FlutterBridge?.closeInvoice) {
                (window as any).FlutterBridge.closeInvoice();
              } else if ((window as any).FlutterBridge?.closeReceipt) {
                (window as any).FlutterBridge.closeReceipt();
              } else {
                window.history.back();
              }
            }}
            style={{
              padding: '10px 24px',
              background: '#333',
              color: '#fff',
              border: '1px solid #555',
              borderRadius: '8px',
              fontSize: '14px',
              cursor: 'pointer'
            }}
          >
            Go Back
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
