package com.example.pharmaaggregatorserver.dto.seller;

import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class SellerDocumentDto {

    private Long sellerDocumentsId;

    private Long productTypeId;

    private String documentNumber;

    private String documentFileUrl;

    private boolean isDocumentVerified;
}
