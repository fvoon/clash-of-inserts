package com.moneylion.fvoon.clashofinserts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners({AuditingEntityListener.class})
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString(exclude = {"processors", "products", "savedState", "modifiedBy", "fundOptionEsigns"})
@NamedEntityGraph(
        name = "allJoins"
)
public class FundOption extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Column(name = "identifier", nullable = false)
    private String identifier;

    @Column(name = "name")
    private String name;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "type", nullable = false)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private FundDetails details;

    @Column(name = "isDeleted", columnDefinition = "BOOLEAN default false", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "isDisabled", columnDefinition = "BOOLEAN default false", nullable = false)
    private Boolean isDisabled = false;

    @Column(name = "isVerified", columnDefinition = "BOOLEAN default false")
    private Boolean isVerified = false;

    @Column(name = "isAvailable", columnDefinition = "BOOLEAN default true")
    private Boolean isAvailable = true;

    @Column(name = "isMoneylion", columnDefinition = "BOOLEAN default false")
    private Boolean isMoneylion = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "billing_address")
    private BillingAddress billingAddress;

    @Convert(converter = FundStatusConverter.class)
    private FundStatus status;

    @JsonIgnore
    @Transient
    private transient Source modifiedBy;

    public enum Source {
        INTERNAL
    }

    public void setType(FundOptionTypes type) {
        this.type = type.getValue();
    }

    public FundOptionTypes getType() {
        return FundOptionTypes.valueOf(type);
    }
}
