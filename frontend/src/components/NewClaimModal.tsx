import React, { useState } from 'react';
import { submitClaim } from '../services/api';
import { ClaimResponseDto } from '../types/claim';
import { X, UploadCloud, FileText, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';

interface NewClaimModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (newClaim: ClaimResponseDto) => void;
}

export const NewClaimModal: React.FC<NewClaimModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const [claimFormFile, setClaimFormFile] = useState<File | null>(null);
  const [combinedDocFile, setCombinedDocFile] = useState<File | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!claimFormFile && !combinedDocFile) {
      setErrorMessage('At least one claim document is required to submit.');
      return;
    }

    try {
      setIsSubmitting(true);
      const newClaim = await submitClaim(claimFormFile, combinedDocFile);
      setIsSubmitting(false);
      onSuccess(newClaim);
    } catch (err: any) {
      setIsSubmitting(false);
      setErrorMessage(err.message || 'An unexpected error occurred during claim processing.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
      <div className="bg-white rounded-3xl max-w-xl w-full border border-slate-200 shadow-2xl overflow-hidden">
        {/* Modal Header */}
        <div className="px-6 py-4 bg-slate-900 text-white flex items-center justify-between">
          <div>
            <h2 className="font-bold text-base">Submit New Claim Payload</h2>
            <p className="text-xs text-slate-400">Upload exactly two mandatory claim documents for automated AI/OCR extraction.</p>
          </div>
          <button
            onClick={onClose}
            disabled={isSubmitting}
            className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body / Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {errorMessage && (
            <div className="p-3.5 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-xs flex items-start gap-2.5">
              <AlertCircle className="w-4 h-4 text-rose-500 shrink-0 mt-0.5" />
              <div>
                <span className="font-bold">Upload Error: </span>
                {errorMessage}
              </div>
            </div>
          )}

          {/* Document 1: Claim Form */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-800 flex items-center justify-between">
              <span>1. Claim Form PDF <span className="text-rose-500">*</span></span>
              <span className="text-[11px] font-normal text-slate-500">Form 2A / Policyholder Claim Form</span>
            </label>
            <div
              className={`border-2 border-dashed rounded-2xl p-4 transition-all text-center cursor-pointer ${
                claimFormFile
                  ? 'border-emerald-500 bg-emerald-50/40'
                  : 'border-slate-300 hover:border-sky-500 hover:bg-sky-50/30'
              }`}
            >
              <input
                type="file"
                accept=".pdf,application/pdf"
                id="claimFormInput"
                className="hidden"
                onChange={(e) => {
                  if (e.target.files && e.target.files[0]) {
                    setClaimFormFile(e.target.files[0]);
                  }
                }}
              />
              <label htmlFor="claimFormInput" className="cursor-pointer block">
                {claimFormFile ? (
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="p-2 bg-emerald-100 text-emerald-700 rounded-lg">
                        <CheckCircle2 className="w-5 h-5" />
                      </div>
                      <div className="text-left">
                        <p className="text-xs font-bold text-slate-900 truncate max-w-xs">{claimFormFile.name}</p>
                        <p className="text-[11px] text-slate-500">{(claimFormFile.size / 1024).toFixed(1)} KB</p>
                      </div>
                    </div>
                    <span className="text-xs font-semibold text-sky-600 hover:underline">Change File</span>
                  </div>
                ) : (
                  <div className="py-2">
                    <UploadCloud className="w-8 h-8 text-sky-500 mx-auto mb-1" />
                    <p className="text-xs font-bold text-slate-700">Click to attach Claim Form PDF</p>
                    <p className="text-[11px] text-slate-400 mt-0.5">Accepts PDF files up to 25MB</p>
                  </div>
                )}
              </label>
            </div>
          </div>

          {/* Document 2: Combined Document */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-800 flex items-center justify-between">
              <span>2. Combined Hospital Document PDF <span className="text-rose-500">*</span></span>
              <span className="text-[11px] font-normal text-slate-500">Discharge Summary + Final Hospital Bill</span>
            </label>
            <div
              className={`border-2 border-dashed rounded-2xl p-4 transition-all text-center cursor-pointer ${
                combinedDocFile
                  ? 'border-emerald-500 bg-emerald-50/40'
                  : 'border-slate-300 hover:border-sky-500 hover:bg-sky-50/30'
              }`}
            >
              <input
                type="file"
                accept=".pdf,application/pdf"
                id="combinedDocInput"
                className="hidden"
                onChange={(e) => {
                  if (e.target.files && e.target.files[0]) {
                    setCombinedDocFile(e.target.files[0]);
                  }
                }}
              />
              <label htmlFor="combinedDocInput" className="cursor-pointer block">
                {combinedDocFile ? (
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="p-2 bg-emerald-100 text-emerald-700 rounded-lg">
                        <CheckCircle2 className="w-5 h-5" />
                      </div>
                      <div className="text-left">
                        <p className="text-xs font-bold text-slate-900 truncate max-w-xs">{combinedDocFile.name}</p>
                        <p className="text-[11px] text-slate-500">{(combinedDocFile.size / 1024).toFixed(1)} KB</p>
                      </div>
                    </div>
                    <span className="text-xs font-semibold text-sky-600 hover:underline">Change File</span>
                  </div>
                ) : (
                  <div className="py-2">
                    <UploadCloud className="w-8 h-8 text-sky-500 mx-auto mb-1" />
                    <p className="text-xs font-bold text-slate-700">Click to attach Combined Hospital PDF</p>
                    <p className="text-[11px] text-slate-400 mt-0.5">Accepts PDF files up to 25MB</p>
                  </div>
                )}
              </label>
            </div>
          </div>

          {/* Form Actions */}
          <div className="pt-3 border-t border-slate-100 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || (!claimFormFile && !combinedDocFile)}
              className="px-5 py-2.5 bg-gradient-to-r from-sky-600 to-blue-600 hover:from-sky-700 hover:to-blue-700 disabled:opacity-50 text-white rounded-xl text-xs font-bold shadow-lg shadow-sky-500/25 flex items-center gap-2 transition-all"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Running Extraction & Rule Engine...
                </>
              ) : (
                <>
                  <FileText className="w-4 h-4" />
                  Ingest & Process Claim
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
