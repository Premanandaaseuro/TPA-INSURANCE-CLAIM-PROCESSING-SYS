import React, { useEffect, useState } from 'react';
import { Header } from './components/Header';
import { ClaimsDashboard } from './components/ClaimsDashboard';
import { ClaimDetailsView } from './components/ClaimDetailsView';
import { NewClaimModal } from './components/NewClaimModal';
import { fetchClaims, fetchClaimByClaimId, clearAllClaimData } from './services/api';
import { ClaimResponseDto } from './types/claim';
import { CheckCircle2, ShieldAlert, Trash2 } from 'lucide-react';

export const App: React.FC = () => {
  const [claims, setClaims] = useState<ClaimResponseDto[]>([]);
  const [selectedClaim, setSelectedClaim] = useState<ClaimResponseDto | null>(null);
  const [activeTab, setActiveTab] = useState<'dashboard' | 'details'>('dashboard');
  const [isNewClaimModalOpen, setIsNewClaimModalOpen] = useState(false);
  const [isClearModalOpen, setIsClearModalOpen] = useState(false);
  const [isClearing, setIsClearing] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadClaims = async () => {
    try {
      setIsRefreshing(true);
      const data = await fetchClaims();
      setClaims(data);
      setErrorMessage(null);
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to connect to claim processor backend.');
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    loadClaims();
  }, []);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setIsNewClaimModalOpen(false);
        setIsClearModalOpen(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const handleSelectClaim = async (claimId: string) => {
    try {
      setIsLoading(true);
      const details = await fetchClaimByClaimId(claimId);
      setSelectedClaim(details);
      setActiveTab('details');
      setErrorMessage(null);
    } catch (err: any) {
      setErrorMessage(`Failed to load details for ${claimId}: ${err.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleNewClaimSuccess = (newClaim: ClaimResponseDto) => {
    setIsNewClaimModalOpen(false);
    setClaims((prev) => [newClaim, ...prev]);
    setSelectedClaim(newClaim);
    setActiveTab('details');
  };

  const handleConfirmClearData = async () => {
    try {
      setIsClearing(true);
      const res = await clearAllClaimData();
      setSuccessMessage(res.message || "All claim test data cleared successfully.");
      setClaims([]);
      setSelectedClaim(null);
      setActiveTab('dashboard');
      setIsClearModalOpen(false);
    } catch (err: any) {
      setErrorMessage(`Failed to clear claim test data: ${err.message}`);
    } finally {
      setIsClearing(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col font-[Inter]">
      {/* Navigation Header */}
      <Header
        activeTab={activeTab}
        onNewClaimClick={() => setIsNewClaimModalOpen(true)}
        onDashboardClick={() => {
          setActiveTab('dashboard');
          setSelectedClaim(null);
        }}
        onRefreshClick={loadClaims}
        onClearDataClick={() => setIsClearModalOpen(true)}
        isRefreshing={isRefreshing}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {successMessage && (
          <div className="mb-6 p-4 bg-emerald-50 border border-emerald-200 rounded-2xl text-emerald-800 text-xs flex items-center justify-between shadow-sm" data-testid="success-banner">
            <div className="flex items-center gap-2.5">
              <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
              <span className="font-semibold" data-testid="success-banner-message">{successMessage}</span>
            </div>
            <button
              data-testid="dismiss-success-banner"
              onClick={() => setSuccessMessage(null)}
              className="text-emerald-700 hover:text-emerald-900 text-xs font-bold"
            >
              Dismiss
            </button>
          </div>
        )}

        {errorMessage && (
          <div className="mb-6 p-4 bg-rose-50 border border-rose-200 rounded-2xl text-rose-800 text-xs flex items-start gap-3 shadow-sm" data-testid="error-banner">
            <ShieldAlert className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
            <div className="flex-1">
              <h4 className="font-bold text-slate-900">Backend Communication Warning</h4>
              <p className="mt-0.5">{errorMessage}</p>
            </div>
            <button
              data-testid="retry-connection-button"
              onClick={loadClaims}
              className="px-3 py-1.5 bg-rose-600 text-white rounded-lg text-[11px] font-bold hover:bg-rose-700 transition-colors"
            >
              Retry Connection
            </button>
          </div>
        )}

        {activeTab === 'dashboard' || !selectedClaim ? (
          <ClaimsDashboard
            claims={claims}
            isLoading={isLoading}
            onSelectClaim={handleSelectClaim}
            onNewClaimClick={() => setIsNewClaimModalOpen(true)}
          />
        ) : (
          <ClaimDetailsView
            claim={selectedClaim}
            onBack={() => {
              setActiveTab('dashboard');
              setSelectedClaim(null);
            }}
          />
        )}
      </main>

      {/* Clear Data Confirmation Modal */}
      {isClearModalOpen && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-fade-in" data-testid="clear-data-modal">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-100 space-y-4">
            <div className="w-12 h-12 rounded-full bg-rose-100 text-rose-600 flex items-center justify-center mx-auto">
              <Trash2 className="w-6 h-6" />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-base font-bold text-slate-900">Clear All Claim Test Data?</h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                Are you sure? This will permanently clear all local claim test data.
              </p>
            </div>

            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 text-[11px] text-slate-600 space-y-1">
              <p className="font-semibold text-slate-700">What will be cleared:</p>
              <ul className="list-disc list-inside space-y-0.5 text-slate-500">
                <li>Submitted claim records and Claim ID sequences</li>
                <li>Extracted JSON and rule audit results</li>
                <li>Uploaded claim documents and discharge details</li>
              </ul>
              <p className="pt-1 text-[10px] text-slate-400">Policies and database schema will remain intact.</p>
            </div>

            <div className="flex items-center gap-3 pt-2">
              <button
                data-testid="cancel-clear-data-button"
                type="button"
                onClick={() => setIsClearModalOpen(false)}
                disabled={isClearing}
                className="flex-1 py-2.5 px-4 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-xl transition-colors"
              >
                Cancel
              </button>
              <button
                data-testid="confirm-clear-data-button"
                type="button"
                onClick={handleConfirmClearData}
                disabled={isClearing}
                className="flex-1 py-2.5 px-4 bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold rounded-xl shadow-lg shadow-rose-500/20 transition-all flex items-center justify-center gap-2"
              >
                {isClearing ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    <span>Clearing...</span>
                  </>
                ) : (
                  <span>Clear Data</span>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 py-6 mt-auto">
        <div className="max-w-7xl mx-auto px-4 text-center text-xs text-slate-400 font-medium">
          TPA Health Insurance Claim Processing System • Production-Grade Modular Monolith Architecture
        </div>
      </footer>

      {/* Upload Wizard Modal */}
      <NewClaimModal
        isOpen={isNewClaimModalOpen}
        onClose={() => setIsNewClaimModalOpen(false)}
        onSuccess={handleNewClaimSuccess}
      />
    </div>
  );
};
