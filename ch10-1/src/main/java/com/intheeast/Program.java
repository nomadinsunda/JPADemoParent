package com.intheeast;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import com.intheeast.entity.Comment;
import com.intheeast.entity.Post;
import com.intheeast.entity.QComment;
import com.intheeast.entity.QPost;
import com.intheeast.utilities.PostService;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class Program {
    
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

    public static void main(String[] args) throws IOException {
        try {
            // 1. 데이터 존재 여부 확인 및 로드
            if (!isDataExists()) {
                System.out.println(">>> [INFO] 데이터가 없어 JSON 파일을 로딩합니다.");                
                
                URL resourceUrl = Program.class.getClassLoader().getResource("posts.json");

                if (resourceUrl == null) {
                    System.out.println("파일을 찾을 수 없습니다: src/main/resources/posts.json");
                    return;
                }

                // 3. URL에서 파일 경로(String)를 추출하여 메서드 호출
                String absolutePath = resourceUrl.getPath();
                PostService.savePostsFromJson(emf, absolutePath);
                System.out.println(">>> 데이터 로딩 완료");
            }
            
            System.out.println("\n>>> [STEP 1] Post 전체 조회 시작 (N+1 발생 상황)");
            triggerNPlusOneWithPostAndComment();
            
            System.out.println("\n>>> [STEP 2] QueryDSL Fetch Join 실행 (N+1 해결)");
            solveNPlusOneWithQueryDSL();
            
            System.out.println("\n--- [STEP 3] 전체 Post 단계적 페이지네이션 실행 ---");
            displayAllPostsByPaging(10);

            // 현재 DB에 존재하는 실제 Comment ID 하나 가져오기 (하드코딩 방지)
            Long targetCommentId = getAnyExistingCommentId();

            if (targetCommentId != null) {
                // 단건 조회 테스트 (LAZY 확인)
                System.out.println("\n--- [STEP 4] findCommentById (LAZY 상태 확인) ---");
                Comment comment = findCommentById(targetCommentId);
                System.out.println("댓글 ID: " + comment.getId() + " | 내용: " + comment.getText());
                
                // 여기서 comment.getPost().getTitle()을 호출하면 두 번째 쿼리가 나갑니다 (Lazy Loading)
                System.out.println("연관된 포스트 제목(Lazy): " + comment.getPost().getTitle());
            }

            // Fetch Join을 이용한 최적화 조회 (N+1 해결)
            System.out.println("\n--- [STEP 5] findMyCommentsWithPost (Fetch Join) ---");
            List<Comment> optimizedComments = findMyCommentsWithPost();
            for (Comment c : optimizedComments) {
                // fetchJoin() 덕분에 추가 쿼리 없이 포스트 정보에 접근 가능
                System.out.println("댓글: " + c.getText() + " | 원문 포스트: " + c.getPost().getTitle());
            }

            // 키워드 검색 페이징 테스트
            System.out.println("\n--- [STEP 6] findCommentsByKeyword (Paging) ---");
            String targetText = "Very"; // "Great", "Really" ...
            List<Comment> results = findCommentsByKeyword(targetText, 0, 3);
            System.out.println("검색 결과 수: " + results.size());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (emf != null && emf.isOpen()) emf.close();
        }
    }
    
	// Post :  @OneToMany(fetch = FetchType.LAZY)
	// Comment : @ManyToOne(fetch = FetchType.LAZY)
	private static void triggerNPlusOneWithPostAndComment() {
	    EntityManager em = emf.createEntityManager();
	    try {
	        System.out.println("================ 모든 Post 조회 (1번 쿼리 발생) ================");
	        
	        // 1. 첫 번째 쿼리 실행 (1 발생)
	        List<Post> posts = em.createQuery("select p from Post p", Post.class)
	                             .getResultList();
	        
	        int nPlusOneCount = 0; // N번 발생 횟수를 저장할 변수

	        System.out.println("--- 연관된 Comment 조회 시작 ---");

	        for (Post post : posts) {
	            // FetchType.LAZY로 인해 아래 메서드 호출 시 쿼리가 실행됩니다.
	            // 쿼리 실행 직전에 카운트를 올립니다.
	            nPlusOneCount++; 
	            
	            int commentCount = post.getCommentList().size(); 
	            System.out.println("포스트 제목: " + post.getTitle() + ", 댓글 수: " + commentCount);
	        }
	        
	        System.out.println("================ 조회 종료 ================");
	        System.out.println(">>> 총 실행된 쿼리 횟수: 1 (Post 조회) + " + nPlusOneCount + " (Comment 조회) = " + (1 + nPlusOneCount));
	        System.out.println(">>> N+1 문제 발생 횟수(N): " + nPlusOneCount + "번");
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
	}
	
	// Post :  @OneToMany(fetch = FetchType.LAZY)
	// Comment : @ManyToOne(fetch = FetchType.LAZY)
	private static void solveNPlusOneWithQueryDSL() {
	    EntityManager em = emf.createEntityManager();
	    // QueryDSL 쿼리 작성을 위한 Factory 생성
	    JPAQueryFactory queryFactory = new JPAQueryFactory(em);

	    try {
	        System.out.println("================ QueryDSL Fetch Join 실행 (쿼리 1번 발생) ================");
	        
	        QPost post = QPost.post;
	        QComment comment = QComment.comment;
	        
	        /*
	         * SELECT p FROM Post p JOIN FETCH p.commentList 와 동일한 동작
	         * .fetchJoin()을 호출하는 것이 핵심입니다.
	         */
	        List<Post> posts = queryFactory
	                .selectFrom(post)
	                .leftJoin(post.commentList, comment).fetchJoin() // 연관된 엔티티를 한 번에 Join해서 가져옴
	                .distinct() // 1:N 조인 시 발생하는 중복 데이터 제거 (엔티티 수준)
	                .fetch();

	        System.out.println("--- 연관된 Comment 접근 (추가 쿼리 발생하지 않음) ---");

	        int nPlusOneCount = 0; 
	        for (Post p : posts) {
	            // 이미 Fetch Join으로 데이터를 다 가져왔으므로, 
	            // 아래 호출 시 DB에 다시 쿼리를 날리지 않고 1차 캐시(메모리)에서 꺼내옵니다.
	            int commentCount = p.getCommentList().size();
	            System.out.println("포스트 제목: " + p.getTitle() + ", 댓글 수: " + commentCount);
	        }
	        
	        System.out.println("================ 조회 종료 ================");
	        System.out.println(">>> 총 실행된 쿼리 횟수: 1 (Join 쿼리 1번으로 끝)");
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
	}
	
	/**
     * 전체 데이터를 페이지 단위로 끊어서 순차적으로 모두 출력하는 메서드
     */
    private static void displayAllPostsByPaging(int pageSize) {
        EntityManager em = emf.createEntityManager();
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QPost post = QPost.post;

        try {
            // 1. 전체 데이터 개수 조회
            Long totalCount = queryFactory
                    .select(post.count())
                    .from(post)
                    .fetchOne();

            if (totalCount == null || totalCount == 0) {
                System.out.println("조회할 데이터가 없습니다.");
                return;
            }

            // 2. 전체 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            System.out.println("\n[Pagination Summary]");
            System.out.println("총 데이터 개수: " + totalCount);
            System.out.println("한 페이지당 개수: " + pageSize);
            System.out.println("총 페이지 수: " + totalPages);
            System.out.println("============================================");

            // 3. 루프를 돌며 단계적으로 모든 페이지 조회
            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                int offset = (pageNum - 1) * pageSize;

                List<Post> content = queryFactory
                        .selectFrom(post)
                        .orderBy(post.id.asc()) // ID 순으로 정렬
                        .offset(offset)
                        .limit(pageSize)
                        .fetch();

                System.out.println("\n>>> 현재 페이지: " + pageNum + " / " + totalPages);
                for (Post p : content) {
                    System.out.println("  [ID: " + p.getId() + "] " + p.getTitle());
                }
                System.out.println("--------------------------------------------");
            }

            System.out.println("모든 페이지 출력이 완료되었습니다.");

        } finally {
            em.close();
        }
    }

    // --- DB에서 실제 존재하는 아무 ID나 하나 가져오는 메서드 ---
    private static Long getAnyExistingCommentId() {
        EntityManager em = emf.createEntityManager();
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QComment comment = QComment.comment;
        try {
            return queryFactory.select(comment.id).from(comment).limit(1).fetchOne();
        } finally {
            em.close();
        }
    }

    // --- 데이터 존재 체크 ---
    private static boolean isDataExists() {
        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery("select count(p) from Post p", Long.class).getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    // --- [ID 기반 단건 조회] ---
    private static Comment findCommentById(Long commentId) {
        EntityManager em = emf.createEntityManager();
        try {
            JPAQueryFactory queryFactory = new JPAQueryFactory(em);
            QComment comment = QComment.comment;

            Comment result = queryFactory.selectFrom(comment)
                    .where(comment.id.eq(commentId))
                    .fetchOne();

            if (result != null) {
                // [강제 초기화] 세션이 닫히기 전에 프록시를 실제 객체로 바꿈
                result.getPost().getTitle(); 
            }
            return result;
        } finally {
            em.close(); // 여기서 세션이 닫힘
        }
    }

    // --- [Fetch Join 최적화 조회] ---
    private static List<Comment> findMyCommentsWithPost() {
        EntityManager em = emf.createEntityManager();
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QComment comment = QComment.comment;
        QPost post = QPost.post;
        try {
            return queryFactory.selectFrom(comment)
                    .join(comment.post, post).fetchJoin() // 이 한 줄이 N+1을 막습니다.
                    .fetch();
        } finally {
            em.close();
        }
    }

    // --- [동적 검색 및 페이징] ---
    private static List<Comment> findCommentsByKeyword(String keyword, int offset, int limit) {
        EntityManager em = emf.createEntityManager();
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QComment comment = QComment.comment;
        try {
            return queryFactory.selectFrom(comment)
                    .where(comment.text.contains(keyword))
                    .orderBy(comment.id.desc())
                    .offset(offset)
                    .limit(limit)
                    .fetch();
        } finally {
            em.close();
        }
    }
    
    
    private static void updateFirstPostTitle(String newTitle) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QPost post = QPost.post;

        try {
            tx.begin();
            // 1. 영속성 컨텍스트에 엔티티 로드
            Post firstPost = queryFactory.selectFrom(post).orderBy(post.id.asc()).limit(1).fetchOne();
            
            if (firstPost != null) {
                // 2. 엔티티 값 변경 (em.update 같은 메서드 필요 없음)
                firstPost.setTitle(newTitle);
                System.out.println("ID " + firstPost.getId() + "번 제목 변경 완료.");
            }
            tx.commit(); // 이때 변경 사항이 DB에 반영됨
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }    

    // --- [리팩토링] 검색 + 페이지네이션 통합 ---
    private static List<Comment> findCommentsByKeywordWithPaging(String keyword, int page, int size) {
        EntityManager em = emf.createEntityManager();
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QComment comment = QComment.comment;
        try {
            return queryFactory.selectFrom(comment)
                    .where(comment.text.contains(keyword))
                    .orderBy(comment.id.desc())
                    .offset((page - 1) * size)
                    .limit(size)
                    .fetch();
        } finally {
            em.close();
        }
    }
    
    // --- [추가] 벌크 삭제: 성능 최적화 ---
    private static void deleteAllPostsBulk() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QPost post = QPost.post;
        QComment comment = QComment.comment;

        try {
            tx.begin();
            // 자식 테이블(Comment) 먼저 벌크 삭제 후 부모 삭제
            queryFactory.delete(comment).execute();
            queryFactory.delete(post).execute();
            tx.commit();
            System.out.println("모든 데이터가 벌크 삭제되었습니다.");
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}

