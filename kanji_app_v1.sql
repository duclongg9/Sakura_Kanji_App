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
