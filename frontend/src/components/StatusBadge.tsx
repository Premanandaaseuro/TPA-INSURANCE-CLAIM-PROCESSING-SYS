import React from 'react';
import { ClaimStatus } from '../types/claim';
import { CheckCircle2, AlertTriangle, XCircle, Clock } from 'lucide-react';

interface StatusBadgeProps {
  status: ClaimStatus;
  size?: 'sm' | 'md' | 'lg';
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, size = 'md' }) => {
  const sizeClass = size === 'sm'
    ? 'px-2.5 py-0.5 text-xs'
    : size === 'lg'
    ? 'px-4 py-1.5 text-sm'
    : 'px-3 py-1 text-xs';

  const iconSize = size === 'sm' ? 'w-3.5 h-3.5' : 'w-4 h-4';

  switch (status) {
    case 'APPROVED':
      return (
        <span className={`inline-flex items-center gap-1.5 rounded-full font-bold bg-emerald-50 text-emerald-700 border border-emerald-300 ${sizeClass}`}>
          <CheckCircle2 className={iconSize} />
          APPROVED
        </span>
      );

    case 'REJECTED':
      return (
        <span className={`inline-flex items-center gap-1.5 rounded-full font-bold bg-rose-50 text-rose-700 border border-rose-300 ${sizeClass}`}>
          <XCircle className={iconSize} />
          REJECTED
        </span>
      );

    case 'NEEDS_MANUAL_REVIEW':
      return (
        <span className={`inline-flex items-center gap-1.5 rounded-full font-bold bg-amber-50 text-amber-700 border border-amber-300 ${sizeClass}`}>
          <AlertTriangle className={iconSize} />
          MANUAL REVIEW
        </span>
      );

    case 'PENDING':
    default:
      return (
        <span className={`inline-flex items-center gap-1.5 rounded-full font-bold bg-slate-100 text-slate-600 border border-slate-300 ${sizeClass}`}>
          <Clock className={iconSize} />
          PENDING
        </span>
      );
  }
};
