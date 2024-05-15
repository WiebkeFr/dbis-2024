## 3.1 Isolation Levels
#### a) https://www.postgresql.org/docs/current/transaction-iso.html
- (Read Uncommited) treated as Read Commited
- Read Committed (default/current)
- Repeatable Read
- Serializable
psql: `SHOW default_transaction_isolation;`

#### b) 
```sql
CREATE TABLE sheet3 (
    id INT,
    name VARCHAR(255)
)
INSERT INTO sheet3 (id, name)
VALUES (1, 'Anna'),
VALUES (2, 'Sophie');
```

#### c) 
```
\echo :AUTOCOMMIT # returning 'on'
\set AUTOCOMMIT off
SELECT * FROM sheet3 WHERE id = 1;
SELECT relation::regclass, mode, granted FROM pg_locks WHERE relation::regclass = 'sheet3'::regclass;
```
returns: 
```
relation |      mode       | granted 
----------+-----------------+---------
sheet3   | AccessShareLock | t
(1 row)
```
This lock is for reading a table using SELECT: (https://www.postgresql.org/docs/current/explicit-locking.html)

#### d) 
```
SHOW TRANSACTION ISOLATION LEVEL;
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
SELECT * FROM sheet3 WHERE id = 1;
SELECT relation::regclass, mode, granted FROM pg_locks WHERE relation::regclass = 'sheet3'::regclass; 
```
returns: 
  ```
  relation |      mode       | granted 
  ----------+-----------------+---------
  sheet3   | AccessShareLock | t
  sheet3   | SIReadLock      | t
  (2 rows)
  ```
- SIReadLock is a predicate lock in serializable mode
- https://www.postgresql.org/docs/current/transaction-iso.html#XACT-SERIALIZABLE

## 3.2 Lock Conflicts
#### a)
C1: AUTOCOMMIT OFF, TRANSACTION ISOLATION: READ COMMITTED
C2: AUTOCOMMIT ON, TRANSACTION ISOLATION: READ COMMITTED

Query by C1 (w/o commit) -> Insert row by C2 -> Query by C1 -> Commit by 1C

What happens?
- successful first query and insertion
- second query already reads additional line
- commit successful

#### b)
1C: AUTOCOMMIT OFF, TRANSACTION ISOLATION: REPEATABLE READ
2C: AUTOCOMMIT ON, TRANSACTION ISOLATION: READ COMMITTED

Query by C1 (w/o commit) -> Insert row by C2 -> Query by C1 -> Commit by 1C

What happens?
- successful first query
- insertion successful and visible in DBeaver
- second query returns same result as the first query (w/o new record)
- locks before commit:  
  ```
  relation |      mode       | granted 
  ----------+-----------------+---------
  sheet3   | AccessShareLock | t
  ```
- commit successful

2PL: 2-Phase-Locking-Protocoll with expanding phase and shrinking phase of R-locks and X-locks
=> not the expected behaviour, because C1 has Read-Lock for sheet3.
   Therefore, C2 can either read as well or has to wait to write until C1 is committed

#### c) 
1C: AUTOCOMMIT OFF, TRANSACTION ISOLATION: READ COMMITTED
2C: AUTOCOMMIT ON, TRANSACTION ISOLATION: READ COMMITTED

Row change in C1 -> Row change in C2 (other row) -> Row change in C2

-  UPDATE sheet3 SET name = 'Mateo' WHERE id = 6; (in C1)
- Locks after row change:
  ```
    relation |       mode       | granted 
    ----------+------------------+---------
    sheet3   | RowExclusiveLock | t
    ```
- UPDATE sheet3 SET name = 'Mateo' WHERE id = 3; (in C2)
- UPDATE sheet3 SET name = 'Mateo' WHERE id = 6; (in C2)
- no response (return value/ positive/ negative) => cancelling with '^C'
- As soon as C1 is committed, the queries are run..

No difference with following isolation modes:
1C: AUTOCOMMIT OFF, TRANSACTION ISOLATION: REPEATABLE READ
2C: AUTOCOMMIT ON, TRANSACTION ISOLATION: READ COMMITTED

#### d) 
- Syntax error in Transaction
- Deadlock
- Violating of contraints (e.g. unique value)
- Lost update prevention (2 transaction serializable: update (of C2) between read and write of first connection - ERROR:  could not serialize access due to concurrent update)

#### e) Deadlock: 2 different connection waiting on one another
C1: UPDATE sheet3 SET name = 'SAMI' WHERE id = 1;
C2: UPDATE sheet3 SET name = 'ALFI' WHERE id = 3;
C1: UPDATE sheet3 SET name = 'SAMI updated' WHERE id = 3; --> waiting on C2
C2: UPDATE sheet3 SET name = 'ALFI updated' WHERE id = 1; --> waiting on C1

```
[automatic postgres handling]
ERROR:  deadlock detected
DETAIL:  Process 44603 waits for ShareLock on transaction 3137817; blocked by process 44559.
Process 44559 waits for ShareLock on transaction 3137818; blocked by process 44603.
HINT:  See server log for query details.
CONTEXT:  while updating tuple (0,1) in relation "sheet3"
````

1C: Commit; (Success)
2C: Commit; (Rollback)

## 3.3 Scheduling
#### C1 im Vergleich 
##### C1:
pool-1-thread-1sql = SELECT name FROM dissheet3 WHERE id = 1;
Goofy
pool-2-thread-1sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;
pool-2-thread-1sql = COMMIT;
pool-1-thread-1sql = UPDATE dissheet3 SET name = name || ' + Max' WHERE id = 1;
pool-1-thread-1sql = SELECT name FROM dissheet3 WHERE id = 1;
Mickey + Max
pool-1-thread-1sql = COMMIT;
Waiting for threads
Finished all threads
1,Mickey + Max
2,Donald
3,Tick
4,Trick
5,Track

##### C1 mit SS2PL:
pool-1-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 1 FOR UPDATE;
Goofy
pool-2-thread-1 sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;
pool-1-thread-1 sql = UPDATE dissheet3 SET name = name || ' + Max' WHERE id = 1;
pool-1-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 1 FOR SHARE;
Goofy + Max
pool-1-thread-1 sql = COMMIT;
EXCEPTION in pool-2-thread-1 during operation: UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;
ROLLBACK: pool-2-thread-1
REPEAT: pool-2-thread-1 sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;
pool-2-thread-1 sql = COMMIT;
Waiting for threads
Finished all threads
1,Mickey
2,Donald
3,Tick
4,Trick
5,Track

##### C1 mit Serializable
pool-1-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 1 FOR UPDATE;
Goofy
pool-2-thread-1 sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;
pool-1-thread-1 sql = UPDATE dissheet3 SET name = name || ' + Max' WHERE id = 1;
pool-1-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 1 FOR SHARE;
Goofy + Max
pool-1-thread-1 sql = COMMIT;
EXCEPTION in pool-2-thread-1 during operation: UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;
ROLLBACK: pool-2-thread-1
repeat: pool-2-thread-1 sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;
pool-2-thread-1 sql = COMMIT;
Waiting for threads
Finished all threads
1,Mickey
2,Donald
3,Tick
4,Trick
5,Track

#### C2 im Vergleich 
##### C2:
pool-1-thread-1sql = SELECT name FROM dissheet3 WHERE id = 1;
Goofy
pool-2-thread-1sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;
pool-2-thread-1sql = COMMIT;
pool-1-thread-1sql = SELECT name FROM dissheet3 WHERE id = 1;
Mickey
pool-1-thread-1sql = COMMIT;
Waiting for threads
Finished all threads
1,Mickey
2,Donald
3,Tick
4,Trick
5,Track

##### C2 mit SS2PL:
pool-1-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 1 FOR SHARE;
Goofy
pool-2-thread-1 sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;
pool-1-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 1;
Goofy <-- same value as before
pool-1-thread-1 sql = COMMIT;
pool-2-thread-1 sql = COMMIT;
Waiting for threads
Finished all threads
1,Mickey
2,Donald
3,Tick
4,Trick
5,Track

##### C2 mit Serializablility
pool-1-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 1 FOR SHARE;
Goofy
pool-2-thread-1 sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;
pool-1-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 1;
Goofy
pool-1-thread-1 sql = COMMIT;
pool-2-thread-1 sql = COMMIT;
Waiting for threads
Finished all threads
1,Mickey
2,Donald
3,Tick
4,Trick
5,Track

#### C3 im Vergleich
##### C3:
pool-2-thread-1sql = SELECT name FROM dissheet3 WHERE id = 2;
Donald
pool-1-thread-1sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 2;
pool-1-thread-1sql = UPDATE dissheet3 SET name = 'Truck' WHERE id = 4;
pool-1-thread-1sql = COMMIT;
pool-2-thread-1sql = SELECT name FROM dissheet3 WHERE id = 4;
Truck
pool-2-thread-1sql = UPDATE dissheet3 SET name = 'Luigi' WHERE id = 2;
pool-2-thread-1sql = UPDATE dissheet3 SET name = 'Mario' WHERE id = 4;
pool-2-thread-1sql = COMMIT;
Waiting for threads
Finished all threads
1,Goofy
2,Luigi
3,Tick
4,Mario
5,Track

##### C3 mit SS2PL:
pool-2-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 2 FOR UPDATE;
Donald
pool-1-thread-1 sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 2;
pool-2-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 4 FOR UPDATE;
Trick
pool-2-thread-1 sql = UPDATE dissheet3 SET name = 'Luigi' WHERE id = 2;
pool-2-thread-1 sql = UPDATE dissheet3 SET name = 'Mario' WHERE id = 4;
pool-2-thread-1 sql = COMMIT;
EXCEPTION in pool-1-thread-1 during operation: UPDATE dissheet3 SET name = 'Mickey' WHERE id = 2;
ROLLBACK: pool-1-thread-1
REPEAT: pool-1-thread-1 sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 2;
pool-1-thread-1 sql = UPDATE dissheet3 SET name = 'Truck' WHERE id = 4;
pool-1-thread-1 sql = COMMIT;
Waiting for threads
Finished all threads
1,Goofy
2,Mickey
3,Tick
4,Truck
5,Track

##### C3 mit Serializability
pool-2-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 2 FOR UPDATE;
Donald
pool-1-thread-1 sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 2;
pool-2-thread-1 sql = SELECT name FROM dissheet3 WHERE id = 4 FOR UPDATE;
Trick
pool-2-thread-1 sql = UPDATE dissheet3 SET name = 'Luigi' WHERE id = 2;
pool-2-thread-1 sql = UPDATE dissheet3 SET name = 'Mario' WHERE id = 4;
pool-2-thread-1 sql = COMMIT;
EXCEPTION in pool-1-thread-1 during operation: UPDATE dissheet3 SET name = 'Mickey' WHERE id = 2;
ROLLBACK: pool-1-thread-1
REPEAT: pool-1-thread-1 sql = UPDATE dissheet3 SET name = 'Mickey' WHERE id = 2;
pool-1-thread-1 sql = UPDATE dissheet3 SET name = 'Truck' WHERE id = 4;
pool-1-thread-1 sql = COMMIT;
Waiting for threads
Finished all threads
1,Goofy
2,Mickey
3,Tick
4,Truck
5,Track