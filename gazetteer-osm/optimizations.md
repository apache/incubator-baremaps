# Optimizing postgresql table for more than 100K inserts per second

* Create `UNLOGGED` table. This reduces the amount of info written to persistent storage by up to 2x.
* Set `WITH (autovacuum_enabled=false)` on the table. This saves CPU time and IO bandwidth
  on useless vacuuming of the table (since we never `DELETE` or `UPDATE` the table).
* Insert rows with `COPY FROM STDIN`. This is the fastest possible approach to insert rows into table.
* Minimize the number of indexes in the table, since they slow down inserts. Usually an index
  on `time timestamp with time zone` is enough.
* Add `synchronous_commit = off` to `postgresql.conf`.
* Use table inheritance for fast removal of old info:
```sql
CREATE TABLE parent ... ;
CREATE TABLE child_1() INHERITS (parent);
CREATE TABLE child_2() INHERITS (parent);

-- always INSERT rows into child_1.
-- SELECT from parent.

-- periodically run the follwing sql for rotating child_1 with child_2:
TRUNCATE TABLE child_2;
BEGIN;
ALTER TABLE child_1 RENAME TO child_tmp;
ALTER TABLE child_2 RENAME TO child_1;
ALTER TABLE child_tmp RENAME TO child_2;
COMMIT;
```
This is much faster comparing to
```sql
DELETE FROM parent WHERE time < now() - interval 'given period'
```
This also avoids table fragmentation, so `SELECT` queries work faster on the table.