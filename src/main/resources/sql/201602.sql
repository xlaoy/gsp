--´´½¨±í
CREATE TABLE gsp.gsp_job (
tag varchar(50) COLLATE "default" NOT NULL,
status varchar(50) COLLATE "default",
update_time timestamp(6)
);
ALTER TABLE gsp.gsp_job ADD PRIMARY KEY (tag);

insert into gsp.gsp_job values ('ticket-autoTomerchant', 'FINISH', null);
