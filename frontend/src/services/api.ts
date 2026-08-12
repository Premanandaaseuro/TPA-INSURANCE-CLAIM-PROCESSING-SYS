import { ClaimResponseDto } from '../types/claim';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:7002/api/claims';

export async function fetchClaims(): Promise<ClaimResponseDto[]> {
  const response = await fetch(API_BASE_URL);
  if (!response.ok) {
    throw new Error(`Failed to fetch claims list (HTTP ${response.status})`);
  }
  return response.json();
}

export async function fetchClaimByClaimId(claimId: string): Promise<ClaimResponseDto> {
  const response = await fetch(`${API_BASE_URL}/${claimId}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch claim details for ${claimId} (HTTP ${response.status})`);
  }
  return response.json();
}

export async function submitClaim(claimForm: File, combinedHospitalDocument: File): Promise<ClaimResponseDto> {
  const formData = new FormData();
  formData.append('claimForm', claimForm);
  formData.append('combinedHospitalDocument', combinedHospitalDocument);

  const response = await fetch(API_BASE_URL, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    let errorMsg = `Upload failed (HTTP ${response.status})`;
    try {
      const errJson = await response.json();
      if (errJson.message) errorMsg = errJson.message;
    } catch (_) {}
    throw new Error(errorMsg);
  }

  return response.json();
}

export function getExportPdfUrl(claimId: string): string {
  return `${API_BASE_URL}/${claimId}/pdf`;
}

export async function clearAllClaimData(): Promise<{ message: string }> {
  const response = await fetch(`${API_BASE_URL}/clear-test-data`, {
    method: 'POST',
  });
  if (!response.ok) {
    throw new Error(`Failed to clear claim test data (HTTP ${response.status})`);
  }
  return response.json();
}
