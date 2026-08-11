import React, { useState } from 'react';
import { ClaimResponseDto, ClaimStatus } from '../types/claim';
import { StatusBadge } from './StatusBadge';
import { MetricCard } from './MetricCard';
import { Search, FileText, CheckCircle, AlertTriangle, XCircle, ArrowRight, Layers } from 'lucide-react';

interface ClaimsDashboardProps {
  claims: ClaimResponseDto[];
  isLoading: boolean;
  onSelectClaim: (claimId: string) => void;
  onNewClaimClick: () => void;
}

export const ClaimsDashboard: React.FC<ClaimsDashboardProps> = ({
  claims,
  isLoading,
  onSelectClaim,
  onNewClaimClick,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | ClaimStatus>('ALL');

  const totalClaims = claims.length;
  const approvedClaims = claims.filter(c => c.status === 'APPROVED').length;
  const reviewClaims = claims.filter(c => c.status === 'NEEDS_MANUAL_REVIEW').length;
  const rejectedClaims = claims.filter(c => c.status === 'REJECTED').length;

  const filteredClaims = claims.filter(claim => {
    const matchesSearch =
      (claim.claimId && claim.claimId.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (claim.patientName && claim.patientName.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (claim.policyNumber && claim.policyNumber.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (claim.hospitalName && claim.hospitalName.toLowerCase().includes(searchTerm.toLowerCase()));

    const matchesStatus = statusFilter === 'ALL' || claim.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      {/* Top Banner / Metrics Overview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Total Submissions"
          value={totalClaims}
          subtitle="Processed claims"
          icon={Layers}
          iconBg="bg-sky-500/10"
          iconColor="text-sky-600"
        />
        <MetricCard
          title="Auto Approved"
          value={approvedClaims}
          subtitle="Zero rule violations"
          icon={CheckCircle}
          iconBg="bg-emerald-500/10"
          iconColor="text-emerald-600"
        />
        <MetricCard
          title="Needs Review"
          value={reviewClaims}
          subtitle="Flagged for manual audit"
          icon={AlertTriangle}
          iconBg="bg-amber-500/10"
          iconColor="text-amber-600"
        />
        <MetricCard
          title="Rejected Claims"
          value={rejectedClaims}
          subtitle="Policy/document failed"
          icon={XCircle}
          iconBg="bg-rose-500/10"
          iconColor="text-rose-600"
        />
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-sm flex flex-col md:flex-row items-center justify-between gap-4">
        {/* Search */}
        <div className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
          <input
            type="text"
            placeholder="Search Claim ID, Patient, Policy..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all"
          />
        </div>

        {/* Status Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto w-full md:w-auto pb-1 md:pb-0">
          {(['ALL', 'APPROVED', 'NEEDS_MANUAL_REVIEW', 'REJECTED'] as const).map((st) => (
            <button
              key={st}
              onClick={() => setStatusFilter(st)}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                statusFilter === st
                  ? 'bg-slate-900 text-white shadow-sm'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {st === 'ALL' ? 'All Claims' : st === 'NEEDS_MANUAL_REVIEW' ? 'Review Queue' : st}
            </button>
          ))}
        </div>
      </div>

      {/* Claims List Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-12 text-center">
            <div className="inline-block w-8 h-8 border-3 border-sky-500 border-t-transparent rounded-full animate-spin"></div>
            <p className="mt-3 text-xs font-medium text-slate-500">Loading claims database...</p>
          </div>
        ) : filteredClaims.length === 0 ? (
          <div className="p-12 text-center">
            <FileText className="w-12 h-12 text-slate-300 mx-auto mb-3" />
            <h3 className="text-sm font-bold text-slate-800">No claims found</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1">
              {claims.length === 0
                ? "No insurance claims have been ingested yet. Click below to submit your first claim."
                : "No claims matched your search or status filter criteria."}
            </p>
            {claims.length === 0 && (
              <button
                onClick={onNewClaimClick}
                className="mt-4 px-4 py-2 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-xl shadow-sm transition-colors"
              >
                Submit First Claim
              </button>
            )}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/80 border-b border-slate-200 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3.5 px-4">Claim ID</th>
                  <th className="py-3.5 px-4">Patient / Policy</th>
                  <th className="py-3.5 px-4">Hospital Name</th>
                  <th className="py-3.5 px-4">Claimed Amount</th>
                  <th className="py-3.5 px-4">Status</th>
                  <th className="py-3.5 px-4">Processed Date</th>
                  <th className="py-3.5 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {filteredClaims.map((claim) => (
                  <tr
                    key={claim.claimId}
                    className="hover:bg-slate-50/60 transition-colors group cursor-pointer"
                    onClick={() => onSelectClaim(claim.claimId)}
                  >
                    <td className="py-3.5 px-4 font-bold text-sky-600 group-hover:underline">
                      {claim.claimId}
                    </td>
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-slate-900">{claim.patientName || 'N/A'}</div>
                      <div className="text-[11px] text-slate-500 font-mono">{claim.policyNumber || 'Policy Unextracted'}</div>
                    </td>
                    <td className="py-3.5 px-4 text-slate-700 font-medium">
                      {claim.hospitalName || 'N/A'}
                    </td>
                    <td className="py-3.5 px-4 font-bold text-slate-900">
                      {claim.claimedAmount ? `₹${claim.claimedAmount.toLocaleString()}` : 'N/A'}
                    </td>
                    <td className="py-3.5 px-4">
                      <StatusBadge status={claim.status} size="sm" />
                    </td>
                    <td className="py-3.5 px-4 text-slate-500 text-[11px]">
                      {new Date(claim.createdAt).toLocaleDateString()}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onSelectClaim(claim.claimId);
                        }}
                        className="inline-flex items-center gap-1 text-xs font-semibold text-sky-600 hover:text-sky-800 bg-sky-50 hover:bg-sky-100 px-3 py-1.5 rounded-lg transition-colors"
                      >
                        View Audit
                        <ArrowRight className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
