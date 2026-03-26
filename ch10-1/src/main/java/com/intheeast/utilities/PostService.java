package com.intheeast.utilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intheeast.entity.Comment;
import com.intheeast.entity.Post;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class PostService {


    public static void savePostsFromJson(EntityManagerFactory emf, String jsonFilePath) throws IOException {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();  // 트랜잭션 시작

            // ObjectMapper를 사용해 JSON 파일을 Post 객체 리스트로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            List<Post> posts = objectMapper.readValue(new File(jsonFilePath),
                    new TypeReference<List<com.intheeast.entity.Post>>() {});

            // 각 Post와 Comment를 데이터베이스에 저장
//            for (Post post : posts) {
//                // 댓글 리스트에서 각 댓글을 Post에 추가
//                for (Comment comment : post.getCommentList()) {
//                    post.addComment(comment);  // 댓글과의 연관관계 설정
//                }
//
//                em.persist(post);  // Post 엔티티 저장 (CascadeType.ALL이므로 Comment도 저장됨)
//            }
            
            // 위 for loop 대신에 이 for loop를 사용합니다.
            for (Post post : posts) {

                em.persist(post);  // Post 엔티티 저장 (CascadeType.ALL이므로 Comment도 저장됨)
            }

            tx.commit();  // 트랜잭션 커밋 (DB에 반영)

        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();  // 에러 발생 시 롤백
        } finally {
            em.close();  // EntityManager 종료
        }
    }


}
