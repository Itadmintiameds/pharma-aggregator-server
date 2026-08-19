package com.example.pharmaaggregatorserver.controller.quote;

import com.example.pharmaaggregatorserver.dto.quote.QuoteRequestCreateDTO;
import com.example.pharmaaggregatorserver.dto.quote.QuoteRequestResponseDTO;
import com.example.pharmaaggregatorserver.exception.UnauthorizedException;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.quote.QuoteRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buyer/quote-requests")
@RequiredArgsConstructor
public class BuyerQuoteRequestController {

    private final QuoteRequestService quoteRequestService;

    // The caller's own buyerUserId is always resolved from the authenticated
    // JWT principal (mirrors SellerOrderController) rather than trusted from
    // the request body. Used by every endpoint except create(), which must
    // also work for a logged-out guest.
    private Long resolveAuthenticatedBuyerUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl user)) {
            throw new UnauthorizedException("Unauthorized access");
        }
        boolean isBuyer = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_BUYER"));
        if (!isBuyer) {
            throw new UnauthorizedException("Unauthorized access");
        }
        return user.getId();
    }

    // Same JWT-principal resolution as above, but returns null instead of
    // throwing for an anonymous caller — create() treats null as "provision
    // or reuse a buyer account from the submitted email" (guest checkout).
    private Long resolveOptionalBuyerUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl user) {
            boolean isBuyer = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_BUYER"));
            if (isBuyer) {
                return user.getId();
            }
        }
        return null;
    }

    // POST /buyer/quote-requests — used by both "Request Price Option" and
    // "Get a Quote" forms on the product page; requestType distinguishes them.
    // Works for both logged-in buyers and anonymous guests.
    @PostMapping
    public ResponseEntity<ApiResponse<QuoteRequestResponseDTO>> create(
            @Valid @RequestBody QuoteRequestCreateDTO request, Authentication authentication) {
        Long buyerUserId = resolveOptionalBuyerUserId(authentication);
        QuoteRequestResponseDTO response = quoteRequestService.create(buyerUserId, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Request submitted successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuoteRequestResponseDTO>>> list(Authentication authentication) {
        Long buyerUserId = resolveAuthenticatedBuyerUserId(authentication);
        List<QuoteRequestResponseDTO> response = quoteRequestService.listForBuyer(buyerUserId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(), "Requests fetched successfully", response, (long) response.size()));
    }

    @PatchMapping("/{quoteRequestId}/accept")
    public ResponseEntity<ApiResponse<QuoteRequestResponseDTO>> accept(
            @PathVariable Long quoteRequestId, Authentication authentication) {
        Long buyerUserId = resolveAuthenticatedBuyerUserId(authentication);
        QuoteRequestResponseDTO response = quoteRequestService.accept(quoteRequestId, buyerUserId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Quote accepted", response));
    }

    @PatchMapping("/{quoteRequestId}/reject")
    public ResponseEntity<ApiResponse<QuoteRequestResponseDTO>> reject(
            @PathVariable Long quoteRequestId, Authentication authentication) {
        Long buyerUserId = resolveAuthenticatedBuyerUserId(authentication);
        QuoteRequestResponseDTO response = quoteRequestService.reject(quoteRequestId, buyerUserId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Quote rejected", response));
    }
}
