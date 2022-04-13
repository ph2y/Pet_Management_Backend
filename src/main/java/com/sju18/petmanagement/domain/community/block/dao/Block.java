package com.sju18.petmanagement.domain.community.block.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import lombok.*;

import javax.persistence.*;

@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"blocker_id", "blocked_id"}
                )
        }
)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // blocker: 차단을 한 주체
    @ManyToOne
    @JoinColumn(name = "blocker_id")
    Account blocker;

    // blocked: 차단을 당한 객체
    @ManyToOne
    @JoinColumn(name = "blocked_id")
    Account blocked;
}
