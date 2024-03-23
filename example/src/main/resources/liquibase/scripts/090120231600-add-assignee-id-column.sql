alter table tickets
    add column assignee_id uuid;

comment on column tickets.assignee_id is 'Идентификатор исполнителя обращения';