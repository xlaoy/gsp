--创建万达不统计的影院信息表
CREATE TABLE "gsp"."wanda_not_include_cinema" (
"place_id" int4 NOT NULL,
"place_name" varchar COLLATE "default"
);
ALTER TABLE "gsp"."wanda_not_include_cinema" ADD PRIMARY KEY ("place_id");

--插入万达不统计的影院的信息
INSERT INTO "gsp"."wanda_not_include_cinema" VALUES ('63364', '五角场万达电影城');
INSERT INTO "gsp"."wanda_not_include_cinema" VALUES ('2088696', '周浦万达电影城');
INSERT INTO "gsp"."wanda_not_include_cinema" VALUES ('37925152', '江桥万达电影城');
INSERT INTO "gsp"."wanda_not_include_cinema" VALUES ('61431049', '宝山万达电影城');
INSERT INTO "gsp"."wanda_not_include_cinema" VALUES ('101311273', '太仓万达电影城');
INSERT INTO "gsp"."wanda_not_include_cinema" VALUES ('114483306', '松江万达电影城');
INSERT INTO "gsp"."wanda_not_include_cinema" VALUES ('133265263', '金山万达电影城');