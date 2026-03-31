package com.example.pharmaaggregatorserver.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tm_pack_type")
public class PackType {

    @Id
    @Column(name = "pack_id")
    private Long packId;

    @Column(name = "pack_type")
    private String packType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosage_id", nullable = false)
    @JsonIgnore
    private DosageForm dosageForm;

    @OneToMany(mappedBy = "packType")
    @JsonIgnore
    private List<PackagingDetails> packagingDetailsList;

}
