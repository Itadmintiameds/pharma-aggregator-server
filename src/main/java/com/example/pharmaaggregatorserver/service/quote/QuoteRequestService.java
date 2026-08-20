package com.example.pharmaaggregatorserver.service.quote;

import com.example.pharmaaggregatorserver.dto.quote.QuoteRequestCreateDTO;
import com.example.pharmaaggregatorserver.dto.quote.QuoteRequestResponseDTO;
import com.example.pharmaaggregatorserver.dto.quote.SellerQuoteResponseDTO;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import com.example.pharmaaggregatorserver.entity.quote.QuoteRequest;
import com.example.pharmaaggregatorserver.enums.QuoteRequestStatus;
import com.example.pharmaaggregatorserver.enums.QuoteRequestType;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerUserRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductDetailsRepository;
import com.example.pharmaaggregatorserver.repository.quote.QuoteRequestRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import com.example.pharmaaggregatorserver.utils.PasswordGeneratorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteRequestService {

    private final QuoteRequestRepository quoteRequestRepository;
    private final ProductDetailsRepository productDetailsRepository;
    private final BuyerUserRepository buyerUserRepository;
    private final EmailService emailService;
    private final PasswordGeneratorUtils passwordGeneratorUtils;
    private final PasswordEncoder passwordEncoder;

    // buyerUserId is null for a guest submission (no logged-in buyer) — the
    // caller (BuyerQuoteRequestController) only resolves it from the JWT when
    // one is present, never trusting a client-supplied id either way.
    @Transactional
    public QuoteRequestResponseDTO create(Long buyerUserId, QuoteRequestCreateDTO request) {
        ProductDetails product = productDetailsRepository.findById(request.getProductId())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Product not found: " + request.getProductId()));

        BuyerUser buyerUser = buyerUserId != null
                ? buyerUserRepository.findById(buyerUserId)
                        .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "Buyer account not found"))
                : resolveOrCreateGuestBuyer(request);

        QuoteRequestType type;
        try {
            type = QuoteRequestType.valueOf(request.getRequestType());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid request type: " + request.getRequestType());
        }

        QuoteRequest quoteRequest = new QuoteRequest();
        quoteRequest.setRequestType(type);
        quoteRequest.setProduct(product);
        quoteRequest.setSeller(product.getSeller());
        quoteRequest.setBuyerUser(buyerUser);
        quoteRequest.setQuantity(request.getQuantity());
        quoteRequest.setUnit(request.getUnit());
        quoteRequest.setTargetPrice(request.getTargetPrice());
        quoteRequest.setPincode(request.getPincode());
        quoteRequest.setDeliveryLocation(request.getDeliveryLocation());
        quoteRequest.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        quoteRequest.setPaymentTerms(request.getPaymentTerms());
        quoteRequest.setCompanyName(request.getCompanyName());
        quoteRequest.setGstNumber(request.getGstNumber());
        quoteRequest.setContactPerson(request.getContactPerson());
        quoteRequest.setPhone(request.getPhone());
        quoteRequest.setEmail(request.getEmail());
        quoteRequest.setMessage(request.getMessage());
        quoteRequest.setStatus(QuoteRequestStatus.PENDING);

        QuoteRequest saved = quoteRequestRepository.save(quoteRequest);
        notifyOnCreate(saved);
        return toDto(saved);
    }

    public List<QuoteRequestResponseDTO> listForBuyer(Long buyerUserId) {
        return quoteRequestRepository.findByBuyerUser_BuyerUserIdOrderByCreatedAtDesc(buyerUserId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<QuoteRequestResponseDTO> listForSeller(String sellerId) {
        return quoteRequestRepository.findBySeller_SellerIdOrderByCreatedAtDesc(sellerId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public QuoteRequestResponseDTO respond(Long quoteRequestId, String sellerId, SellerQuoteResponseDTO request) {
        QuoteRequest quoteRequest = quoteRequestRepository.findByQuoteRequestIdAndSeller_SellerId(quoteRequestId, sellerId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Quote request not found: " + quoteRequestId));

        if (quoteRequest.getStatus() != QuoteRequestStatus.PENDING) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "This request has already been responded to");
        }

        quoteRequest.setQuotedPrice(request.getQuotedPrice());
        quoteRequest.setQuoteValidUntil(request.getQuoteValidUntil());
        quoteRequest.setSellerNotes(request.getSellerNotes());
        quoteRequest.setStatus(QuoteRequestStatus.QUOTED);

        QuoteRequest saved = quoteRequestRepository.save(quoteRequest);
        notifyOnRespond(saved);
        return toDto(saved);
    }

    @Transactional
    public QuoteRequestResponseDTO accept(Long quoteRequestId, Long buyerUserId) {
        QuoteRequest quoteRequest = quoteRequestRepository.findByQuoteRequestIdAndBuyerUser_BuyerUserId(quoteRequestId, buyerUserId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Quote request not found: " + quoteRequestId));

        if (quoteRequest.getStatus() != QuoteRequestStatus.QUOTED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Only a quoted request can be accepted");
        }

        quoteRequest.setStatus(QuoteRequestStatus.ACCEPTED);
        return toDto(quoteRequestRepository.save(quoteRequest));
    }

    @Transactional
    public QuoteRequestResponseDTO reject(Long quoteRequestId, Long buyerUserId) {
        QuoteRequest quoteRequest = quoteRequestRepository.findByQuoteRequestIdAndBuyerUser_BuyerUserId(quoteRequestId, buyerUserId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Quote request not found: " + quoteRequestId));

        if (quoteRequest.getStatus() != QuoteRequestStatus.QUOTED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Only a quoted request can be rejected");
        }

        quoteRequest.setStatus(QuoteRequestStatus.REJECTED);
        return toDto(quoteRequestRepository.save(quoteRequest));
    }

    // A guest (not logged in) submitting a request either matches an existing
    // buyer account by email, or gets a brand-new BuyerUser auto-provisioned
    // with a temporary password. A match still holding isPasswordTemporary
    // (they've never actually logged in — the earlier temp-password email may
    // have been lost) gets a freshly rotated temp password re-emailed, since
    // that's the only way for them to ever get in otherwise. A match that has
    // already set a real password is reused as-is — no credentials touched.
    private BuyerUser resolveOrCreateGuestBuyer(QuoteRequestCreateDTO request) {
        return buyerUserRepository.findByEmail(request.getEmail())
                .map(existing -> existing.isPasswordTemporary() ? rotateGuestTempPassword(existing) : existing)
                .orElseGet(() -> createGuestBuyer(request));
    }

    private BuyerUser createGuestBuyer(QuoteRequestCreateDTO request) {
        BuyerUser buyerUser = new BuyerUser();
        buyerUser.setEmail(request.getEmail());
        buyerUser.setPhone(request.getPhone());
        buyerUser.setFullName(request.getContactPerson());
        buyerUser.setPasswordTemporary(true);
        buyerUser.setEmailVerified(false);
        buyerUser.setPhoneVerified(false);
        buyerUser.setActive(true);
        buyerUser.setAccountLocked(false);
        buyerUser.setFailedLoginAttempts(0);

        return rotateGuestTempPassword(buyerUser);
    }

    // Generates a fresh temp password, hashes + persists it, and emails the
    // plaintext to the buyer. Shared by first-time guest account creation and
    // by re-sending credentials on a later guest submission that still hasn't
    // been logged into.
    private BuyerUser rotateGuestTempPassword(BuyerUser buyerUser) {
        String tempPassword = passwordGeneratorUtils.generateTemporaryPassword();
        buyerUser.setPasswordHash(passwordEncoder.encode(tempPassword));
        buyerUser.setPasswordTemporary(true);

        BuyerUser saved = buyerUserRepository.save(buyerUser);
        sendGuestAccountEmail(saved, tempPassword);
        return saved;
    }

    private void sendGuestAccountEmail(BuyerUser buyerUser, String tempPassword) {
        try {
            emailService.sendMail(
                    buyerUser.getEmail(),
                    "Your TiaMeds Marketplace buyer account login details",
                    "Hi " + buyerUser.getFullName() + ",\n\n"
                            + "Here are your buyer account login details so you can track this request:\n\n"
                            + "Email: " + buyerUser.getEmail() + "\n"
                            + "Temporary password: " + tempPassword + "\n\n"
                            + "You'll be asked to set a new password the first time you log in. "
                            + "If you were sent a temporary password before, that one no longer works — use this one.\n\n"
                            + "Thanks,\nTiaMeds Marketplace"
            );
        } catch (Exception e) {
            log.warn("Failed to send guest-account email to {}: {}", buyerUser.getEmail(), e.getMessage());
        }
    }

    // Best-effort — a mail-server hiccup must never fail the buyer's
    // submission or the seller's response, so every send is caught and logged.
    private void notifyOnCreate(QuoteRequest q) {
        String typeLabel = q.getRequestType() == QuoteRequestType.RFQ ? "RFQ" : "price request";
        String productName = q.getProduct().getProductName();
        String quantityText = q.getQuantity() + (q.getUnit() != null && !q.getUnit().isBlank() ? " " + q.getUnit() : "");

        try {
            emailService.sendMail(
                    q.getEmail(),
                    "We've received your " + typeLabel + " for " + productName,
                    "Hi,\n\nWe've received your " + typeLabel + " for " + productName + " (quantity: " + quantityText + ").\n"
                            + "The seller will respond with a price shortly. You can track this request anytime from your buyer dashboard under RFQ & Quotes.\n\n"
                            + "Reference: #" + q.getQuoteRequestId() + "\n\nThanks,\nTiaMeds Marketplace"
            );
        } catch (Exception e) {
            log.warn("Failed to send buyer confirmation email for quote request {}: {}", q.getQuoteRequestId(), e.getMessage());
        }

        String sellerEmail = q.getSeller().getEmail();
        if (sellerEmail != null && !sellerEmail.isBlank()) {
            try {
                emailService.sendMail(
                        sellerEmail,
                        "New " + typeLabel + " for " + productName,
                        "Hi,\n\nYou've received a new " + typeLabel + " for " + productName + " (quantity: " + quantityText + ").\n"
                                + "Please log in to your seller dashboard's Quote Requests section to respond.\n\n"
                                + "Reference: #" + q.getQuoteRequestId() + "\n\nThanks,\nTiaMeds Marketplace"
                );
            } catch (Exception e) {
                log.warn("Failed to send seller notification email for quote request {}: {}", q.getQuoteRequestId(), e.getMessage());
            }
        }
    }

    private void notifyOnRespond(QuoteRequest q) {
        if (q.getEmail() == null || q.getEmail().isBlank()) {
            return;
        }
        try {
            emailService.sendMail(
                    q.getEmail(),
                    "You've received a quote for " + q.getProduct().getProductName(),
                    "Hi,\n\nThe seller has quoted ₹" + q.getQuotedPrice() + " for your request on "
                            + q.getProduct().getProductName() + "."
                            + (q.getQuoteValidUntil() != null ? " This quote is valid until " + q.getQuoteValidUntil() + "." : "")
                            + "\n\nLog in to your buyer dashboard to accept or reject this quote.\n\n"
                            + "Reference: #" + q.getQuoteRequestId() + "\n\nThanks,\nTiaMeds Marketplace"
            );
        } catch (Exception e) {
            log.warn("Failed to send buyer quote-received email for quote request {}: {}", q.getQuoteRequestId(), e.getMessage());
        }
    }

    private QuoteRequestResponseDTO toDto(QuoteRequest q) {
        return QuoteRequestResponseDTO.builder()
                .quoteRequestId(q.getQuoteRequestId())
                .requestType(q.getRequestType().name())
                .status(q.getStatus().name())
                .productId(q.getProduct().getProductId())
                .productName(q.getProduct().getProductName())
                .sellerId(q.getSeller().getSellerId())
                .sellerName(q.getSeller().getSellerName())
                .quantity(q.getQuantity())
                .unit(q.getUnit())
                .targetPrice(q.getTargetPrice())
                .pincode(q.getPincode())
                .deliveryLocation(q.getDeliveryLocation())
                .expectedDeliveryDate(q.getExpectedDeliveryDate())
                .paymentTerms(q.getPaymentTerms())
                .companyName(q.getCompanyName())
                .gstNumber(q.getGstNumber())
                .contactPerson(q.getContactPerson())
                .phone(q.getPhone())
                .email(q.getEmail())
                .message(q.getMessage())
                .quotedPrice(q.getQuotedPrice())
                .quoteValidUntil(q.getQuoteValidUntil())
                .sellerNotes(q.getSellerNotes())
                .orderId(q.getOrderId())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }
}
