-- 1. 회원 생성
INSERT INTO tbl_member (id, email, nickname, password, profile_image_url, birthday, introduce, oauth_provider,
                        created_at)
VALUES (1, 'thfans0521@naver.com', 'solmoon', '$2a$10$Xz3jSgZkK8m7eW8a8rNlSeE.encoded-password', NULL, '1998-05-21',
        '안녕하세요, 전시회를 좋아하는 개발자 강솔문입니다.', 'NONE', now());


-- 2. 전시회 생성 (Gallery)
INSERT INTO tbl_gallery (id, title, content, thumbnail, start_date, end_date, cost, template, fee, generated_cost,
                         is_paid, re_exhibition_request_count, address, meber_id, created_at)
VALUES (1, 'JMeter 테스트 전시', '성능 측정을 위한 전시 설명입니다.', 'https://example.com/sample-thumbnail.jpg',
        '2025-04-10 10:00:00', '2025-04-30 18:00:00', 'FREE', 'ONE', 0, 0, false, 0, '서울특별시 마포구 동교로 23길 10, 홍대 전시관 1층',
        1, now());


-- 3. 채팅방 생성 (ChatRoom)
INSERT INTO tbl_chat_room (id, title, gallery_id, created_at)
VALUES (1, 'JMeter 테스트 채팅방', 1, now());
