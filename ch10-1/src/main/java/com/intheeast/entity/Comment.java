package com.intheeast.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static jakarta.persistence.FetchType.EAGER;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
public class Comment extends BaseEntity{

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="COMMENT_ID")
    private Long id;

    @Lob
    private String text;

    @JsonBackReference
    /*
     <Comment를 단독으로 조회하는 실무 사례>
     1. 관리자 페이지 (Admin Dashboard)
       댓글 관리 메뉴: 특정 포스트와 상관없이 **"최근 달린 댓글 목록"**이나 **"신고된 댓글 목록"**을 한눈에 보여줘야 할 때가 있습니다. 이때는 Comment 테이블을 기준으로 페이징 처리를 하며 조회하게 됩니다.

     2. 마이페이지 (My Page)
       내가 쓴 댓글 보기: 사용자가 자기가 작성한 댓글들을 모아보고 싶을 때입니다. where user_id = :userId 조건으로 Comment 테이블을 직접 쿼리합니다.

     3. 알림 서비스 (Notification)
       누군가 내 글에 댓글을 달면 알림이 가죠? 그 알림을 클릭했을 때 특정 댓글(ID) 하나만 가져와서 보여주거나 수정하는 API가 호출될 수 있습니다.
       
     그러므로 FetchType을 LAZY로 설정
     */
    @ManyToOne(fetch = FetchType.LAZY) // 
    @JoinColumn(name = "POST_ID")
    private Post post;
}
