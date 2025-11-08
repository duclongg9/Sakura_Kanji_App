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
  id        BIGINT        NOT NULL AUTO_INCREMENT,
  userName  VARCHAR(120)  NOT NULL,
  email     VARCHAR(190)  NOT NULL,
  imgUrl    VARCHAR(255)  NULL,
  matKhau   VARCHAR(255)  NULL,
  roleId    TINYINT       NOT NULL DEFAULT 2,          -- 1/2/3
  createdAt TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_user_email (email),
  KEY idx_user_role (roleId),
  CONSTRAINT fk_user_role FOREIGN KEY (roleId) REFERENCES Role(id)
) ENGINE=InnoDB;

-- Seed user mẫu để test đăng nhập local
INSERT INTO `User` (userName, email, matKhau, roleId)
VALUES ('admin', 'admin@example.com', '123456', 1)
ON DUPLICATE KEY UPDATE
  userName = VALUES(userName),
  matKhau = VALUES(matKhau),
  roleId = VALUES(roleId);

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
  ('Tăng tốc N4', 2, 'Chữ Kanji thường gặp trong N4.', TRUE, 'FREE')
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
  ('本', 'bản', 'ホン', 'もと', 'Sách, gốc rễ', 2),
  ('学', 'học', 'ガク', 'まな.ぶ', 'Học tập', 2),
  ('行', 'hành', 'コウ, ギョウ', 'い.く, おこな.う', 'Đi lại, thực hiện', 2),
  ('高', 'cao', 'コウ', 'たか.い', 'Cao, đắt', 3),
  ('校', 'giáo', 'コウ', 'かま', 'Trường học', 3)
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
  (2, 'Sinh hoạt hằng ngày', 'Từ vựng liên quan đến học tập.', 1),
  (3, 'Trường lớp N4', 'Bài học mở rộng cho N4.', 1)
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
  (4, 'Kanji nào đại diện cho trường học?', '校 xuất hiện trong 学校.', 1)
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
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 4 AND order_index = 1), '校', 1),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 4 AND order_index = 1), '日', 0),
  ((SELECT question_id FROM quiz_questions WHERE lesson_id = 4 AND order_index = 1), '月', 0)
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  is_correct = VALUES(is_correct);
