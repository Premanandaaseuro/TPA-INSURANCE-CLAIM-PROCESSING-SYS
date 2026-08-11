import React from 'react';
import { ClaimResponseDto } from '../types/claim';
import { StatusBadge } from './StatusBadge';
import { DecisionBanner } from './DecisionBanner';
import { getExportPdfUrl } from '../services/api';
import {
  ArrowLeft,
  Download,
  ShieldCheck,
  User,
  Building2,
  Calendar,
  IndianRupee,
  FileCheck,
  FileSpreadsheet,
  CheckCircle2,
  XCircle,
  AlertTriangle,
} from 'lucide-react';

interface ClaimDetailsViewProps {
  claim: ClaimResponseDto;
  onBack: () => void;
}

export const ClaimDetailsView: React.FC<ClaimDetailsViewProps> = ({ claim, onBack }) => {
  const exportUrl = getExportPdfUrl(claim.claimId);

  return (
    <div className="space-y-6">
      {/* Top Bar: Back & Download PDF */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <button
          onClick={onBack}
          className="inline-flex items-center gap-2 text-xs font-semibold text-slate-600 hover:text-slate-900 bg-white border border-slate-200 px-3.5 py-2 rounded-xl shadow-sm hover:shadow transition-all"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Dashboard
        </button>

        <a
          href={exportUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-2 text-xs font-bold text-white bg-slate-900 hover:bg-slate-800 px-4 py-2.5 rounded-xl shadow-md transition-all active:scale-95"
        >
          <Download className="w-4 h-4 text-sky-400" />
          Export Summary PDF
        </a>
      </div>

      {/* Decision Header Card */}
      <div className={`bg-white rounded-3xl p-6 border shadow-sm relative overflow-hidden ${
        claim.status === 'APPROVED'
          ? 'border-emerald-200'
          : claim.status === 'REJECTED'
          ? 'border-rose-200'
          : claim.status === 'NEEDS_MANUAL_REVIEW'
          ? 'border-amber-200'
          : 'border-slate-200'
      }`}>
        {/* Claim ID + Status Badge row */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-slate-100">
          <div>
            <div className="flex items-center gap-3 flex-wrap">
              <h2 className="text-2xl font-black text-slate-900 tracking-tight">{claim.claimId}</h2>
              <StatusBadge status={claim.status} size="lg" />
            </div>
            <p className="text-xs text-slate-500 mt-1">
              Submitted on {new Date(claim.createdAt).toLocaleString()}
              {claim.processedAt && ` · Processed ${new Date(claim.processedAt).toLocaleString()}`}
            </p>
          </div>
        </div>

        {/* Decision Banner — colour-coded APPROVED / REJECTED / NEEDS_MANUAL_REVIEW */}
        <div className="mt-4">
          <DecisionBanner status={claim.status} decisionReason={claim.decisionReason} />
        </div>
      </div>

      {/* Grid: Extracted Structured Data */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-1">
          <div className="flex items-center gap-2 text-xs font-medium text-slate-400 uppercase tracking-wider">
            <ShieldCheck className="w-4 h-4 text-sky-500" />
            Policy Information
          </div>
          <p className="text-base font-bold text-slate-900">{claim.policyNumber || 'Unextracted'}</p>
          <p className="text-xs text-slate-500 font-medium">{claim.carrierName || 'Standard Carrier'}</p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-1">
          <div className="flex items-center gap-2 text-xs font-medium text-slate-400 uppercase tracking-wider">
            <User className="w-4 h-4 text-indigo-500" />
            Patient & Customer
          </div>
          <p className="text-base font-bold text-slate-900">{claim.patientName || 'N/A'}</p>
          <p className="text-xs text-slate-500 font-medium">Customer: {claim.customerName || 'N/A'}</p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-1">
          <div className="flex items-center gap-2 text-xs font-medium text-slate-400 uppercase tracking-wider">
            <Building2 className="w-4 h-4 text-amber-500" />
            Hospital & Dates
          </div>
          <p className="text-base font-bold text-slate-900 truncate">{claim.hospitalName || 'N/A'}</p>
          <p className="text-xs text-slate-500 font-medium">
            {claim.admissionDate || '?'} to {claim.dischargeDate || '?'}
          </p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-1">
          <div className="flex items-center gap-2 text-xs font-medium text-slate-400 uppercase tracking-wider">
            <IndianRupee className="w-4 h-4 text-emerald-500" />
            Claimed Amount
          </div>
          <p className="text-base font-black text-slate-900">
            {claim.claimedAmount ? `₹${claim.claimedAmount.toLocaleString()}` : 'N/A'}
          </p>
          <p className="text-xs text-slate-500 font-medium">Type: {claim.claimType || 'REIMBURSEMENT'}</p>
        </div>
      </div>

      {/* 10 Business Rules Audit Log */}
      <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <FileCheck className="w-5 h-5 text-sky-600" />
              Automated Business Rules Audit Trail (R01 – R10)
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">
              Deterministic rule evaluation results and priority matrix assessment.
            </p>
          </div>
        </div>

        {claim.ruleResults && claim.ruleResults.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/80 border-b border-slate-200 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3.5 px-4">Rule ID</th>
                  <th className="py-3.5 px-4">Rule Description</th>
                  <th className="py-3.5 px-4">Result</th>
                  <th className="py-3.5 px-4">Severity</th>
                  <th className="py-3.5 px-4">Audit Details / Trigger Reason</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {claim.ruleResults.map((rule) => {
                  const rowBg = !rule.passed
                    ? rule.severity === 'REJECTED'
                      ? 'bg-rose-50/40'
                      : 'bg-amber-50/40'
                    : '';

                  return (
                    <tr key={rule.ruleCode} className={`hover:bg-slate-50/60 transition-colors ${rowBg}`}>
                      <td className="py-3.5 px-4 font-mono font-bold text-slate-800">
                        {rule.ruleCode}
                      </td>
                      <td className="py-3.5 px-4 font-semibold text-slate-900">
                        {rule.ruleName}
                      </td>
                      <td className="py-3.5 px-4">
                        {rule.passed ? (
                          <span className="inline-flex items-center gap-1 text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full text-[11px] font-bold border border-emerald-200">
                            <CheckCircle2 className="w-3.5 h-3.5" />
                            PASS
                          </span>
                        ) : rule.severity === 'REJECTED' ? (
                          <span className="inline-flex items-center gap-1 text-rose-700 bg-rose-50 px-2.5 py-1 rounded-full text-[11px] font-bold border border-rose-200">
                            <XCircle className="w-3.5 h-3.5" />
                            FAIL
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 text-amber-700 bg-amber-50 px-2.5 py-1 rounded-full text-[11px] font-bold border border-amber-200">
                            <AlertTriangle className="w-3.5 h-3.5" />
                            FAIL
                          </span>
                        )}
                      </td>
                      <td className="py-3.5 px-4">
                        {rule.severity === 'REJECTED' ? (
                          <span className="inline-block px-2 py-0.5 rounded text-[10px] font-bold bg-rose-100 text-rose-700 border border-rose-200">REJECTED</span>
                        ) : rule.severity === 'NEEDS_MANUAL_REVIEW' ? (
                          <span className="inline-block px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-700 border border-amber-200">MANUAL REVIEW</span>
                        ) : (
                          <span className="text-slate-400 font-medium text-[11px]">—</span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-slate-600 font-medium">
                        {rule.details || 'Evaluated successfully.'}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="p-8 text-center text-xs text-slate-500">
            No rule execution log records found.
          </div>
        )}
      </div>

      {/* Attached PDF Documents List */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm space-y-4">
        <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
          <FileSpreadsheet className="w-4 h-4 text-sky-600" />
          Ingested Document Payloads
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {claim.documents.map((doc) => (
            <div key={doc.id} className="p-4 rounded-2xl bg-slate-50 border border-slate-200 flex items-center justify-between">
              <div className="space-y-0.5">
                <p className="text-xs font-bold text-slate-900">{doc.originalFilename}</p>
                <p className="text-[11px] text-slate-500 font-medium">
                  Type: <span className="font-semibold text-slate-700">{doc.documentType}</span>
                </p>
                {doc.checksumSha256 && (
                  <p className="text-[10px] text-slate-400 font-mono truncate max-w-xs">
                    SHA256: {doc.checksumSha256.substring(0, 16)}...
                  </p>
                )}
              </div>
              <span className="px-2.5 py-1 bg-white text-slate-600 text-[10px] font-bold rounded-lg border border-slate-200 shadow-2xs">
                {(doc.fileSize ? doc.fileSize / 1024 : 0).toFixed(1)} KB
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
