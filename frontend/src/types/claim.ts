export type ClaimStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'NEEDS_MANUAL_REVIEW';
export type DocumentType = 'CLAIM_FORM' | 'COMBINED_HOSPITAL_DOCUMENT';
export type RuleSeverity = 'REJECTED' | 'NEEDS_MANUAL_REVIEW';

export interface ClaimDocumentDto {
  id: number;
  documentType: DocumentType;
  originalFilename: string;
  storedFilename: string;
  filePath: string;
  contentType?: string;
  fileSize?: number;
  checksumSha256?: string;
  uploadedAt: string;
}

export interface ClaimRuleResultDto {
  id: number;
  ruleCode: string;
  ruleName: string;
  passed: boolean;
  severity?: RuleSeverity;
  details?: string;
  evaluatedAt: string;
}

export interface ClaimResponseDto {
  id: number;
  claimId: string;
  claimNumber?: string;
  policyNumber?: string;
  policyId?: string;
  customerName?: string;
  carrierName?: string;
  policyName?: string;
  patientName?: string;
  hospitalName?: string;
  admissionDate?: string;
  dischargeDate?: string;
  claimedAmount?: number;
  claimType?: string;
  status: ClaimStatus;
  decisionReason?: string;
  createdAt: string;
  processedAt?: string;
  documents: ClaimDocumentDto[];
  ruleResults?: ClaimRuleResultDto[];
}
