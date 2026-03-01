package com.moneyapp.repo

import com.moneyapp.data.Event
import com.moneyapp.data.EventDao
import com.moneyapp.data.Transaction
import com.moneyapp.data.TransactionDao
import com.moneyapp.data.LinkingGroup
import com.moneyapp.data.LinkingGroupDao

class ReconciliationRepository(
    private val eventDao: EventDao,
    private val transactionDao: TransactionDao,
    private val linkingGroupDao: LinkingGroupDao
) {

    // Get all events with their remaining amounts
    suspend fun getEventsWithRemaining(): List<EventWithRemaining> {
        val events = eventDao.getAll()
        return events.map { event ->
            val linkedAmount = calculateLinkedAmount(event.id)
            val remaining = event.amount - linkedAmount
            EventWithRemaining(event, remaining, linkedAmount)
        }
    }

    // Get all transactions
    suspend fun getAllTransactions(): List<Transaction> {
        return transactionDao.getAll()
    }

    // Calculate linked amount for an event
    private suspend fun calculateLinkedAmount(eventId: String): Long {
        val group = findGroupByEventId(eventId) ?: return 0L
        val transactionsInGroup = getTransactionsInGroup(group.id)
        return transactionsInGroup.sumOf { it.amount }
    }

    // Find group containing an event
    private suspend fun findGroupByEventId(eventId: String): LinkingGroup? {
        val event = eventDao.getById(eventId) ?: return null
        return if (event.groupId.isNotEmpty()) linkingGroupDao.getById(event.groupId) else null
    }

    // Get all transactions in a group
    private suspend fun getTransactionsInGroup(groupId: String): List<Transaction> {
        return getAllTransactions().filter { it.groupId == groupId }
    }

    // Get all events in a group
    suspend fun getEventsInGroup(groupId: String): List<Event> {
        return eventDao.getAll().filter { it.groupId == groupId }
    }

    // Create new linking group
    suspend fun createLinkingGroup(): LinkingGroup {
        val group = LinkingGroup()
        linkingGroupDao.insert(group)
        return group
    }

    // Link event to transaction via group
    suspend fun linkEventToGroup(event: Event, groupId: String): Event {
        val updated = event.copy(groupId = groupId)
        eventDao.update(updated)
        return updated
    }

    // Link transaction to group
    suspend fun linkTransactionToGroup(transaction: Transaction, groupId: String): Transaction {
        val updated = transaction.copy(groupId = groupId)
        transactionDao.update(updated)
        return updated
    }

    // Unlink event from group
    suspend fun unlinkEvent(event: Event): Event {
        val updated = event.copy(groupId = "")
        eventDao.update(updated)
        return updated
    }

    // Unlink transaction from group
    suspend fun unlinkTransaction(transaction: Transaction): Transaction {
        val updated = transaction.copy(groupId = "")
        transactionDao.update(updated)
        return updated
    }

    // Resolve event (mark as RESOLVED)
    suspend fun resolveEvent(event: Event): Event {
        val updated = event.copy(state = "RESOLVED")
        eventDao.update(updated)
        return updated
    }

    // Check if group is fully linked (sum matches)
    suspend fun isGroupFullyLinked(groupId: String): Boolean {
        val events = getEventsInGroup(groupId)
        val transactions = getTransactionsInGroup(groupId)
        val eventSum = events.sumOf { it.amount }
        val transactionSum = transactions.sumOf { it.amount }
        return eventSum == transactionSum
    }

    // Get unlinked events
    suspend fun getUnlinkedEvents(): List<EventWithRemaining> {
        return getEventsWithRemaining().filter { it.event.groupId.isEmpty() }
    }

    // Get unlinked transactions
    suspend fun getUnlinkedTransactions(): List<Transaction> {
        return getAllTransactions().filter { it.groupId.isEmpty() }
    }

    // Filter by fund
    suspend fun getEventsByFund(fundId: String): List<EventWithRemaining> {
        return getEventsWithRemaining().filter { it.event.fundId == fundId }
    }

    // Filter by state
    suspend fun getEventsByState(state: String): List<EventWithRemaining> {
        return getEventsWithRemaining().filter { it.event.state == state }
    }

    // Filter by date range
    suspend fun getEventsByDateRange(startMs: Long, endMs: Long): List<EventWithRemaining> {
        return getEventsWithRemaining()
            .filter { it.event.createdAt in startMs..endMs }
    }
}

data class EventWithRemaining(
    val event: Event,
    val remaining: Long,
    val linked: Long
)
