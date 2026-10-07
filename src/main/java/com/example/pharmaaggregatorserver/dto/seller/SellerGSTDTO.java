package com.example.pharmaaggregatorserver.dto.seller;

import com.example.pharmaaggregatorserver.entity.seller.Seller;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class SellerGSTDTO {

    private Long sellerGstId;

    private String gstNumber;

    private String gstFileUrl;

    private boolean isGstVerified;
}
