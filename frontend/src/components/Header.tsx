import React from 'react';
import { ShieldCheck, PlusCircle, LayoutDashboard, RefreshCw } from 'lucide-react';

interface HeaderProps {
  activeTab: 'dashboard' | 'details';
  onNewClaimClick: () => void;
  onDashboardClick: () => void;
  onRefreshClick: () => void;
  isRefreshing?: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  activeTab,
  onNewClaimClick,
  onDashboardClick,
  onRefreshClick,
  isRefreshing,
}) => {
  return (
    <header className="bg-slate-900 border-b border-slate-800 text-white sticky top-0 z-30 shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <div 
          className="flex items-center gap-3 cursor-pointer group"
          onClick={onDashboardClick}
        >
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-500 flex items-center justify-center shadow-lg shadow-sky-500/20 group-hover:scale-105 transition-transform">
            <ShieldCheck className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="font-bold text-lg leading-tight tracking-tight text-white">TPA ClaimEngine</h1>
            <p className="text-xs text-slate-400 font-medium">Third-Party Administrator Adjudication Portal</p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-3">
          <button
            onClick={onRefreshClick}
            disabled={isRefreshing}
            className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors flex items-center gap-1.5 text-xs font-medium"
            title="Refresh Data"
          >
            <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin text-sky-400' : ''}`} />
            <span className="hidden sm:inline">Refresh</span>
          </button>

          <button
            onClick={onDashboardClick}
            className={`px-3 py-2 rounded-lg text-xs font-medium flex items-center gap-2 transition-colors ${
              activeTab === 'dashboard'
                ? 'bg-slate-800 text-sky-400 border border-slate-700'
                : 'text-slate-300 hover:bg-slate-800'
            }`}
          >
            <LayoutDashboard className="w-4 h-4" />
            Dashboard
          </button>

          <button
            onClick={onNewClaimClick}
            className="px-4 py-2 bg-gradient-to-r from-sky-500 to-blue-600 hover:from-sky-600 hover:to-blue-700 text-white rounded-lg text-xs font-semibold flex items-center gap-2 shadow-lg shadow-sky-500/25 transition-all hover:shadow-sky-500/40 active:scale-95"
          >
            <PlusCircle className="w-4 h-4" />
            Submit New Claim
          </button>
        </div>
      </div>
    </header>
  );
};
