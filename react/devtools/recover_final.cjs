const fs = require('fs');

let content = fs.readFileSync('moolam/pagudhigal/Vanigargal.tsx', 'utf-8');

// The corrupted code chunk currently looks like this:
const corruptedChunk = `  const shareEmail = (bill) => {
                    </div>
                    <div>
                      <h3 className="client-card-name">{clientName}</h3>`;

const validChunk = `  const shareEmail = (bill) => {
    const subject = \`Invoice \${bill.invoiceNumber}\`;
    const body = \`Dear \${bill.clientName},\\n\\nPlease find the details of your invoice:\\n\\nInvoice No: \${bill.invoiceNumber}\\nAmount: \${formatCurrency(bill.totalAmount)}\\nDate: \${new Date(bill.invoiceDate).toLocaleDateString('en-IN')}\\nDue: \${bill.status === 'paid' ? 'Paid' : 'Pending'}\\n\\nRegards\`;
    window.open(\`mailto:?subject=\${encodeURIComponent(subject)}&body=\${encodeURIComponent(body)}\`, '_blank');
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">{t('clientsTitle')}</h1>
          <p className="page-subtitle">{t('clientsSubtitle')}</p>
        </div>
        <div className="flex gap-2">
          <input type="file" accept=".csv" ref={csvInputRef} style={{ display: 'none' }} onChange={handleCSVImport} />
          <button className="btn btn-secondary" onClick={() => csvInputRef.current?.click()}>
            <Upload size={16} /> {t('importCSV')}
          </button>
          <button className="btn btn-secondary" onClick={openAddClient}>
            <Plus size={18} /> {t('addClient')}
          </button>
          <button className="btn btn-primary" onClick={onNew}>
            <FileText size={18} /> {t('newInvoiceBtn')}
          </button>
        </div>
      </div>

      {/* Add/Edit Client Modal */}
      <VanigarThirai show={showForm} onClose={closeForm} onSave={handleModalSave} client={modalClient} isEditing={!!editingClientId} defaultCountry={profileCountry} />

      {/* Search */}
      <div className="glass-panel p-4 mb-6">
        <div className="search-box" style={{ maxWidth: '400px' }}>
          <Search size={16} className="search-icon" />
          <input type="text" placeholder={t('searchClients')} value={search}
            onChange={e => setSearch(e.target.value)} className="search-input" />
          {search && <button className="icon-btn" onClick={() => setSearch('')}><X size={14} /></button>}
        </div>
      </div>

      {/* Client cards */}
      {sortedClients.length === 0 ? (
        <div className="glass-panel p-6">
          <div className="empty-maanilam">
            <Users size={48} />
            <p>{t('noClientsFound')}</p>
            <button className="btn btn-secondary" onClick={openAddClient} style={{ marginTop: '0.5rem' }}>
              <Plus size={16} /> {t('addFirstClient')}
            </button>
          </div>
        </div>
      ) : (
        <div className="client-list">
          {sortedClients.map(clientName => {
            const stats = getClientStats(clientName);
            const savedClient = clients.find(c => c.name === clientName);
            const isExpanded = expandedClient === clientName;
            const clientBills = isExpanded ? getClientBills(clientName) : [];

            return (
              <div key={clientName} className="glass-panel mb-4" style={{ overflow: 'hidden' }}>
                {/* Client header */}
                <div className="client-card-header" onClick={() => setExpandedClient(isExpanded ? null : clientName)}>
                  <div className="client-card-info">
                    <div className="client-avatar">
                      {clientName.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <h3 className="client-card-name">{clientName}</h3>`;

content = content.replace(corruptedChunk, validChunk);
fs.writeFileSync('moolam/pagudhigal/Vanigargal.tsx', content, 'utf-8');
console.log('Recovery completed!');
