-- create types
create type TICKET_STATUS as enum ('IN_PROGRESS', 'CLOSED');
create type DECISION_STATUS as enum (
    'CONSULTATION_TOOK_PLACE' /* Консультация состоялась */,
    'REQUEST_FORWARDED' /* Обращение перенаправлено */,
    'THE_CALL_WAS_DROPPED' /* Звонок сорвался - не удалось дозвониться */,
    'POSTPONED' /* Обращение отложено */,
    'SPAM' /* Спам */
    );
create type CHANNEL_TYPE as enum ('PHONE');
create type CHANNEL as enum ('VOX_IMPLANT');

-- create tables
create table issues (
    id                    uuid,
    order_id              uuid,
    dark_store_id         uuid,
    ticket_id             uuid                    not null,
    subject_ids           uuid[],
    tag_ids               uuid[],
    customer_phone_number text,

    -- Системные поля
    created_at            timestamp default now() not null,
    updated_at            timestamp default now() not null,
    archived_at           timestamp,
    deleted_at            timestamp,
    created_by            uuid                    not null,
    updated_by            uuid                    not null,
    primary key (id)
);

comment on table issues is 'Причина обращения';

comment on column issues.id is 'Идентификатор записи';
comment on column issues.order_id is 'Идентификатор заказа Самокат';
comment on column issues.dark_store_id is 'Идентификатор даркстора';
comment on column issues.ticket_id is 'Идентификатор обращения';
comment on column issues.subject_ids is 'Идентификаторы тематик обращения';
comment on column issues.tag_ids is 'Дополнительные теги';
comment on column issues.customer_phone_number is 'Телефон клиента';

-- Системные поля
comment on column issues.created_at is 'Дата и время создания записи';
comment on column issues.updated_at is 'Дата и время обновления записи';
comment on column issues.archived_at is 'Дата и время архивации записи';
comment on column issues.deleted_at is 'Дата и время удаления записи';
comment on column issues.created_by is 'Идентификатор пользователя, создавшего запись';
comment on column issues.updated_by is 'Идентификатор пользователя, изменившего запись';

create index issues_tag_ids_idx on issues using GIN (tag_ids);
create index issues_subject_ids_idx on issues using GIN (subject_ids);

create table tickets (
    id                  uuid                                not null,
    human_readable_id   bigserial                           not null,
    comment             text,
    ticket_status       TICKET_STATUS default 'IN_PROGRESS' not null,
    decision_status     DECISION_STATUS,
    call_id             text,
    caller_phone_number text,
    channel             CHANNEL,
    channel_type        CHANNEL_TYPE,
    closed_at           timestamp,
    -- Системные поля
    created_at          timestamp     default now()         not null,
    updated_at          timestamp     default now()         not null,
    archived_at         timestamp,
    deleted_at          timestamp,
    created_by          uuid                                not null,
    updated_by          uuid                                not null,
    primary key (id)
);

comment on table tickets is 'Обращения пользователей';

comment on column tickets.id is 'Идентификатор записи';
comment on column tickets.human_readable_id is 'Человека-читаемый id обращения';
comment on column tickets.comment is 'Комментарий к обращению';
comment on column tickets.ticket_status is 'Статус обращения';
comment on column tickets.decision_status is 'Статус звонка';
comment on column tickets.call_id is 'Идентификатор звонка во внешней системе (например Voximplant)';
comment on column tickets.caller_phone_number is 'Телефон абонента (номер телефона с которого позвонил абонент)';
comment on column tickets.channel is 'Канал обращения';
comment on column tickets.channel_type is 'Тип канала обращения';
comment on column tickets.closed_at is 'Дата и время закрытия обращения';

-- Системные поля
comment on column tickets.created_at is 'Дата и время создания записи';
comment on column tickets.updated_at is 'Дата и время обновления записи';
comment on column tickets.archived_at is 'Дата и время архивации записи';
comment on column tickets.deleted_at is 'Дата и время удаления записи';
comment on column tickets.created_by is 'Идентификатор пользователя, создавшего запись';
comment on column tickets.updated_by is 'Идентификатор пользователя, изменившего запись';

-- create foreign keys
-- issues table
alter table issues
    add constraint issues_tickets_id_fk
        foreign key (ticket_id) references tickets;