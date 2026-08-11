import React from 'react';
import { ClaimStatus } from '../types/claim';
import { CheckCircle2, AlertTriangle, XCircle, Clock } from 'lucide-react';

interface StatusBadgeProps {
  status: ClaimStatus;
  size?: 'sm' | 'md' | 'lg';
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, size = 'md' }) => {
  switch (status) {
    case 'APPROVED':
      return (
        <span className={`inline-flex items-center gap-1.5 rounded-full font-semibold bg-emerald-500/10 text-emerald-700 border border-emerald-500/30 ${
          size === 'sm' ? 'px-2.5 py-0.5 text-xs' : size === 'lg' ? 'px-4 py-1.5 text-sm' : 'px-3 py-1 text-xs'
        }`}>
          <CheckCircle2 className={size === 'sm' ? 'w-3.5 h-3.5' : 'w-4 h-4'} />
          APPROVED
        </span>
      );

    case 'REJECTED':
      return (
        <span className={`inline-flex items-center gap-1.5 rounded-full font-semibold bg-rose-500/10 text-rose-700 border border-rose-500/30 ${
          size === 'sm' ? 'px-2.5 py-0.5 text-xs' : size === 'lg' ? 'px-4 py-1.5 text-sm' : 'px-3 py-1 text-xs'
        }`}>
          <XCircle className={size === 'sm' ? 'w-3.5 h-3.5' : 'w-4 h-4'} />
          REJECTED
        </span>
      );

    case 'NEEDS_MANUAL_REVIEW':
      return (
        <span className={`inline-flex items-center gap-1.5 rounded-full font-semibold bg-amber-500/10 text-amber-800 border border-amber-500/30 ${
          size === 'sm' ? 'px-2.5 py-0.5 text-xs' : size === 'lg' ? 'px-4 py-1.5 text-sm' : 'px-3 py-1 text-xs'
        }`}>
          <AlertTriangle className={size === 'sm' ? 'w-3.5 h-3.5' : 'w-4 h-4'} />
          MANUAL REVIEW
        </span>
      );

    case 'PENDING':
    default:
      return (
        <span className={`inline-flex items-center gap-1.5 rounded-full font-semibold bg-slate-500/10 text-slate-700 border border-slate-500/30 ${
          size === 'sm' ? 'px-2.5 py-0.5 text-xs' : size === 'lg' ? 'px-4 py-1.5 text-sm' : 'px-3 py-1 text-xs'
        }`}>
          <Clock className={size === 'sm' ? 'w-3.5 h-3.5' : 'w-4 h-4'} />
          PENDING
        </span>
      );
  }
};
