package com.uijae.cms.domain.model;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
// 엔티티 클래스가 아니지만, 다른 엔티티 클래스에서 공통으로 사용될 수 있는 필드와 메서드를 제공하는 클래스임을 나타낸다.
@MappedSuperclass
// 엔티티에 대한 추가적인 작업을 처리할 수 있게 해준다. JPA Auditing 을 사용하여, 엔티티가 저장 또는 업데이트 될 때 자동으로 날짜를 채워준다.
@EntityListeners(value = {AuditingEntityListener.class})
public class BaseEntity {

    // 엔티티가 처음 저장 될 때, 해당 필드에 자동으로 현재 날짜와 시간이 저장
    @CreatedDate
    private LocalDateTime createdAt;
    // 엔티티가 수정될 때마다 해당 필드에 자동으로 수정된 날짜와 시간 저장
    @LastModifiedDate
    private LocalDateTime modifiedAt;
}
