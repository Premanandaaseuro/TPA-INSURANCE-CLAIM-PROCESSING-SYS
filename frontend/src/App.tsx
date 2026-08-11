import React, { useEffect, useState } from 'react';
import { Header } from './components/Header';
import { ClaimsDashboard } from './components/ClaimsDashboard';
import { ClaimDetailsView } from './components/ClaimDetailsView';
import { NewClaimModal } from './components/NewClaimModal';
import { fetchClaims, fetchClaimByClaimId } from './services/api';
import { ClaimResponseDto } from './types/claim';
import { AlertCircle, ShieldAlert } from 'lucide-react';

export const App: React.FC = () => {
  const [claims, setClaims] = useState<ClaimResponseDto[]>([]);
  const [selectedClaim, setSelectedClaim] = useState<ClaimResponseDto | null>(null);
  const [activeTab, setActiveTab] = useState<'dashboard' | 'details'>('dashboard');
  const [isNewClaimModalOpen, setIsNewClaimModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

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
        isRefreshing={isRefreshing}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {errorMessage && (
          <div className="mb-6 p-4 bg-rose-50 border border-rose-200 rounded-2xl text-rose-800 text-xs flex items-start gap-3 shadow-sm">
            <ShieldAlert className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
            <div className="flex-1">
              <h4 className="font-bold text-slate-900">Backend Communication Warning</h4>
              <p className="mt-0.5">{errorMessage}</p>
            </div>
            <button
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
