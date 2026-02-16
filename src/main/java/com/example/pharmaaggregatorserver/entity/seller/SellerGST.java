package com.example.pharmaaggregatorserver.entity.seller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_seller_gst")
public class SellerGST {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_gst_id")
    private Long sellerGstId;

    @Column(name = "gst_number", nullable = false, length = 100)
    private String gstNumber;

    @Column(name = "gst_document_file_url", nullable = false)
    private String gstFileUrl;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", unique = true, nullable = false)
    @JsonIgnore
    private Seller seller;

    @Column(name = "is_gst_verified", columnDefinition = "boolean default false", nullable = false)
    private boolean isGstVerified = false;

}
