const fs = require('fs');
let v = fs.readFileSync('moolam/pagudhigal/Vanigargal.tsx', 'utf-8');

const brokenBlock = `    <div className="dashboard-container">
      <div className="page-header">
        <div>
            const stats = getClientStats(clientName);
            const savedClient = clients.find(c => c.name === clientName);
            const isExpanded = expandedClient === clientName;
            const clientBills = isExpanded ? getClientBills(clientName) : [];

            return (
              <div key={clientName} className="glass-panel mb-4" style={{ overflow: 'hidden' }}>`;

const fixedBlock = `    <div className="dashboard-container">
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
              <div key={clientName} className="glass-panel mb-4" style={{ overflow: 'hidden' }}>`;

if (v.includes(brokenBlock)) {
  v = v.replace(brokenBlock, fixedBlock);
  fs.writeFileSync('moolam/pagudhigal/Vanigargal.tsx', v, 'utf-8');
  console.log('Successfully recovered the UI section.');
} else {
  console.log('Broken block not found. Checking exactly what is there...');
  const idx = v.indexOf('<div className="dashboard-container">');
  console.log(v.substring(idx, idx + 500));
}
