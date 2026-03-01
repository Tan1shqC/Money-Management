# Money Manager — Android App

Personal Finance + UPI Ledger app for Android.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Database:** Room (SQLite)

## Project Structure

```
app/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── java/com/moneyapp/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── Event.kt
│   │   │   │   ├── Transaction.kt
│   │   │   │   ├── Fund.kt
│   │   │   │   ├── EventTransactionLink.kt
│   │   │   │   ├── EventDao.kt
│   │   │   │   ├── TransactionDao.kt
│   │   │   │   ├── FundDao.kt
│   │   │   │   └── MoneyDatabase.kt
│   │   │   └── ui/
│   │   │       └── HomeScreen.kt
│   │   ├── res/
│   │   │   └── values/
│   │   │       └── themes.xml
│   │   └── AndroidManifest.xml
│   └── test/
│       └── java/com/moneyapp/ExampleUnitTest.kt

build.gradle.kts
settings.gradle.kts
```

## Build & Run

```bash
./gradlew build
./gradlew installDebug
```

## What's Included

- **Models**: Event, Transaction, Fund, EventTransactionLink
- **Database**: Room setup with DAOs
- **UI**: Basic Compose screen with a button to create events
- **App**: Working MainActivity with Material3 theme

Next steps: expand workflows (payment, settlement, reconciliation), add repositories, and implement full linking logic.

Make Payment
Create Event
Create Transaction
Weekly audit page, Events and Transaction Links view page, filters on fund type, time range etc, sort based on event date, transaction date.
Maybe a minified version of the above for a glance.
Funds page (for analytics with dashboards)