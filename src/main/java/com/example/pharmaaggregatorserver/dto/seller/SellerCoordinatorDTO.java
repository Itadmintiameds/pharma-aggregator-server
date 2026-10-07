package com.example.pharmaaggregatorserver.dto.seller;

import com.example.pharmaaggregatorserver.entity.seller.Seller;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class SellerCoordinatorDTO {

    private Long sellerCoordinatorId;

    private String name;

    private String designation;

    private String email;

    private boolean isEmailVerified;

    private String mobile;

    private boolean isPhoneVerified;

}
