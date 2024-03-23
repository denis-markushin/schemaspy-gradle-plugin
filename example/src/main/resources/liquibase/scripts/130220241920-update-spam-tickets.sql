begin;
update tickets set decision_status='CONSULTATION_TOOK_PLACE' where decision_status = 'SPAM';
commit;