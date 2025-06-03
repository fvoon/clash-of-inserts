package com.moneylion.fvoon.clashofinserts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.ObjectUtils;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "fundOptions")
public class BillingAddress extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "addr_line1", nullable = false)
    private String addrLine1;

    @Column(name = "addr_line2")
    private String addrLine2;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "hash", nullable = false)
    private String hash;

    @Column(name = "address_id")
    private String addressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_source", nullable = false)
    private AddressSource addressSource;

    public enum AddressSource {
        USERAPI,
        USER,
        WALLET
    }

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @JsonIgnore
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "billingAddress", cascade = CascadeType.ALL)
    private List<FundOption> fundOptions = new ArrayList<>();

    @Builder(builderMethodName = "billingAddressBuilder")
    public BillingAddress(String userId,
                          String addrLine1,
                          String addrLine2,
                          String city,
                          String postalCode,
                          String state,
                          String hash,
                          String addressId,
                          AddressSource addressSource,
                          boolean deleted,
                          List<FundOption> fundOptions,
                          Date createdAt,
                          Date updatedAt) {
        this.userId = userId;
        this.addrLine1 = addrLine1;
        this.addrLine2 = addrLine2;
        this.city = city;
        this.postalCode = postalCode;
        this.state = state;
        this.hash = hash;
        this.addressId = addressId;
        this.addressSource = addressSource;
        this.deleted = deleted;
        this.fundOptions = fundOptions;
        this.createdAt = ObjectUtils.isEmpty(createdAt) ? new Date() : createdAt;
        this.updatedAt = ObjectUtils.isEmpty(updatedAt) ? new Date() : updatedAt;
    }

    public BillingAddressDTO toDto() {
        return BillingAddressDTO.builder()
                .billingAddressId(this.id)
                .addrLine1(this.addrLine1)
                .addrLine2(this.addrLine2)
                .city(this.city)
                .postalCode(this.postalCode)
                .state(this.state)
                .build();
    }

    public boolean validateFields() {
        if (isEmpty(this.userId)) {
            log.error("BillingAddress validation failed: Missing userId");
            return false;
        }
        if (isEmpty(this.addrLine1)) {
            log.error("BillingAddress validation failed: Missing addrLine1");
            return false;
        }
        if (isEmpty(this.city)) {
            log.error("BillingAddress validation failed: Missing city");
            return false;
        }
        if (isEmpty(this.postalCode)) {
            log.error("BillingAddress validation failed: Missing postalCode");
            return false;
        }
        if (isEmpty(this.state)) {
            log.error("BillingAddress validation failed: Missing state");
            return false;
        }
        if (isEmpty(this.hash)) {
            log.error("BillingAddress validation failed: Missing hash");
            return false;
        }
        if (AddressSource.USER.equals(this.addressSource) && isEmpty(this.addressId)) {
            log.error("BillingAddress validation failed: Missing addressId");
            return false;
        }

        return true;
    }

    public boolean matches(BillingAddress address) {
        String otherAddressFingerPrint = Optional.ofNullable(address)
                .map(a -> generateFingerPrint(a.addrLine1, a.addrLine2, a.city, a.postalCode, a.state))
                .orElse("");
        return this.getFingerPrint().equals(otherAddressFingerPrint);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof BillingAddress) {
            return Optional.ofNullable(object)
                    .map(obj -> (BillingAddress) obj)
                    .map(billingAddress -> this.hash.equals(billingAddress.hash))
                    .orElse(false);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.hash);
    }

    public static String getBillingAddressHash(String addrLine1, String addrLine2, String city, String postalCode, String state) {
        return DigestUtils.sha256Hex(generateFingerPrint(addrLine1, addrLine2, city, postalCode, state));
    }

    @JsonIgnore
    private String getFingerPrint() {
        return generateFingerPrint(this.addrLine1, this.addrLine2, this.city, this.postalCode, this.state);
    }

    private static String generateFingerPrint(String addrLine1, String addrLine2, String city, String postalCode, String state) {
        String rawString = addrLine1 + (isEmpty(addrLine2) ? "" : addrLine2) + city + postalCode + state;
        return rawString
                .replaceAll("\\s+", "")
                .replaceAll("\\.+", "")
                .toLowerCase();
    }
}