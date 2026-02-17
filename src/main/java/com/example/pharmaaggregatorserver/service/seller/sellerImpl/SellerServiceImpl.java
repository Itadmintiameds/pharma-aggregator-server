package com.example.pharmaaggregatorserver.service.seller.sellerImpl;

import com.example.pharmaaggregatorserver.dto.seller.SellerDTO;
import com.example.pharmaaggregatorserver.dto.seller.SellerDocumentDto;
import com.example.pharmaaggregatorserver.entity.master.CompanyTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;
import com.example.pharmaaggregatorserver.entity.seller.*;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.mapper.seller.*;
import com.example.pharmaaggregatorserver.repository.master.*;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.service.seller.SellerService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final CompanyTypeMasterRepository companyTypeMasterRepository;
    private final SellerTypeMasterRepository sellerTypeMasterRepository;
    private final ProductTypeMasterRepository productTypeMasterRepository;
    private final StateMasterRepository stateMasterRepository;
    private final DistrictMasterRepository districtMasterRepository;
    private final TalukaMasterRepository talukaMasterRepository;

    @Override
    public List<SellerDTO> findAll() {
        List<Seller> sellers = sellerRepository.findAll();
        if (sellers.isEmpty()) {
            return List.of();
        }
        return sellers
                .stream()
                .map(SellerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SellerDTO findBySellerId(String sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found with id: " + sellerId));
        return SellerMapper.toDto(seller);
    }

    @Override
    @Transactional
    public SellerDTO save(SellerDTO sellerDTO) {
        // Create Seller entity
        Seller seller = new Seller();
        seller.setSellerId(sellerDTO.getSellerId());
        seller.setSellerName(sellerDTO.getSellerName());
        seller.setPhone(sellerDTO.getPhone());
        seller.setPhoneVerified(sellerDTO.isPhoneVerified());
        seller.setEmail(sellerDTO.getEmail());
        seller.setEmailVerified(sellerDTO.isEmailVerified());
        seller.setWebsite(sellerDTO.getWebsite());
        seller.setStatus(sellerDTO.getStatus());
        seller.setTermsAccepted(sellerDTO.isTermsAccepted());
        seller.setCreatedBy("ADMIN");
        seller.setUpdatedBy("ADMIN");

        // Set CompanyType
        if (sellerDTO.getCompanyTypeId() != null) {
            CompanyTypeMaster companyType = companyTypeMasterRepository.findById(sellerDTO.getCompanyTypeId())
                    .orElseThrow(() -> new NotFoundException("Company type not found with id: " + sellerDTO.getCompanyTypeId()));
            seller.setCompanyType(companyType);
        }

        // Set SellerType
        if (sellerDTO.getSellerTypeId() != null) {
            SellerTypeMaster sellerType = sellerTypeMasterRepository.findById(sellerDTO.getSellerTypeId())
                    .orElseThrow(() -> new NotFoundException("Seller type not found with id: " + sellerDTO.getSellerTypeId()));
            seller.setSellerType(sellerType);
        }

        // Set ProductTypes
        if (sellerDTO.getProductTypeIds() != null && !sellerDTO.getProductTypeIds().isEmpty()) {
            List<ProductTypeMaster> productTypes = productTypeMasterRepository.findAllById(sellerDTO.getProductTypeIds());
            if (productTypes.size() != sellerDTO.getProductTypeIds().size()) {
                throw new NotFoundException("One or more product types not found");
            }
            seller.setProductTypes(productTypes);
        }

        // Save Seller first (required for relationships)
        Seller savedSeller = sellerRepository.save(seller);

        // Save Address (OneToOne relationship)
        if (sellerDTO.getAddress() != null) {
            SellerAddress address = SellerAddressMapper.toEntity(sellerDTO.getAddress());
            address.setSeller(savedSeller);
            address.setState(stateMasterRepository.findById(sellerDTO.getAddress().getStateId())
                    .orElseThrow(() -> new NotFoundException("State not found")));
            address.setDistrict(districtMasterRepository.findById(sellerDTO.getAddress().getDistrictId())
                    .orElseThrow(() -> new NotFoundException("District not found")));
            address.setTaluka(talukaMasterRepository.findById(sellerDTO.getAddress().getTalukaId())
                    .orElseThrow(() -> new NotFoundException("Taluka not found")));
            address.setCreatedBy("ADMIN");
            address.setUpdatedBy("ADMIN");
            savedSeller.setAddress(address);
        }

        // Save Coordinator (OneToOne relationship)
        if (sellerDTO.getCoordinator() != null) {
            SellerCoordinator coordinator = SellerCoordinatorMapper.toEntity(sellerDTO.getCoordinator());
            coordinator.setSeller(savedSeller);
            coordinator.setCreatedBy("ADMIN");
            coordinator.setUpdatedBy("ADMIN");
            savedSeller.setCoordinator(coordinator);
        }

        // Save Bank Details (OneToOne relationship)
        if (sellerDTO.getBankDetails() != null) {
            SellerBankDetails bankDetails = SellerBankDetailsMapper.toEntity(sellerDTO.getBankDetails());
            bankDetails.setSeller(savedSeller);
            bankDetails.setCreatedBy("ADMIN");
            bankDetails.setUpdatedBy("ADMIN");
            savedSeller.setBankDetails(bankDetails);
        }

        // Save GST (OneToOne relationship)
        if (sellerDTO.getSellerGST() != null) {
            SellerGST sellerGST = SellerGSTMapper.toEntity(sellerDTO.getSellerGST());
            sellerGST.setSeller(savedSeller);
            savedSeller.setSellerGST(sellerGST);
        }

        // Save Documents (OneToMany relationship)
        if (sellerDTO.getDocuments() != null && !sellerDTO.getDocuments().isEmpty()) {
            List<SellerDocument> documents = new ArrayList<>();
            for (SellerDocumentDto docDto : sellerDTO.getDocuments()) {
                SellerDocument document = SellerDocumentMapper.toEntity(docDto);
                document.setSeller(savedSeller);
                document.setCreatedBy("ADMIN");
                document.setUpdatedBy("ADMIN");

                // Set ProductType for document
                if (docDto.getProductTypeId() != null) {
                    ProductTypeMaster productType = productTypeMasterRepository.findById(docDto.getProductTypeId())
                            .orElseThrow(() -> new NotFoundException("Product type not found with id: " + docDto.getProductTypeId()));
                    document.setProductTypes(productType);
                }
                documents.add(document);
            }
            savedSeller.setDocuments(documents);
        }

        // Save with all relationships (cascade will handle child entities)
        Seller finalSeller = sellerRepository.save(savedSeller);
        return SellerMapper.toDto(finalSeller);
    }

    @Override
    public void deleteBySellerId(String sellerId) {
        sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found with id: " + sellerId));
        sellerRepository.deleteById(sellerId);
    }

    @Override
    @Transactional
    public SellerDTO updateSeller(String sellerId, SellerDTO sellerDTO) {
        // Find existing seller
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found with id: " + sellerId));

        // Update basic fields
        seller.setSellerName(sellerDTO.getSellerName());
        seller.setPhone(sellerDTO.getPhone());
        seller.setPhoneVerified(sellerDTO.isPhoneVerified());
        seller.setEmail(sellerDTO.getEmail());
        seller.setEmailVerified(sellerDTO.isEmailVerified());
        seller.setWebsite(sellerDTO.getWebsite());
        seller.setStatus(sellerDTO.getStatus());
        seller.setTermsAccepted(sellerDTO.isTermsAccepted());

        // Update CompanyType
        if (sellerDTO.getCompanyTypeId() != null) {
            CompanyTypeMaster companyType = companyTypeMasterRepository.findById(sellerDTO.getCompanyTypeId())
                    .orElseThrow(() -> new NotFoundException("Company type not found with id: " + sellerDTO.getCompanyTypeId()));
            seller.setCompanyType(companyType);
        }

        // Update SellerType
        if (sellerDTO.getSellerTypeId() != null) {
            SellerTypeMaster sellerType = sellerTypeMasterRepository.findById(sellerDTO.getSellerTypeId())
                    .orElseThrow(() -> new NotFoundException("Seller type not found with id: " + sellerDTO.getSellerTypeId()));
            seller.setSellerType(sellerType);
        }

        // Update ProductTypes
        if (sellerDTO.getProductTypeIds() != null) {
            List<ProductTypeMaster> productTypes = productTypeMasterRepository.findAllById(sellerDTO.getProductTypeIds());
            if (productTypes.size() != sellerDTO.getProductTypeIds().size()) {
                throw new NotFoundException("One or more product types not found");
            }
            seller.setProductTypes(productTypes);
        }

        // Update Address
        if (sellerDTO.getAddress() != null) {
            if (seller.getAddress() != null) {
                // Update existing address
                SellerAddress existingAddress = seller.getAddress();
                SellerAddress updatedAddress = SellerAddressMapper.toEntity(sellerDTO.getAddress());

                existingAddress.setCity(updatedAddress.getCity());
                existingAddress.setStreet(updatedAddress.getStreet());
                existingAddress.setBuildingNo(updatedAddress.getBuildingNo());
                existingAddress.setLandmark(updatedAddress.getLandmark());
                existingAddress.setPinCode(updatedAddress.getPinCode());
                // Note: State, District, Taluka need to be set from IDs if changed
            } else {
                // Create new address
                SellerAddress address = SellerAddressMapper.toEntity(sellerDTO.getAddress());
                address.setSeller(seller);
                seller.setAddress(address);
            }
        }

        // Update Coordinator
        if (sellerDTO.getCoordinator() != null) {
            if (seller.getCoordinator() != null) {
                // Update existing coordinator
                SellerCoordinator existingCoordinator = seller.getCoordinator();
                SellerCoordinator updatedCoordinator = SellerCoordinatorMapper.toEntity(sellerDTO.getCoordinator());

                existingCoordinator.setName(updatedCoordinator.getName());
                existingCoordinator.setDesignation(updatedCoordinator.getDesignation());
                existingCoordinator.setEmail(updatedCoordinator.getEmail());
                existingCoordinator.setEmailVerified(updatedCoordinator.isEmailVerified());
                existingCoordinator.setMobile(updatedCoordinator.getMobile());
                existingCoordinator.setPhoneVerified(updatedCoordinator.isPhoneVerified());
            } else {
                // Create new coordinator
                SellerCoordinator coordinator = SellerCoordinatorMapper.toEntity(sellerDTO.getCoordinator());
                coordinator.setSeller(seller);
                seller.setCoordinator(coordinator);
            }
        }

        // Update Bank Details
        if (sellerDTO.getBankDetails() != null) {
            if (seller.getBankDetails() != null) {
                // Update existing bank details
                SellerBankDetails existingBankDetails = seller.getBankDetails();
                SellerBankDetails updatedBankDetails = SellerBankDetailsMapper.toEntity(sellerDTO.getBankDetails());

                existingBankDetails.setBankName(updatedBankDetails.getBankName());
                existingBankDetails.setBranch(updatedBankDetails.getBranch());
                existingBankDetails.setIfscCode(updatedBankDetails.getIfscCode());
                existingBankDetails.setAccountNumber(updatedBankDetails.getAccountNumber());
                existingBankDetails.setAccountHolderName(updatedBankDetails.getAccountHolderName());
                existingBankDetails.setBankDocumentFileUrl(updatedBankDetails.getBankDocumentFileUrl());
            } else {
                // Create new bank details
                SellerBankDetails bankDetails = SellerBankDetailsMapper.toEntity(sellerDTO.getBankDetails());
                bankDetails.setSeller(seller);
                seller.setBankDetails(bankDetails);
            }
        }

        // Update GST
        if (sellerDTO.getSellerGST() != null) {
            if (seller.getSellerGST() != null) {
                // Update existing GST
                SellerGST existingGST = seller.getSellerGST();
                SellerGST updatedGST = SellerGSTMapper.toEntity(sellerDTO.getSellerGST());

                existingGST.setGstNumber(updatedGST.getGstNumber());
                existingGST.setGstFileUrl(updatedGST.getGstFileUrl());
                existingGST.setGstVerified(updatedGST.isGstVerified());
            } else {
                // Create new GST
                SellerGST sellerGST = SellerGSTMapper.toEntity(sellerDTO.getSellerGST());
                sellerGST.setSeller(seller);
                seller.setSellerGST(sellerGST);
            }
        }

        // Update Documents - clear and recreate
        if (sellerDTO.getDocuments() != null) {
            // Clear existing documents (orphanRemoval will delete them)
            seller.getDocuments().clear();

            // Add new documents
            for (var docDto : sellerDTO.getDocuments()) {
                SellerDocument document = SellerDocumentMapper.toEntity(docDto);
                document.setSeller(seller);

                // Set ProductType for document
                if (docDto.getProductTypeId() != null) {
                    ProductTypeMaster productType = productTypeMasterRepository.findById(docDto.getProductTypeId())
                            .orElseThrow(() -> new NotFoundException("Product type not found with id: " + docDto.getProductTypeId()));
                    document.setProductTypes(productType);
                }
                seller.getDocuments().add(document);
            }
        }

        // Save updated seller (cascade will handle child entities)
        Seller updatedSeller = sellerRepository.save(seller);
        return SellerMapper.toDto(updatedSeller);
    }
}
