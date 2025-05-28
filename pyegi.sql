create database pyegibot;
use pyegibot;

CREATE TABLE waste_search (
  category_id INT PRIMARY KEY,
  category VARCHAR(5) UNIQUE NOT NULL
);
CREATE TABLE home_appliances (
  id INT PRIMARY KEY,
  category INT NOT NULL,
  item VARCHAR(45) NOT NULL,
  standard VARCHAR(45),
  fee INT NOT NULL,
  FOREIGN KEY (category) REFERENCES waste_search(category_id)
);
CREATE TABLE furniture (
  id INT PRIMARY KEY,
  category INT NOT NULL,
  item VARCHAR(45) NOT NULL,
  standard1 VARCHAR(45),
  standard2 VARCHAR(45),
  fee INT NOT NULL,
  FOREIGN KEY (category) REFERENCES waste_search(category_id)
);
CREATE TABLE daily_necessities (
  id INT PRIMARY KEY,
  category INT NOT NULL,
  item VARCHAR(45) NOT NULL,
  standard1 VARCHAR(45),
  standard2 VARCHAR(45),
  fee INT NOT NULL,
  FOREIGN KEY (category) REFERENCES waste_search(category_id)
);

INSERT INTO waste_search (category_id, category) VALUES
(1, '가전제품류'),
(2, '가구류'),
(3, '생활용품류');

INSERT INTO home_appliances (id, category, item, standard, fee) VALUES
(1, 1, '공기청정기', '높이 1m 이상', 3000),
(2, 1, '식기건조기', '높이 1m 이상', 3500),
(3, 1, '식기세척기', '높이 1m 이상', 3500),
(4, 1, '에어컨 실외기', '높이 1m 이상', 2000),
(5, 1, '정수기', '높이 1m 이상', 5000);

INSERT INTO furniture (id, category, item, standard1, standard2, fee) VALUES
(1, 2, 'TV 받침대(1)', '길이 120cm 이상', '', 5000),
(2, 2, 'TV 받침대(2)', '길이 120cm 미만', '', 3000),
(3, 2, '거울(1)', '1㎡ 이상', '', 2000),
(4, 2, '거울(2)', '1㎡ 미만', '', 1000),
(5, 2, '찬장(1)', '폭 130cm 이상', '', 10000),
(6, 2, '찬장(2)', '폭 120cm 이상', '', 7000),
(7, 2, '찬장(3)', '폭 90cm 이상', '', 4000),
(8, 2, '찬장(4)', '폭 90cm 미만', '', 3500),
(9, 2, '책상(1)', '길이 120cm 이상', '', 5000),
(10, 2, '책상(2)', '길이 120cm 미만', '', 3000),
(11, 2, '책장(1)', '90cm', '180cm 이상', 9000),
(12, 2, '책장(2)', '90cm', '180cm 미만', 5000),
(13, 2, '책장(3)', '100cm 이하', '100cm 이하', 2000),
(14, 2, '싱크대(1)', '상판 길이 120cm 이상', '', 6000),
(15, 2, '싱크대(2)', '상판 길이 120cm 미만', '', 4000),
(16, 2, '유리', '1㎡ 당', '', 2000),
(17, 2, '장롱(1)', '가로 120cm 이상', '', 17900),
(18, 2, '장롱(2)', '가로 90cm 이상', '', 11900),
(19, 2, '장롱(3)', '가로 90cm 미만', '', 7000),
(20, 2, '장식장(1)', '높이 180cm 이상', '', 10000),
(21, 2, '장식장(2)', '높이 120cm 이상', '', 5500),
(22, 2, '장식장(3)', '높이 120cm 미만', '', 4000);

INSERT INTO daily_necessities (id, category, item, standard1, standard2, fee) VALUES
(1, 3, '게시판 · 화이트보드(1)', '가로 1m 이상', '', 2000),
(2, 3, '게시판 · 화이트보드(2)', '가로 1m 미만', '', 1000),
(3, 3, '고무통(1)', '지름 1m 이상', '', 5000),
(4, 3, '고무통(2)', '지름 50cm 이상', '', 3000),
(5, 3, '고무통(3)', '지름 50cm 미만', '', 2000),
(6, 3, '벽시계(1)', '길이 1m 이상', '', 2000),
(7, 3, '벽시계(2)', '길이 1m 미만', '', 1000),
(8, 3, '빨래건조대(1)', '길이 1m 이상', '', 2000),
(9, 3, '빨래건조대(2)', '길이 1m 미만', '', 1000),
(10, 3, '액자(1)', '높이 1m 이상', '', 3000),
(11, 3, '액자(2)', '높이 50cm 이상 ~ 1m 미만', '', 2000),
(12, 3, '액자(3)', '높이 50cm 미만', '', 1000),
(13, 3, '어항(1)', '가로1m', '높이 60cm 초과', 6000),
(14, 3, '어항(2)', '가로1m', '높이 60cm 이하', 4000),
(15, 3, '조명기구(1)', '1m 이상', '1m 이상', 2000),
(16, 3, '조명기구(2)', '1m 미만', '1m 미만', 1000),
(17, 3, '캣타워(1)', '높이 1m 이상', '', 5000),
(18, 3, '캣타워(2)', '높이 1m 미만', '', 3000),
(19, 3, '항아리(1)', '높이 50cm 이상', '', 2000),
(20, 3, '항아리(2)', '높이 50cm 미만', '', 1000),                             
(21, 3, '화분(1)', '높이 50cm 이상', '', 1500),
(22, 3, '화분(2)', '높이 50cm 미만', '', 1000);

                                     