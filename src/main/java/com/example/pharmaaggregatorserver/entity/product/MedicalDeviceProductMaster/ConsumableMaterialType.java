package com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster;

import com.example.pharmaaggregatorserver.entity.product.ProductAttributeConsumableMedical;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_consumable_material_type_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableMaterialType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_type_id")
    private Long materialTypeId;

    @Column(name = "material_type_name", nullable = false)
    private String materialTypeName;

    @Column(name = "description")
    private String description;

    // Bi-directional mapping (optional but recommended)
    @ManyToMany(mappedBy = "materialTypes")
    @ToString.Exclude
    private java.util.List<ProductAttributeConsumableMedical> productAttributes;
}