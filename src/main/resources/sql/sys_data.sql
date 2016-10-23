CREATE TABLE "gsp"."sys_data" (
"recordid" int4 NOT NULL,
"type" varchar(50) COLLATE "default",
"code" varchar(50) COLLATE "default",
"name" varchar(500) COLLATE "default",
"special" text COLLATE "default",
"updatetime" timestamp(6)
);

ALTER TABLE "gsp"."sys_data" ADD PRIMARY KEY ("recordid");
