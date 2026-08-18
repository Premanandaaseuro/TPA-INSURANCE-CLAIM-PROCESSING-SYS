import { PDFDocument, StandardFonts, rgb } from 'pdf-lib';
import fs from 'fs';
import path from 'path';

const FIXTURE_DIR = path.join(process.cwd(), 'TPA_Playwright_E2E', 'fixtures', 'generated_pdfs');

if (!fs.existsSync(FIXTURE_DIR)) {
  fs.mkdirSync(FIXTURE_DIR, { recursive: true });
}

export async function generatePdf(filename, textLines) {
  const filePath = path.join(FIXTURE_DIR, filename);
  const pdfDoc = await PDFDocument.create();
  const page = pdfDoc.addPage([600, 800]);
  const font = await pdfDoc.embedFont(StandardFonts.Helvetica);
  const boldFont = await pdfDoc.embedFont(StandardFonts.HelveticaBold);

  let y = 750;
  for (const line of textLines) {
    const isHeader = line.startsWith('#');
    const cleanText = line.replace(/^#\s*/, '');
    page.drawText(cleanText, {
      x: 50,
      y: y,
      size: isHeader ? 16 : 12,
      font: isHeader ? boldFont : font,
      color: isHeader ? rgb(0, 0.2, 0.6) : rgb(0.1, 0.1, 0.1),
    });
    y -= isHeader ? 25 : 18;
  }

  const pdfBytes = await pdfDoc.save();
  fs.writeFileSync(filePath, pdfBytes);
  return filePath;
}

export async function initSamplePdfs() {
  const validClaimForm = await generatePdf('valid_claim_form_pol_10001.pdf', [
    '# TPA HEALTH INSURANCE CLAIM FORM',
    'Claim ID: CLM-TEST-1001',
    'Patient Name: Rahul Kumar',
    'Policy Number: POL-10001',
    'Hospital Name: Apollo Hospital',
    'Admission Date: 2026-05-10',
    'Discharge Date: 2026-05-15',
    'Claimed Amount: 45000',
    'Diagnosis: Acute Appendicitis',
  ]);

  const validHospitalDoc = await generatePdf('valid_combined_hospital_document.pdf', [
    '# APOLLO HOSPITAL - DISCHARGE SUMMARY & FINAL BILL',
    'Patient Name: Rahul Kumar',
    'Policy Number: POL-10001',
    'Hospital Name: Apollo Hospital',
    'Date of Admission: 2026-05-10',
    'Date of Discharge: 2026-05-15',
    'Total Billed Amount: 45000',
    'Hospital Bill Breakdown: ICU Charges - 20000, Surgery - 25000',
  ]);

  const inactivePolicyClaimForm = await generatePdf('inactive_policy_pol_10002.pdf', [
    '# TPA HEALTH INSURANCE CLAIM FORM',
    'Patient Name: Rahul Kumar',
    'Policy Number: POL-10002',
    'Hospital Name: Apollo Hospital',
    'Admission Date: 2026-05-10',
    'Discharge Date: 2026-05-15',
    'Claimed Amount: 45000',
  ]);

  const mismatchedPatientClaimForm = await generatePdf('mismatched_patient_name.pdf', [
    '# TPA HEALTH INSURANCE CLAIM FORM',
    'Patient Name: Vikram Singh',
    'Policy Number: POL-10001',
    'Hospital Name: Apollo Hospital',
    'Admission Date: 2026-05-10',
    'Discharge Date: 2026-05-15',
    'Claimed Amount: 45000',
  ]);

  const mismatchedHospitalClaimForm = await generatePdf('mismatched_hospital_claim_form.pdf', [
    '# TPA HEALTH INSURANCE CLAIM FORM',
    'Patient Name: Rahul Kumar',
    'Policy Number: POL-10001',
    'Hospital Name: Fortis Healthcare',
    'Admission Date: 2026-05-10',
    'Discharge Date: 2026-05-15',
    'Claimed Amount: 45000',
  ]);

  const mismatchedHospitalDoc = await generatePdf('mismatched_hospital_doc.pdf', [
    '# FORTIS HEALTHCARE - DISCHARGE SUMMARY',
    'Patient Name: Rahul Kumar',
    'Policy Number: POL-10001',
    'Hospital Name: Fortis Healthcare',
    'Date of Admission: 2026-05-10',
    'Date of Discharge: 2026-05-15',
    'Total Billed Amount: 45000',
  ]);

  const invalidDatesClaimForm = await generatePdf('invalid_dates_claim_form.pdf', [
    '# TPA HEALTH INSURANCE CLAIM FORM',
    'Patient Name: Rahul Kumar',
    'Policy Number: POL-10001',
    'Hospital Name: Apollo Hospital',
    'Admission Date: 2026-05-20',
    'Discharge Date: 2026-05-10',
    'Claimed Amount: 45000',
  ]);

  const invalidDatesHospitalDoc = await generatePdf('invalid_dates_hospital_doc.pdf', [
    '# APOLLO HOSPITAL - DISCHARGE SUMMARY & FINAL BILL',
    'Patient Name: Rahul Kumar',
    'Policy Number: POL-10001',
    'Hospital Name: Apollo Hospital',
    'Date of Admission: 2026-05-20',
    'Date of Discharge: 2026-05-10',
    'Total Billed Amount: 45000',
  ]);

  const highValueClaimForm = await generatePdf('high_value_claim_form.pdf', [
    '# TPA HEALTH INSURANCE CLAIM FORM',
    'Patient Name: Rahul Kumar',
    'Policy Number: POL-10001',
    'Hospital Name: Apollo Hospital',
    'Admission Date: 2026-05-10',
    'Discharge Date: 2026-05-15',
    'Claimed Amount: 150000',
  ]);

  const amountExceedsBillClaimForm = await generatePdf('amount_exceeds_bill.pdf', [
    '# TPA HEALTH INSURANCE CLAIM FORM',
    'Patient Name: Rahul Kumar',
    'Policy Number: POL-10001',
    'Hospital Name: Apollo Hospital',
    'Admission Date: 2026-05-10',
    'Discharge Date: 2026-05-15',
    'Claimed Amount: 95000',
  ]);

  const amountExceedsBillHospitalDoc = await generatePdf('amount_exceeds_bill_hospital_doc.pdf', [
    '# APOLLO HOSPITAL - DISCHARGE SUMMARY & FINAL BILL',
    'Patient Name: Rahul Kumar',
    'Policy Number: POL-10001',
    'Hospital Name: Apollo Hospital',
    'Date of Admission: 2026-05-10',
    'Date of Discharge: 2026-05-15',
    'Total Billed Amount: 40000',
  ]);

  const invalidTextPath = path.join(FIXTURE_DIR, 'invalid_text_file.txt');
  fs.writeFileSync(invalidTextPath, 'This is a plain text file, not a PDF document.');

  const corruptedPdfPath = path.join(FIXTURE_DIR, 'corrupted_pdf_file.pdf');
  fs.writeFileSync(corruptedPdfPath, 'corrupted pdf data stream bytes');

  return {
    validClaimForm,
    validHospitalDoc,
    inactivePolicyClaimForm,
    mismatchedPatientClaimForm,
    mismatchedHospitalClaimForm,
    mismatchedHospitalDoc,
    invalidDatesClaimForm,
    invalidDatesHospitalDoc,
    highValueClaimForm,
    amountExceedsBillClaimForm,
    amountExceedsBillHospitalDoc,
    invalidTextFile: invalidTextPath,
    corruptedPdf: corruptedPdfPath,
    readPdfBuffer: async (filePath) => fs.readFileSync(filePath),
  };
}

let cachedPdfs = null;
export const samplePdfs = new Proxy({}, {
  get: (target, prop) => {
    if (prop === 'then') return undefined;
    if (!cachedPdfs) {
      cachedPdfs = initSamplePdfs();
    }
    if (prop === 'readPdfBuffer') {
      return async (filePath) => fs.readFileSync(filePath);
    }
    return cachedPdfs.then(pdfs => pdfs[prop]);
  }
});
