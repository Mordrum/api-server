alter table if exists civilization drop constraint if exists fk_civilization_founder_id;

alter table if exists civilization_allies drop constraint if exists fk_civilization_allies_civilization_1;
drop index if exists ix_civilization_allies_civilization_1;

alter table if exists civilization_allies drop constraint if exists fk_civilization_allies_civilization_2;
drop index if exists ix_civilization_allies_civilization_2;

alter table if exists civilization_enemies drop constraint if exists fk_civilization_enemies_civilization_1;
drop index if exists ix_civilization_enemies_civilization_1;

alter table if exists civilization_enemies drop constraint if exists fk_civilization_enemies_civilization_2;
drop index if exists ix_civilization_enemies_civilization_2;

alter table if exists civilization_chunk drop constraint if exists fk_civilization_chunk_civilization_id;
drop index if exists ix_civilization_chunk_civilization_id;

alter table if exists fishing_record drop constraint if exists fk_fishing_record_player_id;
drop index if exists ix_fishing_record_player_id;

alter table if exists player drop constraint if exists fk_player_civilization_id;
drop index if exists ix_player_civilization_id;

drop table if exists civilization cascade;

drop table if exists civilization_allies cascade;

drop table if exists civilization_enemies cascade;

drop table if exists civilization_chunk cascade;

drop table if exists fishing_record cascade;

drop table if exists player cascade;

