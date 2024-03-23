update public.tickets
set deleted_at = now()
where human_readable_id in
      (1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
       13, 14, 15, 16, 18, 19, 21, 22,
       24, 25, 27, 28, 29, 30, 31, 35, 36);

create unique index tickets_call_id_assignee_id_uindex
    on public.tickets (call_id, assignee_id)
    where tickets.archived_at is null
        and tickets.deleted_at is null;