const fs = require('fs');
const path = require('path');
const { PDFDocument, StandardFonts, rgb } = require('pdf-lib');

/**
 * PDF Test Data Generator for Playwright E2E Test Suite
 * Generates realistic PDFs using pdf-lib for R01 to R10 rules & Golden Claim
 */

async function createPdf(lines) {
  const pdfDoc = await PDFDocument.create();
  const page = pdfDoc.addPage([600, 800]);
  const font = await pdfDoc.embedFont(StandardFonts.Helvetica);
  const fontBold = await pdfDoc.embedFont(StandardFonts.HelveticaBold);

  let y = 750;
  for (const line of lines) {
    if (line.isBold) {
      page.drawText(line.text, { x: 50, y, size: line.size || 14, font: fontBold, color: rgb(0, 0.2, 0.4) });
    } else {
      page.drawText(line.text, { x: 50, y, size: line.size || 11, font, color: rgb(0.1, 0.1, 0.1) });
    }
    y -= line.spacing || 20;
  }

  const pdfBytes = await pdfDoc.save();
  return Buffer.from(pdfBytes);
}

function ensureDir(dirPath) {
  if (!fs.existsSync(dirPath)) {
    fs.mkdirSync(dirPath, { recursive: true });
  }
}

async function generateAllPdfs() {
  const baseDir = path.join(__dirname, 'test-data');

  // 1. Golden Valid Claim (PASS R01-R10 -> APPROVED)
  const goldenDir = path.join(baseDir, 'golden_valid_claim');
  ensureDir(goldenDir);
  fs.writeFileSync(path.join(goldenDir, 'ClaimForm_Golden.pdf'), await createPdf([
    { text: 'HEALTH INSURANCE CLAIM FORM', isBold: true, size: 16, spacing: 30 },
    { text: 'Policy Number: POL-10001' },
    { text: 'Policy ID: PID-10001' },
    { text: 'Customer Name: Rahul Kumar' },
    { text: 'Patient Name: Rahul Kumar' },
    { text: 'Hospital Name: Aseuro Care Hospital' },
    { text: 'Admission Date: 2026-11-20' },
    { text: 'Discharge Date: 2026-11-24' },
    { text: 'Claimed Amount: 35000' },
    { text: 'Claim Type: REIMBURSEMENT' }
  ]));
  fs.writeFileSync(path.join(goldenDir, 'CombinedHospitalDoc_Golden.pdf'), await createPdf([
    { text: 'DISCHARGE SUMMARY', isBold: true, size: 16, spacing: 25 },
    { text: 'Hospital Name: Aseuro Care Hospital' },
    { text: 'Patient Name: Rahul Kumar' },
    { text: 'Admission Date: 2026-11-20' },
    { text: 'Discharge Date: 2026-11-24' },
    { text: 'Diagnosis: Acute Gastroenteritis' },
    { text: 'Treating Doctor: Dr. S. Mehta', spacing: 40 },
    { text: 'FINAL HOSPITAL BILL', isBold: true, size: 16, spacing: 25 },
    { text: 'Bill Number: HB-2026-1001' },
    { text: 'Bill Date: 2026-11-24' },
    { text: 'Hospital Name: Aseuro Care Hospital' },
    { text: 'Patient Name: Rahul Kumar' },
    { text: 'Room Rent Charges: 15000' },
    { text: 'Pharmacy Charges: 10000' },
    { text: 'Total Bill Amount: 35000' }
  ]));

  // 2. 01_approved
  const approvedDir = path.join(baseDir, '01_approved');
  ensureDir(approvedDir);
  fs.copyFileSync(path.join(goldenDir, 'ClaimForm_Golden.pdf'), path.join(approvedDir, 'ClaimForm_Approved.pdf'));
  fs.copyFileSync(path.join(goldenDir, 'CombinedHospitalDoc_Golden.pdf'), path.join(approvedDir, 'CombinedHospitalDoc_Approved.pdf'));

  // 3. 02_r02_missing_combined
  const r02Dir = path.join(baseDir, '02_r02_missing_combined');
  ensureDir(r02Dir);
  fs.copyFileSync(path.join(goldenDir, 'ClaimForm_Golden.pdf'), path.join(r02Dir, 'ClaimForm_Only.pdf'));

  // 4. 03_r03_inactive_policy (POL-10002 is INACTIVE in DB)
  const r03Dir = path.join(baseDir, '03_r03_inactive_policy');
  ensureDir(r03Dir);
  fs.writeFileSync(path.join(r03Dir, 'ClaimForm_InactivePolicy.pdf'), await createPdf([
    { text: 'HEALTH INSURANCE CLAIM FORM', isBold: true, size: 16, spacing: 30 },
    { text: 'Policy Number: POL-10002' },
    { text: 'Policy ID: PID-10002' },
    { text: 'Customer Name: Priya Sharma' },
    { text: 'Patient Name: Priya Sharma' },
    { text: 'Hospital Name: City Hospital' },
    { text: 'Admission Date: 2026-05-10' },
    { text: 'Discharge Date: 2026-05-14' },
    { text: 'Claimed Amount: 20000' },
    { text: 'Claim Type: REIMBURSEMENT' }
  ]));
  fs.writeFileSync(path.join(r03Dir, 'CombinedHospitalDoc_InactivePolicy.pdf'), await createPdf([
    { text: 'DISCHARGE SUMMARY', isBold: true, size: 16, spacing: 25 },
    { text: 'Hospital Name: City Hospital' },
    { text: 'Patient Name: Priya Sharma' },
    { text: 'Admission Date: 2026-05-10' },
    { text: 'Discharge Date: 2026-05-14' },
    { text: 'Diagnosis: Viral Fever', spacing: 40 },
    { text: 'FINAL HOSPITAL BILL', isBold: true, size: 16, spacing: 25 },
    { text: 'Bill Number: HB-2026-3001' },
    { text: 'Bill Date: 2026-05-14' },
    { text: 'Hospital Name: City Hospital' },
    { text: 'Patient Name: Priya Sharma' },
    { text: 'Total Bill Amount: 20000' }
  ]));

  // 5. 04_r04_missing_policy_number (Policy ID only: PID-10008, NO Policy Number: POL-10008)
  const r04Dir = path.join(baseDir, '04_r04_missing_policy_number');
  ensureDir(r04Dir);
  fs.writeFileSync(path.join(r04Dir, 'ClaimForm_MissingPolicyNumber.pdf'), await createPdf([
    { text: 'HEALTH INSURANCE CLAIM FORM', isBold: true, size: 16, spacing: 30 },
    { text: 'Policy ID: PID-10008' },
    { text: 'Customer Name: Vikram Reddy' },
    { text: 'Patient Name: Vikram Reddy' },
    { text: 'Hospital Name: Fortis Hospital' },
    { text: 'Admission Date: 2026-06-01' },
    { text: 'Discharge Date: 2026-06-05' },
    { text: 'Claimed Amount: 30000' },
    { text: 'Claim Type: REIMBURSEMENT' }
  ]));
  fs.writeFileSync(path.join(r04Dir, 'CombinedHospitalDoc_MissingPolicyNumber.pdf'), await createPdf([
    { text: 'DISCHARGE SUMMARY', isBold: true, size: 16, spacing: 25 },
    { text: 'Hospital Name: Fortis Hospital' },
    { text: 'Patient Name: Vikram Reddy' },
    { text: 'Admission Date: 2026-06-01' },
    { text: 'Discharge Date: 2026-06-05' },
    { text: 'Diagnosis: Dengue Fever', spacing: 40 },
    { text: 'FINAL HOSPITAL BILL', isBold: true, size: 16, spacing: 25 },
    { text: 'Bill Number: HB-2026-4001' },
    { text: 'Bill Date: 2026-06-05' },
    { text: 'Hospital Name: Fortis Hospital' },
    { text: 'Patient Name: Vikram Reddy' },
    { text: 'Total Bill Amount: 30000' }
  ]));

  // 6. 05_r05_patient_mismatch
  const r05Dir = path.join(baseDir, '05_r05_patient_mismatch');
  ensureDir(r05Dir);
  fs.writeFileSync(path.join(r05Dir, 'ClaimForm_PatientMismatch.pdf'), await createPdf([
    { text: 'HEALTH INSURANCE CLAIM FORM', isBold: true, size: 16, spacing: 30 },
    { text: 'Policy Number: POL-10004' },
    { text: 'Customer Name: Priya Sharma' },
    { text: 'Patient Name: Priya Sharma' },
    { text: 'Hospital Name: Max Healthcare' },
    { text: 'Admission Date: 2026-05-15' },
    { text: 'Discharge Date: 2026-05-18' },
    { text: 'Claimed Amount: 25000' }
  ]));
  fs.writeFileSync(path.join(r05Dir, 'CombinedHospitalDoc_PatientMismatch.pdf'), await createPdf([
    { text: 'DISCHARGE SUMMARY', isBold: true, size: 16, spacing: 25 },
    { text: 'Hospital Name: Max Healthcare' },
    { text: 'Patient Name: Sunita Sharma' }, // Mismatch!
    { text: 'Admission Date: 2026-05-15' },
    { text: 'Discharge Date: 2026-05-18' },
    { text: 'Diagnosis: Bronchitis', spacing: 40 },
    { text: 'FINAL HOSPITAL BILL', isBold: true, size: 16, spacing: 25 },
    { text: 'Bill Number: HB-2026-5001' },
    { text: 'Hospital Name: Max Healthcare' },
    { text: 'Patient Name: Sunita Sharma' },
    { text: 'Total Bill Amount: 25000' }
  ]));

  // 7. 06_r06_hospital_mismatch
  const r06Dir = path.join(baseDir, '06_r06_hospital_mismatch');
  ensureDir(r06Dir);
  fs.writeFileSync(path.join(r06Dir, 'ClaimForm_HospitalMismatch.pdf'), await createPdf([
    { text: 'HEALTH INSURANCE CLAIM FORM', isBold: true, size: 16, spacing: 30 },
    { text: 'Policy Number: POL-10005' },
    { text: 'Customer Name: Amit Patel' },
    { text: 'Patient Name: Amit Patel' },
    { text: 'Hospital Name: Manipal Hospital Bengaluru' },
    { text: 'Admission Date: 2026-06-10' },
    { text: 'Discharge Date: 2026-06-14' },
    { text: 'Claimed Amount: 40000' }
  ]));
  fs.writeFileSync(path.join(r06Dir, 'CombinedHospitalDoc_HospitalMismatch.pdf'), await createPdf([
    { text: 'DISCHARGE SUMMARY', isBold: true, size: 16, spacing: 25 },
    { text: 'Hospital Name: Apollo Hospital Bengaluru' }, // Mismatch!
    { text: 'Patient Name: Amit Patel' },
    { text: 'Admission Date: 2026-06-10' },
    { text: 'Discharge Date: 2026-06-14' },
    { text: 'Diagnosis: Typhoid', spacing: 40 },
    { text: 'FINAL HOSPITAL BILL', isBold: true, size: 16, spacing: 25 },
    { text: 'Bill Number: HB-2026-6001' },
    { text: 'Hospital Name: Apollo Hospital Bengaluru' },
    { text: 'Patient Name: Amit Patel' },
    { text: 'Total Bill Amount: 40000' }
  ]));

  // 8. 07_r07_date_mismatch
  const r07Dir = path.join(baseDir, '07_r07_date_mismatch');
  ensureDir(r07Dir);
  fs.writeFileSync(path.join(r07Dir, 'ClaimForm_DateMismatch.pdf'), await createPdf([
    { text: 'HEALTH INSURANCE CLAIM FORM', isBold: true, size: 16, spacing: 30 },
    { text: 'Policy Number: POL-10006' },
    { text: 'Customer Name: Suresh Menon' },
    { text: 'Patient Name: Suresh Menon' },
    { text: 'Hospital Name: Columbia Asia Hospital' },
    { text: 'Admission Date: 2026-07-01' },
    { text: 'Discharge Date: 2026-07-05' },
    { text: 'Claimed Amount: 30000' }
  ]));
  fs.writeFileSync(path.join(r07Dir, 'CombinedHospitalDoc_DateMismatch.pdf'), await createPdf([
    { text: 'DISCHARGE SUMMARY', isBold: true, size: 16, spacing: 25 },
    { text: 'Hospital Name: Columbia Asia Hospital' },
    { text: 'Patient Name: Suresh Menon' },
    { text: 'Admission Date: 2026-07-02' }, // Mismatch!
    { text: 'Discharge Date: 2026-07-05' },
    { text: 'Diagnosis: Pneumonia', spacing: 40 },
    { text: 'FINAL HOSPITAL BILL', isBold: true, size: 16, spacing: 25 },
    { text: 'Bill Number: HB-2026-7001' },
    { text: 'Hospital Name: Columbia Asia Hospital' },
    { text: 'Patient Name: Suresh Menon' },
    { text: 'Total Bill Amount: 30000' }
  ]));

  // 9. 08_r08_claim_greater_bill (Claimed 45000 > Bill 30000)
  const r08Dir = path.join(baseDir, '08_r08_claim_greater_bill');
  ensureDir(r08Dir);
  fs.writeFileSync(path.join(r08Dir, 'ClaimForm_ClaimGreaterBill.pdf'), await createPdf([
    { text: 'HEALTH INSURANCE CLAIM FORM', isBold: true, size: 16, spacing: 30 },
    { text: 'Policy Number: POL-10007' },
    { text: 'Customer Name: Kavita Singh' },
    { text: 'Patient Name: Kavita Singh' },
    { text: 'Hospital Name: Narayana Health' },
    { text: 'Admission Date: 2026-08-01' },
    { text: 'Discharge Date: 2026-08-04' },
    { text: 'Claimed Amount: 45000' } // Claimed 45000
  ]));
  fs.writeFileSync(path.join(r08Dir, 'CombinedHospitalDoc_ClaimGreaterBill.pdf'), await createPdf([
    { text: 'DISCHARGE SUMMARY', isBold: true, size: 16, spacing: 25 },
    { text: 'Hospital Name: Narayana Health' },
    { text: 'Patient Name: Kavita Singh' },
    { text: 'Admission Date: 2026-08-01' },
    { text: 'Discharge Date: 2026-08-04' },
    { text: 'Diagnosis: Malaria', spacing: 40 },
    { text: 'FINAL HOSPITAL BILL', isBold: true, size: 16, spacing: 25 },
    { text: 'Bill Number: HB-2026-8001' },
    { text: 'Hospital Name: Narayana Health' },
    { text: 'Patient Name: Kavita Singh' },
    { text: 'Total Bill Amount: 30000' } // Bill 30000
  ]));

  // 10. 09_r09_high_claim (Claim 50001 > 50000)
  const r09Dir = path.join(baseDir, '09_r09_high_claim');
  ensureDir(r09Dir);
  fs.writeFileSync(path.join(r09Dir, 'ClaimForm_HighClaim.pdf'), await createPdf([
    { text: 'HEALTH INSURANCE CLAIM FORM', isBold: true, size: 16, spacing: 30 },
    { text: 'Policy Number: POL-10009' },
    { text: 'Customer Name: Ananya Roy' },
    { text: 'Patient Name: Ananya Roy' },
    { text: 'Hospital Name: Aster CMI Hospital' },
    { text: 'Admission Date: 2026-09-01' },
    { text: 'Discharge Date: 2026-09-05' },
    { text: 'Claimed Amount: 50001' } // > 50000 boundary failure
  ]));
  fs.writeFileSync(path.join(r09Dir, 'CombinedHospitalDoc_HighClaim.pdf'), await createPdf([
    { text: 'DISCHARGE SUMMARY', isBold: true, size: 16, spacing: 25 },
    { text: 'Hospital Name: Aster CMI Hospital' },
    { text: 'Patient Name: Ananya Roy' },
    { text: 'Admission Date: 2026-09-01' },
    { text: 'Discharge Date: 2026-09-05' },
    { text: 'Diagnosis: Cholecystectomy', spacing: 40 },
    { text: 'FINAL HOSPITAL BILL', isBold: true, size: 16, spacing: 25 },
    { text: 'Bill Number: HB-2026-9001' },
    { text: 'Hospital Name: Aster CMI Hospital' },
    { text: 'Patient Name: Ananya Roy' },
    { text: 'Total Bill Amount: 50001' }
  ]));

  // 11. 10_r10_duplicate
  const r10Dir = path.join(baseDir, '10_r10_duplicate');
  ensureDir(r10Dir);
  fs.writeFileSync(path.join(r10Dir, 'ClaimForm_Duplicate.pdf'), await createPdf([
    { text: 'HEALTH INSURANCE CLAIM FORM', isBold: true, size: 16, spacing: 30 },
    { text: 'Policy Number: POL-10001' },
    { text: 'Customer Name: Rahul Kumar' },
    { text: 'Patient Name: Rahul Kumar' },
    { text: 'Hospital Name: Aseuro Care Hospital' },
    { text: 'Admission Date: 2026-11-20' },
    { text: 'Discharge Date: 2026-11-24' },
    { text: 'Claimed Amount: 35000' }
  ]));
  fs.writeFileSync(path.join(r10Dir, 'CombinedHospitalDoc_Duplicate.pdf'), await createPdf([
    { text: 'DISCHARGE SUMMARY', isBold: true, size: 16, spacing: 25 },
    { text: 'Hospital Name: Aseuro Care Hospital' },
    { text: 'Patient Name: Rahul Kumar' },
    { text: 'Admission Date: 2026-11-20' },
    { text: 'Discharge Date: 2026-11-24' },
    { text: 'Diagnosis: Acute Gastroenteritis', spacing: 40 },
    { text: 'FINAL HOSPITAL BILL', isBold: true, size: 16, spacing: 25 },
    { text: 'Bill Number: HB-2026-1001-DUP' },
    { text: 'Hospital Name: Aseuro Care Hospital' },
    { text: 'Patient Name: Rahul Kumar' },
    { text: 'Total Bill Amount: 35000' }
  ]));

  console.log('✅ All PDF Test Datasets successfully generated in:', baseDir);
}

generateAllPdfs().catch(err => {
  console.error('Error generating PDF test fixtures:', err);
  process.exit(1);
});
