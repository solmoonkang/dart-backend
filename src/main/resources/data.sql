-- 1. 회원 생성
INSERT INTO tbl_member (id, email, nickname, password, profile_image_url, birthday, introduce, oauth_provider,
                        created_at)
VALUES (1, 'thfans0521@naver.com', 'solmoon', '{bcrypt}encoded_password_here', NULL, '2000-01-01', '테스트 유저입니다', 'NONE',
        now());


-- 2. 전시회 생성 (Gallery)
INSERT INTO tbl_gallery (id, title, content, thumbnail, start_date, end_date, cost, template, fee, generated_cost,
                         is_paid, re_exhibition_request_count, address, meber_id, created_at)
VALUES (1, 'JMeter 테스트 전시', '성능 측정을 위한 전시 설명입니다.', 'https://example.com/sample-thumbnail.jpg',
        '2025-04-08 00:00:00', '2025-04-30 23:59:59', 'FREE', 'ONE', 0, 0, false, 0, '서울시 강남구 어딘가', 1, now());


-- 3. 채팅방 생성 (ChatRoom)
INSERT INTO tbl_chat_room (id, title, gallery_id, created_at)
VALUES (1, 'JMeter 테스트 채팅방', 1, now());
