create table civilization (
  id                            bigserial not null,
  name                          varchar(255),
  banner                        varchar(255),
  invite_only                   boolean,
  founder_id                    bigint not null,
  primary_color                 integer,
  secondary_color               integer,
  tertiary_color                integer,
  tag                           varchar(255),
  welcome_message               varchar(255),
  motd                          varchar(255),
  description                   varchar(255),
  primary_focus                 integer,
  version                       bigint not null,
  created_at                    timestamptz not null,
  updated_at                    timestamptz not null,
  constraint ck_civilization_primary_color check ( primary_color in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21)),
  constraint ck_civilization_secondary_color check ( secondary_color in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21)),
  constraint ck_civilization_tertiary_color check ( tertiary_color in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21)),
  constraint ck_civilization_primary_focus check ( primary_focus in (0,1,2,3,4)),
  constraint uq_civilization_name unique (name),
  constraint uq_civilization_banner unique (banner),
  constraint uq_civilization_founder_id unique (founder_id),
  constraint pk_civilization primary key (id)
);

create table civilization_allies (
  left_ally                     bigint not null,
  right_ally                    bigint not null,
  constraint pk_civilization_allies primary key (left_ally,right_ally)
);

create table civilization_enemies (
  left_enemy                    bigint not null,
  right_enemy                   bigint not null,
  constraint pk_civilization_enemies primary key (left_enemy,right_enemy)
);

create table civilization_chunk (
  id                            bigserial not null,
  civilization_id               bigint,
  x                             integer,
  z                             integer,
  version                       bigint not null,
  created_at                    timestamptz not null,
  updated_at                    timestamptz not null,
  constraint pk_civilization_chunk primary key (id)
);

create table fishing_record (
  id                            bigserial not null,
  player_id                     bigint,
  fish                          integer,
  weight                        float,
  version                       bigint not null,
  created_at                    timestamptz not null,
  updated_at                    timestamptz not null,
  constraint pk_fishing_record primary key (id)
);

create table player (
  id                            bigserial not null,
  uuid                          uuid,
  civilization_id               bigint,
  version                       bigint not null,
  created_at                    timestamptz not null,
  updated_at                    timestamptz not null,
  constraint pk_player primary key (id)
);

alter table civilization add constraint fk_civilization_founder_id foreign key (founder_id) references player (id) on delete restrict on update restrict;

alter table civilization_allies add constraint fk_civilization_allies_civilization_1 foreign key (left_ally) references civilization (id) on delete restrict on update restrict;
create index ix_civilization_allies_civilization_1 on civilization_allies (left_ally);

alter table civilization_allies add constraint fk_civilization_allies_civilization_2 foreign key (right_ally) references civilization (id) on delete restrict on update restrict;
create index ix_civilization_allies_civilization_2 on civilization_allies (right_ally);

alter table civilization_enemies add constraint fk_civilization_enemies_civilization_1 foreign key (left_enemy) references civilization (id) on delete restrict on update restrict;
create index ix_civilization_enemies_civilization_1 on civilization_enemies (left_enemy);

alter table civilization_enemies add constraint fk_civilization_enemies_civilization_2 foreign key (right_enemy) references civilization (id) on delete restrict on update restrict;
create index ix_civilization_enemies_civilization_2 on civilization_enemies (right_enemy);

alter table civilization_chunk add constraint fk_civilization_chunk_civilization_id foreign key (civilization_id) references civilization (id) on delete restrict on update restrict;
create index ix_civilization_chunk_civilization_id on civilization_chunk (civilization_id);

alter table fishing_record add constraint fk_fishing_record_player_id foreign key (player_id) references player (id) on delete restrict on update restrict;
create index ix_fishing_record_player_id on fishing_record (player_id);

alter table player add constraint fk_player_civilization_id foreign key (civilization_id) references civilization (id) on delete restrict on update restrict;
create index ix_player_civilization_id on player (civilization_id);

