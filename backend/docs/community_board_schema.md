# 커뮤니티(게시판) DB 설계 초안

목표: 게시글/댓글/추천/조회수(카운터 + 중복방지 로그)를 지원하는 최소 스키마.

## 테이블

### `community_posts` (게시글)
- `id` BIGINT PK
- `author_id` BIGINT NOT NULL, FK -> `users.id`
- `title` VARCHAR(200) NOT NULL
- `content` TEXT NOT NULL
- `status` VARCHAR(20) NOT NULL (`ACTIVE`, `DELETED`)  // 게시글/댓글 공통
- `view_count` INT NOT NULL (기본 0)
- `recommend_count` INT NOT NULL (기본 0)
- `comment_count` INT NOT NULL (기본 0)
- `created_at` DATETIME NOT NULL
- `updated_at` DATETIME NOT NULL
- `deleted_at` DATETIME NULL

인덱스 권장
- `idx_community_posts_created_at` (`created_at`)
- `idx_community_posts_author_id_created_at` (`author_id`, `created_at`)
- `idx_community_posts_recommend_count_created_at` (`recommend_count`, `created_at`)
- `idx_community_posts_view_count_created_at` (`view_count`, `created_at`)

### `community_comments` (댓글)
- `id` BIGINT PK
- `post_id` BIGINT NOT NULL, FK -> `community_posts.id`
- `author_id` BIGINT NOT NULL, FK -> `users.id`
- `parent_id` BIGINT NULL, FK -> `community_comments.id` (대댓글)
- `content` TEXT NOT NULL
- `status` VARCHAR(20) NOT NULL (`ACTIVE`, `DELETED`)  // 게시글/댓글 공통
- `created_at` DATETIME NOT NULL
- `updated_at` DATETIME NOT NULL
- `deleted_at` DATETIME NULL

인덱스 권장
- `idx_community_comments_post_id_created_at` (`post_id`, `created_at`)
- `idx_community_comments_parent_id_created_at` (`parent_id`, `created_at`)

### `community_post_recommends` (게시글 추천 - 중복 방지)
유저 1명이 게시글 1개에 추천 1회만 가능하도록 PK를 `(post_id, user_id)`로 구성.

- `post_id` BIGINT NOT NULL, FK -> `community_posts.id`
- `user_id` BIGINT NOT NULL, FK -> `users.id`
- `created_at` DATETIME NOT NULL

제약/인덱스
- PK: (`post_id`, `user_id`)
- `idx_community_post_recommends_user_id_created_at` (`user_id`, `created_at`)

### `community_post_views` (게시글 조회 로그 - 일 단위 중복 방지)
조회수는 `community_posts.view_count`에 저장하고, 중복(예: 같은 유저가 같은 날 여러 번 조회)을 막기 위해 로그 테이블을 둠.

- `post_id` BIGINT NOT NULL, FK -> `community_posts.id`
- `user_id` BIGINT NOT NULL, FK -> `users.id`
- `viewed_date` DATE NOT NULL (중복 방지 키)
- `viewed_at` DATETIME NOT NULL

제약/인덱스
- PK: (`post_id`, `user_id`, `viewed_date`)  // 유저-게시글-일자 단위 1회
- `idx_community_post_views_post_id_viewed_date` (`post_id`, `viewed_date`)

## 카운터 컬럼 운용 가이드
- 추천/조회/댓글 수는 읽기 성능을 위해 게시글 테이블에 `*_count`로 유지.
- 중복 방지/감사 목적은 로그 테이블(`community_post_recommends`, `community_post_views`)로 처리.
- 서비스 레이어에서 트랜잭션으로 "로그 insert 성공 시 카운터 증가" 형태로 맞추면 정합성이 단순해짐.
