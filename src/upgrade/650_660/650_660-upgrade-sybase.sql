-- New columns in CertificateData are added by the JPA provider if there are sufficient privileges
-- if not added automatically the following SQL statements can be run to add the new columns 
-- ALTER TABLE CertificateData ADD notBefore DECIMAL(20,0);
-- ALTER TABLE CertificateData ADD endEntityProfileId INTEGER;
-- ALTER TABLE CertificateData ADD subjectAltName VARCHAR(2000);
--
-- Table ProfileData is new and is added by the JPA provider if there are sufficient privileges. 
-- See create-tables-database.sql
--
-- subjectDN and subjectAltName columns in UserData has been extended to accommodate longer names
-- subjectDN from 250 to 400 characters and subjectAltName from 250 to 2000 characters
-- ALTER TABLE UserData MODIFY subjectAltName VARCHAR(2000);
-- ALTER TABLE UserData MODIFY subjectDN VARCHAR(400);
-- ALTER TABLE CertificateData MODIFY subjectDN VARCHAR(400);
