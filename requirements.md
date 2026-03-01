# Personal Finance + UPI Ledger App – Requirements (v2)

## 1. Goal

Build a personal Android app (not for Play Store distribution) to track and reconcile:

* UPI payments
* Card payments
* Friend settlements (credits/debits)
* Refunds
* Subscriptions / auto deductions

Tech Stack:

* Kotlin
* Jetpack Compose (UI)
* Room (SQLite abstraction)

No server-side validation. Fully offline. Optional local sync to laptop server.

Target usage: ~10–20 UPI payments per week + periodic card & settlement activity.

---

# 2. Core Philosophy

## 2.1 First-Class Concepts

Two first-class objects:

1. **Event** → What actually happened in real life (spend, credit, share, subscription, etc.)
2. **Transaction** → What appears in bank / card / UPI log.

These are NOT the same thing.

* Events drive **spend analysis**.
* Transactions drive **bank reconciliation & balance correctness**.

The system must allow linking between them.

---

## 2.2 Source of Truth

* Real-world action → represented by **Event**.
* Bank statement → represented by **Transaction**.
* Final correctness → achieved when all transactions are properly linked to events.

Spend analytics use **Events**.
Bank balance correctness uses **Transactions**.

---

## 2.3 Design Principles

* Persist before launching UPI intent (crash-safe).
* Events must be easy to create immediately.
* Linking can happen later (backfill model).
* Manual resolution is expected and supported.
* All changes must be auditable.
* Stable recovery checkpoints must always exist.

---

# 3. Core Objects

## 3.1 Event (First-Class)

Represents a logical financial action.

Examples:

* Paid ₹500 for dinner
* Friend owes ₹200
* Subscription renewal
* Paid via card
* Paid for friend
* Friend paid for me

Fields (initial):

* id (UUID)
* amount (Long, paise, signed: debit negative, credit positive)
* type (SPEND / CREDIT / SUBSCRIPTION / SETTLEMENT / SHARE / MANUAL)
* description
* category
* createdAt
* logicalFund (optional future)
* isResolved (derived or explicit)

Events are the basis for analytics.

---

## 3.2 Transaction (Bank/Card/UPI Log Entry)

Represents what appears in bank/card system.

Examples:

* UPI debit
* Card swipe
* UPI credit
* Refund
* Settlement credit

Fields:

* id (UUID)
* sourceType (UPI / CARD / BANK_SYNC / MANUAL)
* amount (Long, paise, signed)
* state (LAUNCHED / SUCCESS / FAILED / PENDING / SYNCED)
* externalTxnId (bank txn id / UTR)
* rawResponse
* occurredAt
* syncedAt

Transactions are for reconciliation.

---

## 3.3 Event ↔ Transaction Relationship

Many-to-Many relationship.

Scenarios supported:

* One Event ↔ One Transaction (normal UPI payment)
* Multiple Events ↔ One Transaction (settlement covering multiple spends)
* One Event ↔ Multiple Transactions (partial refunds, split payments)

A join table must exist:

EventTransactionLink:

* id
* eventId
* transactionId
* linkedAmount
* linkedAt
* notes

---

# 4. Workflows

## 4.1 Payment via App (UPI Intent)

Flow:

* Create Event (SPEND)
* Persist Event immediately
* Launch UPI intent
* On SUCCESS → create Transaction (UPI)
* Link Event ↔ Transaction

If app crashes:

* Event already exists
* Transaction may be backfilled manually later

---

## 4.2 Payment via External PSP App

* Create Event manually
* Optional: attach timestamp (current or custom)
* Create Transaction immediately OR later
* Link now or backfill later

---

## 4.3 Card Payment

* Create Event
* Transaction may not exist immediately
* Later during weekly sync:

  * Create Transaction from bank statement
  * Link to Event

---

## 4.4 Friend Settlement

Case A: You pay for friend

* Create Event (SPEND full amount)
* Create sub-event or share logic for friend's portion
* Later when friend pays you:

  * Create CREDIT Event
  * Create Transaction
  * Link appropriately

Case B: Friend pays for you

* Create CREDIT Event
* Link later when settlement transaction appears

Audit may show time difference between credit and resolution — acceptable.

---

## 4.5 Refunds

* Refund is:

  * CREDIT Event
  * Refund Transaction
* Link to original Event(s)
* Sort of like a event deletion

---

## 4.6 Subscription Auto Deductions

* Pre-create recurring Event template
* When deduction appears:

  * Create Transaction
  * Auto-link by amount + description

Option to transfer subscription payment source (UPI → Card).

---

## 4.7 Weekly Bank Sync (Manual Backfill)

Once per week:

* Manually input / import bank statement
* Create Transactions
* Link unmatched Transactions to Events

There should rarely be many unmatched items.

If numbers don’t make sense:

* It means Events or Links are missing.
* User must create missing Event at minimum.

Rule:
Creating Event is mandatory at time of action.
Linking can happen later.

---

# 5. Audit & Integrity

* All changes to Event and Transaction tables must be audited.

* Maintain an AuditLog table:

  * entityType
  * entityId
  * changeType
  * previousState
  * newState
  * changedAt

* Maintain periodic stable checkpoints.

* Recovery must always be possible from local DB.

---

# 6. Funds (First-Class Budget Object)

Funds are first-class objects independent of both Events and Transactions.

Funds represent logical budgeting partitions of money.
They are NOT bank accounts.
They are NOT transactions.
They are internal accounting layers.

Examples:

* Office Travel
* Hobbies
* Free Cash
* Subscriptions
* Investments
* Miscellaneous

Each Event must belong to exactly one Fund.

---

## 6.1 Fund Model

Each Fund has:

* id (UUID)
* name
* monthlyAllocation (Long, paise)
* currentBalance (derived or stored + audited)
* carryForwardEnabled (Boolean)
* isArchived
* createdAt

Fund balance is determined by:

Beginning of month balance

* Monthly allocation
  ± Inter-fund transfers

- Sum of completed Events assigned to the fund

Fund balance is NOT derived from bank Transactions.

---

## 6.2 Monthly Cycle

At start of each month:

* Allocation amount is credited to each active Fund.
* Surplus from previous month is carried forward (configurable per fund).
* Optionally, surplus can move automatically to "Free Cash".

Two possible implementation strategies:

1. Synthetic monthly allocation entries (fund-internal ledger entries)
2. Maintain a running balance and audit deltas

Decision:
Funds maintain their own internal balance model.
Changes to balance must be auditable.

Monthly allocation is NOT a bank transaction.
It is a Fund-level adjustment.

---

## 6.3 Fund Adjustments

Fund balance changes can occur due to:

1. Monthly allocation
2. Event completion (spend or credit)
3. Inter-fund transfer
4. Manual correction (rare, audited)

Inter-fund transfers:

* Reduce balance from Fund A
* Increase balance in Fund B
* Must be atomic
* Must be audited

These are internal ledger movements and not bank transactions.

---

## 6.4 Fund ↔ Event Relationship

Rules:

* Event must reference exactly one Fund.
* Fund balance changes ONLY when Event reaches a fully completed state.
* Pending Events must not affect Fund balance.

Spend analytics per fund are calculated from Events.
Fund balance correctness is based on audited Fund balance changes.

---

## 6.5 Fund History & Flexibility

Funds must support:

* Rename
* Archive
* Monthly allocation change effective from specific month

Historical events must not change when fund settings change.

---

# 7. Analytics

Analytics must use **Events**, not Transactions.

Derived views:

1. Bank Balance → Sum of Transactions
2. Logical Balance → Sum of Events
3. Fund Balance → Allocation - Spend per fund
4. Friend Balance → Net of related Events

Dashboards include:

* Weekly spend
* Monthly spend
* Spend by category
* Spend by fund
* Friend settlement summary
* Unlinked transactions count

Optional:

* Monthly reset
* Automatic surplus transfer to Free Cash

---

# 8. Edge Cases

* Payment done but app crashed → Event exists, Transaction backfilled later.
* Card payment without transaction log → create Event immediately.
* Friend payments both ways + card usage → supported via many-to-many linking.
* Unnamed temporary transaction allowed for emergency capture.

---

# 8. Non-Functional Requirements

* Fully offline.
* Crash-safe.
* Minimal permissions (SMS optional).
* Clear state machine.
* Editable links (not derived fields).
* Compose UI for editing table relationships.

---

# 9. Future Enhancements

* Laptop sync over home WiFi (local server).
* Database encryption.
* Automatic bank statement parsing.
* Advanced dashboards.

---

(We will now move toward formal schema design and state machine definition before coding.)
