package com.example.pharmaaggregatorserver.dto.seller;

import com.example.pharmaaggregatorserver.entity.seller.Seller;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class SellerBankDetailsDTO {

    private Long sellerBankDetailsId;

    private String bankName;

    private String branch;

    private String ifscCode;

    private String accountNumber;

    private String accountHolderName;

    private String bankDocumentFileUrl;
}
