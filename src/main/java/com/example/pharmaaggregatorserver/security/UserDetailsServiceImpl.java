package com.example.pharmaaggregatorserver.security;

import com.example.pharmaaggregatorserver.entity.auth.User;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;

import com.example.pharmaaggregatorserver.repository.auth.UserRepository;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final BuyerUserRepository buyerUserRepository;

    // Seller/admin logins live in tbl_user; buyer logins live in the fully
    // separate tbl_buyer_user (see BuyerAuthService). The JWT subject is just
    // the username/email either side issued it for — if the same email is
    // registered on both sides (a person who is both a seller and a buyer),
    // this lookup order alone can't tell which account issued the token.
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByUsername(username, false);
    }

    // preferBuyer lets AuthTokenFilter tell us which domain issued the
    // request (based on the /buyer/** path prefix), so an email registered
    // on both sides resolves to the correct account instead of always
    // hitting whichever table happens to be checked first. Without this, a
    // buyer JWT for a dual-registered email would silently resolve to the
    // seller/admin identity (no ROLE_BUYER), 401ing every buyer endpoint.
    @Transactional
    public UserDetails loadUserByUsername(String username, boolean preferBuyer) throws UsernameNotFoundException {
        if (preferBuyer) {
            Optional<UserDetails> buyer = buyerUserRepository.findByEmail(username).map(this::buildBuyerUserDetails);
            if (buyer.isPresent()) {
                return buyer.get();
            }
            return userRepository.findByUsername(username).map(UserDetailsImpl::build)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        }

        Optional<UserDetails> seller = userRepository.findByUsername(username).map(UserDetailsImpl::build);
        if (seller.isPresent()) {
            return seller.get();
        }
        return buyerUserRepository.findByEmail(username)
                .map(this::buildBuyerUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    }

    private UserDetails buildBuyerUserDetails(BuyerUser buyerUser) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_BUYER"));
        return UserDetailsImpl.builder()
                .id(buyerUser.getBuyerUserId())
                .username(buyerUser.getEmail())
                .password(buyerUser.getPasswordHash())
                .isPasswordTemporary(buyerUser.isPasswordTemporary())
                .isActive(buyerUser.isActive())
                .isAccountLocked(buyerUser.isAccountLocked())
                .authorities(authorities)
                .build();
    }
}