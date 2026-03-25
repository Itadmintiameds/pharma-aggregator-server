package com.example.pharmaaggregatorserver.service.profile;

import com.example.pharmaaggregatorserver.dto.seller.profile.PendingSellerDocumentUploadRequest;
import com.example.pharmaaggregatorserver.dto.seller.profile.PendingSellerDocumentUploadResponse;

public interface PendingSellerDocumentService {

    PendingSellerDocumentUploadResponse uploadDocuments(Long pendingSellerId,
                                                        PendingSellerDocumentUploadRequest request);
}