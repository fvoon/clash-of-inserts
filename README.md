# clash-of-inserts

A Spring Boot + PostgreSQL demo to explore how to prevent concurrent inserts that bypass logical constraints.


## 🧠 Problem

In systems where the database must enforce **logical uniqueness** beyond simple unique indexes — for example:

> "Only one ACH fund option is allowed per user for a given bank account number and routing number"

— it's common to implement a `BEFORE INSERT` trigger that checks for existing matching rows and raises an exception if one is found.

However, **concurrent inserts** can still **bypass this logic**, unless the transaction isolation level is set appropriately.


## 🔥 The Race Condition

Imagine two concurrent transactions performing:

```sql
SELECT * FROM fund_option WHERE user_id = 'user123' AND bank_account_number = '2222' AND routing_number = '1111';
-- Both see no results
-- Both proceed to INSERT the same logical fund_option
```

- Even though a trigger checks for an existing row during `BEFORE INSERT`, **both transactions** pass that check because the row hasn't been committed yet.
- Both inserts go through — resulting in **duplicate logical entries**.

This happens under the default isolation level (**Read Committed**) because:
- Each transaction sees only committed data.
- The uncommitted insert from the other transaction is invisible.


## 🛠 Why BEFORE INSERT Triggers Alone Are Not Enough

Triggers are evaluated in the context of **the current transaction's view of the data**.

- If two transactions don't see each other's uncommitted inserts, the trigger condition (e.g., `EXISTS`) fails to catch the race.
- Triggers cannot "see the future" or prevent a race based on invisible actions from other concurrent transactions.


## ✅ The Fix: Serializable Isolation

When using **Serializable isolation**, PostgreSQL ensures:

- The entire transaction behaves as if no other concurrent transaction existed.
- If two transactions read the same "empty" state and both try to insert, PostgreSQL will abort one with a serialization error (`SQLSTATE 40001`). In a Spring Boot application using Hibernate, you may see an error like this:
    ```
  org.springframework.dao.CannotAcquireLockException: Hibernate transaction: Unable to commit against JDBC Connection; ERROR: could not serialize access due to read/write dependencies among transactions
  Detail: Reason code: Canceled on identification as a pivot, during commit attempt.
  Hint: The transaction might succeed if retried.
  ```

This prevents the race and enforces serial equivalence.


## 🧪 What This Project Demonstrates

- How concurrent inserts bypass trigger-based validations.
- Why PostgreSQL's default isolation level isn't enough.
- How Isolation.SERIALIZABLE fixes this issue.


## 📚 Learn More

- [📘 PostgreSQL Serializable Isolation](https://www.postgresql.org/docs/current/transaction-iso.html)
- [🧪 PostgreSQL Concurrency Control](https://www.postgresql.org/docs/current/mvcc-intro.html)
- [🧪 Triggers to enforce constraints in PostgreSQL](https://www.cybertec-postgresql.com/en/triggers-to-enforce-constraints/#what-about-these-“constraint-triggers”)