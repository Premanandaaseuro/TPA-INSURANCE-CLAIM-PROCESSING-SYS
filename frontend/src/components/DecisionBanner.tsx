import React from 'react';
import { ClaimStatus } from '../types/claim';
import { CheckCircle2, XCircle, AlertTriangle } from 'lucide-react';

interface DecisionBannerProps {
  status: ClaimStatus;
  decisionReason?: string;
}

const FALLBACK_REASON: Record<ClaimStatus, string> = {
  APPROVED: 'All mandatory validation rules passed.',
  REJECTED: 'Claim rejected based on mandatory validation rules.',
  NEEDS_MANUAL_REVIEW: 'Claim requires manual review.',
  PENDING: 'Claim is pending processing.',
};

export const DecisionBanner: React.FC<DecisionBannerProps> = ({ status, decisionReason }) => {
  const reason = decisionReason?.trim() || FALLBACK_REASON[status];

  if (status === 'APPROVED') {
    return (
      <div className="rounded-2xl border border-emerald-300 bg-emerald-50 p-5">
        <div className="flex items-center gap-3 mb-2">
          <div className="flex items-center justify-center w-9 h-9 rounded-full bg-emerald-100 border border-emerald-300 shrink-0">
            <CheckCircle2 className="w-5 h-5 text-emerald-600" />
          </div>
          <span className="text-base font-black text-emerald-800 tracking-tight">✓ APPROVED</span>
        </div>
        <p className="text-sm text-emerald-700 font-medium leading-relaxed pl-12">
          {reason}
        </p>
      </div>
    );
  }

  if (status === 'REJECTED') {
    return (
      <div className="rounded-2xl border border-rose-300 bg-rose-50 p-5">
        <div className="flex items-center gap-3 mb-2">
          <div className="flex items-center justify-center w-9 h-9 rounded-full bg-rose-100 border border-rose-300 shrink-0">
            <XCircle className="w-5 h-5 text-rose-600" />
          </div>
          <span className="text-base font-black text-rose-800 tracking-tight">✕ REJECTED</span>
        </div>
        <p className="text-sm text-rose-700 font-medium leading-relaxed pl-12">
          {reason}
        </p>
      </div>
    );
  }

  if (status === 'NEEDS_MANUAL_REVIEW') {
    return (
      <div className="rounded-2xl border border-amber-300 bg-amber-50 p-5">
        <div className="flex items-center gap-3 mb-2">
          <div className="flex items-center justify-center w-9 h-9 rounded-full bg-amber-100 border border-amber-300 shrink-0">
            <AlertTriangle className="w-5 h-5 text-amber-600" />
          </div>
          <span className="text-base font-black text-amber-800 tracking-tight">⚠ NEEDS MANUAL REVIEW</span>
        </div>
        <p className="text-sm text-amber-700 font-medium leading-relaxed pl-12">
          {reason}
        </p>
      </div>
    );
  }

  return null;
};
