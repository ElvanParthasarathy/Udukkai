          {/* Terms and notes accept rich HTML; same DOMPurify sanitization the extraSections
              block uses, which keeps b/i/u/lists/links/headings and strips everything else. */}
          {(() => {
            const termsHtml = customTerms ? DOMPurify.sanitize(customTerms) : '';
            const hasTerms = termsHtml && termsHtml.replace(/<[^>]*>/g, '').trim();
            return showTerms && hasTerms ? (
              <div className="inv-footer-block">
                <h4 className="inv-section-label">TERMS & CONDITIONS</h4>
                <div className="inv-terms inv-rich" dangerouslySetInnerHTML={{ __html: termsHtml }} />
              </div>
            ) : null;
          })()}
