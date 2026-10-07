-- Manual, re-runnable seed/update script for tbl_legal_content.
--
-- Unlike V3__create_legal_content_table.sql (Flyway — runs once, ever, and
-- must never be edited after it has applied), this script is meant to be
-- run by hand against your local DB whenever you edit the source content in
--   pharma-aggregator-client/docs/buyer_terms_and_condition.md
--   pharma-aggregator-client/docs/seller_terms_and_condition.md
-- and want those changes reflected in tbl_legal_content without waiting for
-- a new Flyway migration. ON CONFLICT ... DO UPDATE means it's safe to run
-- as many times as you like; version is bumped only when content actually
-- changed.
--
-- Run against your local DB, e.g.:
--   psql -U postgres -d pharma_aggregator_test_local -f legal_content_seed.sql

INSERT INTO tbl_legal_content (content_key, title, content, version, is_active, created_by, updated_by)
VALUES (
    'BUYER_TERMS',
    'Buyer Company Pre-Registration Mandatory Declarations & Acceptance',
    $BUYER$
<h2>Authorized Representation & Legal Binding</h2>
<ul>
<li>As a condition precedent to Buyer Company registration on the TiaMeds Marketplace platform, the following mandatory declarations and acceptances shall be reviewed and accepted either directly by the Buyer Company or by an individual duly authorized to act on behalf of the Buyer Company.</li>
<li>Any acceptance provided through the TiaMeds Marketplace platform shall be deemed to constitute a legally binding acceptance by the Buyer Company.</li>
<li>The Buyer Company confirms that the individual completing the registration process is duly authorized to submit information, provide declarations, place or authorize procurement transactions, and accept obligations on behalf of the Buyer Company.</li>
<li>The Buyer Company shall remain responsible for activities and transactions carried out through its authorized account and authorized users.</li>
<li>Registration of the Buyer Company on the TiaMeds Marketplace platform shall not proceed unless the final mandatory acceptance checkbox is acknowledged.</li>
</ul>

<h2>Buyer Eligibility & Valid License Confirmation</h2>
<ul>
<li>The Buyer Company confirms that it is legally established and authorized to procure pharmaceutical products and/or other regulated healthcare products applicable to its business category.</li>
<li>The Buyer Company confirms that it holds all valid, active, and applicable licences, registrations, approvals, and authorizations required under applicable laws for the procurement, purchase, stocking, storage, distribution, dispensing, institutional use, retail, wholesale, or other permitted handling of pharmaceutical products, as applicable to its business activities.</li>
<li>The Buyer Company confirms that pharmaceutical products shall be purchased through the TiaMeds Marketplace platform only where such procurement falls within the scope of its applicable licences, registrations, approvals, and legal authorizations.</li>
<li>The Buyer Company shall ensure that all licences, registrations, approvals, and authorizations required for its activities on the TiaMeds Marketplace platform remain valid, active, and applicable throughout its participation on the platform.</li>
<li>The Buyer Company shall promptly notify TiaMeds Marketplace of any suspension, cancellation, surrender, restriction, material modification, or regulatory action affecting its licences, registrations, approvals, authorizations, or its legal eligibility to procure, possess, stock, distribute, dispense, use, or otherwise handle pharmaceutical products, as applicable to its business activities.</li>
<li>The Buyer Company confirms that, while continuing to transact through the TiaMeds Marketplace platform, there has been no undisclosed material change in the status of its licences, registrations, approvals, authorizations, or regulatory eligibility that would make it legally ineligible to undertake the applicable transaction.</li>
<li>TiaMeds Marketplace shall monitor the expiry dates of licences, registrations, approvals, and authorizations based on the validity information captured and verified during the Buyer Company compliance process and may require renewal documentation or re-verification before or upon expiry.</li>
<li>TiaMeds Marketplace may periodically verify or re-verify the validity, authenticity, status, and applicability of the Buyer Company's licences, registrations, approvals, authorizations, and other compliance documentation.</li>
<li>The Buyer Company shall provide updated documents, information, clarifications, or supporting evidence whenever reasonably required by TiaMeds Marketplace for compliance verification or re-verification.</li>
<li>Failure by the Buyer Company to disclose any material change affecting its regulatory eligibility, or submission or continued use of invalid, suspended, cancelled, restricted, misleading, or otherwise non-compliant documentation, may result in transaction restriction, compliance review, suspension of platform access, regulatory reporting where legally required, or termination of the Buyer Company's account, subject to applicable law and TiaMeds Marketplace policies.</li>
</ul>

<h2>Regulatory Compliance Commitment</h2>
<p>The Buyer Company agrees to conduct all procurement and related activities through the TiaMeds Marketplace platform in accordance with applicable laws, regulations, standards, licence conditions, and regulatory requirements, including, where applicable:</p>
<ul>
<li>Drugs and Cosmetics Act, 1940</li>
<li>Drugs and Cosmetics Rules, 1945</li>
<li>Medical Devices Rules, 2017</li>
<li>Applicable CDSCO guidelines</li>
<li>Applicable State FDA regulations</li>
<li>Applicable taxation and trade laws</li>
<li>Any other applicable Central or State laws and regulatory requirements</li>
</ul>
<p>The Buyer Company accepts responsibility for ensuring ongoing compliance applicable to its procurement, receipt, possession, stocking, storage, distribution, dispensing, resale, institutional use, or other handling of products purchased through the TiaMeds Marketplace platform.</p>

<h2>Lawful Procurement & Authorized Use</h2>
<ul>
<li>The Buyer Company confirms that all purchases made through the TiaMeds Marketplace platform shall be for legitimate, lawful, and authorized business, healthcare, institutional, distribution, dispensing, or other legally permitted purposes.</li>
<li>The Buyer Company shall purchase only those pharmaceutical products that it is legally entitled to procure, possess, stock, distribute, dispense, use, or otherwise handle under its applicable licences and authorizations.</li>
<li>The Buyer Company shall not use the TiaMeds Marketplace platform to procure pharmaceutical products for any unlawful, unauthorized, fraudulent, speculative, or prohibited purpose.</li>
<li>The Buyer Company shall not purchase products on behalf of any person or entity that is not legally eligible to procure or possess such products where such procurement is restricted under applicable law.</li>
</ul>

<h2>Controlled, Scheduled & Restricted Product Compliance</h2>
<ul>
<li>The Buyer Company shall ensure compliance with all applicable requirements and restrictions relating to prescription medicines, scheduled drugs, controlled substances, narcotic drugs, psychotropic substances, regulated medical products, and other restricted product categories.</li>
<li>Where applicable, the Buyer Company confirms that it shall procure Schedule H, H1, X, narcotic, psychotropic, or otherwise controlled or restricted products only when legally authorized and only in accordance with applicable licences, permits, documentation, record-keeping requirements, quantity restrictions, and regulatory conditions.</li>
<li>The Buyer Company acknowledges that TiaMeds Marketplace may restrict, block, hold, review, or require additional verification for transactions involving regulated or restricted products.</li>
<li>The Buyer Company shall provide additional licences, permits, declarations, documentation, or information whenever reasonably required for verification of its legal eligibility to procure a particular product or product category.</li>
</ul>

<h2>Prohibited Procurement, Drug Diversion, Abuse & Misuse Prevention</h2>
<p>The Buyer Company shall not knowingly procure, request, facilitate, receive, possess, distribute, or otherwise transact in:</p>
<ul>
<li>Banned drugs</li>
<li>Prohibited products</li>
<li>Spurious products</li>
<li>Counterfeit products</li>
<li>Misbranded products</li>
<li>Adulterated products</li>
<li>Expired medicines</li>
<li>Recalled products</li>
<li>Unauthorized imports</li>
<li>Products restricted from procurement or possession under applicable laws</li>
</ul>
<p>The Buyer Company shall not use the TiaMeds Marketplace platform for drug diversion, unauthorized resale, abuse or misuse of medicines, fraudulent procurement, circular trading, artificial or sham transactions, inventory manipulation for unlawful purposes, transactions using false or unauthorized documentation, or any other unlawful transaction or activity.</p>
<p>Any unlawful procurement, diversion, misuse, unauthorized resale, fraudulent activity, or regulatory violation attributable to the Buyer Company shall remain the responsibility of the Buyer Company.</p>

<h2>Accuracy of Buyer Information & Documentation</h2>
<ul>
<li>The Buyer Company confirms that all information and documentation provided during registration, verification, procurement, or subsequent use of the TiaMeds Marketplace platform shall be accurate, authentic, complete, lawful, valid, and up to date.</li>
<li>This includes, as applicable, legal entity information, GST registration details, PAN and statutory information, drug licences and regulatory approvals, business registrations, authorized representative information, billing and delivery addresses, bank and payment information, and other compliance documentation requested by TiaMeds Marketplace.</li>
<li>The Buyer Company shall not submit forged, fabricated, altered, expired, misleading, unauthorized, or otherwise invalid documentation.</li>
<li>The Buyer Company shall promptly update in TiaMeds Marketplace whenever any material information or submitted documentation changes.</li>
</ul>

<h2>Account Security, Authorized Users & Transaction Responsibility</h2>
<ul>
<li>The Buyer Company shall be responsible for maintaining the confidentiality and security of its account credentials, OTPs, passwords, and other authentication mechanisms.</li>
<li>The Buyer Company shall ensure that access to its TiaMeds Marketplace account is provided only to duly authorized personnel.</li>
<li>Orders, purchase requests, confirmations, approvals, and other transactions performed through the Buyer Company's authenticated account by its authorized users shall be treated as actions performed on behalf of the Buyer Company.</li>
<li>The Buyer Company shall promptly notify TiaMeds Marketplace regarding any suspected unauthorized access, credential compromise, fraudulent transaction, or misuse of its account.</li>
<li>The Buyer Company shall remain responsible for maintaining appropriate internal authorization controls governing employees or representatives permitted to place or approve orders.</li>
</ul>

<h2>Order Placement & Procurement Responsibility</h2>
<ul>
<li>The Buyer Company confirms that it shall review product details, quantities, pricing, commercial terms, applicable taxes, Seller information, delivery terms, and other relevant transaction information before placing or confirming an order.</li>
<li>The Buyer Company shall be responsible for ensuring that quantities ordered are consistent with its lawful business requirements, licence conditions, storage capacity, and applicable regulatory requirements.</li>
<li>Placement of an order through the TiaMeds Marketplace platform shall constitute a procurement request by the Buyer Company, subject to Seller acceptance, product availability, compliance verification, and applicable marketplace rules.</li>
<li>TiaMeds Marketplace may hold, flag, restrict, or cancel transactions where required for regulatory compliance, fraud prevention, risk management, licence verification, or pursuant to directions of competent authorities.</li>
</ul>

<h2>Pricing, Taxation, Payment & Financial Compliance</h2>
<ul>
<li>The Buyer Company acknowledges that product pricing, discounts, commercial terms, applicable taxes, delivery charges, and other transaction-related amounts shall be displayed or communicated through the TiaMeds Marketplace platform as applicable.</li>
<li>The Buyer Company shall be responsible for reviewing and accepting the commercial terms applicable to an order before confirming the transaction.</li>
<li>The Buyer Company shall make payments only through payment methods and mechanisms permitted by the TiaMeds Marketplace platform and in accordance with applicable laws.</li>
<li>The Buyer Company shall not use fraudulent, unauthorized, unlawful, or misrepresented payment instruments or sources of funds.</li>
<li>The Buyer Company shall remain responsible for its applicable accounting, taxation, statutory reporting, withholding, reconciliation, and financial compliance obligations arising from transactions conducted through the platform.</li>
<li>TiaMeds Marketplace shall not independently determine pharmaceutical pricing between authorized Buyers and Sellers except to the extent necessary for operation of platform functionality, fees, promotions, or other expressly disclosed marketplace services.</li>
</ul>

<h2>Product Receipt, Inspection, Storage & Handling</h2>
<ul>
<li>The Buyer Company acknowledges that the receipt, inspection, acceptance, rejection, storage, handling, and use of products purchased through the TiaMeds Marketplace platform shall be matters between the Buyer Company and the respective Seller Company, subject to applicable laws and the agreed transaction terms.</li>
<li>Upon receipt of products, the Buyer Company shall undertake appropriate verification, as applicable, including product identity, quantity, packaging condition, batch information, expiry information, invoice and delivery documentation, visible transit damage, and temperature or cold-chain condition where applicable.</li>
<li>Any discrepancy, shortage, damage, suspected tampering, temperature excursion, incorrect product, quality concern, or other issue identified upon or after receipt shall be raised by the Buyer Company with the respective Seller Company and handled between the Buyer Company and Seller Company.</li>
<li>Following delivery and transfer of responsibility in accordance with the applicable transaction terms, the Buyer Company shall be responsible for appropriate storage, handling, environmental controls, inventory management, and cold-chain maintenance applicable to products in its possession.</li>
<li>The Buyer Company shall maintain products in accordance with applicable seller instructions, regulatory requirements, licence conditions, and applicable good storage practices.</li>
<li>TiaMeds Marketplace acts solely as a technology-enabled marketplace and transaction facilitation platform and shall not be responsible for the physical receipt, inspection, acceptance, rejection, storage, handling, transportation condition, product condition, or other physical fulfilment obligations relating to products transacted between the Buyer Company and Seller Company, except to the extent expressly undertaken by TiaMeds Marketplace under a separately specified service or as required under applicable law.</li>
</ul>

<h2>Returns, Recalls, Market Withdrawal & Safety Cooperation</h2>
<ul>
<li>The Buyer Company shall comply with applicable TiaMeds Marketplace and Seller procedures relating to cancellations, returns, replacements, refunds, reverse logistics, product recalls, and market withdrawals.</li>
<li>The Buyer Company shall immediately stop further distribution, dispensing, use, or transfer of an affected product where required pursuant to a valid recall, regulatory instruction, safety communication, or market withdrawal.</li>
<li>The Buyer Company shall cooperate with the Seller, TiaMeds Marketplace, manufacturers, regulators, and other competent authorities in connection with product recalls, market withdrawals, quarantine requirements, product complaints, quality investigations, adverse event investigations, regulatory inquiries, and traceability exercises.</li>
<li>The Buyer Company shall maintain relevant purchase, batch, inventory, distribution, and other records as required under applicable law and its licence conditions.</li>
</ul>

<h2>Product Complaints, Adverse Events & Pharmacovigilance Cooperation</h2>
<ul>
<li>Where applicable to the Buyer Company's activities, the Buyer Company shall comply with applicable requirements concerning product complaints, suspected quality defects, adverse events, safety information, and pharmacovigilance reporting.</li>
<li>The Buyer Company shall promptly communicate relevant product quality or safety information to the appropriate Seller, manufacturer, TiaMeds Marketplace, regulatory authority, or other responsible entity where required under applicable law.</li>
<li>The Buyer Company shall not knowingly suppress, alter, conceal, or delay legally required information concerning serious product quality or safety matters.</li>
</ul>

<h2>Records, Audit Trails & Compliance Verification</h2>
<ul>
<li>The Buyer Company acknowledges that TiaMeds Marketplace may maintain registration records, verification history, transaction records, order history, payment-related records, system logs, audit trails, and compliance records for legitimate platform, regulatory, security, fraud-prevention, dispute-resolution, and legal purposes.</li>
<li>TiaMeds Marketplace may conduct verification reviews, compliance checks, risk assessments, or due diligence relating to the Buyer Company and transactions conducted through the platform.</li>
<li>The Buyer Company shall reasonably cooperate with requests for updated licences, statutory documents, transaction clarification, authorization evidence, or other compliance information.</li>
<li>TiaMeds Marketplace may restrict procurement capabilities or platform access pending completion of required verification or investigation.</li>
</ul>

<h2>Data Protection & Privacy</h2>
<ul>
<li>The Buyer Company confirms that information and personal data submitted to the TiaMeds Marketplace platform shall be provided lawfully and with appropriate authority.</li>
<li>The Buyer Company shall ensure that it has appropriate authorization to provide personal information relating to its employees, representatives, delivery contacts, or other individuals whose information is submitted through the platform.</li>
<li>TiaMeds Marketplace shall maintain reasonable technical, administrative, organizational, and security safeguards to protect platform data in accordance with applicable laws, including the Digital Personal Data Protection (DPDP) Act, 2023.</li>
<li>Buyer Company information may be disclosed to Sellers, payment providers, regulators, courts, law enforcement agencies, or government authorities where reasonably necessary for transaction fulfilment, platform operations, compliance, investigation, dispute resolution, or where legally required, subject to applicable law and TiaMeds Marketplace policies.</li>
</ul>

<h2>TiaMeds Marketplace Platform Role & Nature</h2>
<ul>
<li>TiaMeds Marketplace operates as a technology-enabled pharmaceutical procurement and transaction facilitation platform connecting authorized Buyers and Sellers.</li>
<li>The Buyer Company acknowledges that the Seller participating in a transaction remains responsible for the products sold by that Seller, including applicable product quality, authenticity, regulatory compliance, invoicing, and other Seller obligations as provided under applicable laws and marketplace requirements.</li>
<li>TiaMeds Marketplace functions as a neutral intermediary platform and does not independently manufacture, distribute, prescribe, recommend, or endorse pharmaceutical products.</li>
<li>TiaMeds Marketplace reserves the right to verify Buyer eligibility, request additional documentation, restrict product access, hold or block transactions, suspend Buyer accounts, freeze platform access, initiate compliance reviews or investigations, support regulatory authorities, and take fraud-prevention or risk-mitigation measures whenever reasonably required for compliance, safety, legal obligations, or regulatory directives.</li>
<li>TiaMeds Marketplace shall maintain appropriate platform governance, security, backup, and business-continuity measures consistent with its role as a technology marketplace.</li>
</ul>

<h2>Suspension, Restriction & Regulatory Cooperation</h2>
<p>The Buyer Company acknowledges that TiaMeds Marketplace may suspend, restrict, or terminate Buyer access, procurement capability, specific transactions, or access to particular product categories where:</p>
<ul>
<li>Applicable licences or registrations expire, are suspended, cancelled, or become invalid</li>
<li>Required documentation cannot be verified</li>
<li>Buyer eligibility changes</li>
<li>Suspected fraud or unauthorized activity is identified</li>
<li>Suspicious transaction patterns are detected</li>
<li>Applicable laws or regulatory requirements require restriction</li>
<li>A competent authority issues relevant instructions</li>
<li>The Buyer Company materially violates applicable marketplace policies or obligations</li>
</ul>
<p>TiaMeds Marketplace may preserve and provide relevant records or information to competent regulatory, judicial, governmental, or law-enforcement authorities where legally required.</p>

<h2>Indemnity & Limitation of Liability</h2>
<p>The Buyer Company agrees to indemnify, defend, and hold harmless TiaMeds Marketplace, its affiliates, management, employees, technology partners, and representatives against claims, liabilities, losses, penalties, damages, actions, proceedings, or regulatory consequences to the extent arising from:</p>
<ul>
<li>Unlawful procurement by the Buyer Company</li>
<li>Buyer regulatory non-compliance</li>
<li>Invalid, false, or misleading Buyer documentation</li>
<li>Unauthorized or fraudulent use attributable to the Buyer Company</li>
<li>Drug diversion or unauthorized resale attributable to the Buyer Company</li>
<li>Misrepresentation by the Buyer Company</li>
<li>Violation of applicable licence conditions</li>
<li>Illegal transactions attributable to the Buyer Company</li>
<li>Buyer Company's breach of applicable laws or these declarations</li>
</ul>
<p>TiaMeds Marketplace shall not be liable for obligations, liabilities, losses, or regulatory violations arising from acts, omissions, business conduct, misuse, unlawful procurement, storage failures, or regulatory failures attributable to the Buyer Company, subject always to applicable law.</p>

<h2>Final Mandatory Acceptance</h2>
<p>I hereby confirm that I am duly authorized to act on behalf of the Buyer Company and that I have read, understood, acknowledged, and accepted all the above declarations, obligations, eligibility requirements, compliance requirements, procurement responsibilities, data protection conditions, audit provisions, and marketplace governance policies applicable to participation as a Buyer on the TiaMeds Marketplace platform. I confirm that the Buyer Company shall procure pharmaceutical and other regulated products only to the extent legally permitted under its applicable licences, registrations, approvals, and authorizations. I further understand that any non-compliance, invalid licence or documentation, failure to disclose a material change in regulatory eligibility, suspected unlawful activity, or regulatory concern may result in transaction restriction, compliance review, suspension, regulatory reporting where legally required, investigation, restriction of platform access, or permanent removal from the TiaMeds Marketplace platform.</p>
$BUYER$,
    1,
    TRUE,
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (content_key) DO UPDATE
    SET title      = EXCLUDED.title,
        content    = EXCLUDED.content,
        version    = tbl_legal_content.version + 1,
        updated_by = 'SYSTEM'
    WHERE tbl_legal_content.content IS DISTINCT FROM EXCLUDED.content
       OR tbl_legal_content.title IS DISTINCT FROM EXCLUDED.title;

INSERT INTO tbl_legal_content (content_key, title, content, version, is_active, created_by, updated_by)
VALUES (
    'SELLER_TERMS',
    'Seller Company Pre-Registration Mandatory Declarations & Acceptance',
    $SELLER$
<h2>1. Authorized Representation & Legal Binding</h2>
<ul>
<li>As a condition precedent to Seller Company registration on the TiaMeds Marketplace platform, the following mandatory declarations and acceptances shall be reviewed and accepted either directly by the Seller Company or by an individual duly authorized to act on behalf of the Seller Company.</li>
<li>Any acceptance provided through the TiaMeds Marketplace platform shall be deemed to constitute a legally binding acceptance by the Seller Company.</li>
<li>The Seller Company confirms that the individual completing the registration process is duly authorized to submit information, provide declarations, and accept obligations on behalf of the Seller Company.</li>
<li>Registration of the Seller Company on the TiaMeds Marketplace platform shall not proceed unless the final mandatory acceptance checkbox is acknowledged.</li>
</ul>

<h2>2. Valid License Confirmation</h2>
<ul>
<li>The Seller Company confirms that it holds valid, active, and applicable pharmaceutical licenses, approvals, and authorizations required under applicable laws for manufacturing, distribution, stocking, wholesale, retail, or sale of pharmaceutical products.</li>
<li>The Seller Company confirms that all products listed, onboarded, marketed, distributed, or sold through the TiaMeds Marketplace platform fall within the scope of its valid regulatory licenses and approvals.</li>
<li>The Seller Company shall ensure that all licenses remain valid and active throughout its participation on the TiaMeds Marketplace platform.</li>
<li>The Seller Company shall immediately notify TiaMeds Marketplace support team regarding any suspension, cancellation, expiry, restriction, or regulatory action impacting its licenses or operations.</li>
</ul>

<h2>3. Regulatory Compliance Commitment</h2>
<p>The Seller Company confirms that all products onboarded on the TiaMeds Marketplace platform comply with all applicable laws, regulations, standards, and regulatory requirements, including but not limited to:</p>
<ul>
<li>Drugs and Cosmetics Act, 1940</li>
<li>Drugs and Cosmetics Rules, 1945</li>
<li>Medical Devices Rules, 2017</li>
<li>Applicable CDSCO guidelines</li>
<li>Applicable State FDA regulations</li>
<li>Any other applicable Central or State laws</li>
</ul>
<p>The Seller Company accepts sole responsibility for ensuring ongoing regulatory compliance of all products listed or sold through the TiaMeds Marketplace platform.</p>

<h2>4. Buyer Eligibility & Controlled Transaction Compliance</h2>
<ul>
<li>The Seller Company confirms that pharmaceutical products shall be sold only to legally authorized, verified, and eligible buyers onboarded on the TiaMeds Marketplace platform.</li>
<li>The Seller Company shall ensure compliance with all restrictions applicable to prescription medicines, controlled substances, scheduled drugs, regulated medical products, and jurisdiction-specific sale restrictions.</li>
<li>The Seller Company shall ensure that Schedule H, H1, X, narcotic, psychotropic, temperature-sensitive, or otherwise regulated products are sold strictly in accordance with applicable laws and valid documentation requirements.</li>
</ul>

<h2>5. Prohibited & Restricted Products</h2>
<p>The Seller Company confirms that it shall not onboard, distribute, advertise, market, or sell any:</p>
<ul>
<li>Banned drugs</li>
<li>Prohibited products</li>
<li>Spurious products</li>
<li>Counterfeit products</li>
<li>Misbranded products</li>
<li>Adulterated products</li>
<li>Expired medicines</li>
<li>Recalled products</li>
<li>Unauthorized imports</li>
<li>Products restricted under applicable laws</li>
</ul>
<p>The registering Seller Company confirms that they will list and sell only those products that are within the scope of their applicable licenses on the TiaMeds Marketplace platform.</p>

<h2>6. Product Quality, Safety & Authenticity Responsibility</h2>
<ul>
<li>The Seller Company shall remain solely responsible for the quality, safety, efficacy, authenticity, labelling, packaging, storage condition, batch integrity, and regulatory compliance of all products listed on the TiaMeds Marketplace platform.</li>
<li>Any liability arising from sub-standard, defective, adulterated, counterfeit, misbranded, damaged, or non-compliant products shall rest entirely with the Seller Company.</li>
<li>The TiaMeds Marketplace platform will not be responsible for quality verification for products onboarded on the TiaMeds Marketplace platform.</li>
</ul>

<h2>7. Product Information Accuracy & Legal Compliance</h2>
<p>The registering Seller Company confirms that it is solely responsible for ensuring all product information provided during product onboarding is accurate, complete, and truthful, and that the product listing complies with all applicable laws (as mentioned in point no.3). Any misleading, exaggerated, or false claims are entirely the liability of the registering Seller Company.</p>

<h2>8. Pricing, Trade & DPCO Compliance</h2>
<p>The Seller Company confirms that all product pricing shall comply with:</p>
<ul>
<li>Drugs Price Control Order (DPCO), 2013</li>
<li>National Pharmaceutical Pricing Authority (NPPA) notifications</li>
<li>Applicable taxation laws</li>
<li>Applicable trade regulations</li>
<li>Any other statutory pricing requirements</li>
</ul>
<p>The Seller Company shall ensure that no overcharging occurs beyond applicable MRP, ceiling price, or statutory limits. The Seller Company shall remain solely responsible for all pricing decisions, discounts, commercial terms, and pricing compliance obligations. TiaMeds Marketplace shall not influence or control pharmaceutical pricing decisions between authorized buyers and sellers.</p>

<h2>9. Drug Diversion, Abuse & Misuse Prevention</h2>
<p>The Seller Company shall take appropriate measures to prevent:</p>
<ul>
<li>Drug diversion</li>
<li>Unauthorized resale</li>
<li>Abuse or misuse of medicines</li>
<li>Inventory hoarding</li>
<li>Fraudulent procurement</li>
<li>Circular trading</li>
<li>Suspicious ordering patterns</li>
<li>Unlawful transactions</li>
</ul>
<p>The Seller Company confirms that products sold through the TiaMeds Marketplace platform shall be used only for lawful medical, institutional, or healthcare purposes. Any unlawful activity, any non-compliance or violation shall be entirely the liability of the registering Seller Company.</p>

<h2>10. Storage, Logistics & Cold-Chain Compliance</h2>
<p>The Seller Company shall remain fully responsible for appropriate:</p>
<ul>
<li>Storage</li>
<li>Warehousing</li>
<li>Packaging</li>
<li>Transportation</li>
<li>Environmental controls</li>
<li>Cold-chain maintenance</li>
</ul>
<p>applicable to products sold through the TiaMeds Marketplace platform. The Seller Company shall ensure preservation of product quality, safety, stability, and integrity throughout logistics and delivery operations. Any loss, degradation, contamination, spoilage, temperature excursion, or logistics non-compliance shall remain solely the liability of the Seller Company.</p>

<h2>11. Product Recall & Market Withdrawal</h2>
<p>The Seller Company shall immediately initiate product recalls, market withdrawals, quarantine procedures, or regulatory notifications whenever required by:</p>
<ul>
<li>CDSCO</li>
<li>State FDA</li>
<li>Manufacturers</li>
<li>Courts</li>
<li>Other competent authorities</li>
</ul>
<p>The Seller Company shall remain solely responsible for recall execution, stakeholder communication, investigation support, and regulatory compliance relating to affected products.</p>

<h2>12. Order Management, Returns & Pharmacovigilance</h2>
<p>The Seller Company shall remain solely responsible for management of:</p>
<ul>
<li>Order fulfilment</li>
<li>Cancellations</li>
<li>Replacements</li>
<li>Returns</li>
<li>Refunds</li>
<li>Reverse logistics</li>
</ul>
<p>The Seller Company shall comply with all applicable regulatory requirements relating to adverse event reporting, product complaints, quality defects, safety monitoring, and pharmacovigilance obligations. The Seller Company shall ensure timely reporting in accordance with:</p>
<ul>
<li>Drugs and Cosmetics Act, 1940</li>
<li>Drugs and Cosmetics Rules, 1945</li>
<li>Pharmacovigilance Programme of India (PvPI) Guidelines</li>
<li>CDSCO guidelines</li>
<li>Other applicable laws and regulations</li>
</ul>
<p>Any lapse, delay, suppression, or non-compliance relating to pharmacovigilance obligations shall remain solely the liability of the Seller Company.</p>

<h2>13. Invoicing, Taxation & Financial Compliance</h2>
<p>The registering Seller Company confirms that it will take full responsibility for issuing invoices in its company name in accordance with below applicable laws:</p>
<ul>
<li>Goods and Services Tax (GST) Act, 2017</li>
<li>Income Tax Act, 1961</li>
<li>Companies Act, 2013</li>
<li>State-specific Tax Rules</li>
<li>Any other statutory regulations</li>
</ul>
<p>The Seller Company shall maintain complete financial, taxation, and transaction records as required under applicable laws. Any non-compliance relating to taxation, invoicing, statutory deductions, reporting obligations, or financial irregularities shall remain solely the liability of the Seller Company.</p>

<h2>14. Data Protection, Privacy, Audit Trails & Record Retention</h2>
<ul>
<li>The Seller Company confirms that all information and documentation submitted to the TiaMeds Marketplace platform shall be accurate, lawful, complete, and up to date.</li>
<li>TiaMeds Marketplace shall maintain reasonable technical, administrative, organizational, and security safeguards to protect platform data in accordance with applicable laws, including the Digital Personal Data Protection (DPDP) Act, 2023.</li>
<li>TiaMeds Marketplace may implement role-based access controls, audit logging, encryption mechanisms, data monitoring controls, secure API integrations, and compliance monitoring systems to support platform governance and data protection.</li>
<li>TiaMeds Marketplace shall maintain transaction logs, audit trails, onboarding history, and compliance records to support traceability, investigations, regulatory verification, and dispute resolution.</li>
<li>TiaMeds Marketplace may conduct verification reviews, compliance audits, risk assessments, or due diligence checks relating to Seller Companies and their listed products.</li>
<li>Seller information may be disclosed to regulators, courts, law enforcement agencies, or government authorities whenever legally required.</li>
</ul>

<h2>15. TiaMeds Marketplace Platform Role & Nature</h2>
<ul>
<li>TiaMeds Marketplace operates as a technology-enabled pharmaceutical procurement and transaction facilitation platform connecting authorized buyers and sellers.</li>
<li>TiaMeds Marketplace functions as a neutral intermediary platform and does not independently manufacture, distribute, prescribe, recommend, or endorse pharmaceutical products.</li>
<li>TiaMeds Marketplace reserves the right to suspend Seller accounts, block transactions, restrict listings, remove products, freeze platform access, initiate investigations, support regulatory authorities, and request additional documentation whenever required for compliance, risk mitigation, fraud prevention, legal obligations, or regulatory directives.</li>
<li>TiaMeds Marketplace shall always maintain platform availability, except during scheduled maintenance windows and force majeure events, and shall provide Seller Companies with reasonable advance notice of any planned maintenance activities that may impact platform availability.</li>
<li>TiaMeds Marketplace shall maintain appropriate backup, disaster recovery, and system failover mechanisms to support business continuity.</li>
</ul>

<h2>16. Indemnity & Limitation of Liability</h2>
<p>The Seller Company agrees to fully indemnify, defend, and hold harmless TiaMeds Marketplace, its affiliates, management, employees, technology partners, and representatives against any claims, liabilities, losses, penalties, damages, actions, proceedings, or regulatory consequences arising from:</p>
<ul>
<li>Seller activities</li>
<li>Product-related violations</li>
<li>Regulatory non-compliance</li>
<li>Misrepresentation</li>
<li>Fraudulent conduct</li>
<li>Intellectual property violations</li>
<li>Product defects</li>
<li>Illegal transactions</li>
</ul>
<p>TiaMeds Marketplace shall not be liable for any obligations, liabilities, losses, or violations arising from the products, actions, omissions, business conduct, or regulatory failures of Seller Companies.</p>

<h2>Final Mandatory Acceptance</h2>
<p>I hereby confirm that I have read, understood, acknowledged, and accepted all the above declarations, obligations, compliance requirements, operational responsibilities, data protection conditions, audit provisions, and marketplace governance policies applicable to participation on the TiaMeds Marketplace platform. I further understand that any non-compliance may result in suspension, regulatory reporting, investigation, restriction of platform access, or permanent removal from the TiaMeds Marketplace platform.</p>
$SELLER$,
    1,
    TRUE,
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (content_key) DO UPDATE
    SET title      = EXCLUDED.title,
        content    = EXCLUDED.content,
        version    = tbl_legal_content.version + 1,
        updated_by = 'SYSTEM'
    WHERE tbl_legal_content.content IS DISTINCT FROM EXCLUDED.content
       OR tbl_legal_content.title IS DISTINCT FROM EXCLUDED.title;
