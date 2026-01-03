alter table public.employees
    alter column manager_id drop not null;

alter table public.employees
    alter column position drop not null;

alter table public.employees
    alter column first_time drop not null;

