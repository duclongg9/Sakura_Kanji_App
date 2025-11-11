CREATE DATABASE IF NOT EXISTS kanji_app
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kanji_app;

-- Roles
DROP TABLE IF EXISTS Role;
CREATE TABLE Role (
  id        TINYINT     NOT NULL PRIMARY KEY,          -- 1=Admin, 2=User, 3=VIP
  roleName  VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB;

INSERT INTO Role (id, roleName) VALUES
  (1,'ADMIN'), (2,'USER'), (3,'VIP')
ON DUPLICATE KEY UPDATE roleName = VALUES(roleName);

-- JLPTLevel
DROP TABLE IF EXISTS JLPTLevel;
CREATE TABLE JLPTLevel (
  id         INT         NOT NULL AUTO_INCREMENT,
  nameLevel  VARCHAR(20) NOT NULL,                     -- "N5".."N1"
  PRIMARY KEY (id),
  UNIQUE KEY uq_jlpt_name (nameLevel)
) ENGINE=InnoDB;

INSERT INTO JLPTLevel (nameLevel) VALUES ('N5'),('N4'),('N3'),('N2'),('N1')
ON DUPLICATE KEY UPDATE nameLevel = VALUES(nameLevel);

-- User (dùng backtick để tránh đụng hàm USER())
DROP TABLE IF EXISTS `User`;
CREATE TABLE `User` (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  userName       VARCHAR(120)  NOT NULL,
  email          VARCHAR(190)  NOT NULL,
  imgUrl         VARCHAR(255)  NULL,
  matKhau        VARCHAR(255)  NULL,
  roleId         TINYINT       NOT NULL DEFAULT 2,          -- 1/2/3
  accountTier    ENUM('FREE','VIP','ADMIN') NOT NULL DEFAULT 'FREE',
  vipExpiresAt   DATETIME      NULL,
  accountBalance DECIMAL(12,2) NOT NULL DEFAULT 0,
  bio            TEXT          NULL,
  createdAt      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_user_email (email),
  KEY idx_user_role (roleId),
  CONSTRAINT fk_user_role FOREIGN KEY (roleId) REFERENCES Role(id)
) ENGINE=InnoDB;

-- Seed user mẫu để test đăng nhập local
INSERT INTO `User` (userName, email, matKhau, roleId, accountTier, accountBalance, bio, imgUrl)
VALUES ('admin', 'admin@example.com', '123456', 1, 'ADMIN', 250000, 'Admin giàu kinh nghiệm, hỗ trợ cộng đồng học Kanji. Tài khoản có quyền quản trị đầy đủ.', 'https://i.pravatar.cc/256?img=12')
ON DUPLICATE KEY UPDATE
  userName = VALUES(userName),
  matKhau = VALUES(matKhau),
  roleId = VALUES(roleId),
  accountTier = VALUES(accountTier),
  accountBalance = VALUES(accountBalance),
  bio = VALUES(bio),
  imgUrl = VALUES(imgUrl);

INSERT INTO `User` (userName, email, matKhau, roleId, accountTier, accountBalance, bio, imgUrl)
VALUES
  ('vip_hana', 'hana.vip@example.com', '123456', 3, 'VIP', 650000, 'Hana là thành viên VIP đã học Kanji 5 năm, thường chia sẻ mẹo ghi nhớ siêu tốc.', 'https://i.pravatar.cc/256?img=32'),
  ('minh_hocvien', 'minh.student@example.com', '123456', 2, 'FREE', 120000, 'Minh là sinh viên yêu tiếng Nhật, đang tiết kiệm để nâng cấp VIP cho mùa thi JLPT.', 'https://i.pravatar.cc/256?img=47'),
  ('thu_travel', 'thu.travel@example.com', '123456', 2, 'FREE', 45000, 'Thu mê khám phá Nhật Bản, tập trung vào các chủ đề du lịch và giao tiếp.', 'https://i.pravatar.cc/256?img=15')
ON DUPLICATE KEY UPDATE
  userName = VALUES(userName),
  matKhau = VALUES(matKhau),
  roleId = VALUES(roleId),
  accountTier = VALUES(accountTier),
  accountBalance = VALUES(accountBalance),
  bio = VALUES(bio),
  imgUrl = VALUES(imgUrl);

-- Level
DROP TABLE IF EXISTS Level;
CREATE TABLE Level (
  id           INT            NOT NULL AUTO_INCREMENT,
  name         VARCHAR(100)   NOT NULL,                   -- ví dụ: "Cơ bản"
  jlptLevelId  INT            NOT NULL,                   -- FK -> JLPTLevel
  description  TEXT           NULL,
  isActive     BOOLEAN        NOT NULL DEFAULT TRUE,      -- bật/tắt level
  accessTier   ENUM('FREE','PAID') NOT NULL DEFAULT 'FREE',  -- FREE: ai cũng xem; PAID: VIP/Admin
  createdAt    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_level_name (name),
  KEY idx_level_jlpt (jlptLevelId),
  CONSTRAINT fk_level_jlpt FOREIGN KEY (jlptLevelId) REFERENCES JLPTLevel(id)
) ENGINE=InnoDB;

INSERT INTO Level (name, jlptLevelId, description, isActive, accessTier)
VALUES
  ('Cơ bản 1', 1, 'Những chữ Kanji đơn giản nhất cho người mới bắt đầu.', TRUE, 'FREE'),
  ('Cơ bản 2', 1, 'Củng cố từ vựng N5 thông dụng.', TRUE, 'FREE'),
  ('Tăng tốc N4', 2, 'Chữ Kanji thường gặp trong N4.', TRUE, 'FREE'),
  ('N4 luyện tập', 2, 'Chương trình N4 nâng cao với ví dụ hội thoại đời sống.', TRUE, 'PAID'),
  ('Khởi động N3', 3, 'Lộ trình nhập môn JLPT N3 với chủ đề công việc và giao tiếp.', TRUE, 'PAID'),
  ('Chữ ghép N3 nâng cao', 3, 'Khai phá các chữ ghép khó nhớ cùng mẹo ghi nhớ sáng tạo.', TRUE, 'PAID'),
  ('Tinh luyện N2', 4, 'Kho Kanji nâng cao dùng trong môi trường doanh nghiệp và báo chí.', TRUE, 'PAID'),
  ('Đỉnh cao N1', 5, 'Tuyển tập Kanji học thuật giúp sẵn sàng cho chứng chỉ N1.', TRUE, 'PAID')
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  isActive = VALUES(isActive),
  accessTier = VALUES(accessTier);

-- Kanji
DROP TABLE IF EXISTS Kanji;
CREATE TABLE Kanji (
  id        BIGINT       NOT NULL AUTO_INCREMENT,
  kanji     CHAR(1)      NOT NULL,
  hanViet   VARCHAR(100) NULL,
  amOn      VARCHAR(255) NULL,
  amKun     VARCHAR(255) NULL,
  moTa      TEXT         NULL,
  levelId   INT          NULL,                            -- gắn vào Level
  createdAt TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_kanji_char (kanji),
  KEY idx_kanji_level (levelId),
  CONSTRAINT fk_kanji_level FOREIGN KEY (levelId) REFERENCES Level(id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

INSERT INTO Kanji (kanji, hanViet, amOn, amKun, moTa, levelId) VALUES
  ('日', 'nhật', 'ニチ', 'ひ, び', 'Mặt trời, ngày', 1),
  ('月', 'nguyệt', 'ゲツ, ガツ', 'つき', 'Mặt trăng, tháng', 1),
  ('山', 'sơn', 'サン', 'やま', 'Núi cao', 1),
  ('川', 'xuyên', 'セン', 'かわ', 'Dòng sông', 1),
  ('田', 'điền', 'デン', 'た', 'Cánh đồng, ruộng lúa', 1),
  ('木', 'mộc', 'ボク, モク', 'き, こ', 'Cây cối, gỗ', 1),
  ('本', 'bản', 'ホン', 'もと', 'Sách, gốc rễ', 2),
  ('学', 'học', 'ガク', 'まな.ぶ', 'Học tập', 2),
  ('行', 'hành', 'コウ, ギョウ', 'い.く, おこな.う', 'Đi lại, thực hiện', 2),
  ('食', 'thực', 'ショク, ジキ', 'た.べる, く.う', 'Ăn uống, bữa ăn', 2),
  ('話', 'thoại', 'ワ', 'はな.す, はなし', 'Trò chuyện, câu chuyện', 3),
  ('高', 'cao', 'コウ', 'たか.い', 'Cao, đắt', 3),
  ('校', 'giáo', 'コウ', 'かま', 'Trường học', 3),
  ('旅', 'lữ', 'リョ', 'たび', 'Chuyến đi, du lịch', 4),
  ('業', 'nghiệp', 'ギョウ', 'わざ', 'Công việc, sự nghiệp', 4),
  ('練', 'luyện', 'レン', 'ね.る', 'Rèn luyện, thực hành', 4),
  ('経', 'kinh', 'ケイ, キョウ', 'へ.る', 'Kinh nghiệm, trải qua', 5),
  ('験', 'nghiệm', 'ケン, ゲン', 'ため.す', 'Thử nghiệm, kiểm tra', 5),
  ('想', 'tưởng', 'ソウ, ソ', 'おも.う', 'Tưởng tượng, suy nghĩ', 5),
  ('海', 'hải', 'カイ', 'うみ', 'Biển cả, sóng nước', (SELECT id FROM Level WHERE name = 'Cơ bản 2')),
  ('森', 'sâm', 'シン', 'もり', 'Khu rừng rậm rạp', (SELECT id FROM Level WHERE name = 'Cơ bản 2')),
  ('働', 'động', 'ドウ', 'はたら.く', 'Lao động, làm việc', (SELECT id FROM Level WHERE name = 'Tăng tốc N4')),
  ('準', 'chuẩn', 'ジュン', 'じゅん.び', 'Chuẩn bị, sẵn sàng', (SELECT id FROM Level WHERE name = 'Chữ ghép N3 nâng cao')),
  ('護', 'hộ', 'ゴ', 'まも.る', 'Bảo vệ, che chở', (SELECT id FROM Level WHERE name = 'Tinh luyện N2')),
  ('響', 'hưởng', 'キョウ', 'ひび.く', 'Ảnh hưởng, vang vọng', (SELECT id FROM Level WHERE name = 'Đỉnh cao N1'))
ON DUPLICATE KEY UPDATE
  hanViet = VALUES(hanViet),
  amOn = VALUES(amOn),
  amKun = VALUES(amKun),
  moTa = VALUES(moTa),
  levelId = VALUES(levelId);

-- Lesson
DROP TABLE IF EXISTS lessons;
CREATE TABLE lessons (
    lesson_id   BIGINT       NOT NULL AUTO_INCREMENT,
    level_id    INT          NOT NULL,
    title       VARCHAR(150) NOT NULL,
    overview    TEXT         NULL,
    order_index INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (lesson_id),
    KEY idx_lesson_level (level_id),
    CONSTRAINT fk_lesson_level FOREIGN KEY (level_id) REFERENCES Level(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO lessons (level_id, title, overview, order_index) VALUES
  (1, 'Ngày và đêm', 'Làm quen với 日 và 月 qua ví dụ đơn giản.', 1),
  (1, 'Thiên nhiên quanh ta', 'Núi và sông trong Kanji.', 2),
  (1, 'Cuộc sống nông thôn', 'Các chữ Kanji chỉ đồng ruộng và cây cối.', 3),
  (2, 'Sinh hoạt hằng ngày', 'Từ vựng liên quan đến học tập.', 1),
  (2, 'Ẩm thực và giao tiếp', 'Thực hành 食 và 話 trong ngữ cảnh đời sống.', 2),
  (3, 'Trường lớp N4', 'Bài học mở rộng cho N4.', 1),
  (4, 'Du lịch Nhật Bản', 'Từ vựng chủ đề di chuyển và dịch vụ du lịch.', 1),
  (5, 'N3 nghề nghiệp', 'Thuật ngữ công việc và suy nghĩ chuyên sâu.', 1),
  ((SELECT id FROM Level WHERE name = 'Chữ ghép N3 nâng cao'), 'Chữ ghép thực dụng', 'Áp dụng các chữ ghép trong môi trường làm việc.', 1),
  ((SELECT id FROM Level WHERE name = 'Tinh luyện N2'), 'Tin tức buổi sáng', 'Đọc hiểu Kanji báo chí và tin tức kinh tế.', 1),
  ((SELECT id FROM Level WHERE name = 'Tinh luyện N2'), 'Hội thoại doanh nghiệp', 'Từ vựng giao tiếp trong môi trường công sở Nhật.', 2),
  ((SELECT id FROM Level WHERE name = 'Đỉnh cao N1'), 'Diễn thuyết học thuật', 'Rèn luyện Kanji học thuật và văn phong trang trọng.', 1)
ON DUPLICATE KEY UPDATE
  overview = VALUES(overview),
  order_index = VALUES(order_index);

-- Quiz question + choice
DROP TABLE IF EXISTS quiz_choices;
DROP TABLE IF EXISTS quiz_questions;

CREATE TABLE quiz_questions (
    question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lesson_id   BIGINT NOT NULL,
    prompt      TEXT   NOT NULL,
    explanation TEXT   NOT NULL,
    order_index INT    NOT NULL,
    CONSTRAINT fk_question_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quiz_choices (
    choice_id  BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    content    VARCHAR(255) NOT NULL,
    is_correct TINYINT(1)  NOT NULL,
    CONSTRAINT fk_choice_question FOREIGN KEY (question_id) REFERENCES quiz_questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO quiz_questions (lesson_id, prompt, explanation, order_index) VALUES
  (1, 'Kanji nào mang nghĩa mặt trời/ngày?', '日 biểu thị mặt trời và ngày.', 1),
  (1, 'Chữ 月 đọc on-yomi là gì?', '月 đọc on-yomi là ゲツ hoặc ガツ.', 2),
  (2, 'Kanji nào thể hiện núi?', '山 mang nghĩa núi.', 1),
  (3, 'Từ nào nghĩa là học tập?', '学 dùng trong 学校 (trường học).', 1),
  (3, 'Kanji nào biểu thị hoạt động đi lại?', '行 được dùng cho các động từ di chuyển.', 2),
  (4, 'Kanji nào đại diện cho trường học?', '校 xuất hiện trong 学校.', 1),
  (5, 'Chữ nào diễn tả hoạt động luyện tập?', '練 mang nghĩa rèn luyện.', 1),
  (6, 'Kanji nào dùng để nói về chuyến đi?', '旅 thể hiện hành trình, du lịch.', 1),
  (7, 'Kanji nào mô tả kinh nghiệm tích lũy?', '経 biểu thị kinh nghiệm, trải qua.', 1),
  (8, 'Kanji nào nói về việc thử nghiệm, kiểm tra?', '験 xuất hiện trong 試験 (kỳ thi).', 1),
  ((SELECT lesson_id FROM lessons WHERE title = 'Chữ ghép thực dụng'), 'Chữ Kanji nào thường dùng để nói "chuẩn bị"?', '準 xuất hiện trong 準備 (chuẩn bị).', 1),
  ((SELECT lesson_id FROM lessons WHERE title = 'Tin tức buổi sáng'), 'Kanji nào xuất hiện nhiều trong tin tức kinh tế?', '護 được dùng trong 護送 (hộ tống) hay 保護 (bảo hộ).', 1),
  ((SELECT lesson_id FROM lessons WHERE title = 'Diễn thuyết học thuật'), 'Chữ nào diễn tả âm vang và ảnh hưởng?', '響 biểu thị sự vang dội và ảnh hưởng.', 1)
ON DUPLICATE KEY UPDATE
  prompt = VALUES(prompt),
  explanation = VALUES(explanation),
  order_index = VALUES(order_index);

INSERT INTO quiz_choices (question_id, content, is_correct) VALUES
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 1 AND order_index = 1), '日', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 1 AND order_index = 1), '月', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 1 AND order_index = 1), '山', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 1 AND order_index = 2), 'ゲツ / ガツ', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 1 AND order_index = 2), 'ニチ', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 1 AND order_index = 2), 'サン', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 2 AND order_index = 1), '山', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 2 AND order_index = 1), '川', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 2 AND order_index = 1), '本', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 3 AND order_index = 1), '学', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 3 AND order_index = 1), '行', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 3 AND order_index = 1), '高', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 3 AND order_index = 2), '行', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 3 AND order_index = 2), '食', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 3 AND order_index = 2), '話', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 4 AND order_index = 1), '校', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 4 AND order_index = 1), '日', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 4 AND order_index = 1), '月', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 5 AND order_index = 1), '練', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 5 AND order_index = 1), '旅', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 5 AND order_index = 1), '験', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 6 AND order_index = 1), '旅', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 6 AND order_index = 1), '話', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 6 AND order_index = 1), '高', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 7 AND order_index = 1), '経', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 7 AND order_index = 1), '学', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 7 AND order_index = 1), '校', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 8 AND order_index = 1), '験', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 8 AND order_index = 1), '経', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 8 AND order_index = 1), '想', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = (SELECT lesson_id FROM lessons WHERE title = 'Chữ ghép thực dụng') AND order_index = 1), '準', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = (SELECT lesson_id FROM lessons WHERE title = 'Chữ ghép thực dụng') AND order_index = 1), '護', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = (SELECT lesson_id FROM lessons WHERE title = 'Chữ ghép thực dụng') AND order_index = 1), '経', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = (SELECT lesson_id FROM lessons WHERE title = 'Tin tức buổi sáng') AND order_index = 1), '護', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = (SELECT lesson_id FROM lessons WHERE title = 'Tin tức buổi sáng') AND order_index = 1), '旅', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = (SELECT lesson_id FROM lessons WHERE title = 'Tin tức buổi sáng') AND order_index = 1), '森', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = (SELECT lesson_id FROM lessons WHERE title = 'Diễn thuyết học thuật') AND order_index = 1), '響', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = (SELECT lesson_id FROM lessons WHERE title = 'Diễn thuyết học thuật') AND order_index = 1), '準', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = (SELECT lesson_id FROM lessons WHERE title = 'Diễn thuyết học thuật') AND order_index = 1), '森', 0)
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  is_correct = VALUES(is_correct);

-- Account upgrade requests cho tính năng VIP
DROP TABLE IF EXISTS AccountUpgradeRequest;
CREATE TABLE AccountUpgradeRequest (
    request_id    BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    currentRoleId TINYINT      NOT NULL,
    targetRoleId  TINYINT      NOT NULL,
    note          TEXT         NULL,
    receiptImagePath VARCHAR(255) NULL,
    transactionCode VARCHAR(100) NULL,
    status        ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    createdAt     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processedAt   TIMESTAMP    NULL,
    PRIMARY KEY (request_id),
    KEY idx_upgrade_user (user_id),
    CONSTRAINT fk_upgrade_user FOREIGN KEY (user_id) REFERENCES `User`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO AccountUpgradeRequest (request_id, user_id, currentRoleId, targetRoleId, note, receiptImagePath, transactionCode, status)
VALUES
  (1, 1, 1, 1, 'Admin kiểm tra workflow', NULL, 'ADM-001', 'APPROVED'),
  (2, (SELECT id FROM `User` WHERE email = 'minh.student@example.com'), 2, 3, 'Muốn ôn luyện nhanh trước kỳ thi N3.', NULL, NULL, 'PENDING'),
  (3, (SELECT id FROM `User` WHERE email = 'thu.travel@example.com'), 2, 3, 'Đăng ký VIP để mở bài học về du lịch.', NULL, NULL, 'PENDING')
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  note = VALUES(note),
  receiptImagePath = VALUES(receiptImagePath),
  transactionCode = VALUES(transactionCode);

-- WalletDeposit lưu giao dịch nạp tiền qua mã QR
DROP TABLE IF EXISTS WalletDeposit;
CREATE TABLE WalletDeposit (
    deposit_id  BIGINT        NOT NULL AUTO_INCREMENT,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    qrCodeUrl   VARCHAR(255)  NOT NULL,
    status      ENUM('PENDING','PAID','CANCELLED') NOT NULL DEFAULT 'PENDING',
    createdAt   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (deposit_id),
    KEY idx_deposit_user (user_id),
    CONSTRAINT fk_deposit_user FOREIGN KEY (user_id) REFERENCES `User`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO WalletDeposit (deposit_id, user_id, amount, qrCodeUrl, status)
VALUES
  (1, 1, 500000, 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=ADMIN-DEMO-500K', 'PAID'),
  (2, (SELECT id FROM `User` WHERE email = 'hana.vip@example.com'), 300000, 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=VIP-HANA-300K', 'PAID'),
  (3, (SELECT id FROM `User` WHERE email = 'minh.student@example.com'), 150000, 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=MINH-TOPUP-150K', 'PENDING')
ON DUPLICATE KEY UPDATE
  status = VALUES(status);

-- Giao dịch thanh toán MoMo
DROP TABLE IF EXISTS MomoPayment;
CREATE TABLE MomoPayment (
    payment_id  BIGINT        NOT NULL AUTO_INCREMENT,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    plan_code   VARCHAR(40)   NOT NULL,
    order_id    VARCHAR(64)   NOT NULL,
    request_id  VARCHAR(64)   NOT NULL,
    status      ENUM('PENDING','SUCCESS','FAILED') NOT NULL DEFAULT 'PENDING',
    payUrl      TEXT          NULL,
    deeplink    TEXT          NULL,
    resultCode  INT           NULL,
    message     VARCHAR(255)  NULL,
    momoTransId VARCHAR(64)   NULL,
    createdAt   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (payment_id),
    UNIQUE KEY uq_momo_order (order_id),
    CONSTRAINT fk_momo_user FOREIGN KEY (user_id) REFERENCES `User`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
